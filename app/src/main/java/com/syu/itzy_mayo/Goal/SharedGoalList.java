package com.syu.itzy_mayo.Goal;

import java.util.ArrayList;
import java.util.List;

public class SharedGoalList {
    private static SharedGoalList instance;
    private List<Goal> allGoals = new ArrayList<>();

    private SharedGoalList() {}

    public static SharedGoalList get() {
        if (instance == null) instance = new SharedGoalList();
        return instance;
    }

    public void setAllGoals(List<Goal> goals) {
        this.allGoals = goals;
    }

    public List<Goal> getAllGoals() {
        return allGoals;
    }
}