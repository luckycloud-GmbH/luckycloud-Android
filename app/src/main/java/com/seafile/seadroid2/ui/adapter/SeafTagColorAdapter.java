package com.seafile.seadroid2.ui.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.ui.activity.BrowserActivity;

import java.util.List;

public class SeafTagColorAdapter extends RecyclerView.Adapter<SeafTagColorAdapter.MyViewHolder> {

    private static final String DEBUG_TAG = "SeafTagColorAdapter";
    private List<String> tagColors;
    private String selectedTagColor;

    public SeafTagColorAdapter(List<String> colors) {
        tagColors = Lists.newArrayListWithCapacity(0);
        tagColors.addAll(colors);
        selectedTagColor = "";
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView colorCard;
        ImageView colorCheckImage;
        MyViewHolder(View view) {
            super(view);
            colorCard = view.findViewById(R.id.color_card);
            colorCheckImage = view.findViewById(R.id.color_check_image);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_color_item, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int position) {
        String item = tagColors.get(position);
        viewHolder.colorCard.setCardBackgroundColor(Color.parseColor(item));
        viewHolder.colorCheckImage.setVisibility(item.equals(selectedTagColor) ? View.VISIBLE : View.GONE);

        viewHolder.colorCard.setOnClickListener(v -> {
            selectedTagColor = item;
            notifyDataSetChanged();
        });
    }
    @Override
    public int getItemCount() {
        return tagColors.size();
    }

    public void addEntry(String entry) {
        tagColors.add(entry);
        // Collections.sort(repoTags);
        notifyChanged();
    }

    public void add(String entry) {
        tagColors.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    public void setItems(List<String> colors) {
        tagColors.clear();
        tagColors.addAll(colors);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        tagColors.clear();
    }

    public String getSelectedTagColor() {
        return selectedTagColor;
    }
}

