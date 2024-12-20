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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    TextView usernameTextView, nameAPI,emailAPI;

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

        // Retrieve the username from the arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.getString("name");
            usernameTextView.setText(name);
            nameAPI.setText(name);
            // Display the username
        }
        Bundle bundlee = getArguments();
        if (bundlee != null) {
            String email = bundlee.getString("email");
            emailAPI.setText(email);}


        // Set sự kiện cho nút back để quay lại ProfileFragment
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
}
