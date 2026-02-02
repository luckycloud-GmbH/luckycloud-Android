package com.seafile.seadroid2.ui.adapter;

import android.util.Log;
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
import com.seafile.seadroid2.data.SeafShareableGroup;
import com.seafile.seadroid2.ui.NavContext;
import com.seafile.seadroid2.ui.activity.AccountsActivity;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SeafShareableGroupAdapter extends BaseAdapter {

    private static final String DEBUG_TAG = "SeafShareableGroupAdapter";
    private ArrayList<SeafShareableGroup> seafShareableGroups;
    private ArrayList<SeafShareableGroup> allShareableGroups;
    private ArrayList<SeafShareableGroup> selectedShareableGroups;
    private ShareDialogActivity mActivity;
    private NavContext nav;
    private DataManager dataManager;
    private String search = "";

    public SeafShareableGroupAdapter(ShareDialogActivity activity) {
        mActivity = activity;
        seafShareableGroups = Lists.newArrayList();
        allShareableGroups = Lists.newArrayList();
        selectedShareableGroups = Lists.newArrayList();
    }


    @Override
    public int getCount() {
        return seafShareableGroups.size();
    }

    @Override
    public boolean isEmpty() {
        return seafShareableGroups.isEmpty();
    }

    public void add(SeafShareableGroup entry) {
        seafShareableGroups.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public SeafShareableGroup getItem(int position) {
        return seafShareableGroups.get(position);
    }

    public void setItems(List<SeafShareableGroup> items1, List<SeafShareableGroup> items2) {
        allShareableGroups.clear();
        allShareableGroups.addAll(items1);
        selectedShareableGroups.clear();
        selectedShareableGroups.addAll(items2);
        setSearchString("");
    }

    public void setSearchString(String s) {
        search = s.toLowerCase();
        seafShareableGroups.clear();
        for (SeafShareableGroup item: allShareableGroups) {
            if (selectedShareableGroups.contains(item)) {
                seafShareableGroups.add(item);
            } else {
                if (item.getName().toLowerCase(Locale.ROOT).contains(search)) {
                    seafShareableGroups.add(item);
                }
            }
        }
        notifyChanged();
    }

    public ArrayList<SeafShareableGroup> getSelectedShareableGroups() {
        return selectedShareableGroups;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        seafShareableGroups.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.share_item_group, null);
        CheckBox groupCheckbox = (CheckBox) view.findViewById(R.id.group_checkbox);

        SeafShareableGroup item = seafShareableGroups.get(position);
        groupCheckbox.setText(item.getName());
        groupCheckbox.setChecked(selectedShareableGroups.contains(item));
        groupCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SeafShareableGroup item = seafShareableGroups.get(position);
                if (selectedShareableGroups.contains(item)) {
                    selectedShareableGroups.remove(item);
                } else {
                    selectedShareableGroups.add(item);
                }
                setSearchString(search);
            }
        });
        return view;
    }
}

