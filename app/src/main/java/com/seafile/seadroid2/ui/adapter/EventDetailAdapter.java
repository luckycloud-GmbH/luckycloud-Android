package com.seafile.seadroid2.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.joanzapata.iconify.fonts.MaterialCommunityIcons;
import com.joanzapata.iconify.widget.IconTextView;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.data.EventDetailsFileItem;

import java.util.List;

public class EventDetailAdapter extends BaseAdapter {
    private List<EventDetailsFileItem> items;
    private Context context;

    public EventDetailAdapter(Context context, List<EventDetailsFileItem> items) {
        this.items = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (contentView == null) {
            holder = new ViewHolder();
            contentView = View.inflate(context, R.layout.list_item_event_detail, null);
            holder.file = (TextView) contentView.findViewById(R.id.tv_diff_file_name);
            holder.icon = (IconTextView) contentView.findViewById(R.id.tv_diff_icon);
            contentView.setTag(holder);
        } else {
            holder = (ViewHolder) contentView.getTag();
        }

        final EventDetailsFileItem eventDetailsFileItem = items.get(position);

        holder.file.setText(eventDetailsFileItem.getPath());
        switch (eventDetailsFileItem.geteType()) {
            case FILE_ADDED:
            case DIR_ADDED:
                holder.file.setTextColor(context.getResources().getColor(R.color.luckycloud_green));
                holder.icon.setText("{" + MaterialCommunityIcons.mdi_plus.key() + " @color/luckycloud_green}");
                holder.icon.setTextColor(context.getResources().getColor(R.color.luckycloud_green));
                break;
            case FILE_MODIFIED:
                holder.file.setTextColor(context.getResources().getColor(R.color.lucky_yellow));
                holder.icon.setText("{" + MaterialCommunityIcons.mdi_pencil.key() + " #D6CC55}");
                break;
            case FILE_RENAMED:
                holder.file.setTextColor(context.getResources().getColor(R.color.lucky_black));
                holder.icon.setText("{" + MaterialCommunityIcons.mdi_arrow_right.key() + " #292929}");
                break;
            case FILE_DELETED:
            case DIR_DELETED:
                holder.file.setTextColor(context.getResources().getColor(R.color.red));
                holder.icon.setText("{" + MaterialCommunityIcons.mdi_minus.key() + " #E54B68}");
                break;
        }

        return contentView;
    }

    static class ViewHolder{
        public TextView file;
        public IconTextView icon;
    }
}
