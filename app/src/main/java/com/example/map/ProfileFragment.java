package com.example.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ProfileFragment extends Fragment {

    TextView usertext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageButton backButton = view.findViewById(R.id.back_button);
        Button btEdit = view.findViewById(R.id.btEdit);
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
