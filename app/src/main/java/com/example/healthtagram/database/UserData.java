package com.example.healthtagram.database;

public class UserData {
    private String userName;
    private String profile;
    private String bio;

    public UserData(){}

    public UserData(String userName, String profile, String bio) {
        this.userName = userName;
        this.profile = profile;
        this.bio = bio;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUserName() {
        return userName;

    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getProfile() {
        return profile;
    }


    public void setProfile(String profile) {
        this.profile = profile;
    }

}