package com.syu.itzy_mayo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class IntroMemoryGameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_game_memory);

        TextView guide = findViewById(R.id.text_game_guide);
        guide.setText("🧠 메모리 게임\n\n뒤집힌 카드를 두 개씩 맞추세요.\n모든 쌍을 찾으면 게임 클리어!");

        Button start = findViewById(R.id.btn_start);
        start.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemoryGameActivity.class);
            startActivity(intent);
        });

        ImageButton back = findViewById(R.id.btn_back);
        back.setOnClickListener(v -> finish());
    }
}