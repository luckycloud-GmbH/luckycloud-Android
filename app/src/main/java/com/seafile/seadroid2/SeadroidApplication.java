package com.seafile.seadroid2;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.RestrictionsManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.common.collect.Lists;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.seafile.seadroid2.avatar.AuthImageDownloader;
import com.seafile.seadroid2.data.SeafPhoto;
import com.seafile.seadroid2.data.StorageManager;
import com.seafile.seadroid2.gesturelock.AppLockManager;
import com.seafile.seadroid2.ui.CustomNotificationBuilder;
import com.seafile.seadroid2.util.CrashHandler;
import com.seafile.seadroid2.util.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeadroidApplication extends Application implements LifecycleObserver {
    private static Context context;
    private int waitingNumber;
    private int totalNumber;
    private int scanUploadStatus;
    private static SeadroidApplication instance;
    private int totalBackup;
    private int waitingBackup;
    private List<String> loginConfig;
    private List<String> galleryPhotos;

    public void onCreate() {
        super.onCreate();
        Iconify.with(new MaterialCommunityModule());
        instance = this;
        initImageLoader(getApplicationContext());

        // set gesture lock if available
        AppLockManager.getInstance().enableDefaultAppLockIfAvailable(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(SeadroidApplication.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    initNotificationChannel();
                }
            } else {
                initNotificationChannel();
            }
        }
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        Utils.logPhoneModelInfo();
        writeNoMediaFile();
        Utils.setAppearance();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        fetchLoginConfig();
    }

    private void fetchLoginConfig() {
        String server = "";
        String user = "";
        String password = "";

        RestrictionsManager restrictionsMgr =
                (RestrictionsManager) getSystemService(Context.RESTRICTIONS_SERVICE);
        Bundle appRestrictions = restrictionsMgr.getApplicationRestrictions();
        if (appRestrictions.containsKey("login_configuration")) {
            Bundle loginRestrictions = appRestrictions.getBundle("login_configuration");
            if (loginRestrictions != null) {
                if (loginRestrictions.containsKey("login_server")) {
                    server = loginRestrictions.getString("login_server", "");
                }
                if (loginRestrictions.containsKey("login_user")) {
                    user = loginRestrictions.getString("login_user", "");
                }
                if (loginRestrictions.containsKey("login_password")) {
                    password = loginRestrictions.getString("login_password", "");
                }
            }
        }

        if (!server.isEmpty() || !user.isEmpty() || !password.isEmpty()) {
            setLoginConfig(Arrays.asList(server, user, password));
        } else {
            setLoginConfig(new ArrayList<>());
        }
    }

    private void writeNoMediaFile() {
        File[] externalMediaDirs = SeadroidApplication.getAppContext().getExternalMediaDirs();
        String rootPath = externalMediaDirs[0].getAbsolutePath();
        File dirsFile = new File(rootPath);
        if (!dirsFile.exists()) {
            dirsFile.mkdirs();
        }
        File file = new File(dirsFile.toString(), ".nomedia");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
            }
        }

        try {
            FileWriter filerWriter = new FileWriter(file, true);
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        SeadroidApplication.context = this;
    }

    public static Context getAppContext() {
        return SeadroidApplication.context;
    }

    public static SeadroidApplication getInstance() {
        return instance;
    }

    public static void initImageLoader(Context context) {

        File cacheDir = StorageManager.getInstance().getThumbnailsDir();
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .diskCache(new UnlimitedDiscCache(cacheDir))
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .imageDownloader(new AuthImageDownloader(context, 10000, 10000))
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    public void initNotificationChannel() {
        String channelName = getString(R.string.channel_name_error);
        createNotificationChannel(CustomNotificationBuilder.CHANNEL_ID_ERROR, channelName, NotificationManager.IMPORTANCE_DEFAULT,false,true);

        channelName = getString(R.string.channel_name_upload);
        createNotificationChannel(CustomNotificationBuilder.CHANNEL_ID_UPLOAD, channelName, NotificationManager.IMPORTANCE_LOW,false,false);

        channelName = getString(R.string.channel_name_download);
        createNotificationChannel(CustomNotificationBuilder.CHANNEL_ID_DOWNLOAD, channelName, NotificationManager.IMPORTANCE_LOW,false,false);

    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance,boolean isVibrate, boolean hasSound ) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setShowBadge(true);
        channel.enableVibration(isVibrate);
        channel.enableLights(true);
        if (!hasSound) {
            channel.setSound(null, null);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    public void setCameraUploadNumber(int waitingNumber, int totalNumber) {
        this.waitingNumber = waitingNumber;
        this.totalNumber = totalNumber;
    }

    public int getWaitingNumber() {
        return waitingNumber;
    }

    public int getTotalNumber() {
        return totalNumber;
    }

    public void setScanUploadStatus(int scanUploadStatus) {
        this.scanUploadStatus = scanUploadStatus;
    }

    public int getScanUploadStatus() {
        return scanUploadStatus;
    }

    public int getTotalBackup() {
        return totalBackup;
    }

    public int getWaitingBackup() {
        return waitingBackup;
    }

    public void setFolderBackupNumber(int totalBackup, int waitingBackup) {
        this.totalBackup = totalBackup;
        this.waitingBackup = waitingBackup;
    }

    public String isActivityVisible() {
        return ProcessLifecycleOwner.get().getLifecycle().getCurrentState().name();
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//    public void onAppBackgrounded() {
//        //App in background
//
//        Log.e(TAG, "************* backgrounded");
//        Log.e(TAG, "************* " + isActivityVisible());
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_START)
//    public void  onAppForegrounded() {
//
//        Log.e(TAG, "************* foregrounded");
//        Log.e(TAG, "************* " + isActivityVisible());
//        // App in foreground
//    }

    public List<String> getLoginConfig() {
        return loginConfig;
    }

    public void setLoginConfig(List<String> _loginConfig) {
        loginConfig = _loginConfig;
    }

    public void saveGalleryPhotos(List<SeafPhoto> photos) {
        if (galleryPhotos == null) {
            galleryPhotos = Lists.newArrayList();
        }
        galleryPhotos.clear();
        for (SeafPhoto photo:photos) {
            galleryPhotos.add(Utils.pathJoin(photo.getRepoName() + Utils.pathJoin(photo.getDirPath(), photo.getName())));
        }
    }

    public void clearGalleryPhotos() {
        if (galleryPhotos != null) {
            galleryPhotos.clear();
        }
    }

    public List<String> getGalleryPhotos() {
        if (galleryPhotos == null) {
            galleryPhotos = Lists.newArrayList();
        }
        return galleryPhotos;
    }
}
