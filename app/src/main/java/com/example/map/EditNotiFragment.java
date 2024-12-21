package com.example.map;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.media.MediaPlayer;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EditNotiFragment extends BottomSheetDialogFragment {

    private AudioManager audioManager;
    private RadioGroup radioGroupApp;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "AppPreferences";
    private static final String MODE_KEY = "Mode";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout cho BottomSheet
        View rootView = inflater.inflate(R.layout.fragment_edit_noti, container, false);

        // Khởi tạo AudioManager để thay đổi chế độ âm thanh trong ứng dụng
        audioManager = (AudioManager) getActivity().getSystemService(getContext().AUDIO_SERVICE);

        // Lấy tham chiếu đến RadioGroup
        radioGroupApp = rootView.findViewById(R.id.radio_group_app);
        // Lấy SharedPreferences để lưu chế độ người dùng đã chọn
        sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, getContext().MODE_PRIVATE);

        // Đặt chế độ âm thanh ban đầu khi mở app (chế độ mặc định là "normal")
        int savedMode = sharedPreferences.getInt(MODE_KEY, AudioManager.RINGER_MODE_NORMAL);
        if (savedMode == AudioManager.RINGER_MODE_NORMAL) {
            setNormalMode();
            radioGroupApp.check(R.id.radio_normalMode); // Chọn chế độ bình thường
        } else if (savedMode == AudioManager.RINGER_MODE_VIBRATE) {
            setVibrationMode();
            radioGroupApp.check(R.id.radio_vibrationMode); // Chọn chế độ rung
        }

        // Lắng nghe sự thay đổi trong RadioGroup
        radioGroupApp.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_normalMode) {
                setNormalMode();
                playSound(R.raw.notification);  // Phát âm thanh khi chọn chế độ bình thường
                saveMode(AudioManager.RINGER_MODE_NORMAL);
            } else if (checkedId == R.id.radio_vibrationMode) {
                setVibrationMode();
                saveMode(AudioManager.RINGER_MODE_VIBRATE);
            }
        });

        return rootView;
    }

    // Phương thức để đặt chế độ âm thanh bình thường
    private void setNormalMode() {
        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }

    // Phương thức để đặt chế độ rung
    private void setVibrationMode() {
        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
    }

    // Phương thức để phát âm thanh khi thay đổi chế độ
    private void playSound(int soundResId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), soundResId);
        mediaPlayer.start();  // Phát âm thanh
    }

    // Phương thức để lưu chế độ đã chọn vào SharedPreferences
    private void saveMode(int mode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(MODE_KEY, mode);
        editor.apply();
    }
}