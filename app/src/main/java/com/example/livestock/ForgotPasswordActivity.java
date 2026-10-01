package com.example.livestock;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Collections;
import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etInput;
    private TextInputLayout layoutInput;
    private MaterialButton btnAction;
    private TextView tvStepTitle, tvStepDesc;
    private DatabaseHelper dbHelper;
    private String currentEmail = "";
    private int step = 1; // 1: Email, 2: Code, 3: New Password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvStepTitle = findViewById(R.id.tv_step_title);
        tvStepDesc = findViewById(R.id.tv_step_desc);
        etInput = findViewById(R.id.et_forgot_input);
        layoutInput = findViewById(R.id.layout_input);
        btnAction = findViewById(R.id.btn_forgot_action);
        View btnBackToLogin = findViewById(R.id.btn_back_to_login);

        btnBackToLogin.setOnClickListener(v -> finish());

        btnAction.setOnClickListener(v -> handleAction());
    }

    private void handleAction() {
        String value = etInput.getText().toString().trim();

        if (step == 1) { // Verify Email
            if (value.isEmpty()) { etInput.setError("Enter email"); return; }
            if (dbHelper.getUserIdByEmail(value) != -1) {
                currentEmail = value;
                String code = String.format("%06d", new Random().nextInt(999999));
                dbHelper.saveResetCode(currentEmail, code);
                NotificationHelper.sendEmailBroadcast(this, Collections.singletonList(currentEmail), 
                        "Livestock Password Reset Code", "Your verification code is: " + code);
                
                step = 2;
                updateUI();
            } else {
                Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
            }
        } else if (step == 2) { // Verify Code
            if (value.isEmpty()) { etInput.setError("Enter code"); return; }
            if (dbHelper.verifyResetCode(currentEmail, value)) {
                step = 3;
                updateUI();
            } else {
                Toast.makeText(this, "Invalid code", Toast.LENGTH_SHORT).show();
            }
        } else if (step == 3) { // Reset Password
            if (value.length() < 4) { etInput.setError("Too short"); return; }
            if (dbHelper.updatePassword(currentEmail, value)) {
                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void updateUI() {
        etInput.setText("");
        if (step == 2) {
            tvStepTitle.setText("Verify Code");
            tvStepDesc.setText("Enter the 6-digit code sent to " + currentEmail);
            layoutInput.setHint("Verification Code");
            btnAction.setText("VERIFY CODE");
        } else if (step == 3) {
            tvStepTitle.setText("New Password");
            tvStepDesc.setText("Set a strong password for your account.");
            layoutInput.setHint("New Password");
            etInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnAction.setText("UPDATE PASSWORD");
        }
    }
}
