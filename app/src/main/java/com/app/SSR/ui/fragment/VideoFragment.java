package com.app.SSR.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.SSR.R;
import com.app.SSR.ui.activity.Show;
import com.app.SSR.ui.adapter.ListAdapter;
import com.app.SSR.interfaces.OnClick;
import com.app.SSR.util.Constant;
import com.app.SSR.util.Events;
import com.app.SSR.util.GlobalBus;
import com.app.SSR.util.Method;
import com.google.android.material.textview.MaterialTextView;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class VideoFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private ProgressBar progressBar;
    private ListAdapter listAdapter;
    private RecyclerView recyclerView;
    private MaterialTextView textViewNoData;
    private LayoutAnimationController animation;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment, container, false);

        GlobalBus.getBus().register(this);

        int resId = R.anim.layout_animation_from_bottom;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        progressBar = view.findViewById(R.id.progressbar_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_fragment);
        textViewNoData = view.findViewById(R.id.textView_noData_fragment);
        textViewNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutAnimation(animation);

        onClick = (OnClick) (position, type, data) -> startActivity(new Intent(getActivity(), Show.class)
                .putExtra("position", position)
                .putExtra("type", type));
        method = new Method(getActivity(), onClick);

        new Video().execute();

        return view;

    }

    @Subscribe
    public void getNotify(Events.AdapterNotify adapterNotify) {
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        } else {
            setVideoData();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class Video extends AsyncTask<String, String, String> {

        Queue<File> files;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            files = new LinkedList<>(Arrays.asList(method.getDownload().listFiles()));
            Constant.videoArray.clear();
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                while (!files.isEmpty()) {
                    File file = files.remove();
                    if (file.isDirectory()) {
                        files.addAll(Arrays.asList(file.listFiles()));
                    } else if (file.getName().endsWith(".mp4")) {
                        Constant.videoArray.add(file);
                    }
                }
            } catch (Exception e) {
                Log.d("error", e.toString());
            }

            Collections.sort(Constant.videoArray);
            Collections.reverse(Constant.videoArray);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (Constant.videoArray.size() == 0) {
                textViewNoData.setVisibility(View.VISIBLE);
            } else {
                setVideoData();
            }

            progressBar.setVisibility(View.GONE);

            super.onPostExecute(s);
        }
    }

    private void setVideoData() {
        textViewNoData.setVisibility(View.GONE);
        listAdapter = new ListAdapter(getActivity(), Constant.videoArray, "video", onClick);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutAnimation(animation);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister the registered event.
        GlobalBus.getBus().unregister(this);
    }

}
