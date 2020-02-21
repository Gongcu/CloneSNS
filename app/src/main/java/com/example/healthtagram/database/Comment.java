package com.example.healthtagram.database;

public class Comment{ //댓글 관리 클래스
    private String uid;
    private String userId;
    private String comment;
    private Long date;

    public Comment(){}

    public Comment(String uid, String userId, String comment, Long date) {
        this.uid = uid;
        this.userId = userId;
        this.comment = comment;
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}