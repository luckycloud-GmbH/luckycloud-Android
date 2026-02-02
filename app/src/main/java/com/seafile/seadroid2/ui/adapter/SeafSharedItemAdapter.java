package com.seafile.seadroid2.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafPermission;
import com.seafile.seadroid2.data.SeafSharedItem;
import com.seafile.seadroid2.ui.NavContext;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SeafSharedItemAdapter extends BaseAdapter {

    private static final String DEBUG_TAG = "SeafPermissionAdapter";
    private ArrayList<SeafSharedItem> seafSharedItems;
    private ArrayList<SeafPermission> seafPermissions;
    private ShareDialogActivity mActivity;
    private NavContext nav;
    private DataManager dataManager;
    private Boolean isShareGroup;

    public SeafSharedItemAdapter(ShareDialogActivity activity) {
        mActivity = activity;
        seafSharedItems = Lists.newArrayList();
        seafPermissions = Lists.newArrayList();
    }


    @Override
    public int getCount() {
        return seafSharedItems.size();
    }

    @Override
    public boolean isEmpty() {
        return seafSharedItems.isEmpty();
    }

    public void add(SeafSharedItem entry) {
        seafSharedItems.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public SeafSharedItem getItem(int position) {
        return seafSharedItems.get(position);
    }

    public void setItems(List<SeafSharedItem> items1, List<SeafPermission> items2, boolean flag) {
        seafSharedItems.clear();
        seafSharedItems.addAll(items1);
        seafPermissions.clear();
        seafPermissions.addAll(items2);
        isShareGroup = flag;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        seafSharedItems.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.share_item_shared, null);
        TextView sharedNameText = (TextView) view.findViewById(R.id.shared_name_text);
        TextView sharedPermissionText = (TextView) view.findViewById(R.id.shared_permission_text);

        SeafSharedItem item = seafSharedItems.get(position);
        sharedNameText.setText(isShareGroup? item.getGroup_info_name() : item.getUser_info_nickname());

        for (SeafPermission seafPermission : seafPermissions) {
            if (seafPermission.getId().equals(item.getPermission()) || ("custom-" + seafPermission.getId()).equals(item.getPermission())) {
                sharedPermissionText.setText(seafPermission.getName());
            }
        }
        return view;
    }
}

