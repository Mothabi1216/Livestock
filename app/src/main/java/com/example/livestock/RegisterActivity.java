package com.example.livestock;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

public class RegisterActivity extends AppCompatActivity {

    private View glassContainer, tvTitle, layoutProfilePic;
    private EditText etFullName, etEmail, etPhone, etPassword;
    private ImageView imgProfilePreview;
    private Uri selectedImageUri;
    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Glide.with(this).load(selectedImageUri).centerCrop().into(imgProfilePreview);
                    imgProfilePreview.setPadding(0, 0, 0, 0);
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) openGallery();
                else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dbHelper = new DatabaseHelper(this);

        glassContainer = findViewById(R.id.glass_container);
        tvTitle = findViewById(R.id.register_title);
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        layoutProfilePic = findViewById(R.id.layout_profile_pic);
        imgProfilePreview = findViewById(R.id.img_profile_preview);
        View btnSubmit = findViewById(R.id.btn_register_submit);
        View btnReturnHome = findViewById(R.id.btn_return_home);

        prepareEntrance();
        glassContainer.post(this::startEntranceAnimation);

        layoutProfilePic.setOnClickListener(v -> checkPermissions());
        btnReturnHome.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> registerUser());
    }

    private void checkPermissions() {
        String perm = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ? 
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissionLauncher.launch(perm);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void registerUser() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String pwd = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "Name, Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use the dbHelper.addUser method instead of manual insert to ensure consistency (audit logs, etc.)
        String profilePic = (selectedImageUri != null) ? selectedImageUri.toString() : "";
        if (dbHelper.addUser(name, email, phone, pwd, 1, profilePic)) {
            Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Email might already exist.", Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareEntrance() {
        glassContainer.setTranslationX(-500f);
        glassContainer.setTranslationY(-500f);
        glassContainer.setScaleX(0.8f);
        glassContainer.setScaleY(0.8f);
        glassContainer.setAlpha(0f);
    }

    private void startEntranceAnimation() {
        glassContainer.animate().translationX(0).translationY(0).scaleX(1.0f).scaleY(1.0f).alpha(1.0f)
                .setDuration(800).setInterpolator(new OvershootInterpolator()).start();
    }
}
