package me.chiontang.wechatmomentexport.models;

/**
 * Created by chiontang on 2/4/16.
 */
public class Comment {
    public String authorName;
    public String content;
    public String toUser;
    public String authorId;

    public Comment(String authorId, String authorName, String content, String toUser) {
        Comment.this.authorId = authorId;
        Comment.this.authorName = authorName;
        Comment.this.content = content;
        Comment.this.toUser = toUser;
    }

    public Comment() {

    }
}