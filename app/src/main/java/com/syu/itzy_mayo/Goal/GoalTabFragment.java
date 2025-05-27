package com.syu.itzy_mayo.Goal;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.syu.itzy_mayo.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GoalTabFragment extends Fragment implements GoalAdapter.OnGoalCheckListener {
    private String tabType;
    private GoalAdapter adapter;
    private RecyclerView recyclerView;
    private static final int[] CALENDAR_DAYS = {
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
    };
    public static GoalTabFragment newInstance(String tabType) {
        GoalTabFragment fragment = new GoalTabFragment();
        Bundle args = new Bundle();
        args.putString("tabType", tabType);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.goal_tab_fragment, container, false);
        recyclerView = view.findViewById(R.id.goalRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Bundle args = getArguments();
        tabType = (args != null) ? args.getString("tabType", "all") : "all";
        adapter = new GoalAdapter(this, "today".equals(tabType), getNowTime());
        recyclerView.setAdapter(adapter);

        refresh();

        if ("all".equals(tabType)) {
            attachSwipeToDelete();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder a, @NonNull RecyclerView.ViewHolder b) { return false; }
                    @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int pos = viewHolder.getAdapterPosition();
                        Goal g = adapter.getGoal(pos);
                        SharedGoalList.get().removeGoal(g);
                        refreshAll();
                    }
                };
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);
    }

    public static String getNowTime() {
        Calendar cal = Calendar.getInstance();
        return String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    public void showAddGoalDialog() {
        if (!"all".equals(tabType)) {
            Toast.makeText(getContext(), "전체 목표 탭에서만 추가할 수 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_goal, null);

        EditText etTitle = dialogView.findViewById(R.id.etGoalTitle);
        EditText etTime  = dialogView.findViewById(R.id.etGoalTime);
        ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroupDays);

        final List<Integer> selectedDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            final int dayIdx = i;
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedDays.add(dayIdx);
                else selectedDays.remove((Integer) dayIdx);
            });
        }

        etTime.setOnClickListener(v -> {
            if (getActivity() != null) {
                InputMethodManager imm = (InputMethodManager) requireContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                View currentFocus = getActivity().getCurrentFocus();
                if (currentFocus != null)
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
            int hour = 8, minute = 0;
            TimePickerDialog timePicker = new TimePickerDialog(getContext(), (pickerView, h, m) -> {
                String timeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m);
                etTime.setText(timeStr);
            }, hour, minute, true);
            timePicker.show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle("새 목표 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String time  = etTime.getText().toString().trim();
                    if(!title.isEmpty() && !time.isEmpty() && !selectedDays.isEmpty()) {
                        Goal newGoal = new Goal(title, time, new ArrayList<>(selectedDays));
                        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
                        newGoal.createdDate = todayStr;
                        SharedGoalList.get().addGoal(newGoal);
                        for (int dayIdx : selectedDays) {
                            setGoalAlarm(
                                    getContext(), title, time, dayIdx,
                                    "5분 전! - " + title, -5
                            );
                            setGoalAlarm(
                                    getContext(), title, time, dayIdx,
                                    "아직 목표를 완료하지 않았습니다. 다시 확인해주세요 - " + title, 10
                            );
                        }
                        refreshAll();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    public void refresh() {
        if ("today".equals(tabType)) {
            String nowTime = getNowTime();
            adapter = new GoalAdapter(this, true, nowTime); // 최신 nowTime 반영
            recyclerView.setAdapter(adapter);
            adapter.submitList(SharedGoalList.get().getTodayGoals());
        } else {
            adapter = new GoalAdapter(this, false, null);
            recyclerView.setAdapter(adapter);
            adapter.submitList(new ArrayList<>(SharedGoalList.get().getAllGoals()));
        }
    }

    public void refreshAll() {
        refresh();
        GoalPagerFragment parent = (GoalPagerFragment) getParentFragment();
        if (parent != null) parent.refreshAllTabs();
    }

    private void setGoalAlarm(
            Context context, String title, String goalTime, int dayOfWeek,
            String msg, int offsetMinutes) {
        String[] t = goalTime.split(":");
        int hour = Integer.parseInt(t[0]);
        int min = Integer.parseInt(t[1]) + offsetMinutes;
        while (min < 0)   { min += 60; hour -= 1; }
        while (min >= 60) { min -= 60; hour += 1; }
        int baseDayOfWeek = CALENDAR_DAYS[dayOfWeek];

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, hour);
        alarmTime.set(Calendar.MINUTE, min);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);

        while (alarmTime.get(Calendar.DAY_OF_WEEK) != baseDayOfWeek
                || alarmTime.getTimeInMillis() < System.currentTimeMillis()) {
            alarmTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        int requestCode = (title+goalTime+dayOfWeek+offsetMinutes).hashCode();

        Intent intent = new Intent(context, MyAlarmReceiver.class);
        intent.putExtra("msg", msg);
        intent.putExtra("title", title);
        intent.putExtra("goalTime", goalTime);
        intent.putExtra("dayOfWeek", dayOfWeek);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
        }
    }

    @Override
    public void onGoalCheckedChanged(Goal goal) {
        for(Goal g : SharedGoalList.get().getAllGoals()) {
            if (g.equals(goal)) g.isCompleted = goal.isCompleted;
        }
        refreshAll();
    }
}