package com.syu.itzy_mayo.location;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.syu.itzy_mayo.MainActivity; // Assuming MainActivity is the entry point
import com.syu.itzy_mayo.R; // Assuming R class is in this package

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LocationUploaderService extends Service {

    private static final String TAG = "LocationUploaderSvc";
    private static final String NOTIFICATION_CHANNEL_ID = "LocationUploaderChannel";
    private static final int NOTIFICATION_ID = 12345678;
    private static final long UPDATE_INTERVAL_MS = 30 * 1000; // 30 sec
    private static final long FASTEST_INTERVAL_MS = 15 * 1000;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private Handler serviceHandler;
    private Runnable periodicUpdateRunnable;

    private static final String POST_URL = "http://ec2-3-36-62-246.ap-northeast-2.compute.amazonaws.com:8080/api/v1/locations/";

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        serviceHandler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
        setupLocationCallback();
        setupPeriodicUpload();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        startForeground(NOTIFICATION_ID, createNotification());
        startLocationUpdates();
        serviceHandler.post(periodicUpdateRunnable);
        return START_STICKY;
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d(TAG, "Location Update: " + location.getLatitude() + ", " + location.getLongitude());
                        currentLocation = location;
                    }
                }
            }
        };
    }

    private void setupPeriodicUpload() {
        periodicUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentLocation != null) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String firebaseUserId = currentUser.getUid();
                        LocationData data = new LocationData(firebaseUserId, currentLocation.getLatitude(), currentLocation.getLongitude());
                        sendLocationToServer(data);
                    } else {
                        Log.w(TAG, "User not logged in, skipping location upload.");
                    }
                }
                else {
                    Log.w(TAG, "Current location unknown, skipping upload.");
                }
                // Schedule the next execution
                serviceHandler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        };
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted. Cannot start updates.");
            stopSelf();
            return;
        }
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            Log.d(TAG, "Requested location updates");
        } catch (SecurityException e) {
            Log.e(TAG, "Lost location permission. Could not request updates. " + e.getMessage());
            stopSelf();
        }
    }

    private void sendLocationToServer(LocationData locationData) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(POST_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);

                String jsonPayload = locationData.toJson();
                Log.d(TAG, "Sending JSON: " + jsonPayload);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "POST Response Code :: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    Log.i(TAG, "Location uploaded successfully.");
                } else {
                    Log.e(TAG, "Error uploading location. Response code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending location to server", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Location Uploader Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Location Tracking Active")
                .setContentText("Uploading your location periodically.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d(TAG, "Removed location updates");
        }
        if (serviceHandler != null && periodicUpdateRunnable != null) {
            serviceHandler.removeCallbacks(periodicUpdateRunnable);
            Log.d(TAG, "Stopped periodic uploader");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
} 