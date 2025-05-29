package com.syu.itzy_mayo.Goal;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.syu.itzy_mayo.ItzyMayoApplication;
import com.syu.itzy_mayo.R;
import com.syu.itzy_mayo.UserSessionManager;

import java.text.SimpleDateFormat;
import java.util.*;

public class GoalTabFragment extends Fragment implements GoalAdapter.OnGoalCheckListener {

    private String tabType;
    private GoalAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvReport, tvEmpty;
    private final GoalFirestoreHelper firestoreHelper = new GoalFirestoreHelper();
    private final UserSessionManager sessionManager = ItzyMayoApplication.getInstance().getSessionManager();

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

        tvReport = view.findViewById(R.id.tvWeeklyReport);
        tvEmpty = view.findViewById(R.id.tvEmptyView);
        tabType = getArguments() != null ? getArguments().getString("tabType", "all") : "all";

        refresh();
        if ("all".equals(tabType)) attachSwipeToDelete();
        return view;
    }

    public void refresh() {
        boolean today = "today".equals(tabType);
        String nowTime = getNowTime();

        firestoreHelper.loadGoals(sessionManager.getUserId(), today, goals -> {
            if (today) {
                int todayIdx = getTodayIndices().get(0);
                goals.removeIf(goal -> !goal.getDaysOfWeek().contains(todayIdx));
            }

            SharedGoalList.get().setAllGoals(goals);

            adapter = new GoalAdapter(this, today, nowTime, goal -> showEditGoalDialog(goal));
            recyclerView.setAdapter(adapter);
            adapter.submitList(goals);

            updateReport(goals);
            if (tvEmpty != null) {
                tvEmpty.setVisibility(goals.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void refreshAll() {
        refresh();
        Fragment parent = getParentFragment();
        if (parent instanceof GoalPagerFragment) {
            ((GoalPagerFragment) parent).refreshAllTabs();
        }
    }

    public void showAddGoalDialog() {
        if (!"all".equals(tabType)) {
            Toast.makeText(getContext(), "전체 목표 탭에서만 추가할 수 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_goal, null);
        EditText etTitle = dialogView.findViewById(R.id.etGoalTitle);
        EditText etTime = dialogView.findViewById(R.id.etGoalTime);
        ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroupDays);

        Set<Integer> selectedDays = new HashSet<>();
        int[] chipIds = {R.id.chipSun, R.id.chipMon, R.id.chipTue, R.id.chipWed, R.id.chipThu, R.id.chipFri, R.id.chipSat};
        for (int i = 0; i < chipIds.length; i++) {
            final int dayIdx = i;
            Chip chip = dialogView.findViewById(chipIds[i]);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedDays.add(dayIdx);
                else selectedDays.remove(dayIdx);
            });
        }

        etTime.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (getActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
            TimePickerDialog dialog = new TimePickerDialog(getContext(), (view1, hour, minute) -> {
                etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }, 8, 0, true);
            dialog.show();
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("새 목표 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String time = etTime.getText().toString().trim();
                    if (!title.isEmpty() && !time.isEmpty() && !selectedDays.isEmpty()) {
                        Goal newGoal = new Goal(title, time, new ArrayList<>(selectedDays));
                        newGoal.setCreatedDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime()));
                        newGoal.setUserId(sessionManager.getUserId());

                        firestoreHelper.addGoal(newGoal, this::refreshAll, () ->
                                Toast.makeText(getContext(), "저장 실패", Toast.LENGTH_SHORT).show()
                        );
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    public void showEditGoalDialog(Goal goal) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_goal, null);
        EditText etTitle = dialogView.findViewById(R.id.etGoalTitle);
        EditText etTime = dialogView.findViewById(R.id.etGoalTime);
        ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroupDays);

        etTitle.setText(goal.getTitle());
        etTime.setText(goal.getTime());

        Set<Integer> selectedDays = new HashSet<>(goal.getDaysOfWeek());
        int[] chipIds = {R.id.chipSun, R.id.chipMon, R.id.chipTue, R.id.chipWed, R.id.chipThu, R.id.chipFri, R.id.chipSat};
        for (int i = 0; i < chipIds.length; i++) {
            final int dayIdx = i;
            Chip chip = dialogView.findViewById(chipIds[i]);
            chip.setChecked(selectedDays.contains(dayIdx));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedDays.add(dayIdx);
                else selectedDays.remove(dayIdx);
            });
        }

        etTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(getContext(), (view1, hour, minute) -> {
                etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }, 8, 0, true);
            dialog.show();
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("목표 수정")
                .setView(dialogView)
                .setPositiveButton("저장", (dialog, which) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String newTime = etTime.getText().toString().trim();
                    if (!newTitle.isEmpty() && !newTime.isEmpty() && !selectedDays.isEmpty()) {
                        goal.setTitle(newTitle);
                        goal.setTime(newTime);
                        goal.setDaysOfWeek(new ArrayList<>(selectedDays));
                        firestoreHelper.updateGoal(goal, this::refreshAll, () ->
                                Toast.makeText(getContext(), "수정 실패", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    @Override
    public void onGoalCheckedChanged(Goal goal) {
        firestoreHelper.updateGoalStatus(goal, goal.isCompleted(), goal.getCheckedDate());

        for (Goal g : SharedGoalList.get().getAllGoals()) {
            if (g.getGoalId().equals(goal.getGoalId())) {
                g.setCompleted(goal.isCompleted());
                g.setCheckedDate(goal.getCheckedDate());
                break;
            }
        }

        updateReport(SharedGoalList.get().getAllGoals());
    }

    private void updateReport(List<Goal> goals) {
        if (!"today".equals(tabType) || tvReport == null) return;
        int total = goals.size(), done = 0;

        for (Goal g : goals) {
            if (g.isCompleted()) done++;
        }

        int percent = total == 0 ? 0 : (int) ((done * 100.0) / total);
        tvReport.setText("오늘 목표 달성률: " + percent + "%");
    }

    private boolean isWithin15Minutes(String goalTimeStr, String nowStr) {
        try {
            String[] parts = goalTimeStr.split(":"), nowParts = nowStr.split(":");
            int goalHour = Integer.parseInt(parts[0]), goalMinute = Integer.parseInt(parts[1]);
            int nowHour = Integer.parseInt(nowParts[0]), nowMinute = Integer.parseInt(nowParts[1]);

            Calendar goal = Calendar.getInstance();
            goal.set(Calendar.HOUR_OF_DAY, goalHour);
            goal.set(Calendar.MINUTE, goalMinute);

            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, nowHour);
            now.set(Calendar.MINUTE, nowMinute);

            long diffMillis = Math.abs(now.getTimeInMillis() - goal.getTimeInMillis());
            return (diffMillis / (60 * 1000)) <= 15;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getNowTime() {
        Calendar cal = Calendar.getInstance();
        return String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    public static List<Integer> getTodayIndices() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return Collections.singletonList((day - 1) % 7);
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder a, @NonNull RecyclerView.ViewHolder b) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                Goal g = adapter.getGoal(pos);
                adapter.removeItem(pos);
                firestoreHelper.deleteGoal(g);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }
}
