package com.seafile.seadroid2.ui.dialog;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Choose an app from a list of apps or custom actions
 */
public class AppChoiceDialog2 extends DialogFragment {
    private List<ResolveInfo> mAppInfos = Lists.newArrayList();
    private List<ResolveInfo> mShowAppInfos = Lists.newArrayList();
    private OnItemSelectedListener mListener;
    private String mTitle;
    private AppsListAdapter adapter;

    private List<CustomAction> customActions = Lists.newArrayList();
    private List<CustomAction> showCustomActions = Lists.newArrayList();

    public interface OnItemSelectedListener {
        void onAppSelected(ResolveInfo appInfo);
        void onCustomActionSelected(CustomAction action);
    }

    public void init(String title, List<ResolveInfo> appInfos, OnItemSelectedListener listener) {
        mAppInfos = appInfos;
        mShowAppInfos = Lists.newArrayList(appInfos);
        mListener = listener;
        mTitle = title;
    }

    public void addCustomAction(int id, Drawable icon, String description) {
        customActions.add(new CustomAction(id, icon, description));
        showCustomActions.add(new CustomAction(id, icon, description));
    }

    private void onAppSelected(int index) {
        dismiss();
        if (index < showCustomActions.size()) {
            CustomAction action = showCustomActions.get(index);
            if (mListener != null) {
                mListener.onCustomActionSelected(action);
            }
        } else {
            ResolveInfo info = mShowAppInfos.get(index - showCustomActions.size());
            if (mListener != null) {
                mListener.onAppSelected(info);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout view = (LinearLayout)inflater.inflate(R.layout.appchoice_dialog_layout, null);
        CardView closeCard = view.findViewById(R.id.close_card);

        TextView titleText = view.findViewById(R.id.title_text);
        titleText.setText(mTitle);

        EditText searchEdit = view.findViewById(R.id.et_content);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mShowAppInfos.clear();
                PackageManager pm = getActivity().getPackageManager();
                for (ResolveInfo info:mAppInfos) {
                    if (info.activityInfo.loadLabel(pm).toString().toLowerCase().contains(s.toString().toLowerCase())) {
                        mShowAppInfos.add(info);
                    }
                }

                showCustomActions.clear();
                for (CustomAction action:customActions) {
                    if (action.description.toLowerCase().contains(s.toString().toLowerCase())) {
                        showCustomActions.add(action);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ListView listView = view.findViewById(android.R.id.list);
        adapter = new AppsListAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            onAppSelected(position);
        });
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        closeCard.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }


    private class AppsListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return showCustomActions.size() + mShowAppInfos.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            if (position < showCustomActions.size()) {
                return showCustomActions.get(position);
            }
            return mShowAppInfos.get(position - showCustomActions.size());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            Viewholder viewHolder;

            if (convertView == null) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.app_list_item2, null);
                ImageView icon = (ImageView)view.findViewById(R.id.app_icon);
                TextView desc = (TextView)view.findViewById(R.id.app_desc);
                viewHolder = new Viewholder(icon, desc);
                view.setTag(viewHolder);
            } else {
                viewHolder = (Viewholder)convertView.getTag();
            }

            if (position < showCustomActions.size()) {
                setCustomAction(viewHolder, showCustomActions.get(position));
            } else {
                setAppInfo(viewHolder, mShowAppInfos.get(position - showCustomActions.size()));
            }

            return view;
        }

        private void setCustomAction(Viewholder viewHolder, CustomAction customAction) {
            viewHolder.icon.setImageDrawable(customAction.icon);
            viewHolder.desc.setText(customAction.description);
        }

        private void setAppInfo(Viewholder viewHolder, ResolveInfo info) {
            PackageManager pm = getActivity().getPackageManager();
            CharSequence appDesc = info.activityInfo.loadLabel(pm);
            Drawable appIcon = info.activityInfo.loadIcon(pm);

            viewHolder.icon.setImageDrawable(appIcon);
            viewHolder.desc.setText(appDesc);
        }

        private class Viewholder {
            ImageView icon;
            TextView desc;

            Viewholder(ImageView icon, TextView desc) {
                this.icon = icon;
                this.desc = desc;
            }
        }

    }

    public static class CustomAction {
        public final int id;
        public final Drawable icon;
        public final String description;

        public CustomAction(int id, Drawable icon, String description) {
            this.id = id;
            this.icon = icon;
            this.description = description;
        }
    }
}