package com.example.map;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    TextView usernameTextView, nameAPI,emailAPI;
    private RetrofitInterface retrofitInterface;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageButton backButton = view.findViewById(R.id.back_button);
        Button btEdit = view.findViewById(R.id.btEdit);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        nameAPI = view.findViewById(R.id.nameAPI);
        emailAPI = view.findViewById(R.id.emailAPI);
        // Lấy avatar đã lưu
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        int avatarId = prefs.getInt("avatar", R.drawable.baseline_person_24); // Giá trị mặc định

        // Đặt avatar vào ImageView
        ImageView avatarImageView = view.findViewById(R.id.avatarImageView);
        avatarImageView.setImageResource(avatarId);

        // Lấy dữ liệu từ Bundle và thiết lập vào các TextView
        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.getString("name");
            String email = bundle.getString("email");

            if (name != null) {
                usernameTextView.setText(name);
                nameAPI.setText(name);
            }

            if (email != null) {
                emailAPI.setText(email);
            }
        }

// Khởi tạo Retrofit
        String baseUrl = requireContext().getString(R.string.retrofit_url);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);

        // Gọi API để lấy thông tin người dùng
        getUserData();
        // Set sự kiện cho nút back để quay lại setting
        backButton.setOnClickListener(v -> {
            // Quay lại trang ProfileFragment
            getParentFragmentManager().popBackStack(); // Quay lại fragment trước đó trong back stack
        });
        btEdit.setOnClickListener(v -> {
            // Tạo đối tượng EditProfileFragment
            EditProfileFragment editProfileFragment = new EditProfileFragment();
            // Chuyển sang EditProfileFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, editProfileFragment); // Thay thế fragment hiện tại
            transaction.addToBackStack(null); // Thêm vào back stack để có thể quay lại
            transaction.commit();
        });
        return view;
    }
    private void getUserData() {
        // Giả sử bạn có email của người dùng từ SharedPreferences hoặc Bundle
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String email = prefs.getString("email", null);

        if (email != null) {
            Call<UserData> call = retrofitInterface.getUserByEmail(email);
            call.enqueue(new Callback<UserData>() {
                @Override
                public void onResponse(Call<UserData> call, Response<UserData> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserData userData = response.body();
                        usernameTextView.setText(userData.getName());
                        nameAPI.setText(userData.getName());
                        emailAPI.setText(userData.getEmail());
                    } else {
                        Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserData> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(requireContext(), "Email not found", Toast.LENGTH_SHORT).show();
        }
    }
}
