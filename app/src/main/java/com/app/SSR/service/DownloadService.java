package com.app.SSR.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.app.SSR.R;
import com.app.SSR.util.Constant;
import com.app.SSR.util.Events;
import com.app.SSR.util.GlobalBus;
import com.app.SSR.util.Method;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class DownloadService extends Service {

    private int position = 0;
    private RemoteViews rv;
    private OkHttpClient client;
    private final int CHANEL_ID = 105;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private static final String CANCEL_TAG = "c_tag";
    private final String NOTIFICATION_CHANNEL_ID = "download_service";
    public static final String ACTION_START = "com.download.action.START";
    public static final String ACTION_STOP = "com.download.action.STOP";

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NotNull Message message) {
            int progress = Integer.parseInt(message.obj.toString());
            switch (message.what) {
                case 1:
                    rv.setTextViewText(R.id.nf_title, getString(R.string.app_name));
                    rv.setProgressBar(R.id.progress, 100, progress, false);
                    rv.setTextViewText(R.id.nf_percentage, getResources().getString(R.string.downloading) + " " + "(" + progress + " %)");
                    notificationManager.notify(CHANEL_ID, builder.build());
                    break;
                case 2:
                    if (Constant.downloadArray.size() - 1 != position) {
                        position++;
                        init(Constant.downloadArray.get(position));
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.downloading), Toast.LENGTH_SHORT).show();
                        position = 0;
                        stopForeground(false);
                        stopSelf();
                        Events.AdapterNotify adapterNotify = new Events.AdapterNotify("");
                        GlobalBus.getBus().post(adapterNotify);
                        Method.isDownload = true;
                    }
                    break;
            }
            return false;
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_stat_ic_notification);
        builder.setTicker(getResources().getString(R.string.downloading));
        builder.setWhen(System.currentTimeMillis());
        builder.setOnlyAlertOnce(true);

        rv = new RemoteViews(getPackageName(), R.layout.my_custom_notification);
        rv.setTextViewText(R.id.nf_title, getString(R.string.app_name));
        rv.setProgressBar(R.id.progress, 100, 0, false);
        rv.setTextViewText(R.id.nf_percentage, getResources().getString(R.string.downloading) + " " + "(0%)");

        Intent intentClose = new Intent(this, DownloadService.class);
        intentClose.setAction(ACTION_STOP);
        int intentFlagType;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            intentFlagType = PendingIntent.FLAG_IMMUTABLE;  // or only use FLAG_MUTABLE >> if it needs to be used with inline replies or bubbles.
        } else {
            intentFlagType = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        PendingIntent closeIntent = PendingIntent.getService(this, 0, intentClose, intentFlagType);
        rv.setOnClickPendingIntent(R.id.nf_close, closeIntent);

        builder.setCustomContentView(rv);
        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getResources().getString(R.string.app_name);// The user-visible name of the channel.
            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        startForeground(CHANEL_ID, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(false);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            if (intent.getAction() != null && intent.getAction().equals(ACTION_START)) {
                Method.isDownload = false;
                init(Constant.downloadArray.get(position));
            }
            if (intent.getAction() != null && intent.getAction().equals(ACTION_STOP)) {
                if (client != null) {
                    for (Call call : client.dispatcher().runningCalls()) {
                        if (call.request().tag().equals(CANCEL_TAG))
                            call.cancel();
                    }
                }
                Method.isDownload = true;
                stopForeground(false);
                stopSelf();
            }
        } catch (Exception e) {
            Log.d("error", e.toString());
            stopForeground(false);
            stopSelf();
        }
        return START_STICKY;
    }


    public void init(final String downloadUrl) {

        final String iconsStoragePath = getExternalFilesDir(getResources().getString(R.string.download_folder_path)).toString();

        //Using Date class
        Date date = new Date();
        //Pattern for showing milliseconds in the time "SSS"
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String stringDate = sdf.format(date);

        //Using Calendar class
        Calendar cal = Calendar.getInstance();
        String s = sdf.format(cal.getTime());

        final String string;

        if (downloadUrl.contains(".jpg") || downloadUrl.contains(".webp")) {
            string = "Image-" + s + ".jpg";
        } else {
            string = "Image-" + s + ".mp4";
        }
        Log.d("file_name", string);

        new Thread(new Runnable() {
            @Override
            public void run() {

                client = new OkHttpClient();
                Request.Builder builder = new Request.Builder()
                        .url(downloadUrl)
                        .addHeader("Accept-Encoding", "identity")
                        .get()
                        .tag(CANCEL_TAG);

                Call call = client.newCall(builder.build());

                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("TAG", "=============onFailure===============");
                        e.printStackTrace();
                        Log.d("error_downloading", e.toString());
                        // Method.isDownload = true;
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        Log.e("TAG", "=============onResponse===============");
                        Log.e("TAG", "request headers:" + response.request().headers());
                        Log.e("TAG", "response headers:" + response.headers());
                        assert response.body() != null;
                        ResponseBody responseBody = ProgressHelper.withProgress(response.body(), new ProgressUIListener() {

                            //if you don't need this method, don't override this method. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressStart(long totalBytes) {
                                super.onUIProgressStart(totalBytes);
                                Log.e("TAG", "onUIProgressStart:" + totalBytes);
                            }

                            @Override
                            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                                Log.e("TAG", "=============start===============");
                                Log.e("TAG", "numBytes:" + numBytes);
                                Log.e("TAG", "totalBytes:" + totalBytes);
                                Log.e("TAG", "percent:" + percent);
                                Log.e("TAG", "speed:" + speed);
                                Log.e("TAG", "============= end ===============");
                                Message msg = mHandler.obtainMessage();
                                msg.what = 1;
                                msg.obj = (int) (100 * percent) + "";
                                mHandler.sendMessage(msg);
                            }

                            //if you don't need this method, don't override this method. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressFinish() {
                                super.onUIProgressFinish();
                                Log.e("TAG", "onUIProgressFinish:");
                                Message msg = mHandler.obtainMessage();
                                msg.what = 2;
                                msg.obj = 0 + "";
                                mHandler.sendMessage(msg);
                                if (string.contains(".jpg")) {
                                    if (Constant.imageArray != null) {
                                        Constant.imageArray.add(0, new File(iconsStoragePath + "/" + string));
                                    }
                                } else {
                                    if (Constant.videoArray != null) {
                                        Constant.videoArray.add(0, new File(iconsStoragePath + "/" + string));
                                    }
                                }
                                try {
                                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{iconsStoragePath + "/" + string},
                                            null,
                                            (path, uri) -> {

                                            });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });


                        try {

                            BufferedSource source = responseBody.source();
                            File outFile = new File(iconsStoragePath + "/" + string);
                            BufferedSink sink = Okio.buffer(Okio.sink(outFile));
                            source.readAll(sink);
                            sink.flush();
                            source.close();

                        } catch (Exception e) {
                            Log.d("show_data", e.toString());
                        }

                    }
                });
            }
        }).start();
    }
}
