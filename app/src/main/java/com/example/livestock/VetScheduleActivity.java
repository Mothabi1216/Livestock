package com.example.livestock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

public class VetScheduleActivity extends AppCompatActivity {

    private TableLayout tableSchedule;
    private View headerSection, cardTable, tvEmpty;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vet_schedule);

        dbHelper = new DatabaseHelper(this);

        // Initialize Views
        tableSchedule = findViewById(R.id.table_schedule);
        headerSection = findViewById(R.id.header_section);
        cardTable = findViewById(R.id.card_table_container);
        tvEmpty = findViewById(R.id.tv_empty_schedule);

        findViewById(R.id.btn_back_schedule).setOnClickListener(v -> finish());

        // Load Schedule
        loadSchedule();

        // Prepare and Start Animations
        prepareAnimations();
        headerSection.post(this::startEntranceAnimation);
    }

    private void loadSchedule() {
        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String vetEmail = pref.getString("userEmail", "");
        
        List<Map<String, String>> appointments = dbHelper.getVetSchedule(vetEmail);

        if (appointments.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            cardTable.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            cardTable.setVisibility(View.VISIBLE);

            for (Map<String, String> appt : appointments) {
                TableRow row = new TableRow(this);
                row.setPadding(0, 16, 0, 16);
                row.setGravity(Gravity.CENTER_VERTICAL);

                row.addView(createCell(appt.get("animal")));
                row.addView(createCell(appt.get("farmer")));
                row.addView(createCell(appt.get("location")));
                row.addView(createCell(appt.get("desc")));
                row.addView(createCell(appt.get("symptoms")));
                row.addView(createCell(appt.get("report_date")));
                
                TextView dateCell = createCell(appt.get("visit_date"));
                dateCell.setTextColor(Color.parseColor("#64B5F6")); // Light blue for scheduled visit
                dateCell.setTypeface(null, Typeface.BOLD);
                row.addView(dateCell);

                tableSchedule.addView(row);
                
                // Add separator line
                View separator = new View(this);
                separator.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                separator.setBackgroundColor(Color.WHITE);
                separator.setAlpha(0.1f);
                tableSchedule.addView(separator);
            }
        }
    }

    private TextView createCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text != null && !text.equals("null") ? text : "N/A");
        tv.setTextColor(Color.WHITE);
        tv.setPadding(32, 24, 32, 24);
        tv.setTextSize(13.0f);
        return tv;
    }

    private void prepareAnimations() {
        headerSection.setAlpha(0f);
        headerSection.setTranslationY(-50f);
        
        cardTable.setAlpha(0f);
        cardTable.setTranslationY(100f);
        cardTable.setScaleX(0.9f);
        cardTable.setScaleY(0.9f);
    }

    private void startEntranceAnimation() {
        headerSection.animate().alpha(1f).translationY(0).setDuration(600).start();
        
        cardTable.animate()
                .alpha(1f)
                .translationY(0)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(700)
                .setStartDelay(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
}
