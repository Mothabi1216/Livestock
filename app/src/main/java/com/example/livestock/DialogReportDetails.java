package com.example.livestock;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class DialogReportDetails extends DialogFragment {

    private static final String ARG_REPORT_ID = "report_id";
    private int reportId;
    private DatabaseHelper dbHelper;
    private MapView mapDetail;
    
    // Carousel state
    private List<String> imageList = new ArrayList<>();
    private int currentImageIndex = 0;
    private ImageView imgMain;
    private TextView tvCounter;

    public static DialogReportDetails newInstance(int reportId) {
        DialogReportDetails fragment = new DialogReportDetails();
        Bundle args = new Bundle();
        args.putInt(ARG_REPORT_ID, reportId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        if (getArguments() != null) {
            reportId = getArguments().getInt(ARG_REPORT_ID);
        }
        dbHelper = new DatabaseHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_report_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        mapDetail = view.findViewById(R.id.map_view);
        imgMain = view.findViewById(R.id.img_evidence_main);
        tvCounter = view.findViewById(R.id.tv_image_counter);
        
        view.findViewById(R.id.btn_back_detail).setOnClickListener(v -> dismiss());

        setupDatePicker(view);
        loadReportDetails(view);
        setupCarousel(view);
    }

    private void setupCarousel(View view) {
        ImageButton btnPrev = view.findViewById(R.id.btn_prev_image);
        ImageButton btnNext = view.findViewById(R.id.btn_next_image);

        btnPrev.setOnClickListener(v -> {
            if (imageList.size() > 0) {
                currentImageIndex = (currentImageIndex - 1 + imageList.size()) % imageList.size();
                updateDisplayImage();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (imageList.size() > 0) {
                currentImageIndex = (currentImageIndex + 1) % imageList.size();
                updateDisplayImage();
            }
        });
    }

    private void updateDisplayImage() {
        if (imageList.isEmpty()) {
            imgMain.setImageResource(R.drawable.pic2);
            tvCounter.setText("0/0");
            return;
        }
        
        Glide.with(this)
                .load(imageList.get(currentImageIndex))
                .centerCrop()
                .placeholder(R.drawable.pic2)
                .into(imgMain);
        
        tvCounter.setText((currentImageIndex + 1) + "/" + imageList.size());
    }

    private void loadReportDetails(View view) {
        Map<String, String> report = dbHelper.getReportById(reportId);
        if (report == null) {
            Toast.makeText(getContext(), "Report not found", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        setText(view, R.id.tv_detail_id, "REP-" + report.get("id"));
        setText(view, R.id.tv_detail_farmer, report.get("farmer"));
        setText(view, R.id.tv_detail_animal, report.get("animal"));
        
        String locationStr = report.get("location") + ", " + report.get("village");
        setText(view, R.id.tv_detail_location, locationStr);

        String latStr = report.get("lat");
        String lngStr = report.get("lng");
        if (latStr != null && lngStr != null && !latStr.equals("0.0")) {
            try {
                double lat = Double.parseDouble(latStr);
                double lng = Double.parseDouble(lngStr);
                setupReadOnlyMap(lat, lng);
            } catch (Exception e) {
                view.findViewById(R.id.card_map_section).setVisibility(View.GONE);
            }
        } else {
            view.findViewById(R.id.card_map_section).setVisibility(View.GONE);
        }

        imageList = dbHelper.getReportImages(reportId);
        updateDisplayImage();

        setEditText(view, R.id.et_visit_date, report.get("visit_date"));
        setEditText(view, R.id.et_vet_response, report.get("response"));

        setupActionButtons(view);
    }

    private void setupReadOnlyMap(double lat, double lng) {
        if (mapDetail == null) return;
        mapDetail.setTileSource(TileSourceFactory.MAPNIK);
        GeoPoint point = new GeoPoint(lat, lng);
        mapDetail.getController().setZoom(17.0);
        mapDetail.getController().setCenter(point);
        mapDetail.setMultiTouchControls(false);
        Marker marker = new Marker(mapDetail);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Animal Location");
        mapDetail.getOverlays().add(marker);
        mapDetail.invalidate();
    }

    private void setupActionButtons(View view) {
        MaterialButton btnAccept = view.findViewById(R.id.btn_accept);
        MaterialButton btnReject = view.findViewById(R.id.btn_reject);

        SharedPreferences pref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String vetEmail = pref.getString("userEmail", "");
        int vetId = dbHelper.getUserIdByEmail(vetEmail);

        btnAccept.setOnClickListener(v -> {
            String response = ((EditText)view.findViewById(R.id.et_vet_response)).getText().toString().trim();
            String visitDate = ((EditText)view.findViewById(R.id.et_visit_date)).getText().toString().trim();
            if (response.isEmpty()) {
                Toast.makeText(getContext(), "Please provide a diagnosis or response", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbHelper.addVetResponse(String.valueOf(reportId), vetId, response, visitDate, "", "Approved")) {
                Toast.makeText(getContext(), "Report Approved & Scheduled!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) getActivity().recreate();
                dismiss();
            }
        });

        btnReject.setOnClickListener(v -> {
            if (dbHelper.addVetResponse(String.valueOf(reportId), vetId, "Rejected by Vet", "", "", "Rejected")) {
                Toast.makeText(getContext(), "Report Rejected", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) getActivity().recreate();
                dismiss();
            }
        });
    }

    private void setupDatePicker(View view) {
        EditText etVisitDate = view.findViewById(R.id.et_visit_date);
        etVisitDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view1, year, month, day) -> {
                etVisitDate.setText(year + "-" + (month + 1) + "-" + day);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setText(View view, int id, String text) {
        TextView tv = view.findViewById(id);
        if (tv != null) tv.setText(text != null && !text.isEmpty() && !text.equals("null") ? text : "N/A");
    }

    private void setEditText(View view, int id, String text) {
        EditText et = view.findViewById(id);
        if (et != null) et.setText(text != null && !text.equals("null") ? text : "");
    }

    @Override
    public void onResume() { super.onResume(); if (mapDetail != null) mapDetail.onResume(); }
    @Override
    public void onPause() { super.onPause(); if (mapDetail != null) mapDetail.onPause(); }
}
