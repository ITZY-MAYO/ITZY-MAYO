package com.syu.itzy_mayo;

import android.os.Bundle;
import java.util.Locale;

public class MathGameActivity extends BaseGameActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_math_game;
    }

    @Override
    protected int getGameContentLayoutRes() {
        return R.layout.math_game_content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // BaseGameActivity에서 모든 초기화 수행됨
    }

    @Override
    protected void generateNewQuestion() {
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        int operator = random.nextInt(3); // 0:+, 1:-, 2:*
        String op;

        switch (operator) {
            case 0: op = "+"; correctAnswer = a + b; break;
            case 1: op = "-"; correctAnswer = a - b; break;
            default: op = "\u00D7"; correctAnswer = a * b; break;
        }

        questionText.setText(String.format(Locale.getDefault(), "%d %s %d = ?", a, op, b));
        int correctIndex = random.nextInt(4);

        for (int i = 0; i < 4; i++) {
            if (i == correctIndex) {
                answerButtons[i].setText(String.valueOf(correctAnswer));
            } else {
                int wrong;
                do {
                    wrong = correctAnswer + random.nextInt(11) - 5;
                } while (wrong == correctAnswer);
                answerButtons[i].setText(String.valueOf(wrong));
            }
        }

        startQuestionTimer();
    }
}
