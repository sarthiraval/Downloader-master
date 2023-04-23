package com.app.SSR.ui.activity;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.app.SSR.R;
import com.app.SSR.ui.adapter.ViewPagerAdapter;
import com.app.SSR.interfaces.GetData;
import com.app.SSR.interfaces.OnClick;
import com.app.SSR.service.ActiveService;
import com.app.SSR.service.DownloadService;
import com.app.SSR.util.Constant;
import com.app.SSR.util.Events;
import com.app.SSR.util.FindData;
import com.app.SSR.util.GlobalBus;
import com.app.SSR.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Method method;
    private String[] pageTitle;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private DrawerLayout drawer;
    private TextInputEditText editText;
    private SwitchMaterial switchMaterial;
    private ViewPagerAdapter pagerAdapter;
    private NavigationView navigationView;
    private InputMethodManager inputMethodManager;
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlobalBus.getBus().register(this);

        File root = new File(getExternalFilesDir(getResources().getString(R.string.download_folder_path)).toString());
        if (!root.exists()) {
            root.mkdir();
        }

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        pageTitle = new String[]{getResources().getString(R.string.image), getResources().getString(R.string.video)};

        method = new Method(MainActivity.this, (OnClick) (position, type, data) -> {
            if (type.equals("getData")) {
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage(getResources().getString(R.string.loading));
                progressDialog.setCancelable(false);
                progressDialog.show();

                FindData findData = new FindData(getApplicationContext(), (GetData) (linkList, message, isData) -> {
                    if (isData) {
                        if (linkList.size() != 0) {
                            Constant.downloadArray.clear();
                            Constant.downloadArray.addAll(linkList);
                            Intent intent = new Intent(MainActivity.this, DownloadService.class);
                            intent.setAction("com.download.action.START");
                            startService(intent);
                        } else {
                            method.alertBox(getResources().getString(R.string.no_data_found));
                        }
                    } else {
                        method.alertBox(message);
                    }

                    editText.setText("");
                    progressDialog.dismiss();

                });
                findData.data(data);
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        MaterialToolbar toolbar = findViewById(R.id.toolbar_main);
        viewPager2 = findViewById(R.id.viewPager_welcome);
        tabLayout = findViewById(R.id.tab_layout);
        editText = findViewById(R.id.editText_main);
        MaterialButton button = findViewById(R.id.button_submit_main);
        ConstraintLayout conData = findViewById(R.id.con_main);

        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_side_nav);

        navigationView.setNavigationItemSelectedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            navigationView.getMenu().getItem(0).setVisible(false);
            conData.setVisibility(View.VISIBLE);
        } else {
            navigationView.getMenu().getItem(0).setVisible(true);
            conData.setVisibility(View.GONE);
        }

        Menu menu = navigationView.getMenu();
        final MenuItem menuItem = menu.findItem(R.id.drawer_switch);
        View actionView = menuItem.getActionView();
        switchMaterial = actionView.findViewById(R.id.switch_view);
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                menuItem.setTitle(getResources().getString(R.string.service_on));
                appServiceStart();
            } else {
                menuItem.setTitle(getResources().getString(R.string.service_off));
                appServiceStop();
            }
        });

        //set gravity for tab bar
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        //change ViewPager page when tab selected
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        button.setOnClickListener(v -> {
            String url = editText.getText().toString();

            editText.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);

            if (!url.equals("")) {
                method.onClick(0, "getData", url);
            } else {
                method.alertBox(getResources().getString(R.string.please_enter_url));
            }

        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!isMyServiceRunning()) {
                switchMaterial.setChecked(true);
            }
        }

        //set viewpager adapter
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), pageTitle.length);
        viewPager2.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager2, true, (tab, position) -> {
            // position of the current tab and that tab
            tab.setText(pageTitle[position]);
        }).attach();

    }


    @Subscribe
    public void getService(Events.ServiceNotify serviceNotify) {
        if (switchMaterial != null) {
            switchMaterial.setChecked(false);
        }
    }

    private void appServiceStart() {
        Intent intent = new Intent(getApplicationContext(), ActiveService.class);
        intent.setAction("com.action.serviceStart");
        startService(intent);
    }

    private void appServiceStop() {
        Intent intent = new Intent(getApplicationContext(), ActiveService.class);
        intent.setAction("com.action.serviceStop");
        startService(intent);
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ActiveService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.Please_click_BACK_again_to_exit), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.open_app_menu) {
            if (method.isAppInstalledInstagram(MainActivity.this)) {
                try {
                    PackageManager manager = this.getPackageManager();
                    Intent intent = manager.getLaunchIntentForPackage("com.instagram.android");
                    assert intent != null;
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(intent);
                } catch (Exception e) {
                    method.alertBox(getResources().getString(R.string.wrong));
                }
            } else {
                method.alertBox(getResources().getString(R.string.app_not_install));
            }
        }

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NotNull MenuItem item) {

        if (getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        // Handle navigation view item clicks here.
        //Checking if the item is in checked state or not, if not make it in checked state
        item.setChecked(!item.isChecked());

        //Closing drawer on item click
        drawer.closeDrawers();

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.setting) {
            unSelect(1);
            startActivity(new Intent(MainActivity.this, Setting.class));
            return true;
        }
        return true;
    }

    private void unSelect(int position) {
        navigationView.getMenu().getItem(position).setChecked(false);
        navigationView.getMenu().getItem(position).setCheckable(false);
    }

    private void select(int position) {
        navigationView.getMenu().getItem(position).setChecked(true);
    }

    @Override
    protected void onDestroy() {
        switchMaterial.setChecked(false);
        GlobalBus.getBus().unregister(this);
        super.onDestroy();
    }
}


