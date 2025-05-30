package com.syu.itzy_mayo;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import java.util.Random;
import android.view.View;

public class Game2048Activity extends BaseGameActivity {

    private GridLayout gridLayout;
    private final int GRID_SIZE = 4;
    private final TextView[][] cells = new TextView[GRID_SIZE][GRID_SIZE];
    private final Random random = new Random();
    @SuppressWarnings("FieldCanBeLocal")
    private float startX, startY;
    private int score = 0;
    private TextView gameOverText;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_game_2048;
    }

    @Override
    protected int getGameContentLayoutRes() {
        return R.layout.game_2048_content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout innerContainer = findViewById(R.id.game_inner_container);

        innerContainer.post(() -> {
            int size = Math.min(innerContainer.getWidth(), innerContainer.getHeight());
            int marginPerSide = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
            int totalMargin = marginPerSide * 2 * GRID_SIZE;
            int cellSize = (size - totalMargin) / GRID_SIZE;

            gridLayout = new GridLayout(this);
            gridLayout.setColumnCount(GRID_SIZE);
            gridLayout.setRowCount(GRID_SIZE);
            gridLayout.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));

            for (int y = 0; y < GRID_SIZE; y++) {
                for (int x = 0; x < GRID_SIZE; x++) {
                    TextView cell = createCell(cellSize);
                    gridLayout.addView(cell);
                    cells[x][y] = cell;
                }
            }

            innerContainer.addView(gridLayout);

            // ✅ Game Over 텍스트를 Java 코드로 최상단에 추가
            gameOverText = new TextView(this);
            gameOverText.setText(getString(R.string.game_over));
            gameOverText.setTextSize(32);
            gameOverText.setTextColor(Color.WHITE);
            gameOverText.setTypeface(Typeface.DEFAULT_BOLD);
            gameOverText.setGravity(Gravity.CENTER);
            gameOverText.setBackgroundColor(Color.parseColor("#80000000"));
            gameOverText.setVisibility(View.GONE);
            gameOverText.setElevation(10f);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;
            innerContainer.addView(gameOverText, params);

            addRandomTile();
            addRandomTile();
            updateScoreText();
            startRuntimeTimerIfNeeded(); // start the timer only after everything is ready
        });
    }

    private void showGameOver() {
        if (gameOverText != null) {
            gameOverText.setVisibility(View.VISIBLE);
            gameOverText.bringToFront();
        }
        pauseGame(); // ensure the timer stops
    }

    private TextView createCell(int size) {
        TextView cell = new TextView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(4, 4, 4, 4);
        cell.setLayoutParams(params);
        cell.setGravity(Gravity.CENTER);
        cell.setTypeface(Typeface.DEFAULT_BOLD);
        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        cell.setBackgroundColor(Color.LTGRAY);
        return cell;
    }

    private void increaseScore(int value) {
        score += value;
        updateScoreText();
    }

    private void updateScoreText() {
        if (scoreText != null) {
            scoreText.setText(getString(R.string.score_format, score));
        }
    }

    private boolean canMove() {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                String current = cells[x][y].getText().toString();
                if (current.isEmpty()) return true;
                int value = Integer.parseInt(current);
                if (x + 1 < GRID_SIZE && value == getValueAt(x + 1, y)) return true;
                if (x - 1 >= 0 && value == getValueAt(x - 1, y)) return true;
                if (y + 1 < GRID_SIZE && value == getValueAt(x, y + 1)) return true;
                if (y - 1 >= 0 && value == getValueAt(x, y - 1)) return true;
            }
        }
        return false;
    }

    private boolean cannotMove() {
        return !canMove();
    }

    private int getValueAt(int x, int y) {
        String text = cells[x][y].getText().toString();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    private boolean isBoardFull() {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                if (cells[x][y].getText().toString().isEmpty()) return false;
            }
        }
        return true;
    }

    private void addRandomTile() {
        if (isBoardFull() && cannotMove()) {
            showGameOver();
            return;
        }

        int x, y;
        do {
            x = random.nextInt(GRID_SIZE);
            y = random.nextInt(GRID_SIZE);
        } while (!cells[x][y].getText().toString().isEmpty());

        cells[x][y].setText(random.nextInt(10) < 9 ? "2" : "4");
        updateCellColor(cells[x][y]);
    }

    private void updateCellColor(TextView cell) {
        switch (cell.getText().toString()) {
            case "2": cell.setBackgroundColor(Color.parseColor("#EEE4DA")); break;
            case "4": cell.setBackgroundColor(Color.parseColor("#EDE0C8")); break;
            case "8": cell.setBackgroundColor(Color.parseColor("#F2B179")); break;
            case "16": cell.setBackgroundColor(Color.parseColor("#F59563")); break;
            case "32": cell.setBackgroundColor(Color.parseColor("#F67C5F")); break;
            case "64": cell.setBackgroundColor(Color.parseColor("#F65E3B")); break;
            case "128": cell.setBackgroundColor(Color.parseColor("#EDCF72")); break;
            case "256": cell.setBackgroundColor(Color.parseColor("#EDCC61")); break;
            case "512": cell.setBackgroundColor(Color.parseColor("#EDC850")); break;
            case "1024": cell.setBackgroundColor(Color.parseColor("#EDC53F")); break;
            case "2048": cell.setBackgroundColor(Color.parseColor("#EDC22E")); break;
            default: cell.setBackgroundColor(Color.LTGRAY); break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGamePaused()) return true;

        final float SWIPE_THRESHOLD = 100f; // 스와이프 인식 최소 거리

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                return true;

            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();

                float deltaX = endX - startX;
                float deltaY = endY - startY;

                boolean moved = false;

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    // 좌우 스와이프
                    if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                        if (deltaX > 0) moved = swipeRight();
                        else moved = swipeLeft();
                    }
                } else {
                    // 상하 스와이프
                    if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                        if (deltaY > 0) moved = swipeDown();
                        else moved = swipeUp();
                    }
                }

                if (moved) addRandomTile();

                if (isBoardFull() && cannotMove()) {
                    new android.os.Handler().postDelayed(this::showGameOver, 100);
                }

                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isGamePaused()) return true;

        boolean moved;

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT: moved = swipeLeft(); break;
            case KeyEvent.KEYCODE_DPAD_RIGHT: moved = swipeRight(); break;
            case KeyEvent.KEYCODE_DPAD_UP: moved = swipeUp(); break;
            case KeyEvent.KEYCODE_DPAD_DOWN: moved = swipeDown(); break;
            default: return super.onKeyDown(keyCode, event);
        }

        if (moved) addRandomTile();

        if (isBoardFull() && cannotMove()) {
            new android.os.Handler().postDelayed(this::showGameOver, 100);
        }

        return true;
    }
    private boolean swipeLeft() {
        boolean moved = false;
        for (int y = 0; y < GRID_SIZE; y++) {
            int[] newRow = new int[GRID_SIZE];
            boolean[] merged = new boolean[GRID_SIZE];
            int index = 0;

            for (int x = 0; x < GRID_SIZE; x++) {
                String cellText = cells[x][y].getText().toString();
                if (!cellText.isEmpty()) {
                    int value = Integer.parseInt(cellText);
                    if (index > 0 && newRow[index - 1] == value && !merged[index - 1]) {
                        newRow[index - 1] *= 2;
                        merged[index - 1] = true;
                        moved = true;
                        increaseScore(newRow[index - 1]);
                    } else {
                        if (newRow[index] != 0) moved = true;
                        newRow[index++] = value;
                    }
                }
            }

            for (int x = 0; x < GRID_SIZE; x++) {
                String newValue = newRow[x] == 0 ? "" : String.valueOf(newRow[x]);
                if (!cells[x][y].getText().toString().equals(newValue)) moved = true;
                cells[x][y].setText(newValue);
                updateCellColor(cells[x][y]);
            }
        }
        return moved;
    }

    private boolean swipeRight() {
        boolean moved = false;
        for (int y = 0; y < GRID_SIZE; y++) {
            int[] newRow = new int[GRID_SIZE];
            boolean[] merged = new boolean[GRID_SIZE];
            int index = GRID_SIZE - 1;

            for (int x = GRID_SIZE - 1; x >= 0; x--) {
                String cellText = cells[x][y].getText().toString();
                if (!cellText.isEmpty()) {
                    int value = Integer.parseInt(cellText);
                    if (index < GRID_SIZE - 1 && newRow[index + 1] == value && !merged[index + 1]) {
                        newRow[index + 1] *= 2;
                        merged[index + 1] = true;
                        moved = true;
                        increaseScore(newRow[index + 1]);
                    } else {
                        if (newRow[index] != 0) moved = true;
                        newRow[index--] = value;
                    }
                }
            }

            for (int x = 0; x < GRID_SIZE; x++) {
                String newValue = newRow[x] == 0 ? "" : String.valueOf(newRow[x]);
                if (!cells[x][y].getText().toString().equals(newValue)) moved = true;
                cells[x][y].setText(newValue);
                updateCellColor(cells[x][y]);
            }
        }
        return moved;
    }

    private boolean swipeUp() {
        boolean moved = false;
        for (int x = 0; x < GRID_SIZE; x++) {
            int[] newCol = new int[GRID_SIZE];
            boolean[] merged = new boolean[GRID_SIZE];
            int index = 0;

            for (int y = 0; y < GRID_SIZE; y++) {
                String cellText = cells[x][y].getText().toString();
                if (!cellText.isEmpty()) {
                    int value = Integer.parseInt(cellText);
                    if (index > 0 && newCol[index - 1] == value && !merged[index - 1]) {
                        newCol[index - 1] *= 2;
                        merged[index - 1] = true;
                        moved = true;
                        increaseScore(newCol[index - 1]);
                    } else {
                        if (newCol[index] != 0) moved = true;
                        newCol[index++] = value;
                    }
                }
            }

            for (int y = 0; y < GRID_SIZE; y++) {
                String newValue = newCol[y] == 0 ? "" : String.valueOf(newCol[y]);
                if (!cells[x][y].getText().toString().equals(newValue)) moved = true;
                cells[x][y].setText(newValue);
                updateCellColor(cells[x][y]);
            }
        }
        return moved;
    }

    private boolean swipeDown() {
        boolean moved = false;
        for (int x = 0; x < GRID_SIZE; x++) {
            int[] newCol = new int[GRID_SIZE];
            boolean[] merged = new boolean[GRID_SIZE];
            int index = GRID_SIZE - 1;

            for (int y = GRID_SIZE - 1; y >= 0; y--) {
                String cellText = cells[x][y].getText().toString();
                if (!cellText.isEmpty()) {
                    int value = Integer.parseInt(cellText);
                    if (index < GRID_SIZE - 1 && newCol[index + 1] == value && !merged[index + 1]) {
                        newCol[index + 1] *= 2;
                        merged[index + 1] = true;
                        moved = true;
                        increaseScore(newCol[index + 1]);
                    } else {
                        if (newCol[index] != 0) moved = true;
                        newCol[index--] = value;
                    }
                }
            }

            for (int y = 0; y < GRID_SIZE; y++) {
                String newValue = newCol[y] == 0 ? "" : String.valueOf(newCol[y]);
                if (!cells[x][y].getText().toString().equals(newValue)) moved = true;
                cells[x][y].setText(newValue);
                updateCellColor(cells[x][y]);
            }
        }
        return moved;
    }
}