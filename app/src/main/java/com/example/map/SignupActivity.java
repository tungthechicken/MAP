package com.example.map;

import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    EditText signupUsername;
    EditText signupPassword;
    Button signupButton;
    TextView loginPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupUsername = findViewById(R.id.signupUsername);
        signupPassword = findViewById(R.id.signupPassword);
        signupButton = findViewById(R.id.signupButton);
        loginPrompt = findViewById(R.id.loginPrompt);

        signupUsername.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        signupUsername.setRawInputType(InputType.TYPE_CLASS_TEXT);

        signupUsername.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                signupPassword.requestFocus();
                return true;
            }
            return false;
        });

        signupPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                signupButton.performClick();
                return true;
            }
            return false;
        });

        signupButton.setOnClickListener(view -> {
            Toast.makeText(this, "Signup button clicked", Toast.LENGTH_SHORT).show();
        });

        loginPrompt.setOnClickListener(view -> {
            finish();
        });
    }
}