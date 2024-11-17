package com.example.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Tìm ImageButton theo ID
        ImageButton imageSetting = view.findViewById(R.id.imageSetting);

        // Set listener cho ImageButton
        imageSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khi nhấn vào ImageButton, chuyển đến SettingsFragment
                // Sử dụng FragmentTransaction để thay thế fragment hiện tại
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new SettingsFragment()); // Thay thế ProfileFragment bằng SettingsFragment
                transaction.addToBackStack(null); // Thêm vào back stack để có thể quay lại ProfileFragment
                transaction.commit();
            }
        });

        return view;
    }
}
