package com.syu.itzy_mayo.Goal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.syu.itzy_mayo.MainActivity;
import com.syu.itzy_mayo.R;

public class MyAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("msg");
        String goalId = intent.getStringExtra("goalId");

        if (goalId == null || goalId.isEmpty()) return;

        FirebaseFirestore.getInstance()
                .collection("goals")
                .document(goalId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    Goal goal = snapshot.toObject(Goal.class);
                    if (goal == null || goal.isCompleted()) return;

                    showNotification(context, msg);
                });
    }

    private void showNotification(Context context, String msg) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("goal_channel", "목표 알림", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "goal_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("목표 알림")
                .setContentText(msg)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
    }
}
