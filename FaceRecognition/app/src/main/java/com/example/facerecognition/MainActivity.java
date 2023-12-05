package com.example.facerecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Bitmap originalBitmap;
    private Bitmap emojiBitmap; // Bitmap for the emoji
    private Bitmap santaHatBitmap;
    private List<Face> detectedFaces; // List to store detected faces
    private static final int PICK_IMAGE = 1; // Request code for picking an image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        Button detectButton = findViewById(R.id.button_detect);
        Button placeEmojiButton = findViewById(R.id.button_place_emoji);
        Button placeSantaHatButton = findViewById(R.id.button_place_santa_hat);
        Button openGalleryButton = findViewById(R.id.button_open_gallery);

        // Load and scale down the image from resources
        originalBitmap = decodeBitmapFromResource(getResources(), R.drawable.ml, 800, 600);
        emojiBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.emoji); // Replace 'emoji' with your emoji resource name
        santaHatBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.santahat); // Replace 'santa_hat' with your resource name
        imageView.setImageBitmap(originalBitmap);

        detectButton.setOnClickListener(view -> detectFacesAndDrawRectangles());

        placeEmojiButton.setOnClickListener(view -> {
            if (detectedFaces != null) {
                placeEmojisOnFaces(originalBitmap, detectedFaces);
            }
        });

        placeSantaHatButton.setOnClickListener(view -> {
            if (detectedFaces != null) {
                placeSantaHatsOnFaces(originalBitmap, detectedFaces);
            }
        });

        openGalleryButton.setOnClickListener(view -> openGallery());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                imageView.setImageBitmap(originalBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void detectFacesAndDrawRectangles() {
        InputImage image = InputImage.fromBitmap(originalBitmap, 0);

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build();

        FaceDetector detector = FaceDetection.getClient(options);

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    // Store the detected faces in the global list
                    detectedFaces = faces;

                    // Draw rectangles or do any other processing here
                    Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    drawRectanglesOnFaces(mutableBitmap, detectedFaces);
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                });
    }

    private void drawRectanglesOnFaces(Bitmap bitmap, List<Face> faces) {
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.YELLOW); // Set the color to bright yellow
        paint.setStyle(Paint.Style.STROKE);

        // MARK: Differs for image size
        paint.setStrokeWidth(10); // Increase the line thickness

        for (Face face : faces) {
            Rect bounds = face.getBoundingBox();
            canvas.drawRect(bounds, paint);
        }

        runOnUiThread(() -> imageView.setImageBitmap(bitmap));
    }


    private Bitmap decodeBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void placeEmojisOnFaces(Bitmap originalBitmap, List<Face> faces) {
        Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        for (Face face : faces) {
            Rect bounds = face.getBoundingBox();
            int emojiWidth = (int)(bounds.width() * 0.75);
            int emojiHeight = (int)(bounds.height() * 0.75);
            Bitmap scaledEmoji = Bitmap.createScaledBitmap(emojiBitmap, emojiWidth, emojiHeight, false);
            canvas.drawBitmap(scaledEmoji, bounds.centerX() - emojiWidth / 2f, bounds.centerY() - emojiHeight / 2f, null);
        }

        runOnUiThread(() -> imageView.setImageBitmap(mutableBitmap));
    }

    private void placeSantaHatsOnFaces(Bitmap originalBitmap, List<Face> faces) {
        Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        for (Face face : faces) {
            Rect bounds = face.getBoundingBox();
            int hatWidth = bounds.width();
            int hatHeight = (int)(bounds.height() * 1); // Adjust size as needed
            Bitmap scaledHat = Bitmap.createScaledBitmap(santaHatBitmap, hatWidth, hatHeight, false);
            float x = bounds.centerX() - hatWidth / 2f;
            float y = bounds.top - hatHeight / 2f; // Adjust position as needed
            canvas.drawBitmap(scaledHat, x, y, null);
        }

        runOnUiThread(() -> imageView.setImageBitmap(mutableBitmap));
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }
}