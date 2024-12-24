package com.example.map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    Animation topAnim, bottomAnim;
    ImageView image;
    TextView appName, appSlogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        // Animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        image = findViewById(R.id.imageView);
        appName = findViewById(R.id.textView);
        appSlogan = findViewById(R.id.textView2);

        // Set animations
        image.setAnimation(topAnim);
        appName.setAnimation(bottomAnim);
        appSlogan.setAnimation(bottomAnim);

        // Delay for 2 seconds and then start the main activity
        new Handler().postDelayed(() -> {
            SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
            boolean isFirstRun = preferences.getBoolean("isFirstRun", true);

            // Check if the app is running for the first time
            if (isFirstRun) {
                // Show the app intro
                startActivity(new Intent(SplashScreenActivity.this, AppIntroActivity.class));
            } else {
                // Start the main activity
                startActivity(new Intent(SplashScreenActivity.this, CentralLoginActivity.class));
            }
            finish();
        }, 2000);
    }
}