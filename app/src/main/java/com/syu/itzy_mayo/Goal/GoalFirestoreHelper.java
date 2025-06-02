package com.syu.itzy_mayo.Goal;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GoalFirestoreHelper {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addGoal(Goal goal, Runnable onSuccess, Runnable onFailure) {
        DocumentReference ref = db.collection("goals").document();
        goal.setGoalId(ref.getId());
        ref.set(goal)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Goal added with ID: " + ref.getId());
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding goal", e);
                    onFailure.run();
                });
    }

    public void loadGoals(String userId, boolean todayOnly, Consumer<List<Goal>> callback) {
        db.collection("goals")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Goal> goalList = new ArrayList<>();
                    int todayIndex = GoalTabFragment.getTodayIndices().get(0);

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Goal goal = doc.toObject(Goal.class);
                        if (goal == null) continue;
                        goal.setGoalId(doc.getId());

                        Object rawList = doc.get("daysOfWeek");
                        List<Integer> parsedDays = new ArrayList<>();
                        if (rawList instanceof List<?>) {
                            for (Object obj : (List<?>) rawList) {
                                if (obj instanceof Number) {
                                    parsedDays.add(((Number) obj).intValue());
                                }
                            }
                            goal.setDaysOfWeek(parsedDays);
                        }

                        if (!todayOnly || (goal.getDaysOfWeek() != null && goal.getDaysOfWeek().contains(todayIndex))) {
                            goalList.add(goal);
                        }
                    }

                    callback.accept(goalList);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading goals", e);
                    callback.accept(new ArrayList<>());
                });
    }

    public void updateGoalStatus(Goal goal, boolean isCompleted, String checkedDate) {
        if (goal.getGoalId() == null) return;
        db.collection("goals")
                .document(goal.getGoalId())
                .update("isCompleted", isCompleted, "checkedDate", checkedDate);
    }

    public void deleteGoal(Goal goal) {
        if (goal.getGoalId() == null) return;
        db.collection("goals")
                .document(goal.getGoalId())
                .delete();
    }

    public void updateGoal(Goal goal, Runnable onSuccess, Runnable onFailure) {
        if (goal.getGoalId() == null) {
            onFailure.run();
            return;
        }
        db.collection("goals")
                .document(goal.getGoalId())
                .set(goal)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.run());
    }
}
