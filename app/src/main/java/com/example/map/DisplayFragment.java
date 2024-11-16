package com.example.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

public class DisplayFragment extends Fragment {

    private ImageButton backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display, container, false);

        // Lấy reference của backButton từ layout
        backButton = view.findViewById(R.id.back_button);

        // Set sự kiện cho nút back để quay lại Fragment cha
        backButton.setOnClickListener(v -> {
            // Quay lại Fragment trước đó trong back stack của Fragment cha
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}
