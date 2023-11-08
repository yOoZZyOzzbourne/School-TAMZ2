package com.tamz.soko2023;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private SokoView sokoView;
    private static final int REQUEST_CODE_SELECT_LEVEL = 1;

    private static String initialLevel = "Level 1\n" +
            "'PICOKOSMOS 01'\n" +
            "######\n" +
            " #   ####\n" +
            " #      #\n" +
            "### **# #\n" +
            "#  #* *@#\n" +
            "#   * ###\n" +
            "#  ##  #\n" +
            "##     #\n" +
            " #.$#  #\n" +
            " #  ####\n" +
            " ####";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sokoView = findViewById(R.id.sokoView);
        sokoView = new SokoView(this, initialLevel);
        setContentView(sokoView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu); // make sure the file name is correct
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.reset) {
            resetGame(); // Existing code...
            return true;
        } else if (id == R.id.list) {
            // Start LevelsActivity and wait for the result
            Intent levelsIntent = new Intent(this, LevelsActivity.class);
            startActivityForResult(levelsIntent, REQUEST_CODE_SELECT_LEVEL);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_LEVEL && resultCode == RESULT_OK && data != null) {
            String levelData = data.getStringExtra("LEVEL_DATA");
            if (levelData != null) {
                loadLevel(levelData);
            }
        }
    }

    // Add a new method to load the level into SokoView
    private void loadLevel(String levelData) {
        if (sokoView != null) {
            sokoView.loadLevel(levelData);
            sokoView.invalidate(); // Redraw the view with the new level
        }
    }
    private void resetGame() {
        sokoView.reset();
        Toast.makeText(this, "Game Reset!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (
                keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {

            sokoView.handleKeyDown(keyCode, event);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
