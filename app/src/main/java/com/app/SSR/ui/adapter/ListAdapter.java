package com.app.SSR.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.app.SSR.interfaces.OnClick;
import com.app.SSR.R;
import com.app.SSR.util.Method;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private Method method;
    private String string;
    private Activity activity;
    private int columnWidth;
    private List<File> stringList;

    public ListAdapter(Activity activity, List<File> stringList, String string, OnClick onClick) {
        this.activity = activity;
        this.stringList = stringList;
        this.string = string;
        method = new Method(activity, onClick);
        columnWidth = method.getScreenWidth();
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(activity).inflate(R.layout.list_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.imageView.setLayoutParams(new ConstraintLayout.LayoutParams(columnWidth / 2, columnWidth / 2));
        Glide.with(activity).load(stringList.get(position).toString())
                .placeholder(R.drawable.place_holder)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(v -> method.onClick(position, string,""));

    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_list_adapter);

        }
    }
}
