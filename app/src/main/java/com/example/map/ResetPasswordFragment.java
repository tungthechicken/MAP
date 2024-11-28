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
    private TextView otpDialog, newPasswordDialog;
    private Button verifyOtpButton, resetPasswordButton, debugOtpButton, debugNewPasswordButton;
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
        debugOtpButton = view.findViewById(R.id.debugOtpButton);
        debugNewPasswordButton = view.findViewById(R.id.debugNewPasswordButton);

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

        resetPasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString().trim();
            if (!newPassword.isEmpty()) {
                resetPassword(userEmail, newPassword);
            } else {
                Toast.makeText(getActivity(), "Please enter new password", Toast.LENGTH_SHORT).show();
            }
        });

        debugOtpButton.setOnClickListener(v -> showOtpPhase());
        debugNewPasswordButton.setOnClickListener(v -> showResetPasswordPrompt());

        return view;
    }

    /**
     * Sets up the OTP input fields with text watchers to handle text changes and key events.
     */
    private void setupOtpInputs() {
        otpEditText1.addTextChangedListener(new GenericTextWatcher(otpEditText1, otpEditText2, null));
        otpEditText2.addTextChangedListener(new GenericTextWatcher(otpEditText2, otpEditText3, otpEditText1));
        otpEditText3.addTextChangedListener(new GenericTextWatcher(otpEditText3, otpEditText4, otpEditText2));
        otpEditText4.addTextChangedListener(new GenericTextWatcher(otpEditText4, otpEditText5, otpEditText3));
        otpEditText5.addTextChangedListener(new GenericTextWatcher(otpEditText5, otpEditText6, otpEditText4));
        otpEditText6.addTextChangedListener(new GenericTextWatcher(otpEditText6, null, otpEditText5));
    }

    /**
     * Verifies the OTP by making a network call to the server.
     * @param email The user's email address.
     * @param otp The OTP entered by the user.
     */
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

    /**
     * Shows the OTP input phase by making the OTP input fields visible. For debugging purposes only.
     * Remember to remove this method in production!
     */
    private void showOtpPhase() {
        otpEditText1.setVisibility(View.VISIBLE);
        otpEditText2.setVisibility(View.VISIBLE);
        otpEditText3.setVisibility(View.VISIBLE);
        otpEditText4.setVisibility(View.VISIBLE);
        otpEditText5.setVisibility(View.VISIBLE);
        otpEditText6.setVisibility(View.VISIBLE);
        newPasswordEditText.setVisibility(View.GONE);
        verifyOtpButton.setVisibility(View.VISIBLE);
        otpDialog.setVisibility(View.VISIBLE);
        newPasswordDialog.setVisibility(View.GONE);
        resetPasswordButton.setVisibility(View.GONE);
    }

    /**
     * Shows the reset password prompt by making the new password input field visible.
     */
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
        newPasswordDialog.setVisibility(View.VISIBLE);
        resetPasswordButton.setVisibility(View.VISIBLE);
    }

    /**
     * Resets the user's password by making a network call to the server.
     * @param email The user's email address.
     * @param newPassword The new password entered by the user.
     */
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
                    // Go back to login screen
                    if (getActivity() != null) {
                        ViewPager viewPager = getActivity().findViewById(R.id.viewPager);
                        if (viewPager != null) {
                            viewPager.setCurrentItem(0); // Switch to the LoginFragment
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

    /**
     * A generic text watcher to handle text changes and key events for OTP input fields.
     */
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