package com.seafile.seadroid2.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafPermission;
import com.seafile.seadroid2.ui.NavContext;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SeafPermission2Adapter extends BaseAdapter {

    private static final String DEBUG_TAG = "SeafPermissionAdapter";
    private ArrayList<SeafPermission> seafPermissions;
    private String selectedPermissionId;
    private ShareDialogActivity mActivity;
    private NavContext nav;
    private DataManager dataManager;

    public SeafPermission2Adapter(ShareDialogActivity activity) {
        mActivity = activity;
        seafPermissions = Lists.newArrayList();
    }


    @Override
    public int getCount() {
        return seafPermissions.size();
    }

    @Override
    public boolean isEmpty() {
        return seafPermissions.isEmpty();
    }

    public void add(SeafPermission entry) {
        seafPermissions.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public SeafPermission getItem(int position) {
        return seafPermissions.get(position);
    }

    public void setItems(List<SeafPermission> items, String item) {
        seafPermissions.clear();
        seafPermissions.addAll(items);
        selectedPermissionId = item;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        seafPermissions.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.share_item_permission2, null);
        TextView nameText = view.findViewById(R.id.name_text);
        TextView descText = view.findViewById(R.id.desc_text);

        SeafPermission item = seafPermissions.get(position);
        nameText.setText(item.getName());
        descText.setText(item.getDescription());

        if (selectedPermissionId.equals(item.getId())) {
            nameText.setTextColor(mActivity.getResources().getColor(R.color.luckycloud_green));
            descText.setTextColor(mActivity.getResources().getColor(R.color.luckycloud_green));
        }
        return view;
    }
}

