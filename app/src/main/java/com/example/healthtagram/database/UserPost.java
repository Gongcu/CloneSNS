package com.example.healthtagram.database;

import java.util.HashMap;
import java.util.Map;

public class UserPost {
    private String photo;
    private String text;
    private Long timestamp;
    private String uid;  //getUid()
    private String userId; //유저 이미지 관리
    private int favoriteCount;  //좋아요 수
    private Map<String, Boolean> favorites; //좋아요 중복 방지

    class Comment{ //댓글 관리 클래스
        private String uid;
        private String userId;
        private String comment;
        private String date;

        public Comment(String uid, String userId, String comment, String date) {
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

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    public UserPost(){}

    public UserPost(String photo, String text, Long timestamp) {
        this.photo = photo;
        this.text = text;
        this.timestamp = timestamp;
    }

    public UserPost(String photo, String text, Long timestamp, String uid, String userId) {
        this.photo = photo;
        this.text = text;
        this.timestamp = timestamp;
        this.uid = uid;
        this.userId = userId;
        favoriteCount=0;
        favorites= new HashMap<>();
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

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Map<String, Boolean> getFavorites() {
        return favorites;
    }

    public void setFavorites(Map<String, Boolean> favorites) {
        this.favorites = favorites;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setDate(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}