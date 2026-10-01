package com.example.livestock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Map;

public class VetDashboardActivity extends AppCompatActivity {

    private View profileSection, cardNew, cardMy, cardAlerts, cardSchedule, cardProfile, btnLogout;
    private ShapeableImageView profileImage, dialogProfileImg;
    private TextView tvVetName, tvWelcome;
    private DatabaseHelper dbHelper;
    private String selectedImageUri = "";
    private int userId;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri.toString();
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) { e.printStackTrace(); }
                    
                    if (dialogProfileImg != null) {
                        loadCircularImage(selectedImageUri, dialogProfileImg);
                    }
                    Toast.makeText(this, "Photo Selected!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vet_dashboard);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = sharedPref.getInt("userId", -1);

        profileSection = findViewById(R.id.profile_section);
        profileImage = findViewById(R.id.profile_image);
        tvVetName = findViewById(R.id.tv_vet_name_display);
        tvWelcome = findViewById(R.id.welcome_msg);

        cardNew = findViewById(R.id.card_new_reports);
        cardMy = findViewById(R.id.card_my_reports);
        cardAlerts = findViewById(R.id.card_alerts);
        cardSchedule = findViewById(R.id.card_schedule);
        cardProfile = findViewById(R.id.card_profile);
        btnLogout = findViewById(R.id.btn_logout);

        loadVetSession();
        prepareEntrance();
        if (profileSection != null) profileSection.post(this::startEntranceAnimation);
        setupListeners();
    }

    private void loadVetSession() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String name = sharedPref.getString("userName", "Vet Officer");
        String pic = sharedPref.getString("userProfilePic", "");
        if (tvVetName != null) tvVetName.setText(name);
        if (tvWelcome != null) tvWelcome.setText("Welcome, Dr. " + name + " 👋");
        if (profileImage != null) loadCircularImage(pic, profileImage);
    }

    private void loadCircularImage(String uriString, ImageView imageView) {
        Object target = (uriString == null || uriString.isEmpty()) ? R.drawable.pic2 : Uri.parse(uriString);
        Glide.with(this).load(target)
                .circleCrop()
                .placeholder(R.drawable.pic2)
                .error(R.drawable.pic2)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    private void prepareEntrance() {
        if (profileSection != null) {
            profileSection.setAlpha(0f);
            profileSection.setTranslationY(-50f);
        }
        View[] components = {cardNew, cardMy, cardAlerts, cardSchedule, cardProfile, btnLogout};
        for (View c : components) { 
            if (c != null) { 
                c.setAlpha(0f); 
                c.setTranslationY(100f); 
                c.setScaleX(0.9f); 
                c.setScaleY(0.9f); 
            } 
        }
    }

    private void startEntranceAnimation() {
        if (profileSection != null) profileSection.animate().alpha(1f).translationY(0).setDuration(600).start();
        long delay = 200;
        View[] components = {cardNew, cardMy, cardAlerts, cardSchedule, cardProfile, btnLogout};
        for (View c : components) {
            if (c != null) {
                c.animate().alpha(1f).translationY(0).scaleX(1f).scaleY(1f).setDuration(500).setStartDelay(delay).setInterpolator(new OvershootInterpolator()).start();
                delay += 100;
            }
        }
    }

    private void setupListeners() {
        if (cardNew != null) cardNew.setOnClickListener(v -> { 
            animateClick(v); 
            Intent intent = new Intent(this, ReportRegistryActivity.class);
            startActivity(intent);
        });

        if (cardMy != null) cardMy.setOnClickListener(v -> {
            animateClick(v);
            Intent intent = new Intent(this, PreviousReportsActivity.class);
            startActivity(intent);
        });
        
        if (cardSchedule != null) cardSchedule.setOnClickListener(v -> {
            animateClick(v);
            Intent intent = new Intent(this, VetScheduleActivity.class);
            startActivity(intent);
        });

        if (cardProfile != null) cardProfile.setOnClickListener(v -> { animateClick(v); showProfileDialog(); });
        
        if (btnLogout != null) btnLogout.setOnClickListener(v -> { 
            animateClick(v); 
            getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
            finish();
        });

        if (cardAlerts != null) {
            cardAlerts.setOnClickListener(view -> {
                animateClick(view);
                showAlertMenu();
            });
        }
    }

    private void showAlertMenu() {
        String[] options = {"Send New Alert", "View Alerts History"};
        new AlertDialog.Builder(this)
                .setTitle("Outbreak Alerts")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showSendAlertDialog();
                    else showViewAlertsDialog();
                }).show();
    }

    private void showSendAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Livestock);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_send_alert, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        TextInputEditText etTitle = view.findViewById(R.id.et_alert_title);
        TextInputEditText etDisease = view.findViewById(R.id.et_alert_disease);
        TextInputEditText etDistrict = view.findViewById(R.id.et_alert_district);
        TextInputEditText etMessage = view.findViewById(R.id.et_alert_message);
        AutoCompleteTextView spinnerSeriousness = view.findViewById(R.id.spinner_alert_seriousness);
        
        String[] levels = {"Low", "Moderate", "High", "Critical"};
        spinnerSeriousness.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, levels));

        view.findViewById(R.id.btn_cancel_alert).setOnClickListener(v -> dialog.dismiss());
        
        view.findViewById(R.id.btn_broadcast_alert).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String disease = etDisease.getText().toString().trim();
            String district = etDistrict.getText().toString().trim();
            String message = etMessage.getText().toString().trim();
            String severity = spinnerSeriousness.getText().toString();

            if (title.isEmpty() || message.isEmpty() || severity.isEmpty()) { 
                Toast.makeText(this, "Fields missing!", Toast.LENGTH_SHORT).show(); 
                return; 
            }

            NotificationHelper.broadcastAlert(this, title, message, disease, severity, district, userId);
            Toast.makeText(this, "Alert Broadcasted & Emails Sent!", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showViewAlertsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Livestock);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_view_users, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        ((TextView)view.findViewById(R.id.tv_table_title)).setText("Global Outbreak Alerts");
        view.findViewById(R.id.btn_back_table).setOnClickListener(v -> dialog.dismiss());
        TableLayout table = view.findViewById(R.id.table_users);
        table.removeAllViews();

        TableRow header = new TableRow(this);
        header.setBackgroundColor(Color.parseColor("#333333"));
        header.addView(createTableHeaderCell("SEVERITY"));
        header.addView(createTableHeaderCell("TITLE"));
        header.addView(createTableHeaderCell("SENT ON"));
        table.addView(header);

        List<Map<String, String>> alerts = dbHelper.getAllAlerts(userId);
        for (Map<String, String> alert : alerts) {
            TableRow row = new TableRow(this);
            row.setPadding(0, 16, 0, 16);
            
            TextView tvLevel = createTableCell(alert.get("severity"));
            tvLevel.setTextColor(getAlertColor(alert.get("severity")));
            tvLevel.setTypeface(null, Typeface.BOLD);
            
            row.addView(tvLevel);
            row.addView(createTableCell(alert.get("title")));
            row.addView(createTableCell(alert.get("created_at")));

            row.setOnClickListener(v -> {
                dbHelper.markAlertAsRead(userId, Integer.parseInt(alert.get("id")));
                new AlertDialog.Builder(this, R.style.Theme_Livestock)
                        .setTitle(alert.get("title"))
                        .setMessage(alert.get("message") + "\n\nLocation: " + alert.get("district") + "\nBy: " + alert.get("author"))
                        .setPositiveButton("OK", null).show();
            });
            table.addView(row);
        }
        dialog.show();
    }

    private int getAlertColor(String level) {
        if (level == null) return Color.WHITE;
        if (level.equalsIgnoreCase("Critical")) return Color.parseColor("#FF1744");
        if (level.equalsIgnoreCase("High")) return Color.parseColor("#FF5252");
        if (level.equalsIgnoreCase("Moderate")) return Color.parseColor("#FFD740");
        return Color.parseColor("#81C784");
    }

    private TextView createTableHeaderCell(String text) {
        TextView tv = createTableCell(text);
        tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private TextView createTableCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text != null ? text : "---");
        tv.setTextColor(Color.WHITE);
        tv.setPadding(24, 32, 24, 32);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(13);
        return tv;
    }

    private void showProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View view = getLayoutInflater().inflate(R.layout.dialog_profile, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        dialogProfileImg = view.findViewById(R.id.img_profile_edit);
        TextInputEditText etName = view.findViewById(R.id.et_profile_name);
        TextInputEditText etEmail = view.findViewById(R.id.et_profile_email);
        TextInputEditText etPhone = view.findViewById(R.id.et_profile_phone);
        TextInputEditText etRole = view.findViewById(R.id.et_profile_role);
        MaterialButton btnUpdate = view.findViewById(R.id.btn_update_profile);

        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        etName.setText(pref.getString("userName", ""));
        etEmail.setText(pref.getString("userEmail", ""));
        etPhone.setText(pref.getString("userPhone", ""));
        etRole.setText("Veterinary Officer");
        selectedImageUri = pref.getString("userProfilePic", "");

        loadCircularImage(selectedImageUri, dialogProfileImg);

        view.findViewById(R.id.btn_back_profile).setOnClickListener(v -> dialog.dismiss());
        
        dialogProfileImg.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        btnUpdate.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString();

            if (newName.isEmpty()) {
                etName.setError("Name is required");
                return;
            }

            if (dbHelper.updateUserProfile(email, newName, newPhone, selectedImageUri)) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("userName", newName);
                editor.putString("userPhone", newPhone);
                editor.putString("userProfilePic", selectedImageUri);
                editor.apply();
                
                loadVetSession();
                Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void animateClick(View v) { if (v != null) v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()).start(); }
}
