package com.seafile.seadroid2.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.transfer.DownloadTaskInfo;
import com.seafile.seadroid2.transfer.TaskState;
import com.seafile.seadroid2.transfer.TransferTaskInfo;
import com.seafile.seadroid2.transfer.UploadTaskInfo;
import com.seafile.seadroid2.ui.adapter.TransferTaskAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Download tasks fragments
 */
public class TransferFragment extends TransferTaskFragment {
    private static final String DEBUG_TAG = "TransferFragment";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity.callOnNewIntent();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected List<? extends TransferTaskInfo> getTransferTaskInfos() {
        List<TransferTaskInfo> infos = Lists.newArrayList();

        addIfNotNull(infos, mActivity.getDataManager().getDownloadsFromDB());
        addIfNotNull(infos, mActivity.getDataManager().getUploadsFromDB());
        addIfNotNull(infos, txService.getAllDownloadTaskInfos());
        addIfNotNull(infos, txService.getAllUploadTaskInfos());

        Map<String, TransferTaskInfo> unique = new HashMap<>();
        for (TransferTaskInfo info : infos) {
            if (info.isDownloadTask) {
                unique.put(DownloadTaskInfo.toString((DownloadTaskInfo)info), info);
            } else {
                unique.put(UploadTaskInfo.toString((UploadTaskInfo)info), info);
            }
        }
        infos = new ArrayList<>(unique.values());

        Collections.sort(infos, new TransferTaskInfo.TaskInfoComparator());
        Collections.sort(infos, new TransferTaskInfo.TransferTaskStartTimeComparator());

        return infos;
    }

    private void addIfNotNull(List<TransferTaskInfo> list, List<? extends TransferTaskInfo> items) {
        if (items != null) list.addAll(items);
    }

    @Override
    protected void setUpTransferList() {

        Log.d(DEBUG_TAG, "bind TransferService");
        adapter = new TransferTaskAdapter(mActivity);
        adapter.setTransferType(SettingsManager.instance().getTransferTypePref());
        adapter.setTransferTaskInfos(getTransferTaskInfos());
        mTransferTaskListView.setDivider(null);
        mTransferTaskListView.setAdapter(adapter);

    }

    protected boolean isNeedUpdateProgress() {
        return !txService.getAllDownloadTaskInfos().isEmpty() && !txService.getAllUploadTaskInfos().isEmpty();
    }

    /**
     * retry all failed tasks
     */
    public void retryAllFailedTasks() {
        if (txService != null) {
            txService.restartAllDownloadTasksByState(TaskState.FAILED);
            txService.restartAllUploadTasksByState(TaskState.FAILED);
        }
    }

    /**
     * restart all cancelled tasks
     */
    public void restartAllCancelledTasks() {
        if (txService != null) {
            txService.restartAllDownloadTasksByState(TaskState.CANCELLED);
            txService.restartAllUploadTasksByState(TaskState.CANCELLED);
        }
    }

    /**
     * remove all failed download tasks
     */
    public void removeAllFailedTasks() {
        if (txService != null) {
            txService.removeAllDownloadTasksByState(TaskState.FAILED);
            txService.removeAllUploadTasksByState(TaskState.FAILED);
        }
    }

    /**
     * remove all {@link TaskState#FINISHED}, {@link TaskState#FAILED} and {@link TaskState#CANCELLED} download tasks.
     */
    public void removeAllTasks() {
        if (txService != null) {
            txService.removeAllDownloadTasksByState(TaskState.FINISHED);
            txService.removeAllDownloadTasksByState(TaskState.FAILED);
            txService.removeAllDownloadTasksByState(TaskState.CANCELLED);

            txService.removeAllUploadTasksByState(TaskState.FINISHED);
            txService.removeAllUploadTasksByState(TaskState.FAILED);
            txService.removeAllUploadTasksByState(TaskState.CANCELLED);
        }

        mActivity.getDataManager().clearDownloadAndUploadCache();
    }

    /**
     * remove all finished download tasks
     */
    public void removeAllFinishedDownloadTasks() {
        if (txService != null) {
            txService.removeAllDownloadTasksByState(TaskState.FINISHED);
            txService.removeAllUploadTasksByState(TaskState.FINISHED);
        }
    }

    /**
     * cancel all download tasks
     */
    public void cancelAllDownloadUploadTasks() {
        if (txService != null) {
            txService.cancellAllDownloadTasks();
            txService.cancelAllUploadTasks();
        }
    }

    /**
     * cancel tasks by ids
     */
    public void cancelDownloadTasksByIds(List<Integer> ids) {
        if (txService != null) {
            txService.cancellDownloadTasksByIds(ids);
        }
    }

    private void cancelUploadTasksByIds(List<Integer> ids) {
        if (txService != null) {
            txService.cancelUploadTasksByIds(ids);
        }
    }

    /**
     * remove cancelled tasks by Ids
     */
    public void removeDownloadTasksByIds(List<Integer> ids) {
        if (txService != null) {
            txService.removeDownloadTasksByIds(ids);
        }
    }

    private void removeUploadTasksByIds(List<Integer> ids) {
        if (txService != null) {
            txService.removeUploadTasksByIds(ids);
        }

    }

    /**
     * restart tasks by Ids
     */
    public void restartDownloadTasksByIds(List<Integer> ids) {
        if (txService != null) {
            txService.restartDownloadTasksByIds(ids);
        }
    }

    public void restartUploadTasksByIds(List<Integer> ids) {
        if (txService != null) {
            txService.restartUploadTasksByIds(ids);
        }
    }

    public void deleteSelectedItem(TransferTaskInfo task) {
        List<TransferTaskInfo> tasks = Lists.newArrayList();
        tasks.add(task);
        deleteSelectedItems(tasks);
    }


    @Override
    protected void deleteSelectedItems(List<TransferTaskInfo> tasks) {
        List<Integer> downloadIds = Lists.newArrayList();
        List<Integer> uploadIds = Lists.newArrayList();
        for (TransferTaskInfo task: tasks) {
            if (task.isDownloadTask) {
                downloadIds.add(task.taskID);
            }
            if (!task.isDownloadTask) {
                uploadIds.add(task.taskID);
            }
        }

        cancelDownloadTasksByIds(downloadIds);
        removeDownloadTasksByIds(downloadIds);

        cancelUploadTasksByIds(uploadIds);
        removeUploadTasksByIds(uploadIds);
    }

    @Override
    protected void restartSelectedItems(List<TransferTaskInfo> tasks) {
        List<Integer> downloadIds = Lists.newArrayList();
        List<Integer> uploadIds = Lists.newArrayList();
        for (TransferTaskInfo task: tasks) {
            if (task.isDownloadTask) {
                downloadIds.add(task.taskID);
            }
            if (!task.isDownloadTask) {
                uploadIds.add(task.taskID);
            }
        }

        restartDownloadTasksByIds(downloadIds);
        removeDownloadTasksByIds(downloadIds);

        restartUploadTasksByIds(uploadIds);
        removeUploadTasksByIds(uploadIds);
    }

}
