package com.example.map;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ForgotPasswordActivity extends AppCompatActivity {

    private RetrofitInterface retrofitInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        String BASE_URL = getString(R.string.retrofit_url);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        showForgotPasswordEmailFragment();
    }

    private void showForgotPasswordEmailFragment() {
        Fragment fragment = new ForgotPasswordEmailFragment(retrofitInterface);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.forgot_password_container, fragment);
        transaction.commit();
    }

    public void showResetPasswordFragment(String email) {
        Fragment fragment = new ResetPasswordFragment(retrofitInterface, email);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.forgot_password_container, fragment);
        transaction.commit();
    }
}