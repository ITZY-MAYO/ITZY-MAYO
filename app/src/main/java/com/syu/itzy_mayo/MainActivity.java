package com.syu.itzy_mayo;

import android.annotation.SuppressLint;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.syu.itzy_mayo.Goal.GoalPagerFragment;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.naver.maps.map.NaverMapSdk;

public class MainActivity extends AppCompatActivity {
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.menu_frame_layout, new MapFragment()).commit();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new ItemSelectedListener());

        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NcpKeyClient(BuildConfig.NCP_CLIENT_ID));
    }

    class ItemSelectedListener implements BottomNavigationView.OnItemSelectedListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment targetFragment;

            if (menuItem.getItemId() == R.id.nav_home) {
                targetFragment = new MapFragment();
            } else if (menuItem.getItemId() == R.id.nav_feed) {
                targetFragment = new FeedFragment();
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