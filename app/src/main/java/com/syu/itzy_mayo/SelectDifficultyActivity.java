package com.syu.itzy_mayo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
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
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.difficulty_levels)) {
            @Override
            public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK);  // 기본 항목 색
                return view;
            }

            @Override
            public @NonNull View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                CharSequence rawItem = getItem(position);
                String item = rawItem != null ? rawItem.toString() : "";

                textView.setTextColor(Color.parseColor("#333333"));
                return view;
            }
        };
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