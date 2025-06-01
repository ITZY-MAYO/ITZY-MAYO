package com.syu.itzy_mayo;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.syu.itzy_mayo.Schedule.Schedule;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ScheduleFragment extends Fragment {

    private WebView addressWebView;
    private EditText searchEditText;
    private MaterialCalendarView calendarView;
    private EditText scheduleTitleEditText;
    private EditText scheduleDescriptionEditText;
    private Button scheduleSaveButton;
    private Button showAllSchedulesButton;
    private LinearLayout savedScheduleList;
    private GeoPoint selectedGeoPoint = null;

    private Context context;
    private String selectedDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
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

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", date.getYear(), date.getMonth() + 1, date.getDay());
            loadScheduleFromFirestore(selectedDate);
        });

        scheduleSaveButton.setOnClickListener(view1 -> {
            String title = scheduleTitleEditText.getText().toString().trim();
            String desc = scheduleDescriptionEditText.getText().toString().trim();
            String address = searchEditText.getText().toString().trim();
            String date = selectedDate;

            if (title.isEmpty() && desc.isEmpty()) return;

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser == null) {
                Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = currentUser.getUid();
            GeoPoint geoPoint = selectedGeoPoint != null ? selectedGeoPoint : new GeoPoint(0, 0);

            Timestamp timestamp;
            try {
                Date parsedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
                timestamp = new Timestamp(parsedDate);
            } catch (Exception e) {
                e.printStackTrace();
                timestamp = Timestamp.now();
            }

            Schedule schedule = new Schedule(title, desc, uid, geoPoint, timestamp, address);

            db.collection("schedule")
                    .add(schedule)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(context, "일정이 저장되었습니다", Toast.LENGTH_SHORT).show();
                        scheduleTitleEditText.setText("");
                        scheduleDescriptionEditText.setText("");
                        selectedGeoPoint = null;
                        loadScheduleFromFirestore(selectedDate);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "일정 저장 실패", Toast.LENGTH_SHORT).show();
                    });
        });

        showAllSchedulesButton.setOnClickListener(v -> showAllSchedulesFromFirestore());

        loadScheduleFromFirestore(selectedDate);

        return view;
    }

    private void fetchCoordinatesFromNaverLocalSearch(String query) {
        new Thread(() -> {
            try {
                String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
                String apiUrl = "https://openapi.naver.com/v1/search/local.json?query=" + encodedQuery;

                java.net.URL url = new java.net.URL(apiUrl);
                java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("X-Naver-Client-Id", "BuildConfig.NAVER_API_CLIENT_ID");
                con.setRequestProperty("X-Naver-Client-Secret", "BuildConfig.NAVER_API_CLIENT_SECRET");

                int responseCode = con.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();

                    JSONObject json = new JSONObject(sb.toString());
                    if (json.getJSONArray("items").length() > 0) {
                        JSONObject item = json.getJSONArray("items").getJSONObject(0);
                        double lon = Double.parseDouble(item.getString("mapx")) / 1_0000000.0;
                        double lat = Double.parseDouble(item.getString("mapy")) / 1_0000000.0;
                        selectedGeoPoint = new GeoPoint(lat, lon);
                    }
                } else {
                    Log.e("NaverLocalSearch", "응답 실패: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadScheduleFromFirestore(String selectedDate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) return;

        String uid = currentUser.getUid();

        db.collection("schedule")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    savedScheduleList.removeAllViews();
                    Set<CalendarDay> eventDates = new HashSet<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Schedule schedule = doc.toObject(Schedule.class);
                        Date date = schedule.getDatetime().toDate();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        CalendarDay day = CalendarDay.from(calendar);
                        eventDates.add(day);

                        String scheduleDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                        if (scheduleDate.equals(selectedDate)) {
                            try {
                                JSONObject item = new JSONObject();
                                item.put("title", schedule.getTitle());
                                item.put("desc", schedule.getContent());
                                item.put("address", schedule.getAddress());
                                item.put("date", scheduleDate);
                                item.put("docId", doc.getId());
                                addScheduleView(item);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    calendarView.removeDecorators();
                    calendarView.addDecorator(new EventDecorator(Color.RED, eventDates));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "불러오기 실패", e));
    }

    private void showAllSchedulesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) return;

        String uid = currentUser.getUid();

        db.collection("schedule")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    savedScheduleList.removeAllViews();
                    Set<CalendarDay> eventDates = new HashSet<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Schedule schedule = doc.toObject(Schedule.class);
                        Date date = schedule.getDatetime().toDate();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        CalendarDay day = CalendarDay.from(calendar);
                        eventDates.add(day);

                        try {
                            String scheduleDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                            JSONObject item = new JSONObject();
                            item.put("title", schedule.getTitle());
                            item.put("desc", schedule.getContent());
                            item.put("address", schedule.getAddress());
                            item.put("date", scheduleDate);
                            item.put("docId", doc.getId());
                            addScheduleView(item);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    calendarView.removeDecorators();
                    calendarView.addDecorator(new EventDecorator(Color.RED, eventDates));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "전체 일정 불러오기 실패", e));
    }

    private void addScheduleView(JSONObject item) throws JSONException {
        String title = item.getString("title");
        String desc = item.getString("desc");
        String address = item.optString("address", "");
        String date = item.optString("date", "");
        String docId = item.optString("docId", "");

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(0, 0, 0, 12);
        container.setGravity(Gravity.CENTER_VERTICAL);

        TextView scheduleView = new TextView(context);
        scheduleView.setText("\uD83D\uDCC5 " + date + "\n\uD83D\uDCCD " + address + "\n\uD83D\uDCCC " + title + "\n\uD83D\uDCDD " + desc);
        scheduleView.setBackgroundColor(0xFFEEEEEE);
        scheduleView.setTextSize(16);
        scheduleView.setTextColor(0xFF000000);
        scheduleView.setPadding(12, 12, 12, 12);
        scheduleView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        ImageButton deleteBtn = new ImageButton(context);
        deleteBtn.setImageResource(android.R.drawable.ic_menu_delete);
        deleteBtn.setBackgroundColor(0x00000000);
        deleteBtn.setVisibility(View.GONE);

        deleteBtn.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("schedule").document(docId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "일정이 삭제되었습니다", Toast.LENGTH_SHORT).show();
                        savedScheduleList.removeView(container);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        container.setOnLongClickListener(v -> {
            deleteBtn.setVisibility(deleteBtn.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            return true;
        });

        container.addView(scheduleView);
        container.addView(deleteBtn);
        savedScheduleList.addView(container);
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void onAddressSelected(final String address, final String latStr, final String lngStr) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                searchEditText.setText(address);
                try {
                    double lat = Double.parseDouble(latStr);
                    double lng = Double.parseDouble(lngStr);
                    selectedGeoPoint = new GeoPoint(lat, lng);
                } catch (NumberFormatException e) {
                    selectedGeoPoint = null;
                    Toast.makeText(context, "위도/경도 값을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                addressWebView.setVisibility(View.GONE);
            });
        }

        @JavascriptInterface
        public void onAddressSelected(final String address) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                searchEditText.setText(address);
                fetchCoordinatesFromNaverLocalSearch(address);
                addressWebView.setVisibility(View.GONE);
            });
        }
    }
}
