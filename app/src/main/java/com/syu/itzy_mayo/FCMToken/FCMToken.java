package com.syu.itzy_mayo.FCMToken;

import com.google.firebase.firestore.DocumentId;

public class FCMToken {
    @DocumentId
    private String id;
    private String token;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public FCMToken(String id, String token) {
        this.id = id;
        this.token = token;
    }

    public FCMToken() {
    }
}
