package com.seafile.seadroid2.ui.adapter;

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
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.data.SeafFileTag;
import com.seafile.seadroid2.ui.dialog.GalleryDialog;

import java.util.List;

public class SeafTagColorAdapter3 extends RecyclerView.Adapter<SeafTagColorAdapter3.MyViewHolder> {

    private static final String DEBUG_TAG = "SeafTagColorAdapter";
    private List<SeafFileTag> seafFileTags;
    private SeafDirent dirent;
    private GalleryDialog mFragment;

    public SeafTagColorAdapter3(GalleryDialog fragment) {
        mFragment = fragment;
        seafFileTags = Lists.newArrayListWithCapacity(0);
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
                .inflate(R.layout.tag_color_item3, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int position) {
        SeafFileTag item = seafFileTags.get(position);
        viewHolder.colorCard.setOnClickListener(v -> {
            mFragment.mTagsBtn.callOnClick();
        });
        viewHolder.colorCard.setCardBackgroundColor(Color.parseColor(item.getTag_color()));
        viewHolder.colorCheckImage.setVisibility(View.GONE);
    }
    @Override
    public int getItemCount() {
        return seafFileTags.size();
    }

    public void addEntry(SeafFileTag entry) {
        seafFileTags.add(entry);
        // Collections.sort(repoTags);
        notifyChanged();
    }

    public void add(SeafFileTag entry) {
        seafFileTags.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    public void setItems(List<SeafFileTag> tags) {
        seafFileTags.clear();
        seafFileTags.addAll(tags);
    }

    public void setDirent(SeafDirent seafDirent) {
        dirent = seafDirent;
        seafFileTags.clear();
        seafFileTags.addAll(dirent.getFileTags());
        notifyChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        seafFileTags.clear();
    }
}

