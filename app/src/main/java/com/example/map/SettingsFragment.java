package com.example.map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.FrameLayout;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SettingsFragment extends Fragment {
    private GoogleSignInClient gsc;

    private LinearLayout profile;
    private LinearLayout notiEdit;
    private LinearLayout displayEdit;
    private LinearLayout aboutEdit;
    private LinearLayout qaaEdit;
    private View overlay;
    private String name;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            name = bundle.getString("name");
        }
        if (name == null) {
            name = "User"; // Default value if name is null
        }

        // Initialize GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(getActivity(), gso);

        // Find views
        profile = view.findViewById(R.id.Profile);
        notiEdit = view.findViewById(R.id.Noti);
        LinearLayout logOutLayout = view.findViewById(R.id.logOut);
        displayEdit = view.findViewById(R.id.display);
        aboutEdit = view.findViewById(R.id.about);
        qaaEdit = view.findViewById(R.id.QaA);
        overlay = view.findViewById(R.id.overlay);

        // Set click listeners
        logOutLayout.setOnClickListener(v -> openlogOut());
        profile.setOnClickListener(v -> openProfile());
        notiEdit.setOnClickListener(v -> openNoti());
        displayEdit.setOnClickListener(v -> openDisplay());
        aboutEdit.setOnClickListener(v -> openAbout());
        qaaEdit.setOnClickListener(v -> openQAA());

        return view;
    }
    private void openProfile() {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", name); // Pass the username
        profileFragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openNoti() {
        // Hiển thị EditNotiFragment dưới dạng BottomSheet
        EditNotiFragment editNotiFragment = new EditNotiFragment();  // Thay AboutFragment bằng EditNotiFragment
        editNotiFragment.show(getChildFragmentManager(), editNotiFragment.getTag());

        // Hiển thị overlay khi BottomSheet mở
        overlay.setVisibility(View.VISIBLE);

        // Đóng BottomSheet khi nhấn vào overlay
        overlay.setOnClickListener(v -> {
            editNotiFragment.dismiss();  // Đóng BottomSheet
            overlay.setVisibility(View.GONE);  // Ẩn overlay
        });
    }

    private void openDisplay() {
        // Hiển thị DisplayFragment dưới dạng BottomSheet
        DisplayFragment displayFragment = new DisplayFragment();
        displayFragment.show(getChildFragmentManager(), displayFragment.getTag());

        // Hiển thị overlay khi BottomSheet mở
        overlay.setVisibility(View.VISIBLE);

        // Đóng BottomSheet khi nhấn vào overlay
        overlay.setOnClickListener(v -> {
            displayFragment.dismiss();  // Đóng BottomSheet
            overlay.setVisibility(View.GONE);  // Ẩn overlay
        });
    }

    private void openAbout() {
        // Hiển thị AboutFragment dưới dạng BottomSheet
        AboutFragment aboutFragment = new AboutFragment();  // Thay DisplayFragment bằng AboutFragment
        aboutFragment.show(getChildFragmentManager(), aboutFragment.getTag());

        // Hiển thị overlay khi BottomSheet mở
        overlay.setVisibility(View.VISIBLE);

        // Đóng BottomSheet khi nhấn vào overlay
        overlay.setOnClickListener(v -> {
            aboutFragment.dismiss();  // Đóng BottomSheet
            overlay.setVisibility(View.GONE);  // Ẩn overlay
        });
    }

    private void openQAA() {
        // Hiển thị QAAFragment dưới dạng BottomSheet
        QAAFragment qaaFragment = new QAAFragment();  // Thay AboutFragment bằng QAAFragment
        qaaFragment.show(getChildFragmentManager(), qaaFragment.getTag());

// Hiển thị overlay khi BottomSheet mở
        overlay.setVisibility(View.VISIBLE);

// Đóng BottomSheet khi nhấn vào overlay
        overlay.setOnClickListener(v -> {
            qaaFragment.dismiss();  // Đóng BottomSheet
            overlay.setVisibility(View.GONE);  // Ẩn overlay
        });

    }
    private String  getUsername() {
        return name;
    }
    private void openlogOut() {
        // Tạo một AlertDialog để xác nhận đăng xuất
        new AlertDialog.Builder(getActivity())
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Người dùng xác nhận đăng xuất
                    performLogout();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Người dùng hủy đăng xuất
                    dialog.dismiss();
                })
                .show();
    }

    // Phương thức thực hiện đăng xuất
    private void performLogout() {
        // Clear the SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Sign out of Google
        gsc.signOut().addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Start the CentralLoginActivity
                Intent intent = new Intent(getActivity(), CentralLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

}