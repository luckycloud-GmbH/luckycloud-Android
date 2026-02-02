package com.seafile.seadroid2.folderbackup.selectfolder;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.common.collect.Maps;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.ui.dialog.CustomProgressDialog;
import com.seafile.seadroid2.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BeanListManager {
    public static final int TYPE_ADD_TAB_BAR = 0;
    public static final int TYPE_DEL_TAB_BAR = 1;
    public static final int TYPE_INIT_TAB_BAR = 2;

    private static final String DEFAULT_FALLBACK_STORAGE_PATH = "/storage/sdcard0";
    private static final String INTERNAL_SHARED_STORAGE = "Internal shared storage";
    public static final Pattern DIR_SEPARATOR = Pattern.compile("/");

    public static Map<String, String> storageNames = Maps.newHashMap();

    public static void upDataFileBeanListByAsyn(Activity at, List<String> selectFilePath,
                                                List<FileBean> fileBeanList, FileListAdapter fileListAdapter,
                                                String path, List<String> fileTypes, int sortType) {

        if (fileBeanList == null) {
            fileBeanList = new ArrayList<>();
        } else if (fileBeanList.size() != 0) {
            fileBeanList.clear();
        }

        Observable.just(fileBeanList).map(new Function<List<FileBean>, List<FileBean>>() {
                    @Override
                    public List<FileBean> apply(List<FileBean> fileBeanList) throws Throwable {
                        if (path.isEmpty()) {
                            List<String> paths;
                            if (SDK_INT >= N) {
                                paths = getStorageDirectoriesNew(at);
                            } else {
                                paths = getStorageDirectoriesLegacy(at);
                            }
                            FileBean fileBean;
                            for (String path : paths) {
                                fileBean = new FileBean(path);
                                fileBeanList.add(fileBean);
                            }
                            return fileBeanList;
                        } else {
                            FileBean fileBean;
                            File file = FileTools.getFileByPath(path);
                            File[] files = file.listFiles();
                            if (files != null) {
                                for (File value : files) {
                                    fileBean = new FileBean(value.getAbsolutePath());
                                    if (selectFilePath != null && selectFilePath.size() > 0) {
                                        if (selectFilePath.contains(fileBean.getFilePath())) {
                                            fileBean.setChecked(true);
                                        }
                                    }
                                    fileBeanList.add(fileBean);
                                }
                            }
                            sortFileBeanList(fileBeanList, sortType);
                            return fileBeanList;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<FileBean>>() {
                    public CustomProgressDialog pg;

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        pg = new CustomProgressDialog(at);
                        pg.setCancelable(false);
                        pg.setHeight((int) at.getResources().getDimension(R.dimen.normal_progress_logo_width));
                        pg.show();
                    }

                    @Override
                    public void onNext(@NonNull List<FileBean> fileBeans) {
                        if (fileListAdapter != null) {
                            fileListAdapter.updateListData(fileBeans, storageNames);
                            fileListAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        pg.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        pg.dismiss();
                    }
                });
    }

    @TargetApi(N)
    public static synchronized List<String> getStorageDirectoriesNew(Activity activity) {
        // Final set of paths
        storageNames.clear();
        ArrayList<String> volumes = new ArrayList<>();
        android.os.storage.StorageManager sm = activity.getSystemService(android.os.storage.StorageManager.class);
        for (StorageVolume volume : sm.getStorageVolumes()) {
            if (!volume.getState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)
                    && !volume.getState().equalsIgnoreCase(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                continue;
            }
            File path = Utils.getVolumeDirectory(volume);
            String name = volume.getDescription(activity);
            if (INTERNAL_SHARED_STORAGE.equalsIgnoreCase(name)) {
                name = activity.getString(R.string.internal_storage);
            }
            if (!volume.isRemovable()) {
                storageNames.put(path.getAbsolutePath(), activity.getString(R.string.internal_storage));
            } else {
                // HACK: There is no reliable way to distinguish USB and SD external storage
                // However it is often enough to check for "USB" String
                if (name.toUpperCase().contains("USB") || path.getPath().toUpperCase().contains("USB")) {
                    continue;
                } else {
                    storageNames.put(path.getAbsolutePath(), activity.getString(R.string.sdcard));
                }
            }
            volumes.add(path.getAbsolutePath());
        }
        return volumes;
    }

    public static synchronized List<String> getStorageDirectoriesLegacy(Activity activity) {
        storageNames.clear();
        List<String> rv = new ArrayList<>();

        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                // Check for actual existence of the directory before adding to list
                if (new File(DEFAULT_FALLBACK_STORAGE_PATH).exists()) {
                    rv.add(DEFAULT_FALLBACK_STORAGE_PATH);
                } else {
                    // We know nothing else, use Environment's fallback
                    rv.add(Environment.getExternalStorageDirectory().getAbsolutePath());
                }
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (SDK_INT < JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        if (SDK_INT >= M && checkStoragePermission(activity)) rv.clear();
        if (SDK_INT >= KITKAT) {
            ArrayList<String> strings = getExtSdCardPathsForActivity(activity);
            for (String s : strings) {
                File f = new File(s);
                if (!rv.contains(s) && canListFiles(f)) rv.add(s);
            }
        }

        for (String file : rv) {
            if ("/storage/emulated/legacy".equals(file)
                    || "/storage/emulated/0".equals(file)
                    || "/mnt/sdcard".equals(file)) {
                storageNames.put(file, activity.getString(R.string.internal_storage));
            } else if ("/storage/sdcard1".equals(file)) {
                storageNames.put(file, activity.getString(R.string.sdcard));
            } else if ("/".equals(file)) {
                rv.remove(file);
            } else {
                storageNames.put(file, activity.getString(R.string.sdcard));
            }
        }

        return rv;
    }

    public static boolean checkStoragePermission(Activity activity) {
        // Verify that all required contact permissions have been granted.
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return (ActivityCompat.checkSelfPermission(
                    activity, Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    == PackageManager.PERMISSION_GRANTED)
                    || (ActivityCompat.checkSelfPermission(
                    activity, Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    == PackageManager.PERMISSION_GRANTED)
                    || Environment.isExternalStorageManager();
        } else {
            return ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static boolean canListFiles(File f) {
        return f.canRead() && f.isDirectory();
    }

    public static ArrayList<String> getExtSdCardPathsForActivity(Context context) {
        ArrayList<String> paths = new ArrayList<>();
        for (File file:context.getExternalFilesDirs("external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("Warn", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    var path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = (new File(path)).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) paths.add("/storage/sdcard1");
        return paths;
    }

    public static void sortFileBeanList(List<FileBean> fileBeanList, int sortType) {
        Collections.sort(fileBeanList, new Comparator<FileBean>() {
            @Override
            public int compare(FileBean file1, FileBean file2) {

                if (file1.isDir() && file2.isFile())
                    return -1;
                if (file1.isFile() && file2.isDir())
                    return 1;

                switch (sortType) {
                    case Constants.SORT_NAME_ASC:
                        return file1.getFileName().compareToIgnoreCase(file2.getFileName());
                    case Constants.SORT_NAME_DESC:
                        return file2.getFileName().compareToIgnoreCase(file1.getFileName());
                    case Constants.SORT_TIME_ASC:
                        long diff = file1.getModifyTime() - file2.getModifyTime();
                        if (diff > 0)
                            return 1;
                        else if (diff == 0)
                            return 0;
                        else
                            return -1;
                    case Constants.SORT_TIME_DESC:
                        diff = file2.getModifyTime() - file1.getModifyTime();
                        if (diff > 0)
                            return 1;
                        else if (diff == 0)
                            return 0;
                        else
                            return -1;
                    case Constants.SORT_SIZE_ASC:
                        diff = file1.getSimpleSize() - file2.getSimpleSize();
                        if (diff > 0)
                            return 1;
                        else if (diff == 0)
                            return 0;
                        else
                            return -1;
                    case Constants.SORT_SIZE_DESC:
                        diff = file2.getSimpleSize() - file1.getSimpleSize();
                        if (diff > 0)
                            return 1;
                        else if (diff == 0)
                            return 0;
                        else
                            return -1;
                    default:
                        return 0;
                }
            }
        });
    }

    public static void getTabbarFileBeanList(List<TabBarFileBean> tabbarList,
                                             String path, List<String> allPathsList) {
        tabbarList.add(0, new TabBarFileBean(path,
                SeadroidApplication.getAppContext().getString(R.string.phone)));
//        if (allPathsList.contains(path)) {
//            tabbarList.add(1, new TabBarFileBean(path,
//                    SeadroidApplication.getAppContext().getString(R.string.internal_storage)));
//            return;
//        }
    }

    public static List<TabBarFileBean> upDataTabbarFileBeanList(List<TabBarFileBean> tabbarList,
                                                                TabBarFileListAdapter tabbarAdapter,
                                                                String path, int type, List<String> allPathsList,
                                                                CardView mTabBarMoreCard) {
        switch (type) {
            case TYPE_ADD_TAB_BAR:
                tabbarList.add(new TabBarFileBean(path));
                break;
            case TYPE_DEL_TAB_BAR:
                for (int i = tabbarList.size() - 1; i >= 0; i--) {
                    if (tabbarList.get(i).getFilePath().length() > path.length()) {
                        tabbarList.remove(i);
                    } else {
                        break;
                    }
                }
                break;
            case TYPE_INIT_TAB_BAR:
                if (tabbarList == null) {
                    tabbarList = new ArrayList<>();
                }
//                else {
//                    tabbarList.clear();
//                }
                getTabbarFileBeanList(tabbarList, path, allPathsList);
                break;
        }
        if (mTabBarMoreCard != null) {
            mTabBarMoreCard.setVisibility(tabbarList.size() == 1 ? View.GONE : View.VISIBLE);
        }
        if (tabbarAdapter != null) {
            tabbarAdapter.updateListData(tabbarList, storageNames);
            tabbarAdapter.notifyDataSetChanged();
        }
        return tabbarList;
    }
}

