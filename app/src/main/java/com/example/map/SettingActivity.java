package com.example.map;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class SettingActivity extends AppCompatActivity {
    private LinearLayout profileLayout;
    private ImageButton detailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        profileLayout = findViewById(R.id.Profile);
        detailButton = findViewById(R.id.chitiet1);

        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProfile();
            }
        });
        detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProfile();
            }
        });
    }
    private void openEditProfile() {
        // Chuyển đến EditProfileActivity
        Intent intent = new Intent(SettingActivity.this, editProfileActivity.class);
        startActivity(intent);
    }
}
