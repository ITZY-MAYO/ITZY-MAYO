package com.syu.itzy_mayo.Goal;

import java.util.List;
import java.util.Objects;

public class Goal {
    private String title;
    private String time;
    private List<Integer> daysOfWeek;
    private boolean isCompleted;
    private String checkedDate;
    private String createdDate;
    private String userId;

    public Goal() {} // Firestore 기본 생성자

    public Goal(String title, String time, List<Integer> daysOfWeek) {
        this.title = title;
        this.time = time;
        this.daysOfWeek = daysOfWeek;
        this.isCompleted = false;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public List<Integer> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<Integer> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getCheckedDate() { return checkedDate; }
    public void setCheckedDate(String checkedDate) { this.checkedDate = checkedDate; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Goal goal = (Goal) obj;

        return isCompleted == goal.isCompleted &&
                Objects.equals(title, goal.title) &&
                Objects.equals(time, goal.time) &&
                Objects.equals(daysOfWeek, goal.daysOfWeek) &&
                Objects.equals(checkedDate, goal.checkedDate) &&
                Objects.equals(createdDate, goal.createdDate) &&
                Objects.equals(userId, goal.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, time, daysOfWeek, isCompleted, checkedDate, createdDate, userId);
    }

}