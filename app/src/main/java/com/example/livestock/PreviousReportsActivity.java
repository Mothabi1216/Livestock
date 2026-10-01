package com.example.livestock;

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

public class PreviousReportsActivity extends AppCompatActivity {

    private TableLayout tablePreviousReports;
    private View headerSection, cardTable, tvEmpty;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_reports);

        dbHelper = new DatabaseHelper(this);

        // Initialize Views
        tablePreviousReports = findViewById(R.id.table_previous_reports);
        headerSection = findViewById(R.id.header_section_prev);
        cardTable = findViewById(R.id.card_table_prev_container);
        tvEmpty = findViewById(R.id.tv_empty_prev_reports);

        findViewById(R.id.btn_back_prev_reports).setOnClickListener(v -> finish());

        // Load Previous Reports
        loadPreviousReports();

        // Prepare and Start Animations
        prepareAnimations();
        headerSection.post(this::startEntranceAnimation);
    }

    private void loadPreviousReports() {
        List<Map<String, String>> reports = dbHelper.getPreviousReports();

        if (reports.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            cardTable.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            cardTable.setVisibility(View.VISIBLE);

            for (Map<String, String> report : reports) {
                TableRow row = new TableRow(this);
                row.setPadding(0, 16, 0, 16);
                row.setGravity(Gravity.CENTER_VERTICAL);

                row.addView(createCell(report.get("animal")));
                row.addView(createCell(report.get("farmer")));
                row.addView(createCell(report.get("disease")));
                
                TextView statusCell = createCell(report.get("status"));
                statusCell.setTextColor(getStatusColor(report.get("status")));
                statusCell.setTypeface(null, Typeface.BOLD);
                row.addView(statusCell);

                row.addView(createCell(report.get("date")));
                row.addView(createCell(report.get("vet")));

                tablePreviousReports.addView(row);
                
                // Add separator line
                View separator = new View(this);
                separator.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                separator.setBackgroundColor(Color.WHITE);
                separator.setAlpha(0.1f);
                tablePreviousReports.addView(separator);
            }
        }
    }

    private TextView createCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text != null ? text : "N/A");
        tv.setTextColor(Color.WHITE);
        tv.setPadding(24, 16, 24, 16);
        tv.setTextSize(14.0f);
        return tv;
    }

    private int getStatusColor(String status) {
        if (status == null) return Color.WHITE;
        switch (status.toLowerCase()) {
            case "resolved": return Color.parseColor("#81C784"); // Green
            case "visited": return Color.parseColor("#64B5F6"); // Blue
            case "action taken": return Color.parseColor("#FFD740"); // Amber
            default: return Color.WHITE;
        }
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
