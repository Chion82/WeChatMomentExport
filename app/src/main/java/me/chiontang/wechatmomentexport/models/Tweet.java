package me.chiontang.wechatmomentexport.models;

import java.util.ArrayList;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by chiontang on 2/4/16.
 */
public class Tweet implements Cloneable {
    public String id = "";
    public String author = "";
    public String content = "";
    public String authorId = "";
    public ArrayList<Like> likes = new ArrayList<Like>();
    public ArrayList<Comment> comments = new ArrayList<Comment>();
    public ArrayList<String> mediaList = new ArrayList<String>();
    public String rawXML = "";
    public long timestamp = 0;
    public boolean ready = false;

    public void print() {
        XposedBridge.log("================================");
        XposedBridge.log("id: " + this.id);
        XposedBridge.log("Author: " + this.author);
        XposedBridge.log("Content: " + this.content);
        XposedBridge.log("Likes:");
        for (int i=0; i<likes.size();i++) {
            XposedBridge.log(likes.get(i).userName);
        }
        XposedBridge.log("Comments:");
        for (int i=0; i<comments.size();i++) {
            Comment comment = comments.get(i);
            XposedBridge.log("CommentAuthor: " + comment.authorName + "; CommentContent: " + comment.content + "; ToUser: " + comment.toUser);
        }
        XposedBridge.log("Media List:");
        for (int i=0;i<mediaList.size();i++) {
            XposedBridge.log(mediaList.get(i));
        }
    }

    public Tweet clone() {
        Tweet newTweet = new Tweet();
        newTweet.id = this.id;
        newTweet.author = this.author;
        newTweet.content = this.content;
        newTweet.authorId = this.authorId;
        newTweet.likes = new ArrayList<Like>(this.likes);
        newTweet.comments = new ArrayList<Comment>(this.comments);
        newTweet.mediaList = new ArrayList<String>(this.mediaList);
        newTweet.rawXML = this.rawXML;
        newTweet.timestamp = this.timestamp;
        return newTweet;
    }

    public void clear() {
        id = "";
        author = "";
        content = "";
        authorId = "";
        likes.clear();
        comments.clear();
        mediaList.clear();
        rawXML = "";
    }
}
