package com.seafile.seadroid2.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.ui.activity.SeafilePathChooserActivity;

import java.util.Collections;
import java.util.List;

public class DirentsAdapter extends BaseAdapter {

    private List<SeafDirent> dirents;
    private Context mContext;

    public DirentsAdapter(Context context) {
        dirents = Lists.newArrayList();
        mContext = context;
    }

    /** sort files type */
    public static final int SORT_BY_NAME = 9;
    /** sort files type */
    public static final int SORT_BY_LAST_MODIFIED_TIME = 10;
    /** sort files order */
    public static final int SORT_ORDER_ASCENDING = 11;
    /** sort files order */
    public static final int SORT_ORDER_DESCENDING = 12;

    @Override
    public int getCount() {
        return dirents.size();
    }

    @Override
    public boolean isEmpty() {
        return dirents.isEmpty();
    }

    public void add(SeafDirent entry) {
        dirents.add(entry);
    }


    @Override
    public SeafDirent getItem(int position) {
        return dirents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clearDirents() {
        dirents.clear();
    }

    public void setDirents(List<SeafDirent> dirents) {
        clearDirents();
        this.dirents.addAll(dirents);
        notifyDataSetChanged();
    }

    public void sortFiles(int type, int order) {
        List<SeafDirent> folders = Lists.newArrayList();
        List<SeafDirent> files = Lists.newArrayList();

        for (SeafDirent item : dirents) {
            if (item.isDir()) {
                folders.add(item);
            } else {
                files.add(item);
            }
        }

        dirents.clear();

        // sort SeafDirents
        if (type == SORT_BY_NAME) {
            // sort by name, in ascending order
            Collections.sort(folders, new SeafDirent.DirentNameComparator());
            Collections.sort(files, new SeafDirent.DirentNameComparator());
            if (order == SORT_ORDER_DESCENDING) {
                Collections.reverse(folders);
                Collections.reverse(files);
            }
        } else if (type == SORT_BY_LAST_MODIFIED_TIME) {
            // sort by last modified time, in ascending order
            Collections.sort(folders, new SeafDirent.DirentLastMTimeComparator());
            Collections.sort(files, new SeafDirent.DirentLastMTimeComparator());
            if (order == SORT_ORDER_DESCENDING) {
                Collections.reverse(folders);
                Collections.reverse(files);
            }
        }
        // Adds the objects in the specified collection to this ArrayList
        dirents.addAll(folders);
        dirents.addAll(files);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;
        SeafDirent dirent = dirents.get(position);

        if (convertView == null) {
            view = LayoutInflater.from(mContext).
                    inflate(R.layout.list_item_dirent, null);
            CardView viewCard = (CardView) view.findViewById(R.id.view_card);
            TextView title = (TextView) view.findViewById(R.id.list_item_title);
            TextView subtitle = (TextView) view.findViewById(R.id.list_item_subtitle);
            ImageView icon = (ImageView) view.findViewById(R.id.list_item_icon);
            ImageView image = (ImageView) view.findViewById(R.id.list_item_image);
            viewHolder = new ViewHolder(viewCard, title, subtitle, icon, image);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(dirent.getTitle());
        viewHolder.subtitle.setText(dirent.getSubtitle());
        viewHolder.icon.setImageResource(dirent.getIcon());
        viewHolder.viewCard.setOnClickListener(v -> ((SeafilePathChooserActivity)mContext).onListItemClick(position));
        viewHolder.image.setVisibility(dirent.isDir() ? View.VISIBLE : View.GONE);

        float alpha;
        int titleColor;

        if (dirent.isDir()) {
            alpha = 1;
//            titleColor = Color.BLACK;
        } else {
            alpha = (float) 75 / 255;
//            titleColor = Color.GRAY;
        }
//        viewHolder.title.setTextColor(titleColor);
        viewHolder.subtitle.setTextColor(Color.GRAY);
        viewHolder.icon.setAlpha(alpha);

        return view;
    }

    private static class ViewHolder {
        CardView viewCard;
        TextView title, subtitle;
        ImageView icon, image;

        public ViewHolder(CardView viewCard, TextView title, TextView subtitle, ImageView icon, ImageView image) {
            super();
            this.viewCard = viewCard;
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
            this.image = image;
        }
    }
}