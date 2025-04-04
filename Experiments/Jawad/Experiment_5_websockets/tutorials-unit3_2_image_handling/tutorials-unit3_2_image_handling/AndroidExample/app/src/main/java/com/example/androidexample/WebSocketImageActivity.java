package com.example.androidexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WebSocketImageActivity extends AppCompatActivity {
    private static final String TAG = "WebSocketImageActivity";

    // Update these with laptop's IP
    private static final String UPLOAD_URL = "http://192.168.1.150:8080/images";
    private static final String IMAGE_URL = "http://192.168.1.150:8080/images/";
    private static final String LATEST_IMAGE_URL = "http://192.168.1.150:8080/latest-image";

    private Button connectButton;
    private Button selectButton;
    private Button uploadButton;
    private TextView statusText;
    private ImageView previewImage;
    private ImageView receivedImage;

    private Uri selectedImageUri;

    // Polling mechanism
    private Handler pollingHandler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;
    private int lastImageId = 0;
    private boolean isPolling = false;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_image);

        // Initialize views
        connectButton = findViewById(R.id.connectButton);
        selectButton = findViewById(R.id.selectButton);
        uploadButton = findViewById(R.id.uploadButton);
        statusText = findViewById(R.id.statusText);
        previewImage = findViewById(R.id.previewImage);
        receivedImage = findViewById(R.id.receivedImage);

        // Set initial button states
        selectButton.setEnabled(false);
        uploadButton.setEnabled(false);

        // Register image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            previewImage.setImageBitmap(bitmap);
                            uploadButton.setEnabled(true);
                        } catch (IOException e) {
                            Log.e(TAG, "Error loading image", e);
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Set click listeners
        connectButton.setOnClickListener(v -> togglePolling());
        selectButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        uploadButton.setOnClickListener(v -> uploadImage());
    }

    private void togglePolling() {
        if (isPolling) {
            stopPolling();
        } else {
            startPolling();
        }
    }

    private void startPolling() {
        isPolling = true;
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                checkForNewImages();
                pollingHandler.postDelayed(this, 3000); // Poll every 3 seconds
            }
        };
        pollingHandler.post(pollingRunnable);

        statusText.setText("Connected (Polling)");
        connectButton.setText("Disconnect");
        selectButton.setEnabled(true);
    }

    private void stopPolling() {
        isPolling = false;
        if (pollingHandler != null && pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
            pollingRunnable = null;
        }

        statusText.setText("Disconnected");
        connectButton.setText("Connect");
        selectButton.setEnabled(false);
        uploadButton.setEnabled(false);
    }

    private void checkForNewImages() {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                LATEST_IMAGE_URL,
                response -> {
                    try {
                        int latestId = Integer.parseInt(response.trim());
                        if (latestId > lastImageId) {
                            loadImage(IMAGE_URL + latestId);
                            lastImageId = latestId;
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing response: " + response, e);
                    }
                },
                error -> {
                    Log.e(TAG, "Error checking for new images", error);
                    Toast.makeText(WebSocketImageActivity.this,
                            "Error: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void uploadImage() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Convert image to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] imageData = byteArrayOutputStream.toByteArray();

            // Create multipart request using your existing class
            MultipartRequest multipartRequest = new MultipartRequest(
                    Request.Method.POST,
                    UPLOAD_URL,
                    imageData,
                    response -> {
                        Log.d(TAG, "Upload response: " + response);
                        Toast.makeText(WebSocketImageActivity.this,
                                "Image uploaded successfully",
                                Toast.LENGTH_SHORT).show();

                        // Reset UI after upload
                        previewImage.setImageResource(android.R.color.transparent);
                        selectedImageUri = null;
                        uploadButton.setEnabled(false);
                    },
                    error -> {
                        Log.e(TAG, "Upload error", error);
                        Toast.makeText(WebSocketImageActivity.this,
                                "Upload failed: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
            );

            // Use your existing VolleySingleton
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(multipartRequest);

        } catch (IOException e) {
            Log.e(TAG, "Error preparing image", e);
            Toast.makeText(this, "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImage(String imageUrl) {
        ImageRequest imageRequest = new ImageRequest(
                imageUrl,
                response -> {
                    receivedImage.setImageBitmap(response);
                    Toast.makeText(WebSocketImageActivity.this,
                            "New image received",
                            Toast.LENGTH_SHORT).show();
                },
                0, 0, ImageView.ScaleType.FIT_CENTER, null,
                error -> {
                    Log.e(TAG, "Error loading image", error);
                    Toast.makeText(WebSocketImageActivity.this,
                            "Error loading image",
                            Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(imageRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
    }
}