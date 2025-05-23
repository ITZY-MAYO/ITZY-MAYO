package com.syu.itzy_mayo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ScheduleFragment extends Fragment {

    private EditText inputTodo;
    private Button addButton;
    private LinearLayout todoListLayout;
    private Button saveButton;
    private WebView addressWebView;
    private EditText searchEditText;

    private CalendarView calendarView;
    private EditText scheduleTitleEditText;
    private EditText scheduleDescriptionEditText;
    private Button scheduleSaveButton;

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_LAST_ADDRESS = "lastAddress";
    private static final String KEY_LAST_SCHEDULE_TITLE = "lastTitle";
    private static final String KEY_LAST_SCHEDULE_DESC = "lastDesc";

    private Context context;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        context = view.getContext();

        addressWebView = view.findViewById(R.id.addressWebView);
        searchEditText = view.findViewById(R.id.searchEditText);

        calendarView = view.findViewById(R.id.calendarView);
        scheduleTitleEditText = view.findViewById(R.id.scheduleTitle);
        scheduleDescriptionEditText = view.findViewById(R.id.scheduleDescription);
        scheduleSaveButton = view.findViewById(R.id.scheduleSaveButton);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedAddress = prefs.getString(KEY_LAST_ADDRESS, "");
        searchEditText.setText(savedAddress);
        scheduleTitleEditText.setText(prefs.getString(KEY_LAST_SCHEDULE_TITLE, ""));
        scheduleDescriptionEditText.setText(prefs.getString(KEY_LAST_SCHEDULE_DESC, ""));


        WebSettings webSettings = addressWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        addressWebView.addJavascriptInterface(new AndroidBridge(), "AndroidInterface");
        addressWebView.setVisibility(View.GONE);

        searchEditText.setOnClickListener(v -> {
            addressWebView.setVisibility(View.VISIBLE);
            addressWebView.loadUrl("file:///android_asset/kakao_address.html");
        });

        scheduleSaveButton.setOnClickListener(view1 -> {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(KEY_LAST_SCHEDULE_TITLE, scheduleTitleEditText.getText().toString());
            editor.putString(KEY_LAST_SCHEDULE_DESC, scheduleDescriptionEditText.getText().toString());
            editor.apply();
            Toast.makeText(context, "일정이 저장되었습니다", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void addNewTask(String taskText) {
        CheckBox checkBox = new CheckBox(context);
        checkBox.setText(taskText);
        checkBox.setTextColor(0xFF6D4C41);
        checkBox.setTextSize(16);
        checkBox.setPadding(0, 0, 0, 12);
        todoListLayout.addView(checkBox);
    }

    private void saveCheckedTasks() {
        StringBuilder saved = new StringBuilder("저장된 항목:\n");
        boolean hasChecked = false;

        for (int i = 0; i < todoListLayout.getChildCount(); i++) {
            View child = todoListLayout.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox cb = (CheckBox) child;
                if (cb.isChecked()) {
                    saved.append("- ").append(cb.getText().toString()).append("\n");
                    hasChecked = true;
                }
            }
        }

        Toast.makeText(context, hasChecked ? saved.toString() : "선택된 항목이 없습니다.", Toast.LENGTH_SHORT).show();
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void onAddressSelected(final String address) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putString(KEY_LAST_ADDRESS, address).apply();
                searchEditText.setText(address);
                addressWebView.setVisibility(View.GONE);
            });
        }
    }
}