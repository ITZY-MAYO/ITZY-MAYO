package com.syu.itzy_mayo;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Random;

/**
 * 공통 게임 Activity 템플릿
 * - game_box_container.xml을 include한 레이아웃 사용
 * - 뒤로가기, 일시정지, 타이머, 점수, 시간 등 공통 로직 포함
 * - 각 게임 콘텐츠만 getGameContentLayoutRes()로 지정
 */
public abstract class BaseGameActivity extends AppCompatActivity {

    protected FrameLayout gameInnerContainer;
    private ImageButton pauseButton;
    private boolean isPaused = false;
    public boolean isGamePaused() {
        return isPaused;
    }

    protected TextView questionText;
    protected TextView timerText;
    protected TextView runtimeText;
    protected TextView scoreText;
    protected final Button[] answerButtons = new Button[4];

    protected final Handler runtimeHandler = new Handler();
    protected CountDownTimer questionTimer;
    protected final Random random = new Random();

    private int totalSeconds = 0;
    private int score = 0;
    protected int correctAnswer;

    @LayoutRes
    protected abstract int getLayoutResId();

    @LayoutRes
    protected abstract int getGameContentLayoutRes();

    /**
     * 일부 게임에서 runtime 타이머를 끌 수 있음 (예: 2048 등)
     */
    protected boolean useRuntimeTimer() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());

        pauseButton = findViewById(R.id.btn_pause);
        pauseButton.setOnClickListener(v -> togglePause());

        gameInnerContainer = findViewById(R.id.game_inner_container);
        int contentLayoutRes = getGameContentLayoutRes();
        if (contentLayoutRes != 0) {
            View content = LayoutInflater.from(this).inflate(contentLayoutRes, gameInnerContainer, false);
            gameInnerContainer.addView(content);

            questionText = content.findViewById(R.id.text_question);
            timerText = content.findViewById(R.id.text_timer);
            answerButtons[0] = content.findViewById(R.id.btn_answer_1);
            answerButtons[1] = content.findViewById(R.id.btn_answer_2);
            answerButtons[2] = content.findViewById(R.id.btn_answer_3);
            answerButtons[3] = content.findViewById(R.id.btn_answer_4);

            for (Button button : answerButtons) {
                button.setOnClickListener(v -> checkAnswer(((Button) v).getText().toString()));
            }
        }

        runtimeText = findViewById(R.id.text_runtime);
        scoreText = findViewById(R.id.text_score);

        if (useRuntimeTimer()) {
            runtimeHandler.postDelayed(runtimeRunnable, 1000);
        }

        if (contentLayoutRes != 0) {
            generateNewQuestion();
        }
    }

    private void togglePause() {
        isPaused = !isPaused;
        pauseButton.setImageResource(isPaused ? R.drawable.play_fill : R.drawable.pause_fill);
        if (isPaused) {
            pauseGame();
        } else {
            resumeGame();
        }
    }

    private void pauseGame() {
        if (questionTimer != null) questionTimer.cancel();
        runtimeHandler.removeCallbacks(runtimeRunnable);
        for (Button btn : answerButtons) {
            if (btn != null) btn.setEnabled(false);
        }
    }

    private void resumeGame() {
        startQuestionTimer();
        if (useRuntimeTimer()) {
            runtimeHandler.postDelayed(runtimeRunnable, 1000);
        }
        for (Button btn : answerButtons) {
            if (btn != null) btn.setEnabled(true);
        }
    }

    private final Runnable runtimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPaused || runtimeText == null) return;

            totalSeconds++;
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            String timeFormatted = getString(R.string.timer_format, minutes, seconds);
            runtimeText.setText(timeFormatted);
            runtimeHandler.postDelayed(this, 1000);
        }
    };

    protected void generateNewQuestion() {
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        int operator = random.nextInt(3);
        String op;

        switch (operator) {
            case 0: op = "+"; correctAnswer = a + b; break;
            case 1: op = "-"; correctAnswer = a - b; break;
            default: op = "×"; correctAnswer = a * b; break;
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

    protected void startQuestionTimer() {
        if (questionTimer != null) questionTimer.cancel();
        questionTimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                if (timerText != null) {
                    timerText.setText(getString(R.string.runtime_format, millisUntilFinished / 1000));
                }
            }
            public void onFinish() {
                if (timerText != null) {
                    timerText.setText(getString(R.string.runtime_format, 0));
                }
                generateNewQuestion();
            }
        }.start();
    }

    private void checkAnswer(String userAnswer) {
        if (Integer.parseInt(userAnswer) == correctAnswer) {
            score++;
            if (scoreText != null) {
                scoreText.setText(getString(R.string.score_format, score));
            }
            generateNewQuestion();
        } else {
            if (timerText != null) {
                timerText.setText(getString(R.string.wrong_text));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        runtimeHandler.removeCallbacks(runtimeRunnable);
        if (questionTimer != null) questionTimer.cancel();
    }
}
