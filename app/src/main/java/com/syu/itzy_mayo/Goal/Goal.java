package com.syu.itzy_mayo.Goal;

import java.util.ArrayList;
import java.util.List;

public class Goal {
    private String goalId;
    private String title;
    private String time;
    private List<Integer> daysOfWeek = new ArrayList<>();
    private String createdDate;
    private String userId;
    private boolean isCompleted;
    private String checkedDate;

    public Goal() {}

    public Goal(String title, String time, List<Integer> daysOfWeek) {
        this.title = title;
        this.time = time;
        this.daysOfWeek = daysOfWeek;
    }

    // --- Getter & Setter ---

    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public List<Integer> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<Integer> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getCheckedDate() { return checkedDate; }
    public void setCheckedDate(String checkedDate) { this.checkedDate = checkedDate; }
}
