package com.example.map;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;

public class IntroMap1DialogFragment extends DialogFragment {

    Button skipButton;
    Button backButton;
    Button nextButton;
    int count = 1;
    ImageView imageView;
    TextView textViewhd;
    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout và gán vào biến view
        View view = inflater.inflate(R.layout.fragment_intro_map1_dialog, container, false);

        imageView = view.findViewById(R.id.ImageViewPage);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd1));

        textViewhd = view.findViewById(R.id.textViewhd);
        textViewhd.setText(R.string.hd1);



        // Tìm nút Skip và gắn sự kiện click
        skipButton = view.findViewById(R.id.skipButton);
        skipButton.setOnClickListener(v -> dismiss()); // Đóng DialogFragment khi nhấn Skip

        backButton = view.findViewById(R.id.backbtn);
        backButton.setOnClickListener(v -> backpage());


        nextButton = view.findViewById(R.id.nextbtn);
        nextButton.setOnClickListener(v -> nextpage());

        return view; // Trả về view đã inflate
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    void nextpage()
    {
        if (count==1)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd2));
            textViewhd.setText(R.string.hd2);
            count++;
        }
        else if (count==2)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd3));
            textViewhd.setText(R.string.hd3);
            count++;
        }
        else if (count==3)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd4));
            textViewhd.setText(R.string.hd4);
            count++;
        }
        else if (count == 4)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd5));
            textViewhd.setText(R.string.hd5);
            count++;
        }
        else if (count == 5)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd6));
            textViewhd.setText(R.string.hd6);
            count++;
        }
        else if (count == 6)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd7));
            textViewhd.setText(R.string.hd7);
            count++;
        }
        else if (count == 7)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd8));
            textViewhd.setText(R.string.hd8);
            count++;
        }
        else if (count == 8)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd9));
            textViewhd.setText(R.string.hd9);
            count++;
        }
        else if (count == 9)
        {
            return;
        }

    }
    @SuppressLint("UseCompatLoadingForDrawables")
    void backpage()
    {
        if (count==2)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd1));
            textViewhd.setText(R.string.hd1);
            count--;
        }
        else if (count==3)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd2));
            textViewhd.setText(R.string.hd2);
            count--;
        }
        else if (count==4)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd3));
            textViewhd.setText(R.string.hd3);
            count--;
        }
        else if (count==5)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd4));
            textViewhd.setText(R.string.hd4);
            count--;
        }
        else if (count==6)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd5));
            textViewhd.setText(R.string.hd5);
            count--;
        }
        else if (count==7)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd6));
            textViewhd.setText(R.string.hd6);
            count--;
        }
        else if (count==8)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd7));
            textViewhd.setText(R.string.hd7);
            count--;
        }
        else if (count==9)
        {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.hd8));
            textViewhd.setText(R.string.hd8);
            count--;
        }
        else if (count==1)
        {
            return;
        }
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
