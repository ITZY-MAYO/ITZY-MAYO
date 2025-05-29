package com.syu.itzy_mayo;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class GameResultView extends FrameLayout {

    private TextView titleText;
    private TextView scoreText;
    private TextView timeText;

    public GameResultView(Context context) {
        super(context);
        init(context);
    }

    public GameResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.game_result_panel, this, true);
        titleText = findViewById(R.id.text_result_title);
        scoreText = findViewById(R.id.text_result_score);
        timeText = findViewById(R.id.text_result_time);
        setVisibility(View.GONE);
    }

    public void showSuccess(String score, String time) {
        titleText.setText("🎉 성공!");
        titleText.setTextColor(Color.parseColor("#4CAF50")); // Green
        scoreText.setText("점수: " + score);
        timeText.setText("시간: " + time);
        setVisibility(View.VISIBLE);
    }

    public void showFailure(String score, String time) {
        titleText.setText("💀 실패!");
        titleText.setTextColor(Color.parseColor("#F44336")); // Red
        scoreText.setText("점수: " + score);
        timeText.setText("시간: " + time);
        setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.GONE);
    }
}