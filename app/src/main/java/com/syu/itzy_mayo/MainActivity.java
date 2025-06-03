package com.syu.itzy_mayo;

import android.Manifest;
import android.annotation.SuppressLint;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.syu.itzy_mayo.Goal.GoalPagerFragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.naver.maps.map.NaverMapSdk;

public class MainActivity extends AppCompatActivity {
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.menu_frame_layout, new MapFragment()).commit();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new ItemSelectedListener());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1000 // requestCode (원하는 숫자 사용 가능)
            );
            return;
        }
        String channelId = "schedule_notice_channel";
        String channelName = "Schedule Channel";
        String channelDescription = "일정 알림을 위한 채널";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription(channelDescription);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    class ItemSelectedListener implements BottomNavigationView.OnItemSelectedListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment targetFragment;

            if (menuItem.getItemId() == R.id.nav_home) {
                targetFragment = new MapFragment();
            } else if (menuItem.getItemId() == R.id.nav_games) {
                targetFragment = new GamesFragment();
            } else if (menuItem.getItemId() == R.id.nav_schedule) {
                targetFragment = new ScheduleFragment();
            } else if (menuItem.getItemId() == R.id.nav_goal) {
                targetFragment = new GoalPagerFragment();
            } else if (menuItem.getItemId() == R.id.nav_settings) {
                targetFragment = new SettingsFragment();
            } else {
                return false;
            }
            transaction.replace(R.id.menu_frame_layout, targetFragment).commit();
            return true;
        }
    }
}