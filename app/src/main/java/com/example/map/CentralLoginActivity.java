package com.example.map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CentralLoginActivity extends AppCompatActivity {

    private RetrofitInterface retrofitInterface;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central_login);

        String BASE_URL = getString(R.string.retrofit_url);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new LoginFragment(retrofitInterface);
                    case 1:
                        return new SignupFragment(retrofitInterface);
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "Login";
                    case 1:
                        return "Signup";
                    default:
                        return null;
                }
            }
        });

        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton fabGoogle = findViewById(R.id.fabGoogle);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        fabGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                String email = account.getEmail();
                String name = account.getDisplayName();

                Toast.makeText(this, "Welcome " + name, Toast.LENGTH_SHORT).show();

                sendUserDataToServer(email, name);
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign-In failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendUserDataToServer(String email, String name) {
        HashMap<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("name", name);
        map.put("isLinkedGoogle", "true");

        Call<UserData> call = retrofitInterface.sendUserData(map);
        call.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                if (response.isSuccessful()) {
                    UserData user = response.body();
                    if (user != null && user.getPassword() == null) {
                        navigateToSetPasswordActivity(email);
                    } else {
                        navigateToCentralActivity();
                    }
                } else {
                    Toast.makeText(CentralLoginActivity.this, "Failed to send data to server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                Toast.makeText(CentralLoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToSetPasswordActivity(String email) {
        Intent intent = new Intent(this, SetPasswordActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    void navigateToCentralActivity() {
        Intent intent = new Intent(this, CentralActivity.class);
        startActivity(intent);
        finish();
    }
}