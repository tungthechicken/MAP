package com.example.map;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordFragment extends Fragment {

    private EditText otpEditText;
    private EditText newPasswordEditText, confirmPasswordEditText;
    private TextView otpDialog, newPasswordDialog, resendOtpTextView;
    private Button verifyOtpButton, resetPasswordButton;
    private RetrofitInterface retrofitInterface;
    private String userEmail;
    private CountDownTimer countDownTimer;

    public ResetPasswordFragment(RetrofitInterface retrofitInterface, String email) {
        this.retrofitInterface = retrofitInterface;
        this.userEmail = email;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        otpEditText = view.findViewById(R.id.otpEditText);
        otpDialog = view.findViewById(R.id.otpDialog);
        newPasswordDialog = view.findViewById(R.id.newPasswordDialog);
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = view.findViewById(R.id.newPasswordEditTextConfirm);
        verifyOtpButton = view.findViewById(R.id.verifyOtpButton);
        resetPasswordButton = view.findViewById(R.id.resetPasswordButton);
        resendOtpTextView = view.findViewById(R.id.resendOtpTextView);

        verifyOtpButton.setOnClickListener(v -> {
            String otp = otpEditText.getText().toString().trim();
            if (!otp.isEmpty()) {
                verifyOtp(userEmail, otp);
            } else {
                Toast.makeText(getActivity(), "Please enter OTP", Toast.LENGTH_SHORT).show();
            }
        });

        resendOtpTextView.setOnClickListener(v -> {
            resendOtpTextView.setEnabled(false);
            startResendOtpCooldown();
            sendResetPasswordOTPRequest(userEmail);
        });

        resendOtpTextView.setEnabled(false); // Disable initially
        startResendOtpCooldown(); // Start cooldown on fragment creation

        resetPasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            resetPassword(userEmail, newPassword);
        });

        return view;
    }

    private void startResendOtpCooldown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                resendOtpTextView.setText("Resend OTP in " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                resendOtpTextView.setText("Resend OTP");
                resendOtpTextView.setEnabled(true);
            }
        }.start();
    }

    private void sendResetPasswordOTPRequest(String email) {
        HashMap<String, String> map = new HashMap<>();
        map.put("email", email);

        Call<Void> call = retrofitInterface.forgotPassword(map);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(getActivity(), "OTP sent to your email", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getActivity(), "Error contacting server!", Toast.LENGTH_SHORT).show();
            }
        });
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
        newPasswordEditText.setVisibility(View.VISIBLE);
        verifyOtpButton.setVisibility(View.GONE);
        otpDialog.setVisibility(View.GONE);
        resendOtpTextView.setVisibility(View.GONE);
        newPasswordDialog.setVisibility(View.VISIBLE);
        confirmPasswordEditText.setVisibility(View.VISIBLE);
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
                    Toast.makeText(getActivity(), "Password has been reset. Going back to login screen...", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        ViewPager viewPager = getActivity().findViewById(R.id.viewPager);
                        if (viewPager != null) {
                            viewPager.setCurrentItem(0);
                        }
                    }
                    requireActivity().finish();
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