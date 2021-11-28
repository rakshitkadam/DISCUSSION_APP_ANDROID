package com.stabstudio.discussionapp.Models;

import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Discussion {

    private String id;
    private String place_id;
    private String user_id;
    private String subject;
    private String visibleToID;
    private String content;
    private String timestamp;
    private int likes;
    private int comments;

    public Discussion(){
    }

    public Discussion(String id, String place_id, String user_id, String subject, String visibleTo, String content, String timestamp, int likes, int comments) {
        this.id = id;
        this.place_id = place_id;
        this.user_id = user_id;
        this.subject = subject;
        this.visibleToID = visibleTo;
        this.content = content;
        this.timestamp = timestamp;
        this.likes = likes;
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public String getVisibleToID() {
        return visibleToID;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public void incrementLike(){
        this.likes++;
    }

    public void decrementLike(){
        this.likes--;
    }
}
