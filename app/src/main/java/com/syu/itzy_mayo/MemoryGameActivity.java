package com.syu.itzy_mayo;

import com.syu.itzy_mayo.GameResultActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.ArrayList;

public class MemoryGameActivity extends BaseGameActivity {

    private GridLayout grid;
    private final int[] icons = {
            R.drawable.memory_bolt, R.drawable.memory_ladybug,
            R.drawable.memory_leaf, R.drawable.memory_rainbow,
            R.drawable.memory_sun, R.drawable.memory_heart
    };
    private ArrayList<Integer> cardImages;
    private ImageButton firstCard, secondCard;
    private boolean isFlipping = false;
    private int matchCount = 0;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_memory_game;
    }

    @Override
    protected int getGameContentLayoutRes() {
        return R.layout.memory_game_content;
    }

    @Override
    protected boolean useRuntimeTimer() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        grid = findViewById(R.id.memory_grid);
        setupGame();
    }
    @Override
    protected void generateNewQuestion() {
        // 메모리 게임은 문제를 생성하지 않음
    }

    private void setupGame() {
        cardImages = new ArrayList<>();
        for (int icon : icons) {
            cardImages.add(icon);
            cardImages.add(icon);
        }
        Collections.shuffle(cardImages);

        grid.removeAllViews();
        firstCard = secondCard = null;
        matchCount = 0;

        for (int i = 0; i < cardImages.size(); i++) {
            // ✅ 여기서부터 카드 하나 생성
            ImageButton card = new ImageButton(this);

            // 🔧 이미지 설정 및 크기 조절
            card.setScaleType(ImageView.ScaleType.FIT_CENTER);  // 비율 유지
            card.setAdjustViewBounds(true);                     // 크기 조정 허용
            card.setPadding(12, 12, 12, 12);                     // 여백

            // 카드 뒷면 이미지
            card.setBackgroundResource(R.drawable.card_back);

            // 레이아웃 비율 조정
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            card.setLayoutParams(params);

            // 카드 이미지 태그로 저장
            int imageRes = cardImages.get(i);
            card.setTag(imageRes);

            // 클릭 리스너 연결
            card.setOnClickListener(v -> handleCardFlip((ImageButton) v));

            // 뷰 추가
            grid.addView(card);
        }
    }

    private void handleCardFlip(ImageButton card) {
        if (isFlipping || card == firstCard || card.getDrawable() != null) return;

        card.setImageResource((int) card.getTag());

        if (firstCard == null) {
            firstCard = card;
        } else {
            secondCard = card;
            isFlipping = true;

            new Handler().postDelayed(() -> {
                if ((int) firstCard.getTag() == (int) secondCard.getTag()) {
                    matchCount++;
                    firstCard.setEnabled(false);
                    secondCard.setEnabled(false);
                } else {
                    firstCard.setImageDrawable(null);
                    secondCard.setImageDrawable(null);
                }
                firstCard = secondCard = null;
                isFlipping = false;

                // 모든 카드가 맞춰졌으면 결과 화면으로 이동
                if (matchCount == icons.length) {
                    showResult();
                }
            }, 1000);
        }
    }

    // 결과 화면으로 이동하는 메서드
    private void showResult() {
        int score = matchCount * 10;  // 점수 계산 방식
        long elapsedMillis = getElapsedTime();  // BaseGameActivity로부터 상속된 시간 함수
        int seconds = (int) (elapsedMillis / 1000);
        int minutes = seconds / 60;
        seconds %= 60;

        String timeFormatted = String.format("%02d:%02d", minutes, seconds);

        android.content.Intent intent = new android.content.Intent(this, GameResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("time", timeFormatted);
        startActivity(intent);
        finish();
    }
}