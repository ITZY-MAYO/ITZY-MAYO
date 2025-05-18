package com.syu.itzy_mayo.Schedule;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;


public class Schedule {
    private String title;
    private String content;
    private String userId;
    private GeoPoint geoPoint;
    private Timestamp datetime;

    public Schedule() {}

    public Schedule(String title, String content, String userId, GeoPoint geoPoint, Timestamp datetime) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.geoPoint = geoPoint;
        this.datetime = datetime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Timestamp getDatetime() {
        return datetime;
    }

    public void setDatetime(Timestamp datetime) {
        this.datetime = datetime;
    }
}
