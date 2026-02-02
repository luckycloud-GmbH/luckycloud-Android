package com.seafile.seadroid2.folderbackup;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.util.Utils;

/**
 * Periodic watchdog that restarts FolderBackupService when backup is enabled but not running.
 */
public class FolderBackupWatchdogService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        String backupPaths = SettingsManager.instance().getBackupPaths();
        if (!TextUtils.isEmpty(backupPaths)) {
            boolean running = Utils.isServiceRunning(getApplicationContext(), FolderBackupService.class.getName());
            if (!running) {
                Intent intent = new Intent(getApplicationContext(), FolderBackupService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(getApplicationContext(), intent);
                } else {
                    getApplicationContext().startService(intent);
                }
            }
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
