package com.seafile.seadroid2.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.data.SeafBackup;
import com.seafile.seadroid2.folderbackup.FolderBackupResultActivity;
import com.seafile.seadroid2.transfer.TransferTaskInfo;
import com.seafile.seadroid2.transfer.UploadTaskManager;
import com.seafile.seadroid2.ui.WidgetUtils;
import com.seafile.seadroid2.util.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter class for both uploading and downloading tasks
 */
public class SeafBackupAdapter extends BaseAdapter {

    private static final String DEBUG_TAG = "SeafBackupAdapter";

    private List<SeafBackup> seafBackups;
    private FolderBackupResultActivity mActivity;
    private Map<String, Bitmap> uploadBitmapMap;

    public SeafBackupAdapter(FolderBackupResultActivity activity) {
        this.mActivity = activity;
        seafBackups = Lists.newArrayList();
        uploadBitmapMap = new HashMap<>();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return seafBackups.size();
    }

    @Override
    public boolean isEmpty() {
        return seafBackups.isEmpty();
    }

    @Override
    public SeafBackup getItem(int position) {
        return seafBackups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<SeafBackup> seafBackups) {
        this.seafBackups = seafBackups;
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
                mActivity.getString(R.string.backup_failed);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final SeafBackup backup = seafBackups.get(position);
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
            viewHolder = new ChildViewHolder(viewCard, icon, imageLayout, image, fileLayout, fileIcon, fileText, stateCard, state, targetPath, sourcePath, fileName, fileSize, startTimeText, endTimeText, divider);
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

    private class ChildViewHolder {
        View divider;
        TextView fileName, targetPath, sourcePath, state, fileSize;
        CardView viewCard, stateCard;
        ImageView icon;
        View imageLayout;
        ImageView image;
        View fileLayout;
        ImageView fileIcon;
        TextView fileText;
        TextView startTimeText, endTimeText;

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