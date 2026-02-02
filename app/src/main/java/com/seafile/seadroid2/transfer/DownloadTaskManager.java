package com.seafile.seadroid2.transfer;

import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.notification.DownloadNotificationProvider;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Download task manager
 * <p/>
 */
public class DownloadTaskManager extends TransferManager implements DownloadStateListener {
    private static final String DEBUG_TAG = "DownloadTaskManager";

    public static final String BROADCAST_FILE_DOWNLOAD_SUCCESS = "downloaded";
    public static final String BROADCAST_FILE_DOWNLOAD_FAILED = "downloadFailed";
    public static final String BROADCAST_FILE_DOWNLOAD_PROGRESS = "downloadProgress";

    public static final int DOWNLOAD_LIMIT = 100;

    private static DownloadNotificationProvider mNotifProvider;

    /**
     * Add a new download task.
     * call this method to execute a task immediately.
     */
    public int addTask(Account account, String repoName, String repoID, String path, long fileSize, boolean offlineAvailable, boolean thumbnail) {
        TransferTask task = new DownloadTask(++notificationID, account, repoName, repoID, path, offlineAvailable, thumbnail, -1, this);
        task.totalSize = fileSize;
        TransferTask oldTask = null;
        if (allTaskList.containsValue(task)) {
            oldTask = allTaskList.get(task.taskID);
        }
        if (oldTask != null) {
            if (oldTask.getState().equals(TaskState.CANCELLED)
                    || oldTask.getState().equals(TaskState.FAILED)
                    || oldTask.getState().equals(TaskState.FINISHED)) {
                allTaskList.remove(oldTask);
            } else {
                // return taskID of old task
                return oldTask.getTaskID();
            }
        }
        if (!thumbnail)
            Utils.utilsLogInfo(true,
                SeadroidApplication.getAppContext().getString(R.string.start_downloading) +
                        "\n\n" +
                        Utils.fileNameFromPath(path));
        allTaskList.put(task.getTaskID(),task);
        ConcurrentAsyncTask.execute(task);
        return task.getTaskID();
    }

    public void addTaskToQue(Account account, String repoName, String repoID, String path, boolean offlineAvailable, boolean thumbnail, long size) {
        // create a new one to avoid IllegalStateException
        DownloadTask downloadTask = new DownloadTask(++notificationID, account, repoName, repoID, path, offlineAvailable, thumbnail, size, this);
        addTaskToQue(downloadTask);
        if (!thumbnail) {
            Utils.utilsLogInfo(true,
                    SeadroidApplication.getAppContext().getString(R.string.start_downloading) +
                            "\n\n" +
                            Utils.fileNameFromPath(path));
        }
    }

    public int getDownloadingFileCountByPath(String repoID, String dir) {
        List<DownloadTaskInfo> downloadTaskInfos = getTaskInfoListByPath(repoID, dir);
        int count = 0;
        for (DownloadTaskInfo downloadTaskInfo : downloadTaskInfos) {
            if (downloadTaskInfo.state.equals(TaskState.INIT)
                    || downloadTaskInfo.state.equals(TaskState.TRANSFERRING))
                count++;
        }
        return count;
    }

    /**
     * get all download task info under a specific directory.
     *
     * @param repoID
     * @param dir
     * @return List<DownloadTaskInfo>
     */
    public List<DownloadTaskInfo> getTaskInfoListByPath(String repoID, String dir) {
        ArrayList<DownloadTaskInfo> infos = Lists.newArrayList();
        Iterator<Map.Entry<Integer, TransferTask>> iterator = allTaskList.entrySet().iterator();

        try {
            while (iterator.hasNext()) {
                Map.Entry<Integer, TransferTask> next = iterator.next();
                TransferTask value = next.getValue();
                if (value.getRepoID().equals(repoID)) {

                    String parentDir = Utils.getParentPath(value.getPath());
                    if (parentDir.equals(dir)) {
                        DownloadTaskInfo info = ((DownloadTask) value).getTaskInfo();
                        if (info.thumbnail) {
                            if (info.state == TaskState.FINISHED) {
                                infos.add(info);
                            }
                        } else {
                            infos.add(info);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return infos;
    }

    /**
     * get all download task info under a specific repo.
     *
     * @param repoID
     * @return List<DownloadTaskInfo>
     */
    public List<DownloadTaskInfo> getTaskInfoListByRepo(String repoID) {
        ArrayList<DownloadTaskInfo> infos = Lists.newArrayList();
        Iterator<Map.Entry<Integer, TransferTask>> iterator = allTaskList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, TransferTask> next = iterator.next();
            TransferTask value = next.getValue();
            if (value.getRepoID().equals(repoID)) {
                infos.add(((DownloadTask) value).getTaskInfo());
            }
        }

        return infos;
    }

    public void retry(int taskID) {
        DownloadTask task = (DownloadTask) getTask(taskID);
        if (task == null || !task.canRetry())
            return;
        addTaskToQue(task.getAccount(), task.getRepoName(), task.getRepoID(), task.getPath(), task.getOfflineAvailable(), task.getThumbnail(), task.getSize());
    }

    private void notifyProgress(int taskID) {
        DownloadTaskInfo info = (DownloadTaskInfo) getTaskInfo(taskID);
        if (info == null)
            return;

        if (mNotifProvider != null && !info.thumbnail)
            mNotifProvider.updateNotification();
    }

    public void saveNotifProvider(DownloadNotificationProvider provider) {
        mNotifProvider = provider;
    }

    public boolean hasNotifProvider() {
        return mNotifProvider != null;
    }

    public DownloadNotificationProvider getNotifProvider() {
        if (hasNotifProvider())
            return mNotifProvider;
        else
            return null;
    }

    public void cancelAllDownloadNotification() {
        if (mNotifProvider != null)
            mNotifProvider.cancelNotification();
    }

    // -------------------------- listener method --------------------//
    @Override
    public void onFileDownloadProgress(int taskID) {
        Intent localIntent = new Intent(BROADCAST_ACTION).putExtra("type",
                BROADCAST_FILE_DOWNLOAD_PROGRESS).putExtra("taskID", taskID);
        LocalBroadcastManager.getInstance(SeadroidApplication.getAppContext()).sendBroadcast(localIntent);
        notifyProgress(taskID);
    }

    @Override
    public void onFileDownloaded(int taskID) {
        remove(taskID);
        doNext();
        Intent localIntent = new Intent(BROADCAST_ACTION).putExtra("type",
                BROADCAST_FILE_DOWNLOAD_SUCCESS).putExtra("taskID", taskID);
        LocalBroadcastManager.getInstance(SeadroidApplication.getAppContext()).sendBroadcast(localIntent);
        try {
            DownloadTaskInfo info = (DownloadTaskInfo) getTaskInfo(taskID);
            if (!info.thumbnail)
                Utils.utilsLogInfo(true,
                    SeadroidApplication.getAppContext().getString(R.string.downloaded) +
                            "\n\n" +
                            Utils.fileNameFromPath(info.localFilePath));
        } catch (Exception ignored) {

        }
        notifyProgress(taskID);
    }

    @Override
    public void onFileDownloadFailed(int taskID) {
        remove(taskID);
        doNext();
        Intent localIntent = new Intent(BROADCAST_ACTION).putExtra("type",
                BROADCAST_FILE_DOWNLOAD_FAILED).putExtra("taskID", taskID);
        LocalBroadcastManager.getInstance(SeadroidApplication.getAppContext()).sendBroadcast(localIntent);
        try {
            DownloadTaskInfo info = (DownloadTaskInfo) getTaskInfo(taskID);
            if (!info.thumbnail) {
                Utils.utilsLogInfo(true,
                        SeadroidApplication.getAppContext().getString(R.string.failed_download) +
                                "\n\n" +
                                Utils.fileNameFromPath(info.localFilePath));
            } else {
                retry(taskID);
                return;
            }

        } catch (Exception ignored) {}

        notifyProgress(taskID);
    }
}
