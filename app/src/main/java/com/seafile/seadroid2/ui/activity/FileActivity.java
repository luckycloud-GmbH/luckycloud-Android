package com.seafile.seadroid2.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeafConnection;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.notification.DownloadNotificationProvider;
import com.seafile.seadroid2.transfer.DownloadTaskInfo;
import com.seafile.seadroid2.transfer.TaskState;
import com.seafile.seadroid2.transfer.TransferService;
import com.seafile.seadroid2.transfer.TransferService.TransferBinder;
import com.seafile.seadroid2.ui.dialog.PasswordDialog;
import com.seafile.seadroid2.ui.dialog.TaskDialog;
import com.seafile.seadroid2.util.Utils;

import java.io.File;
import java.net.HttpURLConnection;

/**
 * Display a file
 */
public class FileActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {
    private static final String DEBUG_TAG = "FileActivity";

    private TextView mFileNameText;
    private ImageView mFileIcon;
    private CardView mButtonCancel;

    private TextView mProgressText;
    private ProgressBar mProgressBar;

    private String mRepoName, mRepoID, mFilePath;
    private DataManager mDataManager;
    private Account mAccount;

    private int mTaskID = -1;
    private TransferService mTransferService;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(DEBUG_TAG, "TransferService connected");

            TransferBinder binder = (TransferBinder) service;
            mTransferService = binder.getService();
            onTransferServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    private boolean timerStarted = false;

    private final Handler mTimer = new Handler();
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!timerStarted || mTransferService == null) {
                Log.d(DEBUG_TAG, "Timer stopped or service null, skipping update.");
                return;
            }

            DownloadTaskInfo downloadTaskInfo = mTransferService.getDownloadTaskInfo(mTaskID);
            if (downloadTaskInfo != null) {
                if (downloadTaskInfo.state == TaskState.INIT ||
                        downloadTaskInfo.state == TaskState.TRANSFERRING) {
                    onFileDownloadProgress(downloadTaskInfo);
                } else if (downloadTaskInfo.state == TaskState.FAILED) {
                    onFileDownloadFailed(downloadTaskInfo);
                } else if (downloadTaskInfo.state == TaskState.FINISHED) {
                    onFileDownloaded();
                }
                // CANCELLED → no UI updates
            }

            if (timerStarted) {
                Log.d(DEBUG_TAG, "Timer tick at " + System.currentTimeMillis());
                mTimer.postDelayed(this, 1000);
            }
        }
    };
    private int openOfficeValue;
    private boolean isOpenWith;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        mAccount = intent.getParcelableExtra("account");
        mRepoName = intent.getStringExtra("repoName");
        mRepoID = intent.getStringExtra("repoID");
        mFilePath = intent.getStringExtra("filePath");
        mTaskID = intent.getIntExtra("taskID", 0);
        openOfficeValue = intent.getIntExtra("open_office", SettingsManager.OPEN_OFFICE_ONLY_OFFICE);
        isOpenWith = intent.getBooleanExtra("is_open_with", false);
        mDataManager = new DataManager(mAccount);

        setContentView(R.layout.file_activity);
        initWidgets();
        bindTransferService();
    }

    @Override
    protected void onStart() {
        Log.d(DEBUG_TAG, "onStart");
        super.onStart();
        if (mTransferService != null) {
            startTimer();
        }
    }

    @Override
    protected void onStop() {
        Log.d(DEBUG_TAG, "onStop");
        super.onStop();
        stopTimer();
    }

    @Override
    protected void onDestroy() {
        Log.d(DEBUG_TAG, "onDestroy");
        super.onDestroy();
        stopTimer();
        if (mTransferService != null) {
            unbindService(mConnection);
            mTransferService = null;
        }

    }

    private void initWidgets() {
        mFileNameText = (TextView) findViewById(R.id.file_name);
        mFileIcon = (ImageView) findViewById(R.id.file_icon);
        mButtonCancel = (CardView) findViewById(R.id.op_cancel);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressText = (TextView) findViewById(R.id.progress_text);

        String fileName = Utils.fileNameFromPath(mFilePath);
        mFileNameText.setText(fileName);

        // icon
        mFileIcon.setImageResource(Utils.getFileIcon(fileName));

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTaskID > 0) {
                    mTransferService.cancelDownloadTask(mTaskID);
                    mTransferService.cancelNotification();
                }
                finish();
            }
        });

        Toolbar toolbar = getActionBarToolbar();
        toolbar.setOnMenuItemClickListener(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(fileName);
    }

    private void onTransferServiceConnected() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
        mProgressText.setVisibility(View.VISIBLE);

        startTimer();
    }

    private void bindTransferService() {
        Intent txIntent = new Intent(this, TransferService.class);
        startService(txIntent);
        Log.d(DEBUG_TAG, "start TransferService");

        // bind transfer service
        // Intent bIntent = new Intent(this, TransferService.class);
        bindService(txIntent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d(DEBUG_TAG, "try bind TransferService");
    }

    private void onFileDownloadProgress(DownloadTaskInfo info) {
        long fileSize = info.fileSize;
        long finished = info.finished;

        mProgressBar.setIndeterminate(false);
        int percent;
        if (fileSize == 0) {
            percent = 100;
        } else {
            percent = (int)(finished * 100 / fileSize);
        }
        mProgressBar.setProgress(percent);

        String txt = Utils.readableFileSize(finished) + " / " + Utils.readableFileSize(fileSize);

        mProgressText.setText(txt);

        if (!mTransferService.hasDownloadNotifProvider()) {
            DownloadNotificationProvider provider = new DownloadNotificationProvider(
                    mTransferService.getDownloadTaskManager(),
                    mTransferService);
            mTransferService.saveDownloadNotifProvider(provider);
        } else {
            // if the notificationManager mapping the repoID exist, update its data set
            // mTransferService.getDownloadNotifProvider().updateTotalSize(fileSize);
        }
    }

    private void onFileDownloaded() {
        mProgressBar.setVisibility(View.GONE);
        mProgressText.setVisibility(View.GONE);
        mButtonCancel.setVisibility(View.GONE);

        File file = mDataManager.getLocalRepoFile(mRepoName, mRepoID, mFilePath);
        if (file != null && timerStarted) {
            Intent result = new Intent();
            result.putExtra("path", file.getAbsolutePath());
            result.putExtra("open_office", openOfficeValue);
            result.putExtra("is_open_with", isOpenWith);
            setResult(RESULT_OK, result);
        } else {
            setResult(RESULT_CANCELED);
        }
        stopTimer();
        finish();
    }

    private void onFileDownloadFailed(DownloadTaskInfo info) {
        mProgressBar.setVisibility(View.GONE);
        mProgressText.setVisibility(View.GONE);
        mButtonCancel.setVisibility(View.GONE);

        SeafException err = info.err;
        String fileName = Utils.fileNameFromPath(info.pathInRepo);
        if (err.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            showToast(String.format(getString(R.string.file_not_found), fileName));
        } else if (err.getCode() == SeafConnection.HTTP_STATUS_REPO_PASSWORD_REQUIRED) {
            handlePassword();
        } else {
            showToast(String.format(getString(R.string.op_exception_failed_to_download_file), fileName));
        }
        stopTimer();
    }

    private void handlePassword() {
        PasswordDialog passwordDialog = new PasswordDialog();
        passwordDialog.setRepo(mRepoName, mRepoID, mAccount);
        passwordDialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                mTaskID = mTransferService.addDownloadTask(mAccount,
                        mRepoName,
                        mRepoID,
                        mFilePath,
                        true);
            }

            @Override
            public void onTaskCancelled() {
                finish();
            }
        });
        passwordDialog.show(getSupportFragmentManager(), "DialogFragment");
    }

    public void showToast(CharSequence msg) {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void startTimer() {
        if (timerStarted) {
            return;
        }
        timerStarted = true;
        Log.d(DEBUG_TAG, "timer started");
        mTimer.postDelayed(timerRunnable, 1000);
    }

    public void stopTimer() {
        if (!timerStarted) {
            return;
        }
        timerStarted = false;
        Log.d(DEBUG_TAG, "timer stopped");
        mTimer.removeCallbacks(timerRunnable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mTaskID > 0) {
                mTransferService.cancelDownloadTask(mTaskID);
                mTransferService.cancelNotification();
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
}
