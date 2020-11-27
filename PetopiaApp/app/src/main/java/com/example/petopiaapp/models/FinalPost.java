package com.example.petopiaapp.models;

import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class FinalPost {
    private String description;
    private String postid;
    private String publisher;
    private String title;

    FinalPost() { }

    public FinalPost(String description, String postid, String publisher, String title) {
        this.description = description;
        this.postid = postid;
        this.publisher = publisher;
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
