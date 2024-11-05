package com.example.map;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsFragment extends Fragment {
    private LinearLayout profileEdit;
    private ImageButton chitietProfile;

    private LinearLayout notiEdit;
    private ImageButton chitietNoti;

    private LinearLayout updateEdit;
    private ImageButton chitietUpdate;

    private LinearLayout displayEdit;
    private ImageButton chitietDisplay;

    private LinearLayout privacyEdit;
    private ImageButton chitietPrivacy;

    private LinearLayout qaaEdit;
    private ImageButton chitietQaa;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        profileEdit = view.findViewById(R.id.Profile);
        chitietProfile = view.findViewById(R.id.chitiet1);

        notiEdit = view.findViewById(R.id.Noti);
        chitietNoti = view.findViewById(R.id.chitiet2);

        // Setup logout button and destroy everything
        ImageView logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            // Handle logout button click
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        displayEdit = view.findViewById(R.id.display);
        chitietDisplay = view.findViewById(R.id.chitiet3);

        updateEdit = view.findViewById(R.id.update);
        chitietUpdate = view.findViewById(R.id.chitiet4);

        privacyEdit = view.findViewById(R.id.privacy);
        chitietPrivacy = view.findViewById(R.id.chitiet5);

        qaaEdit = view.findViewById(R.id.QaA);
        chitietQaa = view.findViewById(R.id.chitiet6);

        // Setup logout button and destroy everything
        LinearLayout logOut = view.findViewById(R.id.logOut);
        logOut.setOnClickListener(v -> {
            // Handle logout button click
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        // Chuyển đến EditProfileActivity
        profileEdit.setOnClickListener(v -> openEditProfile());
        chitietProfile.setOnClickListener(v -> openEditProfile());

        // Chuyển đến EditNotiActivity
        notiEdit.setOnClickListener(v -> openNoti());
        chitietNoti.setOnClickListener(v -> openNoti());

        // Chuyển đến DisplayActivity
        displayEdit.setOnClickListener(v -> openDisplay());
        chitietDisplay.setOnClickListener(v -> openDisplay());

        // Chuyển đến UpdateActivity
        updateEdit.setOnClickListener(v -> openUpdate());
        chitietUpdate.setOnClickListener(v -> openUpdate());

        // Chuyển đến PrivacyActivity
        privacyEdit.setOnClickListener(v -> openPrivacy());
        chitietPrivacy.setOnClickListener(v -> openPrivacy());

        // Chuyển đến QAAActivity
        qaaEdit.setOnClickListener(v -> openQAA());
        chitietQaa.setOnClickListener(v -> openQAA());

        return view;
    }

    private void openEditProfile() {
        // Chuyển đến editProfileActivity
        Intent intent = new Intent(getActivity(), editProfileActivity.class);
        startActivity(intent);
    }

    private void openNoti() {
        // Chuyển đến editNotiActivity
        Intent intent = new Intent(getActivity(), editNotiActivity.class);
        startActivity(intent);
    }

    private void openUpdate() {
        // Chuyển đến UpdateActivity
        Intent intent = new Intent(getActivity(), UpdateActivity.class);
        startActivity(intent);
    }

    private void openDisplay() {
        // Chuyển đến DisplayActivity
        Intent intent = new Intent(getActivity(), DisplayActivity.class);
        startActivity(intent);
    }

    private void openPrivacy() {
        // Chuyển đến PrivacyActivity
        Intent intent = new Intent(getActivity(), PrivacyActivity.class);
        startActivity(intent);
    }

    private void openQAA() {
        // Chuyển đến QAAActivity
        Intent intent = new Intent(getActivity(), QuestionAndAnswerActivity.class);
        startActivity(intent);
    }
}