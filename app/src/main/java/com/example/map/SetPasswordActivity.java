package com.example.map;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SetPasswordActivity extends AppCompatActivity {

    private RetrofitInterface retrofitInterface;
    private EditText passwordEdit;
    private EditText confirmPasswordEdit;
    private String email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        passwordEdit = findViewById(R.id.password);
        confirmPasswordEdit = findViewById(R.id.confirm_password);
        Button setPasswordBtn = findViewById(R.id.set_password_button);

        setPasswordBtn.setOnClickListener(v -> {
            String password = passwordEdit.getText().toString();
            String confirmPassword = confirmPasswordEdit.getText().toString();

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            setPassword(email, password);
        });

        String BASE_URL = getString(R.string.retrofit_url);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);
    }

    private void setPassword(String email, String password) {
        HashMap<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("password", password);

        Call<Void> call = retrofitInterface.setPassword(map);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(SetPasswordActivity.this, "Password set successfully", Toast.LENGTH_LONG).show();
                        navigateToCentralActivity();
                    } else {
                        Toast.makeText(SetPasswordActivity.this, "Failed to set password", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(SetPasswordActivity.this, t.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void navigateToCentralActivity() {
        Intent intent = new Intent(this, CentralActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}