package com.example.map;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
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

    private EditText otpEditText1, otpEditText2, otpEditText3, otpEditText4, otpEditText5, otpEditText6;
    private EditText newPasswordEditText;
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

        otpEditText1 = view.findViewById(R.id.otpEditText1);
        otpEditText2 = view.findViewById(R.id.otpEditText2);
        otpEditText3 = view.findViewById(R.id.otpEditText3);
        otpEditText4 = view.findViewById(R.id.otpEditText4);
        otpEditText5 = view.findViewById(R.id.otpEditText5);
        otpEditText6 = view.findViewById(R.id.otpEditText6);
        otpDialog = view.findViewById(R.id.otpDialog);
        newPasswordDialog = view.findViewById(R.id.newPasswordDialog);
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        verifyOtpButton = view.findViewById(R.id.verifyOtpButton);
        resetPasswordButton = view.findViewById(R.id.resetPasswordButton);
        resendOtpTextView = view.findViewById(R.id.resendOtpTextView);

        setupOtpInputs();

        verifyOtpButton.setOnClickListener(v -> {
            String otp = otpEditText1.getText().toString().trim() +
                    otpEditText2.getText().toString().trim() +
                    otpEditText3.getText().toString().trim() +
                    otpEditText4.getText().toString().trim() +
                    otpEditText5.getText().toString().trim() +
                    otpEditText6.getText().toString().trim();
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
            if (!newPassword.isEmpty()) {
                resetPassword(userEmail, newPassword);
            } else {
                Toast.makeText(getActivity(), "Please enter new password", Toast.LENGTH_SHORT).show();
            }
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

    private void setupOtpInputs() {
        otpEditText1.addTextChangedListener(new GenericTextWatcher(otpEditText1, otpEditText2, null));
        otpEditText2.addTextChangedListener(new GenericTextWatcher(otpEditText2, otpEditText3, otpEditText1));
        otpEditText3.addTextChangedListener(new GenericTextWatcher(otpEditText3, otpEditText4, otpEditText2));
        otpEditText4.addTextChangedListener(new GenericTextWatcher(otpEditText4, otpEditText5, otpEditText3));
        otpEditText5.addTextChangedListener(new GenericTextWatcher(otpEditText5, otpEditText6, otpEditText4));
        otpEditText6.addTextChangedListener(new GenericTextWatcher(otpEditText6, null, otpEditText5));
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
        otpEditText1.setVisibility(View.GONE);
        otpEditText2.setVisibility(View.GONE);
        otpEditText3.setVisibility(View.GONE);
        otpEditText4.setVisibility(View.GONE);
        otpEditText5.setVisibility(View.GONE);
        otpEditText6.setVisibility(View.GONE);
        newPasswordEditText.setVisibility(View.VISIBLE);
        verifyOtpButton.setVisibility(View.GONE);
        otpDialog.setVisibility(View.GONE);
        resendOtpTextView.setVisibility(View.GONE);
        newPasswordDialog.setVisibility(View.VISIBLE);
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

    public static class GenericTextWatcher implements TextWatcher, View.OnKeyListener {

        private final EditText currentView;
        private final EditText nextView;
        private final EditText previousView;

        public GenericTextWatcher(EditText currentView, EditText nextView, EditText previousView) {
            this.currentView = currentView;
            this.nextView = nextView;
            this.previousView = previousView;
            this.currentView.setOnKeyListener(this);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (currentView.getText().toString().isEmpty() && previousView != null) {
                    previousView.requestFocus();
                    previousView.setText("");
                }
            }
            return false;
        }
    }
}