package com.example.livestock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private View glassContainer, layoutEmail, layoutPassword, cbRemember, btnSubmit, tvTitle, tvGotoRegister, tvForgotPassword, btnReturnHome;
    private EditText etEmail, etPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        glassContainer = findViewById(R.id.glass_container);
        tvTitle = findViewById(R.id.login_title);
        layoutEmail = findViewById(R.id.layout_email);
        layoutPassword = findViewById(R.id.layout_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        cbRemember = findViewById(R.id.cb_remember);
        btnSubmit = findViewById(R.id.btn_login_submit);
        tvGotoRegister = findViewById(R.id.tv_goto_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnReturnHome = findViewById(R.id.btn_return_home);

        prepareEntrance();
        glassContainer.post(this::startEntranceAnimation);

        btnReturnHome.setOnClickListener(v -> finish());
        tvGotoRegister.setOnClickListener(v -> {
            glassContainer.animate().translationX(-1500f).alpha(0f).setDuration(500).withEndAction(() -> {
                startActivity(new Intent(this, RegisterActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }).start();
        });

        btnSubmit.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                performLogin();
            }).start();
        });

        // Now redirecting to the actual ForgotPasswordActivity instead of a dialog
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) { Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show(); return; }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("Users", null, "email = ? AND password_hash = ?", new String[]{email, password}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            if (cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                int roleId = cursor.getInt(cursor.getColumnIndexOrThrow("role_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("full_names"));
                
                SharedPreferences.Editor editor = getSharedPreferences("UserSession", MODE_PRIVATE).edit();
                editor.putInt("userId", id);
                editor.putString("userName", name);
                editor.putString("userEmail", email);
                editor.putString("userPhone", cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                editor.putString("userProfilePic", cursor.getString(cursor.getColumnIndexOrThrow("profile_pic")));
                editor.putInt("roleId", roleId);
                editor.apply();

                Toast.makeText(this, "Welcome back, " + name, Toast.LENGTH_SHORT).show();
                if (roleId == 1) startActivity(new Intent(this, FarmerDashboardActivity.class));
                else if (roleId == 2) startActivity(new Intent(this, VetDashboardActivity.class));
                else if (roleId == 3) startActivity(new Intent(this, AdminDashboardActivity.class));
                finish();
            } else Toast.makeText(this, "Account is inactive.", Toast.LENGTH_LONG).show();
            cursor.close();
        } else Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
    }

    private void prepareEntrance() {
        glassContainer.setTranslationX(500f); glassContainer.setTranslationY(-500f); glassContainer.setScaleX(0.8f); glassContainer.setScaleY(0.8f); glassContainer.setAlpha(0f);
        View[] fields = {tvTitle, layoutEmail, layoutPassword, findViewById(R.id.login_options), btnSubmit, tvGotoRegister, btnReturnHome};
        for (View v : fields) { v.setAlpha(0f); v.setTranslationY(50f); }
    }

    private void startEntranceAnimation() {
        glassContainer.animate().translationX(0).translationY(0).scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(800).setInterpolator(new OvershootInterpolator()).start();
        long delay = 400;
        View[] fields = {tvTitle, layoutEmail, layoutPassword, findViewById(R.id.login_options)};
        for (View v : fields) { v.animate().alpha(1.0f).translationY(0).setDuration(500).setStartDelay(delay).start(); delay += 150; }
        btnSubmit.animate().alpha(1.0f).translationY(0).setDuration(500).setStartDelay(delay).start();
        tvGotoRegister.animate().alpha(1.0f).translationY(0).setDuration(500).setStartDelay(delay + 150).start();
        btnReturnHome.animate().alpha(1.0f).translationY(0).setDuration(500).setStartDelay(delay + 300).start();
    }
}
