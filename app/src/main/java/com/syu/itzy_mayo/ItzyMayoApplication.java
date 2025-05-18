package com.syu.itzy_mayo;


import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.naver.maps.map.NaverMapSdk;

public class ItzyMayoApplication extends Application {
    private static ItzyMayoApplication instance;
    private FirebaseAuth firebaseAuth;
    private UserSessionManager sessionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = UserSessionManager.getInstance(this);
        sessionManager.setFirebaseAuth(firebaseAuth);

        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    sessionManager.createLoginSession(user);
                } else {
                    sessionManager.logoutUser();
                }
            }
        });
        // 네이버 지도 SDK 초기화
        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NcpKeyClient(BuildConfig.NCP_CLIENT_ID));
    }
    public static ItzyMayoApplication getInstance() {
        return instance;
    }
    public UserSessionManager getSessionManager() {
        return sessionManager;
    }
    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

}