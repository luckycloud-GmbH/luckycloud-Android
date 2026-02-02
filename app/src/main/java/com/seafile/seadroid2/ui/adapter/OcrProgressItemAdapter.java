package com.seafile.seadroid2.ui.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.google.common.collect.Lists;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.data.EventDetailsFileItem;
import com.seafile.seadroid2.data.SeafEvent;
import com.seafile.seadroid2.data.SeafItem;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.fragment.ActivitiesFragment;
import com.seafile.seadroid2.ui.widget.CircleImageView;
import com.seafile.seadroid2.util.SystemSwitchUtils;
import com.seafile.seadroid2.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter for Activities tab
 */
public class OcrProgressItemAdapter extends BaseAdapter {
    public static final String DEBUG_TAG = OcrProgressItemAdapter.class.getSimpleName();

    private ArrayList<Integer> items;
    private Activity mActivity;

    public OcrProgressItemAdapter(Activity activity) {
        this.mActivity = activity;
        this.items = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }

    public void add(Integer entry) {
        items.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public Integer getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void init(int count) {
        items.clear();
        for (int i = 0; i < count; i++) {
            items.add(0);
        }
    }

    public void setItem(int index, int progress) {
        items.set(index, progress);
        notifyChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        View view = LayoutInflater.from(mActivity).inflate(R.layout.list_item_ocr_progress, null);

        TextView pageText = view.findViewById(R.id.progress_text);
        TextView progressText = view.findViewById(R.id.page_text);
        viewHolder = new ViewHolder(pageText, progressText);
        viewHolder.pageText.setText(mActivity.getResources().getString(R.string.page_text, position + 1));
        viewHolder.progressText.setText(mActivity.getResources().getString(
                R.string.detecting_text,
                items.get(position)));

        return view;
    }

    private static class ViewHolder {
        TextView progressText;
        TextView pageText;
        public ViewHolder(TextView progressText, TextView pageText) {
            super();
            this.progressText = progressText;
            this.pageText = pageText;
        }
    }

}
