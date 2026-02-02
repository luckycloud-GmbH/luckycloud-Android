package com.seafile.seadroid2.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.data.SeafBackup;
import com.seafile.seadroid2.transfer.UploadTaskManager;
import com.seafile.seadroid2.ui.WidgetUtils;
import com.seafile.seadroid2.folderbackup.FolderBackupResultActivity;
import com.seafile.seadroid2.util.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter class for both uploading and downloading tasks
 */
public class SeafBackupExpandableAdapter extends BaseExpandableListAdapter {

    private static final String DEBUG_TAG = "SeafBackupExpandableAdapter";

    private List<String> expandableTitleList;
    private HashMap<String, List<SeafBackup>> expandableBackups;
    private FolderBackupResultActivity mActivity;
    private Map<String, Bitmap> uploadBitmapMap;

    public SeafBackupExpandableAdapter(FolderBackupResultActivity activity) {
        this.mActivity = activity;
        expandableTitleList = Lists.newArrayList();
        expandableBackups = new HashMap<>();
        uploadBitmapMap = new HashMap<>();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return false;
    }

    @Override
    public Object getChild(int lstPosn, int expanded_ListPosition) {
        return this.expandableBackups.get(this.expandableTitleList.get(lstPosn)).get(expanded_ListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expanded_ListPosition) {
        return expanded_ListPosition;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableBackups.get(this.expandableTitleList.get(listPosition)).size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableTitleList.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableTitleList.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    public void setData(List<String> expandableTitleList, HashMap<String, List<SeafBackup>> expandableBackups) {
        this.expandableTitleList = expandableTitleList;
        this.expandableBackups = expandableBackups;
        notifyChanged();
    }

    private void updateBackupView(SeafBackup backup, ChildViewHolder viewHolder) {
        String stateStr = "";
        int stateColor = mActivity.getResources().getColor(R.color.luckycloud_green);
        long totalSize = backup.totalSize;

        switch (backup.state) {
            case UploadTaskManager.BROADCAST_FILE_UPLOAD_SUCCESS:
                stateStr = mActivity.getString(R.string.backup_succeed);
                stateColor = mActivity.getResources().getColor(R.color.luckycloud_green);
                viewHolder.fileSize.setVisibility(View.VISIBLE);
                break;
            case UploadTaskManager.BROADCAST_FILE_UPLOAD_CANCELLED:
                stateStr = mActivity.getString(R.string.backup_canceled);
                stateColor = mActivity.getResources().getColor(R.color.red);
                viewHolder.fileSize.setVisibility(View.INVISIBLE);
                break;
            case UploadTaskManager.BROADCAST_FILE_UPLOAD_FAILED:
                stateStr = mActivity.getString(R.string.backup_failed);
                stateColor = mActivity.getResources().getColor(R.color.red);
                viewHolder.fileSize.setVisibility(View.INVISIBLE);
                break;
        }
        viewHolder.fileSize.setText(Utils.readableFileSize(totalSize));
        viewHolder.state.setText(stateStr);
        viewHolder.stateCard.setCardBackgroundColor(stateColor);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        View view = convertView;
        final GroupViewHolder groupViewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.backup_list_group, null);
            TextView listTitleTextView = (TextView) view.findViewById(R.id.title_text);
            TextView listCountTextView = (TextView) view.findViewById(R.id.count_text);
            View divider = view.findViewById(R.id.divider);
            groupViewHolder = new GroupViewHolder(listTitleTextView, listCountTextView, divider);
            view.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }

        groupViewHolder.titleText.setText(listTitle);
        List<SeafBackup> backups = expandableBackups.get(listTitle);
        if (backups == null) {
            groupViewHolder.countText.setVisibility(View.GONE);
        } else {
            groupViewHolder.countText.setText(String.valueOf(backups.size()));
        }

        groupViewHolder.divider.setVisibility(listPosition == 0 ? View.GONE : View.VISIBLE);

        return view;
    }

    @Override
    public View getChildView(int lstPosn, final int expanded_ListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final SeafBackup backup = (SeafBackup) getChild(lstPosn, expanded_ListPosition);
        View view = convertView;
        final ChildViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.backup_list_item, null);
            CardView viewCard = (CardView)view.findViewById(R.id.view_card);
            ImageView icon = (ImageView)view.findViewById(R.id.backup_file_icon);
            View imageLayout = view.findViewById(R.id.backup_file_image_layout);
            ImageView image = view.findViewById(R.id.backup_file_icon_image);
            View fileLayout = view.findViewById(R.id.backup_file_layout);
            ImageView fileIcon = view.findViewById(R.id.backup_file_icon_with_extension);
            TextView fileText = view.findViewById(R.id.backup_file_icon_text);
            CardView stateCard = (CardView) view.findViewById(R.id.backup_file_state_card);
            TextView state = (TextView)view.findViewById(R.id.backup_file_state);
            TextView targetPath = (TextView)view.findViewById(R.id.backup_target_path);
            TextView sourcePath = (TextView)view.findViewById(R.id.backup_source_path);
            TextView fileName = (TextView)view.findViewById(R.id.backup_file_name);
            TextView fileSize = (TextView)view.findViewById(R.id.backup_file_size);
            TextView startTimeText = (TextView)view.findViewById(R.id.backup_start_time_text);
            TextView endTimeText = (TextView)view.findViewById(R.id.backup_end_time_text);
            View divider = view.findViewById(R.id.divider);
            viewHolder = new ChildViewHolder(
                    viewCard,
                    icon,
                    imageLayout,
                    image,
                    fileLayout,
                    fileIcon,
                    fileText,
                    stateCard,
                    state,
                    targetPath,
                    sourcePath,
                    fileName,
                    fileSize,
                    startTimeText,
                    endTimeText,
                    divider);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ChildViewHolder) convertView.getTag();
        }

        int iconID;
        iconID = Utils.getFileIcon(backup.sourcePath);
        String fullPath = Utils.pathJoin(backup.repoName, backup.parentDir);
        viewHolder.icon.setImageResource(iconID);
        viewHolder.targetPath.setText(fullPath);
        viewHolder.fileName.setText(backup.name);
        viewHolder.sourcePath.setText(Utils.pathSplit(backup.sourcePath, backup.name));

        String title = viewHolder.fileName.getText().toString();
        viewHolder.icon.setTag(R.id.imageloader_uri, title);
        viewHolder.startTimeText.setText(Utils.translateLuckyTime(backup.startTime));
        viewHolder.endTimeText.setText(Utils.translateLuckyTime(backup.endTime));

        if (Utils.isViewableImage(title)) {
            setImage(backup, title, viewHolder);
        } else {
            setIcon(title, viewHolder);
        }

        updateBackupView(backup, viewHolder);

        viewHolder.divider.setVisibility(expanded_ListPosition == 0 ? View.GONE : View.VISIBLE);

        return view;
    }

    private void setImage(SeafBackup backup, String title, ChildViewHolder viewHolder) {
        String path = backup.sourcePath;
        Bitmap myBitmap = uploadBitmapMap.get(path);
        if (myBitmap == null) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                myBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imgFile.getAbsolutePath()), WidgetUtils.getThumbnailWidth(1), WidgetUtils.getThumbnailWidth(1));
                uploadBitmapMap.put(path, myBitmap);
            }
        }

        String tag = (String) viewHolder.icon.getTag(R.id.imageloader_uri);
        if (myBitmap != null && tag.equals(title)) {
            viewHolder.icon.setVisibility(View.GONE);
            viewHolder.imageLayout.setVisibility(View.VISIBLE);
            viewHolder.fileLayout.setVisibility(View.INVISIBLE);

            viewHolder.image.setImageBitmap(myBitmap);
        } else {
            setIcon(title, viewHolder);
        }
    }

    private void setIcon(String title, ChildViewHolder viewHolder) {
        viewHolder.icon.setVisibility(View.GONE);
        viewHolder.imageLayout.setVisibility(View.INVISIBLE);
        viewHolder.fileLayout.setVisibility(View.VISIBLE);

        int icon = Utils.getFileIcon(title);

        if (icon == R.drawable.ic_file) {
            viewHolder.fileIcon.setVisibility(View.GONE);
            viewHolder.fileText.setVisibility(View.VISIBLE);
            viewHolder.fileText.setText(Utils.getFileExtension(title));
        } else {
            viewHolder.fileIcon.setVisibility(View.VISIBLE);
            viewHolder.fileText.setVisibility(View.GONE);
            viewHolder.fileIcon.setImageResource(icon);
        }
    }

    private class GroupViewHolder {
        TextView titleText;
        TextView countText;
        View divider;

        public GroupViewHolder(TextView titleText, TextView countText, View divider) {
            super();
            this.titleText = titleText;
            this.countText = countText;
            this.divider = divider;
        }
    }

    private class ChildViewHolder {
        View divider;
        TextView targetPath, sourcePath, fileName, fileSize, state;
        CardView viewCard, stateCard;
        ImageView icon;
        View imageLayout;
        ImageView image;
        View fileLayout;
        ImageView fileIcon;
        TextView fileText, startTimeText, endTimeText;

        public ChildViewHolder(
                CardView viewCard,
                ImageView icon,
                View imageLayout,
                ImageView image,
                View fileLayout,
                ImageView fileIcon,
                TextView fileText,
                CardView stateCard,
                TextView state,
                TextView targetPath,
                TextView sourcePath,
                TextView fileName,
                TextView fileSize,
                TextView startTimeText,
                TextView endTimeText,
                View divider) {
            super();
            this.viewCard = viewCard;
            this.icon = icon;
            this.imageLayout = imageLayout;
            this.image = image;
            this.fileLayout = fileLayout;
            this.fileIcon = fileIcon;
            this.fileText = fileText;
            this.stateCard = stateCard;
            this.state = state;
            this.targetPath = targetPath;
            this.sourcePath = sourcePath;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.startTimeText = startTimeText;
            this.endTimeText = endTimeText;
            this.divider = divider;
        }
    }
}