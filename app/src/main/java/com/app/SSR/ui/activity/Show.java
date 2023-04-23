package com.app.SSR.ui.activity;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import com.app.SSR.R;
import com.app.SSR.ui.adapter.ShowAdapter;
import com.app.SSR.util.Constant;
import com.app.SSR.util.Events;
import com.app.SSR.util.GlobalBus;
import com.app.SSR.util.Method;
import com.app.SSR.util.ZoomOutTransformation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.theartofdev.edmodo.cropper.CropImage;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Show extends AppCompatActivity {

    private String type;
    private Method method;
    private Animation myAnim;
    private ViewPager viewPager;
    private List<File> showArray;
    private ShowAdapter showAdapter;
    private ConstraintLayout conSetAsWallpaper;
    private ImageView imageViewTwo, imageViewSetWallpaper, imageViewDelete, imageViewShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);

        method = new Method(Show.this);
        method.changeStatusBarColor();

        GlobalBus.getBus().register(this);

        Intent in = getIntent();
        int selectedPosition = in.getIntExtra("position", 0);
        type = in.getStringExtra("type");

        showArray = new ArrayList<>();

        myAnim = AnimationUtils.loadAnimation(Show.this, R.anim.bounce);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_imageShow);
        viewPager = findViewById(R.id.viewpager_imageShow);
        imageViewTwo = findViewById(R.id.imageView_line_two_imageShow);
        conSetAsWallpaper = findViewById(R.id.con_setAsWall_imageShow);
        ConstraintLayout conDelete = findViewById(R.id.con_delete_imageShow);
        ConstraintLayout conShare = findViewById(R.id.con_share_imageShow);
        imageViewSetWallpaper = findViewById(R.id.imageView_setAsWall_imageShow);
        imageViewDelete = findViewById(R.id.imageView_delete_imageShow);
        imageViewShare = findViewById(R.id.imageView_share_imageShow);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        switch (type) {
            case "image":
                showArray = Constant.imageArray;
                break;
            case "video":
                showArray = Constant.videoArray;
                break;
        }

        ZoomOutTransformation zoomOutTransformation = new ZoomOutTransformation();
        viewPager.setPageTransformer(true, zoomOutTransformation);

        showAdapter = new ShowAdapter(Show.this, showArray, type);
        viewPager.setAdapter(showAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(selectedPosition);

        checkImage();

        conSetAsWallpaper.setOnClickListener(v -> {
            imageViewSetWallpaper.startAnimation(myAnim);
            CropImage.activity(Uri.fromFile(new File(showArray.get(viewPager.getCurrentItem()).toString()))).start(Show.this);
        });

        conDelete.setOnClickListener(v -> {
            imageViewDelete.startAnimation(myAnim);
            if (method.pref.getBoolean(method.isDelete, true)) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Show.this, R.style.DialogTitleTextStyle);
                builder.setMessage(getResources().getString(R.string.delete_title));
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setCancelable(false);
                builder.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {

                });
                builder.setPositiveButton(getResources().getString(R.string.yes), (arg0, arg1) -> {
                    deleteFile();
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                deleteFile();
            }
        });

        conShare.setOnClickListener(v -> {
            imageViewShare.startAnimation(myAnim);
            switch (type) {
                case "image":
                    method.share(showArray.get(viewPager.getCurrentItem()).toString(), "image");
                    break;
                case "video":
                    method.share(showArray.get(viewPager.getCurrentItem()).toString(), "video");
                    break;
            }
            Toast.makeText(Show.this, getResources().getString(R.string.share), Toast.LENGTH_SHORT).show();
        });

    }

    @Subscribe
    public void getNotify(Events.AdapterNotify adapterNotify) {
        if (showAdapter != null) {
            showAdapter.notifyDataSetChanged();
        }
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
    }

    //	page change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            checkImage();
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

    };

    private void deleteFile() {
        if (showArray.size() != 0) {
            try {
                File files = new File(showArray.get(viewPager.getCurrentItem()).toString());
                files.delete();
                showArray.remove(viewPager.getCurrentItem());
                showAdapter.notifyDataSetChanged();
                if (showArray.size() == 0) {
                    onBackPressed();
                }
                Events.AdapterNotify adapterNotify = new Events.AdapterNotify(type);
                GlobalBus.getBus().post(adapterNotify);
                Toast.makeText(Show.this, getResources().getString(R.string.delete), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                method.alertBox(getResources().getString(R.string.wrong));
            }
        }
    }

    public void checkImage() {

        if (type.equals("video")) {
            imageViewTwo.setVisibility(View.GONE);
            conSetAsWallpaper.setVisibility(View.GONE);
        } else {
            imageViewTwo.setVisibility(View.VISIBLE);
            conSetAsWallpaper.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    Bitmap myBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                    WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                    myWallpaperManager.setBitmap(myBitmap);
                } catch (IOException e) {
                    Log.d("data_app", e.toString());
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("data_app", error.toString());
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    protected void onDestroy() {
        // Unregister the registered event.
        GlobalBus.getBus().unregister(this);
        super.onDestroy();
    }
}
