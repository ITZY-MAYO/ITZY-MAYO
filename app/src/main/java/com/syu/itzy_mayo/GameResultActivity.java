package com.syu.itzy_mayo;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_game_result);

        findViewById(R.id.game_result_panel).setVisibility(View.VISIBLE);
        TextView scoreText = findViewById(R.id.text_score);
        TextView timeText = findViewById(R.id.text_time);

        int score = getIntent().getIntExtra("score", 0);
        String time = getIntent().getStringExtra("time");

        scoreText.setText(getString(R.string.score_format, score));
        timeText.setText(getString(R.string.result_time_format, time));
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}