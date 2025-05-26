package com.syu.itzy_mayo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

public class GuessNumberActivity extends BaseGameActivity {

    private EditText inputNumber;
    private Button guessButton, restartButton;
    private TextView resultText, attemptsText;

    private int targetNumber;
    private int remainingAttempts;
    private int maxAttempts;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_guess_number;
    }

    @Override
    protected int getGameContentLayoutRes() {
        return R.layout.guess_number_game_content;
    }

    @Override
    protected boolean useRuntimeTimer() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inputNumber = findViewById(R.id.inputNumber);
        guessButton = findViewById(R.id.guessButton);
        restartButton = findViewById(R.id.restartButton);
        resultText = findViewById(R.id.resultText);
        attemptsText = findViewById(R.id.attemptsText);

        String difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) difficulty = getString(R.string.difficulty_normal); // 안전하게

        setAttemptsByDifficulty(difficulty);

        restartButton.setOnClickListener(v -> startGame());
        guessButton.setOnClickListener(v -> checkGuess());

        startGame();
    }

    private void setAttemptsByDifficulty(String difficulty) {
        if (difficulty.equals(getString(R.string.difficulty_easy))) {
            maxAttempts = 20;
        } else if (difficulty.equals(getString(R.string.difficulty_hard))) {
            maxAttempts = 10;
        } else {
            maxAttempts = 15;
        }
    }

    private void startGame() {
        targetNumber = new Random().nextInt(100) + 1;
        remainingAttempts = maxAttempts;

        inputNumber.setText("");
        resultText.setText(getString(R.string.enter_number_hint));  // "숫자를 입력하세요 (1~100)"
        updateAttemptsText();
        guessButton.setEnabled(true);
    }

    private void checkGuess() {
        String input = inputNumber.getText().toString().trim();
        if (input.isEmpty()) return;

        int guess = Integer.parseInt(input);
        remainingAttempts--;

        if (guess < targetNumber) {
            resultText.setText(getString(R.string.wrong_up));
        } else if (guess > targetNumber) {
            resultText.setText(getString(R.string.wrong_down));
        } else {
            resultText.setText(getString(R.string.correct_answer));
            guessButton.setEnabled(false);
            return;
        }

        if (remainingAttempts == 0) {
            resultText.setText(getString(R.string.game_failed, targetNumber)); // %1$d
            guessButton.setEnabled(false);
        }

        updateAttemptsText();
        inputNumber.setText("");
    }

    private void updateAttemptsText() {
        attemptsText.setText(getString(R.string.remaining_attempts_format, remainingAttempts, maxAttempts));
    }
}