package com.app.SSR.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.app.SSR.R;
import com.app.SSR.ui.adapter.WelcomePagerAdapter;
import com.app.SSR.util.Method;
import com.google.android.material.textview.MaterialTextView;

public class WelcomeActivity extends AppCompatActivity {

    private Method method;
    private int[] layouts;
    private ViewPager viewPager;
    private MaterialTextView textViewSkip, textViewNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        method = new Method(WelcomeActivity.this);

        // Checking for first time launch - before calling setContentView()
        if (!method.isFirstTimeLaunch()) {
            startActivity(new Intent(WelcomeActivity.this, SplashScreen.class));
            finish();
        }

        // Making notification bar transparent
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        method.changeStatusBarColor();// making notification bar transparent

        setContentView(R.layout.activity_welcome);

        viewPager = findViewById(R.id.viewPager_welcome);
        textViewSkip = findViewById(R.id.textView_skip_welcome);
        textViewNext = findViewById(R.id.textView_next_welcome);

        layouts = new int[]{R.layout.welcome_slide_one, R.layout.welcome_slide_two,};

        WelcomePagerAdapter welcomePagerAdapter = new WelcomePagerAdapter(WelcomeActivity.this, layouts);
        viewPager.setAdapter(welcomePagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        textViewSkip.setOnClickListener(v -> launchHomeScreen());

        textViewNext.setOnClickListener(v -> {
            // checking for last page
            // if last page home screen will be launched
            int current = viewPager.getCurrentItem() + 1;
            if (current < layouts.length) {
                // move to next screen
                viewPager.setCurrentItem(current);
            } else {
                launchHomeScreen();
            }
        });

    }

    private void launchHomeScreen() {
        method.setFirstTimeLaunch(false);
        startActivity(new Intent(WelcomeActivity.this, SplashScreen.class));
        finishAffinity();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                textViewNext.setText(getString(R.string.start));
                textViewSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                textViewNext.setText(getString(R.string.next));
                textViewSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

}
