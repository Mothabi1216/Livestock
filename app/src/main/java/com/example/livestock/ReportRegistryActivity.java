package com.example.livestock;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

public class ReportRegistryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_view_reports);

        dbHelper = new DatabaseHelper(this);

        findViewById(R.id.btn_back_reports).setOnClickListener(v -> finish());

        TableLayout tableLayout = findViewById(R.id.table_reports);
        if (tableLayout != null) {
            populateTable(tableLayout);
        }
    }

    private void populateTable(TableLayout tableLayout) {
        List<Map<String, String>> reports = dbHelper.getAllPendingReports();
        
        // Remove all except header and spacing
        int count = tableLayout.getChildCount();
        if (count > 2) {
            tableLayout.removeViews(2, count - 2);
        }

        for (Map<String, String> report : reports) {
            TableRow row = new TableRow(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            
            // Set margins for the row to show the border clearly
            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(20, 20, 20, 20);
            row.setLayoutParams(rowParams);
            
            // Apply Dynamic Border based on severity
            applySeverityRowStyle(row, report.get("severity"));
            
            row.setClickable(true);
            row.setFocusable(true);
            
            row.addView(createTableTextView(report.get("id")));
            row.addView(createTableTextView(report.get("farmer")));

            TextView sev = createTableTextView(report.get("severity"));
            sev.setTextColor(getSeverityColor(report.get("severity")));
            sev.setTypeface(null, Typeface.BOLD);
            row.addView(sev);

            row.addView(createTableTextView(report.get("animal")));
            row.addView(createTableTextView(report.get("desc")));
            row.addView(createTableTextView(report.get("symptoms")));
            row.addView(createTableTextView(report.get("suspected")));
            row.addView(createTableTextView(report.get("affected")));
            row.addView(createTableTextView(report.get("location")));
            row.addView(createTableTextView(report.get("village")));
            row.addView(createTableTextView(report.get("date")));

            row.setOnClickListener(v -> {
                try {
                    String idStr = report.get("id");
                    if (idStr != null) {
                        int reportId = Integer.parseInt(idStr);
                        DialogReportDetails dialog = DialogReportDetails.newInstance(reportId);
                        dialog.show(getSupportFragmentManager(), "ReportDetails");
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error opening report", Toast.LENGTH_SHORT).show();
                }
            });
            
            tableLayout.addView(row);
            
            // Spacer View to separate the "glowing" rows
            View spacer = new View(this);
            spacer.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 25));
            tableLayout.addView(spacer);
        }
    }

    private void applySeverityRowStyle(TableRow row, String severity) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#33FFFFFF")); // Semi-transparent row background
        gd.setCornerRadius(20);
        
        int strokeWidth = 5;
        int strokeColor = getSeverityColor(severity);
        
        // Enhance Critical/High borders
        if (severity != null && (severity.equalsIgnoreCase("Critical") || severity.equalsIgnoreCase("High"))) {
            strokeWidth = 12; // Much thicker for danger
        } else if (severity != null && severity.equalsIgnoreCase("Moderate")) {
            strokeWidth = 8;
        }
        
        gd.setStroke(strokeWidth, strokeColor);
        row.setBackground(gd);
        row.setPadding(10, 15, 10, 15);
    }

    private TextView createTableTextView(String t) {
        TextView tv = new TextView(this);
        tv.setText(t != null ? t : "N/A");
        tv.setPadding(32, 40, 32, 40);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(14.0f);
        return tv;
    }

    private int getSeverityColor(String s) {
        if (s == null) return Color.WHITE;
        if (s.equalsIgnoreCase("CRITICAL") || s.equalsIgnoreCase("HIGH")) return Color.parseColor("#FF5252"); // Red
        if (s.equalsIgnoreCase("MODERATE")) return Color.parseColor("#FFD740"); // Yellow
        return Color.parseColor("#81C784"); // Green
    }
}
