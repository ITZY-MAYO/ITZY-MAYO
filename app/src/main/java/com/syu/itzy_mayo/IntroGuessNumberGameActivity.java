package com.syu.itzy_mayo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class IntroGuessNumberGameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_game_guess_number);

        TextView guide = findViewById(R.id.text_game_guide);
        guide.setText("🔢 숫자 추측 게임\n\n1~100 사이의 숫자를 맞추세요!\n난이도에 따라 기회가 달라집니다.");

        Button start = findViewById(R.id.btn_start);
        start.setOnClickListener(v -> {
            Intent intent = new Intent(this, GuessNumberActivity.class);
            startActivity(intent);
        });

        ImageButton back = findViewById(R.id.btn_back);
        back.setOnClickListener(v -> finish());
    }
}