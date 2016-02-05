package me.chiontang.wechatmomentexport;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import de.robv.android.xposed.XposedBridge;
import me.chiontang.wechatmomentexport.models.Tweet;

/**
 * Created by chiontang on 2/4/16.
 */
public class IntervalThread extends Thread {

    ArrayList<Tweet> tweetList;

    IntervalThread(ArrayList<Tweet> tweetList) {
        this.tweetList = tweetList;
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!Config.enabled || !Config.ready) {
                return;
            }
            saveToFile();
        }
    }

    private void saveToFile() {
        JSONArray tweetListJSON = new JSONArray();

        for (int tweetIndex=0; tweetIndex<tweetList.size(); tweetIndex++) {
            Tweet currentTweet = tweetList.get(tweetIndex);
            if (!currentTweet.ready) {
                continue;
            }
            JSONObject tweetJSON = new JSONObject();
            JSONArray commentsJSON = new JSONArray();
            JSONArray likesJSON = new JSONArray();
            JSONArray mediaListJSON = new JSONArray();
            try {
                tweetJSON.put("snsId", currentTweet.id);
                tweetJSON.put("authorName", currentTweet.author);
                tweetJSON.put("authorId", currentTweet.authorId);
                tweetJSON.put("content", currentTweet.content);
                for (int i = 0; i < currentTweet.comments.size(); i++) {
                    JSONObject commentJSON = new JSONObject();
                    commentJSON.put("authorName", currentTweet.comments.get(i).authorName);
                    commentJSON.put("authorId", currentTweet.comments.get(i).authorId);
                    commentJSON.put("content", currentTweet.comments.get(i).content);
                    commentJSON.put("toUserName", currentTweet.comments.get(i).toUser);
                    commentJSON.put("toUserId", currentTweet.comments.get(i).toUserId);
                    commentsJSON.put(commentJSON);
                }
                tweetJSON.put("comments", commentsJSON);
                for (int i = 0; i < currentTweet.likes.size(); i++) {
                    JSONObject likeJSON = new JSONObject();
                    likeJSON.put("userName", currentTweet.likes.get(i).userName);
                    likeJSON.put("userId", currentTweet.likes.get(i).userId);
                    likesJSON.put(likeJSON);
                }
                tweetJSON.put("likes", likesJSON);
                for (int i = 0; i < currentTweet.mediaList.size(); i++) {
                    mediaListJSON.put(currentTweet.mediaList.get(i));
                }
                tweetJSON.put("mediaList", mediaListJSON);
                tweetJSON.put("rawXML", currentTweet.rawXML);
                tweetJSON.put("timestamp", currentTweet.timestamp);

                tweetListJSON.put(tweetJSON);

            } catch (Exception exception) {
                XposedBridge.log(exception.getMessage());
            }
        }

        File jsonFile = new File(Config.outputFile);
        if (!jsonFile.exists()) {
            try {
                jsonFile.createNewFile();
            } catch (IOException e) {
                XposedBridge.log(e.getMessage());
            }
        }

        try {
            FileWriter fw = new FileWriter(jsonFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(tweetListJSON.toString());
            bw.close();
        } catch (IOException e) {
            XposedBridge.log(e.getMessage());
        }
    }
}
