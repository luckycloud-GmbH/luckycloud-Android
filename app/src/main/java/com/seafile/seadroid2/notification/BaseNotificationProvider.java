package com.seafile.seadroid2.notification;

import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.transfer.TransferManager;
import com.seafile.seadroid2.transfer.TransferService;

import java.util.Timer;
import java.util.TimerTask;

/**
 * All downloading events will be represented by one downloading notification, at the same time all
 * uploading events will be represented by one uploading notification as well.
 * maintain state of downloading or uploading events and update the relevant notification.
 */
public abstract class BaseNotificationProvider {

    protected NotificationCompat.Builder mNotifBuilder;

    protected NotificationManager mNotifMgr = (NotificationManager) SeadroidApplication.getAppContext().
            getSystemService(SeadroidApplication.getAppContext().NOTIFICATION_SERVICE);

    public static final String NOTIFICATION_MESSAGE_KEY = "notification message key";
    /** Creates an explicit flag for opening @{link com.seafile.seadroid2.ui.fragment.DownloadTaskFragment}
     * in @{link com.seafile.seadroid2.ui.activity.TransferActivity} */
    public static final String NOTIFICATION_OPEN_DOWNLOAD_TAB = "open download tab notification";
    /** Creates an explicit flag for opening @{link com.seafile.seadroid2.ui.fragment.UploadTaskFragment}
     * in @{link com.seafile.seadroid2.ui.activity.TransferActivity} */
    public static final String NOTIFICATION_OPEN_UPLOAD_TAB = "open upload tab notification";
    public static final String TO_TRANSFER_LIST = "to transfer list";

    public static final int NOTIFICATION_ID_DOWNLOAD = 1;
    public static final int NOTIFICATION_ID_UPLOAD = 2;

    protected TransferManager txMgr;
    protected TransferService txService;

    public BaseNotificationProvider(TransferManager transferManager,
                                    TransferService transferService) {
        this.txMgr = transferManager;
        this.txService = transferService;
    }

    /**
     * calculate state
     *
     * @return
     *        {@code NotificationState.NOTIFICATION_STATE_FAILED}, when at least one task failed
     *        {@code NotificationState.NOTIFICATION_STATE_CANCELLED}, when at least one task cancelled
     *        {@code NotificationState.NOTIFICATION_STATE_PROGRESS}, when at least one task in progress
     *        {@code NotificationState.NOTIFICATION_STATE_COMPLETED}, otherwise.
     */
    protected abstract NotificationState getState();

    /**
     * get notification id
     *
     * @return
     *          notificationID
     */
    protected abstract int getNotificationID();

    /**
     * get notification title texts
     *
     * @return
     *          some descriptions shown in notification title
     */
    protected abstract String getNotificationTitle(NotificationState state);

    /**
     * get downloading or uploading status
     *
     * @return
     *         texts of downloading or uploading status
     */
    protected abstract String getProgressInfo(NotificationState state);

    /**
     * get progress of transferred files
     *
     * @return
     *          progress
     */
    protected abstract int getProgress();

    /**
     * start to show a notification
     *
     */
    protected abstract void notifyStarted();

    /**
     * update notification
     */
    public void updateNotification() {
        if (txService == null || mNotifMgr == null) return;

        NotificationState state = getState();

        if (mNotifBuilder == null && state == NotificationState.NOTIFICATION_STATE_PROGRESS) {
            notifyStarted();
        }

        if (mNotifBuilder == null) return;

        int notifId = getNotificationID();
        String notifTitle = getNotificationTitle(state);
        String progressInfo = getProgressInfo(state);
        int progress = getProgress();

        switch (state) {
            case NOTIFICATION_STATE_PROGRESS:
                notifyProgress(notifId, notifTitle, progressInfo, progress);
                break;
            case NOTIFICATION_STATE_COMPLETED_WITH_ERRORS:
                notifyCompletedWithErrors(notifId, notifTitle, progressInfo, progress);
                break;
            case NOTIFICATION_STATE_COMPLETED:
                notifyCompleted(notifId, notifTitle, progressInfo);
                break;
        }
    }

    /**
     * update notification when downloading or uploading in progress
     *
     * @param notificationID
     *          use to update the notification later on
     * @param title
     *          some descriptions shown in notification title
     * @param info
     *          some descriptions to indicate the upload status
     * @param progress
     *          progress value to update build-in progressbar
     *
     */
    private void notifyProgress(int notificationID,
                                String title,
                                String info,
                                int progress) {
        mNotifBuilder
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(title)
                .setContentText(info)
                .setProgress(100, progress, false)
                .setOngoing(true)
                .setAutoCancel(false);

        mNotifMgr.notify(notificationID, mNotifBuilder.build());
    }

    /**
     * update notification when completed
     *
     * @param notificationID
     *          use to update the notification later on
     * @param title
     *          some descriptions shown in notification title
     *
     */
    private void notifyCompleted(int notificationID, String title, String info) {
        mNotifBuilder
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(title)
                .setContentText(info)
                .setProgress(0, 0, false)
                .setOngoing(false)
                .setAutoCancel(true);

        mNotifMgr.notify(notificationID, mNotifBuilder.build());
        mNotifBuilder = null;

        cancelWithDelay(txService, 5000, false);
    }

    /**
     * update notification when failed or cancelled
     *
     * @param notificationID
     *          use to update the notification later on
     * @param title
     *          some descriptions shown in notification title
     * @param info
     *          some descriptions to indicate the upload status
     * @param progress
     *          progress value to update build-in progressbar
     *
     */
    protected void notifyCompletedWithErrors(int notificationID, String title, String info, int progress) {
        mNotifBuilder
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(title)
                .setContentText(info)
                //.setProgress(100, progress, false)
                .setProgress(0, 0, false)
                .setOngoing(false)
                .setAutoCancel(true);

        mNotifMgr.notify(notificationID, mNotifBuilder.build());
        mNotifBuilder = null;

        cancelWithDelay(txService, 5000, false);
    }

    /**
     * Delay for a while before cancel notification in order user can see the result
     *
     * @param transferService
     * @param delayInMillis
     */
    public static void cancelWithDelay(final TransferService transferService,
                                       long delayInMillis, boolean removeNotification) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (transferService != null && !transferService.isTransferring()) {
                    transferService.stopForeground(removeNotification);
                    transferService.isStartForeground = false;
                }
            }
        }, delayInMillis);

    }

    /**
     * Clear notification from notification area
     */
    public void cancelNotification() {
        mNotifMgr.cancelAll();
        mNotifBuilder = null;
        cancelWithDelay(txService, 2000, true);
    }

    public enum NotificationState {
        NOTIFICATION_STATE_PROGRESS,
        NOTIFICATION_STATE_COMPLETED,
        NOTIFICATION_STATE_COMPLETED_WITH_ERRORS
    }

}
