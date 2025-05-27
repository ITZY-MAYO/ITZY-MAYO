package com.syu.itzy_mayo.Goal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;

public class SharedGoalList {
    private static final SharedGoalList instance = new SharedGoalList();
    private final List<Goal> allGoals = new ArrayList<>();
    public static SharedGoalList get() { return instance; }

    public List<Goal> getAllGoals() { return allGoals; }

    public List<Goal> getTodayGoals() {
        Calendar calendar = Calendar.getInstance();
        int todayDow = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        List<Goal> filtered = new ArrayList<>();
        for (Goal g : allGoals) {
            if (g.daysOfWeek != null && g.daysOfWeek.contains(todayDow)) {
                filtered.add(g);
            }
        }
        return filtered;
    }

    public void addGoal(Goal g) { allGoals.add(g); }
    public void removeGoal(Goal g) { allGoals.remove(g);}
}