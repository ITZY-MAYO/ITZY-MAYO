package com.syu.itzy_mayo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.syu.itzy_mayo.FCMToken.FCMToken;
import com.syu.itzy_mayo.location.LocationUploaderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
    private Context appContext;

    private UserSessionManager(Context context) {
        this.appContext = context.getApplicationContext();
        SharedPreferences pref = this.appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    public static synchronized UserSessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserSessionManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private void startLocationService() {
        if (appContext == null) {
            Log.e(TAG, "Context is null, cannot start LocationUploaderService.");
            return;
        }
        Intent serviceIntent = new Intent(appContext, LocationUploaderService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(serviceIntent);
            Log.d(TAG, "Starting LocationUploaderService in foreground mode.");
        } else {
            appContext.startService(serviceIntent);
            Log.d(TAG, "Starting LocationUploaderService in background mode.");
        }
    }

    private void stopLocationService() {
        if (appContext == null) {
            Log.e(TAG, "Context is null, cannot stop LocationUploaderService.");
            return;
        }
        Intent serviceIntent = new Intent(appContext, LocationUploaderService.class);
        appContext.stopService(serviceIntent);
        Log.d(TAG, "Stopping LocationUploaderService.");
    }
    
    public void createLoginSession(FirebaseUser user) {
        if (user != null) {
            editor.putBoolean(KEY_IS_LOGGED_IN, true)
                    .putString(KEY_USER_ID, user.getUid())
                    .putString(KEY_USER_EMAIL, user.getEmail())
                    .apply();
            notifyAuthStateChanged(user);
            Log.d(TAG, "로그인 세션 생성: " + user.getEmail());

            startLocationService();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
                DocumentReference docRef = db.collection("fcm_token").document(user.getUid());
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()) {
                            FCMToken fcmToken = documentSnapshot.toObject(FCMToken.class);
                            if(!Objects.equals(fcmToken.getToken(), s)) {
                                Log.i("USM", "Updating Token");
                                docRef.update("token", s).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }

                        } else {
                            FCMToken fcmToken = new FCMToken(user.getUid(), s);
                            Log.i("USM", "Adding Token");
                            docRef.set(fcmToken).addOnSuccessListener(documentReference -> {
                            });
                        }

                    }
                }).addOnFailureListener(e -> {

                }).addOnFailureListener(e -> {
                });
            });
        }
    }
    public boolean isLoggedIn() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null;
    }
    
    public void logoutUser() {
        if (isLoggedIn()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("fcm_token").document(getUserId());
            Log.i("USM", "Deleting Token");
            docRef.delete();
            firebaseAuth.signOut();
        }
        stopLocationService();
        editor.clear();
        editor.apply();
        Log.d(TAG, "사용자 로그아웃");
        notifyAuthStateChanged(null);

    }
    
    public String getDisplayName() {
        if (isLoggedIn()) {
            return Objects.requireNonNull(firebaseAuth.getCurrentUser()).getDisplayName();
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