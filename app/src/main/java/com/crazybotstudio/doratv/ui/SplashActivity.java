package com.crazybotstudio.doratv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.crazybotstudio.doratv.R;
import com.crazybotstudio.doratv.player.PlayerActivity;
import com.startapp.sdk.ads.splash.SplashConfig;
import com.startapp.sdk.adsbase.StartAppAd;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vpnControl.stopVpn(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        imageView = findViewById(R.id.imageView);
//        StartAppAd.showSplash(this, savedInstanceState, new SplashConfig()
//                .setTheme(SplashConfig.Theme.USER_DEFINED)
//                .setCustomScreen(R.layout.activity_splash)
//        );
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashActivity.this, CategoryActivity.class);
            startActivity(i);
            finish();
        }, 3500);

    }

    @Override
    protected void onStart() {
        vpnControl.stopVpn(this);
        super.onStart();
    }
}