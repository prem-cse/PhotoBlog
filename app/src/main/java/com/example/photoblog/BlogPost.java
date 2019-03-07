package com.example.photoblog;


import java.sql.Timestamp;
import java.util.Date;

public class BlogPost extends PostId{
private String Desc, postImage,thumb,user;
private Timestamp time;

    public BlogPost(){}

    public BlogPost(String desc, String image, String thumb, Timestamp time, String user) {
        Desc = desc;
        this.postImage = image;
        this.thumb = thumb;
        this.time = time;
        this.user = user;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public Timestamp getTimestamp() {
        return time;
    }

    public void setTimestamp(Timestamp time) {
        this.time = time;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}