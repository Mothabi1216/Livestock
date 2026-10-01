package com.example.livestock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // INITIALIZE DATABASE (This makes it appear in App Inspection)
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.getWritableDatabase();

        // Handle Window Insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        View header = findViewById(R.id.header);
        View heroSection = findViewById(R.id.hero_section);
        View buttonContainer = findViewById(R.id.button_container);
        View footer = findViewById(R.id.footer);
        
        View btnLogin = findViewById(R.id.btn_login);
        View btnRegister = findViewById(R.id.btn_register);
        View btnAbout = findViewById(R.id.btn_about);
        View btnContact = findViewById(R.id.btn_contact);

        // Load Animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        
        // Apply Animations with slight delays for a staggered entrance
        header.startAnimation(fadeIn);
        
        heroSection.setAlpha(0);
        heroSection.postDelayed(() -> {
            heroSection.setAlpha(1);
            heroSection.startAnimation(fadeIn);
        }, 200);

        buttonContainer.setAlpha(0);
        buttonContainer.postDelayed(() -> {
            buttonContainer.setAlpha(1);
            buttonContainer.startAnimation(fadeIn);
        }, 400);

        footer.setAlpha(0);
        footer.postDelayed(() -> {
            footer.setAlpha(1);
            footer.startAnimation(fadeIn);
        }, 600);

        // Set Click Listeners with feedback and navigation
        btnLogin.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                // Navigate to LoginActivity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }).start();
        });

        btnRegister.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                // Navigate to RegisterActivity
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }).start();
        });

        btnAbout.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AboutUsActivity.class));
        });

        btnContact.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ContactUsActivity.class));
        });
    }
}
