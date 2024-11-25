package com.example.map;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class SettingsFragment extends Fragment {
    private LinearLayout profileEdit;

    private LinearLayout notiEdit;

    private LinearLayout updateEdit;

    private LinearLayout displayEdit;

    private LinearLayout aboutEdit;

    private LinearLayout qaaEdit;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Tìm các view
        profileEdit = view.findViewById(R.id.Profile);

        notiEdit = view.findViewById(R.id.Noti);

        LinearLayout logOutLayout = view.findViewById(R.id.logOut);


// Cài đặt sự kiện nhấn cho LinearLayout
        logOutLayout.setOnClickListener(v -> {
            openlogOut();
        });


        displayEdit = view.findViewById(R.id.display);

        updateEdit = view.findViewById(R.id.update);

        aboutEdit = view.findViewById(R.id.about);

        qaaEdit = view.findViewById(R.id.QaA);

        profileEdit.setOnClickListener(v -> openEditProfile());

        notiEdit.setOnClickListener(v -> openNoti());

        displayEdit.setOnClickListener(v -> openDisplay());

        updateEdit.setOnClickListener(v -> openUpdate());

        aboutEdit.setOnClickListener(v -> openAbout());

        qaaEdit.setOnClickListener(v -> openQAA());

        return view;
    }
    // Hàm xử lý việc đăng xuất
    private void openlogOut() {
        Intent intent = new Intent(getActivity(), CentralLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void openEditProfile() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new EditProfileFragment());
        // Thêm transaction vào back stack để người dùng có thể quay lại sau
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openNoti() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new EditNotiFragment());
        // Thêm transaction vào back stack để người dùng có thể quay lại sau
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openUpdate() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new UpdateFragment());
        // Thêm transaction vào back stack để người dùng có thể quay lại sau
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openDisplay() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new DisplayFragment());
        // Thêm transaction vào back stack để người dùng có thể quay lại sau
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openAbout() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new AboutFragment());
        // Thêm transaction vào back stack để người dùng có thể quay lại sau
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openQAA() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new QAAFragment());
        // Thêm transaction vào back stack để người dùng có thể quay lại sau
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
