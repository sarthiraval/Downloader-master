package com.app.SSR.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.SSR.R;
import com.app.SSR.util.Method;


public class SplashScreen extends AppCompatActivity {

    private Boolean isCancelled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Method method = new Method(SplashScreen.this);
        switch (method.getThem()) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                break;
        }

        setContentView(R.layout.activity_splash_screen);

        // Making notification bar transparent
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        method.changeStatusBarColor();

        int SPLASH_TIME_OUT = 1000;
        new Handler().postDelayed(() -> {
            if (!isCancelled) {
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                finishAffinity();
            }
        }, SPLASH_TIME_OUT);

    }

    @Override
    protected void onDestroy() {
        isCancelled = true;
        super.onDestroy();
    }

}
