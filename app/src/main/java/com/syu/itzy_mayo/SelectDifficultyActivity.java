package com.syu.itzy_mayo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class SelectDifficultyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_difficulty);  // ✅ 레이아웃 연결

        // 🔙 뒤로가기 버튼 리스너 추가
        ImageButton back = findViewById(R.id.btn_back);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        // 🎚️ Spinner 초기화
        Spinner spinner = findViewById(R.id.spinner_difficulty);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // ▶️ 확인 버튼
        Button confirm = findViewById(R.id.btn_confirm);
        confirm.setOnClickListener(v -> {
            String difficulty = spinner.getSelectedItem().toString();
            Intent intent = new Intent(this, GuessNumberActivity.class);
            intent.putExtra("difficulty", difficulty);
            startActivity(intent);
        });
    }
}