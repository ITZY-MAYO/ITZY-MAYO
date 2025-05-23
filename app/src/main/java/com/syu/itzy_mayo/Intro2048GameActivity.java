package com.syu.itzy_mayo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Intro2048GameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_game_2048);

        TextView guide = findViewById(R.id.text_game_guide);
        guide.setText("🔢 2048 게임\n\n같은 숫자를 합쳐서 2048을 만들어보세요!\n슬라이드로 숫자를 이동하세요.");

        Button start = findViewById(R.id.btn_start);
        start.setOnClickListener(v -> {
            Intent intent = new Intent(this, Game2048Activity.class);
            startActivity(intent);
        });

        ImageButton back = findViewById(R.id.btn_back);
        back.setOnClickListener(v -> finish());
    }
}