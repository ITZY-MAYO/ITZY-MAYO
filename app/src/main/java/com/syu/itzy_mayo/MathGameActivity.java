package com.syu.itzy_mayo;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import java.util.Locale;

public class MathGameActivity extends BaseGameActivity {

    private View pauseOverlay;
    private final Handler pauseHandler = new Handler();
    private final Runnable pauseCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (pauseOverlay != null) {
                pauseOverlay.setVisibility(isGamePaused() ? View.VISIBLE : View.GONE);
            }
            pauseHandler.postDelayed(this, 100);
        }
    };

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

        // 오버레이 뷰 연결
        pauseOverlay = findViewById(R.id.pause_overlay);

        // pause 상태 감지 루프 시작
        pauseHandler.postDelayed(pauseCheckRunnable, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pauseHandler.removeCallbacks(pauseCheckRunnable);
    }

    @Override
    protected void generateNewQuestion() {
        if (isGamePaused()) return; // 일시정지 중엔 문제 새로 생성 안 함

        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        int operator = random.nextInt(3); // 0:+, 1:-, 2:*
        String op;

        switch (operator) {
            case 0: op = "+"; correctAnswer = a + b; break;
            case 1: op = "-"; correctAnswer = a - b; break;
            default: op = "x"; correctAnswer = a * b; break;
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