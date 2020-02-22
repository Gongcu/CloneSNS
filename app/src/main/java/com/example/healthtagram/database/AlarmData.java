package com.example.healthtagram.database;

public class AlarmData {
    private String userid;
    private String uid;
    private String destinationUid;
    private int kind;
    private String message;
    private Long timestamp;

    //like:0   comment:1    follow:2
    public AlarmData() {
    }

    public AlarmData(String userid, String uid, String destinationUid, int kind, String message, Long timestamp) {
        this.userid = userid;
        this.uid = uid;
        this.destinationUid = destinationUid;
        this.kind = kind;
        this.message = message;
        this.timestamp = timestamp;
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
