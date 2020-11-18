package com.example.petopiaapp.models;

public class QnaPost {
    private String description;
    private String postid;
    private String title;
    private String userId;
    private String userPhoto;

    public QnaPost(String description, String title, String userId, String userPhoto) {
        this.description = description;
        this.title = title;
        this.userId = userId;
        this.userPhoto = userPhoto;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public QnaPost() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }
}
