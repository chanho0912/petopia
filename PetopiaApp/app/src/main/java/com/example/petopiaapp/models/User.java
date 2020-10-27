package com.example.petopiaapp.models;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String user_id;
    public String pw;
    public String nickname;
    public String UID;
    public String update_list;
    public String friend_list;
    public String qna_list;
    public String filter;


    public User(String user_id, String pw, String nickname, String UID) {
        this.user_id=user_id;
        this.pw=pw;
        this.nickname=nickname;
        this.UID=UID;
        this.update_list= "@"; // initial value : discussion needed
        this.qna_list="@";  // Default value: discussion needed
        this.friend_list="@";//initial value: discussion needed
        // category_id, filter_list는 SurveyActivity에서 child 추가
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("user_id", user_id);
        result.put("pw", pw);
        result.put("nickname", nickname);
        result.put("UID", UID);
        result.put("update_list", update_list);
        result.put("qna_list",qna_list);
        result.put("friend_list",friend_list);
        return result;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getPw() {
        return pw;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUID() {
        return UID;
    }

    public String getUpdate_list() {
        return update_list;
    }

    public String getQna_list(){return qna_list;}

    public String getFilter() {
        return filter;
    }

    public String getFriend_list(){return friend_list;}
}
