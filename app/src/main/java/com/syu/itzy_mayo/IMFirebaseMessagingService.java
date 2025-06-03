package com.syu.itzy_mayo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.syu.itzy_mayo.FCMToken.FCMToken;

public class IMFirebaseMessagingService extends FirebaseMessagingService {
    private Toast toast;

    private void showToast(String message){
        if (toast != null) {
            toast.cancel();
        }
        if(getApplicationContext()   != null) {
            toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.e("FCMToken", "New Token: "+ token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        String title = "일정 알림";
        String body = "주변 일정을 확인하세요";

        if (message.getNotification() != null) {
            // FCM 알림 메시지 (Notification Payload)
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        } else if (!message.getData().isEmpty()) {
            // FCM 데이터 메시지 (Data Payload)
            title = message.getData().get("title");
            body = message.getData().get("body");
        }

        // 알림 생성
        String channelId = "schedule_notice_channel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(0, builder.build());
    }
}
