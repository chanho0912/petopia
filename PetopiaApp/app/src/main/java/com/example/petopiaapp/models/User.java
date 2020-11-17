package com.example.petopiaapp.models;

import java.util.HashMap;
import java.util.Map;

public class User {


    private String email;
    private String username;
    private String UID;
    private String update_list;
    private String friend_list;
    private String qna_list;
    private String imageurl;

    public User(String email, String username, String UID, String imageurl) {
        this.email = email;
        this.username = username;
        this.UID = UID;
        this.imageurl = imageurl;
    }

    public User() {
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("username", username);
        result.put("UID", UID);
        result.put("update_list", update_list);
        result.put("qna_list",qna_list);
        result.put("friend_list",friend_list);
        result.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/petopia-881b9.appspot.com/o/placeholder.png?alt=media&token=d5e67af1-8654-4e40-b7ba-dd6dc769515d");
        return result;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUpdate_list() {
        return update_list;
    }

    public void setUpdate_list(String update_list) {
        this.update_list = update_list;
    }

    public String getFriend_list() {
        return friend_list;
    }

    public void setFriend_list(String friend_list) {
        this.friend_list = friend_list;
    }

    public String getQna_list() {
        return qna_list;
    }

    public void setQna_list(String qna_list) {
        this.qna_list = qna_list;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
