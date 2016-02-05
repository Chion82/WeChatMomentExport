package me.chiontang.wechatmomentexport;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import me.chiontang.wechatmomentexport.models.Comment;
import me.chiontang.wechatmomentexport.models.Like;
import me.chiontang.wechatmomentexport.models.Tweet;
import me.chiontang.wechatmomentexport.models.User;

public class Main implements IXposedHookLoadPackage {

    Tweet currentTweet = new Tweet();
    ArrayList<Tweet> tweetList = new ArrayList<Tweet>();
    String lastTimelineId = "";
    Thread intervalSaveThread = null;
    Context appContext = null;
    String wechatVersion = "";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.tencent.mm"))
            return;

        loadFromSharedPreference();

        findAndHookMethod("com.tencent.mm.ui.LauncherUI", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (appContext != null) {
                    return;
                }
                XposedBridge.log("LauncherUI hooked.");
                appContext = ((Activity)param.thisObject).getApplicationContext();
                PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(lpparam.packageName, 0);
                if (pInfo != null)
                    wechatVersion = pInfo.versionName;
                XposedBridge.log("WeChat version=" + wechatVersion);
                Config.checkWeChatVersion(wechatVersion);

                if (!Config.ready) {
                    return;
                }

                hookMethods(lpparam);
            }
        });

    }

    private void hookMethods(final LoadPackageParam lpparam) {
        findAndHookMethod(Config.PROTOCAL_SNS_DETAIL_CLASS, lpparam.classLoader, Config.PROTOCAL_SNS_DETAIL_METHOD, int.class, Object[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                loadFromSharedPreference();
                if (!Config.enabled || !Config.ready) {
                    intervalSaveThread = null;
                    tweetList.clear();
                    return;
                }
                Class atp = XposedHelpers.findClass(Config.PROTOCAL_SNS_DETAIL_CLASS, lpparam.classLoader);
                Class Parser = XposedHelpers.findClass(Config.SNS_XML_GENERATOR_CLASS, lpparam.classLoader);
                Method parseMethod = Parser.getMethod(Config.SNS_XML_GENERATOR_METHOD, atp);
                try {
                    String result = (String) parseMethod.invoke(this, param.thisObject);
                    if (!getTimelineId(result).equals(lastTimelineId))
                        currentTweet.clear();
                    parseTimelineXML(result);
                    addTweetToListNoRepeat();
                    lastTimelineId = getTimelineId(result);
                } catch (Exception e) {

                }
                if (intervalSaveThread == null) {
                    intervalSaveThread = new IntervalThread(tweetList);
                    intervalSaveThread.start();
                }
            }
        });

        findAndHookMethod(Config.PROTOCAL_SNS_OBJECT_CLASS, lpparam.classLoader, Config.PROTOCAL_SNS_OBJECT_METHOD, int.class, Object[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                loadFromSharedPreference();
                if (!Config.enabled || !Config.ready) {
                    tweetList.clear();
                    return;
                }
                Object aqiObject = param.thisObject;
                parseSnsObject(aqiObject);
            }
        });
    }

    private void loadFromSharedPreference() {
        XSharedPreferences pref = new XSharedPreferences(Main.class.getPackage().getName(), "config");
        pref.makeWorldReadable();
        pref.reload();
        Config.enabled = pref.getBoolean("enabled", false);
        Config.outputFile = pref.getString("outputFile", Environment.getExternalStorageDirectory() + "/moments_output.json");
    }

    private String getTimelineId(String xmlResult) {
        Pattern idPattern = Pattern.compile("<id><!\\[CDATA\\[(.+?)\\]\\]></id>");
        Matcher idMatcher = idPattern.matcher(xmlResult);
        if (idMatcher.find()) {
            return idMatcher.group(1);
        } else {
            return "";
        }
    }

    private void parseTimelineXML(String xmlResult) throws Throwable {
        Pattern userIdPattern = Pattern.compile("<username><!\\[CDATA\\[(.+?)\\]\\]></username>");
        Pattern contentPattern = Pattern.compile("<contentDesc><!\\[CDATA\\[(.+?)\\]\\]></contentDesc>", Pattern.DOTALL);
        Pattern mediaPattern = Pattern.compile("<media>.*?<url.*?><!\\[CDATA\\[(.+?)\\]\\]></url>.*?</media>");
        Pattern timestampPattern = Pattern.compile("<createTime><!\\[CDATA\\[(.+?)\\]\\]></createTime>");

        Matcher userIdMatcher = userIdPattern.matcher(xmlResult);
        Matcher contentMatcher = contentPattern.matcher(xmlResult);
        Matcher mediaMatcher = mediaPattern.matcher(xmlResult);
        Matcher timestampMatcher = timestampPattern.matcher(xmlResult);

        currentTweet.id = getTimelineId(xmlResult);

        currentTweet.rawXML = xmlResult;

        if (timestampMatcher.find()) {
            currentTweet.timestamp = Integer.parseInt(timestampMatcher.group(1));
        }

        if (userIdMatcher.find()) {
            currentTweet.authorId = userIdMatcher.group(1);
        }

        if (contentMatcher.find()) {
            currentTweet.content = contentMatcher.group(1);
        }

        while (mediaMatcher.find()) {
            boolean flag = true;
            for (int i=0;i<currentTweet.mediaList.size();i++) {
                if (currentTweet.mediaList.get(i).equals(mediaMatcher.group(1))) {
                    flag = false;
                    break;
                }
            }
            if (flag)
                currentTweet.mediaList.add(mediaMatcher.group(1));
        }

    }

    private void parseSnsObject(Object aqiObject) throws Throwable{
        Tweet matchTweet = null;
        Field field = null;
        Object userId=null, nickname=null;

        field = aqiObject.getClass().getField(Config.PROTOCAL_SNS_OBJECT_USERID_FIELD);
        userId = field.get(aqiObject);

        field = aqiObject.getClass().getField(Config.PROTOCAL_SNS_OBJECT_NICKNAME_FIELD);
        nickname = field.get(aqiObject);

        field = aqiObject.getClass().getField(Config.PROTOCAL_SNS_OBJECT_TIMESTAMP_FIELD);
        long snsTimestamp = ((Integer) field.get(aqiObject)).longValue();

        if (userId == null || nickname == null) {
            return;
        }

        for (int i=0;i<tweetList.size();i++) {
            Tweet tweet = tweetList.get(i);
            if (tweet.timestamp == snsTimestamp && tweet.authorId.equals((String)userId)) {
                matchTweet = tweet;
                break;
            }
        }

        if (matchTweet == null) {
            return;
        }

        matchTweet.ready = true;
        matchTweet.author = (String)nickname;
        field = aqiObject.getClass().getField(Config.PROTOCAL_SNS_OBJECT_COMMENTS_FIELD);
        LinkedList list = (LinkedList)field.get(aqiObject);
        for (int i=0;i<list.size();i++) {
            Object childObject = list.get(i);
            parseSnsObjectExt(childObject, true, matchTweet);
        }

        field = aqiObject.getClass().getField(Config.PROTOCAL_SNS_OBJECT_LIKES_FIELD);
        LinkedList likeList = (LinkedList)field.get(aqiObject);
        for (int i=0;i<likeList.size();i++) {
            Object likeObject = likeList.get(i);
            parseSnsObjectExt(likeObject, false, matchTweet);
        }
        matchTweet.print();

    }

    private void parseSnsObjectExt(Object apzObject, boolean isComment, Tweet matchTweet) throws Throwable {
        if (isComment) {
            Field field = apzObject.getClass().getField(Config.SNS_OBJECT_EXT_AUTHOR_NAME_FIELD);
            Object authorName = field.get(apzObject);

            field = apzObject.getClass().getField(Config.SNS_OBJECT_EXT_REPLY_TO_FIELD);
            Object replyToUserId = field.get(apzObject);

            field = apzObject.getClass().getField(Config.SNS_OBJECT_EXT_COMMENT_FIELD);
            Object commentContent = field.get(apzObject);

            field = apzObject.getClass().getField(Config.SNS_OBJECT_EXT_AUTHOR_ID_FIELD);
            Object authorId = field.get(apzObject);

            if (authorId == null || commentContent == null || authorName == null) {
                return;
            }

            for (int i = 0; i < matchTweet.comments.size(); i++) {
                Comment loadedComment = matchTweet.comments.get(i);
                if (loadedComment.authorId.equals((String) authorId) && loadedComment.content.equals((String) commentContent)) {
                    return;
                }
            }

            Comment newComment = new Comment();
            newComment.authorName = (String) authorName;
            newComment.content = (String) commentContent;
            newComment.authorId = (String) authorId;
            newComment.toUserId = (String) replyToUserId;

            for (int i = 0; i < matchTweet.comments.size(); i++) {
                Comment loadedComment = matchTweet.comments.get(i);
                if (replyToUserId != null && loadedComment.authorId.equals((String) replyToUserId)) {
                    newComment.toUser = loadedComment.authorName;
                    break;
                }
            }

            matchTweet.comments.add(newComment);
        } else {
            Field field = apzObject.getClass().getField(Config.SNS_OBJECT_EXT_AUTHOR_NAME_FIELD);
            Object nickname = field.get(apzObject);
            field = apzObject.getClass().getField(Config.SNS_OBJECT_EXT_AUTHOR_ID_FIELD);
            Object userId = field.get(apzObject);
            if (nickname == null || userId == null) {
                return;
            }

            if (((String)userId).equals("")) {
                return;
            }
            for (int i = 0; i < matchTweet.likes.size(); i++) {
                if (matchTweet.likes.get(i).userId.equals((String)userId)) {
                    return;
                }
            }
            Like newLike = new Like();
            newLike.userId = (String)userId;
            newLike.userName = (String)nickname;
            matchTweet.likes.add(newLike);
        }
    }

    private void addTweetToListNoRepeat() {
        if (currentTweet.id.equals("")) {
            return;
        }
        int replaceIndex = -1;
        for (int i=0;i<tweetList.size();i++) {
            Tweet loadedTweet = tweetList.get(i);
            if (loadedTweet.id.equals(currentTweet.id)) {
                replaceIndex = i;
                break;
            }
        }

        Tweet tweetToAdd = currentTweet.clone();
        if (replaceIndex == -1) {
            tweetList.add(tweetToAdd);
        } else {
            tweetList.remove(replaceIndex);
            tweetList.add(replaceIndex, tweetToAdd);
        }

    }

}
