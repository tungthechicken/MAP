package com.example.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class IntroMap1DialogFragment extends DialogFragment {

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout và gán vào biến view
        View view = inflater.inflate(R.layout.fragment_intro_map1_dialog, container, false);

        // Tìm nút Skip và gắn sự kiện click
        Button skipButton = view.findViewById(R.id.skipButton);
        skipButton.setOnClickListener(v -> dismiss()); // Đóng DialogFragment khi nhấn Skip

        Button backButton = view.findViewById(R.id.backbtn);

        Button nextButton = view.findViewById(R.id.nextbtn);

        return view; // Trả về view đã inflate
    }


    @Override
    public void onStart() {
        super.onStart();
        // Đặt kích thước dialog full màn hình
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}
