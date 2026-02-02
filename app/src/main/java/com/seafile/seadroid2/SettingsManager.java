package com.seafile.seadroid2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.seafile.seadroid2.account.AccountManager;
import com.seafile.seadroid2.account.SupportAccountManager;
import com.seafile.seadroid2.folderbackup.selectfolder.Constants;
import com.seafile.seadroid2.gesturelock.LockPatternUtils;
import com.seafile.seadroid2.util.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * Access the app settings
 */
public final class SettingsManager {
    private static final String DEBUG_TAG = "SettingsManager";

    /**
     * sort files type
     */
    public static final int SORT_BY_NAME = 11;
    public static final int SORT_BY_LAST_MODIFIED_TIME = 12;
    public static final int SORT_ORDER_ASCENDING = 13;
    public static final int SORT_ORDER_DESCENDING = 14;

    /**
     * grid files type
     */
    public static final int GRID_BY_LIST = 1;
    public static final int GRID_BY_MINIMAL_LIST = 2;
    public static final int GRID_BY_SMALL_TILE = 3;
    public static final int GRID_BY_BIG_TILE = 4;

    public static final int TRANSFER_ALL = 4;
    public static final int TRANSFER_UPLOAD = 5;
    public static final int TRANSFER_DOWNLOAD = 6;

    private static SettingsManager instance;
    private static SharedPreferences settingsSharedPref;
    private static SharedPreferences sharedPref;
    private static SharedPreferences.Editor editor;

    private SettingsManager() {
        if (SeadroidApplication.getAppContext() != null) {
            settingsSharedPref = PreferenceManager.getDefaultSharedPreferences(SeadroidApplication.getAppContext());
            sharedPref = SeadroidApplication.getAppContext().getSharedPreferences(SupportAccountManager.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            editor = sharedPref.edit();
        }
    }


    // Account
    public static final String SETTINGS_ACCOUNT_INFO_KEY = "account_info_user_key";
    public static final String SETTINGS_ACCOUNT_SPACE_KEY = "account_info_space_key";
    public static final String SETTINGS_ACCOUNT_SIGN_OUT_KEY = "account_sign_out_key";

    // privacy category
    public static final String PRIVACY_CATEGORY_KEY = "category_privacy_key";

    // Client side encryption
    public static final String CLIENT_ENC_SWITCH_KEY = "client_encrypt_switch_key";
    public static final String CLEAR_PASSOWR_SWITCH_KEY = "clear_password_switch_key";
    public static final String AUTO_CLEAR_PASSOWR_SWITCH_KEY = "auto_clear_password_switch_key";
    public static final String EXCEPTION_ENCRYPTED_REPO_SWITCH_KEY = "exception_encrypted_repo_switch_key";
    public static final String THUMB_IN_ENC_REPO_SWITCH_KEY = "thumb_in_enc_repo_switch_key";
    public static final String SAVE_FILES_OF_THUMB_IN_CACHE_SWITCH_KEY = "save_files_of_thumb_in_cache_switch_key";
    public static final String ALL_ENC_THUMBS_COUNT = "all_enc_thumbs_count";
    public static final String ALL_ENC_IMAGES_COUNT = "all_enc_images_count";

    // Gesture Lock
    public static final String GESTURE_LOCK_SWITCH_KEY = "gesture_lock_switch_key";
    public static final String GESTURE_LOCK_KEY = "gesture_lock_key";
    public static final int GESTURE_LOCK_REQUEST = 11;
    public static final int GESTURE_UNLOCK_REQUEST = 12;

    // biometric auth
    public static final String BIOMETRIC_AUTH_SWITCH_KEY = "biometric_auth_switch_key";

    // auto lock
    public static final String AUTO_LOCK_WHEN_BACK = "auto_lock_when_back";
    public static final String AUTO_LOCK_WHEN_HOME = "auto_lock_when_home";
    public static final String AUTO_LOCK_WHEN_DEVICE_LOCK = "auto_lock_when_device_lock";
    public static final String AUTO_LOCK_EXPIRATION_TIME = "auto_lock_expiration_time";
    public static final String FORCE_DARK_MODE = "force_dark_mode";
    public static final String FORCE_LIGHT_MODE = "force_light_mode";
    public static final String BOTTOM_BAR_SIZE = "bottom_bar_size";
    public static final String EXECUTE_OCR_VALUE = "execute_ocr_value";
    public static final String OPEN_OFFICE_VALUE = "open_office_value";
    public static final String OPEN_PDF_VALUE = "open_pdf_value";
    public static final String CACHE_MAXIMUM_SIZE = "cache_maximum_size";

    public static final int BOTTOM_BAR_LARGE = 0;
    public static final int BOTTOM_BAR_MEDIUM = 1;
    public static final int BOTTOM_BAR_SMALL = 2;
    public static final int BOTTOM_BAR_NONE = 3;

    public static final int EXECUTE_OCR_ASK_EVERYTIME = 0;
    public static final int EXECUTE_OCR_ACTIVATED = 1;
    public static final int EXECUTE_OCR_DEACTIVATED = 2;

    public static final int OPEN_OFFICE_OPTIONAL = 0;
    public static final int OPEN_OFFICE_ONLY_OFFICE = 1;
    public static final int OPEN_OFFICE_IN_APP_OFFICE = 2;
    public static final int OPEN_OFFICE_EXTERNAL_APP = 3;

    public static final int OPEN_PDF_OPTIONAL = 0;
    public static final int OPEN_PDF_IN_APP_PDF = 1;
    public static final int OPEN_PDF_EXTERNAL_APP = 2;

    public static final int CACHE_MAXIMUM_SIZE_1 = 1;
    public static final int CACHE_MAXIMUM_SIZE_5 = 5;
    public static final int CACHE_MAXIMUM_SIZE_10 = 10;

    // Camera upload
    public static final String PKG = "com.seafile.seadroid2";

    public static final String SHARED_PREF_CONTACTS_UPLOAD_REPO_ID = PKG + ".contacts.repoid";
    public static final String SHARED_PREF_CONTACTS_UPLOAD_REPO_NAME = PKG + ".contacts.repoName";

    public static final String SHARED_PREF_STORAGE_DIR = PKG + ".storageId";

    public static final String SHARED_PREF_CAMERA_UPLOAD_REPO_ID = PKG + ".camera.repoid";
    public static final String SHARED_PREF_CAMERA_UPLOAD_REPO_NAME = PKG + ".camera.repoName";
    public static final String SHARED_PREF_CAMERA_UPLOAD_ACCOUNT_EMAIL = PKG + ".camera.account.email";
    public static final String SHARED_PREF_CAMERA_UPLOAD_ACCOUNT_NAME = PKG + ".camera.account.name";
    public static final String SHARED_PREF_CAMERA_UPLOAD_ACCOUNT_SERVER = PKG + ".camera.account.server";
    public static final String SHARED_PREF_CAMERA_UPLOAD_ACCOUNT_TOKEN = PKG + ".camera.account.token";
    public static final String CAMERA_UPLOAD_SWITCH_KEY = "camera_upload_switch_key";
    public static final String CAMERA_UPLOAD_REPO_KEY = "camera_upload_repo_key";
    public static final String CAMERA_UPLOAD_ADVANCED_SCREEN_KEY = "screen_camera_upload_advanced_feature";
    public static final String CAMERA_UPLOAD_ADVANCED_CATEGORY_KEY = "category_camera_upload_advanced_key";
    public static final String CAMERA_UPLOAD_ALLOW_DATA_PLAN_SWITCH_KEY = "allow_data_plan_switch_key";

    public static final String CAMERA_UPLOAD_ALLOW_VIDEOS_SWITCH_KEY = "allow_videos_upload_switch_key";
    public static final String CAMERA_UPLOAD_BUCKETS_KEY = "camera_upload_buckets_key";
    public static final String CAMERA_UPLOAD_CATEGORY_KEY = "category_camera_upload_key";
    public static final String CAMERA_UPLOAD_CUSTOM_BUCKETS_KEY = "camera_upload_buckets_switch_key";
    public static final String SHARED_PREF_CAMERA_UPLOAD_BUCKETS = PKG + ".camera.buckets";
    //contacts
    public static final String CONTACTS_UPLOAD_CATEGORY_KEY = "category_contacts_upload_key";
    public static final String CONTACTS_UPLOAD_SWITCH_KEY = "contacts_upload_switch_key";

    //ABOUT
    public static final String SETTINGS_ABOUT_CATEGORY_KEY = "settings_section_about_key";
    public static final String SETTINGS_ABOUT_VERSION_KEY = "settings_about_version_key";
    public static final String SETTINGS_ABOUT_AUTHOR_KEY = "settings_about_author_key";
    public static final String SETTINGS_PRIVACY_POLICY_KEY = "settings_privacy_policy_key";
    public static final String CONTACTS_UPLOAD_REPO_KEY = "contacts_upload_repo_key";
    public static final String CONTACTS_UPLOAD_REPO_TIME_KEY = "contacts_upload_repo_time_key";
    public static final String CONTACTS_UPLOAD_REPO_BACKUP_KEY = "contacts_upload_repo_backup_key";
    public static final String CONTACTS_UPLOAD_REPO_RECOVERY_KEY = "contacts_upload_repo_recovery_key";

    // Cache
    public static final String SETTINGS_CACHE_CATEGORY_KEY = "settings_cache_key";
    public static final String SETTINGS_CACHE_SIZE_KEY = "settings_cache_info_key";
    public static final String SETTINGS_CLEAR_CACHE_KEY = "settings_clear_cache_key";
    public static final String SETTINGS_CACHE_DIR_KEY = "settings_cache_location_key";
    public static final String CAMERA_UPLOAD_STATE = "camera_upload_state";
    public static final String DELETE_CACHE_AUTOMATIC = "delete_cache_automatic";

    // Download
    public static final String DOWNLOAD_DATA_LOCATION = "download_data_location";
    public static final String DOWNLOAD_DATA_LOCATION_DEFAULT =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
            "/" +
            SeadroidApplication.getAppContext().getString(R.string.app_name);

    // Grid files
    public static final String GRID_FILES_TYPE = "grid_files_type";

    // Repo type
    public static final String REPO_TYPE_PERSONAL = "repo_type_personal";
    public static final String REPO_TYPE_GROUP = "repo_type_group";
    public static final String REPO_TYPE_SHARED = "repo_type_shared";

    // Sort files
    public static final String SORT_FILES_TYPE = "sort_files_type";
    public static final String SORT_FILES_ORDER = "sort_files_order";

    // Backup Sort files
    public static final String BACKUP_SORT_TYPE = "backup_sort_type";

    // Transfer type
    public static final String TRANSFER_TYPE = "transfer_type";

    //CameraSyncStatus
    public static final String WAITING_UPLOAD_NUMBER = "waiting_upload_number";
    public static final String TOTAL_UPLOAD_NUMBER = "total_upload_number";
    public static final String PIC_CHECK_START = "pic_check_start";
    public static final String UPLOAD_COMPLETED_TIME = "upload_completed_time";

    //FolderBackupStatus
    public static final String FOLDER_BACKUP_SWITCH_KEY = "folder_backup_switch_key";
    public static final String FOLDER_BACKUP_ALLOW_DATA_PLAN_SWITCH_KEY = "folder_backup_allow_data_plan_switch_key";
    public static final String FOLDER_AUTOMATIC_BACKUP_SWITCH_KEY = "folder_automatic_backup_switch_key";
    public static final String FOLDER_BACKUP_ACCOUNT_EMAIL = "folder_backup_account_email";
    public static final String FOLDER_BACKUP_CATEGORY_KEY = "folder_backup_category_key";
    public static final String FOLDER_BACKUP_MODE = "folder_backup_mode";
    public static final String FOLDER_BACKUP_LIBRARY_KEY = "folder_backup_library_key";
    public static final String SELECTED_BACKUP_FOLDERS_KEY = "selected_backup_folders_key";
    public static final String FOLDER_BACKUP_STATE = "folder_backup_state";
    public static final String FOLDER_BACKUP_PATHS = "folder_backup_paths";
    public static final String FOLDER_BACKUP_COMPLETED_TIME = "folder_backup_completed_time";
    public static final String FOLDER_BACKUP_COMPLETED_TOTAL = "folder_backup_completed_total";
    public static final String FOLDER_BACKUP_SAVE_TO_LOCAL = "folder_backup_save_to_local";

    public static final String PDF_HIGHLIGHT_THICKNESS = "pdf_highlight_thickness";
    public static final String PDF_HIGHLIGHT_COLOR = "pdf_highlight_color";
    public static final String PDF_FONT_SIZE = "pdf_font_size";
    public static final String PDF_FONT_COLOR = "pdf_font_color";
    public static final String PDF_INK_THICKNESS = "pdf_ink_thickness";
    public static final String PDF_INK_OPACITY = "pdf_ink_opacity";
    public static final String PDF_INK_COLOR = "pdf_ink_color";

    public static long lock_timestamp = 0;
    public static boolean gIsLoginedUser = false;

    //public static final long LOCK_EXPIRATION_MSECS = 5 * 60 * 1000;
    public static final long LOCK_EXPIRATION_MSECS = 500;

    public static final String PRIVACY_POLICY_CONFIRMED = "privacy_policy_confirmed";

    public static SettingsManager instance() {
        if (instance == null) {
            synchronized (SettingsManager.class) {
                if (instance == null) {
                    instance = new SettingsManager();
                }
            }
        }

        if (settingsSharedPref == null) {
            settingsSharedPref = PreferenceManager.getDefaultSharedPreferences(SeadroidApplication.getAppContext());
        }
        if (sharedPref == null) {
            sharedPref = SeadroidApplication.getAppContext().getSharedPreferences(SupportAccountManager.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            editor = sharedPref.edit();
        }
        return instance;
    }


    public void registerSharedPreferencesListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        settingsSharedPref.registerOnSharedPreferenceChangeListener(listener);
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterSharedPreferencesListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        settingsSharedPref.unregisterOnSharedPreferenceChangeListener(listener);
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Client side encryption only support for encrypted library
     */
    public void setupEncrypt(boolean enable) {
        settingsSharedPref.edit().putBoolean(CLIENT_ENC_SWITCH_KEY, enable).commit();
    }

    /**
     * Whether the user has enabled client side encryption
     */
    public boolean isEncryptEnabled() {
        return settingsSharedPref.getBoolean(CLIENT_ENC_SWITCH_KEY, true);
    }

    /**
     * Auto clear password
     */
    public void setupPasswordAutoClear(boolean enable) {
        settingsSharedPref.edit().putBoolean(AUTO_CLEAR_PASSOWR_SWITCH_KEY, enable).commit();
    }

    /**
     * Whether the user has enabled password auto clear when logout account
     */
    public boolean isPasswordAutoClearEnabled() {
        return settingsSharedPref.getBoolean(AUTO_CLEAR_PASSOWR_SWITCH_KEY, false);
    }

    public void setupExceptionEncryptedRepo(boolean enable) {
        settingsSharedPref.edit().putBoolean(EXCEPTION_ENCRYPTED_REPO_SWITCH_KEY, enable).commit();
    }

    public boolean isExceptionEncryptedRepo() {
        return settingsSharedPref.getBoolean(EXCEPTION_ENCRYPTED_REPO_SWITCH_KEY, true);
    }

    public void setupThumbInEncRepo(boolean enable) {
        settingsSharedPref.edit().putBoolean(THUMB_IN_ENC_REPO_SWITCH_KEY, enable).commit();
    }

    public boolean isThumbInEncRepo() {
        return settingsSharedPref.getBoolean(THUMB_IN_ENC_REPO_SWITCH_KEY, false);
    }

    public void setupSaveFilesOfThumbInCache(boolean enable) {
        settingsSharedPref.edit().putBoolean(SAVE_FILES_OF_THUMB_IN_CACHE_SWITCH_KEY, enable).commit();
    }

    public boolean isSaveFilesOfThumbInCache() {
        return settingsSharedPref.getBoolean(SAVE_FILES_OF_THUMB_IN_CACHE_SWITCH_KEY, false);
    }

    public void setupAllEncThumbsCount(int count) {
        settingsSharedPref.edit().putInt(ALL_ENC_THUMBS_COUNT, count).commit();
    }

    public int getAllEncThumbsCount() {
        return settingsSharedPref.getInt(ALL_ENC_THUMBS_COUNT, -1);
    }

    public void setupAllEncImagesCount(int count) {
        settingsSharedPref.edit().putInt(ALL_ENC_IMAGES_COUNT, count).commit();
    }

    public int getAllEncImagesCount() {
        return settingsSharedPref.getInt(ALL_ENC_IMAGES_COUNT, -1);
    }

    public void setupGestureLock() {
        settingsSharedPref.edit().putBoolean(GESTURE_LOCK_SWITCH_KEY, true).commit();
        saveGestureLockTimeStamp();
    }

    public void setupGestureUnlock() {
        settingsSharedPref.edit().putBoolean(GESTURE_LOCK_SWITCH_KEY, false).commit();
    }

    /**
     * Whether the user has setup a gesture lock or not
     */
    public boolean isGestureLockEnabled() {
        LockPatternUtils mLockPatternUtils = new LockPatternUtils(SeadroidApplication.getAppContext());
        return settingsSharedPref.getBoolean(GESTURE_LOCK_SWITCH_KEY, false) && mLockPatternUtils.savedPatternExists();
    }

    /**
     * For convenience, if the user has given the correct gesture lock, he
     * would not be asked for gesture lock for a short period of time.
     */
    public boolean isGestureLockRequired() {
        if (!isGestureLockEnabled()) {
            if (!isBiometricAuthEnabled()) return false;
        } else {
            LockPatternUtils mLockPatternUtils = new LockPatternUtils(SeadroidApplication.getAppContext());
            if (!mLockPatternUtils.savedPatternExists()) {
                return false;
            }
        }

        long now = System.currentTimeMillis();
        if (now < lock_timestamp + getAutoLockExpirationTime()) {
            return false;
        }

        return true;
    }

    public void saveGestureLockTimeStamp() {
        lock_timestamp = System.currentTimeMillis();
    }

    public void savePreviousGestureLockTimeStamp() {
        lock_timestamp = System.currentTimeMillis() - getAutoLockExpirationTime();
    }

    /**
     * set Biometric Auth
     */
    public void setupBiometricAuth(boolean enable) {
        settingsSharedPref.edit().putBoolean(BIOMETRIC_AUTH_SWITCH_KEY, enable)
                .commit();
    }

    /**
     * Whether the user has setup a biometric auth or not
     */
    public boolean isBiometricAuthEnabled() {
        if (settingsSharedPref.getBoolean(BIOMETRIC_AUTH_SWITCH_KEY, false) == false)
            return false;
        return true;
    }

    public void setupAutoLockWhenBack(boolean enable) {
        settingsSharedPref.edit().putBoolean(AUTO_LOCK_WHEN_BACK, enable).commit();
    }
    public boolean isAutoLockWhenBack() {
        return settingsSharedPref.getBoolean(AUTO_LOCK_WHEN_BACK, true) ;
    }

    public void setupAutoLockWhenHome(boolean enable) {
        settingsSharedPref.edit().putBoolean(AUTO_LOCK_WHEN_HOME, enable).commit();
    }
    public boolean isAutoLockWhenHome() {
        return settingsSharedPref.getBoolean(AUTO_LOCK_WHEN_HOME, true) ;
    }

    public void setupAutoLockWhenDeviceLock(boolean enable) {
        settingsSharedPref.edit().putBoolean(AUTO_LOCK_WHEN_DEVICE_LOCK, enable).commit();
    }

    public boolean isAutoLockWhenDeviceLock() {
        return settingsSharedPref.getBoolean(AUTO_LOCK_WHEN_DEVICE_LOCK, true) ;
    }

    public void setupAutoLockExpirationTime(long expirationTime) {
        settingsSharedPref.edit().putLong(AUTO_LOCK_EXPIRATION_TIME, expirationTime).commit();
    }

    public long getAutoLockExpirationTime() {
        return settingsSharedPref.getLong(AUTO_LOCK_EXPIRATION_TIME, 500) ;
    }

    public void setupForceDarkMode(boolean enable) {
        settingsSharedPref.edit().putBoolean(FORCE_DARK_MODE, enable).commit();
    }

    public boolean isForceDarkMode() {
        return settingsSharedPref.getBoolean(FORCE_DARK_MODE, false) ;
    }

    public void setupForceLightMode(boolean enable) {
        settingsSharedPref.edit().putBoolean(FORCE_LIGHT_MODE, enable).commit();
    }

    public boolean isForceLightMode() {
        return settingsSharedPref.getBoolean(FORCE_LIGHT_MODE, false) ;
    }

    public void setupBottomBarSize(int bottomBarSize) {
        settingsSharedPref.edit().putInt(BOTTOM_BAR_SIZE, bottomBarSize).commit();
    }

    public int getBottomBarSize() {
        return settingsSharedPref.getInt(BOTTOM_BAR_SIZE, BOTTOM_BAR_MEDIUM) ;
    }

    public void setupExecuteOcrValue(int executeOcrValue) {
        settingsSharedPref.edit().putInt(EXECUTE_OCR_VALUE, executeOcrValue).commit();
    }

    public int getExecuteOcrValue() {
        return settingsSharedPref.getInt(EXECUTE_OCR_VALUE, EXECUTE_OCR_ASK_EVERYTIME) ;
    }

    public void setupOpenOfficeValue(int openOfficeValue) {
        settingsSharedPref.edit().putInt(OPEN_OFFICE_VALUE, openOfficeValue).commit();
    }

    public int getOpenOfficeValue() {
        return settingsSharedPref.getInt(OPEN_OFFICE_VALUE, OPEN_OFFICE_OPTIONAL) ;
    }

    public void setupOpenPdfValue(int openPdfValue) {
        settingsSharedPref.edit().putInt(OPEN_PDF_VALUE, openPdfValue).commit();
    }

    public int getOpenPdfValue() {
        return settingsSharedPref.getInt(OPEN_PDF_VALUE, OPEN_PDF_OPTIONAL) ;
    }

    public String getCameraUploadRepoName() {
        return sharedPref.getString(SHARED_PREF_CAMERA_UPLOAD_REPO_NAME, null);
    }

    public String getContactsUploadRepoName() {
        return sharedPref.getString(SHARED_PREF_CONTACTS_UPLOAD_REPO_NAME, null);
    }

    public void saveCameraUploadRepoInfo(String repoId, String repoName) {
        editor.putString(SHARED_PREF_CAMERA_UPLOAD_REPO_ID, repoId);
        editor.putString(SHARED_PREF_CAMERA_UPLOAD_REPO_NAME, repoName);
        editor.commit();
    }

    public boolean checkCameraUploadNetworkAvailable() {
        if (!Utils.isNetworkOn()) {
            return false;
        }
        // user does not allow mobile connections
        if (!Utils.isWiFiOn() && !isDataPlanAllowed()) {
            return false;
        }
        // Wi-Fi or 2G/3G/4G connections available
        return true;
    }

    public boolean isDataPlanAllowed() {
        return settingsSharedPref.getBoolean(CAMERA_UPLOAD_ALLOW_DATA_PLAN_SWITCH_KEY, false);
    }

    public boolean isFolderBackupDataPlanAllowed() {
        return settingsSharedPref.getBoolean(FOLDER_BACKUP_ALLOW_DATA_PLAN_SWITCH_KEY, false);
    }

    public boolean isVideosUploadAllowed() {
        return settingsSharedPref.getBoolean(CAMERA_UPLOAD_ALLOW_VIDEOS_SWITCH_KEY, false);
    }

    public void saveDataPlanAllowed(boolean isAllowed) {
        settingsSharedPref.edit().putBoolean(CAMERA_UPLOAD_ALLOW_DATA_PLAN_SWITCH_KEY, isAllowed).commit();
    }

    public void saveFolderBackupDataPlanAllowed(boolean isAllowed) {
        settingsSharedPref.edit().putBoolean(FOLDER_BACKUP_ALLOW_DATA_PLAN_SWITCH_KEY, isAllowed).commit();
    }

    public void saveFolderAutomaticBackup(boolean isAllowed) {
        settingsSharedPref.edit().putBoolean(FOLDER_AUTOMATIC_BACKUP_SWITCH_KEY, isAllowed).commit();
    }

    public boolean isFolderAutomaticBackup() {
        return settingsSharedPref.getBoolean(FOLDER_AUTOMATIC_BACKUP_SWITCH_KEY, false);
    }

    public void saveFolderBackupSaveToCache(boolean isAllowed) {
        settingsSharedPref.edit().putBoolean(FOLDER_BACKUP_SAVE_TO_LOCAL, isAllowed).commit();
    }

    public boolean isFolderBackupSaveToCache() {
        return settingsSharedPref.getBoolean(FOLDER_BACKUP_SAVE_TO_LOCAL, false);
    }

    public void saveVideosAllowed(boolean isVideosUploadAllowed) {
        settingsSharedPref.edit().putBoolean(CAMERA_UPLOAD_ALLOW_VIDEOS_SWITCH_KEY, isVideosUploadAllowed).commit();
    }

    public void saveGridFilesPref(int type) {
        editor.putInt(GRID_FILES_TYPE, type).commit();
    }

    public void saveTransferPref(int type) {
        editor.putInt(TRANSFER_TYPE, type).commit();
    }

    public void saveRepoTypePersonalPref(boolean type) {
        editor.putBoolean(REPO_TYPE_PERSONAL, type).commit();
    }

    public void saveRepoTypeGroupPref(boolean type) {
        editor.putBoolean(REPO_TYPE_GROUP, type).commit();
    }

    public void saveRepoTypeSharedPref(boolean type) {
        editor.putBoolean(REPO_TYPE_SHARED, type).commit();
    }

    public void saveSortFilesPref(int type, int order) {
        editor.putInt(SORT_FILES_TYPE, type).commit();
        editor.putInt(SORT_FILES_ORDER, order).commit();
    }

    public void saveBackupSortTypePref(int type) {
        editor.putInt(BACKUP_SORT_TYPE, type).commit();
    }

    public void setCameraUploadBucketList(List<String> list) {
        String s = TextUtils.join(",", list);
        sharedPref.edit().putString(SHARED_PREF_CAMERA_UPLOAD_BUCKETS, s).commit();
    }

    /**
     * @return list of bucket IDs that have been selected for upload. Empty list means "all buckets"
     */
    public List<String> getCameraUploadBucketList() {
        String s = sharedPref.getString(SHARED_PREF_CAMERA_UPLOAD_BUCKETS, "");
        return Arrays.asList(TextUtils.split(s, ","));
    }

    public int getGridFilesTypePref() {
        return sharedPref.getInt(GRID_FILES_TYPE, GRID_BY_LIST);
    }

    public int getGridFilesColumns(int gridFilesType) {
        int columns = 1;
        switch (gridFilesType) {
            case GRID_BY_SMALL_TILE:
                columns = 3;
                break;
            case GRID_BY_BIG_TILE:
                columns = 2;
                break;
            default:
                break;
        }
        return columns;
    }

    public int getTransferTypePref() {
        return sharedPref.getInt(TRANSFER_TYPE, TRANSFER_ALL);
    }

    public boolean getRepoTypePersonalPref() {
        return sharedPref.getBoolean(REPO_TYPE_PERSONAL, true);
    }

    public boolean getRepoTypeGroupPref() {
        return sharedPref.getBoolean(REPO_TYPE_GROUP, true);
    }

    public boolean getRepoTypeSharedPref() {
        return sharedPref.getBoolean(REPO_TYPE_SHARED, true);
    }

    public int getSortFilesTypePref() {
        return sharedPref.getInt(SORT_FILES_TYPE, SORT_BY_LAST_MODIFIED_TIME);
    }

    public int getSortFilesOrderPref() {
        return sharedPref.getInt(SORT_FILES_ORDER, SORT_ORDER_DESCENDING);
    }

    public int getBackupSortTypePref() {
        return sharedPref.getInt(BACKUP_SORT_TYPE, Constants.SORT_TIME_DESC);
    }

    public String getCameraUploadRepoId() {
        return sharedPref.getString(SHARED_PREF_CAMERA_UPLOAD_REPO_ID, null);
    }

    public String getContactsUploadRepoId() {
        return sharedPref.getString(SHARED_PREF_CONTACTS_UPLOAD_REPO_ID, null);
    }

    public int getStorageDir() {
        return sharedPref.getInt(SHARED_PREF_STORAGE_DIR, Integer.MIN_VALUE);
    }

    public void setStorageDir(int dir) {
        editor.putInt(SHARED_PREF_STORAGE_DIR, dir).commit();
    }

    public void saveContactsUploadRepoInfo(String repoId, String repoName) {
        editor.putString(SHARED_PREF_CONTACTS_UPLOAD_REPO_ID, repoId);
        editor.putString(SHARED_PREF_CONTACTS_UPLOAD_REPO_NAME, repoName);
        editor.commit();
    }

    public void saveUploadCompletedTime(String completedTime) {
        editor.putString(UPLOAD_COMPLETED_TIME, completedTime);
        editor.commit();
    }

    public String getUploadCompletedTime() {
        return sharedPref.getString(UPLOAD_COMPLETED_TIME, null);
    }

    public void savePrivacyPolicyConfirmed(int type) {
        editor.putInt(PRIVACY_POLICY_CONFIRMED, type).commit();
    }

    public int getPrivacyPolicyConfirmed() {
        return sharedPref.getInt(PRIVACY_POLICY_CONFIRMED, 0);
    }

    public void saveBackupPaths(String path) {
        editor.putString(FOLDER_BACKUP_PATHS, path);
        editor.commit();
    }

    public String getBackupPaths() {
        return sharedPref.getString(FOLDER_BACKUP_PATHS, null);
    }

    public void saveBackupEmail(String path) {
        editor.putString(FOLDER_BACKUP_ACCOUNT_EMAIL, path);
        editor.commit();
    }

    public String getBackupEmail() {
        return sharedPref.getString(FOLDER_BACKUP_ACCOUNT_EMAIL, null);
    }

    public void saveBackupCompletedTime(String completedTime) {
        editor.putString(FOLDER_BACKUP_COMPLETED_TIME, completedTime);
        editor.commit();
    }

    public String getBackupCompletedTime() {
        return sharedPref.getString(FOLDER_BACKUP_COMPLETED_TIME, "");
    }

    public void saveBackupCompletedTotal(int totalBackup) {
        editor.putInt(FOLDER_BACKUP_COMPLETED_TOTAL, totalBackup);
        editor.commit();
    }

    public int getBackupCompletedTotal() {
        return sharedPref.getInt(FOLDER_BACKUP_COMPLETED_TOTAL, 0);
    }

    public void saveDownloadDataLocation(String path) {
        editor.putString(DOWNLOAD_DATA_LOCATION, path);
        editor.commit();
    }

    public String getDownloadDataLocation() {
        return sharedPref.getString(DOWNLOAD_DATA_LOCATION, DOWNLOAD_DATA_LOCATION_DEFAULT);
    }

    public void savePdfHighlightThickness(int thickness) {
        editor.putInt(PDF_HIGHLIGHT_THICKNESS, thickness);
        editor.commit();
    }

    public int getPdfHighlightThickness() {
        return sharedPref.getInt(PDF_HIGHLIGHT_THICKNESS, 16);
    }

    public void savePdfHighlightColor(int color) {
        editor.putInt(PDF_HIGHLIGHT_COLOR, color);
        editor.commit();
    }

    public int getPdfHighlightColor() {
        return sharedPref.getInt(PDF_HIGHLIGHT_COLOR, Color.parseColor("#000000"));
    }

    public void savePdfInkThickness(int thickness) {
        editor.putInt(PDF_INK_THICKNESS, thickness);
        editor.commit();
    }

    public int getPdfInkThickness() {
        return sharedPref.getInt(PDF_INK_THICKNESS, 10);
    }

    public void savePdfFontSize(int size) {
        editor.putInt(PDF_FONT_SIZE, size);
        editor.commit();
    }

    public int getPdfFontSize() {
        return sharedPref.getInt(PDF_FONT_SIZE, 10);
    }

    public void savePdfFontColor(int color) {
        editor.putInt(PDF_FONT_COLOR, color);
        editor.commit();
    }

    public int getPdfFontColor() {
        return sharedPref.getInt(PDF_FONT_COLOR, Color.parseColor("#000000"));
    }

    public void savePdfInkOpacity(int opacity) {
        editor.putInt(PDF_INK_OPACITY, opacity);
        editor.commit();
    }

    public int getPdfInkOpacity() {
        return sharedPref.getInt(PDF_INK_OPACITY, 100);
    }

    public void savePdfInkColor(int color) {
        editor.putInt(PDF_INK_COLOR, color);
        editor.commit();
    }

    public int getPdfInkColor() {
        return sharedPref.getInt(PDF_INK_COLOR, Color.parseColor("#000000"));
    }

    public void setupDeleteCacheAutomatic(boolean enable) {
        settingsSharedPref.edit().putBoolean(DELETE_CACHE_AUTOMATIC, enable).commit();
    }

    public boolean isDeleteCacheAutomatic() {
        return settingsSharedPref.getBoolean(DELETE_CACHE_AUTOMATIC, false);
    }

    public void setupCacheMaximumSize(int cacheMaximumSize) {
        settingsSharedPref.edit().putInt(CACHE_MAXIMUM_SIZE, cacheMaximumSize).commit();
    }

    public int getCacheMaximumSize() {
        return settingsSharedPref.getInt(CACHE_MAXIMUM_SIZE, CACHE_MAXIMUM_SIZE_1) ;
    }
}
