package com.example.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordEmailFragment extends Fragment {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private RetrofitInterface retrofitInterface;

    public ForgotPasswordEmailFragment(RetrofitInterface retrofitInterface) {
        this.retrofitInterface = retrofitInterface;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password_email, container, false);

        emailEditText = view.findViewById(R.id.emailEditText);
        resetPasswordButton = view.findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (!email.isEmpty()) {
                sendResetPasswordOTPRequest(email);
            } else {
                Toast.makeText(getActivity(), "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void sendResetPasswordOTPRequest(String email) {
        HashMap<String, String> map = new HashMap<>();
        map.put("email", email);

        Call<Void> call = retrofitInterface.forgotPassword(map);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), "OTP sent to your email", Toast.LENGTH_SHORT).show();
                    ((ForgotPasswordActivity) getActivity()).showResetPasswordFragment(email);
                } else {
                    Toast.makeText(getActivity(), "Error sending OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getActivity(), "Error sending OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }
}