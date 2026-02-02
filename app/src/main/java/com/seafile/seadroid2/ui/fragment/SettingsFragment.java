package com.seafile.seadroid2.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.account.AccountInfo;
import com.seafile.seadroid2.account.AccountManager;
import com.seafile.seadroid2.account.ui.AccountDetailActivity;
import com.seafile.seadroid2.cameraupload.CameraUploadConfigActivity;
import com.seafile.seadroid2.cameraupload.CameraUploadManager;
import com.seafile.seadroid2.cameraupload.GalleryBucketUtils;
import com.seafile.seadroid2.data.CameraSyncEvent;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.DatabaseHelper;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.data.ServerInfo;
import com.seafile.seadroid2.data.StorageManager;
import com.seafile.seadroid2.folderbackup.FolderBackupConfigActivity;
import com.seafile.seadroid2.folderbackup.FolderBackupDBHelper;
import com.seafile.seadroid2.folderbackup.FolderBackupEvent;
import com.seafile.seadroid2.folderbackup.RepoConfig;
import com.seafile.seadroid2.folderbackup.selectfolder.StringTools;
import com.seafile.seadroid2.gesturelock.LockPatternUtils;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.activity.CreateGesturePasswordActivity;
import com.seafile.seadroid2.folderbackup.FolderBackupResultActivity;
import com.seafile.seadroid2.ui.activity.PrivacyPolicyActivity;
import com.seafile.seadroid2.ui.activity.SeafilePathChooserActivity;
import com.seafile.seadroid2.ui.activity.UnlockGesturePasswordActivity;
import com.seafile.seadroid2.ui.dialog.ClearCacheTaskDialog;
import com.seafile.seadroid2.ui.dialog.ClearPasswordTaskDialog;
import com.seafile.seadroid2.ui.dialog.DownloadLocationDialog;
import com.seafile.seadroid2.ui.dialog.LogoutDialog;
import com.seafile.seadroid2.ui.dialog.SwitchStorageTaskDialog;
import com.seafile.seadroid2.ui.dialog.TaskDialog;
import com.seafile.seadroid2.ui.dialog.TaskDialog.TaskDialogListener;
import com.seafile.seadroid2.util.CameraSyncStatus;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.SeafileLog;
import com.seafile.seadroid2.util.Utils;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

public class SettingsFragment extends CustomPreferenceFragment {
    private static final String DEBUG_TAG = "SettingsFragment";

    public static final String TAG_LOGOUT_DIALOG_FRAGMENT = "logoutDialogFragmentTabT";
    public static final String CAMERA_UPLOAD_BOTH_PAGES = "com.seafile.seadroid2.camera.upload";
    public static final String CAMERA_UPLOAD_REMOTE_LIBRARY = "com.seafile.seadroid2.camera.upload.library";
    public static final String CAMERA_UPLOAD_LOCAL_DIRECTORIES = "com.seafile.seadroid2.camera.upload.directories";
    public static final String CONTACTS_UPLOAD_REMOTE_LIBRARY = "com.seafile.seadroid2.contacts.upload.library";
    public static final String FOLDER_BACKUP_REMOTE_PAGES = "com.seafile.seadroid2.folder.backup.upload";
    public static final String FOLDER_BACKUP_REMOTE_MODE = "com.seafile.seadroid2.folder.backup.mode";
    public static final String FOLDER_BACKUP_REMOTE_PATH = "com.seafile.seadroid2.folder.backup.path";
    public static final String FOLDER_BACKUP_REMOTE_LIBRARY = "com.seafile.seadroid2.folder.backup.library";
    public static final int CHOOSE_CAMERA_UPLOAD_REQUEST = 2;
    public static final int CHOOSE_BACKUP_UPLOAD_REQUEST = 5;
    public static final int CHOOSE_DOWNLOAD_LOCATION_REQUEST = 6;
    private static final int CHOOSE_COPY_MOVE_DEST_REQUEST = 7;
    //    public static final int CHOOSE_CONTACTS_UPLOAD_REQUEST = 3;
    // Account Info
    private static Map<String, AccountInfo> accountInfoMap = Maps.newHashMap();

    //Biometric Auth
    public static final int BIOMETRIC_AUTH_NO_SUPPORTED = -1;
    public static final int BIOMETRIC_AUTH_NO_SET = 0;
    public static final int BIOMETRIC_AUTH_DEVICE_CREDENTIAL = 1;
    public static final int BIOMETRIC_AUTH_STRONG = 2;


    // privacy

    private BrowserActivity mActivity;
    private String appVersion;
    public SettingsManager settingsMgr;
    private CameraUploadManager cameraManager;
    //    public ContactsUploadManager contactsManager;
    private AccountManager accountMgr;
    private DataManager dataMgr;
    private StorageManager storageManager = StorageManager.getInstance();
    //    private PreferenceCategory cContactsCategory;
//    private Preference cContactsRepoPref;
//    private Preference cContactsRepoTime;
//    private Preference cContactsRepoBackUp;
//    private Preference cContactsRepoRecovery;
    private long mMtime;

    private View mAccountInfoLayout;
    private ViewGroup mAccountInfoCollapseLayout;
    private ImageView mAccountInfoDirectionImage;
    private TextView mAccountInfoUserSummary;
    private CardView mSwitchAccountCard;
    private TextView mAccountInfoSpaceSummary;

    private View mAppSecurityLayout;
    private ViewGroup mAppSecurityCollapseLayout;
    private ImageView mAppSecurityDirectionImage;
    private View mGestureLockLayout;
    private SwitchCompat mGestureLockSwitch;
    private CardView mGestureLockCard;
    private View mBiometricAuthLayout;
    private SwitchCompat mBiometricAuthSwitch;
    private CardView mBiometricAuthCard;
    private View mGestureBiometricServiceLayout;
    private SwitchCompat mWhenBackSwitch;
    private SwitchCompat mWhenHomeSwitch;
    private SwitchCompat mWhenDeviceLockSwitch;
    private CardView mExpirationTimeCard;
    private TextView mExpirationTimeText;
    private PopupWindow mExpirationDropDown = null;
    private CardView mClearLibraryPasswordCard;
    private SwitchCompat mAutoClearPasswordSwitch;
    private View mExceptionEncryptedRepoLayout;
    private SwitchCompat mExceptionEncryptedRepoSwitch;
    private View mClientEncryptLayout;
    private TextView mClientEncryptSummary;
    private SwitchCompat mClientEncryptSwitch;
    private View mBackupFolderLayout;
    private ImageView mBackupFolderDirectionImage;
    private ViewGroup mBackupFolderCollapseLayout;
    private TextView mBackupFolderSummary;
    private SwitchCompat mBackupFolderSwitch;
    private CardView mBackupFolderCard;
    private View mBackupFolderServiceLayout;
    private CardView mBackupFolderModeCard;
    private TextView mBackupFolderModeSummary;
    private CardView mBackupFolderRepoCard;
    private TextView mBackupFolderRepoSummary;
    private CardView mBackupFolderPrefCard;
    private TextView mBackupFolderPrefSummary;
    private CardView mBackupFolderStateCard;
    private TextView mBackupFolderStateSummary;
    private SwitchCompat mBackupFolderSaveToCacheSwitch;
    private CardView mBackupFolderResetCard;
    private Account act;
    private List<String> backupSelectPaths;
    private FolderBackupDBHelper databaseHelper;
    private RepoConfig selectRepoConfig;
    private TextView mCameraUploadSummary;
    private SwitchCompat mCameraUploadSwitch;
    private CardView mCameraUploadCard;
    private View mCameraUploadServiceLayout;
    private CardView mCameraUploadRepoCard;
    private TextView mCameraUploadRepoSummary;
    private CardView mCameraUploadAdvancedFeatureCard;
    private TextView mCameraUploadStateSummary;
    private TextView mAllowDataPlanSummary;
    private SwitchCompat mAllowDataPlanSwitch;
    private TextView mAllowVideosUploadSummary;
    private SwitchCompat mAllowVideosUploadSwitch;
    private TextView mCameraUploadBucketsSummary;
    private SwitchCompat mCameraUploadBucketsSwitch;
    private CardView mCameraUploadBucketsCard;
    private View mChangeAlbumsLayout;
    private CardView mChangeAlbumsCard;
    private TextView mChangeAlbumsSummary;
    private CardView mSyncFolderCard;
    private View mAppearanceLayout;
    private ImageView mAppearanceDirectionImage;
    private ViewGroup mAppearanceCollapseLayout;
    private SwitchCompat mForceDarkModeSwitch;
    private CardView mForceDarkModeCard;
    private SwitchCompat mForceLightModeSwitch;
    private CardView mForceLightModeCard;
    private TextView mBottomBarSummary;
    private List<String> bottomBarTexts;
    private CardView mBottomBarCard;
    private PopupWindow mBottomBarDropDown = null;
    private View mPopupBottomBarView;
    private View mCacheLayout;
    private ImageView mCacheDirectionImage;
    private ViewGroup mCacheCollapseLayout;
    private SwitchCompat mThumbInEncRepoSwitch;
    private View mThumbInEncRepoDetailLayout;
    private SwitchCompat mSaveFilesOfThumbInCacheSwitch;
    private CardView mThumbNetModeCard;
    private View mPopupThumbNetModeView = null;
    private PopupWindow mThumbNetModeDropDown = null;
    private TextView mThumbNetModeText;
    private TextView mThumbCalcResultText;
    private TextView mThumbCalcStatusText;
    private CardView mThumbStatusRefreshCard;
    private CardView mSettingsDownloadDataLocationKeyCard;
    private TextView mSettingsDownloadDataLocationKeySummary;
    private TextView mSettingsCacheInfoSummary;
    private View mSettingsCacheLocationKeyLayout;
    private CardView mSettingsCacheLocationKeyCard;
    private TextView mSettingsCacheLocationKeySummary;
    private CardView mSettingsClearCacheKeyCard;
    private SwitchCompat mDeleteCacheAutomaticSwitch;
    private View mCacheMaximumSizeLayout;
    private CardView mCacheMaximumSizeCard;
    private TextView mCacheMaximumSizeText;
    private PopupWindow mCacheMaximumSizeDropDown = null;
    private View mPopupCacheMaximumSizeView;
    private View mAdvancedOptionsLayout;
    private ImageView mAdvancedOptionsDirectionImage;
    private ViewGroup mAdvancedOptionsCollapseLayout;
    private CardView mSettingsUploadLogKeyCard;
    private TextView mExecuteOcrSummary;
    private List<String> executeOcrTexts;
    private CardView mExecuteOcrCard;
    private PopupWindow mExecuteOcrDropDown = null;
    private View mPopupExecuteOcrView;
    private TextView mOpenOfficeSummary;
    private List<String> openOfficeTexts;
    private CardView mOpenOfficeCard;
    private PopupWindow mOpenOfficeDropDown = null;
    private View mPopupOpenOfficeView;
    private TextView mOpenPdfSummary;
    private List<String> openPdfTexts;
    private CardView mOpenPdfCard;
    private PopupWindow mOpenPdfDropDown = null;
    private View mPopupOpenPdfView;
    private View mAboutLayout;
    private ImageView mAboutDirectionImage;
    private ViewGroup mAboutCollapseLayout;
    private CardView mSettingsPrivacyPolicyKeyCard;
    private View mSettingsPrivacyPolicyKeyBorder;
    private CardView mSettingsAboutAuthorKeyCard;
    private CardView mLogoutCard;
    private TextView mInfoText;
    private View mPopupSelectExpirationTimeView;

    private Boolean accountInfoCollapse = true;
    private Boolean appSecurityCollapse = true;
    private Boolean backupFolderCollapse = true;
    private Boolean appearanceCollapse = true;
    private Boolean cacheCollapse = true;
    private Boolean advancedOptionsCollapse = true;
    private Boolean aboutCollapse = true;

    public static int ACCOUNT_INFO_COLLAPSE = 0;
    public static int APP_SECURITY_COLLAPSE = 1;
    public static int BACKUP_FOLDER_COLLAPSE = 2;
    public static int SYNC_FOLDER_COLLAPSE = 3;
    public static int APPEARANCE_COLLAPSE = 4;
    public static int CACHE_COLLAPSE = 5;
    public static int ADVANCED_OPTIONS_COLLAPSE = 6;
    public static int ABOUT_COLLAPSE = 7;

    private String logFileName;

    public int allEncImagesCount = 0;
    public int allEncThumbsCount = 0;

    @Override
    public void onAttach(Activity activity) {
        Log.d(DEBUG_TAG, "onAttach");
        super.onAttach(activity);

        // global variables
        mActivity = (BrowserActivity) getActivity();
        settingsMgr = SettingsManager.instance();
        accountMgr = new AccountManager(mActivity);
        cameraManager = new CameraUploadManager(mActivity.getApplicationContext());
//        contactsManager = new ContactsUploadManager(mActivity.getApplicationContext());
        act = accountMgr.getCurrentAccount();
        dataMgr = new DataManager(act);
        databaseHelper = FolderBackupDBHelper.getDatabaseHelper();
    }

    public void onCreate(Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        settingsMgr.registerSharedPreferencesListener(settingsListener);
        Account account = accountMgr.getCurrentAccount();
        if (!Utils.isNetworkOn()) {
            mActivity.showShortToast(mActivity, R.string.network_down);
            return;
        }
        String backupPaths = settingsMgr.getBackupPaths();
        if (!TextUtils.isEmpty(backupPaths)) {
            backupSelectPaths = StringTools.getJsonToList(backupPaths);
        }
        ConcurrentAsyncTask.execute(new RequestAccountInfoTask(), account);

    }

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup,
                             Bundle paramBundle) {
        View root = paramLayoutInflater.inflate(R.layout.settings_fragment, paramViewGroup, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initViewAction();
        init();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible)
            calculateCacheSize();
    }

    private void initView(View view) {
        mAccountInfoLayout = view.findViewById(R.id.account_info_layout);
        mAccountInfoCollapseLayout = view.findViewById(R.id.account_info_collapse_layout);
        mAccountInfoDirectionImage = view.findViewById(R.id.account_info_direction_image);
        mAccountInfoUserSummary = view.findViewById(R.id.account_info_user_summary);
        mSwitchAccountCard = view.findViewById(R.id.switch_account_card);
        mAccountInfoSpaceSummary = view.findViewById(R.id.account_info_space_summary);

        mAppSecurityLayout = view.findViewById(R.id.app_security_layout);
        mAppSecurityCollapseLayout = view.findViewById(R.id.app_security_collapse_layout);
        mAppSecurityDirectionImage = view.findViewById(R.id.app_security_direction_image);
        mGestureLockLayout = view.findViewById(R.id.gesture_lock_layout);
        mGestureLockSwitch = view.findViewById(R.id.gesture_lock_switch);
        mGestureLockCard = view.findViewById(R.id.gesture_lock_card);
        mBiometricAuthLayout = view.findViewById(R.id.biometric_auth_layout);
        mBiometricAuthSwitch = view.findViewById(R.id.biometric_auth_switch);
        mBiometricAuthCard = view.findViewById(R.id.biometric_auth_card);
        mGestureBiometricServiceLayout = view.findViewById(R.id.gesture_biometric_service_layout);
        mWhenBackSwitch = view.findViewById(R.id.when_back_switch);
        mWhenHomeSwitch = view.findViewById(R.id.when_home_switch);
        mWhenDeviceLockSwitch = view.findViewById(R.id.when_device_lock_switch);
        mExpirationTimeCard = view.findViewById(R.id.expiration_time_card);
        mExpirationTimeText = view.findViewById(R.id.expiration_time_text);
        mClearLibraryPasswordCard = view.findViewById(R.id.clear_library_password_card);
        mAutoClearPasswordSwitch = view.findViewById(R.id.auto_clear_password_switch);
        mExceptionEncryptedRepoLayout = view.findViewById(R.id.exception_encrypted_repo_layout);
        mExceptionEncryptedRepoSwitch = view.findViewById(R.id.exception_encrypted_repo_switch);
        mClientEncryptLayout = view.findViewById(R.id.client_encrypt_layout);
        mClientEncryptSummary = view.findViewById(R.id.client_encrypt_summary);
        mClientEncryptSwitch = view.findViewById(R.id.client_encrypt_switch);

        mBackupFolderLayout = view.findViewById(R.id.backup_folder_layout);
        mBackupFolderDirectionImage = view.findViewById(R.id.backup_folder_layout_direction_image);
        mBackupFolderCollapseLayout = view.findViewById(R.id.backup_folder_collapse_layout);
        mBackupFolderSummary = view.findViewById(R.id.backup_folder_summary);
        mBackupFolderSwitch = view.findViewById(R.id.backup_folder_switch);
        mBackupFolderCard = view.findViewById(R.id.backup_folder_card);
        mBackupFolderServiceLayout = view.findViewById(R.id.backup_folder_service_layout);
        mBackupFolderModeCard = view.findViewById(R.id.folder_backup_mode_card);
        mBackupFolderModeSummary = view.findViewById(R.id.folder_backup_mode_summary);
        mBackupFolderRepoCard = view.findViewById(R.id.folder_backup_library_key_card);
        mBackupFolderRepoSummary = view.findViewById(R.id.folder_backup_library_key_summary);
        mBackupFolderPrefCard = view.findViewById(R.id.selected_backup_folders_key_card);
        mBackupFolderPrefSummary = view.findViewById(R.id.selected_backup_folders_key_summary);
        mBackupFolderStateCard = view.findViewById(R.id.folder_backup_state_card);
        mBackupFolderStateSummary = view.findViewById(R.id.folder_backup_state_summary);
        mBackupFolderSaveToCacheSwitch = view.findViewById(R.id.save_to_cache_switch);
        mBackupFolderResetCard = view.findViewById(R.id.folder_backup_reset_card);
        mCameraUploadSummary = view.findViewById(R.id.camera_upload_summary);
        mCameraUploadSwitch = view.findViewById(R.id.camera_upload_switch);
        mCameraUploadCard = view.findViewById(R.id.camera_upload_card);
        mCameraUploadServiceLayout = view.findViewById(R.id.camera_upload_service_layout);
        mCameraUploadRepoCard = view.findViewById(R.id.camera_upload_repo_card);
        mCameraUploadRepoSummary = view.findViewById(R.id.camera_upload_repo_summary);
        mCameraUploadAdvancedFeatureCard = view.findViewById(R.id.camera_upload_advanced_feature_card);
        mCameraUploadStateSummary = view.findViewById(R.id.camera_upload_state_summary);
        mAllowDataPlanSummary = view.findViewById(R.id.allow_data_plan_summary);
        mAllowDataPlanSwitch = view.findViewById(R.id.allow_data_plan_switch);
        mAllowVideosUploadSummary = view.findViewById(R.id.allow_videos_upload_summary);
        mAllowVideosUploadSwitch = view.findViewById(R.id.allow_videos_upload_switch);
        mCameraUploadBucketsSummary = view.findViewById(R.id.camera_upload_buckets_summary);
        mCameraUploadBucketsSwitch = view.findViewById(R.id.camera_upload_buckets_switch);
        mCameraUploadBucketsCard = view.findViewById(R.id.camera_upload_buckets_card);
        mChangeAlbumsLayout = view.findViewById(R.id.change_albums_layout);
        mChangeAlbumsCard = view.findViewById(R.id.change_albums_card);
        mChangeAlbumsSummary = view.findViewById(R.id.change_albums_summary);
        mSyncFolderCard = view.findViewById(R.id.sync_folder_card);

        mAppearanceLayout = view.findViewById(R.id.appearance_layout);
        mAppearanceDirectionImage = view.findViewById(R.id.appearance_direction_image);
        mAppearanceCollapseLayout = view.findViewById(R.id.appearance_collapse_layout);
        mForceDarkModeSwitch = view.findViewById(R.id.force_dark_mode_switch);
        mForceDarkModeCard = view.findViewById(R.id.force_dark_mode_card);
        mForceLightModeSwitch = view.findViewById(R.id.force_light_mode_switch);
        mForceLightModeCard = view.findViewById(R.id.force_light_mode_card);
        mBottomBarSummary = view.findViewById(R.id.bottom_bar_summary);
        mBottomBarCard = view.findViewById(R.id.bottom_bar_card);
        mPopupBottomBarView = view.findViewById(R.id.popup_select_bottom_bar_layout);

        mCacheLayout = view.findViewById(R.id.cache_layout);
        mCacheDirectionImage = view.findViewById(R.id.cache_direction_image);
        mCacheCollapseLayout = view.findViewById(R.id.cache_collapse_layout);
        mThumbInEncRepoSwitch = view.findViewById(R.id.thumb_in_enc_repo_switch);
        mThumbInEncRepoDetailLayout = view.findViewById(R.id.thumbnail_in_enc_repo_detail_layout);
        mSaveFilesOfThumbInCacheSwitch = view.findViewById(R.id.save_files_of_thumb_in_cache_switch);
        mThumbNetModeCard = view.findViewById(R.id.thumb_net_mode_card);
        mPopupThumbNetModeView = view.findViewById(R.id.popup_thumb_net_mode_layout);
        mThumbNetModeText = view.findViewById(R.id.thumb_net_mode_text);
        mThumbCalcResultText = view.findViewById(R.id.thumb_calc_result_text);
        mThumbCalcStatusText = view.findViewById(R.id.thumb_calc_status_text);
        mThumbStatusRefreshCard = view.findViewById(R.id.thumb_status_refresh_card);
        mSettingsDownloadDataLocationKeyCard = view.findViewById(R.id.settings_download_data_location_key_card);
        mSettingsDownloadDataLocationKeySummary = view.findViewById(R.id.settings_download_data_location_key_summary);
        mSettingsCacheInfoSummary = view.findViewById(R.id.settings_cache_info_summary);
        mSettingsCacheLocationKeyLayout = view.findViewById(R.id.settings_cache_location_key_layout);
        mSettingsCacheLocationKeyCard = view.findViewById(R.id.settings_cache_location_key_card);
        mSettingsCacheLocationKeySummary = view.findViewById(R.id.settings_cache_location_key_summary);
        mSettingsClearCacheKeyCard = view.findViewById(R.id.settings_clear_cache_key_card);
        mDeleteCacheAutomaticSwitch = view.findViewById(R.id.delete_cache_automatic_switch);
        mCacheMaximumSizeLayout = view.findViewById(R.id.cache_maximum_size_layout);
        mCacheMaximumSizeCard = view.findViewById(R.id.cache_maximum_size_card);
        mCacheMaximumSizeText = view.findViewById(R.id.cache_maximum_size_text);
        mPopupCacheMaximumSizeView = view.findViewById(R.id.popup_cache_maximum_size_layout);

        mAdvancedOptionsLayout = view.findViewById(R.id.advanced_options_layout);
        mAdvancedOptionsDirectionImage = view.findViewById(R.id.advanced_options_direction_image);
        mAdvancedOptionsCollapseLayout = view.findViewById(R.id.advanced_options_collapse_layout);
        mSettingsUploadLogKeyCard = view.findViewById(R.id.settings_upload_log_key_card);
        mExecuteOcrSummary = view.findViewById(R.id.execute_ocr_summary);
        mExecuteOcrCard = view.findViewById(R.id.execute_ocr_card);
        mPopupExecuteOcrView = view.findViewById(R.id.popup_select_execute_ocr_layout);
        mOpenOfficeSummary = view.findViewById(R.id.open_office_summary);
        mOpenOfficeCard = view.findViewById(R.id.open_office_card);
        mPopupOpenOfficeView = view.findViewById(R.id.popup_open_office_layout);
        mOpenPdfSummary = view.findViewById(R.id.open_pdf_summary);
        mOpenPdfCard = view.findViewById(R.id.open_pdf_card);
        mPopupOpenPdfView = view.findViewById(R.id.popup_open_pdf_layout);

        mAboutLayout = view.findViewById(R.id.about_layout);
        mAboutDirectionImage = view.findViewById(R.id.about_direction_image);
        mAboutCollapseLayout = view.findViewById(R.id.about_collapse_layout);
        mSettingsPrivacyPolicyKeyCard = view.findViewById(R.id.settings_privacy_policy_key_card);
        mSettingsPrivacyPolicyKeyBorder = view.findViewById(R.id.settings_privacy_policy_key_border);
        mSettingsAboutAuthorKeyCard = view.findViewById(R.id.settings_about_author_key_card);
        mLogoutCard = view.findViewById(R.id.logout_card);
        mInfoText = view.findViewById(R.id.info_text);
        mPopupSelectExpirationTimeView = view.findViewById(R.id.popup_select_expiration_time_layout);
    }

    private void initViewAction() {
        mAccountInfoLayout.setOnClickListener(v -> {
            upgradeCollapse(ACCOUNT_INFO_COLLAPSE, !accountInfoCollapse);
        });
        mSwitchAccountCard.setOnClickListener(v -> {
            mActivity.accountsCard.callOnClick();
        });
        mAppSecurityLayout.setOnClickListener(v -> {
            upgradeCollapse(APP_SECURITY_COLLAPSE, !appSecurityCollapse);
        });
        mGestureLockCard.setOnClickListener(v -> {
            if (mGestureLockSwitch.isChecked()){
                Intent newIntent = new Intent(getActivity(), UnlockGesturePasswordActivity.class);
                startActivityForResult(newIntent, SettingsManager.GESTURE_UNLOCK_REQUEST);
            } else {
                Intent newIntent = new Intent(getActivity(), CreateGesturePasswordActivity.class);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(newIntent, SettingsManager.GESTURE_LOCK_REQUEST);
                settingsMgr.setupGestureLock();
                refreshGestureBiometricService(true, settingsMgr.isBiometricAuthEnabled());
            }
        });
        mBiometricAuthCard.setOnClickListener(v -> {
            biometricPromptDialog();
        });
        mWhenBackSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsMgr.setupAutoLockWhenBack(isChecked);
        });
        mWhenHomeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsMgr.setupAutoLockWhenHome(isChecked);
        });
        mWhenDeviceLockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsMgr.setupAutoLockWhenDeviceLock(isChecked);
        });
        mExpirationTimeCard.setOnClickListener(v -> {
            showSelectExpirationTimePopup();
        });

        mClearLibraryPasswordCard.setOnClickListener(v -> {
            clearPassword();
        });
        mAutoClearPasswordSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == settingsMgr.isPasswordAutoClearEnabled()) {
                return;
            }
            settingsMgr.setupPasswordAutoClear(isChecked);

            if (isChecked) {
                boolean canLocalDecrypt = hasEncryptedRepoForCameraUploadAndBackup();
                showExceptionEncRepoDialog(canLocalDecrypt);
            } else {
                updateExceptionEncryptedRepoLayout();
            }
        });
        mExceptionEncryptedRepoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == settingsMgr.isExceptionEncryptedRepo()) {
                return;
            }
            if (isChecked) {
                settingsMgr.setupExceptionEncryptedRepo(true);
            } else {
                if (settingsMgr.isExceptionEncryptedRepo()) {
                    boolean canLocalDecrypt = hasEncryptedRepoForCameraUploadAndBackup();
                    showExceptionEncRepoDialog(canLocalDecrypt);
                }
            }
        });
        mThumbInEncRepoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsMgr.setupThumbInEncRepo(isChecked);
            mThumbInEncRepoDetailLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        mSaveFilesOfThumbInCacheSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsMgr.setupSaveFilesOfThumbInCache(isChecked);
        });
        mThumbNetModeCard.setOnClickListener(v -> {
            showThumbNetModePopup();
        });
        mThumbStatusRefreshCard.setOnClickListener(view -> {
            mActivity.getReposFragment().getThumbImagesCountInEncryptedRepos();
        });

        mBackupFolderLayout.setOnClickListener(v -> {
            upgradeCollapse(BACKUP_FOLDER_COLLAPSE, !backupFolderCollapse);
        });
        mBackupFolderCard.setOnClickListener(v -> {
            callOnBackupFolderCard(false);
        });
        mBackupFolderModeCard.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, FolderBackupConfigActivity.class);
            intent.putExtra(FOLDER_BACKUP_REMOTE_MODE, true);
            startActivityForResult(intent, CHOOSE_BACKUP_UPLOAD_REQUEST);
            refreshCameraUploadView();
//            showWifiDialog();
        });
        mBackupFolderRepoCard.setOnClickListener(v -> {
            // choose remote library
            Intent intent = new Intent(mActivity, FolderBackupConfigActivity.class);
            intent.putExtra(FOLDER_BACKUP_REMOTE_LIBRARY, true);
            startActivityForResult(intent, CHOOSE_BACKUP_UPLOAD_REQUEST);
        });
        mBackupFolderPrefCard.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, FolderBackupConfigActivity.class);
            intent.putExtra(FOLDER_BACKUP_REMOTE_PATH, true);
            startActivityForResult(intent, CHOOSE_BACKUP_UPLOAD_REQUEST);
        });
        mBackupFolderStateCard.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, FolderBackupResultActivity.class);
            intent.putExtra("account", mActivity.getAccount());
            startActivity(intent);
        });
        setBackupFolderStateSummary();
        mBackupFolderSaveToCacheSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsMgr.saveFolderBackupSaveToCache(isChecked);
        });
        mBackupFolderResetCard.setOnClickListener(v -> {
            databaseHelper.clearBackupFileInfo();

            String backupEmail = SettingsManager.instance().getBackupEmail();
            if (mActivity.mFolderBackupService != null && !TextUtils.isEmpty(backupEmail)) {
                mActivity.mFolderBackupService.backupFolder(backupEmail);
            }
        });

        mCameraUploadCard.setOnClickListener(v -> {
            if (mCameraUploadSwitch.isChecked()) {
                mCameraUploadServiceLayout.setVisibility(View.GONE);
                mCameraUploadSummary.setText(mActivity.getString(R.string.settings_camera_upload_service_stopped));
                cameraManager.disableCameraUpload();
            } else {
                XXPermissions.with(getActivity()).permission("android.permission.MANAGE_EXTERNAL_STORAGE").request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            mCameraUploadSummary.setText(mActivity.getString(R.string.settings_camera_upload_service_started));
                            Intent intent = new Intent(mActivity, CameraUploadConfigActivity.class);
                            intent.putExtra(CAMERA_UPLOAD_BOTH_PAGES, true);
                            startActivityForResult(intent, CHOOSE_CAMERA_UPLOAD_REQUEST);
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            Toast.makeText(getActivity(), mActivity.getString(R.string.authorization_storage_permission), Toast.LENGTH_LONG).show();
                            XXPermissions.startPermissionActivity(getActivity(), permissions);
                        } else {
                            Toast.makeText(getActivity(), mActivity.getString(R.string.get_storage_permission_failed), Toast.LENGTH_LONG).show();
                            mCameraUploadSummary.setText(mActivity.getString(R.string.settings_camera_upload_service_stopped));
                            mCameraUploadSwitch.setChecked(false);
                        }
                    }
                });
            }
            mCameraUploadSummary.setText(mActivity.getString(mCameraUploadSwitch.isChecked() ? R.string.settings_camera_upload_service_stopped : R.string.settings_camera_upload_service_started));
            mCameraUploadSwitch.setChecked(!mCameraUploadSwitch.isChecked());
        });
        mCameraUploadRepoCard.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, CameraUploadConfigActivity.class);
            intent.putExtra(CAMERA_UPLOAD_REMOTE_LIBRARY, true);
            startActivityForResult(intent, CHOOSE_CAMERA_UPLOAD_REQUEST);
        });
        mAllowDataPlanSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsMgr.saveDataPlanAllowed(isChecked);
            mAllowDataPlanSummary.setText(mActivity.getString(isChecked ? R.string.settings_camera_upload_data_plan_allowed : R.string.settings_camera_upload_default_wifi));
        });
        mAllowVideosUploadSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsMgr.saveVideosAllowed(isChecked);
            mAllowVideosUploadSummary.setText(mActivity.getString(isChecked ? R.string.settings_camera_upload_videos_allowed : R.string.settings_camera_upload_default_photos));
        });
        mCameraUploadBucketsCard.setOnClickListener(v -> {
            if (mCameraUploadBucketsSwitch.isChecked()) {
                mChangeAlbumsLayout.setVisibility(View.GONE);
                mCameraUploadBucketsSwitch.setChecked(false);
                scanCustomDirs(false);
            } else {
                mChangeAlbumsLayout.setVisibility(View.VISIBLE);
                mCameraUploadBucketsSwitch.setChecked(true);
                scanCustomDirs(true);
            }
        });
        mChangeAlbumsCard.setOnClickListener(v -> {
            scanCustomDirs(true);
        });
        mSyncFolderCard.setOnClickListener(v -> {
            mActivity.showShortToast(mActivity, R.string.sync_folder_development);
        });

        mAppearanceLayout.setOnClickListener(v -> {
            upgradeCollapse(APPEARANCE_COLLAPSE, !appearanceCollapse);
        });
        mForceDarkModeCard.setOnClickListener(v -> {
            settingsMgr.setupForceDarkMode(!mForceDarkModeSwitch.isChecked());
            settingsMgr.setupForceLightMode(false);
            mForceDarkModeSwitch.setChecked(!mForceDarkModeSwitch.isChecked());
            mForceLightModeSwitch.setChecked(false);
            Utils.setAppearance();
            mActivity.recreate();
        });
        mForceLightModeCard.setOnClickListener(v -> {
            settingsMgr.setupForceDarkMode(false);
            settingsMgr.setupForceLightMode(!mForceLightModeSwitch.isChecked());
            mForceDarkModeSwitch.setChecked(false);
            mForceLightModeSwitch.setChecked(!mForceLightModeSwitch.isChecked());
            Utils.setAppearance();
            mActivity.recreate();
        });
        mBottomBarCard.setOnClickListener(v -> {
            showBottomBarSizePopup();
        });
        mCacheLayout.setOnClickListener(v -> {
            upgradeCollapse(CACHE_COLLAPSE, !cacheCollapse);
        });
        mSettingsDownloadDataLocationKeyCard.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, DownloadLocationDialog.class);
            startActivityForResult(intent, CHOOSE_DOWNLOAD_LOCATION_REQUEST);
        });
        mDeleteCacheAutomaticSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsMgr.setupDeleteCacheAutomatic(isChecked);
            mCacheMaximumSizeLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        mCacheMaximumSizeCard.setOnClickListener(v -> {
            showCacheMaximumSizePopup();
        });
        mSettingsClearCacheKeyCard.setOnClickListener(v -> {
            clearCache();
        });
        mAdvancedOptionsLayout.setOnClickListener(v -> {
            upgradeCollapse(ADVANCED_OPTIONS_COLLAPSE, !advancedOptionsCollapse);
        });
        mSettingsUploadLogKeyCard.setOnClickListener(v -> {
            mActivity.exportLogFile();
        });
        mExecuteOcrCard.setOnClickListener(v -> {
            showExecuteOcrSizePopup();
        });
        mOpenOfficeCard.setOnClickListener(v -> {
            showOpenOfficePopup();
        });
        mOpenPdfCard.setOnClickListener(v -> {
            showOpenPdfPopup();
        });
        mAboutLayout.setOnClickListener(v -> {
            upgradeCollapse(ABOUT_COLLAPSE, !aboutCollapse);
        });
        mSettingsPrivacyPolicyKeyCard.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, PrivacyPolicyActivity.class);
            mActivity.startActivity(intent);
        });
        mSettingsAboutAuthorKeyCard.setOnClickListener(v -> {
            Dialog dialog = Utils.CustomDialog(mActivity);
            dialog.setContentView(R.layout.dialog_author);
            TextView versionText = dialog.findViewById(R.id.version_text);
            versionText.setText(appVersion);
            dialog.show();
        });

        mLogoutCard.setOnClickListener(v -> logout());
    }

    public void callOnBackupFolderCard(boolean forceUnchecked) {
        if (mBackupFolderSwitch.isChecked() || forceUnchecked) {
            mBackupFolderServiceLayout.setVisibility(View.GONE);
            mBackupFolderSummary.setText(mActivity.getString(R.string.settings_folder_backup_service_stopped));
            mBackupFolderSwitch.setChecked(false);
            settingsMgr.saveFolderAutomaticBackup(false);

            if (mActivity.mFolderBackupService.fileMonitorRunning)
                mActivity.mFolderBackupService.stopFolderMonitor();
            databaseHelper.removeRepoConfig(act.getEmail());
            settingsMgr.saveBackupPaths("");
            if (backupSelectPaths != null)
                backupSelectPaths.clear();
        } else {
            mBackupFolderSummary.setText(mActivity.getString(R.string.settings_folder_backup_service_started));
            XXPermissions.with(getActivity()).permission("android.permission.MANAGE_EXTERNAL_STORAGE").request(new OnPermissionCallback() {

                @Override
                public void onGranted(List<String> permissions, boolean all) {
                    if (all) {
                        Intent intent = new Intent(mActivity, FolderBackupConfigActivity.class);
                        intent.putExtra(FOLDER_BACKUP_REMOTE_PAGES, true);
                        startActivityForResult(intent, CHOOSE_BACKUP_UPLOAD_REQUEST);
                        refreshCameraUploadView();
                    }
                }

                @Override
                public void onDenied(List<String> permissions, boolean never) {
                    if (never) {
                        Toast.makeText(getActivity(), mActivity.getString(R.string.authorization_storage_permission), Toast.LENGTH_LONG).show();
                        XXPermissions.startPermissionActivity(getActivity(), permissions);
                    } else {
                        Toast.makeText(getActivity(), mActivity.getString(R.string.get_storage_permission_failed), Toast.LENGTH_LONG).show();
                        mBackupFolderSwitch.setChecked(false);
                    }
                }
            });
        }
        mBackupFolderSummary.setText(mActivity.getString(mBackupFolderSwitch.isChecked() ? R.string.settings_folder_backup_service_started : R.string.settings_folder_backup_service_stopped));
//            mBackupFolderSwitch.setChecked(!mBackupFolderSwitch.isChecked());
    }

    private void setBackupFolderStateSummary() {
        if (settingsMgr.isFolderAutomaticBackup()) {
            //mBackupFolderStateSummary.setText(Utils.getUploadStateShow(getActivity()));
            int totalBackup = settingsMgr.getBackupCompletedTotal();
            int waitingBackup = SeadroidApplication.getInstance().getWaitingBackup();
            String summary = mActivity.getString(R.string.folder_backup_waiting_state);
            if (waitingBackup == 0) {
                if (totalBackup != 0) {
                    summary = String.format(mActivity.getString(R.string.last_backup_done), totalBackup, settingsMgr.getBackupCompletedTime());
                }
            } else {
                summary = mActivity.getString(R.string.uploaded) + " " + (totalBackup - waitingBackup) + " / " + totalBackup;
            }
            mBackupFolderStateSummary.setText(summary);
        }
    }

    private void init() {
        upgradeCollapse(ACCOUNT_INFO_COLLAPSE, true);
        upgradeCollapse(APP_SECURITY_COLLAPSE, true);
        upgradeCollapse(BACKUP_FOLDER_COLLAPSE, true);
        upgradeCollapse(APPEARANCE_COLLAPSE, true);
        upgradeCollapse(CACHE_COLLAPSE, true);
        upgradeCollapse(ADVANCED_OPTIONS_COLLAPSE, true);
        upgradeCollapse(ABOUT_COLLAPSE, true);

        mGestureLockSwitch.setChecked(settingsMgr.isGestureLockEnabled());
        mBiometricAuthSwitch.setChecked(settingsMgr.isBiometricAuthEnabled());
        mWhenBackSwitch.setChecked(settingsMgr.isAutoLockWhenBack());
        mWhenHomeSwitch.setChecked(settingsMgr.isAutoLockWhenHome());
        mWhenDeviceLockSwitch.setChecked(settingsMgr.isAutoLockWhenDeviceLock());
        setExpirationTimeText(settingsMgr.getAutoLockExpirationTime());
        mAutoClearPasswordSwitch.setChecked(settingsMgr.isPasswordAutoClearEnabled());
        mThumbInEncRepoSwitch.setChecked(settingsMgr.isThumbInEncRepo());
        mSaveFilesOfThumbInCacheSwitch.setChecked(settingsMgr.isSaveFilesOfThumbInCache());
        updateThumbStatus();
        mDeleteCacheAutomaticSwitch.setChecked(settingsMgr.isDeleteCacheAutomatic());
        mCacheMaximumSizeLayout.setVisibility(settingsMgr.isDeleteCacheAutomatic() ? View.VISIBLE : View.GONE);
        mCacheMaximumSizeText.setText(settingsMgr.getCacheMaximumSize() + "GB");;
        mAllowDataPlanSwitch.setChecked(settingsMgr.isDataPlanAllowed());
        mAllowDataPlanSummary.setText(mActivity.getString(settingsMgr.isDataPlanAllowed() ? R.string.settings_camera_upload_data_plan_allowed : R.string.settings_camera_upload_default_wifi));
        mAllowVideosUploadSwitch.setChecked(settingsMgr.isVideosUploadAllowed());
        mAllowVideosUploadSummary.setText(mActivity.getString(settingsMgr.isVideosUploadAllowed() ? R.string.settings_camera_upload_videos_allowed : R.string.settings_camera_upload_default_photos));
        mCameraUploadStateSummary.setText(Utils.getUploadStateShow(getActivity()));
        mForceDarkModeSwitch.setChecked(settingsMgr.isForceDarkMode());
        mForceLightModeSwitch.setChecked(settingsMgr.isForceLightMode());

        mThumbInEncRepoDetailLayout.setVisibility(settingsMgr.isThumbInEncRepo() ? View.VISIBLE : View.GONE);
        updateThumbNetModeText();

        bottomBarTexts = Arrays.asList(
                mActivity.getString(R.string.large),
                mActivity.getString(R.string.medium),
                mActivity.getString(R.string.small),
                mActivity.getString(R.string.hidden));
        mBottomBarSummary.setText(bottomBarTexts.get(settingsMgr.getBottomBarSize()));
        executeOcrTexts = Arrays.asList(
                mActivity.getString(R.string.ask_everytime),
                mActivity.getString(R.string.activated),
                mActivity.getString(R.string.deactivated));
        mExecuteOcrSummary.setText(executeOcrTexts.get(settingsMgr.getExecuteOcrValue()));
        openOfficeTexts = Arrays.asList(
                mActivity.getString(R.string.optional),
                mActivity.getString(R.string.onlyoffice_app),
                mActivity.getString(R.string.in_app_office),
                mActivity.getString(R.string.external_app));
        mOpenOfficeSummary.setText(openOfficeTexts.get(settingsMgr.getOpenOfficeValue()));
        openPdfTexts = Arrays.asList(
                mActivity.getString(R.string.optional),
                mActivity.getString(R.string.in_app_pdf),
                mActivity.getString(R.string.external_app));
        mOpenPdfSummary.setText(openPdfTexts.get(settingsMgr.getOpenPdfValue()));

        String country = Locale.getDefault().getCountry();
        String language = Locale.getDefault().getLanguage();
        if (!TextUtils.equals("CN", country) && !TextUtils.equals("zh", language)) {
            mSettingsPrivacyPolicyKeyCard.setVisibility(View.GONE);
            mSettingsPrivacyPolicyKeyBorder.setVisibility(View.GONE);
        }

        refreshGestureBiometricService(settingsMgr.isGestureLockEnabled(), settingsMgr.isBiometricAuthEnabled());

        String info = "";
        try {
            appVersion = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName;
            info = mActivity.getResources().getString(R.string.app_name) +
                    " " +
                    Calendar.getInstance().get(Calendar.YEAR) +
                    " - " +
                    mActivity.getResources().getString(R.string.settings_about_version_title) +
                    " " +
                    appVersion;
        } catch (NameNotFoundException e) {
            Log.e(DEBUG_TAG, "app version name not found exception");
            info = getString(R.string.not_available);
        }
        mInfoText.setText(info);

        // update Account info settings
        setAccountInfoSummary();

        updateExceptionEncryptedRepoLayout();

        Account currentAccount = accountMgr.getCurrentAccount();
        if (currentAccount != null) {
            final ServerInfo serverInfo = accountMgr.getServerInfo(currentAccount);

            mClientEncryptSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                settingsMgr.setupEncrypt(isChecked);
                mClientEncryptSummary.setText(mActivity.getString(isChecked ? R.string.enc_on : R.string.enc_off));
            });
            mClientEncryptSwitch.setChecked(settingsMgr.isEncryptEnabled());

            if (serverInfo != null && !serverInfo.canLocalDecrypt()) {
                mClientEncryptLayout.setVisibility(View.GONE);
            }
        }

        refreshCameraUploadView();

        mSettingsDownloadDataLocationKeySummary.setText(settingsMgr.getDownloadDataLocation());

        // App Version
        try {
            appVersion = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(DEBUG_TAG, "app version name not found exception");
            appVersion = getString(R.string.not_available);
        }

        // Cache size
        calculateCacheSize();

        // Storage selection only works on KitKat or later
        if (storageManager.supportsMultipleStorageLocations()) {
            updateStorageLocationSummary();
            mSettingsCacheLocationKeyCard.setOnClickListener(v -> {
                new SwitchStorageTaskDialog().show(getFragmentManager(), "Select cache location");
            });
        } else {
            mSettingsCacheLocationKeyLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(DEBUG_TAG, "onDestroy()");
        settingsMgr.unregisterSharedPreferencesListener(settingsListener);
    }

    private void upgradeCollapse(int index, boolean collapse) {
        accountInfoCollapse = true;
        appSecurityCollapse = true;
        backupFolderCollapse = true;
        appearanceCollapse = true;
        cacheCollapse = true;
        advancedOptionsCollapse = true;
        aboutCollapse = true;
        if (index == ACCOUNT_INFO_COLLAPSE) accountInfoCollapse = collapse;
        if (index == APP_SECURITY_COLLAPSE) appSecurityCollapse = collapse;
        if (index == BACKUP_FOLDER_COLLAPSE) backupFolderCollapse = collapse;
        if (index == APPEARANCE_COLLAPSE) appearanceCollapse = collapse;
        if (index == CACHE_COLLAPSE) cacheCollapse = collapse;
        if (index == ADVANCED_OPTIONS_COLLAPSE) advancedOptionsCollapse = collapse;
        if (index == ABOUT_COLLAPSE) aboutCollapse = collapse;

        TransitionManager.beginDelayedTransition(mAccountInfoCollapseLayout);
        ViewGroup.LayoutParams layoutParamsAccountInfo = mAccountInfoCollapseLayout.getLayoutParams();
        layoutParamsAccountInfo.height = accountInfoCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        mAccountInfoCollapseLayout.requestLayout();
        mAccountInfoDirectionImage.setImageDrawable(getResources().getDrawable((accountInfoCollapse) ? R.drawable.ic_left : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(mAppSecurityCollapseLayout);
        ViewGroup.LayoutParams layoutParamsAppSecurity = mAppSecurityCollapseLayout.getLayoutParams();
        layoutParamsAppSecurity.height = appSecurityCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        mAppSecurityCollapseLayout.requestLayout();
        mAppSecurityDirectionImage.setImageDrawable(getResources().getDrawable((appSecurityCollapse) ? R.drawable.ic_left : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(mBackupFolderCollapseLayout);
        ViewGroup.LayoutParams layoutParamsBackupFolder = mBackupFolderCollapseLayout.getLayoutParams();
        layoutParamsBackupFolder.height = backupFolderCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        mBackupFolderCollapseLayout.requestLayout();
        mBackupFolderDirectionImage.setImageDrawable(getResources().getDrawable((backupFolderCollapse) ? R.drawable.ic_left : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(mAppearanceCollapseLayout);
        ViewGroup.LayoutParams layoutParamsAppearance = mAppearanceCollapseLayout.getLayoutParams();
        layoutParamsAppearance.height = appearanceCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        mAppearanceCollapseLayout.requestLayout();
        mAppearanceDirectionImage.setImageDrawable(getResources().getDrawable((appearanceCollapse) ? R.drawable.ic_left : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(mCacheCollapseLayout);
        ViewGroup.LayoutParams layoutParamsCache = mCacheCollapseLayout.getLayoutParams();
        layoutParamsCache.height = cacheCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        mCacheCollapseLayout.requestLayout();
        mCacheDirectionImage.setImageDrawable(getResources().getDrawable((cacheCollapse) ? R.drawable.ic_left : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(mAdvancedOptionsCollapseLayout);
        ViewGroup.LayoutParams layoutParamsAdvancedOptions = mAdvancedOptionsCollapseLayout.getLayoutParams();
        layoutParamsAdvancedOptions.height = advancedOptionsCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        mAdvancedOptionsCollapseLayout.requestLayout();
        mAdvancedOptionsDirectionImage.setImageDrawable(getResources().getDrawable((advancedOptionsCollapse) ? R.drawable.ic_left : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(mAboutCollapseLayout);
        ViewGroup.LayoutParams layoutParamsAbout = mAboutCollapseLayout.getLayoutParams();
        layoutParamsAbout.height = aboutCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        mAboutCollapseLayout.requestLayout();
        mAboutDirectionImage.setImageDrawable(getResources().getDrawable((aboutCollapse) ? R.drawable.ic_left : R.drawable.ic_down));
    }

    private void logout() {
        final LogoutDialog dialog = new LogoutDialog();
        dialog.init();
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                Account account = accountMgr.getCurrentAccount();

                // sign out operations
                accountMgr.signOutAccount(account);

                // password auto clear
                if (settingsMgr.isPasswordAutoClearEnabled()) {
                    Utils.clearPasswordSilently(Lists.newArrayList());
                }

                // restart BrowserActivity (will go to AccountsActivity)
                Intent intent = new Intent(mActivity, BrowserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mActivity.startActivity(intent);
                mActivity.finish();
            }
        });
        dialog.show(mActivity.getSupportFragmentManager(), TAG_LOGOUT_DIALOG_FRAGMENT);
    }

    private void clearPassword() {
        ClearPasswordTaskDialog dialog = new ClearPasswordTaskDialog();
        dialog.setTaskDialogLisenter(new TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                mActivity.showShortToast(mActivity, R.string.clear_password_successful);
            }

            @Override
            public void onTaskFailed(SeafException e) {
                mActivity.showShortToast(mActivity, R.string.clear_password_failed);
            }
        });
        dialog.show(getFragmentManager(), "DialogFragment");
    }

    private void updateStorageLocationSummary() {
        String summary = storageManager.getStorageLocation().description;
        mSettingsCacheLocationKeySummary.setText(summary);
    }

    private void refreshGestureBiometricService(boolean isGestureLockEnabled, boolean isBiometricAuthEnabled) {
        mGestureBiometricServiceLayout.setVisibility(
                isGestureLockEnabled || isBiometricAuthEnabled
                        ? View.VISIBLE : View.GONE);
    }

    private void refreshCameraUploadView() {
        Account camAccount = cameraManager.getCameraAccount();
        String backupEmail = settingsMgr.getBackupEmail();
        if (camAccount != null && settingsMgr.getCameraUploadRepoName() != null) {
            mCameraUploadRepoSummary.setText(camAccount.getSignature() + "/" + settingsMgr.getCameraUploadRepoName());
        }

        mCameraUploadSwitch.setChecked(cameraManager.isCameraUploadEnabled());
        mCameraUploadSummary.setText(cameraManager.isCameraUploadEnabled() ? R.string.settings_camera_upload_service_started : R.string.settings_camera_upload_service_stopped);

        mBackupFolderSwitch.setChecked(settingsMgr.isFolderAutomaticBackup());
        mBackupFolderSummary.setText(settingsMgr.isFolderAutomaticBackup() ? R.string.settings_folder_backup_service_started : R.string.settings_folder_backup_service_stopped);
        mBackupFolderSaveToCacheSwitch.setChecked(settingsMgr.isFolderBackupSaveToCache());

        if (settingsMgr.isFolderAutomaticBackup()) {
            mBackupFolderServiceLayout.setVisibility(View.VISIBLE);

            mBackupFolderModeSummary.setText(settingsMgr.isFolderBackupDataPlanAllowed() ? getActivity().getString(R.string.folder_backup_mode) : "WIFI");

            if (backupSelectPaths == null || backupSelectPaths.size() == 0) {
                mBackupFolderPrefSummary.setText("0");
            } else {
                mBackupFolderPrefSummary.setText(backupSelectPaths.size() + "");
            }

            if (!TextUtils.isEmpty(backupEmail)) {
                try {
                    selectRepoConfig = databaseHelper.getRepoConfig(backupEmail);
                } catch (Exception e) {
                    Utils.utilsLogInfo(true, "refreshCameraUploadView\nError: " + e);
                }
            }

            if (selectRepoConfig != null && !TextUtils.isEmpty(selectRepoConfig.getRepoName())) {
                mBackupFolderRepoSummary.setText(backupEmail + "/" + selectRepoConfig.getRepoName());
            } else {
                mBackupFolderRepoSummary.setText(getActivity().getString(R.string.folder_backup_select_repo_hint));
            }

        } else {
            mBackupFolderServiceLayout.setVisibility(View.GONE);
        }

        if (cameraManager.isCameraUploadEnabled()) {
            mCameraUploadServiceLayout.setVisibility(View.VISIBLE);
        } else {
            mCameraUploadServiceLayout.setVisibility(View.GONE);
        }


        // videos

        List<String> bucketNames = new ArrayList<>();
        List<String> bucketIds = settingsMgr.getCameraUploadBucketList();
        List<GalleryBucketUtils.Bucket> tempBuckets = GalleryBucketUtils.getMediaBuckets(getActivity().getApplicationContext());
        LinkedHashSet<GalleryBucketUtils.Bucket> bucketsSet = new LinkedHashSet<>(tempBuckets.size());
        bucketsSet.addAll(tempBuckets);
        List<GalleryBucketUtils.Bucket> allBuckets = new ArrayList<>(bucketsSet.size());
        Iterator iterator = bucketsSet.iterator();
        while (iterator.hasNext()) {
            GalleryBucketUtils.Bucket bucket = (GalleryBucketUtils.Bucket) iterator.next();
            allBuckets.add(bucket);
        }

        for (GalleryBucketUtils.Bucket bucket : allBuckets) {
            if (bucketIds.contains(bucket.id)) {
                bucketNames.add(bucket.name);
            }
        }

        if (bucketNames.isEmpty()) {
            mChangeAlbumsLayout.setVisibility(View.GONE);
            mCameraUploadBucketsSwitch.setChecked(false);
        } else {
            mCameraUploadBucketsSwitch.setChecked(true);
            mChangeAlbumsSummary.setText(TextUtils.join(", ", bucketNames));
            mChangeAlbumsLayout.setVisibility(View.VISIBLE);
        }
    }

    private void clearCache() {
        ClearCacheTaskDialog dialog = new ClearCacheTaskDialog();
        dialog.setTaskDialogLisenter(new TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                mActivity.getTransferFragment().removeAllFinishedDownloadTasks();
                mActivity.getReposFragment().getAdapter().clearImagePathMap();
                // refresh cache size
                calculateCacheSize();
                //clear Glide cache
                Glide.get(SeadroidApplication.getAppContext()).clearMemory();
                Toast.makeText(mActivity, getString(R.string.settings_clear_cache_success), Toast.LENGTH_SHORT).show();

                mActivity.getReposFragment().refreshOnResume();
                mActivity.getStarredFragment().saveCachedStarredFiles();

                mActivity.getDataManager().clearDownloadAndUploadCache();
                mActivity.getTransferFragment().setUpTransferList();
            }

            @Override
            public void onTaskFailed(SeafException e) {
                Toast.makeText(mActivity, getString(R.string.settings_clear_cache_failed), Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getFragmentManager(), "DialogFragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SettingsManager.GESTURE_LOCK_REQUEST:
                mGestureLockSwitch.setChecked(resultCode == Activity.RESULT_OK);
                refreshGestureBiometricService(resultCode == Activity.RESULT_OK, settingsMgr.isBiometricAuthEnabled());
                break;
            case SettingsManager.GESTURE_UNLOCK_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    LockPatternUtils mLockPatternUtils = new LockPatternUtils(getActivity());
                    mLockPatternUtils.clearLock();
                    mGestureLockSwitch.setChecked(false);
                    settingsMgr.setupGestureUnlock();
                    refreshGestureBiometricService(false, settingsMgr.isBiometricAuthEnabled());
                }
                break;
            case CHOOSE_CAMERA_UPLOAD_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        return;
                    }
                    final String repoName = data.getStringExtra(SeafilePathChooserActivity.DATA_REPO_NAME);
                    final String repoId = data.getStringExtra(SeafilePathChooserActivity.DATA_REPO_ID);
                    final Account account = data.getParcelableExtra(SeafilePathChooserActivity.DATA_ACCOUNT);
                    if (repoName != null && repoId != null) {
                        // Log.d(DEBUG_TAG, "Activating camera upload to " + account + "; " + repoName);
                        cameraManager.setCameraAccount(account);
                        settingsMgr.saveCameraUploadRepoInfo(repoId, repoName);
                        if (settingsMgr.isPasswordAutoClearEnabled() && !settingsMgr.isExceptionEncryptedRepo())
                            showExceptionEncRepoDialog(true);
                    }

                } else if (resultCode == Activity.RESULT_CANCELED) {

                }
                refreshCameraUploadView();
                break;
            case CHOOSE_BACKUP_UPLOAD_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        return;
                    }
                    settingsMgr.saveFolderAutomaticBackup(true);
                    final boolean pathOn = data.getBooleanExtra(FolderBackupConfigActivity.BACKUP_SELECT_PATHS_SWITCH, false);
                    if (pathOn) {
                        final ArrayList<String> pathListExtra = data.getStringArrayListExtra(FolderBackupConfigActivity.BACKUP_SELECT_PATHS);
                        if (pathListExtra != null) {
                            if (backupSelectPaths == null)
                                backupSelectPaths = new ArrayList<>();
                            else
                                backupSelectPaths.clear();

                            backupSelectPaths.addAll(pathListExtra);
                            mBackupFolderPrefSummary.setText(pathListExtra.size() + "");
                        }
                    }
                    if (!TextUtils.isEmpty(settingsMgr.getBackupEmail())) {
                        try {
                            selectRepoConfig = databaseHelper.getRepoConfig(settingsMgr.getBackupEmail());
                            if (selectRepoConfig != null && !TextUtils.isEmpty(selectRepoConfig.getRepoID())) {
                                SeafRepo repo = dataMgr.getCachedRepoByID(selectRepoConfig.getRepoID());
                                if (repo != null && repo.canLocalDecrypt() && settingsMgr.isPasswordAutoClearEnabled() && !settingsMgr.isExceptionEncryptedRepo()) {
                                    showExceptionEncRepoDialog(true);
                                }
                            }
                        } catch (Exception e) {
                            Utils.utilsLogInfo(true, "refreshCameraUploadView\nError: " + e);
                        }
                    }
                    final boolean backupRestart = data.getBooleanExtra(FolderBackupConfigActivity.BACKUP_RESTART, false);
                    if (backupRestart) {
                        mActivity.backupFolder();
                    }
                } else {
                    if (data == null)
                        return;

                    if (!data.hasExtra(FOLDER_BACKUP_REMOTE_PAGES))
                        return;

                    final boolean isChooseAllPages = data.getBooleanExtra(FOLDER_BACKUP_REMOTE_PAGES, false);
                    if (isChooseAllPages) {
                        mBackupFolderSwitch.setChecked(false);

                        databaseHelper.removeRepoConfig(act.getEmail());
                        settingsMgr.saveBackupPaths("");
                        if (backupSelectPaths != null)
                            backupSelectPaths.clear();
                    }
                }
                refreshCameraUploadView();
                break;
            case CHOOSE_DOWNLOAD_LOCATION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        return;
                    }

                    String newPath = data.getStringExtra(DownloadLocationDialog.NEW_DOWNLOAD_DATA_LOCATION);
                    settingsMgr.saveDownloadDataLocation(newPath);
                    mSettingsDownloadDataLocationKeySummary.setText(newPath);
                }
                break;
            case CHOOSE_COPY_MOVE_DEST_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    String dstRepoId, dstRepoName, dstDir;
                    Account account;
                    dstRepoName = data.getStringExtra(SeafilePathChooserActivity.DATA_REPO_NAME);
                    dstRepoId = data.getStringExtra(SeafilePathChooserActivity.DATA_REPO_ID);
                    dstDir = data.getStringExtra(SeafilePathChooserActivity.DATA_DIR);
                    account = data.getParcelableExtra(SeafilePathChooserActivity.DATA_ACCOUNT);
                    notifyFileOverwriting(account, dstRepoName, dstRepoId, dstDir);
                }
                break;
//            case CHOOSE_CONTACTS_UPLOAD_REQUEST:
//                if (resultCode == Activity.RESULT_OK) {
//                    if (data == null) {
//                        return;
//                    }
//                    final String repoName = data.getStringExtra(SeafilePathChooserActivity.DATA_REPO_NAME);
//                    final String repoId = data.getStringExtra(SeafilePathChooserActivity.DATA_REPO_ID);
//                    final Account account = data.getParcelableExtra(SeafilePathChooserActivity.DATA_ACCOUNT);
//                    if (repoName != null && repoId != null) {
//                        //                        Log.d(DEBUG_TAG, "Activating contacts upload to " + account + "; " + repoName);
//                        contactsManager.setContactsAccount(account);
//                        settingsMgr.saveContactsUploadRepoInfo(repoId, repoName);
//                    }
//                } else if (resultCode == Activity.RESULT_CANCELED) {
//                }
////                refreshContactsView();
//                break;

            default:
                break;
        }

    }

    private void notifyFileOverwriting(final Account account,
                                       final String repoName,
                                       final String repoID,
                                       final String targetDir) {
        List<Uri> logPathList = Lists.newArrayList();
        String logDirPath = SeafileLog.getLogDirPath();
        File directory = new File(logDirPath);
        if (logFileName == null) {
            mActivity.showShortToast(mActivity, R.string.unknow_error);
            return;
        }
        File file = new File(directory.toString(), logFileName);
        logPathList.add(Uri.fromFile(file));
//        File[] files = directory.listFiles();
//        for (int i = 0; i < files.length; i++) {
//            File file = files[i];
//            if (file.isFile() && file.getName().contains(SeafileLog.MY_LOG_FILE_NAME))
//                logPathList.add(Uri.fromFile(file));
//        }

        mActivity.uploadFilesFromLocal(logPathList, repoID, repoName, targetDir);
    }

    private void scanCustomDirs(boolean isCustomScanOn) {
        if (isCustomScanOn) {
            Intent intent = new Intent(mActivity, CameraUploadConfigActivity.class);
            intent.putExtra(CAMERA_UPLOAD_LOCAL_DIRECTORIES, true);
            startActivityForResult(intent, CHOOSE_CAMERA_UPLOAD_REQUEST);
        } else {
            List<String> selectedBuckets = new ArrayList<>();
            settingsMgr.setCameraUploadBucketList(selectedBuckets);
            refreshCameraUploadView();
        }
    }

    /**
     * automatically update Account info, like space usage, total space size, from background.
     */
    private class RequestAccountInfoTask extends AsyncTask<Account, Void, AccountInfo> {

        @Override
        protected void onPreExecute() {
            mActivity.setSupportProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected AccountInfo doInBackground(Account... params) {
            AccountInfo accountInfo = null;

            if (params == null) return null;

            try {
                // get account info from server
                accountInfo = dataMgr.getAccountInfo();
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "could not get account info!", e);
            }

            return accountInfo;
        }

        @Override
        protected void onPostExecute(AccountInfo accountInfo) {
            mActivity.setSupportProgressBarIndeterminateVisibility(false);

            if (accountInfo == null) return;

            Account currentAccount = accountMgr.getCurrentAccount();
            if (currentAccount != null)
                saveAccountInfo(currentAccount.getSignature(), accountInfo);

            setAccountInfoSummary();
        }
    }

    private void setAccountInfoSummary() {
        String contactEmail = "";
        Account currentAccount = accountMgr.getCurrentAccount();
        if (currentAccount != null) {
            String signature = currentAccount.getSignature();
            AccountInfo accountInfo = getAccountInfoBySignature(signature);
            if (accountInfo != null) {
                String spaceUsage = accountInfo.getSpaceUsed(mActivity.getResources().getString(R.string.from));
                mAccountInfoSpaceSummary.setText(spaceUsage);

                contactEmail = accountInfo.getContactEmail();
            }
        }

        mAccountInfoUserSummary.setText(contactEmail.isEmpty() || contactEmail.equals("null") ? getCurrentUserIdentifier() : contactEmail);
    }

    public String getCurrentUserIdentifier() {
        Account account = accountMgr.getCurrentAccount();

        if (account == null)
            return "";

        return account.getEmail();
    }

    public void saveAccountInfo(String signature, AccountInfo accountInfo) {
        accountInfoMap.put(signature, accountInfo);
    }

    public AccountInfo getAccountInfoBySignature(String signature) {
        if (accountInfoMap.containsKey(signature))
            return accountInfoMap.get(signature);
        else
            return null;
    }

    private void calculateCacheSize() {
        ConcurrentAsyncTask.execute(new CalculateCacheTask());
    }

    private class CalculateCacheTask extends AsyncTask<String, Void, Long> {

        @Override
        protected Long doInBackground(String... params) {
            return storageManager.getUsedSpace();
        }

        @Override
        protected void onPostExecute(Long aLong) {
            String total = FileUtils.byteCountToDisplaySize(aLong);
            mSettingsCacheInfoSummary.setText(total);
        }

    }

    private class UpdateStorageSLocationSummaryTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void ret) {
            updateStorageLocationSummary();
        }

    }

    private SharedPreferences.OnSharedPreferenceChangeListener settingsListener = (sharedPreferences, key) -> {
        switch (key) {
            case SettingsManager.SHARED_PREF_STORAGE_DIR:
                ConcurrentAsyncTask.execute(new UpdateStorageSLocationSummaryTask());
                break;
        }
    };

    private void showWifiDialog() {
//        String[] buckModes = {"WIFI", getActivity().getString(R.string.folder_backup_mode)};
        boolean selectState = settingsMgr.isFolderBackupDataPlanAllowed();
        Dialog dialog = Utils.CustomDialog(mActivity);
        dialog.setContentView(R.layout.dialog_backup_wifi);

        RadioButton wifiRB = dialog.findViewById(R.id.wifi_rb);
        RadioButton trafficRB = dialog.findViewById(R.id.traffic_rb);
        CardView cancelCard = dialog.findViewById(R.id.cancel_card);
        CardView okCard = dialog.findViewById(R.id.ok_card);

        if (selectState)
            trafficRB.setChecked(true);
        else
            wifiRB.setChecked(true);

        cancelCard.setOnClickListener(v -> {
            dialog.dismiss();
        });
        okCard.setOnClickListener(v -> {
            dialog.dismiss();
            settingsMgr.saveFolderBackupDataPlanAllowed(trafficRB.isChecked());
            mBackupFolderModeSummary.setText(mActivity.getString(trafficRB.isChecked()? R.string.folder_backup_mode : R.string.settings_folder_backup_traffic_hint));
        });
        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        setBackupFolderStateSummary();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CameraSyncEvent result) {
        int scanUploadStatus = SeadroidApplication.getInstance().getScanUploadStatus();
        if (cameraManager.isCameraUploadEnabled() && scanUploadStatus > 0) {
            if (scanUploadStatus == CameraSyncStatus.SCAN_END) {
                SeadroidApplication.getInstance().setScanUploadStatus(CameraSyncStatus.NORMAL);
            }
            mCameraUploadStateSummary.setText(Utils.getUploadStateShow(getActivity()));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FolderBackupEvent result) {
        setBackupFolderStateSummary();
    }

    /*
    check if Biometric authentication is possible on this device.
    return  -1  :   un-supported
            0   :   supported both BIOMETRIC_STRONG and DEVICE_CREDENTIAL
            1   :   Supported only BIOMETRIC_STRONG
     */
    private int checkBiometricAuthPossibility(){
        BiometricManager biometricManager = BiometricManager.from(getActivity());
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return BIOMETRIC_AUTH_STRONG;
        }

        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return BIOMETRIC_AUTH_DEVICE_CREDENTIAL;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return BIOMETRIC_AUTH_NO_SUPPORTED;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return BIOMETRIC_AUTH_NO_SUPPORTED;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
                startActivityForResult(enrollIntent, AccountDetailActivity.REQ_BIOMETRIC);
                return BIOMETRIC_AUTH_NO_SET;
        }
        return BIOMETRIC_AUTH_NO_SET;
    }

    private void switchBiometricAuthSetting(){
        Boolean bUndoValue = !mBiometricAuthSwitch.isChecked();

        settingsMgr.setupBiometricAuth(bUndoValue);
        new Thread(() -> {
            getActivity().runOnUiThread(() -> {
                mBiometricAuthSwitch.setChecked(bUndoValue);
                refreshGestureBiometricService(settingsMgr.isGestureLockEnabled(), bUndoValue);
            });
        }).start();
    }
    private void biometricPromptDialog() {
        int nBiometricAuthPossibility = checkBiometricAuthPossibility();
        if (nBiometricAuthPossibility != BIOMETRIC_AUTH_STRONG && nBiometricAuthPossibility != BIOMETRIC_AUTH_DEVICE_CREDENTIAL){
            Toast.makeText(getActivity(),R.string.biometric_auth_no_support, Toast.LENGTH_SHORT).show();
            return;
        }

        BiometricPrompt.PromptInfo m_promptInfo;
        Executor m_executor = ContextCompat.getMainExecutor(getActivity());
        BiometricPrompt  m_biometricPrompt = new BiometricPrompt(this, m_executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED
                        || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {

                } else {
                    Toast.makeText(getActivity(), errString, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                switchBiometricAuthSetting();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

            }


        });

        if (nBiometricAuthPossibility == BIOMETRIC_AUTH_STRONG) {
            m_promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(mActivity.getResources().getString(R.string.biometry_login_title))
                    //.setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText(mActivity.getResources().getString(R.string.cancel))
                    //.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build();
        } else {
            m_promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(mActivity.getResources().getString(R.string.biometry_login_title))
                    //.setSubtitle("Log in using your biometric credential")
                    //.setNegativeButtonText(mActivity.getResources().getString(R.string.cancel))

                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG|BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build();
        }
        m_biometricPrompt.authenticate(m_promptInfo);
    }

    private void showSelectExpirationTimePopup() {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_expiration_time, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mExpirationDropDown = new PopupWindow(layout, mExpirationTimeCard.getWidth(),
                    mPopupSelectExpirationTimeView.getHeight(),true);//FrameLayout.LayoutParams.WRAP_CONTENT

            final CardView unit0SCard = layout.findViewById(R.id.unit_0s_card);
            final CardView unit10SCard = layout.findViewById(R.id.unit_10s_card);
            final CardView unit60SCard = layout.findViewById(R.id.unit_60s_card);
            final CardView unit15MinCard = layout.findViewById(R.id.unit_15min_card);
            final CardView unit60MinCard = layout.findViewById(R.id.unit_60min_card);

            unit0SCard.setOnClickListener(view -> updateExpirationTime(0));
            unit10SCard.setOnClickListener(view -> updateExpirationTime(10 * 1000));
            unit60SCard.setOnClickListener(view -> updateExpirationTime(60 * 1000));
            unit15MinCard.setOnClickListener(view -> updateExpirationTime(15 * 60 * 1000));
            unit60MinCard.setOnClickListener(view -> updateExpirationTime(60 * 60 * 1000));

            mExpirationDropDown.showAsDropDown(mExpirationTimeCard, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateExpirationTime(long expirationTime) {
        mExpirationDropDown.dismiss();
        settingsMgr.setupAutoLockExpirationTime(expirationTime + 500);
        setExpirationTimeText(expirationTime + 500);
    }

    private void setExpirationTimeText(long expirationTime) {
        expirationTime = expirationTime - 500;
        String time = "0s";
        if (expirationTime == 10 * 1000)
            time = "10s";
        if (expirationTime == 60 * 1000)
            time = "60s";
        if (expirationTime == 15 * 60 * 1000)
            time = "15min";
        if (expirationTime == 60 * 60 * 1000)
            time = "60min";
        mExpirationTimeText.setText(time);
    }

    private void showBottomBarSizePopup() {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_bottom_bar, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mBottomBarDropDown = new PopupWindow(layout, mPopupBottomBarView.getMeasuredWidth(),
                    mPopupBottomBarView.getMeasuredHeight(),true);//FrameLayout.LayoutParams.WRAP_CONTENT

            final CardView largeCard = layout.findViewById(R.id.large_card);
            final CardView mediumCard = layout.findViewById(R.id.medium_card);
            final CardView smallCard = layout.findViewById(R.id.small_card);
            final CardView noneCard = layout.findViewById(R.id.none_card);

            largeCard.setOnClickListener(view -> updateBottomBar(SettingsManager.BOTTOM_BAR_LARGE));
            mediumCard.setOnClickListener(view -> updateBottomBar(SettingsManager.BOTTOM_BAR_MEDIUM));
            smallCard.setOnClickListener(view -> updateBottomBar(SettingsManager.BOTTOM_BAR_SMALL));
            noneCard.setOnClickListener(view -> updateBottomBar(SettingsManager.BOTTOM_BAR_NONE));

            mBottomBarDropDown.showAsDropDown(mBottomBarCard, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBottomBar(int bottomBarSize) {
        mBottomBarDropDown.dismiss();
        if (bottomBarSize == settingsMgr.getBottomBarSize())
            return;
        settingsMgr.setupBottomBarSize(bottomBarSize);
        mBottomBarSummary.setText(bottomBarTexts.get(bottomBarSize));
        mActivity.updateBottomBarSize(bottomBarSize);
    }

    private void showThumbNetModePopup() {
        LayoutInflater mInflater;
        try {
            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_thumb_net_mode, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mThumbNetModeDropDown = new PopupWindow(layout, mPopupThumbNetModeView.getMeasuredWidth(),
                    mPopupThumbNetModeView.getMeasuredHeight(),true);//FrameLayout.LayoutParams.WRAP_CONTENT

            final CardView wifiMobileCard = layout.findViewById(R.id.wifi_mobile_card);
            final CardView wifiCard = layout.findViewById(R.id.wifi_card);

            wifiMobileCard.setOnClickListener(view -> updateDataPlanAllowed(true));
            wifiCard.setOnClickListener(view -> updateDataPlanAllowed(false));

            mThumbNetModeDropDown.showAsDropDown(mThumbNetModeCard, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDataPlanAllowed(boolean flag) {
        settingsMgr.saveDataPlanAllowed(flag);
        if (mThumbNetModeDropDown != null) {
            mThumbNetModeDropDown.dismiss();
        }
        updateThumbNetModeText();
    }

    private void updateThumbNetModeText() {
        mThumbNetModeText.setText(settingsMgr.isDataPlanAllowed() ? R.string.cuc_how_to_upload_second_radio : R.string.cuc_how_to_upload_first_radio);
    }

    private void showExecuteOcrSizePopup() {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_execute_ocr, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mExecuteOcrDropDown = new PopupWindow(layout, mPopupExecuteOcrView.getMeasuredWidth(),
                    mPopupExecuteOcrView.getMeasuredHeight(),true);//FrameLayout.LayoutParams.WRAP_CONTENT

            final CardView askEverytimeCard = layout.findViewById(R.id.ask_everytime_card);
            final CardView activatedCard = layout.findViewById(R.id.activated_card);
            final CardView deactivatedCard = layout.findViewById(R.id.deactivated_card);

            askEverytimeCard.setOnClickListener(view -> updateExecuteOcr(SettingsManager.EXECUTE_OCR_ASK_EVERYTIME));
            activatedCard.setOnClickListener(view -> updateExecuteOcr(SettingsManager.EXECUTE_OCR_ACTIVATED));
            deactivatedCard.setOnClickListener(view -> updateExecuteOcr(SettingsManager.EXECUTE_OCR_DEACTIVATED));

            mExecuteOcrDropDown.showAsDropDown(mExecuteOcrCard, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateExecuteOcr(int executeOcrValue) {
        if (executeOcrValue != settingsMgr.getExecuteOcrValue()) {
            settingsMgr.setupExecuteOcrValue(executeOcrValue);
            mExecuteOcrSummary.setText(executeOcrTexts.get(executeOcrValue));
        }
        if (mExecuteOcrDropDown != null) {
            mExecuteOcrDropDown.dismiss();
        }
    }

    private void showOpenOfficePopup() {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_open_office, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mOpenOfficeDropDown = new PopupWindow(layout, mPopupOpenOfficeView.getMeasuredWidth(),
                    mPopupOpenOfficeView.getMeasuredHeight(),true);//FrameLayout.LayoutParams.WRAP_CONTENT

            final CardView optionalCard = layout.findViewById(R.id.optional_card);
            final CardView onlyOfficeAppCard = layout.findViewById(R.id.onlyoffice_app_card);
            final CardView inAppOfficeCard = layout.findViewById(R.id.in_app_office_card);
            final CardView externalAppCard = layout.findViewById(R.id.external_app_card);

            optionalCard.setOnClickListener(view -> updateOpenOffice(SettingsManager.OPEN_OFFICE_OPTIONAL));
            onlyOfficeAppCard.setOnClickListener(view -> updateOpenOffice(SettingsManager.OPEN_OFFICE_ONLY_OFFICE));
            inAppOfficeCard.setOnClickListener(view -> updateOpenOffice(SettingsManager.OPEN_OFFICE_IN_APP_OFFICE));
            externalAppCard.setOnClickListener(view -> updateOpenOffice(SettingsManager.OPEN_OFFICE_EXTERNAL_APP));

            mOpenOfficeDropDown.showAsDropDown(mOpenOfficeCard, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateOpenOffice(int openOfficeValue) {
        if (openOfficeValue != settingsMgr.getOpenOfficeValue()) {
            settingsMgr.setupOpenOfficeValue(openOfficeValue);
            mOpenOfficeSummary.setText(openOfficeTexts.get(openOfficeValue));
        }
        if (mOpenOfficeDropDown != null) {
            mOpenOfficeDropDown.dismiss();
        }
    }

    private void showOpenPdfPopup() {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_open_pdf, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mOpenPdfDropDown = new PopupWindow(layout, mPopupOpenPdfView.getMeasuredWidth(),
                    mPopupOpenPdfView.getMeasuredHeight(),true);//FrameLayout.LayoutParams.WRAP_CONTENT

            final CardView optionalCard = layout.findViewById(R.id.optional_card);
            final CardView inAppPdfCard = layout.findViewById(R.id.in_app_pdf_card);
            final CardView externalAppCard = layout.findViewById(R.id.external_app_card);

            optionalCard.setOnClickListener(view -> updateOpenPdf(SettingsManager.OPEN_PDF_OPTIONAL));
            inAppPdfCard.setOnClickListener(view -> updateOpenPdf(SettingsManager.OPEN_PDF_IN_APP_PDF));
            externalAppCard.setOnClickListener(view -> updateOpenPdf(SettingsManager.OPEN_PDF_EXTERNAL_APP));

            mOpenPdfDropDown.showAsDropDown(mOpenPdfCard, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateOpenPdf(int openPdfValue) {
        if (openPdfValue != settingsMgr.getOpenPdfValue()) {
            settingsMgr.setupOpenPdfValue(openPdfValue);
            mOpenPdfSummary.setText(openPdfTexts.get(openPdfValue));
        }
        if (mOpenPdfDropDown != null) {
            mOpenPdfDropDown.dismiss();
        }
    }

    public void uploadLog(String fileName) {
        logFileName = fileName;
        Intent chooserIntent = new Intent(mActivity, SeafilePathChooserActivity.class);
        chooserIntent.putExtra(SeafilePathChooserActivity.DATA_ACCOUNT, mActivity.getAccount());
        chooserIntent.putExtra(SeafilePathChooserActivity.IS_COPY, true);
        startActivityForResult(chooserIntent, CHOOSE_COPY_MOVE_DEST_REQUEST);
    }

    private boolean hasEncryptedRepoForCameraUploadAndBackup() {
        boolean canLocalDecrypt = false;
        RepoConfig repoConfig = databaseHelper.getRepoConfig(act.email);
        if (settingsMgr.isFolderAutomaticBackup()) {
            if (repoConfig != null) {
                final SeafRepo backupRepo = dataMgr.getCachedRepoByID(repoConfig.getRepoID());
                if (backupRepo != null) {
                    if (backupRepo.canLocalDecrypt()) {
                        canLocalDecrypt = true;
                    }
                }
            }
        }
        if (cameraManager.isCameraUploadEnabled()) {
            if (settingsMgr.getCameraUploadRepoId() != null) {
                final SeafRepo cameraUploadRepo = dataMgr.getCachedRepoByID(settingsMgr.getCameraUploadRepoId());
                if (cameraUploadRepo != null) {
                    if (cameraUploadRepo.canLocalDecrypt()) {
                        canLocalDecrypt = true;
                    }
                }
            }
        }
        return canLocalDecrypt;
    }

    private void showExceptionEncRepoDialog(boolean used) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = getLayoutInflater();
        RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.dialog_exception_encrypted_repo, null);

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        TextView detailText = (TextView) view.findViewById(R.id.detail_text);
        TextView askText = (TextView) view.findViewById(R.id.ask_text);
        CardView okCard = (CardView) view.findViewById(R.id.ok_card);
        TextView okText = (TextView) view.findViewById(R.id.ok_text);
        CardView cancelCard = (CardView) view.findViewById(R.id.cancel_card);
        TextView cancelText = (TextView) view.findViewById(R.id.cancel_text);

        okText.setText(R.string.yes);
        cancelText.setText(R.string.no);

        if (!used) {
            detailText.setText(R.string.can_use_enc_repo);
            askText.setText(R.string.make_exception_will_use_encrypted_repo);
        }

        okCard.setOnClickListener(v -> {
            updateExceptionEncryptedRepoSwitch(true);
            dialog.dismiss();
        });
        cancelCard.setOnClickListener(v -> {
            updateExceptionEncryptedRepoSwitch(false);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateExceptionEncryptedRepoSwitch(boolean flag) {
        settingsMgr.setupExceptionEncryptedRepo(flag);
        mExceptionEncryptedRepoSwitch.setChecked(flag);
        updateExceptionEncryptedRepoLayout();
    }

    private void updateExceptionEncryptedRepoLayout() {
        mExceptionEncryptedRepoLayout.setVisibility(settingsMgr.isPasswordAutoClearEnabled() ? View.VISIBLE : View.GONE);
        mExceptionEncryptedRepoSwitch.setChecked(settingsMgr.isExceptionEncryptedRepo());
    }

    public void updateThumbStatus() {
        int encThumbsCount = settingsMgr.getAllEncThumbsCount();
        int encImagesCount = settingsMgr.getAllEncImagesCount();

        if (encThumbsCount == -1 || encImagesCount == -1) {
            mThumbCalcResultText.setText("");
            mThumbCalcResultText.setVisibility(View.GONE);
        } else {
            mThumbCalcResultText.setText(String.format(getResources().getString(R.string.thumbnails_created), encThumbsCount, encImagesCount));
            mThumbCalcResultText.setVisibility(View.VISIBLE);
        }
        if (mActivity.getReposFragment().searchEncReposSize == -1) {
            mThumbCalcStatusText.setText("");
            mThumbCalcStatusText.setVisibility(View.GONE);
        } else {
            mThumbCalcStatusText.setText(getResources().getText(R.string.thumbnails_calculating));
            mThumbCalcStatusText.setVisibility(View.VISIBLE);
        }
    }

    public void updateThumbImagesCount(int encThumbsCount, int encImagesCount) {
        if (mActivity.getReposFragment().searchEncReposSize == -1) {
            settingsMgr.setupAllEncThumbsCount(encThumbsCount);
            settingsMgr.setupAllEncImagesCount(encImagesCount);
        }

        allEncThumbsCount = encThumbsCount == -1 ? 0 : encThumbsCount;
        allEncImagesCount = encImagesCount == -1 ? 0 : encImagesCount;
        updateThumbStatus();
    }

    private void showCacheMaximumSizePopup() {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_cache_maximum_size, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mCacheMaximumSizeDropDown = new PopupWindow(layout, mPopupCacheMaximumSizeView.getMeasuredWidth(),
                    mPopupCacheMaximumSizeView.getMeasuredHeight(),true);

            final CardView cacheMax1Card = layout.findViewById(R.id.cache_max_1_card);
            final CardView cacheMax5Card = layout.findViewById(R.id.cache_max_5_card);
            final CardView cacheMax10Card = layout.findViewById(R.id.cache_max_10_card);

            cacheMax1Card.setOnClickListener(view -> updateCacheMaximumSize(SettingsManager.CACHE_MAXIMUM_SIZE_1));
            cacheMax5Card.setOnClickListener(view -> updateCacheMaximumSize(SettingsManager.CACHE_MAXIMUM_SIZE_5));
            cacheMax10Card.setOnClickListener(view -> updateCacheMaximumSize(SettingsManager.CACHE_MAXIMUM_SIZE_10));

            mCacheMaximumSizeDropDown.showAsDropDown(mCacheMaximumSizeCard, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCacheMaximumSize(int cacheMaximumSize) {
        if (cacheMaximumSize != settingsMgr.getCacheMaximumSize()) {
            settingsMgr.setupCacheMaximumSize(cacheMaximumSize);
            mCacheMaximumSizeText.setText(cacheMaximumSize + "GB");
        }
        if (mCacheMaximumSizeDropDown != null) {
            mCacheMaximumSizeDropDown.dismiss();
        }
    }
}
