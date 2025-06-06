package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button uploadBtn, imgBtn, wsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadBtn = findViewById(R.id.btnUploadImage);
        imgBtn = findViewById(R.id.btnImageRequest);
        wsBtn = findViewById(R.id.btnWebSocketImage);

        /* button click listeners */
        uploadBtn.setOnClickListener(this);
        imgBtn.setOnClickListener(this);
        wsBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnUploadImage) {
            startActivity(new Intent(MainActivity.this, ImageUploadActivity.class));
        }
        else if (id == R.id.btnImageRequest) {
            startActivity(new Intent(MainActivity.this, ImageReqActivity.class));
        }
        else if (id == R.id.btnWebSocketImage) {
            startActivity(new Intent(MainActivity.this, WebSocketImageActivity.class));
        }
    }
}