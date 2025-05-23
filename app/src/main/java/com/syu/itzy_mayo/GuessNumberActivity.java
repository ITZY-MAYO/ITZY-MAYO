package com.syu.itzy_mayo;

import android.os.Bundle;
import android.widget.*;

import java.util.Random;

public class GuessNumberActivity extends BaseGameActivity {

    private Spinner difficultySpinner;
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
        return true; // 필요시 false
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        difficultySpinner = findViewById(R.id.difficultySpinner);
        inputNumber = findViewById(R.id.inputNumber);
        guessButton = findViewById(R.id.guessButton);
        restartButton = findViewById(R.id.restartButton);
        resultText = findViewById(R.id.resultText);
        attemptsText = findViewById(R.id.attemptsText);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);

        restartButton.setOnClickListener(v -> startGame());
        guessButton.setOnClickListener(v -> checkGuess());

        startGame();
    }

    private void startGame() {
        String difficulty = difficultySpinner.getSelectedItem().toString();
        switch (difficulty) {
            case "쉬움 (20회)": maxAttempts = 20; break;
            case "보통 (15회)": maxAttempts = 15; break;
            case "어려움 (10회)": maxAttempts = 10; break;
            default: maxAttempts = 15;
        }

        targetNumber = new Random().nextInt(100) + 1;
        remainingAttempts = maxAttempts;

        inputNumber.setText("");
        resultText.setText("숫자를 입력하세요 (1~100)");
        updateAttemptsText();
        guessButton.setEnabled(true);
    }

    private void checkGuess() {
        String input = inputNumber.getText().toString().trim();
        if (input.isEmpty()) return;

        int guess = Integer.parseInt(input);
        remainingAttempts--;

        if (guess < targetNumber) {
            resultText.setText("UP!");
        } else if (guess > targetNumber) {
            resultText.setText("DOWN!");
        } else {
            resultText.setText("정답입니다! 🎉");
            guessButton.setEnabled(false);
            return;
        }

        if (remainingAttempts == 0) {
            resultText.setText("실패! 정답은 " + targetNumber);
            guessButton.setEnabled(false);
        }

        updateAttemptsText();
        inputNumber.setText("");
    }

    private void updateAttemptsText() {
        attemptsText.setText("남은 기회: " + remainingAttempts + " / " + maxAttempts);
    }
}