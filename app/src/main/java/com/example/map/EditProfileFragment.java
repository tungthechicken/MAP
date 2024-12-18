package com.example.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

public class EditProfileFragment extends Fragment {

    private ImageButton backButton;
    private String name;
    EditText usernametext;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            name = bundle.getString("name");
        }

        usernametext = view.findViewById(R.id.editText);
        usernametext.setText(getUsername());

        // Tìm nút back
        backButton = view.findViewById(R.id.back_button);  // Lấy ImageButton từ layout

        // Set sự kiện cho nút back để quay lại ProfileFragment
        backButton.setOnClickListener(v -> {
            // Quay lại trang ProfileFragment
            getParentFragmentManager().popBackStack(); // Quay lại fragment trước đó trong back stack
        });
        return view;
    }
    private String  getUsername() {
        return name;
    }
}