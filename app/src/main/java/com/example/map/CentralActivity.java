package com.example.map;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CentralActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        fetchUserData(email, savedInstanceState);
    }

    private void fetchUserData(String email, Bundle savedInstanceState) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.retrofit_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
        Call<UserData> call = retrofitInterface.getUserByEmail(email);

        call.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(@NonNull Call<UserData> call, @NonNull Response<UserData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String name = response.body().getName();
                    String userCreatedDate = response.body().getUserCreatedDate();
                    setupBottomNavigationView(name, userCreatedDate, savedInstanceState);
                } else {
                    setupBottomNavigationView("User", "", savedInstanceState);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserData> call, @NonNull Throwable t) {
                setupBottomNavigationView("User", "", savedInstanceState);
            }
        });
    }

    private void setupBottomNavigationView(String name, String userCreatedDate, Bundle savedInstanceState) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new DashboardFragment();
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("userCreatedDate", userCreatedDate);
                selectedFragment.setArguments(bundle);
            } else if (itemId == R.id.nav_map) {
                selectedFragment = new MapFragment();
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                selectedFragment.setArguments(bundle);
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                selectedFragment.setArguments(bundle);
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}