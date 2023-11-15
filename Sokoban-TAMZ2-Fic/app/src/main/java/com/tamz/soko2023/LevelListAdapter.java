package com.tamz.soko2023;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class LevelListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> levelNames;
    private final List<String> levelData;

    int lW = 10;
    int lH = 10;

    public LevelListAdapter(Context context, List<String> levelNames, List<String> levelData) {
        super(context, R.layout.list_item_level, levelNames);
        this.context = context;
        this.levelNames = levelNames;
        this.levelData = levelData;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View listItem = inflater.inflate(R.layout.list_item_level, parent, false);

        TextView levelNameView = listItem.findViewById(R.id.levelName);
        ImageView levelPreview = listItem.findViewById(R.id.levelPreview);

        levelNameView.setText(levelNames.get(position));

        // Generate preview bitmap
        Bitmap previewBitmap = generateLevelPreview(levelData.get(position));
        levelPreview.setImageBitmap(previewBitmap);

        return listItem;
    }

    private Bitmap generateLevelPreview(String levelData) {
        int previewSize = 100; // 100x100 pix
        int tileSize = 8; // 8x8 pix

        int[] levelGrid = parseLevel(levelData);

        Bitmap[] scaledBitmaps = loadAndScaleBitmaps(getContext().getResources(), tileSize);

        Bitmap previewBitmap = Bitmap.createBitmap(previewSize, previewSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(previewBitmap);

        for (int y = 0; y < lH; y++) {
            for (int x = 0; x < lW; x++) {
                int index = y * lW + x;
                if (index < levelGrid.length) {
                    int tileType = levelGrid[index];
                    if (tileType >= 0 && tileType < scaledBitmaps.length) {
                        Bitmap tileBitmap = scaledBitmaps[tileType];
                        if (tileBitmap != null) {
                            canvas.drawBitmap(tileBitmap, x * tileSize, y * tileSize, null);
                        }
                    }
                }
            }
        }

        return previewBitmap;
    }

    private Bitmap[] loadAndScaleBitmaps(Resources resources, int tileSize) {
        Bitmap[] scaledBitmaps = new Bitmap[6];

        Bitmap[] originalBitmaps = new Bitmap[] {
                BitmapFactory.decodeResource(resources, R.drawable.empty),
                BitmapFactory.decodeResource(resources, R.drawable.wall),
                BitmapFactory.decodeResource(resources, R.drawable.box),
                BitmapFactory.decodeResource(resources, R.drawable.goal),
                BitmapFactory.decodeResource(resources, R.drawable.hero),
                BitmapFactory.decodeResource(resources, R.drawable.boxok)
        };

        // Scale each bitmap
        for (int i = 0; i < originalBitmaps.length; i++) {
            if (originalBitmaps[i] != null) {
                scaledBitmaps[i] = Bitmap.createScaledBitmap(originalBitmaps[i], tileSize, tileSize, true);
            }
        }

        return scaledBitmaps;
    }


    private int[] parseLevel(String levelString) {
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
}
