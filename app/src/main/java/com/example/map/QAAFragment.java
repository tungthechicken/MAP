package com.example.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.Fragment;

public class QAAFragment extends BottomSheetDialogFragment  {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_qaa, container, false);
        // Lấy tham chiếu đến các câu hỏi và câu trả lời
        TextView question1 = view.findViewById(R.id.question_1);
        TextView answer1 = view.findViewById(R.id.answer_1);

        TextView question2 = view.findViewById(R.id.question_2);
        TextView answer2 = view.findViewById(R.id.answer_2);

        TextView question3 = view.findViewById(R.id.question_3);
        TextView answer3 = view.findViewById(R.id.answer_3);

        TextView question4 = view.findViewById(R.id.question_4);
        TextView answer4 = view.findViewById(R.id.answer_4);

        TextView question5 = view.findViewById(R.id.question_5);
        TextView answer5 = view.findViewById(R.id.answer_5);

        // Thiết lập sự kiện cho các câu hỏi để hiển thị/ẩn câu trả lời
        question1.setOnClickListener(v -> toggleVisibility(answer1));
        question2.setOnClickListener(v -> toggleVisibility(answer2));
        question3.setOnClickListener(v -> toggleVisibility(answer3));
        question4.setOnClickListener(v -> toggleVisibility(answer4));
        question5.setOnClickListener(v -> toggleVisibility(answer5));

        return view;
    }
    // Hàm thay đổi visibility của câu trả lời
    private void toggleVisibility(TextView answer) {
        if (answer.getVisibility() == View.GONE) {
            answer.setVisibility(View.VISIBLE);
        } else {
            answer.setVisibility(View.GONE);
        }
    }

}