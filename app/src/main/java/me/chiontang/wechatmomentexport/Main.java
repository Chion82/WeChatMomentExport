package me.chiontang.wechatmomentexport;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Main implements IXposedHookLoadPackage {

    Object childA = null;
    boolean intervalStarted = false;
    Tweet currentTweet = new Tweet();
    ArrayList<Tweet> tweetList = new ArrayList<Tweet>();
    JSONArray tweetListJSON = new JSONArray();

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.tencent.mm"))
            return;

        findAndHookMethod("com.tencent.mm.plugin.sns.ui.SnsTimeLineUI", lpparam.classLoader, "a", boolean.class, boolean.class, String.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Hooked. ");
                Object currentObject = param.thisObject;
                for (Field field : currentObject.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = field.get(currentObject);
                    if (field.getName().equals("gLZ")) {
                        XposedBridge.log("Child A found.");
                        childA = value;
                        if (!intervalStarted) {
                            intervalStarted = true;
                            new CheckTimelineInterval().start();
                        }
                    }
                }

            }
        });
    }

    private void getAllTextViews(final View v) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                getAllTextViews(child);
            }
        } else if (v instanceof TextView ) {
            dealWithTextView((TextView)v);
        }
    }

    private void dealWithA() throws Throwable{
        if (childA == null) {
            return;
        }
        for (Field field : childA.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(childA);
            if (field.getName().equals("gyO")) {
                ViewGroup vg = (ListView)value;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    currentTweet.author = "";
                    currentTweet.content = "";
                    currentTweet.likes.clear();
                    currentTweet.comments.clear();
                    View child = vg.getChildAt(i);
                    getAllTextViews(child);
                    addTweetToListNoRepeat();
                }
            }
        }

    }

    class CheckTimelineInterval extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    dealWithA();
                    Thread.sleep(200);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    private void dealWithTextView(TextView v) {
        String className = v.getClass().getName();
        String text = ((TextView)v).getText().toString().trim().replaceAll("\n", " ");
        if (!v.isShown())
            return;
        if (text.equals(""))
            return;

        if (className.equals("com.tencent.mm.plugin.sns.ui.AsyncTextView")) {
            currentTweet.author = text;
        }
        else if (className.equals("com.tencent.mm.plugin.sns.ui.SnsTextView")) {
            currentTweet.content = text;
        }
        else if (className.equals("com.tencent.mm.plugin.sns.ui.MaskTextView")) {
            if (!text.contains(":")) {
                for (String likeUser: text.split(",")){
                    currentTweet.likes.add(likeUser.trim());
                }
            } else {
                String[] commentArray = text.split(":");
                String[] userArray = commentArray[0].trim().split("@");
                String commentAuthor = userArray[0].trim();
                String toUser = "";
                if (userArray.length >= 2) {
                    toUser = userArray[1].trim();
                }
                Comment commentToAdd = new Comment(commentAuthor, commentArray[1].trim(), toUser);
                currentTweet.comments.add(commentToAdd);
            }
        }

    }

    private void addTweetToListNoRepeat() {
        if (currentTweet.author.equals("") && currentTweet.content.equals("")) {
            return;
        }
        boolean flag = true;
        for (int i=0;i<tweetList.size();i++) {
            Tweet loadedTweet = tweetList.get(i);
            if (loadedTweet.author.equals(currentTweet.author) && loadedTweet.content.equals(currentTweet.content)) {
                flag = false;
                break;
            }
        }
        if (flag) {
            Tweet tweetToAdd = currentTweet.clone();
            tweetList.add(tweetToAdd);
            tweetToAdd.print();

            JSONObject tweetJSON = new JSONObject();
            JSONArray commentsJSON = new JSONArray();
            JSONArray likesJSON = new JSONArray();
            try {
                tweetJSON.put("author", currentTweet.author);
                tweetJSON.put("content", currentTweet.content);
                for (int i=0;i<currentTweet.comments.size();i++) {
                    JSONObject commentJSON = new JSONObject();
                    commentJSON.put("author", currentTweet.comments.get(i).author);
                    commentJSON.put("content", currentTweet.comments.get(i).content);
                    commentJSON.put("to_user", currentTweet.comments.get(i).toUser);
                    commentsJSON.put(commentJSON);
                }
                tweetJSON.put("comments", commentsJSON);
                for (int i=0;i<currentTweet.likes.size();i++) {
                    likesJSON.put(currentTweet.likes.get(i));
                }
                tweetJSON.put("likes", likesJSON);

                tweetListJSON.put(tweetJSON);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            File jsonFile = new File(Environment.getExternalStorageDirectory() + "/moments_output.json");
            if (!jsonFile.exists()) {
                try {
                    jsonFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                FileWriter fw = new FileWriter(jsonFile.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(tweetListJSON.toString());
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public class Comment {
        public String author;
        public String content;
        public String toUser;

        public Comment(String author, String content, String toUser) {
            Comment.this.author = author;
            Comment.this.content = content;
            Comment.this.toUser = toUser;
        }
    }

    class Tweet extends Object implements Cloneable {
        public String author = "";
        public String content = "";
        public ArrayList<String> likes = new ArrayList<String>();
        public ArrayList<Comment> comments = new ArrayList<Comment>();

        public void print() {
            XposedBridge.log("================================");
            XposedBridge.log("Author: " + this.author);
            XposedBridge.log("Content: " + this.content);
            XposedBridge.log("Likes:");
            for (int i=0; i<likes.size();i++) {
                XposedBridge.log(likes.get(i));
            }
            XposedBridge.log("Comments:");
            for (int i=0; i<comments.size();i++) {
                Comment comment = comments.get(i);
                XposedBridge.log("CommentAuthor: " + comment.author + "; CommentContent: " + comment.content + "; ToUser: " + comment.toUser);
            }
            XposedBridge.log("Moments count: " + tweetList.size());
        }

        public Tweet clone() {
            try {
                return (Tweet) (super.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
