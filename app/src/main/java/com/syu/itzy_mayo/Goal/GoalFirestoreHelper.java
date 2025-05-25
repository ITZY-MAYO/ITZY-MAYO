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
        db.collection("goals")
                .add(goal)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Goal added with ID: " + documentReference.getId());
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
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Goal goal = doc.toObject(Goal.class);
                        if (goal == null) continue;
                        if (!todayOnly || (goal.getDaysOfWeek() != null && goal.getDaysOfWeek().containsAll(GoalTabFragment.getTodayIndices()))) {
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
        db.collection("goals")
                .whereEqualTo("userId", goal.getUserId())
                .whereEqualTo("title", goal.getTitle())
                .whereEqualTo("time", goal.getTime())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        DocumentReference ref = doc.getReference();
                        ref.update("isCompleted", isCompleted, "checkedDate", checkedDate);
                    }
                });
    }

    public void deleteGoal(Goal goal) {
        db.collection("goals")
                .whereEqualTo("userId", goal.getUserId())
                .whereEqualTo("title", goal.getTitle())
                .whereEqualTo("time", goal.getTime())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                });
    }
}

