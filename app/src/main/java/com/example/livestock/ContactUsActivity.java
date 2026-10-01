package com.example.livestock;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

public class ContactUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        // UI Components
        findViewById(R.id.card_phone).setOnClickListener(v -> {
            scaleClick(v);
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+26662705338"));
            startActivity(intent);
        });

        findViewById(R.id.card_email).setOnClickListener(v -> {
            scaleClick(v);
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:mothabi.leboto@bothouniversity.com"));
            startActivity(intent);
        });

        // Navigation
        findViewById(R.id.btn_home).setOnClickListener(v -> finish());
        findViewById(R.id.btn_to_about).setOnClickListener(v -> {
            startActivity(new Intent(this, AboutUsActivity.class));
            finish();
        });

        // Entrance Animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        findViewById(R.id.toolbar).startAnimation(fadeIn);
        findViewById(R.id.contact_content).startAnimation(slideUp);
        findViewById(R.id.navigation_container).startAnimation(slideUp);
    }

    private void scaleClick(android.view.View v) {
        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
            v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
        }).start();
    }
}