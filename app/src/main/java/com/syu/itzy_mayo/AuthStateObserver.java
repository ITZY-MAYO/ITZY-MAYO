package com.syu.itzy_mayo;

import com.google.firebase.auth.FirebaseUser;

public interface AuthStateObserver {
    void onAuthStateChanged(FirebaseUser user);
}
