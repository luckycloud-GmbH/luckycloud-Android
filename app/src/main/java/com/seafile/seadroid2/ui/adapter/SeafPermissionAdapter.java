package com.seafile.seadroid2.ui.adapter;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafCachedFile;
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.data.SeafGroup;
import com.seafile.seadroid2.data.SeafItem;
import com.seafile.seadroid2.data.SeafPermission;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.transfer.DownloadTaskInfo;
import com.seafile.seadroid2.ui.NavContext;
import com.seafile.seadroid2.ui.WidgetUtils;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;
import com.seafile.seadroid2.util.GlideApp;
import com.seafile.seadroid2.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeafPermissionAdapter extends BaseAdapter {

    private static final String DEBUG_TAG = "SeafPermissionAdapter";
    private ArrayList<SeafPermission> items;
    private ShareDialogActivity mActivity;
    private NavContext nav;
    private DataManager dataManager;

    public SeafPermissionAdapter(ShareDialogActivity activity) {
        mActivity = activity;
        items = Lists.newArrayList();
    }


    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void add(SeafPermission entry) {
        items.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public SeafPermission getItem(int position) {
        return items.get(position);
    }

    public void setItems(List<SeafPermission> seafPermissions) {
        items.clear();
        items.addAll(seafPermissions);
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
        View view = LayoutInflater.from(mActivity).inflate(R.layout.share_item_permission, null);
        TextView permissionName = (TextView) view.findViewById(R.id.permission_name_text);
        TextView permissionDescription = (TextView) view.findViewById(R.id.permission_description_text);

        SeafPermission item = items.get(position);
        permissionName.setText(item.getName());
        permissionDescription.setText(item.getDescription());
        return view;
    }
}

