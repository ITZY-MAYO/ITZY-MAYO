package com.syu.itzy_mayo.Schedule;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;


public class Schedule {
    @DocumentId
    private String id;
    private String title;
    private String content;
    private String userId;
    private GeoPoint geoPoint;
    private Timestamp datetime;
    private String address;

    public Schedule() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public Schedule(String title, String content, String userId, GeoPoint geoPoint, Timestamp datetime, String address) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.geoPoint = geoPoint;
        this.datetime = datetime;
        this.address = address;
    }

    public Schedule(String id, String title, String content, String userId, GeoPoint geoPoint, Timestamp datetime, String address) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.geoPoint = geoPoint;
        this.datetime = datetime;
        this.address = address;
    }
}
