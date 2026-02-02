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
import com.seafile.seadroid2.ui.activity.BrowserActivity;

import java.util.List;

public class SeafTagColorAdapter2 extends RecyclerView.Adapter<SeafTagColorAdapter2.MyViewHolder> {

    private static final String DEBUG_TAG = "SeafTagColorAdapter";
    private List<SeafFileTag> seafFileTags;
    private String repoID;
    private SeafDirent dirent;
    private BrowserActivity mActivity;
    private SeafFileTagAdapter.FragmentType fragmentType;

    public SeafTagColorAdapter2(BrowserActivity activity, String seafRepoID, SeafDirent seafDirent, SeafFileTagAdapter.FragmentType fragmentType) {
        mActivity = activity;
        seafFileTags = Lists.newArrayListWithCapacity(0);
        repoID = seafRepoID;
        dirent = seafDirent;
        seafFileTags.addAll(dirent.getFileTags());
        this.fragmentType = fragmentType;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView colorCard1, colorCard11, colorCard2, colorCard22;
        MyViewHolder(View view) {
            super(view);
            colorCard1 = view.findViewById(R.id.color_card1);
            colorCard11 = view.findViewById(R.id.color_card11);
            colorCard2 = view.findViewById(R.id.color_card2);
            colorCard22 = view.findViewById(R.id.color_card22);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_color_item2, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int position) {
        viewHolder.colorCard22.setOnClickListener(v -> {
            mActivity.getReposFragment().selectFileTagsDialog(repoID, dirent, fragmentType);
        });
        viewHolder.colorCard11.setOnClickListener(v -> {
            viewHolder.colorCard22.callOnClick();
        });
        if (position == 0) {
            viewHolder.colorCard1.setVisibility(View.GONE);
            SeafFileTag item = seafFileTags.get(position);
            viewHolder.colorCard22.setCardBackgroundColor(Color.parseColor(item.getTag_color()));
            return;
        }
        if (position == seafFileTags.size()) {
            viewHolder.colorCard2.setVisibility(View.GONE);
            SeafFileTag item = seafFileTags.get(position - 1);
            viewHolder.colorCard11.setCardBackgroundColor(Color.parseColor(item.getTag_color()));
            return;
        }
        SeafFileTag preItem = seafFileTags.get(position - 1);
        SeafFileTag item = seafFileTags.get(position);
        viewHolder.colorCard11.setCardBackgroundColor(Color.parseColor(preItem.getTag_color()));
        viewHolder.colorCard22.setCardBackgroundColor(Color.parseColor(item.getTag_color()));
    }
    @Override
    public int getItemCount() {
        if (seafFileTags.size() == 0) return 0;
        return seafFileTags.size() + 1;
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        seafFileTags.clear();
    }
}

