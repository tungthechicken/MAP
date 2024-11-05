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

    private LinearLayout updateEdit;
    private ImageButton chitietUpdate;

    private LinearLayout displayEdit;
    private ImageButton chitietDisplay;

    private LinearLayout privacyEdit;
    private ImageButton chitietPrivacy;

    private LinearLayout qaaEdit;
    private ImageButton chitietQaa;

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

        displayEdit = findViewById(R.id.display);
        chitietDisplay = findViewById(R.id.chitiet3);

        updateEdit = findViewById(R.id.update);
        chitietUpdate = findViewById(R.id.chitiet4);

        privacyEdit = findViewById(R.id.privacy);
        chitietPrivacy = findViewById(R.id.chitiet5);

        qaaEdit = findViewById(R.id.QaA);
        chitietQaa = findViewById(R.id.chitiet6);

        // Setup logout button and destroy everything
        LinearLayout logOut = findViewById(R.id.logOut);
        logOut.setOnClickListener(view -> {
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
                openNoti();
            }
        });
        chitietNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNoti();
            }
        });

        // Chuyển đến DisplayActivity
        displayEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDisplay();
            }
        });
        chitietDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDisplay();
            }
        });

        // Chuyển đến UpdateActivity
        updateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUpdate();
            }
        });
        chitietUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUpdate();
            }
        });

        // Chuyển đến PrivacyActivity
        privacyEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPrivacy();
            }
        });
        chitietPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPrivacy();
            }
        });

// Chuyển đến QAAActivity
        qaaEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openQAA();
            }
        });
        chitietQaa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openQAA();
            }
        });

    }

    private void openEditProfile() {
        // Chuyển đến editProfileActivity
        Intent intent = new Intent(SettingActivity.this, editProfileActivity.class);
        startActivity(intent);
    }

    private void openNoti() {
        // Chuyển đến editNotiActivity
        Intent intent = new Intent(SettingActivity.this, editNotiActivity.class);
        startActivity(intent);
    }

    private void openUpdate() {
        // Chuyển đến UpdateActivity
        Intent intent = new Intent(SettingActivity.this, UpdateActivity.class);
        startActivity(intent);
    }

    private void openDisplay() {
        // Chuyển đến DisplayActivity
        Intent intent = new Intent(SettingActivity.this, DisplayActivity.class);
        startActivity(intent);
    }

    private void openPrivacy() {
        // Chuyển đến PrivacyActivity
        Intent intent = new Intent(SettingActivity.this, PrivacyActivity.class);
        startActivity(intent);
    }

    private void openQAA() {
        // Chuyển đến QAAActivity
        Intent intent = new Intent(SettingActivity.this, QuestionAndAnswerActivity.class);
        startActivity(intent);
    }
}
