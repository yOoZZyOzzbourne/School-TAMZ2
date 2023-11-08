package com.tamz.soko2023;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LevelsActivity extends AppCompatActivity {

    private ListView levelsListView;
    private ArrayAdapter<String> adapter;
    private List<String> levelNames;
    private List<String> levelData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        levelsListView = findViewById(R.id.levelsListView);
        levelNames = new ArrayList<>();
        levelData = new ArrayList<>();

        try {
            readLevelsFromAssets();
        } catch (IOException e) {
            e.printStackTrace();
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, levelNames);
        levelsListView.setAdapter(adapter);

        levelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedLevelData = levelData.get(position);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("LEVEL_DATA", selectedLevelData);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void readLevelsFromAssets() throws IOException {
        AssetManager assetManager = getAssets();
        InputStream inputStream = assetManager.open("levels.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        StringBuilder levelBuilder = new StringBuilder();
        boolean readingLevel = false;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("Level")) {
                if (readingLevel) {
                    levelData.add(levelBuilder.toString());
                    levelBuilder.setLength(0);
                }
                readingLevel = true;
                levelNames.add(line); // Add the level name to the list
            } else if (readingLevel) {
                levelBuilder.append(line).append("\n");
            }
        }
        if (readingLevel) {
            levelData.add(levelBuilder.toString());
        }

        inputStream.close();
        reader.close();
    }
}
