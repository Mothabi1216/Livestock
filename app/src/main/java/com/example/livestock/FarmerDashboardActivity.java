package com.example.livestock;

import android.app.DatePickerDialog;
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
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FarmerDashboardActivity extends AppCompatActivity {

    private View headerSection, cardReport, cardMyReports, cardAlerts, cardRateVet, cardProfile, btnLogout;
    private TextView tvWelcomeMsg, tvFarmerName, tvAlertBadge;
    private ImageView imgProfile;
    private DatabaseHelper dbHelper;
    private String selectedImageUri = "";
    private ShapeableImageView dialogProfileImg;
    private int userId;

    private double capturedLat = 0.0;
    private double capturedLng = 0.0;
    private TextInputEditText etLat, etLng;

    private final List<String> reportImageUris = new ArrayList<>();
    private ShapeableImageView[] reportImageViews = new ShapeableImageView[3];

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri.toString();
                    try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception e) {}
                    if (dialogProfileImg != null) loadCircularImage(selectedImageUri, dialogProfileImg);
                }
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickReportImages =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception e) {}
                    if (reportImageUris.size() < 3) { reportImageUris.add(uri.toString()); updateReportImageUI(); }
                }
            });

    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    capturedLat = result.getData().getDoubleExtra("lat", 0.0);
                    capturedLng = result.getData().getDoubleExtra("lng", 0.0);
                    if (etLat != null) etLat.setText(String.valueOf(capturedLat));
                    if (etLng != null) etLng.setText(String.valueOf(capturedLng));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);
        dbHelper = new DatabaseHelper(this);

        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = pref.getInt("userId", -1);

        headerSection = findViewById(R.id.header_section);
        tvWelcomeMsg = findViewById(R.id.welcome_msg);
        tvFarmerName = findViewById(R.id.tv_farmer_name);
        imgProfile = findViewById(R.id.img_farmer_profile);
        cardReport = findViewById(R.id.card_report);
        cardMyReports = findViewById(R.id.card_my_reports);
        cardAlerts = findViewById(R.id.card_alerts);
        cardRateVet = findViewById(R.id.card_rate_vet);
        cardProfile = findViewById(R.id.card_profile);
        btnLogout = findViewById(R.id.btn_logout);
        tvAlertBadge = findViewById(R.id.tv_alert_badge);

        loadSessionData();
        prepareAnimations();
        headerSection.post(this::startEntranceAnimation);
        setupClickListeners();
        updateAlertBadge();
    }

    private void loadSessionData() {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String name = sharedPref.getString("userName", "Farmer");
        String profilePicUri = sharedPref.getString("userProfilePic", "");
        if (tvWelcomeMsg != null) tvWelcomeMsg.setText("Welcome, " + name + " 👋");
        if (tvFarmerName != null) tvFarmerName.setText(name);
        if (imgProfile != null) loadCircularImage(profilePicUri, imgProfile);
    }

    private void updateAlertBadge() {
        int count = dbHelper.getUnreadAlertCount(userId);
        if (tvAlertBadge != null) {
            if (count > 0) {
                tvAlertBadge.setText(String.valueOf(count));
                tvAlertBadge.setVisibility(View.VISIBLE);
            } else {
                tvAlertBadge.setVisibility(View.GONE);
            }
        }
    }

    private void loadCircularImage(String uriString, ImageView imageView) {
        Object loadTarget = (uriString == null || uriString.isEmpty()) ? R.drawable.pic2 : Uri.parse(uriString);
        Glide.with(this).load(loadTarget).circleCrop().placeholder(R.drawable.pic2).into(imageView);
    }

    private void updateReportImageUI() {
        for (int i = 0; i < 3; i++) {
            if (i < reportImageUris.size()) {
                reportImageViews[i].setVisibility(View.VISIBLE);
                Glide.with(this).load(Uri.parse(reportImageUris.get(i))).centerCrop().into(reportImageViews[i]);
            } else if (i > 0) reportImageViews[i].setVisibility(View.GONE);
        }
    }

    private void prepareAnimations() {
        headerSection.setAlpha(0f); headerSection.setTranslationY(-50f);
        View[] cards = {imgProfile, tvFarmerName, cardReport, cardMyReports, cardAlerts, cardRateVet, cardProfile, btnLogout};
        for (View card : cards) if (card != null) { card.setAlpha(0f); card.setTranslationY(100f); card.setScaleX(0.9f); card.setScaleY(0.9f); }
    }

    private void startEntranceAnimation() {
        headerSection.animate().alpha(1f).translationY(0).setDuration(600).start();
        long delay = 200;
        View[] cards = {imgProfile, tvFarmerName, cardReport, cardMyReports, cardAlerts, cardRateVet, cardProfile, btnLogout};
        for (View card : cards) if (card != null) { card.animate().alpha(1f).translationY(0).scaleX(1f).scaleY(1f).setDuration(600).setStartDelay(delay).setInterpolator(new OvershootInterpolator()).start(); delay += 150; }
    }

    private void setupClickListeners() {
        cardReport.setOnClickListener(v -> { animateClick(v); showReportDialog(); });
        cardMyReports.setOnClickListener(v -> { animateClick(v); showMyReportsDialog(); });
        cardAlerts.setOnClickListener(v -> { animateClick(v); showViewAlertsDialog(); });
        cardRateVet.setOnClickListener(v -> { animateClick(v); showRateVetDialog(); });
        cardProfile.setOnClickListener(v -> { animateClick(v); showProfileDialog(); });
        if (btnLogout != null) btnLogout.setOnClickListener(v -> { animateClick(v); getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply(); finish(); });
    }

    private void showViewAlertsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Livestock);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_view_users, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        ((TextView)view.findViewById(R.id.tv_table_title)).setText("Global Notifications & Alerts");
        view.findViewById(R.id.btn_back_table).setOnClickListener(v -> dialog.dismiss());
        TableLayout table = view.findViewById(R.id.table_users);
        table.removeAllViews();

        TableRow header = new TableRow(this);
        header.setBackgroundColor(Color.parseColor("#333333"));
        header.addView(createTableHeaderCell("SEVERITY"));
        header.addView(createTableHeaderCell("TITLE"));
        header.addView(createTableHeaderCell("DISTRICT"));
        header.addView(createTableHeaderCell("DATE"));
        table.addView(header);

        for (Map<String, String> alert : dbHelper.getAllAlerts(userId)) {
            TableRow row = new TableRow(this);
            row.setPadding(0, 16, 0, 16);
            
            boolean isRead = "1".equals(alert.get("is_read"));
            if (!isRead) row.setBackgroundColor(Color.parseColor("#1AFFFFFF"));

            TextView tvLevel = createTableCell(alert.get("severity"));
            tvLevel.setTextColor(getAlertColor(alert.get("severity")));
            tvLevel.setTypeface(null, Typeface.BOLD);
            
            row.addView(tvLevel);
            row.addView(createTableCell(alert.get("title")));
            row.addView(createTableCell(alert.get("district")));
            row.addView(createTableCell(alert.get("created_at")));
            
            row.setOnClickListener(v -> {
                dbHelper.markAlertAsRead(userId, Integer.parseInt(alert.get("id")));
                showAlertDialog(alert);
                dialog.dismiss();
                updateAlertBadge();
            });

            table.addView(row);
        }
        dialog.show();
    }

    private void showAlertDialog(Map<String, String> alert) {
        new AlertDialog.Builder(this, R.style.Theme_Livestock)
                .setTitle(alert.get("title"))
                .setMessage("Disease: " + alert.get("disease") + "\n" +
                            "Severity: " + alert.get("severity") + "\n" +
                            "Location: " + alert.get("district") + "\n\n" +
                            alert.get("message") + "\n\n" +
                            "By: " + alert.get("author") + " (" + alert.get("created_at") + ")")
                .setPositiveButton("OK", null)
                .show();
    }

    private int getAlertColor(String level) {
        if (level == null) return Color.WHITE;
        if (level.equalsIgnoreCase("Critical")) return Color.parseColor("#FF1744");
        if (level.equalsIgnoreCase("High")) return Color.parseColor("#FF5252");
        if (level.equalsIgnoreCase("Moderate")) return Color.parseColor("#FFD740");
        return Color.parseColor("#81C784");
    }

    private void showMyReportsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_my_reports, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        TableLayout table = view.findViewById(R.id.table_my_reports);
        view.findViewById(R.id.btn_back_my_reports).setOnClickListener(v -> dialog.dismiss());
        refreshMyReportsTable(table);
        dialog.show();
    }

    private void refreshMyReportsTable(TableLayout table) {
        table.removeAllViews();
        TableRow header = new TableRow(this); header.setBackgroundColor(Color.parseColor("#333333"));
        header.addView(createTableHeaderCell("ID")); header.addView(createTableHeaderCell("ANIMAL"));
        header.addView(createTableHeaderCell("DATE")); header.addView(createTableHeaderCell("DISEASE"));
        header.addView(createTableHeaderCell("VISIT DATE")); header.addView(createTableHeaderCell("VET RESPONSE"));
        header.addView(createTableHeaderCell("VET NAME")); header.addView(createTableHeaderCell("CONFIRM VISIT"));
        table.addView(header);

        List<Map<String, String>> reports = dbHelper.getReportsByFarmer(userId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        for (Map<String, String> report : reports) {
            TableRow row = new TableRow(this); row.setPadding(0, 8, 0, 8); row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(createTableCell("REP-" + report.get("id"))); row.addView(createTableCell(report.get("animal")));
            row.addView(createTableCell(report.get("date"))); row.addView(createTableCell(report.get("suspected")));
            String visitDate = report.get("visit_date"); 
            int confirmed = (report.get("visit_confirmed") != null && !report.get("visit_confirmed").equals("null")) ? Integer.parseInt(report.get("visit_confirmed")) : 0;
            row.addView(createTableCell(visitDate != null && !visitDate.isEmpty() && !visitDate.equals("null") ? visitDate : "Pending"));
            row.addView(createTableCell(report.get("response"))); row.addView(createTableCell(report.get("vet_name")));

            MaterialButton btnConfirm = new MaterialButton(this); btnConfirm.setTextSize(10); btnConfirm.setCornerRadius(15);
            boolean isPastDate = false;
            if (visitDate != null && !visitDate.isEmpty() && !visitDate.equals("null")) { try { isPastDate = sdf.parse(visitDate).before(sdf.parse(today)); } catch (Exception e) {} }

            if (confirmed == 1) { btnConfirm.setText("VISITED ✅"); btnConfirm.setBackgroundColor(Color.parseColor("#81C784")); btnConfirm.setEnabled(false); }
            else if (visitDate == null || visitDate.isEmpty() || visitDate.equals("null")) { btnConfirm.setText("NOT YET"); btnConfirm.setBackgroundColor(Color.parseColor("#FFD740")); btnConfirm.setTextColor(Color.BLACK); btnConfirm.setEnabled(false); }
            else if (isPastDate) { btnConfirm.setText("PAST DUE"); btnConfirm.setBackgroundColor(Color.parseColor("#EF9A9A")); btnConfirm.setEnabled(false); }
            else { btnConfirm.setText("CONFIRM"); btnConfirm.setBackgroundColor(Color.parseColor("#4CAF50")); btnConfirm.setOnClickListener(v -> { if (dbHelper.confirmVisit(Integer.parseInt(report.get("id")))) { Toast.makeText(this, "Confirmed!", Toast.LENGTH_SHORT).show(); refreshMyReportsTable(table); } }); }
            
            btnConfirm.setLayoutParams(btnLp());
            row.addView(btnConfirm);
            table.addView(row);
        }
    }

    private TableRow.LayoutParams btnLp() { TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT); lp.setMargins(10, 10, 10, 10); return lp; }
    private TextView createTableHeaderCell(String text) { TextView tv = createTableCell(text); tv.setTypeface(null, Typeface.BOLD); return tv; }
    private TextView createTableCell(String text) { TextView tv = new TextView(this); tv.setText(text != null && !text.equals("null") ? text : "---"); tv.setTextColor(Color.WHITE); tv.setPadding(24, 32, 24, 32); tv.setGravity(Gravity.CENTER); tv.setTextSize(13); return tv; }

    private void showRateVetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_rate_vet, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        LinearLayout container = view.findViewById(R.id.container_vets_to_rate);
        TextView tvNoVets = view.findViewById(R.id.tv_no_vets);
        view.findViewById(R.id.btn_back_rate).setOnClickListener(v -> dialog.dismiss());
        List<Map<String, String>> vets = dbHelper.getVetsToRate(userId);
        if (vets.isEmpty()) tvNoVets.setVisibility(View.VISIBLE);
        else {
            tvNoVets.setVisibility(View.GONE);
            for (Map<String, String> vet : vets) {
                MaterialCardView card = new MaterialCardView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 32); card.setLayoutParams(params); card.setRadius(40); card.setCardBackgroundColor(Color.parseColor("#26FFFFFF"));
                LinearLayout inner = new LinearLayout(this); inner.setOrientation(LinearLayout.VERTICAL); inner.setPadding(40, 40, 40, 40); inner.setGravity(Gravity.CENTER);
                TextView tvRepId = new TextView(this); tvRepId.setText("REPORT ID: REP-" + vet.get("report_id")); tvRepId.setTextColor(Color.parseColor("#FFD740")); tvRepId.setTextSize(12); inner.addView(tvRepId);
                ShapeableImageView iv = new ShapeableImageView(this); iv.setLayoutParams(new LinearLayout.LayoutParams(180, 180)); loadCircularImage(vet.get("pic"), iv); inner.addView(iv);
                TextView name = new TextView(this); name.setText("Vet: " + vet.get("name")); name.setTextColor(Color.WHITE); name.setTextSize(18); name.setTypeface(null, Typeface.BOLD); inner.addView(name);
                TextView msg = new TextView(this); msg.setText("\"" + vet.get("message") + "\""); msg.setTextColor(Color.WHITE); msg.setAlpha(0.7f); msg.setGravity(Gravity.CENTER); inner.addView(msg);
                RatingBar ratingBar = new RatingBar(this, null, android.R.attr.ratingBarStyle); ratingBar.setNumStars(5); ratingBar.setStepSize(1.0f); inner.addView(ratingBar);
                TextInputEditText etComment = new TextInputEditText(this); etComment.setHint("Write a comment (optional)"); etComment.setTextColor(Color.WHITE); etComment.setHintTextColor(Color.GRAY); inner.addView(etComment);
                MaterialButton btnRate = new MaterialButton(this); btnRate.setText("RATE NOW"); btnRate.setCornerRadius(20); btnRate.setBackgroundColor(Color.parseColor("#4CAF50")); btnRate.setOnClickListener(v -> {
                    if (ratingBar.getRating() == 0) return;
                    if (dbHelper.addRating(userId, Integer.parseInt(vet.get("id")), Integer.parseInt(vet.get("report_id")), ratingBar.getRating(), etComment.getText().toString().trim())) { Toast.makeText(this, "Rated!", Toast.LENGTH_SHORT).show(); container.removeView(card); if (container.getChildCount() == 0) tvNoVets.setVisibility(View.VISIBLE); }
                }); inner.addView(btnRate); card.addView(inner); container.addView(card);
            }
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
        dialogProfileImg.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        view.findViewById(R.id.btn_update_profile).setOnClickListener(v -> {
            if (dbHelper.updateUserProfile(etEmail.getText().toString(), etName.getText().toString().trim(), etPhone.getText().toString().trim(), selectedImageUri)) {
                SharedPreferences.Editor ed = pref.edit(); ed.putString("userName", etName.getText().toString()); ed.putString("userPhone", etPhone.getText().toString()); ed.putString("userProfilePic", selectedImageUri); ed.apply();
                loadSessionData(); Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show(); dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void animateClick(View v) { v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()).start(); }

    private void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_report_case, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        reportImageUris.clear();
        reportImageViews[0] = view.findViewById(R.id.img_upload_1); reportImageViews[1] = view.findViewById(R.id.img_upload_2); reportImageViews[2] = view.findViewById(R.id.img_upload_3);
        AutoCompleteTextView spinnerAnimal = view.findViewById(R.id.spinner_animal_type), spinnerSeverity = view.findViewById(R.id.spinner_severity);
        String[] animals = {"Cattle", "Chicken", "Turkey", "Goat", "Sheep", "Pigs", "Ducks", "Rabbits", "Horses", "Donkeys", "Camels", "Bees"}, severity = {"Low", "Moderate", "High", "Critical"};
        spinnerAnimal.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, animals)); spinnerSeverity.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, severity));
        TextInputEditText etDate = view.findViewById(R.id.et_date_observed), etDesc = view.findViewById(R.id.et_description), etSymp = view.findViewById(R.id.et_symptoms), etSusc = view.findViewById(R.id.et_suspected_disease), etAff = view.findViewById(R.id.et_affected_count), etDist = view.findViewById(R.id.et_district), etVill = view.findViewById(R.id.et_village);
        etLat = view.findViewById(R.id.et_lat); etLng = view.findViewById(R.id.et_lng);
        etDate.setOnClickListener(v -> { Calendar c = Calendar.getInstance(); new DatePickerDialog(this, (v1, y, m, d1) -> etDate.setText(y + "-" + (m + 1) + "-" + d1), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show(); });
        view.findViewById(R.id.btn_select_location).setOnClickListener(v -> mapPickerLauncher.launch(new Intent(this, MapPickerActivity.class)));
        view.findViewById(R.id.btn_add_images).setOnClickListener(v -> pickReportImages.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        view.findViewById(R.id.btn_back_report).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_submit_report).setOnClickListener(v -> {
            if (spinnerAnimal.getText().toString().isEmpty() || etDesc.getText().toString().isEmpty() || capturedLat == 0.0) { Toast.makeText(this, "Fill required fields!", Toast.LENGTH_SHORT).show(); return; }
            long id = dbHelper.addDiseaseReport(userId, spinnerAnimal.getText().toString(), etDesc.getText().toString(), etSymp.getText().toString(), etDate.getText().toString(), capturedLat, capturedLng, spinnerSeverity.getText().toString(), etSusc.getText().toString(), Integer.parseInt(etAff.getText().toString().isEmpty() ? "0" : etAff.getText().toString()), etDist.getText().toString(), etVill.getText().toString());
            if (id != -1) { for (String u : reportImageUris) dbHelper.addReportImage(id, u); Toast.makeText(this, "Submitted!", Toast.LENGTH_LONG).show(); dialog.dismiss(); }
        });
        dialog.show();
    }
}
