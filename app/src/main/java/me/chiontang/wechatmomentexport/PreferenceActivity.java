package me.chiontang.wechatmomentexport;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PreferenceActivity extends AppCompatActivity {

    EditText outputFileEditText = null;
    Button toggleInterceptButton = null;
    TextView wechatStatusTextView = null;
    TextView runningStatusTextView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        outputFileEditText = (EditText)findViewById(R.id.output_file_editText);
        toggleInterceptButton = (Button)findViewById(R.id.toggle_intercept_button);
        wechatStatusTextView = (TextView)findViewById(R.id.wechat_status_textview);
        runningStatusTextView = (TextView)findViewById(R.id.running_status_textview);
        toggleInterceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAndSaveConfig();
                loadConfig();
            }
        });
        testRoot();
    }

    protected void testRoot() {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            Toast.makeText(this, R.string.no_root, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            Log.e("wechatmomentexport", e.getMessage());
        }
    }

    public int findPIDbyPackageName(String packagename) {
        int result = -1;
        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        if (am != null) {
            for (ActivityManager.RunningAppProcessInfo pi : am.getRunningAppProcesses()){
                if (pi.processName.equalsIgnoreCase(packagename)) {
                    result = pi.pid;
                }
                if (result != -1) break;
            }
        } else {
            result = -1;
        }

        return result;
    }

    protected void clearWeChatCache() {
        int weChatPid = findPIDbyPackageName("com.tencent.mm");
        Log.d("wechatmomentexport", "" + weChatPid);
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("kill " + weChatPid + "\n");
            os.writeBytes("cd /data/data/com.tencent.mm/MicroMsg\n");
            os.writeBytes("ls | while read line; do rm ${line}/SnsMicroMsg.db; done\n");
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            Toast.makeText(this, R.string.no_root, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            Log.e("wechatmomentexport", e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkWeChatVersion();
        loadConfig();
        if (Config.enabled) {
            checkOutputFile();
        }
    }

    private void toggleAndSaveConfig() {
        Config.enabled = !Config.enabled;
        Config.outputFile = outputFileEditText.getText().toString();
        SharedPreferences pref = getSharedPreferences("config", Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("enabled", Config.enabled);
        editor.putString("outputFile", Config.outputFile);
        editor.apply();
        if (Config.enabled) {
            clearWeChatCache();
            Toast.makeText(this, R.string.start_intercept, Toast.LENGTH_SHORT).show();
            runningStatusTextView.setText(getString(R.string.started));
        } else {
            Toast.makeText(this, R.string.stop_intercept, Toast.LENGTH_SHORT).show();
            runningStatusTextView.setText(runningStatusTextView.getText() + " " + getString(R.string.stopped));
        }
    }

    private void loadConfig() {
        SharedPreferences pref = getSharedPreferences("config", Context.MODE_WORLD_READABLE);
        Config.enabled = pref.getBoolean("enabled", false);
        Config.outputFile = pref.getString("outputFile", Environment.getExternalStorageDirectory() + "/moments_output.json");
        if (Config.enabled) {
            toggleInterceptButton.setText(R.string.stop_intercept);
        } else {
            toggleInterceptButton.setText(R.string.start_intercept);
        }
        outputFileEditText.setText(Config.outputFile);
    }

    private void checkWeChatVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo("com.tencent.mm", 0);
            Config.checkWeChatVersion(pInfo.versionName);
            String supportedVersions = TextUtils.join(", ", Config.VERSIONS);
            if (Config.ready) {
                wechatStatusTextView.setText(getString(R.string.wechat_version_label) + ": " + pInfo.versionName + "\n" + getString(R.string.wechat_version_supported));
                toggleInterceptButton.setEnabled(true);
            } else {
                wechatStatusTextView.setText(getString(R.string.wechat_version_label) + ": " + pInfo.versionName + "\n" + getString(R.string.wechat_version_unsupported));
                wechatStatusTextView.setText(wechatStatusTextView.getText() + "\n" + getString(R.string.supported_wechat_versions) + ": " + supportedVersions);
                toggleInterceptButton.setEnabled(false);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            wechatStatusTextView.setText(R.string.wechat_not_found);
            toggleInterceptButton.setEnabled(false);
        }
    }

    private void checkOutputFile() {
        File outputFile = new File(Config.outputFile);
        if (!outputFile.exists()) {
            return;
        }
        try {
            FileReader fr = new FileReader(outputFile.getAbsoluteFile());
            BufferedReader br = new BufferedReader(fr);
            String rawString = "";
            String line = "";
            while ((line = br.readLine())!=null) {
                rawString += line;
            }
            JSONArray parsedJSON = new JSONArray(rawString);
            int snsCaptured = parsedJSON.length();
            runningStatusTextView.setText(getString(R.string.sns_captured) + ": " + snsCaptured);
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
