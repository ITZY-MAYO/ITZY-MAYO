package com.syu.itzy_mayo.Goal;

import java.util.List;
import java.util.Objects;

public class Goal {
    public String title;
    public String time;
    public List<Integer> daysOfWeek;
    public boolean isCompleted;
    public String checkedDate;
    public String createdDate;

    public Goal(String title, String time, List<Integer> daysOfWeek) {
        this.title = title;
        this.time = time;
        this.daysOfWeek = daysOfWeek;
        this.isCompleted = false;
        this.checkedDate = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Goal)) return false;
        Goal goal = (Goal) obj;
        return Objects.equals(title, goal.title)
                && Objects.equals(time, goal.time)
                && Objects.equals(daysOfWeek, goal.daysOfWeek)
                && isCompleted == goal.isCompleted
                && Objects.equals(checkedDate, goal.checkedDate);
    }
}