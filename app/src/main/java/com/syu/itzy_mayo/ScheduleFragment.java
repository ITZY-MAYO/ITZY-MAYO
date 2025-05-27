package com.syu.itzy_mayo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScheduleFragment extends Fragment {

    private WebView addressWebView;
    private EditText searchEditText;
    private CalendarView calendarView;
    private EditText scheduleTitleEditText;
    private EditText scheduleDescriptionEditText;
    private Button scheduleSaveButton;
    private LinearLayout savedScheduleList;

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_SCHEDULE_LIST = "scheduleList";
    private static final String KEY_LAST_ADDRESS = "lastAddress";

    private Context context;
    private String selectedDate = "";

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
        savedScheduleList = view.findViewById(R.id.savedScheduleList);

        addressWebView.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        searchEditText.setText("");

        searchEditText.setOnClickListener(v -> {
            searchEditText.setText("");
            if (addressWebView.getVisibility() == View.VISIBLE) {
                addressWebView.setVisibility(View.GONE);
            } else {
                addressWebView.setVisibility(View.VISIBLE);
                addressWebView.loadUrl("file:///android_asset/kakao_address.html");
            }
        });

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

        // 오늘 날짜 기본값 세팅
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 날짜 선택 시 해당 날짜 일정만 로드
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadScheduleListByDate(selectedDate);
        });

        scheduleSaveButton.setOnClickListener(view1 -> {
            String title = scheduleTitleEditText.getText().toString().trim();
            String desc = scheduleDescriptionEditText.getText().toString().trim();
            String address = searchEditText.getText().toString().trim();
            String date = selectedDate;

            if (title.isEmpty() && desc.isEmpty()) return;

            try {
                JSONArray scheduleArray = getScheduleArray();
                JSONObject newItem = new JSONObject();
                newItem.put("title", title);
                newItem.put("desc", desc);
                newItem.put("address", address);
                newItem.put("date", date);
                scheduleArray.put(newItem);

                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putString(KEY_SCHEDULE_LIST, scheduleArray.toString()).apply();

                // 현재 선택된 날짜 일정만 다시 로드
                loadScheduleListByDate(selectedDate);

                scheduleTitleEditText.setText("");
                scheduleDescriptionEditText.setText("");

                Toast.makeText(context, "일정이 저장되었습니다", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        // 초기 화면에 오늘 날짜 일정만 표시
        loadScheduleListByDate(selectedDate);

        return view;
    }

    // 전체 일정 대신 선택 날짜 일정만 보여주는 함수
    private void loadScheduleListByDate(String date) {
        savedScheduleList.removeAllViews();
        JSONArray array = getScheduleArray();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject item = array.getJSONObject(i);
                String itemDate = item.optString("date", "");
                if (itemDate.equals(date)) {
                    addScheduleView(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private JSONArray getScheduleArray() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SCHEDULE_LIST, "[]");
        try {
            return new JSONArray(json);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    private void saveScheduleArray(JSONArray array) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SCHEDULE_LIST, array.toString()).apply();
    }

    private void addScheduleView(JSONObject item) throws JSONException {
        String title = item.getString("title");
        String desc = item.getString("desc");
        String address = item.optString("address", "");
        String date = item.optString("date", "");

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(0, 0, 0, 12);
        container.setGravity(Gravity.CENTER_VERTICAL);

        TextView scheduleView = new TextView(context);
        scheduleView.setText("📅 " + date + "\n📍 " + address + "\n📌 " + title + "\n📝 " + desc);
        scheduleView.setBackgroundColor(Color.parseColor("#EEEEEE"));
        scheduleView.setTextSize(16);
        scheduleView.setTextColor(Color.BLACK);
        scheduleView.setPadding(12, 12, 12, 12);
        scheduleView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        ImageButton deleteBtn = new ImageButton(context);
        deleteBtn.setImageResource(android.R.drawable.ic_menu_delete);
        deleteBtn.setBackgroundColor(Color.TRANSPARENT);
        deleteBtn.setVisibility(View.GONE);

        deleteBtn.setOnClickListener(v -> {
            savedScheduleList.removeView(container);
            removeFromPrefs(title, desc, address, date);
            // 삭제 후 다시 해당 날짜 일정만 로드
            loadScheduleListByDate(selectedDate);
        });

        container.setOnLongClickListener(v -> {
            deleteBtn.setVisibility(deleteBtn.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            return true;
        });

        container.addView(scheduleView);
        container.addView(deleteBtn);
        savedScheduleList.addView(container);
    }

    private void removeFromPrefs(String title, String desc, String address, String date) {
        JSONArray array = getScheduleArray();
        JSONArray newArray = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject obj = array.getJSONObject(i);
                if (!obj.getString("title").equals(title) ||
                        !obj.getString("desc").equals(desc) ||
                        !obj.optString("address").equals(address) ||
                        !obj.optString("date").equals(date)) {
                    newArray.put(obj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        saveScheduleArray(newArray);
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
