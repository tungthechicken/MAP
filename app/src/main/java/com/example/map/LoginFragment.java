package com.example.map;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
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

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private final RetrofitInterface retrofitInterface;

    public LoginFragment(RetrofitInterface retrofitInterface) {
        this.retrofitInterface = retrofitInterface;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        Button loginBtn = view.findViewById(R.id.login_button);
        Button bypassLoginBtn = view.findViewById(R.id.bypass_login_button); // For testing purposes
        final EditText emailEdit = view.findViewById(R.id.login_email);
        final EditText passwordEdit = view.findViewById(R.id.login_password);

        loginBtn.setOnClickListener(view1 -> {
            String email = emailEdit.getText().toString();
            String password = passwordEdit.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

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
                        Intent intent = new Intent(getActivity(), CentralActivity.class);
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
        });

        bypassLoginBtn.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), CentralActivity.class);
            startActivity(intent);
            requireActivity().finish(); // Kill the login activity so the user can't go back unless they log out
        });

        return view;
    }


    // Add underline to the forgot password text, Android Studio does not support underline in XML
    // for some reason, it's stupid. I hate this.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView forgotPasswordTextView = view.findViewById(R.id.forgot_password);
        forgotPasswordTextView.setPaintFlags(forgotPasswordTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }
}