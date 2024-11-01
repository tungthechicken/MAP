package com.example.map;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity_Google extends AppCompatActivity {

    EditText username;
    EditText password;
    Button loginButton;
    TextView signupPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signupPrompt = findViewById(R.id.signupPrompt);

        username.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        username.setRawInputType(InputType.TYPE_CLASS_TEXT);

        username.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                password.requestFocus();
                return true;
            }
            return false;
        });

        password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginButton.performClick();
                return true;
            }
            return false;
        });

        loginButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity_Google.this, MapActivity.class);
            startActivity(intent);
        });

        signupPrompt.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity_Google.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}