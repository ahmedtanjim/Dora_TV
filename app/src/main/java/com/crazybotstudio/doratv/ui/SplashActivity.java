package com.crazybotstudio.doratv.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.crazybotstudio.doratv.R;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vpnControl.stopVpn(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        imageView = findViewById(R.id.imageView);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.side_slide);
        imageView.startAnimation(animation);
        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, CategoryActivity.class);
                startActivity(i);
                finish();
            }
        }, 3500);

    }

    @Override
    protected void onStart() {
        vpnControl.stopVpn(this);
        super.onStart();
    }
}