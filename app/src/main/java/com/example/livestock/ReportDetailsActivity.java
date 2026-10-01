package com.example.livestock;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import java.util.Map;

public class ReportDetailsActivity extends AppCompatActivity {

    private MapView mapView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. OSMDroid Initialization
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.dialog_report_details);

        dbHelper = new DatabaseHelper(this);
        mapView = findViewById(R.id.map_view);

        // Retrieve data from SQLite via Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("report_id")) {
            String reportIdStr = intent.getStringExtra("report_id");
            if (reportIdStr != null) {
                try {
                    // Parse the numeric ID (e.g., "REP-1021" -> 1021)
                    int reportId = Integer.parseInt(reportIdStr.replaceAll("[^0-9]", ""));
                    Map<String, String> reportData = dbHelper.getReportById(reportId);
                    if (reportData != null) {
                        populateDetails(reportData);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error loading report details", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Back button returns to Registry
        View btnBack = findViewById(R.id.btn_back_detail);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void populateDetails(Map<String, String> report) {
        setTextSafe(R.id.tv_detail_id, "REP-" + report.get("id"));
        setTextSafe(R.id.tv_detail_farmer, report.get("farmer"));
        setTextSafe(R.id.tv_detail_animal, report.get("animal"));
        setTextSafe(R.id.tv_detail_location, report.get("location") + " (" + report.get("village") + ")");
        
        // Severity Logic with dynamic border color
        String severity = report.get("severity");
        TextView tvSev = findViewById(R.id.tv_detail_severity);
        MaterialCardView infoCard = findViewById(R.id.card_report_info);
        
        if (tvSev != null) {
            tvSev.setText(severity != null ? severity.toUpperCase() : "NORMAL");
            int color = getSeverityColor(severity);
            tvSev.setTextColor(color);
            if (infoCard != null) {
                infoCard.setStrokeColor(color);
                infoCard.setStrokeWidth(8); // Make it visible
            }
        }

        // Coordinates display
        setTextSafe(R.id.tv_detail_latitude, report.get("lat"));
        setTextSafe(R.id.tv_detail_longitude, report.get("lng"));

        // 2. Setup Read-Only Map
        String latStr = report.get("lat");
        String lngStr = report.get("lng");
        if (latStr != null && lngStr != null) {
            try {
                double lat = Double.parseDouble(latStr);
                double lng = Double.parseDouble(lngStr);
                setupReadOnlyMap(lat, lng);
            } catch (Exception e) {
                hideMapSection();
            }
        } else {
            hideMapSection();
        }

        // Load images
        ImageView imgMain = findViewById(R.id.img_evidence_main);
        if (imgMain != null) {
            String idStr = report.get("id");
            if (idStr != null) {
                try {
                    int reportId = Integer.parseInt(idStr);
                    List<String> images = dbHelper.getReportImages(reportId);
                    if (images != null && !images.isEmpty()) {
                        Glide.with(this)
                                .load(images.get(0))
                                .centerCrop()
                                .placeholder(R.drawable.pic2)
                                .into(imgMain);
                    } else {
                        imgMain.setImageResource(R.drawable.pic2);
                    }
                } catch (Exception e) {
                    imgMain.setImageResource(R.drawable.pic2);
                }
            } else {
                imgMain.setImageResource(R.drawable.pic2);
            }
        }
    }

    private int getSeverityColor(String s) {
        if (s == null) return Color.WHITE;
        if (s.equalsIgnoreCase("CRITICAL") || s.equalsIgnoreCase("HIGH")) return Color.parseColor("#FF5252");
        if (s.equalsIgnoreCase("MODERATE")) return Color.parseColor("#FFD740");
        return Color.parseColor("#81C784");
    }

    private void hideMapSection() {
        View cardMap = findViewById(R.id.card_map_section);
        if (cardMap != null) cardMap.setVisibility(View.GONE);
    }

    private void setupReadOnlyMap(double lat, double lng) {
        if (mapView == null) return;

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        
        // DISABLE ALL INTERACTION (Vet Read-Only Mode)
        mapView.setMultiTouchControls(false);
        mapView.setClickable(false);
        mapView.setFocusable(false);
        
        GeoPoint point = new GeoPoint(lat, lng);
        mapView.getController().setZoom(17.0);
        mapView.getController().setCenter(point);

        // Add Marker
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Animal Location");
        mapView.getOverlays().add(marker);
        
        mapView.invalidate();
    }

    private void setTextSafe(int viewId, String text) {
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText(text != null ? text : "N/A");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }
}
