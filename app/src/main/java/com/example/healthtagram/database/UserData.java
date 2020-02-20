package com.example.healthtagram.database;

import java.util.HashMap;
import java.util.Map;

public class UserData {
    private String userName;
    private String profile;
    private String bio;
    private Map<String, Boolean> follow; //팔로우 중복 방지
    private int following_count;
    private int follower_count;

    public UserData(){}

    public UserData(String userName, String profile, String bio) {
        this.userName = userName;
        this.profile = profile;
        this.bio = bio;
        this.follower_count=0;
        this.following_count=0;
        this.follow=new HashMap<>();
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

    public Map<String, Boolean> getFollow() {
        return follow;
    }

    public void setFollow(Map<String, Boolean> follow) {
        this.follow = follow;
    }

    public int getFollowing_count() {
        return following_count;
    }

    public void setFollowing_count(int following_count) {
        this.following_count = following_count;
    }

    public int getFollower_count() {
        return follower_count;
    }

    public void setFollower_count(int follower_count) {
        this.follower_count = follower_count;
    }
}