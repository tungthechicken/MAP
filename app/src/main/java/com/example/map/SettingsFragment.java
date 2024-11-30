package com.example.map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

        // Initialize GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(getActivity(), gso);

        // Find views
        profileEdit = view.findViewById(R.id.Profile);
        notiEdit = view.findViewById(R.id.Noti);
        LinearLayout logOutLayout = view.findViewById(R.id.logOut);
        displayEdit = view.findViewById(R.id.display);
        updateEdit = view.findViewById(R.id.update);
        aboutEdit = view.findViewById(R.id.about);
        qaaEdit = view.findViewById(R.id.QaA);

        // Set click listeners
        logOutLayout.setOnClickListener(v -> openlogOut());
        profileEdit.setOnClickListener(v -> openEditProfile());
        notiEdit.setOnClickListener(v -> openNoti());
        displayEdit.setOnClickListener(v -> openDisplay());
        updateEdit.setOnClickListener(v -> openUpdate());
        aboutEdit.setOnClickListener(v -> openAbout());
        qaaEdit.setOnClickListener(v -> openQAA());

        return view;
    }

    // Method to handle logout
    private void openlogOut() {
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

    private void openEditProfile() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new EditProfileFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openNoti() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new EditNotiFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openUpdate() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new UpdateFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openDisplay() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new DisplayFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openAbout() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new AboutFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openQAA() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new QAAFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}