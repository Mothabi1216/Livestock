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
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private View graphCard, cardAnnouncement;
    private ImageView imgProfile, navProfilePic;
    private TextView tvAdminName, navAdminName, tvTotalUsers, tvActiveAlerts;
    private DatabaseHelper dbHelper;
    private String selectedImageUri = "";
    private ShapeableImageView dialogProfileImg;
    private TextInputEditText etProfilePicDisplayRef;
    private PieChart reportsChart;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri.toString();
                    try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception e) {}
                    if (dialogProfileImg != null) loadCircularImage(selectedImageUri, dialogProfileImg);
                    if (etProfilePicDisplayRef != null) etProfilePicDisplayRef.setText("Photo Selected ✅");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
                else { setEnabled(false); getOnBackPressedDispatcher().onBackPressed(); }
            }
        });

        imgProfile = findViewById(R.id.admin_profile_pic);
        tvAdminName = findViewById(R.id.tv_admin_name);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvActiveAlerts = findViewById(R.id.tv_active_alerts);
        reportsChart = findViewById(R.id.reports_chart);
        
        View headerView = navigationView.getHeaderView(0);
        navProfilePic = headerView.findViewById(R.id.nav_admin_pic);
        navAdminName = headerView.findViewById(R.id.nav_admin_name);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(Color.WHITE);

        loadAdminSession();
        loadStats();
        setupChart();
        
        graphCard = findViewById(R.id.graph_card);
        cardAnnouncement = findViewById(R.id.card_announcement);
        
        if (cardAnnouncement != null) {
            cardAnnouncement.setOnClickListener(v -> showAnnouncementDialog());
        }

        animateEntrance();
    }

    private void loadStats() {
        if (tvTotalUsers != null) tvTotalUsers.setText(String.valueOf(dbHelper.getTotalUsersCount()));
        if (tvActiveAlerts != null) tvActiveAlerts.setText(String.valueOf(dbHelper.getActiveAlertsCount()));
    }

    private void setupChart() {
        if (reportsChart == null) return;

        Map<String, Integer> data = dbHelper.getReportsByAnimalType();
        List<PieEntry> entries = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        if (entries.isEmpty()) {
            reportsChart.setNoDataText("No reporting data available yet.");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        reportsChart.setData(pieData);
        reportsChart.getDescription().setEnabled(false);
        reportsChart.setCenterText("Reports by Animal");
        reportsChart.setCenterTextColor(Color.WHITE);
        reportsChart.setHoleColor(Color.TRANSPARENT);
        reportsChart.getLegend().setTextColor(Color.WHITE);
        reportsChart.setEntryLabelColor(Color.WHITE);
        reportsChart.animateY(1000);
        reportsChart.invalidate();
    }

    private void loadAdminSession() {
        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String name = pref.getString("userName", "Admin");
        String pic = pref.getString("userProfilePic", "");
        if (tvAdminName != null) tvAdminName.setText(name);
        if (navAdminName != null) navAdminName.setText(name);
        if (imgProfile != null) loadCircularImage(pic, imgProfile);
        if (navProfilePic != null) loadCircularImage(pic, navProfilePic);
    }

    private void loadCircularImage(String uriString, ImageView imageView) {
        Object loadTarget = (uriString == null || uriString.isEmpty()) ? R.drawable.pic2 : Uri.parse(uriString);
        Glide.with(this).load(loadTarget).circleCrop().placeholder(R.drawable.pic2).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
    }

    private void animateEntrance() {
        if (imgProfile != null) { imgProfile.setAlpha(0f); imgProfile.setTranslationY(-30f); imgProfile.animate().alpha(1f).translationY(0).setDuration(800).start(); }
        if (graphCard != null) { graphCard.setAlpha(0f); graphCard.setTranslationY(100f); graphCard.animate().alpha(1f).translationY(0).setDuration(1000).setStartDelay(300).setInterpolator(new OvershootInterpolator()).start(); }
        if (cardAnnouncement != null) { cardAnnouncement.setAlpha(0f); cardAnnouncement.setTranslationY(100f); cardAnnouncement.animate().alpha(1f).translationY(0).setDuration(1000).setStartDelay(500).setInterpolator(new OvershootInterpolator()).start(); }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_logout) showLogoutDialog();
        else if (id == R.id.nav_add_user) showAddUserForm();
        else if (id == R.id.nav_view_users) showUsersTable();
        else if (id == R.id.nav_ratings) showRatingsTable();
        else if (id == R.id.nav_reports) startActivity(new Intent(this, ReportRegistryActivity.class));
        else if (id == R.id.nav_alerts) showAlertMenu();
        else if (id == R.id.nav_audit_logs) showAuditLogs();
        else if (id == R.id.nav_profile) showProfileDialog();
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showAnnouncementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Livestock);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_send_alert, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        TextView titleView = view.findViewById(R.id.tv_dialog_title);
        if (titleView != null) titleView.setText("MAKE SYSTEM ANNOUNCEMENT");

        TextInputEditText etTitle = view.findViewById(R.id.et_alert_title);
        TextInputEditText etMessage = view.findViewById(R.id.et_alert_message);
        
        View diseaseField = view.findViewById(R.id.et_alert_disease);
        View severityField = view.findViewById(R.id.spinner_alert_seriousness);
        View districtField = view.findViewById(R.id.et_alert_district);

        if (diseaseField != null && diseaseField.getParent() != null && diseaseField.getParent().getParent() instanceof TextInputLayout) {
            ((TextInputLayout) diseaseField.getParent().getParent()).setVisibility(View.GONE);
        }
        if (severityField != null && severityField.getParent() != null && severityField.getParent().getParent() instanceof TextInputLayout) {
            ((TextInputLayout) severityField.getParent().getParent()).setVisibility(View.GONE);
        }
        if (districtField != null && districtField.getParent() != null && districtField.getParent().getParent() instanceof TextInputLayout) {
            ((TextInputLayout) districtField.getParent().getParent()).setVisibility(View.GONE);
        }

        view.findViewById(R.id.btn_cancel_alert).setOnClickListener(v -> dialog.dismiss());
        
        MaterialButton btnSend = view.findViewById(R.id.btn_broadcast_alert);
        btnSend.setText("SEND ANNOUNCEMENT");
        btnSend.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String message = etMessage.getText().toString().trim();

            if (title.isEmpty() || message.isEmpty()) { 
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show(); 
                return; 
            }

            List<String> emails = dbHelper.getAllUserEmails();
            EmailHelper.sendAnnouncement(title, message, emails);
            Toast.makeText(this, "Announcement Sent to all users!", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showAlertMenu() {
        String[] options = {"Send New Alert", "View Alert History"};
        new AlertDialog.Builder(this)
                .setTitle("Disease Alerts")
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
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show(); 
                return; 
            }

            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
            int adminId = pref.getInt("userId", -1);
            
            NotificationHelper.broadcastAlert(this, title, message, disease, severity, district, adminId);
            Toast.makeText(this, "Alert Broadcasted & Emails queued!", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showViewAlertsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Livestock);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_view_users, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        ((TextView)view.findViewById(R.id.tv_table_title)).setText("System Alerts History");
        view.findViewById(R.id.btn_back_table).setOnClickListener(v -> dialog.dismiss());
        TableLayout table = view.findViewById(R.id.table_users);
        table.removeAllViews();

        TableRow header = new TableRow(this);
        header.setBackgroundColor(Color.parseColor("#333333"));
        header.addView(createTableTextView("SEVERITY", true));
        header.addView(createTableTextView("TITLE", true));
        header.addView(createTableTextView("DATE", true));
        table.addView(header);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        int uid = pref.getInt("userId", -1);
        
        for (Map<String, String> alert : dbHelper.getAllAlerts(uid)) {
            TableRow row = new TableRow(this);
            row.setPadding(0, 16, 0, 16);
            
            TextView tvLevel = createTableTextView(alert.get("severity"));
            tvLevel.setTextColor(getAlertColor(alert.get("severity")));
            row.addView(tvLevel);
            row.addView(createTableTextView(alert.get("title")));
            row.addView(createTableTextView(alert.get("created_at")));
            
            row.setOnClickListener(v -> {
                dbHelper.markAlertAsRead(uid, Integer.parseInt(alert.get("id")));
                new AlertDialog.Builder(this, R.style.Theme_Livestock)
                        .setTitle(alert.get("title"))
                        .setMessage(alert.get("message") + "\n\nLocation: " + alert.get("district"))
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

    private void showRatingsTable() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Livestock);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_view_users, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        ((TextView)view.findViewById(R.id.tv_table_title)).setText("Vet Service Quality Ratings");
        view.findViewById(R.id.btn_back_table).setOnClickListener(v -> dialog.dismiss());
        TableLayout table = view.findViewById(R.id.table_users);
        table.removeAllViews();

        TableRow header = new TableRow(this);
        header.setBackgroundColor(Color.parseColor("#333333"));
        header.setPadding(0, 20, 0, 20);
        header.addView(createTableTextView("VET NAME", true));
        header.addView(createTableTextView("FARMER", true));
        header.addView(createTableTextView("REPORT ID", true));
        header.addView(createTableTextView("STARS", true));
        header.addView(createTableTextView("DATE", true));
        table.addView(header);

        for (Map<String, String> r : dbHelper.getAllRatings()) {
            TableRow row = new TableRow(this);
            row.setPadding(0, 16, 0, 16); row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(createTableTextView(r.get("vet")));
            row.addView(createTableTextView(r.get("farmer")));
            row.addView(createTableTextView("REP-" + r.get("report_id")));
            TextView starTv = createTableTextView(r.get("stars") + " ⭐");
            starTv.setTextColor(Color.parseColor("#FFD740")); starTv.setTypeface(null, Typeface.BOLD);
            row.addView(starTv);
            row.addView(createTableTextView(r.get("time")));
            table.addView(row);
        }
        dialog.show();
    }

    private void showProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_profile, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialogProfileImg = view.findViewById(R.id.img_profile_edit);
        TextInputEditText etName = view.findViewById(R.id.et_profile_name), etEmail = view.findViewById(R.id.et_profile_email), etPhone = view.findViewById(R.id.et_profile_phone);
        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        etName.setText(pref.getString("userName", "")); etEmail.setText(pref.getString("userEmail", "")); etPhone.setText(pref.getString("userPhone", ""));
        selectedImageUri = pref.getString("userProfilePic", ""); loadCircularImage(selectedImageUri, dialogProfileImg);
        view.findViewById(R.id.btn_back_profile).setOnClickListener(v -> dialog.dismiss());
        dialogProfileImg.setOnClickListener(v -> { etProfilePicDisplayRef = null; pickMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()); });
        view.findViewById(R.id.btn_update_profile).setOnClickListener(v -> {
            if (dbHelper.updateUserProfile(etEmail.getText().toString(), etName.getText().toString().trim(), etPhone.getText().toString().trim(), selectedImageUri)) {
                SharedPreferences.Editor ed = pref.edit(); ed.putString("userName", etName.getText().toString()); ed.putString("userPhone", etPhone.getText().toString()); ed.putString("userProfilePic", selectedImageUri); ed.apply();
                loadAdminSession(); Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show(); dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showUsersTable() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Livestock);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_view_users, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        view.findViewById(R.id.btn_back_table).setOnClickListener(v -> dialog.dismiss());
        TableLayout table = view.findViewById(R.id.table_users);
        refreshUsersTable(table);
        dialog.show();
    }

    private void refreshUsersTable(TableLayout table) {
        table.removeAllViews();
        TableRow header = new TableRow(this); header.setBackgroundColor(Color.parseColor("#333333")); header.setPadding(0, 20, 0, 20);
        header.addView(createTableTextView("PIC", true)); header.addView(createTableTextView("NAME", true));
        header.addView(createTableTextView("EMAIL", true)); header.addView(createTableTextView("PHONE", true));
        header.addView(createTableTextView("ROLE", true)); header.addView(createTableTextView("STATUS", true));
        header.addView(createTableTextView("ACTION", true)); table.addView(header);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        for (Map<String, String> u : dbHelper.getAllUsersExcept(pref.getString("userEmail", ""))) {
            TableRow row = new TableRow(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0, 12, 0, 12);
            ShapeableImageView pic = new ShapeableImageView(this); TableRow.LayoutParams lp = new TableRow.LayoutParams(80, 80); lp.setMargins(16, 16, 16, 16); pic.setLayoutParams(lp); pic.setShapeAppearanceModel(pic.getShapeAppearanceModel().toBuilder().setAllCornerSizes(40).build());
            Glide.with(this).load(u.get("pic") != null && !u.get("pic").isEmpty() ? Uri.parse(u.get("pic")) : R.drawable.pic2).circleCrop().into(pic);
            row.addView(pic); row.addView(createTableTextView(u.get("name"))); row.addView(createTableTextView(u.get("email"))); row.addView(createTableTextView(u.get("phone"))); row.addView(createTableTextView(u.get("role")));
            TextView status = createTableTextView(u.get("status")); status.setTextColor(u.get("status").equals("Active") ? Color.parseColor("#81C784") : Color.parseColor("#EF9A9A")); row.addView(status);
            MaterialButton btn = new MaterialButton(this); int active = Integer.parseInt(u.get("is_active")); btn.setText(active == 1 ? "DEACTIVATE" : "ACTIVATE"); btn.setBackgroundColor(active == 1 ? Color.parseColor("#FF5252") : Color.parseColor("#4CAF50")); btn.setTextSize(10); btn.setCornerRadius(10); row.addView(btn);
            btn.setOnClickListener(v -> { if (dbHelper.toggleUserStatus(Integer.parseInt(u.get("id")), active)) refreshUsersTable(table); });
            table.addView(row);
        }
    }

    private void showAuditLogs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Livestock);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_audit_logs, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        view.findViewById(R.id.btn_back_audit).setOnClickListener(v -> dialog.dismiss());
        TableLayout table = view.findViewById(R.id.table_audit);
        for (Map<String, String> log : dbHelper.getAuditLogs()) {
            TableRow row = new TableRow(this); row.setPadding(0, 12, 0, 12);
            row.addView(createTableTextView(log.get("name"))); row.addView(createTableTextView(log.get("time"))); row.addView(createTableTextView(log.get("action")));
            table.addView(row);
        }
        dialog.show();
    }

    private TextView createTableTextView(String t) { return createTableTextView(t, false); }
    private TextView createTableTextView(String t, boolean head) {
        TextView tv = new TextView(this); tv.setText(t != null ? t : "N/A"); tv.setPadding(16, 32, 16, 32); tv.setTextColor(Color.WHITE); tv.setTextSize(head ? 14 : 13);
        if (head) tv.setTypeface(null, Typeface.BOLD); return tv;
    }

    private void showAddUserForm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Livestock);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        etProfilePicDisplayRef = view.findViewById(R.id.et_profile_pic_display);
        view.findViewById(R.id.btn_back).setOnClickListener(v -> dialog.dismiss());
        if (etProfilePicDisplayRef != null) etProfilePicDisplayRef.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        view.findViewById(R.id.btn_save_user).setOnClickListener(v -> {
            String name = ((TextInputEditText)view.findViewById(R.id.et_full_name)).getText().toString().trim(), email = ((TextInputEditText)view.findViewById(R.id.et_email)).getText().toString().trim(), pwd = ((TextInputEditText)view.findViewById(R.id.et_password)).getText().toString().trim();
            if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) Toast.makeText(this, "Required fields missing", Toast.LENGTH_SHORT).show();
            else if (dbHelper.addUser(name, email, "", pwd, 2, selectedImageUri)) { Toast.makeText(this, "Vet saved!", Toast.LENGTH_LONG).show(); dialog.dismiss(); }
        });
        dialog.show();
    }

    private void showLogoutDialog() { new AlertDialog.Builder(this).setTitle("Logout").setMessage("Are you sure?").setPositiveButton("Logout", (d, w) -> { getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply(); finish(); }).setNegativeButton("Cancel", null).show(); }
}
