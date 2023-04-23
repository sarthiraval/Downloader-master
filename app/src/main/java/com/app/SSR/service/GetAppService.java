package com.app.SSR.service;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.app.SSR.R;
import com.app.SSR.interfaces.GetData;
import com.app.SSR.util.Constant;
import com.app.SSR.util.FindData;

import java.util.ArrayList;

public class GetAppService extends Service {

    private FindData findData;
    private ClipboardManager mClipboardManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        assert mClipboardManager != null;
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);

        findData = new FindData(getApplicationContext(), new GetData() {
            @Override
            public void getData(ArrayList<String> linkList, String message, boolean isData) {
                if (isData) {
                    if (linkList.size() != 0) {
                        Constant.downloadArray.clear();
                        Constant.downloadArray.addAll(linkList);
                        Intent intent = new Intent(getApplicationContext(), DownloadService.class);
                        intent.setAction("com.download.action.START");
                        startService(intent);
                    } else {
                        Toast.makeText(GetAppService.this, getResources().getString(R.string.no_data_found), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GetAppService.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    try {
                        ClipData clip = mClipboardManager.getPrimaryClip();
                        assert clip != null;
                        String string = clip.getItemAt(0).getText().toString();
                        findData.data(string);
                    } catch (Exception e) {
                        Toast.makeText(GetAppService.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            };

    @Override
    public void onDestroy() {
        stopSelf();
        stopForeground(false);
        mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        super.onDestroy();
    }
}