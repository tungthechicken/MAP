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

public class ResetPasswordFragment extends Fragment {

    private EditText otpEditText;
    private EditText newPasswordEditText;
    private Button verifyOtpButton;
    private Button resetPasswordButton;
    private RetrofitInterface retrofitInterface;
    private String userEmail;

    public ResetPasswordFragment(RetrofitInterface retrofitInterface, String email) {
        this.retrofitInterface = retrofitInterface;
        this.userEmail = email;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        otpEditText = view.findViewById(R.id.otpEditText);
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        verifyOtpButton = view.findViewById(R.id.verifyOtpButton);
        resetPasswordButton = view.findViewById(R.id.resetPasswordButton);

        verifyOtpButton.setOnClickListener(v -> {
            String otp = otpEditText.getText().toString().trim();
            if (!otp.isEmpty()) {
                verifyOtp(userEmail, otp);
            } else {
                Toast.makeText(getActivity(), "Please enter OTP", Toast.LENGTH_SHORT).show();
            }
        });

        resetPasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString().trim();
            if (!newPassword.isEmpty()) {
                resetPassword(userEmail, newPassword);
            } else {
                Toast.makeText(getActivity(), "Please enter new password", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void verifyOtp(String email, String otp) {
        HashMap<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("otp", otp);

        Call<Void> call = retrofitInterface.verifyOtp(map);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showResetPasswordPrompt();
                } else {
                    Toast.makeText(getActivity(), "Invalid or expired OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getActivity(), "Error verifying OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResetPasswordPrompt() {
        otpEditText.setVisibility(View.GONE);
        verifyOtpButton.setVisibility(View.GONE);
        newPasswordEditText.setVisibility(View.VISIBLE);
        resetPasswordButton.setVisibility(View.VISIBLE);
    }

    private void resetPassword(String email, String newPassword) {
        HashMap<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("newPassword", newPassword);

        Call<Void> call = retrofitInterface.resetPassword(map);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), "Password has been reset", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Error resetting password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getActivity(), "Error resetting password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}