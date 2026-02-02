package com.seafile.seadroid2.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupWindow;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.account.AccountManager;
import com.seafile.seadroid2.account.Authenticator;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafShareUser;
import com.seafile.seadroid2.data.SeafShareableGroup;
import com.seafile.seadroid2.ui.NavContext;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SeafShareUserAdapter extends BaseAdapter {

    private static final String DEBUG_TAG = "SeafShareUserAdapter";
    private ArrayList<SeafShareUser> seafShareUsers;
    private ArrayList<SeafShareUser> allShareUsers;
    private ArrayList<SeafShareUser> selectedShareUsers;
    private ShareDialogActivity mActivity;
    private PopupWindow mSelectUsersDropdown;
    private int userAdapterHeight;
    private View selectGroupsLayout;
    private NavContext nav;
    private DataManager dataManager;
    private String search = "";
    private String ownerEmail = "";

    public SeafShareUserAdapter(ShareDialogActivity activity, PopupWindow dropdown, int height, View layout) {
        mActivity = activity;
        mSelectUsersDropdown = dropdown;
        userAdapterHeight = height;
        selectGroupsLayout = layout;
        AccountManager accountManager = new AccountManager(mActivity);
        try {
            ownerEmail = accountManager.getCurrentAccount().email;
        } catch (Exception e) {
            e.printStackTrace();
        }
        seafShareUsers = Lists.newArrayList();
        selectedShareUsers = Lists.newArrayList();
    }


    @Override
    public int getCount() {
        return seafShareUsers.size();
    }

    @Override
    public boolean isEmpty() {
        return seafShareUsers.isEmpty();
    }

    public void add(SeafShareUser entry) {
        seafShareUsers.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public SeafShareUser getItem(int position) {
        return seafShareUsers.get(position);
    }

    public void setItems(List<SeafShareUser> items) {
        seafShareUsers.clear();
        seafShareUsers.addAll(items);
        selectedShareUsers.clear();
        selectedShareUsers.addAll(items);
        notifyChanged();
        updatePopupHeight(selectedShareUsers.size());
    }

    public void addItems(List<SeafShareUser> items) {
        seafShareUsers.clear();
        seafShareUsers.addAll(selectedShareUsers);
        List<SeafShareUser> itemsAgain = Lists.newArrayList();
        for (SeafShareUser item: items) {
            if (item.getEmail().equals(ownerEmail)) {
                continue;
            }
            boolean flag = true;
            for (SeafShareUser item1: selectedShareUsers) {
                if (item.toString().equals(item1.toString())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                itemsAgain.add(item);
            }
        }
        seafShareUsers.addAll(itemsAgain);
        updatePopupHeight(selectedShareUsers.size() + itemsAgain.size());
        notifyChanged();
    }

    public ArrayList<SeafShareUser> getSelectedShareUsers() {
        return selectedShareUsers;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        seafShareUsers.clear();
        seafShareUsers.addAll(selectedShareUsers);
        notifyChanged();
        updatePopupHeight(selectedShareUsers.size());
    }

    public void updatePopupHeight(int size) {
        mSelectUsersDropdown.update(selectGroupsLayout, 5, 5, selectGroupsLayout.getMeasuredWidth(), userAdapterHeight * Math.min(size, 5) + 290);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.share_item_group, null);
        CheckBox groupCheckbox = (CheckBox) view.findViewById(R.id.group_checkbox);

        SeafShareUser item = seafShareUsers.get(position);
        groupCheckbox.setText(item.getName());
        groupCheckbox.setChecked(selectedShareUsers.contains(item));
        groupCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SeafShareUser item = seafShareUsers.get(position);
                if (selectedShareUsers.contains(item)) {
                    selectedShareUsers.remove(item);
                } else {
                    selectedShareUsers.add(item);
                }
                notifyChanged();
            }
        });
        return view;
    }
}

