package com.syu.itzy_mayo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class IntroMathGameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_game_math);

        TextView guide = findViewById(R.id.text_game_guide);
        guide.setText("🧮 수학 게임\n\n랜덤 수식에 대한 4지선다 퀴즈입니다.\n제한 시간 내에 정답을 고르세요!");

        Button start = findViewById(R.id.btn_start);
        start.setOnClickListener(v -> {
            Intent intent = new Intent(this, MathGameActivity.class);
            startActivity(intent);
        });

        ImageButton back = findViewById(R.id.btn_back);
        back.setOnClickListener(v -> finish());
    }
}