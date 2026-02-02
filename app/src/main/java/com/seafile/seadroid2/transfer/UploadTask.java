package com.seafile.seadroid2.transfer;

import android.util.Log;
import android.widget.Toast;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.ProgressMonitor;
import com.seafile.seadroid2.data.SeafBackup;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.monitor.AutoUpdateInfo;
import com.seafile.seadroid2.monitor.MonitorDBHelper;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Upload task
 */
public class UploadTask extends TransferTask {
    public static final String DEBUG_TAG = "UploadTask";

    private String dir;   // parent dir
    private boolean isUpdate;  // true if update an existing file
    private boolean isCopyToLocal; // false to turn off copy operation
    private boolean byBlock;
    private UploadStateListener uploadStateListener;

    private DataManager dataManager;

    public UploadTask(String source, int taskID, Account account, String repoID, String repoName,
                      String dir, String filePath, boolean isUpdate, boolean isCopyToLocal, boolean byBlock,
                      UploadStateListener uploadStateListener) {
        super(source, taskID, account, repoName, repoID, filePath);
        this.dir = dir;
        this.isUpdate = isUpdate;
        this.isCopyToLocal = isCopyToLocal;
        this.byBlock = byBlock;
        this.uploadStateListener = uploadStateListener;
        this.totalSize = new File(filePath).length();
        this.finished = 0;
        this.dataManager = new DataManager(account);
        this.source = source;
    }

    public UploadTaskInfo getTaskInfo() {
        UploadTaskInfo info = new UploadTaskInfo(account, taskID, state, repoID,
                repoName, dir, path, isUpdate, isCopyToLocal,
                finished, totalSize, startTime, source, err);
        return info;
    }

    public void cancelUpload() {
        if (state != TaskState.INIT && state != TaskState.TRANSFERRING) {
            return;
        }
        state = TaskState.CANCELLED;
        super.cancel(true);
    }

    @Override
    protected void onPreExecute() {
        state = TaskState.TRANSFERRING;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        long uploaded = values[0];
        // Log.d(DEBUG_TAG, "Uploaded " + uploaded);
        this.finished = uploaded;
        uploadStateListener.onFileUploadProgress(taskID);
    }

    @Override
    protected File doInBackground(Void... params) {
        ProgressMonitor monitor = new ProgressMonitor() {
            @Override
            public void onProgressNotify(long uploaded, boolean updateTotal) {
                publishProgress(uploaded);
            }

            @Override
            public boolean isCancelled() {
                return UploadTask.this.isCancelled();
            }
        };

        try {
            if (byBlock) {
                if (source.equals(Utils.TRANSFER_FOLDER_TAG))
                    dataManager.uploadByBlocks(repoName, repoID, dir, path, monitor, true, isCopyToLocal);
                else
                    dataManager.uploadByBlocks(repoName, repoID, dir, path, monitor, isUpdate, isCopyToLocal);
            } else {
                if (source.equals(Utils.TRANSFER_FOLDER_TAG))
                    dataManager.uploadFile(repoName, repoID, dir, path, monitor, true, isCopyToLocal);
                else
                    dataManager.uploadFile(repoName, repoID, dir, path, monitor, isUpdate, isCopyToLocal);
            }

            SeafRepo repo = dataManager.getCachedRepoByID(repoID);
            if (repo != null && repo.canLocalDecrypt() && Utils.isViewableImage(Utils.fileNameFromPath(path))) {
                String localPath = dataManager.getLocalRepoFilePath(repo.name, repo.id, Utils.pathJoin(dir, Utils.fileNameFromPath(path)));
                if (localPath != null) {
                    Utils.generateEncThumbPath(path, Utils.getEncThumbPath(localPath));
                }
            }
        } catch (SeafException e) {
            try {
                if (byBlock) {
                    dataManager.uploadByBlocks(repoName, repoID, dir, path, monitor, isUpdate, isCopyToLocal);
                } else {
                    dataManager.uploadFile(repoName, repoID, dir, path, monitor, isUpdate, isCopyToLocal);
                }

            } catch (SeafException e2) {
                Log.e(DEBUG_TAG, "Upload exception " + e2.getCode() + " " + e2.getMessage());
                e2.printStackTrace();
                err = e2;
            } catch (NoSuchAlgorithmException | IOException e3) {
                Log.e(DEBUG_TAG, "Upload exception " + e3.getMessage());
//                err = SeafException.unknownException;
                err = new SeafException(SeafException.UPLOAD_EXCEPTION, e3.getMessage());
                e3.printStackTrace();
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            Log.e(DEBUG_TAG, "Upload exception " + e.getMessage());
//            err = SeafException.unknownException;
            err = new SeafException(SeafException.UPLOAD_EXCEPTION, e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        state = err == null ? TaskState.FINISHED : TaskState.FAILED;
        if (uploadStateListener != null) {
            if (err == null) {
                long currentTimeMillis = System.currentTimeMillis();
                SettingsManager.instance().saveUploadCompletedTime(Utils.getSyncCompletedTime(currentTimeMillis));
                try {
                    Utils.utilsLogInfo(true,
                            SeadroidApplication.getAppContext().getString(source.equals(Utils.TRANSFER_FOLDER_TAG) ? R.string.backup_succeed : R.string.uploaded) +
                                    "\n\n" +
                                    Utils.fileNameFromPath(path));
                    if (source.equals(Utils.TRANSFER_FOLDER_TAG)) {
                        int totalBackup = SeadroidApplication.getInstance().getTotalBackup();
                        SettingsManager.instance().saveBackupCompletedTotal(totalBackup);
                        SettingsManager.instance().saveBackupCompletedTime(Utils.getSyncCompletedTime(currentTimeMillis));

                        SeafBackup backup = new SeafBackup(
                                UUID.randomUUID().toString(),
                                Utils.fileNameFromPath(path),
                                path,
                                repoName,
                                dir,
                                totalSize,
                                startTime,
                                currentTimeMillis,
                                UploadTaskManager.BROADCAST_FILE_UPLOAD_SUCCESS,
                                "");
                        dataManager.addBackupsToCache(backup);

                        Utils.utilsBackupLogInfo(
                                SeadroidApplication.getAppContext().getString(R.string.backup_succeed) +
                                        "\n\n" +
                                        Utils.fileNameFromPath(path) + "\n" +
                                        path + "\n" +
                                        Utils.pathJoin(repoName, dir));
                    }
                } catch (Exception ignored) {

                }
                AutoUpdateInfo info = new AutoUpdateInfo(account, repoID, repoName, dir, path);
                ConcurrentAsyncTask.submit(new Runnable() {
                    @Override
                    public void run() {
                        MonitorDBHelper db = MonitorDBHelper.getInstance();
                        db.removeAutoUpdateInfo(info);
                    }
                });
                uploadStateListener.onFileUploaded(taskID);
            } else {
                if (err.getCode() == SeafException.HTTP_ABOVE_QUOTA) {
                    Toast.makeText(SeadroidApplication.getAppContext(), R.string.above_quota, Toast.LENGTH_SHORT).show();
                }
                try {
                    Utils.utilsLogInfo(true,
                            SeadroidApplication.getAppContext().getString(source.equals(Utils.TRANSFER_FOLDER_TAG) ? R.string.backup_failed : R.string.failed_upload) +
                                    "\n" +
                                    "Error: " + err.toString() +
                                    "\n\n" +
                                    Utils.fileNameFromPath(path));
                    if (source.equals(Utils.TRANSFER_FOLDER_TAG)) {
                        SeafBackup backup = new SeafBackup(
                                UUID.randomUUID().toString(),
                                Utils.fileNameFromPath(path),
                                path,
                                repoName,
                                dir,
                                totalSize,
                                startTime,
                                System.currentTimeMillis(),
                                UploadTaskManager.BROADCAST_FILE_UPLOAD_FAILED,
                                err.toString());
                        dataManager.addBackupsToCache(backup);

                        Utils.utilsBackupLogInfo(
                                SeadroidApplication.getAppContext().getString(R.string.backup_failed) +
                                        "\n" +
                                        "Error: " + err.toString() +
                                        "\n\n" +
                                        Utils.fileNameFromPath(path) + "\n" +
                                        path + "\n" +
                                        Utils.pathJoin(repoName, dir));
                    }
                } catch (Exception ignored) {

                }
                uploadStateListener.onFileUploadFailed(taskID);
            }
            dataManager.addUploadToDB(getTaskInfo());
        }
    }

    @Override
    protected void onCancelled() {
        if (uploadStateListener != null) {
            try {
                Utils.utilsLogInfo(true,
                        SeadroidApplication.getAppContext().getString(source.equals(Utils.TRANSFER_FOLDER_TAG) ? R.string.backup_canceled : R.string.canceled_upload) +
                                "\n\n" +
                                Utils.fileNameFromPath(path));
                if (source.equals(Utils.TRANSFER_FOLDER_TAG)) {
                    SeafBackup backup = new SeafBackup(
                            UUID.randomUUID().toString(),
                            Utils.fileNameFromPath(path),
                            path,
                            repoName,
                            dir,
                            totalSize,
                            startTime,
                            System.currentTimeMillis(),
                            UploadTaskManager.BROADCAST_FILE_UPLOAD_CANCELLED,
                            "");
                    dataManager.addBackupsToCache(backup);

                    Utils.utilsBackupLogInfo(
                            SeadroidApplication.getAppContext().getString(R.string.backup_canceled) +
                                    "\n\n" +
                                    Utils.fileNameFromPath(path) + "\n" +
                                    path + "\n" +
                                    Utils.pathJoin(repoName, dir));
                }
            } catch (Exception ignored) {

            }
            uploadStateListener.onFileUploadCancelled(taskID);
        }
    }

    public String getDir() {
        return dir;
    }

    public boolean isCopyToLocal() {
        return isCopyToLocal;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

}