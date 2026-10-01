package com.example.livestock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {

    private ImageView imgLeft, imgCenter, imgRight;
    private final int[] images = {R.drawable.pic1, R.drawable.pic2, R.drawable.pic3, R.drawable.pic4, R.drawable.pic5, R.drawable.pic6, R.drawable.pic7};
    private int currentIndex = 0;
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // Initialize Views
        imgLeft = findViewById(R.id.img_left);
        imgCenter = findViewById(R.id.img_center);
        imgRight = findViewById(R.id.img_right);

        // Setup Toolbar Navigation
        findViewById(R.id.btn_home).setOnClickListener(v -> finish());
        findViewById(R.id.btn_to_contact).setOnClickListener(v -> {
            startActivity(new Intent(this, ContactUsActivity.class));
            finish();
        });

        // Initial Animation for content
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        findViewById(R.id.about_title).startAnimation(fadeIn);
        findViewById(R.id.about_description).startAnimation(fadeIn);

        // Start Auto Switcher
        startImageSwitcher();
        
        // Interaction Animations
        setupImageInteractions(imgLeft);
        setupImageInteractions(imgCenter);
        setupImageInteractions(imgRight);
    }

    private void startImageSwitcher() {
        runnable = new Runnable() {
            @Override
            public void run() {
                switchImages();
                handler.postDelayed(this, 3500); // 3.5 seconds interval
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void switchImages() {
        // Animation logic for switching
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        
        imgLeft.startAnimation(fadeOut);
        imgCenter.startAnimation(fadeOut);
        imgRight.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                currentIndex = (currentIndex + 1) % images.length;
                
                int leftIdx = (currentIndex == 0) ? images.length - 1 : currentIndex - 1;
                int rightIdx = (currentIndex + 1) % images.length;

                imgLeft.setImageResource(images[leftIdx]);
                imgCenter.setImageResource(images[currentIndex]);
                imgRight.setImageResource(images[rightIdx]);

                // Staggered Fade In and Scale with Overshoot
                animateIn(imgLeft, 0);
                animateIn(imgCenter, 200);
                animateIn(imgRight, 400);
            }
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void animateIn(ImageView view, int delay) {
        view.setAlpha(0f);
        view.setScaleX(0.5f);
        view.setScaleY(0.5f);
        
        view.animate()
                .alpha(view == imgCenter ? 1.0f : 0.7f)
                .scaleX(view == imgCenter ? 1.2f : 0.9f)
                .scaleY(view == imgCenter ? 1.2f : 0.9f)
                .setDuration(600)
                .setStartDelay(delay)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void setupImageInteractions(ImageView view) {
        view.setOnClickListener(v -> {
            v.animate().scaleXBy(0.1f).scaleYBy(0.1f).setDuration(200).withEndAction(() -> {
                v.animate().scaleXBy(-0.1f).scaleYBy(-0.1f).setDuration(200).start();
            }).start();
            v.setElevation(20f);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}