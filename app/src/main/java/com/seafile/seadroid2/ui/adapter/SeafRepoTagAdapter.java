package com.seafile.seadroid2.ui.adapter;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafCachedFile;
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.data.SeafGroup;
import com.seafile.seadroid2.data.SeafItem;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.data.SeafRepoTag;
import com.seafile.seadroid2.data.ServerInfo;
import com.seafile.seadroid2.gallery.Image;
import com.seafile.seadroid2.transfer.DownloadTaskInfo;
import com.seafile.seadroid2.ui.NavContext;
import com.seafile.seadroid2.ui.WidgetUtils;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.fragment.ReposFragment;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.GlideApp;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeafRepoTagAdapter extends BaseAdapter {

    private static final String DEBUG_TAG = "SeafRepoTagAdapter";
    private ArrayList<SeafRepoTag> items;
    private BrowserActivity mActivity;
    private DataManager dataManager;
    private NavContext nav;
    private SeafRepo repo;

    public SeafRepoTagAdapter(BrowserActivity activity, NavContext navContext, SeafRepo seafRepo) {
        mActivity = activity;
        nav = navContext;
        repo = seafRepo;
        dataManager = mActivity.getDataManager();
        items = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return items.size() + 2;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean isEnabled(int position) {
        return position != 0;
    }

    public void addEntry(SeafRepoTag entry) {
        items.add(entry);
        // Collections.sort(items);
        notifyChanged();
    }

    public void add(SeafRepoTag entry) {
        items.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public SeafRepoTag getItem(int position) {
        return items.get(position);
    }

    public void setItems(List<SeafRepoTag> tags) {
        items.clear();
        items.addAll(tags);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        items.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Viewholder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.tag_repo_item, null);
            CardView tagCard = (CardView) view.findViewById(R.id.tag_card);
            ImageView tagCheckImage = (ImageView) view.findViewById(R.id.tag_check_image);
            CardView addCard = (CardView) view.findViewById(R.id.add_card);
            viewHolder = new Viewholder(tagCard, tagCheckImage, addCard);
            view.setTag(viewHolder);
        } else {
            viewHolder = (Viewholder) convertView.getTag();
        }

        if (position == 0) {
            viewHolder.tagCard.setVisibility(View.GONE);
            viewHolder.addCard.setVisibility(View.INVISIBLE);
            return view;
        } else if (position == (items.size() + 1)) {
            viewHolder.tagCard.setVisibility(View.GONE);
            viewHolder.addCard.setVisibility(View.VISIBLE);
            viewHolder.addCard.setOnClickListener(v -> {
                mActivity.getReposFragment().showAddRepoTagDialog(repo.getID(), false);
            });
            return view;
        }

        viewHolder.tagCard.setVisibility(View.VISIBLE);
        viewHolder.addCard.setVisibility(View.GONE);

        SeafRepoTag item = items.get(position - 1);

        viewHolder.tagCard.setCardBackgroundColor(Color.parseColor(item.getTag_color()));
        if (repo.selectedRepoTagIDs.contains(item.getRepo_tag_id())) {
            viewHolder.tagCheckImage.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tagCheckImage.setVisibility(View.GONE);
        }

        viewHolder.tagCard.setOnClickListener(v -> {
            if (repo.selectedRepoTagIDs.contains(item.getRepo_tag_id())) {
                repo.selectedRepoTagIDs.remove(item.getRepo_tag_id());
            } else {
                repo.selectedRepoTagIDs.add(item.getRepo_tag_id());
            }
            notifyChanged();
            mActivity.getReposFragment().getAdapter().sortFiles();
            mActivity.getReposFragment().getAdapter().notifyChanged();
        });

        return view;
    }

    private class Viewholder {
        CardView tagCard, addCard;
        ImageView tagCheckImage;

        public Viewholder(CardView tagCard,
                          ImageView tagCheckImage,
                          CardView addCard

        ) {
            super();
            this.tagCard = tagCard;
            this.addCard = addCard;
            this.tagCheckImage = tagCheckImage;
        }
    }
}

