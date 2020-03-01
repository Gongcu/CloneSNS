package com.example.healthtagram.database;

public class AlarmData {
    private String userid;
    private String uid;
    private String username;
    private String userProfile;
    private String destinationUid;
    private String postFileName;
    private int kind;
    private String message;
    private Long timestamp;

    //like:0   comment:1    follow:2
    public AlarmData() {
    }

    public AlarmData(String userid, String uid,String username,String userProfile, String destinationUid, int kind, String message, Long timestamp, String postFileName) {
        this.userid = userid;
        this.uid = uid;
        this.destinationUid = destinationUid;
        this.kind = kind;
        this.message = message;
        this.timestamp = timestamp;
        this.postFileName = postFileName;
        this.username=username;
        this.userProfile=userProfile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public String getPostFileName() {
        return postFileName;
    }

    public void setPostFileName(String postFileName) {
        this.postFileName = postFileName;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDestinationUid() {
        return destinationUid;
    }

    public void setDestinationUid(String destinationUid) {
        this.destinationUid = destinationUid;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
