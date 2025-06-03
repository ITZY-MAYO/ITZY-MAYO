package com.syu.itzy_mayo.location;

public class LocationData {
    String firebase_userid;
    double latitude;
    double longitude;

    public LocationData(String firebase_userid, double latitude, double longitude) {
        this.firebase_userid = firebase_userid;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String toJson() {
        // Produces a compact JSON string: {"firebase_userid": "id", "latitude": 0.0, "longitude": 0.0}
        return String.format("{\"firebase_userid\": \"%s\", \"latitude\": %f, \"longitude\": %f}",
                             firebase_userid, latitude, longitude);
    }
} 