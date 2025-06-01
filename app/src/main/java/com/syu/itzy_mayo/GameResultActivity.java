package com.syu.itzy_mayo;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_game_result); // 이 레이아웃 존재해야 함

        TextView scoreText = findViewById(R.id.text_score);
        TextView timeText = findViewById(R.id.text_time);

        int score = getIntent().getIntExtra("score", 0);
        String time = getIntent().getStringExtra("time");

        scoreText.setText(getString(R.string.score_format, score));
        timeText.setText(getString(R.string.result_time_format, time));
    }
}