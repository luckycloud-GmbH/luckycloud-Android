package com.seafile.seadroid2.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.config.GlideLoadConfig;
import com.seafile.seadroid2.transfer.DownloadTaskInfo;
import com.seafile.seadroid2.transfer.TransferTaskInfo;
import com.seafile.seadroid2.transfer.UploadTaskInfo;
import com.seafile.seadroid2.ui.WidgetUtils;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.fragment.TransferTaskFragment;
import com.seafile.seadroid2.util.GlideApp;
import com.seafile.seadroid2.util.Utils;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter class for both uploading and downloading tasks
 */
public class TransferTaskAdapter extends BaseAdapter {

    private static final String DEBUG_TAG = "TransferTaskAdapter";

    private SparseBooleanArray mSelectedItemsIds;
    private List<Integer> mSelectedItemsPositions = Lists.newArrayList();
    private List<? extends TransferTaskInfo> mTransferTaskInfos;
    private List<? extends TransferTaskInfo> showTransferTaskInfos;
    /** flag to mark if action mode was activated, used to update the state of multi selection buttons */
    private boolean actionModeStarted;
    private BrowserActivity mActivity;
    private TransferTaskFragment mFragment;
    private int transferType;
    private Map<String, Bitmap> uploadBitmapMap;

    /**
     * Constructor of {@link TransferTaskAdapter}
     * <p>
     * set {@link TransferTaskAdapter #mDownloadTaskInfos} to null if the task is a uploading task</br>
     * set {@link TransferTaskAdapter #mUploadTaskInfos} to null if the task is a downloading task </br>
     * set {@link TransferTaskAdapter #mTransferTaskType} 0 to mark as Download Task, 1 mark to mark as Upload Task</br>
     *
     * @param activity
     */
    public TransferTaskAdapter(BrowserActivity activity) {
        this.mActivity = activity;
        this.mSelectedItemsIds = new SparseBooleanArray();
        mTransferTaskInfos = Lists.newArrayList();
        showTransferTaskInfos = Lists.newArrayList();
        uploadBitmapMap = new HashMap<>();
    }

    public List<? extends TransferTaskInfo> getTransferTaskInfos() {
        return mTransferTaskInfos;
    }

    public void setTransferTaskInfos(List<? extends TransferTaskInfo> infos) {
        if (mTransferTaskInfos.size() != infos.size())
            deselectAllItems();
        mTransferTaskInfos = infos;


        setShowTransferTaskInfos();
    }

    public boolean equalLists(List<? extends TransferTaskInfo> newList, List<? extends TransferTaskInfo> oldList) {
        if (newList == null && oldList == null)
            return true;

        if ((newList == null && oldList != null)
                || newList != null && oldList == null
                || newList.size() != oldList.size())
            return false;

        return compareTransferTaskInfos(newList, oldList);
    }

    public void setShowTransferTaskInfos() {
        List<TransferTaskInfo> showInfos = Lists.newArrayList();
        for (TransferTaskInfo info: mTransferTaskInfos) {
            switch (transferType) {
                case SettingsManager.TRANSFER_UPLOAD:
                    if (!info.isDownloadTask)
                        showInfos.add(info);
                    break;
                case SettingsManager.TRANSFER_DOWNLOAD:
                    if (info.isDownloadTask)
                        showInfos.add(info);
                    break;
                default:
                    showInfos.add(info);
                    break;
            }
        }

        if (mFragment == null)
            mFragment = mActivity.getTransferFragment();
        mFragment.showEmptyView(showInfos.isEmpty());

        showTransferTaskInfos = showInfos;
    }

    private boolean compareTransferTaskInfos(List<? extends TransferTaskInfo> infos1, List<? extends TransferTaskInfo> infos2) {
        if (infos1 == null && infos2 == null)
            return true;
        if (infos1 == null || infos2 == null)
            return false;
        if (infos1.size() != infos2.size())
            return false;
        for(int i = 0; i < infos1.size(); i++) {
            TransferTaskInfo info1 = infos1.get(i);
            TransferTaskInfo info2 = infos2.get(i);
            if (info1.toString().equals(info2.toString()) && info1.state == info2.state) {
                long transferedSize1 = 0;
                if (info1.isDownloadTask) {
                    transferedSize1 = ((DownloadTaskInfo)info1).finished;
                } else {
                    transferedSize1 = ((UploadTaskInfo)info1).uploadedSize;
                }
                long transferedSize2 = 0;
                if (info1.isDownloadTask) {
                    transferedSize2 = ((DownloadTaskInfo)info2).finished;
                } else {
                    transferedSize2 = ((UploadTaskInfo)info2).uploadedSize;
                }
                if (transferedSize1 != transferedSize2) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    public void setTransferType(int type) {
        transferType = type;
        setShowTransferTaskInfos();
    }

    @Override
    public boolean hasStableIds() {
        // make adapter with stable ids by return true.
        // Also in {@link #getItemId} must either override hashCode() or has some kind of id field to be returned
        return true;
    }

    @Override
    public int getCount() {
        return showTransferTaskInfos.size();
    }

    @Override
    public boolean isEmpty() {
        return showTransferTaskInfos.isEmpty();
    }

    @Override
    public TransferTaskInfo getItem(int position) {
        return showTransferTaskInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void updateTaskView(TransferTaskInfo info, Viewholder viewHolder) {
        String stateStr = "";
        int stateColor = mActivity.getResources().getColor(R.color.luckycloud_green);
        long totalSize = 0l;
        long transferedSize = 0l;
        if (info.isDownloadTask) {
            DownloadTaskInfo dti = (DownloadTaskInfo) info;
            totalSize = dti.fileSize;
            transferedSize = dti.finished;
        } else if (!info.isDownloadTask) {
            UploadTaskInfo uti = (UploadTaskInfo) info;
            totalSize = uti.totalSize;
            transferedSize = uti.uploadedSize;
        }
        String sizeStr = Utils.readableFileSize(totalSize).toString();

        switch (info.state) {
        case INIT:
            if (info.isDownloadTask)
                stateStr = mActivity.getString(R.string.wait_downloading);
            else if (!info.isDownloadTask)
                stateStr = mActivity.getString(R.string.wait_uploading);
            stateColor = mActivity.getResources().getColor(R.color.red);
            viewHolder.fileSize.setVisibility(View.INVISIBLE);
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.fileProcess.setVisibility(View.INVISIBLE);
            viewHolder.processLayout.setVisibility(View.INVISIBLE);
            break;
        case TRANSFERRING:
            int percent;
            if (totalSize == 0)
                percent = 0;
            else
                percent = (int) (transferedSize * 100 / totalSize);

            viewHolder.progressBar.setProgress(percent);
            viewHolder.fileProcess.setText(percent + "%");
            sizeStr = String.format("%s / %s",
                                    Utils.readableFileSize(transferedSize),
                                    Utils.readableFileSize(totalSize));
            viewHolder.fileSize.setVisibility(View.VISIBLE);
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.fileProcess.setVisibility(View.VISIBLE);
            viewHolder.processLayout.setVisibility(View.VISIBLE);
            if (info.isDownloadTask)
                stateStr = mActivity.getString(R.string.downloading);
            else if (!info.isDownloadTask)
                stateStr = mActivity.getString(R.string.uploading);
            stateColor = mActivity.getResources().getColor(R.color.lucky_yellow);
            break;
        case FINISHED:
            if (info.isDownloadTask)
                stateStr = mActivity.getString(R.string.file_downloaded);
            else if (!info.isDownloadTask)
                stateStr = mActivity.getString(R.string.file_uploaded);
            stateColor = mActivity.getResources().getColor(R.color.luckycloud_green);
            viewHolder.fileSize.setVisibility(View.VISIBLE);
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.fileProcess.setVisibility(View.INVISIBLE);
            viewHolder.processLayout.setVisibility(View.INVISIBLE);
            break;
        case CANCELLED:
            if (info.isDownloadTask)
                stateStr = mActivity.getString(R.string.download_cancelled);
            else if (!info.isDownloadTask)
                stateStr = mActivity.getString(R.string.upload_cancelled);
            stateColor = mActivity.getResources().getColor(R.color.red);
            viewHolder.fileSize.setVisibility(View.INVISIBLE);
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.fileProcess.setVisibility(View.INVISIBLE);
            viewHolder.processLayout.setVisibility(View.INVISIBLE);
            break;
        case FAILED:
            if (info.isDownloadTask)
                stateStr = mActivity.getString(R.string.download_failed);
            else if (!info.isDownloadTask)
                stateStr = mActivity.getString(R.string.upload_failed);
            stateColor = mActivity.getResources().getColor(R.color.red);
            viewHolder.fileSize.setVisibility(View.INVISIBLE);
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.fileProcess.setVisibility(View.INVISIBLE);
            viewHolder.processLayout.setVisibility(View.INVISIBLE);
            break;
        }
        viewHolder.fileSize.setText(Utils.readableFileSize(totalSize));
        viewHolder.state.setText(stateStr);
        viewHolder.stateCard.setCardBackgroundColor(stateColor);
    }

    public int getCheckedItemCount() {
        return mSelectedItemsIds.size();
    }

    public List<Integer> getSelectedIds() {
        return mSelectedItemsPositions;
    }

    public void toggleSelection(int position) {
        if (mSelectedItemsIds.get(position)) {
            // unselected
            mSelectedItemsIds.delete(position);
            mSelectedItemsPositions.remove(Integer.valueOf(position));
        } else {
            mSelectedItemsIds.put(position, true);
            mSelectedItemsPositions.add(position);
        }

        mActivity.onItemSelected();
        notifyChanged();
    }

    public void actionModeOn() {
        actionModeStarted = true;
        notifyChanged();
    }

    public void actionModeOff() {
        actionModeStarted = false;
        notifyChanged();
    }

    public void deselectAllItems() {
        mSelectedItemsIds.clear();
        mSelectedItemsPositions.clear();
        notifyChanged();
    }

    public void selectAllItems() {
        mSelectedItemsIds.clear();
        mSelectedItemsPositions.clear();
        for (int i = 0; i < showTransferTaskInfos.size(); i++) {
            mSelectedItemsIds.put(i, true);
            mSelectedItemsPositions.add(i);
        }
        notifyChanged();
    }
    
    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final Viewholder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.transfer_list_item, null);
            CardView viewCard = (CardView)view.findViewById(R.id.view_card);
            ImageView icon = (ImageView)view.findViewById(R.id.transfer_file_icon);
            View imageLayout = view.findViewById(R.id.transfer_file_image_layout);
            ImageView image = view.findViewById(R.id.transfer_file_icon_image);
            View fileLayout = view.findViewById(R.id.transfer_file_layout);
            ImageView fileIcon = view.findViewById(R.id.transfer_file_icon_with_extension);
            TextView fileText = view.findViewById(R.id.transfer_file_icon_text);
            ImageView multiSelectBtn = (ImageView)view.findViewById(R.id.transfer_file_multi_select_btn);
            CardView stateCard = (CardView) view.findViewById(R.id.transfer_file_state_card);
            TextView state = (TextView)view.findViewById(R.id.transfer_file_state);
            TextView targetPath = (TextView)view.findViewById(R.id.transfer_target_path);
            TextView fileName = (TextView)view.findViewById(R.id.transfer_file_name);
            TextView fileSize = (TextView)view.findViewById(R.id.transfer_file_size);
            TextView fileProcess = (TextView)view.findViewById(R.id.transfer_file_process);
            LinearLayout processLayout = (LinearLayout)view.findViewById(R.id.transfer_process_layout);
            CardView stopCard = (CardView)view.findViewById(R.id.transfer_stop_card);
            ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.transfer_file_progress_bar);
            TextView startTimeText = (TextView)view.findViewById(R.id.transfer_start_time_text);
            viewHolder = new Viewholder(viewCard, icon, imageLayout, image, fileLayout, fileIcon, fileText, multiSelectBtn, state, targetPath, fileName, fileSize, progressBar, stateCard, fileProcess, processLayout, stopCard, startTimeText);
            view.setTag(viewHolder);
        } else {
            viewHolder = (Viewholder) convertView.getTag();
        }

        int iconID;
        TransferTaskInfo info = showTransferTaskInfos.get(position);
        if (info.isDownloadTask) {
            final DownloadTaskInfo taskInfo = (DownloadTaskInfo) showTransferTaskInfos.get(position);
            iconID = Utils.getFileIcon(taskInfo.pathInRepo);
            // the three fields are not dynamic
            viewHolder.icon.setImageResource(iconID);
            viewHolder.targetPath.setText(Utils.pathJoin(taskInfo.repoName, Utils.getParentPath(taskInfo.pathInRepo)));
            viewHolder.fileName.setText(Utils.fileNameFromPath(taskInfo.pathInRepo));
            // Log.d(DEBUG_TAG, "multi select btn checked " + mSelectedItemsIds.get(position));

        } else if (!info.isDownloadTask) {
            UploadTaskInfo taskInfo = (UploadTaskInfo) showTransferTaskInfos.get(position);
            iconID = Utils.getFileIcon(taskInfo.localFilePath);
            String fullpath = Utils.pathJoin(taskInfo.repoName, taskInfo.parentDir);
            // the three fileds is not dynamic
            viewHolder.icon.setImageResource(iconID);
            viewHolder.targetPath.setText(fullpath);
            viewHolder.fileName.setText(Utils.fileNameFromPath(taskInfo.localFilePath));
            // Log.d(DEBUG_TAG, "multi select btn checked " + mSelectedItemsIds.get(position));
        }

        String title = viewHolder.fileName.getText().toString();
        viewHolder.icon.setTag(R.id.imageloader_uri, title);
        viewHolder.startTimeText.setText(Utils.translateTime(info.startTime));

        if (Utils.isViewableImage(title)) {
            if (info.isDownloadTask) {
                setDownloadImage(info, title, viewHolder);
            } else {
                setUploadImage(info, title, viewHolder);
            }
        } else {
            setIcon(title, viewHolder);
        }

        viewHolder.multiSelectBtn.setVisibility(actionModeStarted? View.VISIBLE : View.GONE);
        viewHolder.multiSelectBtn.setImageResource(mSelectedItemsIds.get(position)? R.drawable.checkbox_checked : R.drawable.checkbox_unchecked);

        viewHolder.viewCard.setOnClickListener(v -> {
            mActivity.getTransferFragment().listItemClick(position);
        });
        viewHolder.viewCard.setOnLongClickListener(v -> {
            mActivity.getTransferFragment().listItemLongClick(position);
            return true;
        });

        viewHolder.stopCard.setOnClickListener(v -> {
            if (actionModeStarted) {
                mActivity.getTransferFragment().deselectItems();
            }
            mActivity.getTransferFragment().deleteSelectedItem(info);
        });

        updateTaskView(info, viewHolder);

        return view;
    }

    private void setDownloadImage(TransferTaskInfo info, String title, Viewholder viewHolder) {
        final DownloadTaskInfo taskInfo = (DownloadTaskInfo) info;
        String path = taskInfo.pathInRepo;
        String url = mActivity.getDataManager().getImageThumbnailLink(info.repoName, info.repoID,
                path, WidgetUtils.getThumbnailWidth(1));
        if (mActivity.getDataManager().getCachedRepoByID(info.repoID) == null) {
            url = null;
        } else {
            if (mActivity.getDataManager().getCachedRepoByID(info.repoID).encrypted) {
                url = null;
            }
        }
        if (url == null) {
            setIcon(title, viewHolder);
        } else {
            viewHolder.icon.setVisibility(View.GONE);
            viewHolder.imageLayout.setVisibility(View.VISIBLE);
            viewHolder.fileLayout.setVisibility(View.INVISIBLE);

            GlideApp.with(mActivity)
                    .asFile()
                    .load(GlideLoadConfig.getGlideUrl(url))
                    .override(1024, 1024)
                    .apply(GlideLoadConfig.getOptions("", 1))
                    .thumbnail(0.1f)
                    .into(new Target<File>() {
                              @Override
                              public void onLoadStarted(@Nullable Drawable placeholder) {}
                              @Override
                              public void onLoadFailed(@Nullable Drawable errorDrawable) {}
                              @Override
                              public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                  String tag = (String) viewHolder.icon.getTag(R.id.imageloader_uri);
                                  if (tag.equals(title)) {
                                      Bitmap bitmap = Utils.getBitmapFromFile(resource);
                                      if (bitmap != null) {
                                          viewHolder.image.setImageBitmap(bitmap);
                                      }
                                  } else {
                                      setIcon(title, viewHolder);
                                  }
                              }
                              @Override
                              public void onLoadCleared(@Nullable Drawable placeholder) {}
                              @Override
                              public void getSize(@NonNull SizeReadyCallback cb) {}
                              @Override
                              public void removeCallback(@NonNull SizeReadyCallback cb) {}
                              @Override
                              public void setRequest(@Nullable Request request) {}
                              @Nullable
                              @Override
                              public Request getRequest() { return null; }
                              @Override
                              public void onStart() {}
                              @Override
                              public void onStop() {}
                              @Override
                              public void onDestroy() {}
                          }
                    );
//            GlideApp.with(mActivity)
//                    .asBitmap()
//                    .load(glideUrl)
//                    .override(1024, 1024)
//                    .apply(opt)
//                    .listener(new RequestListener<Bitmap>() {
//                        @Override
//                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//                            String tag = (String) viewHolder.icon.getTag(R.id.imageloader_uri);
//                            if (tag.equals(title)) {
//                                viewHolder.image.setImageBitmap(resource);
//                                return false;
//                            } else {
//                                setIcon(title, viewHolder);
//                            }
//                            return true;
//                        }
//                    })
//                    .into(viewHolder.icon);
        }
    }

    private void setUploadImage(TransferTaskInfo info, String title, Viewholder viewHolder) {
        UploadTaskInfo taskInfo = (UploadTaskInfo) info;
        String path = taskInfo.localFilePath;
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

    private void setIcon(String title, Viewholder viewHolder) {
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

    private class Viewholder {
        ImageView multiSelectBtn;
        TextView targetPath, fileName, fileSize, state, fileProcess;
        CardView viewCard, stateCard, stopCard;
        ProgressBar progressBar;
        LinearLayout processLayout;
        ImageView icon;
        View imageLayout;
        ImageView image;
        View fileLayout;
        ImageView fileIcon;
        TextView fileText, startTimeText;

        public Viewholder(CardView viewCard, ImageView icon, View imageLayout, ImageView image, View fileLayout, ImageView fileIcon, TextView fileText, ImageView multiSelectBtn, TextView state, TextView targetPath,
                          TextView fileName, TextView fileSize, ProgressBar progressBar, CardView stateCard, TextView fileProcess, LinearLayout processLayout, CardView stopCard, TextView startTimeText) {
            super();
            this.viewCard = viewCard;
            this.icon = icon;
            this.imageLayout = imageLayout;
            this.image = image;
            this.fileLayout = fileLayout;
            this.fileIcon = fileIcon;
            this.fileText = fileText;
            this.multiSelectBtn = multiSelectBtn;
            this.state = state;
            this.stateCard = stateCard;
            this.fileProcess = fileProcess;
            this.targetPath = targetPath;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.progressBar = progressBar;
            this.processLayout = processLayout;
            this.stopCard = stopCard;
            this.startTimeText = startTimeText;
        }
    }
}