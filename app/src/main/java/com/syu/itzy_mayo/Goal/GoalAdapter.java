
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

    public interface OnGoalCheckListener {
        void onGoalCheckedChanged(Goal goal);
    }

    public interface GoalClickListener {
        void onGoalLongClick(Goal goal);
    }

    public GoalAdapter(OnGoalCheckListener checkListener, boolean todayOnly, GoalClickListener clickListener) {
        this.checkListener = checkListener;
        this.todayOnly = todayOnly;
        this.clickListener = clickListener;
    }

    public void submitList(List<Goal> list) {
        goalList.clear();
        goalList.addAll(list);
        notifyDataSetChanged();
    }

    public Goal getGoal(int position) {
        return goalList.get(position);
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

        if (todayOnly) {
            boolean isToday = goal.getDaysOfWeek().contains(getTodayIndex());
            boolean isTimePassed = getNowTime().compareTo(goal.getTime()) >= 0;

            if (isToday && isTimePassed) {
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setOnCheckedChangeListener(null);
                holder.checkBox.setChecked(goal.isCompleted());
                holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    goal.setCompleted(isChecked);
                    goal.setCheckedDate(getNowTime());
                    checkListener.onGoalCheckedChanged(goal);
                });
            } else {
                holder.checkBox.setVisibility(View.GONE);
                holder.checkBox.setOnCheckedChangeListener(null);
            }
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.checkBox.setOnCheckedChangeListener(null);
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

    private String getNowTime() {
        Calendar cal = Calendar.getInstance();
        return String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
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
