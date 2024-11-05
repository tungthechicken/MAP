package com.example.map;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingActivity extends AppCompatActivity {
    private LinearLayout profileEdit;
    private ImageButton chitietProfile;

    private LinearLayout notiEdit;
    private ImageButton chitietNoti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        profileEdit = findViewById(R.id.Profile);
        chitietProfile = findViewById(R.id.chitiet1);
        notiEdit = findViewById(R.id.Noti);
        chitietNoti = findViewById(R.id.chitiet2);

        // Setup navigation bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_settings);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (item.getItemId() == R.id.nav_map) {
                startActivity(new Intent(getApplicationContext(), MapActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (item.getItemId() == R.id.nav_settings) {
                return true;
            }
            return false;
        });

        // Setup logout button and destroy everything
        ImageView logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(view -> {
            // Handle logout button click
            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Chuyển đến EditProfileActivity
        profileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProfile();
            }
        });
        chitietProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProfile();
            }
        });

        // Chuyển đến EditNotiActivity
        notiEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotiProfile();
            }
        });
        chitietNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotiProfile();
            }
        });
    }

    private void openEditProfile() {
        // Chuyển đến editProfileActivity
        Intent intent = new Intent(SettingActivity.this, editProfileActivity.class);
        startActivity(intent);
    }

    private void openNotiProfile() {
        // Chuyển đến editNotiActivity
        Intent intent = new Intent(SettingActivity.this, editNotiActivity.class);
        startActivity(intent);
    }
}
