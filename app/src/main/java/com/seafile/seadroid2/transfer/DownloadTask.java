package com.seafile.seadroid2.transfer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.ProgressMonitor;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.ui.WidgetUtils;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Download task
 *
 */
public class DownloadTask extends TransferTask {
    public static final String DEBUG_TAG = "DownloadTask";

    private String localPath;
    private DownloadStateListener downloadStateListener;
    private boolean updateTotal;
    private boolean offlineAvailable;
    private boolean thumbnail;
    private long size;

    private DataManager dataManager;

    public DownloadTask(int taskID,
                        Account account,
                        String repoName,
                        String repoID,
                        String path,
                        boolean offlineAvailable,
                        boolean thumbnail,
                        long size,
                        DownloadStateListener downloadStateListener) {
        super("", taskID, account, repoName, repoID, path);
        this.downloadStateListener = downloadStateListener;
        this.offlineAvailable = offlineAvailable;
        this.thumbnail = thumbnail;
        this.size = size;
        this.dataManager = new DataManager(account);
    }

    /**
     * When downloading a file, we don't know the file size in advance, so
     * we make use of the first progress update to return the file size.
     */
    @Override
    protected void onProgressUpdate(Long... values) {
        state = TaskState.TRANSFERRING;
        if (totalSize == -1 || updateTotal) {
            totalSize = values[0];
            return;
        }
        finished = values[0];
        downloadStateListener.onFileDownloadProgress(taskID);
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            final SeafRepo repo = dataManager.getCachedRepoByID(repoID);
            if (repo != null && repo.canLocalDecrypt()) {
                File file = dataManager.getFileByBlocks(repoName, repoID, path, totalSize, offlineAvailable, thumbnail,
                        new ProgressMonitor() {

                            @Override
                            public void onProgressNotify(long total, boolean updateTotal) {
                                DownloadTask.this.updateTotal = updateTotal;
                                publishProgress(total);
                            }

                            @Override
                            public boolean isCancelled() {
                                return DownloadTask.this.isCancelled();
                            }
                        }
                );

                if (thumbnail) {
                    if (size != file.length()) {
                        file.delete();
                        return null;
                    }
                    String fullPath = Utils.pathJoin(repoName, path);
                    if (!SeadroidApplication.getInstance().getGalleryPhotos().contains(fullPath)) {
                        localPath = file.getPath();
                        try {
                            File thumbFile = new File(Utils.getEncThumbPath(localPath));
                            if (thumbFile.exists()) {
                                thumbFile.delete();
                            }

                            Utils.generateEncThumbPath(localPath, Utils.getEncThumbPath(localPath));
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                        if (!SettingsManager.instance().isSaveFilesOfThumbInCache()) {
                            file.delete();
                        }
                    }
                }

                return file;
            } else
                return dataManager.getFile(repoName, repoID, path, offlineAvailable, thumbnail,
                        new ProgressMonitor() {

                            @Override
                            public void onProgressNotify(long total, boolean updateTotal) {
                                publishProgress(total);
                            }

                            @Override
                            public boolean isCancelled() {
                                return DownloadTask.this.isCancelled();
                            }
                        }
                );
        } catch (SeafException e) {
            err = e;
            return null;
        } catch (JSONException e) {
            err = SeafException.unknownException;
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            err = SeafException.networkException;
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            err = SeafException.unknownException;
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(File file) {
        if (downloadStateListener != null) {
            if (file != null) {
                state = TaskState.FINISHED;
                if (file.exists()) {
                    localPath = file.getPath();
                }
                downloadStateListener.onFileDownloaded(taskID);

                if (Utils.isViewableImage(file.getName())) {
                    if (!offlineAvailable) {
                        final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        final Uri contentUri = Uri.fromFile(file);
                        scanIntent.setData(contentUri);
                        Context context = SeadroidApplication.getAppContext();
                        try {
                            context.sendBroadcast(scanIntent);
                        } catch (Exception ignored) {
                        }
                    }
                }
            } else {
                state = TaskState.FAILED;
                if (err == null)
                    err = SeafException.unknownException;
                downloadStateListener.onFileDownloadFailed(taskID);
            }
            dataManager.addDownloadToDB(getTaskInfo());
        }
    }

    @Override
    protected void onCancelled() {
        state = TaskState.CANCELLED;
    }

    @Override
    public DownloadTaskInfo getTaskInfo() {
        DownloadTaskInfo info = new DownloadTaskInfo(account, taskID, state, repoID,
                repoName, path, localPath, totalSize, finished, startTime, thumbnail, err);
        return info;
    }

    public String getLocalPath() {
        return localPath;
    }

    public boolean getOfflineAvailable() {
        return offlineAvailable;
    }

    public boolean getThumbnail() {
        return thumbnail;
    }

    public long getSize() {
        return size;
    }
}