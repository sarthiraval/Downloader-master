package com.app.SSR.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.app.SSR.BuildConfig;
import com.app.SSR.R;
import com.app.SSR.interfaces.OnClick;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

public class Method {

    public Activity activity;
    private OnClick onClick;
    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    private final String myPreference = "Instagram";
    public String themSetting = "them";
    public String isDelete = "isDelete";
    public static boolean isDownload = true;
    public String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public Method(Activity activity) {
        this.activity = activity;
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
    }

    public Method(Activity activity, OnClick onClick) {
        this.activity = activity;
        this.onClick = onClick;
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public String getThem() {
        return pref.getString(themSetting, "system");
    }

    //get download folder path
    public File getDownload() {
        return new File(activity.getExternalFilesDir(activity.getResources().getString(R.string.download_folder_path)).toString());
    }

    //network check
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void changeStatusBarColor() {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    //Instagram application installation or not check
    public boolean isAppInstalledInstagram(Activity activity) {
        String packageName = "com.instagram.android";
        Intent mIntent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
        return mIntent != null;
    }

    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        columnWidth = point.x;
        return columnWidth;
    }

    public void share(String link, String type) {

        Uri contentUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileprovider", new File(link));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        if (type.equals("image")) {
            shareIntent.setType("image/*");
        } else {
            shareIntent.setType("video/*");
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.setClipData(ClipData.newRawUri("", contentUri));
        shareIntent.putExtra(Intent.EXTRA_TEXT, activity.getResources().getString(R.string.DeveloperName));
        activity.startActivity(Intent.createChooser(shareIntent, activity.getResources().getString(R.string.share_to)));

    }

    public void onClick(final int position, final String type, final String data) {
        onClick.show(position, type, data);
    }

    public void alertBox(String message) {

        try {
            if (activity != null) {
                if (!activity.isFinishing()) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
                    builder.setMessage(message);
                    builder.setPositiveButton(activity.getResources().getString(R.string.ok),
                            (arg0, arg1) -> {

                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        } catch (Exception e) {
            Log.d("error", e.toString());
        }

    }

    //check dark mode or not
    public boolean isDarkMode() {
        int currentNightMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active, we're using the light theme
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active, we're using dark theme
                return true;
            default:
                return false;
        }
    }

    public String webViewText() {
        if (isDarkMode()) {
            return Constant.webViewTextNight;
        } else {
            return Constant.webViewTextDay;
        }
    }

}
