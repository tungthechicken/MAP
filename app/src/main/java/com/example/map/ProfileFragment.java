package com.example.map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private TextView usernameTextView, nameAPI, emailAPI;
    private EditText editname;
    private RetrofitInterface retrofitInterface;
    private ImageView avatarImageView, imagebutton;
    private static final String PREFS_NAME = "prefs";
    private static final String PREF_AVATAR = "avatar";
    private static final String PREF_AVATAR_BITMAP = "avatar_bitmap";
    private final int[] avatarIds = {
            R.drawable.avt1, R.drawable.avt2, R.drawable.avt3, R.drawable.avt4,
            R.drawable.avt5, R.drawable.avt6, R.drawable.avt7, R.drawable.avt8
    };
    private int selectedAvatarId = R.drawable.baseline_person_24;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageButton backButton = view.findViewById(R.id.back_button);
        Button btEdit = view.findViewById(R.id.btEdit);
        Button btSave = view.findViewById(R.id.btSave);
        Button btExit = view.findViewById(R.id.btExit);
        FrameLayout photoedit = view.findViewById(R.id.photo_edit);
        avatarImageView = view.findViewById(R.id.avatarImageView);
        imagebutton = view.findViewById(R.id.image_button);
        editname = view.findViewById(R.id.editText);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        nameAPI = view.findViewById(R.id.nameAPI);
        emailAPI = view.findViewById(R.id.emailAPI);

        // Hide edit elements initially
        editname.setVisibility(View.GONE);
        btSave.setVisibility(View.GONE);
        btExit.setVisibility(View.GONE);
        imagebutton.setVisibility(View.GONE);

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        selectedAvatarId = prefs.getInt(PREF_AVATAR, R.drawable.baseline_person_24);
        String avatarBitmapBase64 = prefs.getString(PREF_AVATAR_BITMAP, null);
        // Hiển thị avatar đã lưu
        if (avatarBitmapBase64 != null) {
            Bitmap bitmap = base64ToBitmap(avatarBitmapBase64);
            updatePhotoButton(bitmap);
        } else {
            avatarImageView.setImageResource(selectedAvatarId);
        }

        // Hiển thị dialog chọn avatar khi bấm vào FrameLayout
        photoedit.setOnClickListener(v -> showAvatarSelectionDialog());

        // Lấy dữ liệu từ Bundle và thiết lập vào các TextView
        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.getString("name");
            String email = bundle.getString("email");

            if (name != null) {
                usernameTextView.setText(name);
                nameAPI.setText(name);
                editname.setText(name);
            }

            if (email != null) {
                emailAPI.setText(email);
            }
        }
        // Nút Save để lưu avatar và username
        btSave.setOnClickListener(v -> {
            saveAvatarSelection();  // Lưu avatar
            saveUserName();    // Lưu username
            // Khôi phục lại trạng thái ban đầu
            editname.setVisibility(View.GONE);
            btSave.setVisibility(View.GONE);
            btExit.setVisibility(View.GONE);
            imagebutton.setVisibility(View.GONE);

            // Hiển thị lại các phần tử không thay đổi (nếu có)
            btEdit.setVisibility(View.VISIBLE);
            nameAPI.setVisibility(View.VISIBLE);
        });
        btExit.setOnClickListener(v -> {
            // Khôi phục lại trạng thái ban đầu
            editname.setVisibility(View.GONE);
            btSave.setVisibility(View.GONE);
            btExit.setVisibility(View.GONE);
            imagebutton.setVisibility(View.GONE);

            // Hiển thị lại các phần tử không thay đổi (nếu có)
            btEdit.setVisibility(View.VISIBLE);
            nameAPI.setVisibility(View.VISIBLE);
        });

// Khởi tạo Retrofit
        String baseUrl = requireContext().getString(R.string.retrofit_url);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);

        // Gọi API để lấy thông tin người dùng
        getUserData();
        // Set sự kiện cho nút back để quay lại setting
        backButton.setOnClickListener(v -> {
            // Quay lại trang ProfileFragment
            getParentFragmentManager().popBackStack(); // Quay lại fragment trước đó trong back stack
        });
        btEdit.setOnClickListener(v -> {
            // Show edit fields
            imagebutton.setVisibility(View.VISIBLE);
            editname.setVisibility(View.VISIBLE);
            btSave.setVisibility(View.VISIBLE);
            btExit.setVisibility(View.VISIBLE);

            // Hide Edit button and name
            btEdit.setVisibility(View.GONE);
            nameAPI.setVisibility(View.GONE);
        });
        return view;
    }

    private void saveUserName() {
        // Lấy tên người dùng từ EditText
        String newUsername = editname.getText().toString().trim();

        // Kiểm tra xem tên có trống không
        if (newUsername.isEmpty()) {
            return;  // Không làm gì nếu tên trống
        }

        // Lấy tên hiện tại từ TextView
        String currentUsername = nameAPI.getText().toString().trim();

        // Kiểm tra xem tên có thay đổi hay không
        if (newUsername.equals(currentUsername)) {
            return;
        }

        // Lấy email từ SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String email = prefs.getString("email", null);

        if (email == null) {
            Toast.makeText(requireContext(), "Email not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API để cập nhật tên nếu tên đã thay đổi
        updateUserNameOnServer(newUsername, email);
    }


    private void showAvatarSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_avt, null);
        builder.setView(dialogView);

        GridView gridViewAvatars = dialogView.findViewById(R.id.gridViewAvatars);
        AvatarAdapter adapter = new AvatarAdapter(requireContext(), avatarIds);
        gridViewAvatars.setAdapter(adapter);

        AlertDialog dialog = builder.create();
        gridViewAvatars.setOnItemClickListener((parent, view, position, id) -> {
            selectedAvatarId = avatarIds[position];
            Bitmap avatarBitmap = BitmapFactory.decodeResource(getResources(), selectedAvatarId);
            updatePhotoButton(avatarBitmap);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveAvatarSelection() {
        // Lưu avatar đã chọn vào SharedPreferences
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), selectedAvatarId);
        String base64Bitmap = bitmapToBase64(bitmap);

        editor.putInt(PREF_AVATAR, selectedAvatarId);
        editor.putString(PREF_AVATAR_BITMAP, base64Bitmap);
        editor.apply();
    }

    private void updatePhotoButton(Bitmap bitmap) {
        Bitmap roundedBitmap = getRoundedCroppedBitmap(bitmap);
        avatarImageView.setImageBitmap(roundedBitmap);
    }

    private Bitmap getRoundedCroppedBitmap(Bitmap bitmap) {
        int width = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, width);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(width / 2f, width / 2f, width / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }


    private void getUserData() {
        // Giả sử bạn có email của người dùng từ SharedPreferences hoặc Bundle
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String email = prefs.getString("email", null);

        if (email != null) {
            Call<UserData> call = retrofitInterface.getUserByEmail(email);
            call.enqueue(new Callback<UserData>() {
                @Override
                public void onResponse(Call<UserData> call, Response<UserData> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserData userData = response.body();
                        usernameTextView.setText(userData.getName());
                        nameAPI.setText(userData.getName());
                        emailAPI.setText(userData.getEmail());
                    } else {
                        Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserData> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(requireContext(), "Email not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserNameOnServer(String newName, String email) {
        // Create a HashMap to hold the data
        HashMap<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("newName", newName);

        // Use the retrofitInterface object to call the updateUserData method
        Call<Void> call = retrofitInterface.updateUserData(userData);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Username updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    // Log the response code and message from the API
                    int statusCode = response.code();
                    String errorMessage = response.message();
                    Log.e(TAG, "Failed to update username. Status code: " + statusCode + ", Message: " + errorMessage);
                    Toast.makeText(requireContext(), "Failed to update username. Status code: " + statusCode + ", Message: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error updating username: " + t.getMessage(), t);
                Toast.makeText(requireContext(), "Error updating username: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
