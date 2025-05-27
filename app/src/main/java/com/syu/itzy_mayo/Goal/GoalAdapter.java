package com.syu.itzy_mayo.Goal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.syu.itzy_mayo.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private final List<Goal> goalList = new ArrayList<>();
    private final boolean todayOnly;
    private final OnGoalCheckListener checkListener;
    private final GoalClickListener clickListener;
    private final String nowTime;

    public interface OnGoalCheckListener {
        void onGoalCheckedChanged(Goal goal);
    }

    public interface GoalClickListener {
        void onGoalLongClick(Goal goal);
    }

    public GoalAdapter(OnGoalCheckListener checkListener, boolean todayOnly, String nowTime, GoalClickListener clickListener) {
        this.checkListener = checkListener;
        this.todayOnly = todayOnly;
        this.nowTime = nowTime;
        this.clickListener = clickListener;
    }

    public void submitList(List<Goal> list) {
        if (goalList.equals(list)) return; // 같으면 갱신 안 함
        goalList.clear();
        goalList.addAll(list);
        notifyDataSetChanged();
    }

    public Goal getGoal(int position) {
        return goalList.get(position);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < goalList.size()) {
            goalList.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal_card, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goalList.get(position);
        holder.title.setText(goal.getTitle());
        holder.time.setText(goal.getTime());
        holder.days.setText(formatDaysOfWeek(goal.getDaysOfWeek()));
        holder.checkBox.setOnCheckedChangeListener(null);

        if (todayOnly) {
            boolean isToday = goal.getDaysOfWeek().contains(getTodayIndex());
            boolean isInRange = isWithin15Minutes(goal.getTime(), nowTime);

            if (isToday && isInRange) {
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setChecked(goal.isCompleted());
                holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    goal.setCompleted(isChecked);
                    goal.setCheckedDate(nowTime);
                    checkListener.onGoalCheckedChanged(goal);
                    notifyItemChanged(holder.getAdapterPosition());
                });
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(v -> {
            clickListener.onGoalLongClick(goal);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView title, time, days;
        CheckBox checkBox;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvGoalTitle);
            time = itemView.findViewById(R.id.tvGoalTime);
            days = itemView.findViewById(R.id.tvGoalDays);
            checkBox = itemView.findViewById(R.id.cbGoalDone);
        }
    }

    private int getTodayIndex() {
        int javaDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return (javaDay - 1) % 7;
    }

    private boolean isWithin15Minutes(String goalTimeStr, String nowStr) {
        try {
            String[] parts = goalTimeStr.split(":");
            int goalHour = Integer.parseInt(parts[0]);
            int goalMinute = Integer.parseInt(parts[1]);

            String[] nowParts = nowStr.split(":");
            int nowHour = Integer.parseInt(nowParts[0]);
            int nowMinute = Integer.parseInt(nowParts[1]);

            Calendar goal = Calendar.getInstance();
            goal.set(Calendar.HOUR_OF_DAY, goalHour);
            goal.set(Calendar.MINUTE, goalMinute);

            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, nowHour);
            now.set(Calendar.MINUTE, nowMinute);

            long diffMillis = Math.abs(now.getTimeInMillis() - goal.getTimeInMillis());
            long diffMinutes = diffMillis / (60 * 1000);

            return diffMinutes <= 15;
        } catch (Exception e) {
            return false;
        }
    }

    private String formatDaysOfWeek(List<Integer> days) {
        if (days == null || days.isEmpty()) return "";
        String[] kor = {"일", "월", "화", "수", "목", "금", "토"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.size(); i++) {
            sb.append(kor[days.get(i)]);
            if (i < days.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
