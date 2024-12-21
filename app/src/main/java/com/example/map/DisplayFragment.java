package com.example.map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

public class DisplayFragment extends BottomSheetDialogFragment {
    private static final String PREFS_NAME = "prefs";
    private static final String PREF_LANGUAGE = "language";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display, container, false);

        RadioGroup radioGroup = view.findViewById(R.id.radio_group_languages);
        RadioButton radioEng = view.findViewById(R.id.radio_eng);
        RadioButton radioVi = view.findViewById(R.id.radio_vi);

        // Lấy ngôn ngữ đã lưu và đặt RadioButton tương ứng
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentLang = prefs.getString(PREF_LANGUAGE, "en");

        if ("en".equals(currentLang)) {
            radioEng.setChecked(true);
        } else if ("vi".equals(currentLang)) {
            radioVi.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_eng) {
                    setLocale("en");
                } else if (checkedId == R.id.radio_vi) {
                    setLocale("vn");
                }
            }
        });

        return view;
    }

    public void setLocale(String lang) {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentLang = prefs.getString(PREF_LANGUAGE, "en");

        if (!currentLang.equals(lang)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_LANGUAGE, lang);
            editor.apply();

            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            getActivity().getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());

            // Khởi động lại CentralLoginActivity
            Intent intent = new Intent(getActivity(), CentralLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}