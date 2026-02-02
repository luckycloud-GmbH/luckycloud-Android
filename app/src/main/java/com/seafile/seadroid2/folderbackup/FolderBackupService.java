package com.seafile.seadroid2.folderbackup;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.account.AccountManager;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.folderbackup.selectfolder.FileBean;
import com.seafile.seadroid2.folderbackup.selectfolder.FileTools;
import com.seafile.seadroid2.folderbackup.selectfolder.StringTools;
import com.seafile.seadroid2.notification.BaseNotificationProvider;
import com.seafile.seadroid2.notification.ForegroundHelper;
import com.seafile.seadroid2.notification.UploadNotificationProvider;
import com.seafile.seadroid2.transfer.TransferManager;
import com.seafile.seadroid2.transfer.TransferService;
import com.seafile.seadroid2.transfer.UploadTaskManager;
import com.seafile.seadroid2.ui.CustomNotificationBuilder;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FolderBackupService extends Service {
    private static final String DEBUG_TAG = "FolderBackupService";
    private static final int NOTIFICATION_ID = 2001;
    private static final int WATCHDOG_JOB_ID = 2002;
    private static final long WATCHDOG_INTERVAL_MS = 15 * 60 * 1000L;
    private static final long MONITOR_INTERVAL_MS = 1000L;
    private static final String CHANNEL_ID = "folder_backup_channel";
    private static final String CHANNEL_NAME = "Folder Backup";
    private static final String CHANNEL_DESCRIPTION = "Folder backup service";
    private Map<String, FolderBackupInfo> fileUploaded = new HashMap<>();
    private final IBinder mBinder = new FileBackupBinder();
    private TransferService txService = null;
    private DataManager dataManager;
    private FolderBackupDBHelper databaseHelper;
    private AccountManager accountManager;
    private Account currentAccount;
    private RepoConfig repoConfig;
    private List<String> backupPathsList;
    private FolderBackupReceiver mFolderBackupReceiver;
    private FileAlterationMonitor fileMonitor;
    public boolean fileMonitorRunning = false;
    public int count = 0;
    private String backupNames = "";
    private String backupDetails = "";

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    boolean isStartForeground = false;

    public class FileBackupBinder extends Binder {
        public FolderBackupService getService() {
            return FolderBackupService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isStartForeground) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    startForegroundWithNotification();
                }
            } else {
                startForegroundWithNotification();
            }
            isStartForeground = true;
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        databaseHelper = FolderBackupDBHelper.getDatabaseHelper();
        Intent bIntent = new Intent(this, TransferService.class);

        accountManager = new AccountManager(this);
        bindService(bIntent, mConnection, Context.BIND_AUTO_CREATE);

        if (mFolderBackupReceiver == null) {
            mFolderBackupReceiver = new FolderBackupReceiver();
        }

        IntentFilter filter = new IntentFilter(TransferManager.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mFolderBackupReceiver, filter);

        String backupPaths = SettingsManager.instance().getBackupPaths();
        if (!TextUtils.isEmpty(backupPaths)) {
            List<String> pathsList = StringTools.getJsonToList(backupPaths);
            if (pathsList != null) {
                startFolderMonitor(pathsList);
                scheduleWatchdog(this);
            }
        }
    }

    /**
     * TODO and FIXME:
     * <p>
     * On Android 5.0, API 21 - Android 7.1, API 25, this feature is not available and needs to be refactored
     * <p>
     * Because Apache Commons IO requires a specified JDK version (>=1.8) and other reasons.
     */
    public void startFolderMonitor(List<String> backupPaths) {
        List<FileAlterationObserver> fileAlterationObserverList = new ArrayList<>();

        for (String str : backupPaths) {
            FileAlterationObserver folderFileObserver = new FileAlterationObserver(str);
            folderFileObserver.addListener(new FolderStateChangedListener());
            fileAlterationObserverList.add(folderFileObserver);
        }

        fileMonitor = new FileAlterationMonitor(MONITOR_INTERVAL_MS, fileAlterationObserverList);
        try {
            fileMonitor.start();
            fileMonitorRunning = true;
        } catch (Exception e) {
            String errMsg = "failed to start file monitor: " + e.getMessage();
            Log.w(DEBUG_TAG, errMsg);
            throw new RuntimeException(errMsg);
        }
    }

    public void stopFolderMonitor() {
        if (fileMonitor != null) {
            try {
                fileMonitor.stop();
                fileMonitorRunning = false;
            } catch (Exception e) {
                String errMsg = "failed to stop file monitor: " + e.getMessage();
                Log.w(DEBUG_TAG, errMsg);
                throw new RuntimeException(errMsg);
            }
        }
    }

    private void startForegroundWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder mNotifBuilder = CustomNotificationBuilder.getNotificationBuilder(SeadroidApplication.getAppContext(),
                        CustomNotificationBuilder.CHANNEL_ID_UPLOAD)
                .setSmallIcon(R.drawable.ic_logo)
                .setOnlyAlertOnce(true)
                .setContentTitle(SeadroidApplication.getAppContext().getString(R.string.app_name))
                .setOngoing(true)
                .setContentText(SeadroidApplication.getAppContext().getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false);

        Notification notification = mNotifBuilder.build();

        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        ForegroundHelper.safeStartForeground(this, BaseNotificationProvider.NOTIFICATION_ID_UPLOAD, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
    }


    public RepoConfig backupRepoConfig(String email) {
        if (databaseHelper == null) {
            databaseHelper = FolderBackupDBHelper.getDatabaseHelper();
        }

        if (!TextUtils.isEmpty(email)) {
            try {
                return databaseHelper.getRepoConfig(email);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public void backupFolder(String email) {
        fileUploaded.clear();
        if (databaseHelper == null) {
            databaseHelper = FolderBackupDBHelper.getDatabaseHelper();
        }

        if (!TextUtils.isEmpty(email)) {
            try {
                repoConfig = databaseHelper.getRepoConfig(email);
            } catch (Exception e) {
                repoConfig = null;
            }
        }

        String backupPaths = SettingsManager.instance().getBackupPaths();
        if (repoConfig == null || TextUtils.isEmpty(backupPaths)) {
            return;
        }
        if (accountManager == null) {
            accountManager = new AccountManager(this);
        }
        currentAccount = accountManager.getCurrentAccount();
        backupPathsList = StringTools.getJsonToList(backupPaths);
        dataManager = new DataManager(currentAccount);

        if (!StringTools.checkFolderUploadNetworkAvailable()) {
//            SeadroidApplication.getInstance().setScanUploadStatus(CameraSyncStatus.NETWORK_UNAVAILABLE);
            return;
        }

        //start backup
        if (txService == null)
            return;
        if (!txService.hasUploadNotifProvider()) {
            UploadNotificationProvider provider = new UploadNotificationProvider(
                    txService.getUploadTaskManager(),
                    txService);
            txService.saveUploadNotifProvider(provider);
        }
        ConcurrentAsyncTask.execute(new FolderBackupTask());
    }

    private class FolderBackupTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            for (String str : backupPathsList) {
                String[] split = str.split("/");
                startBackupFolder(split[split.length - 1] + "/", str);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String FilePath) {
            // Utils.utilsLogInfo(false, "----------" + FilePath);
        }
    }

    private void startBackupFolder(String parentPath, String filePath) {
        count += 1;
        String realPath = "/" + Utils.removeLastPathSeperator(parentPath);
        String realParentPath = Utils.getParentPath(realPath);
        String realDir = Utils.removeFirstPathSeperator(Utils.pathSplit2(realPath, realParentPath));
        try {
            forceCreateDirectory(dataManager, realParentPath, realDir);
        } catch (SeafException e) {
            String errMsg = "failed to create a directory: " + e.getMessage();
            Log.w(DEBUG_TAG, errMsg);
            processBackupNames();
            throw new RuntimeException(errMsg);
        }

        List<FileBean> fileBeanList = new ArrayList<>();
        FileBean fileBean;
        File file = FileTools.getFileByPath(filePath);
        File[] files = file.listFiles();
        if (files != null) {
            for (File value : files) {
                fileBean = new FileBean(value.getAbsolutePath());
                fileBeanList.add(fileBean);
            }
        }
        if (fileBeanList.size() == 0) return;
        for (FileBean fb : fileBeanList) {
            if (fb.isDir()) {
                startBackupFolder(parentPath + fb.getFileName() + "/", fb.getFilePath());
            } else {
                Utils.utilsLogInfo(false, "=relative_path==============" + parentPath + "--------" + fb.getFilePath());

                FolderBackupInfo fileInfo = databaseHelper.getBackupFileInfo(repoConfig.getRepoID(),
                        fb.getFilePath(), fb.getSimpleSize() + "");
                if (fileInfo != null && !TextUtils.isEmpty(fileInfo.filePath)) {
                    Utils.utilsLogInfo(false, "===============" + fileInfo.filePath);
                } else {
                    final SeafRepo repo = dataManager.getCachedRepoByID(repoConfig.getRepoID());

                    boolean isUpdate = false;
                    try {
                        for (SeafDirent dirent : dataManager.getCachedDirents(repoConfig.getRepoID(), realPath)) {
                            if (dirent.name.equals(fb.getFileName())) {
                                isUpdate = true;
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                        processBackupNames();
                    }

                    StringBuilder sb = new StringBuilder();
                    backupNames = sb
                            .append(backupNames).append("\n")
                            .append(fb.getFileName()).toString();
                    StringBuilder sb2 = new StringBuilder();
                    backupDetails = sb2
                            .append(backupDetails).append("\n")
                            .append(fb.getFileName()).append("\n")
                            .append(fb.getFilePath()).append("\n")
                            .append(Utils.pathJoin(repo != null ? repo.name : repoConfig.getRepoName(), parentPath)).append("\n").toString();

                    boolean isFolderBackupSaveToCache = SettingsManager.instance().isFolderBackupSaveToCache();
                    int taskID = 0;
                    if (repo != null && repo.canLocalDecrypt()) {
                        taskID = txService.addTaskToSourceQueBlock(Utils.TRANSFER_FOLDER_TAG, currentAccount, repoConfig.getRepoID(),
                                repoConfig.getRepoName(), parentPath, fb.getFilePath(), isUpdate, isFolderBackupSaveToCache);
                    } else {
                        taskID = txService.addTaskToSourceQue(Utils.TRANSFER_FOLDER_TAG, currentAccount, repoConfig.getRepoID(),
                                repoConfig.getRepoName(), parentPath, fb.getFilePath(), isUpdate, isFolderBackupSaveToCache);
                    }
                    if (taskID != 0) {
                        FolderBackupInfo dirInfo = new FolderBackupInfo(repoConfig.getRepoID(), repoConfig.getRepoName(),
                                parentPath, fb.getFileName(), fb.getFilePath(), fb.getSimpleSize() + "");
                        fileUploaded.put(taskID + "", dirInfo);
                    }
                }
            }
        }

        processBackupNames();
    }

    private void processBackupNames() {
        count -= 1;
        if (count < 1) {
            if (!backupNames.isEmpty()) {
                Utils.utilsLogInfo(true,
                        SeadroidApplication.getAppContext().getString(R.string.backup_started) +
                                "\n" +
                                backupNames);
            }
            if (!backupDetails.isEmpty()) {
                Utils.utilsBackupLogInfo(
                        SeadroidApplication.getAppContext().getString(R.string.backup_started) +
                                "\n" +
                                backupDetails);
            }
            count = 0;
            backupNames = "";
            backupDetails = "";
        }
    }

    private void forceCreateDirectory(DataManager dataManager, String parent, String dir) throws SeafException {
        List<SeafDirent> dirs = dataManager.getDirentsFromServer(repoConfig.getRepoID(), parent);
        boolean found = false;
        for (SeafDirent dirent : dirs) {
            if (dirent.name.equals(dir) && dirent.isDir()) {
                found = true;
            }
        }
        if (!found)
            dataManager.createNewDir(repoConfig.getRepoID(), Utils.pathJoin("/", parent), dir);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TransferService.TransferBinder binder = (TransferService.TransferBinder) service;
            synchronized (this) {
                txService = binder.getService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            synchronized (this) {
                txService = null;
            }
        }
    };

    @Override
    public void onDestroy() {
        // Ensure the foreground notification is dropped before the service dies; otherwise
        // Android 14+ throws ForegroundServiceDidNotStopInTimeException.
        stopForeground(true);

        if (txService != null) {
            unbindService(mConnection);
            txService = null;
        }
        if (mFolderBackupReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mFolderBackupReceiver);
        }
        stopFolderMonitor();
        super.onDestroy();
    }

    private void scheduleWatchdog(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) {
            return;
        }
        JobInfo.Builder builder = new JobInfo.Builder(WATCHDOG_JOB_ID,
                new ComponentName(context, FolderBackupWatchdogService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPeriodic(WATCHDOG_INTERVAL_MS);
        } else {
            builder.setPeriodic(WATCHDOG_INTERVAL_MS);
        }
        scheduler.schedule(builder.build());
    }

    private class FolderStateChangedListener implements FileAlterationListener {

        public FolderStateChangedListener() {

        }

        @Override
        public void onStart(FileAlterationObserver observer) {

        }

        @Override
        public void onDirectoryCreate(File directory) {
            Utils.utilsEventsLogInfo(
                    getResources().getString(R.string.create) +
                            ", " +
                            directory.getName());
            backupFolders();
        }

        @Override
        public void onDirectoryChange(File directory) {
            Utils.utilsEventsLogInfo(
                    getResources().getString(R.string.updated) +
                            ", " +
                            directory.getName());
            backupFolders();
        }

        @Override
        public void onDirectoryDelete(File directory) {
            Utils.utilsEventsLogInfo(
                    getResources().getString(R.string.delete) +
                            ", " +
                            directory.getName());
        }

        @Override
        public void onFileCreate(File file) {
            Utils.utilsEventsLogInfo(
                    getResources().getString(R.string.create) +
                            ", " +
                            file.getName());
            backupFolders();
        }

        @Override
        public void onFileChange(File file) {
            Utils.utilsEventsLogInfo(
                    getResources().getString(R.string.updated) +
                            ", " +
                            file.getName());
            backupFolders();
        }

        @Override
        public void onFileDelete(File file) {
            Utils.utilsEventsLogInfo(
                    getResources().getString(R.string.delete) +
                            ", " +
                            file.getName());
        }

        @Override
        public void onStop(FileAlterationObserver observer) {

        }
    }

    public void backupFolders() {
        String backupEmail = SettingsManager.instance().getBackupEmail();
        if (backupEmail != null) {
            backupFolder(backupEmail);
        }
    }

    private class FolderBackupReceiver extends BroadcastReceiver {

        private FolderBackupReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            if (TextUtils.equals(UploadTaskManager.BROADCAST_FILE_UPLOAD_SUCCESS, type)) {
                int taskID = intent.getIntExtra("taskID", 0);
                onFileBackedUp(taskID);
            }
        }
    }

    private void onFileBackedUp(int taskID) {
        if (fileUploaded != null) {
            FolderBackupInfo uploadInfo = fileUploaded.get(taskID + "");
            if (databaseHelper == null) {
                databaseHelper = FolderBackupDBHelper.getDatabaseHelper();
            }
            if (uploadInfo != null) {
                databaseHelper.saveFileBackupInfo(uploadInfo);
            }
            EventBus.getDefault().post(new FolderBackupEvent("folderBackup"));
            //SettingsManager.instance().saveBackupCompletedTime(Utils.getSyncCompletedTime());
        }
    }
}
