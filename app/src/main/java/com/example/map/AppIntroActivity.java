package com.example.map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.AppIntroPageTransformerType;

public class AppIntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.createInstance("Welcome to Pothole Master!",
                "A companion that helps you avoid potholes in your area.",
                R.drawable.splash_image,
                com.google.android.material.R.color.material_dynamic_tertiary70
        ));

        addSlide(AppIntroFragment.createInstance("Interactive Map",
                "View the potholes in your area and report new ones.",
                R.drawable.intro_img_2,
                com.google.android.material.R.color.material_dynamic_primary70
        ));

        addSlide(AppIntroFragment.createInstance("Road Navigation",
                "Warns you about potholes on your route.",
                R.drawable.intro_img_3,
                com.google.android.material.R.color.material_deep_teal_500
        ));

        addSlide(AppIntroFragment.createInstance("Statistics and Reports",
                "View your pothole reports and statistics.",
                R.drawable.intro_img_4,
                R.color.blue
        ));

        addSlide(AppIntroFragment.createInstance("Let's get started!",
                "Make your journey smoother with Pothole Master!",
                R.drawable.intro_img_5,
                R.color.purple_200
        ));

        // Set the transformer type
        setTransformer(new AppIntroPageTransformerType.Parallax());

        // Show/hide status bar
        showStatusBar(true);

        //Prevent the back button from exiting the slides
        setSystemBackButtonLocked(true);

        //Enable immersive mode (no status and nav bar)
        setImmersiveMode();

        //Enable/disable page indicators
        setIndicatorEnabled(true);

        //Show/hide ALL buttons
        setButtonsEnabled(true);
    }

    @Override
    protected void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        preferences.edit().putBoolean("isFirstRun", false).apply();
        navigateToCentralLoginActivity();
    }

    @Override
    protected void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        preferences.edit().putBoolean("isFirstRun", false).apply();
        navigateToCentralLoginActivity();
    }

    private void navigateToCentralLoginActivity() {
        startActivity(new Intent(AppIntroActivity.this, CentralLoginActivity.class));
        finish();
    }
}