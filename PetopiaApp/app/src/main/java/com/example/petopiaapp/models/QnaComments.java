package com.example.petopiaapp.models;

public class QnaComments {

    private String comment;
    private String publisher;

    public QnaComments(String comment, String publisher) {
        this.comment = comment;
        this.publisher = publisher;
    }

    public QnaComments() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
