package com.tamz.soko2023;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by kru13
 */
public class SokoView extends View {

    Bitmap[] bmp;
    private int moveCount;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private long startTime;
    private long elapsedTime;
    private boolean isTimerRunning;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    int lW = 10;
    int lH = 10;

    int width;
    int height;

    private String actualLevel;
    private int[] originalLevel;

    private int level[];

    public SokoView(Context context, String level) {
        super(context);
        init(context, level);
        actualLevel = level;

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTimerRunning) {
                    long currentTime = System.currentTimeMillis();
                    elapsedTime = currentTime - startTime;
                    invalidate();
                }
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    void init(Context context, String text) {
        bmp = new Bitmap[6];

        bmp[0] = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
        bmp[1] = BitmapFactory.decodeResource(getResources(), R.drawable.wall);
        bmp[2] = BitmapFactory.decodeResource(getResources(), R.drawable.box);
        bmp[3] = BitmapFactory.decodeResource(getResources(), R.drawable.goal);
        bmp[4] = BitmapFactory.decodeResource(getResources(), R.drawable.hero);
        bmp[5] = BitmapFactory.decodeResource(getResources(), R.drawable.boxok);

        loadLevel(text);
        setFocusable(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Calculate the height of each tile based on half the view's height
        height = (h / 2) / lH;

        width = height;

        if (lW * width > w) {
            width = w / lW;
            height = width;
        }

        // Scale the bitmaps to fit the new tile size
        for (int i = 0; i < bmp.length; i++) {
            if (bmp[i] != null) {
                bmp[i] = Bitmap.createScaledBitmap(bmp[i], width, height, true);
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int offsetX = (getWidth() - (lW * width)) / 2;
        int offsetY = (getHeight() - (lH * height)) / 2;

        for (int y = 0; y < lH; y++) {
            for (int x = 0; x < lW; x++) {
                int index = y * lW + x;
                if (index >= 0 && index < level.length) {
                    int tile = level[index];
                    if (tile >= 0 && tile < bmp.length && bmp[tile] != null) {
                        // Draw the bitmap
                        canvas.drawBitmap(bmp[tile], offsetX + x * width, offsetY + y * height, null);
                    }
                } else {
                    Log.e("SokoView", "Invalid index at x=" + x + ", y=" + y);
                }
            }
        }

        paint.setColor(Color.BLACK);
        paint.setTextSize(100);
        canvas.drawText("Moves: " + moveCount, 10, 80, paint);

       if (isTimerRunning) {
            long currentTime = System.currentTimeMillis();
            elapsedTime = currentTime - startTime;
            String timeString = formatTime(elapsedTime);
            paint.setColor(Color.BLACK);
            paint.setTextSize(100);
            canvas.drawText("Time: " + timeString, 10, 180, paint);
        }
    }

    private float x1, y1, x2, y2;
    private static final int MIN_SWIPE_DISTANCE = 150;

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                float deltaX = x2 - x1;
                float deltaY = y2 - y1;

                if (Math.abs(deltaX) > MIN_SWIPE_DISTANCE) {
                    // Left or Right swipe
                    if (x2 > x1) {
                        // Right
                        moveHero("RIGHT");
                    } else {
                        // Left
                        moveHero("LEFT");
                    }
                } else if (Math.abs(deltaY) > MIN_SWIPE_DISTANCE) {
                    // Up or Down swipe
                    if (y2 > y1) {
                        // Down
                        moveHero("DOWN");
                    } else {
                        // Up
                        moveHero("UP");
                    }
                }
                break;
        }
        return true;
    }

    public boolean handleKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                moveHero("UP");
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                moveHero("DOWN");
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                moveHero("LEFT");
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                moveHero("RIGHT");
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void moveHero(String direction) {
        int heroIndex = findHero();
        int heroX = heroIndex % lW;
        int heroY = heroIndex / lW;

        int moveX = 0;
        int moveY = 0;

        switch (direction) {
            case "UP":
                moveY = -1;
                break;
            case "DOWN":
                moveY = 1;
                break;
            case "LEFT":
                moveX = -1;
                break;
            case "RIGHT":
                moveX = 1;
                break;
        }

        int targetX = heroX + moveX;
        int targetY = heroY + moveY;
        int targetIndex = targetY * lW + targetX;
        moveCount++;

        switch (level[targetIndex]) {
            case 0: // Empty
            case 3:
                // Move the hero
                level[heroIndex] = originalLevel[heroIndex] == 3 ? 3 : 0; // Restore goal if there was originally one
                level[targetIndex] = 4;
                break;
            case 2: // Box
            case 5: // Box on goal
                int boxTargetX = targetX + moveX;
                int boxTargetY = targetY + moveY;
                if (boxTargetX >= 0 && boxTargetX < lW && boxTargetY >= 0 && boxTargetY < lH) { // Check bounds
                    int boxTargetIndex = boxTargetY * lW + boxTargetX;
                    if (level[boxTargetIndex] == 0 || level[boxTargetIndex] == 3) {
                        // Move the box
                        level[targetIndex] = originalLevel[targetIndex] == 3 ? 3 : 0;
                        level[boxTargetIndex] = (level[boxTargetIndex] == 3) ? 5 : 2;

                        // Move the hero
                        level[heroIndex] = originalLevel[heroIndex] == 3 ? 3 : 0;
                        level[targetIndex] = 4;
                    }
                }
                break;
        }

        if (checkWin()) {
            stopTimer();
            Toast.makeText(getContext(), "Congratulations! You've won!", Toast.LENGTH_SHORT).show();
        }

        invalidate();
    }

    private int findHero() {
        for (int i = 0; i < level.length; i++) {
            if (level[i] == 4) {
                return i;
            }
        }
        return -1; // Hero not found
    }

    private boolean checkWin() {
        for (int i = 0; i < level.length; i++) {
            if (level[i] == 2) { // If there's a box not on a goal
                return false;
            }
        }

        return true;
    }
    public void loadLevel(String text) {
        actualLevel = text;
        originalLevel = parseOriginalLevel(text);
        level = parseLevel(text);
        moveCount = 0; // Reset the move counter

        startTime = System.currentTimeMillis();
        isTimerRunning = true;
        timerHandler.postDelayed(timerRunnable, 1000); // Start the timer updates

        invalidate();
    }



    public int[] parseOriginalLevel(String levelString) {
        String[] rows = levelString.trim().split("\n");
        lH = rows.length;
        lW = 0;
        for (String row : rows) {
            if (row.length() > lW) {
                lW = row.length();
            }
        }

        int[] originalLevel = new int[lW * lH];
        Arrays.fill(originalLevel, 0);

        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                char item = rows[y].charAt(x);
                int index = y * lW + x;
                switch (item) {
                    case ' ': // An empty space
                        originalLevel[index] = 0;
                        break;
                    case '#': // Wall
                        originalLevel[index] = 1;
                        break;
                    case '.':
                    case '*': // Goal or Box on Goal
                        originalLevel[index] = 3;
                        break;
                }
            }
        }

        return originalLevel;
    }

    public int[] parseLevel(String levelString) {
        String[] rows = levelString.trim().split("\n");
        lH = rows.length;
        lW = 0;
        for (String row : rows) {
            if (row.length() > lW) {
                lW = row.length();
            }
        }

        int[] level = new int[lW * lH];
        Arrays.fill(level, 0); // Fill with empty tiles

        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                char item = rows[y].charAt(x);
                int index = y * lW + x;
                switch (item) {
                    case ' ': // An empty space
                        level[index] = 0;
                        break;
                    case '#': // Wall
                        level[index] = 1;
                        break;
                    case '$': // Box
                        level[index] = 2;
                        break;
                    case '.': // Goal
                        level[index] = 3;
                        break;
                    case '*': // Box on goal
                        level[index] = 5;
                        break;
                    case '@': // Player
                        level[index] = 4;
                        break;
                    case '+': // Player on goal
                        level[index] = 6;
                        break;
                }
            }
        }

        return level;
    }

    private void stopTimer() {
        isTimerRunning = false;
        timerHandler.removeCallbacks(timerRunnable); // Stop the timer updates
    }

    public void reset() {
        loadLevel(actualLevel);
        moveCount = 0; // Reset the move counter
        stopTimer();
    }
}
