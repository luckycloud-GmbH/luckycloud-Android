package com.seafile.seadroid2.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeafTimeAdapter extends BaseAdapter {

    private static final String DEBUG_TAG = "SeafPermissionAdapter";
    private ArrayList<String> times;
    private String selectedTime;
    private ShareDialogActivity mActivity;

    public SeafTimeAdapter(ShareDialogActivity activity) {
        mActivity = activity;
        times = new ArrayList<String>(Arrays.asList(

        ));
    }


    @Override
    public int getCount() {
        return times.size();
    }

    @Override
    public boolean isEmpty() {
        return times.isEmpty();
    }

    public void add(String entry) {
        times.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public String getItem(int position) {
        return times.get(position);
    }

    public void setItems(List<String> items, String item) {
        times.clear();
        times.addAll(items);
        selectedTime = item;
    }

    public void setSelectedTime(String item) {
        selectedTime = item;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        times.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.share_item_time, null);
        TextView timeText = view.findViewById(R.id.time_text);

        String time = times.get(position);
        timeText.setText(time);

        if (time.equals(selectedTime)) {
            timeText.setTextColor(mActivity.getResources().getColor(R.color.luckycloud_green));
        } else {
            timeText.setTextColor(mActivity.getResources().getColor(R.color.text_view_secondary_color));
        }
        return view;
    }
}

