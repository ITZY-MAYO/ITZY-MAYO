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

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Random;

public class MathGameActivity extends AppCompatActivity {

    private TextView questionText;
    private TextView timerText;
    private TextView runtimeText;
    private TextView scoreText;
    private final Button[] answerButtons = new Button[4];

    private int correctAnswer;
    private int score = 0;
    private int totalSeconds = 0;

    private CountDownTimer countDownTimer;
    private final Handler runtimeHandler = new Handler();
    private final Runnable runtimeRunnable = new Runnable() {
        @Override
        public void run() {
            totalSeconds++;
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            String timeFormatted = getString(R.string.timer_format, minutes, seconds);
            runtimeText.setText(timeFormatted);
            runtimeHandler.postDelayed(this, 1000);
        }
    };

    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_game);

        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());

        FrameLayout gameInnerContainer = findViewById(R.id.game_inner_container);
        View gameContent = LayoutInflater.from(this).inflate(R.layout.math_game_content, gameInnerContainer, false);
        gameInnerContainer.addView(gameContent);

        questionText = gameContent.findViewById(R.id.text_question);
        timerText = gameContent.findViewById(R.id.text_timer);
        runtimeText = findViewById(R.id.text_runtime); // 아래에 있는 상태바는 include 밖에 있음
        scoreText = findViewById(R.id.text_score);

        answerButtons[0] = gameContent.findViewById(R.id.btn_answer_1);
        answerButtons[1] = gameContent.findViewById(R.id.btn_answer_2);
        answerButtons[2] = gameContent.findViewById(R.id.btn_answer_3);
        answerButtons[3] = gameContent.findViewById(R.id.btn_answer_4);

        for (Button button : answerButtons) {
            button.setOnClickListener(v -> checkAnswer(((Button) v).getText().toString()));
        }

        runtimeHandler.postDelayed(runtimeRunnable, 1000);
        generateNewQuestion();
    }

    private void generateNewQuestion() {
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        int operator = random.nextInt(3); // 0:+, 1:-, 2:*
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

        startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                String timeText = getString(R.string.runtime_format, millisUntilFinished / 1000);
                timerText.setText(timeText);
            }
            public void onFinish() {
                timerText.setText(getString(R.string.runtime_format, 0));
                generateNewQuestion();
            }
        }.start();
    }

    private void checkAnswer(String userAnswer) {
        if (Integer.parseInt(userAnswer) == correctAnswer) {
            score++;
            scoreText.setText(getString(R.string.score_format, score));
            generateNewQuestion();
        } else {
            timerText.setText(getString(R.string.wrong_text));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        runtimeHandler.removeCallbacks(runtimeRunnable);
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
