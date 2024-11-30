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
import androidx.viewpager.widget.ViewPager;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupFragment extends Fragment {

    private final RetrofitInterface retrofitInterface;

    public SignupFragment(RetrofitInterface retrofitInterface) {
        this.retrofitInterface = retrofitInterface;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_fragment, container, false);

        Button signupBtn = view.findViewById(R.id.signup_button);
        final EditText nameEdit = view.findViewById(R.id.signup_name);
        final EditText emailEdit = view.findViewById(R.id.signup_email);
        final EditText passwordEdit = view.findViewById(R.id.signup_password);
        final EditText passwordConfirmEdit = view.findViewById(R.id.signup_password_confirm);

        signupBtn.setOnClickListener(view1 -> {
            String name = nameEdit.getText().toString();
            String email = emailEdit.getText().toString();
            String password = passwordEdit.getText().toString();
            String password_confirm = passwordConfirmEdit.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            } else if (!password.equals(password_confirm)) {
                Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            } else if (!email.contains("@") || !email.contains(".")) {
                Toast.makeText(getActivity(), "Invalid email", Toast.LENGTH_SHORT).show();
                return;
            }


            HashMap<String, String> map = new HashMap<>();
            map.put("name", name);
            map.put("email", email);
            map.put("password", password);
            map.put("isLinkedGoogle", "false");

            Call<Void> call = retrofitInterface.executeSignup(map);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.code() == 200) {
                        Toast.makeText(getActivity(), "Sign-up successfully! Going back to login screen...", Toast.LENGTH_LONG).show();
                        // Go back to login screen
                        if (getActivity() != null) {
                            ViewPager viewPager = getActivity().findViewById(R.id.viewPager);
                            if (viewPager != null) {
                                viewPager.setCurrentItem(0); // Switch to the LoginFragment
                            }
                        }
                    } else if (response.code() == 400) {
                        Toast.makeText(getActivity(), "Already registered", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        return view;
    }
}