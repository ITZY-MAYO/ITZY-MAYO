package com.syu.itzy_mayo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserSessionManager {
    private static final String TAG = "UserSessionManager";
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    
    private static UserSessionManager instance;
    private final SharedPreferences.Editor editor;
    private FirebaseAuth firebaseAuth;
    private final List<AuthStateObserver> observers = new ArrayList<>();

    private UserSessionManager(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    public static synchronized UserSessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserSessionManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void createLoginSession(FirebaseUser user) {
        if (user != null) {
            editor.putBoolean(KEY_IS_LOGGED_IN, true)
                    .putString(KEY_USER_ID, user.getUid())
                    .putString(KEY_USER_EMAIL, user.getEmail())
                    .apply();
            notifyAuthStateChanged(user);
            Log.d(TAG, "로그인 세션 생성: " + user.getEmail());
        }
    }
    
    public boolean isLoggedIn() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null;
    }
    
public void logoutUser() {
    if (isLoggedIn()) {
        firebaseAuth.signOut();
    }
    // 저장된 사용자 데이터 삭제
    editor.clear();
    editor.apply();
    Log.d(TAG, "사용자 로그아웃");
    notifyAuthStateChanged(null);
}
    
    public String getUserEmail() {
        if (isLoggedIn()) {
            return Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail();
        }
        return null;
    }
    
    public String getUserId() {
        if (isLoggedIn()) {
            return Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        }
        return null;
    }
    public void setFirebaseAuth(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }
    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }
    public void addObserver(AuthStateObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    public void removeObserver(AuthStateObserver observer) {
        observers.remove(observer);
    }
    private void notifyAuthStateChanged(FirebaseUser user) {
        for (AuthStateObserver observer : observers) {
            observer.onAuthStateChanged(user);
        }
    }
} 