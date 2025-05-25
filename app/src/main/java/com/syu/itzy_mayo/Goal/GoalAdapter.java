package com.syu.itzy_mayo.Goal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.syu.itzy_mayo.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GoalAdapter extends ListAdapter<Goal, GoalAdapter.GoalViewHolder> {

    private final boolean todayTab;
    private final String nowTime;
    private final String todayDate;

    public interface OnGoalCheckListener {
        void onGoalCheckedChanged(Goal goal);
    }

    private final OnGoalCheckListener checkListener;

    public GoalAdapter(OnGoalCheckListener listener, boolean todayTab, String nowTime) {
        super(DIFF_CALLBACK);
        this.checkListener = listener;
        this.todayTab = todayTab;
        this.nowTime = nowTime;
        this.todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    public static final DiffUtil.ItemCallback<Goal> DIFF_CALLBACK = new DiffUtil.ItemCallback<Goal>() {
        @Override
        public boolean areItemsTheSame(@NonNull Goal oldItem, @NonNull Goal newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getTime().equals(newItem.getTime())
                    && oldItem.getDaysOfWeek().equals(newItem.getDaysOfWeek());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Goal oldItem, @NonNull Goal newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal_card, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = getItem(position);
        holder.tvTitle.setText(goal.getTitle());
        holder.tvTime.setText(goal.getTime());
        holder.tvDays.setText(daysOfWeekText(goal.getDaysOfWeek()));

        if (!todayTab) {
            holder.cbGoalDone.setVisibility(View.GONE);
            holder.cbGoalDone.setEnabled(false);
            holder.tvGoalStatus.setVisibility(View.GONE);
            return;
        }

        if (goal.getDaysOfWeek() != null && daysContainToday(goal.getDaysOfWeek())) {
            boolean enableCheck = isCheckAvailableTime(goal.getTime(), nowTime);
            holder.cbGoalDone.setVisibility(View.VISIBLE);
            holder.cbGoalDone.setEnabled(enableCheck);
            holder.cbGoalDone.setOnCheckedChangeListener(null);

            boolean checkedToday = todayDate.equals(goal.getCheckedDate());
            holder.cbGoalDone.setChecked(checkedToday);
            holder.tvGoalStatus.setVisibility(checkedToday ? View.VISIBLE : View.GONE);

            holder.cbGoalDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (enableCheck) {
                    goal.setCompleted(isChecked);
                    goal.setCheckedDate(isChecked ? todayDate : null);

                    // 🔥 Firestore 업데이트
                    FirebaseFirestore.getInstance().collection("goal")
                            .whereEqualTo("userId", goal.getUserId())
                            .whereEqualTo("title", goal.getTitle())
                            .whereEqualTo("time", goal.getTime())
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    Map<String, Object> update = new HashMap<>();
                                    update.put("isCompleted", isChecked);
                                    update.put("checkedDate", goal.getCheckedDate());
                                    doc.getReference().set(update, SetOptions.merge());
                                }
                            });

                    if (checkListener != null) checkListener.onGoalCheckedChanged(goal);
                    notifyItemChanged(position);
                }
            });
        } else {
            holder.cbGoalDone.setVisibility(View.GONE);
            holder.cbGoalDone.setEnabled(false);
            holder.tvGoalStatus.setVisibility(View.GONE);
        }
    }

    private boolean daysContainToday(List<Integer> daysOfWeek) {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        return daysOfWeek.contains(today);
    }

    public static boolean isCheckAvailableTime(String goalTime, String nowTime) {
        try {
            String[] gSplit = goalTime.split(":");
            String[] nSplit = nowTime.split(":");
            int goalMin = Integer.parseInt(gSplit[0]) * 60 + Integer.parseInt(gSplit[1]);
            int nowMin = Integer.parseInt(nSplit[0]) * 60 + Integer.parseInt(nSplit[1]);
            return nowMin >= (goalMin - 15) && nowMin <= (goalMin + 15);
        } catch (Exception e) {
            return false;
        }
    }

    public Goal getGoal(int position) {
        return getItem(position);
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvDays, tvGoalStatus;
        CheckBox cbGoalDone;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvGoalTitle);
            tvTime = itemView.findViewById(R.id.tvGoalTime);
            tvDays = itemView.findViewById(R.id.tvGoalDays);
            cbGoalDone = itemView.findViewById(R.id.cbGoalDone);
            tvGoalStatus = itemView.findViewById(R.id.tvGoalStatus);
        }
    }

    public static String daysOfWeekText(List<Integer> days) {
        if (days == null) return "";
        String[] dow = {"일", "월", "화", "수", "목", "금", "토"};
        List<Integer> sortedDays = new ArrayList<>(days);
        java.util.Collections.sort(sortedDays);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sortedDays.size(); i++) {
            sb.append(dow[sortedDays.get(i)]);
            if (i != sortedDays.size() - 1) sb.append(",");
        }
        return sb.toString();
    }
}
