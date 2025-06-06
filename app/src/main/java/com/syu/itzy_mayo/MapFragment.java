package com.syu.itzy_mayo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.syu.itzy_mayo.Schedule.Schedule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class MapFragment extends Fragment implements OnMapReadyCallback, AuthStateObserver {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private FusedLocationProviderClient fusedLocationClient;
    private final ArrayMap<String, Marker> mapMarkers = new ArrayMap<>();
    private ListenerRegistration scheduleListener;
    private UserSessionManager sessionManager;
    private FirebaseFirestore db;
    private Toast toast;

    private void showToast(String message){
        if (toast != null) {
            toast.cancel();
        }
        if(getContext() != null) {
            toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void listenScheduleMarkers() {
        if (sessionManager.isLoggedIn()) {
            String uid = sessionManager.getUserId();
            if (uid == null)
                return;
            if (scheduleListener != null) {
                scheduleListener.remove();
                scheduleListener = null;
            }
            scheduleListener = db.collection("schedule").whereEqualTo("userId", uid)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.w("Firestore", "Listen failed.", e);
                            return;
                        }
                        if (snapshots == null) {
                            return;
                        }
                        Iterator<Map.Entry<String, Marker>> iterator = mapMarkers.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<String, Marker> entry = iterator.next();
                            Marker marker = entry.getValue();
                            if (marker != null) {
                                marker.setMap(null);
                                iterator.remove();
                            }
                        }
                        for (DocumentSnapshot doc : snapshots) {
                            Schedule schedule = doc.toObject(Schedule.class);
                            if (schedule != null && schedule.getTitle() != null && schedule.getGeoPoint() != null) {
                                // 이 코드가 실행되면 db에서 가져온 일정 데이터에 설정된 위치가 지도에 표사됨(DB변경 감지 시)
                                addMarker(schedule.getId(), schedule.getContent(), schedule.getGeoPoint());
                            }
                        }
                    });
        }
    }

    public MapFragment() {
        super(R.layout.fragment_map);
    }

    // 권한 요청 런처
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    initMap();
                    naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
                } else {
                    showMissingPermissionError();
                    naverMap.setLocationTrackingMode(LocationTrackingMode.None);
                }
            });

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 권한 요청을 위한 LocationSource 초기화
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // FusedLocationProviderClient 초기화 (현재 위치 가져오기 위함)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // 지도 초기화
        initMap();
        sessionManager = ItzyMayoApplication.getInstance().getSessionManager();
        sessionManager.addObserver(this);
        db = FirebaseFirestore.getInstance();
        listenScheduleMarkers();
    }

    private void initMap() {
        FragmentManager fm = getChildFragmentManager();
        com.naver.maps.map.MapFragment mapFragment = (com.naver.maps.map.MapFragment) fm.findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = com.naver.maps.map.MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        // UI 설정
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setScaleBarEnabled(true);
        uiSettings.setZoomControlEnabled(true);
        uiSettings.setLocationButtonEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);

        // 지도 길게 누르기 이벤트 처리
        naverMap.setOnMapLongClickListener((point, coord) -> {
            if (sessionManager.isLoggedIn()) {
                showAddScheduleBottomSheet(coord);
            } else {
                showToast("일정 추가를 위해 로그인이 필요합니다.");
            }
        });

        // 위치 추적 모드 및 소스 설정
        this.naverMap.setLocationSource(locationSource);

        // 위치 권한 확인
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        showCurrentLocation();

        // naverMap이 준비된 후, 로그인 상태라면 마커를 추가
        if (sessionManager.isLoggedIn()) {
            String uid = sessionManager.getUserId();
            if (uid != null) {
                // 이 코드가 실행되면 db에서 가져온 일정 데이터에 설정된 위치가 지도에 표사됨(지도 로딩 완료 후)
                addScheduleMarkers(uid);
            }
        }
    }
    

    private void addMarker(String id, String description, GeoPoint geoPoint) {
        LatLng position = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
        Marker marker = new Marker();
        marker.setPosition(position);
        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_marker));
        marker.setWidth(80);
        marker.setHeight(80);
        marker.setCaptionText(description);
        marker.setCaptionColor(Color.BLACK);
        marker.setCaptionHaloColor(Color.WHITE);
        marker.setCaptionTextSize(16);
        marker.setMap(naverMap);
        marker.setOnClickListener(overlay -> {
            showMarkerBottomSheet(id);
            return true;
        });
        this.mapMarkers.put(id, marker);
    }

    private void showMarkerBottomSheet(String id) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_marker_detail, null);
        
        TextView titleTextView = bottomSheetView.findViewById(R.id.text_marker_title);
        TextView contentTextView = bottomSheetView.findViewById(R.id.text_marker_content);
        TextView datetimeTextView = bottomSheetView.findViewById(R.id.text_marker_datetime);
        TextView addressTextView = bottomSheetView.findViewById(R.id.text_marker_address);
        Button closeButton = bottomSheetView.findViewById(R.id.button_close);
        Button deleteButton = bottomSheetView.findViewById(R.id.button_delete);
        db.collection("schedule").document(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Schedule schedule = task.getResult().toObject(Schedule.class);
                titleTextView.setText(schedule.getTitle());
                contentTextView.setText(schedule.getContent());
                datetimeTextView.setText(schedule.getDatetime().toDate().toString());
                addressTextView.setText(schedule.getAddress());
                closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());
                deleteButton.setOnClickListener(v -> {
                    deleteSchedule(id);
                    bottomSheetDialog.dismiss();
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        }).addOnFailureListener(e -> {
            Log.w("Firestore", "Listen failed.", e);
            showToast("일정 정보를 불러오는데 실패했습니다.");
        });
    }
    private void deleteSchedule(String id) {
        db.collection("schedule").document(id).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToast("일정이 삭제되었습니다.");
            }
        }).addOnFailureListener(e -> {
            Log.w("Firestore", "Delete failed.", e);
            showToast("일정 삭제에 실패했습니다.");
        });
    }

    private void showCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(),
                location -> {
                    if (location != null && naverMap != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Log.i("Location", "latitude: " + latitude + " longitude: " + longitude);
                    }
                });
    }

    private void addScheduleMarkers(String uid) {
        db.collection("schedule").whereEqualTo("userId", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Iterator<Map.Entry<String, Marker>> iterator = mapMarkers.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<String, Marker> entry = iterator.next();
                            Marker marker = entry.getValue();
                            if (marker != null) {
                                marker.setMap(null);
                                iterator.remove();
                            }
                        }
                        // DB에서 받아온 schedule로 마커 다시 생성
                        for (DocumentSnapshot doc : task.getResult()) {
                            Schedule schedule = doc.toObject(Schedule.class);
                            if (schedule != null && schedule.getTitle() != null && schedule.getGeoPoint() != null) {
                                // 실제 db에서 가져온 일정 데이터에 설정된 위치가 지도에 표시하는 코드
                                addMarker(schedule.getId(), schedule.getContent(), schedule.getGeoPoint());
                            }
                        }
                    }
                });
    }

    private void showMissingPermissionError() {
        showToast("위치 정보 표시를 위해 권한이 필요합니다.");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (scheduleListener != null) {
            scheduleListener.remove();
            scheduleListener = null;
        }
    }

    @Override
    public void onAuthStateChanged(FirebaseUser user) {
        if (user != null && sessionManager.isLoggedIn()) {
            String uid = sessionManager.getUserId();
            if (uid != null) {
                // 기존 마커 모두 삭제
                Iterator<Map.Entry<String, Marker>> iterator = mapMarkers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Marker> entry = iterator.next();
                    Marker marker = entry.getValue();
                    if (marker != null) {
                        marker.setMap(null);
                        iterator.remove();
                    }
                }
                // 이 코드가 실행되면 db에서 가져온 일정 데이터에 설정된 위치가 지도에 표사됨(인증정보 변경 시)
                addScheduleMarkers(uid);
            }
        } else {
            Iterator<Map.Entry<String, Marker>> iterator = mapMarkers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Marker> entry = iterator.next();
                Marker marker = entry.getValue();
                if (marker != null) {
                    marker.setMap(null);
                    iterator.remove(); 
                }
            }
        }
    }

    private void showAddScheduleBottomSheet(LatLng coord) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add_schedule, null);

        EditText titleInput = bottomSheetView.findViewById(R.id.input_schedule_title);
        EditText contentInput = bottomSheetView.findViewById(R.id.input_schedule_content);
        Button datePickerButton = bottomSheetView.findViewById(R.id.button_date_picker);
        TextView addressText = bottomSheetView.findViewById(R.id.text_schedule_address);
        Button saveButton = bottomSheetView.findViewById(R.id.button_save);
        Button cancelButton = bottomSheetView.findViewById(R.id.button_cancel);

        final Date[] selectedDate = {new Date()}; // 선택된 날짜를 저장할 배열

        // 날짜 선택 버튼 설정
        datePickerButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("날짜 선택")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDate[0] = new Date(selection);
                datePickerButton.setText(android.text.format.DateFormat.getDateFormat(requireContext())
                    .format(selectedDate[0]));
            });

            datePicker.show(getChildFragmentManager(), "DATE_PICKER");
        });

        // 주소 가져오기 (Geocoding)
        reverseGeocode(coord, address -> {
            if (address != null) {
                addressText.setText(address);
            }
        });

        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString();
            String content = contentInput.getText().toString();
            String address = addressText.getText().toString();

            if (title.isEmpty() || content.isEmpty()) {
                showToast("제목과 내용을 입력해주세요.");
                return;
            }

            // Firestore에 일정 저장
            Schedule schedule = new Schedule();
            schedule.setUserId(sessionManager.getUserId());
            schedule.setTitle(title);
            schedule.setContent(content);
            schedule.setDatetime(new Timestamp(selectedDate[0]));
            schedule.setAddress(address);
            schedule.setGeoPoint(new GeoPoint(coord.latitude, coord.longitude));

            db.collection("schedule")
                .add(schedule)
                .addOnSuccessListener(documentReference -> {
                    showToast("일정이 추가되었습니다.");
                    bottomSheetDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    showToast("일정 추가에 실패했습니다.");
                    Log.e("Firestore", "Error adding schedule", e);
                });
        });

        cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void reverseGeocode(LatLng coord, OnAddressReceivedListener listener) {
        String coords = String.format("%f,%f", coord.longitude, coord.latitude);
        String url = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc?output=json&coords=" + coords;
        
        new Thread(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("x-ncp-apigw-api-key-id", BuildConfig.NCP_CLIENT_ID);
                conn.setRequestProperty("x-ncp-apigw-api-key", BuildConfig.NCP_API_KEY);
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                Log.i("ResponseCode", String.valueOf(responseCode));
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // JSON 파싱
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray results = jsonResponse.getJSONArray("results");
                    
                    // 첫 번째 결과(법정동 코드)에서 지역 정보 추출
                    JSONObject firstResult = results.getJSONObject(0);
                    JSONObject region = firstResult.getJSONObject("region");
                    
                    String area1 = region.getJSONObject("area1").getString("name"); // 시/도
                    String area2 = region.getJSONObject("area2").getString("name"); // 시/군/구
                    String area3 = region.getJSONObject("area3").getString("name"); // 동/읍/면
                    
                    // area4가 비어있지 않은 경우에만 포함
                    JSONObject area4Obj = region.getJSONObject("area4");
                    String area4 = area4Obj.getString("name"); // 리
                    
                    // 주소 조합
                    final String address;
                    if (area4.isEmpty()) {
                        address = String.format("%s %s %s", area1, area2, area3);
                    } else {
                        address = String.format("%s %s %s %s", area1, area2, area3, area4);
                    }

                    // UI 스레드에서 결과 전달
                    requireActivity().runOnUiThread(() -> {
                        if (!address.isEmpty()) {
                            listener.onAddressReceived(address);
                        } else {
                            showToast("주소를 가져오는데 실패했습니다.");
                        }
                    });
                } else {
                    requireActivity().runOnUiThread(() -> 
                        showToast("주소를 가져오는데 실패했습니다. Response Error"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> 
                    showToast("주소를 가져오는데 실패했습니다. Error"));
            }
        }).start();
    }

    interface OnAddressReceivedListener {
        void onAddressReceived(String address);
    }
}
