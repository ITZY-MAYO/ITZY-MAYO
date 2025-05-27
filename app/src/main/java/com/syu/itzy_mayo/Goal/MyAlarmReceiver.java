package com.syu.itzy_mayo.Goal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.syu.itzy_mayo.MainActivity;
import com.syu.itzy_mayo.R;

import java.util.List;

public class MyAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("msg");
        String title = intent.getStringExtra("title");
        String time  = intent.getStringExtra("goalTime");

        if (msg != null && msg.contains("아직 목표를 완료하지 않았습니다")) {
            List<Goal> allGoals = SharedGoalList.get().getAllGoals();
            if (allGoals != null && !allGoals.isEmpty()) {
                boolean notChecked = false;
                for (Goal g : allGoals) {
                    if (g.getTitle().equals(title) && g.getTime().equals(time) && !g.isCompleted()) {
                        notChecked = true;
                        break;
                    }
                }
                if (!notChecked) return; // 이미 완료된 경우 알림 무시
            } else {
                // 앱 실행 없이 SharedGoalList 비어있을 가능성 있음
                // 강제로 알림 울리게 허용 (추가 로직 필요 시 Firestore에서 직접 검사 권장)
            }
        }

        // 알림 채널 생성 (Android 8 이상)
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("goal_channel", "목표 알림", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        // 알림 클릭 시 MainActivity 실행
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

        // Android 13+ 권한 체크
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
    }
}