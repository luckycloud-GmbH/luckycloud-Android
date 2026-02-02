package com.seafile.seadroid2.notification;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ServiceInfo;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.transfer.TaskState;
import com.seafile.seadroid2.transfer.TransferService;
import com.seafile.seadroid2.transfer.UploadTaskInfo;
import com.seafile.seadroid2.transfer.UploadTaskManager;
import com.seafile.seadroid2.ui.CustomNotificationBuilder;
import com.seafile.seadroid2.ui.activity.BrowserActivity;

import java.util.List;

/**
 * Upload notification provider
 *
 */
public class UploadNotificationProvider extends BaseNotificationProvider {

    public UploadNotificationProvider(UploadTaskManager uploadTaskManager,
                                      TransferService transferService) {
        super(uploadTaskManager, transferService);

    }

    @Override
    protected NotificationState getState() {
        if (txService == null)
            return NotificationState.NOTIFICATION_STATE_COMPLETED;

        int progressCount = 0;
        int errorCount = 0;

        for (UploadTaskInfo info : txService.getNoneCameraUploadTaskInfos()) {
            if (info == null) continue;
            if (info.state == TaskState.INIT || info.state == TaskState.TRANSFERRING)
                progressCount++;
            else if (info.state == TaskState.FAILED || info.state == TaskState.CANCELLED)
                errorCount++;
        }

        if (progressCount > 0) return NotificationState.NOTIFICATION_STATE_PROGRESS;
        if (errorCount > 0) return NotificationState.NOTIFICATION_STATE_COMPLETED_WITH_ERRORS;
        return NotificationState.NOTIFICATION_STATE_COMPLETED;
    }

    @Override
    protected int getProgress() {
        if (txService == null)
            return 0;

        long uploadedSize = 0;
        long totalSize = 0;

        for (UploadTaskInfo info : txService.getNoneCameraUploadTaskInfos()) {
            if (info == null) continue;
            uploadedSize += info.uploadedSize;
            totalSize += info.totalSize;
        }

        if (totalSize == 0)
            return 0;

        return (int) (uploadedSize * 100 / totalSize);
    }

    @Override
    protected String getProgressInfo(NotificationState state) {
        if (txService == null)
            return "";

        // failed or cancelled tasks won`t be shown in notification state
        // but failed or cancelled detailed info can be viewed in TransferList
        if (state != NotificationState.NOTIFICATION_STATE_PROGRESS) {
            return SeadroidApplication.getAppContext()
                    .getString(R.string.notification_upload_completed);
        }

        int uploadingCount = 0;
        List<UploadTaskInfo> infos = txService.getNoneCameraUploadTaskInfos();
        for (UploadTaskInfo info : infos) {
            if (info.state.equals(TaskState.INIT)
                    || info.state.equals(TaskState.TRANSFERRING))
                uploadingCount++;
        }
        if (uploadingCount != 0)
            return SeadroidApplication.getAppContext().getResources().
                    getQuantityString(R.plurals.notification_upload_info,
                            uploadingCount,
                            uploadingCount,
                            getProgress());
        return "";
    }

    @Override
    protected int getNotificationID() {
        return NOTIFICATION_ID_UPLOAD;
    }

    @Override
    protected String getNotificationTitle(NotificationState state) {
        return SeadroidApplication.getAppContext().getString(
                state == NotificationState.NOTIFICATION_STATE_PROGRESS
                        ? R.string.notification_upload_started_title
                        : R.string.notification_upload_completed);
    }

    @Override
    protected void notifyStarted() {
        Intent dIntent = new Intent(SeadroidApplication.getAppContext(), BrowserActivity.class);
        dIntent.putExtra(NOTIFICATION_MESSAGE_KEY, NOTIFICATION_OPEN_UPLOAD_TAB);
        dIntent.putExtra(TO_TRANSFER_LIST, true);
        dIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent uPendingIntent = PendingIntent.getActivity(
                SeadroidApplication.getAppContext(),
                0,
                dIntent,
                PendingIntent.FLAG_IMMUTABLE);

        mNotifBuilder = CustomNotificationBuilder
                .getNotificationBuilder(SeadroidApplication.getAppContext(),
                        CustomNotificationBuilder.CHANNEL_ID_UPLOAD)
                .setSmallIcon(R.drawable.ic_logo)
                .setOnlyAlertOnce(true)
                .setContentTitle(SeadroidApplication.getAppContext().getString(R.string.notification_upload_started_title))
                .setContentText(SeadroidApplication.getAppContext().getString(R.string.notification_upload_started_title))
                .setContentIntent(uPendingIntent)
                .setOngoing(true)
                .setProgress(100, 0, false);

        // Make this service run in the foreground, supplying the ongoing
        // notification to be shown to the user while in this state.
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//            txService.startForeground(NOTIFICATION_ID_UPLOAD, mNotifBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
//        } else {
//            txService.startForeground(NOTIFICATION_ID_UPLOAD, mNotifBuilder.build());
//        }
        ForegroundHelper.safeStartForeground(txService, NOTIFICATION_ID_UPLOAD, mNotifBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        txService.isStartForeground = true;
    }
}