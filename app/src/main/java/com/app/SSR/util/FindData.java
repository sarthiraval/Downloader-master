package com.app.SSR.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.app.SSR.interfaces.GetData;
import com.app.SSR.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

public class FindData {

    private Context context;
    private GetData getData;

    public FindData(Context context, GetData getData) {
        this.context = context;
        this.getData = getData;
    }

    public void data(String stringData) {

        ArrayList<String> arrayList = new ArrayList<>();

        if (stringData.matches("https://www.instagram.com/(.*)")) {
            String[] data = stringData.split(Pattern.quote("?"));
            String string = data[0];
            if (isNetworkAvailable()) {
                if (Method.isDownload) {
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.addHeader("Accept", "application/json");
                    client.addHeader("Content-Type", "application/json;charset=UTF-8");
                    client.addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
                    client.addHeader("x-requested-with", "XMLHttpRequest");
                    client.get(string + "?__a=1&__d=dis", null, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String res = new String(responseBody);

                            try {
                                JSONObject jsonObject = new JSONObject(res);

                                String link = null;

                                JSONObject objectGraphql = jsonObject.getJSONObject("graphql");
                                JSONObject objectMedia = objectGraphql.getJSONObject("shortcode_media");
                                boolean isVideo = objectMedia.getBoolean("is_video");
                                if (isVideo) {
                                    link = objectMedia.getString("video_url");
                                } else {
                                    link = objectMedia.getString("display_url");
                                }

                                arrayList.add(link);

                                try {
                                    JSONObject objectSidecar = objectMedia.getJSONObject("edge_sidecar_to_children");
                                    JSONArray jsonArray = objectSidecar.getJSONArray("edges");

                                    arrayList.clear();

                                    String edgeSidecar = null;

                                    for (int i = 0; i < jsonArray.length(); i++) {

                                        JSONObject object = jsonArray.getJSONObject(i);
                                        JSONObject node = object.getJSONObject("node");
                                        boolean is_video_group = node.getBoolean("is_video");
                                        if (is_video_group) {
                                            edgeSidecar = node.getString("video_url");
                                        } else {
                                            edgeSidecar = node.getString("display_url");
                                        }
                                        arrayList.add(edgeSidecar);

                                    }

                                } catch (Exception e) {
                                    Log.e("error_show", e.toString());
                                }

                                getData.getData(arrayList, "", true);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                getData.getData(arrayList, context.getResources().getString(R.string.not_support), false);
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            getData.getData(arrayList, context.getResources().getString(R.string.wrong), false);
                        }
                    });
                } else {
                    getData.getData(arrayList, context.getResources().getString(R.string.download_msg), false);
                }
            } else {
                getData.getData(arrayList, context.getResources().getString(R.string.internet_connection), false);
            }
        } else {
            getData.getData(arrayList, context.getResources().getString(R.string.not_support), false);
        }
    }

    //network check
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
