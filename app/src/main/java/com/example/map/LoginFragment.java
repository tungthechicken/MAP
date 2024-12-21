package com.example.map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private final RetrofitInterface retrofitInterface;
    private SharedPreferences sharedPreferences;
    private EditText emailEdit;
    private EditText passwordEdit;
    private CheckBox rememberMeCheckBox;

    public LoginFragment(RetrofitInterface retrofitInterface) {
        this.retrofitInterface = retrofitInterface;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        Button loginBtn = view.findViewById(R.id.login_button);
        TextView forgotPasswordTextView = view.findViewById(R.id.forgot_password);
        emailEdit = view.findViewById(R.id.login_email);
        passwordEdit = view.findViewById(R.id.login_password);
        rememberMeCheckBox = view.findViewById(R.id.remember_me_checkbox);

        // For saving the user's email and password
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "loginPrefs",
                    masterKeyAlias,
                    getContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadPreferences();

        // Login button
        loginBtn.setOnClickListener(view1 -> {
            String email = emailEdit.getText().toString();
            String password = passwordEdit.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            performLogin(email, password);
        });

        // Forgot password text view
        forgotPasswordTextView.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), ForgotPasswordActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Auto-login if remember me is checked
        if (sharedPreferences.getBoolean("rememberMe", false)) {
            String email = sharedPreferences.getString("email", "");
            String password = sharedPreferences.getString("password", "");
            performLogin(email, password);
        }

        TextView forgotPasswordTextView = view.findViewById(R.id.forgot_password);
        forgotPasswordTextView.setPaintFlags(forgotPasswordTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    private void performLogin(String email, String password) {
        HashMap<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("password", password);

        Call<LoginResult> call = retrofitInterface.executeLogin(map);
        call.enqueue(new Callback<LoginResult>() {
            @Override
            public void onResponse(@NonNull Call<LoginResult> call, @NonNull Response<LoginResult> response) {
                if (response.code() == 200) {
                    LoginResult result = response.body();
                    assert result != null;
                    Toast.makeText(getActivity(), "Welcome " + result.getName(), Toast.LENGTH_LONG).show();
                    savePreferences(email, password);
                    // Lưu email vào SharedPreferences
                    SharedPreferences prefs = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("email", result.getEmail());
                    editor.apply();
                    Intent intent = new Intent(getActivity(), CentralActivity.class);
                    // Pass the user's email and name to the CentralActivity
                    intent.putExtra("email", email);
                    intent.putExtra("name", result.getName());
                    startActivity(intent);
                    requireActivity().finish(); // Kill the login activity so the user can't go back unless they log out
                } else if (response.code() == 404) {
                    Toast.makeText(getActivity(), "Wrong Credentials", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResult> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Save the user's email and password if the remember me checkbox is checked
    private void savePreferences(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (rememberMeCheckBox.isChecked()) {
            editor.putString("email", email);
            editor.putString("password", password);
            editor.putBoolean("rememberMe", true);
        } else {
            editor.clear();
        }
        editor.apply();
    }

    // Load the user's email and password if the remember me checkbox was checked
    private void loadPreferences() {
        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);
        if (rememberMe) {
            String email = sharedPreferences.getString("email", "");
            String password = sharedPreferences.getString("password", "");
            emailEdit.setText(email);
            passwordEdit.setText(password);
            rememberMeCheckBox.setChecked(true);
        }
    }
}