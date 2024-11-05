package com.example.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SignupActivity extends AppCompatActivity {

    EditText signupName;
    EditText signupEmail;
    EditText signupPassword;
    EditText signupConfirmPassword;
    Button signupButton;
    TextView loginPrompt;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    ImageView googleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupName = findViewById(R.id.signupName);
        signupEmail = findViewById(R.id.signupEmail);
        signupPassword = findViewById(R.id.signupPassword);
        signupConfirmPassword = findViewById(R.id.signupConfirmPassword);
        signupButton = findViewById(R.id.signupButton);
        loginPrompt = findViewById(R.id.loginPrompt);

        signupName.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        signupName.setRawInputType(InputType.TYPE_CLASS_TEXT);

        signupEmail.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        signupEmail.setRawInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        signupPassword.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        signupPassword.setRawInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        signupConfirmPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        signupConfirmPassword.setRawInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        signupName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                signupEmail.requestFocus();
                return true;
            }
            return false;
        });

        signupEmail.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                signupPassword.requestFocus();
                return true;
            }
            return false;
        });

        signupPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                signupConfirmPassword.requestFocus();
                return true;
            }
            return false;
        });

        signupConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                signupButton.performClick();
                return true;
            }
            return false;
        });

        signupButton.setOnClickListener(view -> {
            // Add your signup logic here
            Toast.makeText(getApplicationContext(), "Signup button clicked", Toast.LENGTH_SHORT).show();
        });

        loginPrompt.setOnClickListener(view -> {
            finish();
        });

        googleBtn = findViewById(R.id.google_btn1);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        googleBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                signIn();
            }
        });
    }

    void signIn(){
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            navigateToSecondActivity();
            try {
                task.getResult(ApiException.class);
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void navigateToSecondActivity(){
        Intent intent = new Intent(SignupActivity.this, CentralActivity.class);
        startActivity(intent);
    }
}