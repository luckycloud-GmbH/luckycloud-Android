package com.seafile.seadroid2.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricPrompt;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.Lists;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.seafile.seadroid2.LogService;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SeafConnection;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.account.AccountManager;
import com.seafile.seadroid2.cameraupload.CameraUploadManager;
import com.seafile.seadroid2.cameraupload.MediaObserverService;
import com.seafile.seadroid2.data.CheckUploadServiceEvent;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.DatabaseHelper;
import com.seafile.seadroid2.data.SeafCachedFile;
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.data.SeafLink;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.data.SeafStarredFile;
import com.seafile.seadroid2.data.SelectedFileInfo;
import com.seafile.seadroid2.data.ServerInfo;
import com.seafile.seadroid2.data.StorageManager;
import com.seafile.seadroid2.folderbackup.FolderBackupDBHelper;
import com.seafile.seadroid2.folderbackup.FolderBackupService;
import com.seafile.seadroid2.folderbackup.RepoConfig;
import com.seafile.seadroid2.monitor.FileMonitorService;
import com.seafile.seadroid2.notification.BaseNotificationProvider;
import com.seafile.seadroid2.notification.DownloadNotificationProvider;
import com.seafile.seadroid2.notification.UploadNotificationProvider;
import com.seafile.seadroid2.pdf.PDFUtils;
import com.seafile.seadroid2.play.exoplayer.CustomExoVideoPlayerActivity;
import com.seafile.seadroid2.play.VideoLinkStateListener;
import com.seafile.seadroid2.play.VideoLinkTask;
import com.seafile.seadroid2.transfer.DownloadTaskInfo;
import com.seafile.seadroid2.transfer.DownloadTaskManager;
import com.seafile.seadroid2.transfer.PendingUploadInfo;
import com.seafile.seadroid2.transfer.TransferManager;
import com.seafile.seadroid2.transfer.TransferService;
import com.seafile.seadroid2.transfer.TransferService.TransferBinder;
import com.seafile.seadroid2.transfer.UploadTaskInfo;
import com.seafile.seadroid2.transfer.UploadTaskManager;
import com.seafile.seadroid2.ui.CopyMoveContext;
import com.seafile.seadroid2.ui.CustomNotificationBuilder;
import com.seafile.seadroid2.ui.dialog.CustomProgressDialog;
import com.seafile.seadroid2.ui.NavContext;
import com.seafile.seadroid2.ui.WidgetUtils;
import com.seafile.seadroid2.ui.adapter.SeafItemAdapter;
import com.seafile.seadroid2.ui.adapter.SeafRepoTagAdapter;
import com.seafile.seadroid2.ui.dialog.AppChoiceDialog;
import com.seafile.seadroid2.ui.dialog.AppChoiceDialog2;
import com.seafile.seadroid2.ui.dialog.CopyMoveDialog;
import com.seafile.seadroid2.ui.dialog.DeleteFileDialog;
import com.seafile.seadroid2.ui.dialog.DeleteRepoDialog;
import com.seafile.seadroid2.ui.dialog.FetchFileDialog;
import com.seafile.seadroid2.ui.dialog.GalleryDialog;
import com.seafile.seadroid2.ui.dialog.NewDirDialog;
import com.seafile.seadroid2.ui.dialog.NewFileDialog;
import com.seafile.seadroid2.ui.dialog.NewFileFormatDialog;
import com.seafile.seadroid2.ui.dialog.NewRepoDialog;
import com.seafile.seadroid2.ui.dialog.OcrProgressDialog;
import com.seafile.seadroid2.ui.dialog.OpenOfficeDialog;
import com.seafile.seadroid2.ui.dialog.OpenPdfDialog;
import com.seafile.seadroid2.ui.dialog.PasswordDialog;
import com.seafile.seadroid2.ui.dialog.RenameFileDialog;
import com.seafile.seadroid2.ui.dialog.RenameRepoDialog;
import com.seafile.seadroid2.ui.dialog.SslConfirmDialog;
import com.seafile.seadroid2.ui.dialog.TaskDialog;
import com.seafile.seadroid2.ui.fragment.AccountsFragment;
import com.seafile.seadroid2.ui.fragment.ActivitiesFragment;
import com.seafile.seadroid2.ui.fragment.ReposFragment;
import com.seafile.seadroid2.ui.fragment.SettingsFragment;
import com.seafile.seadroid2.ui.fragment.StarredFragment;
import com.seafile.seadroid2.ui.fragment.TransferFragment;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.FileHelper;
import com.seafile.seadroid2.util.HomeWatcher;
import com.seafile.seadroid2.util.SeafileLog;
import com.seafile.seadroid2.util.Utils;
import com.seafile.seadroid2.util.UtilsJellyBean;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Executor;

public class BrowserActivity extends BaseActivity implements ReposFragment.OnFileSelectedListener,
        StarredFragment.OnStarredFileSelectedListener,
        FragmentManager.OnBackStackChangedListener,
        VideoLinkStateListener,
        UCropFragmentCallback {

    private static final String DEBUG_TAG = "BrowserActivity";
    public static final String ACTIONBAR_PARENT_PATH = "/";

    public static final String MULTI_FILES_PATHS = "com.seafile.seadroid2.fileschooser.paths";

    public static final String OPEN_FILE_DIALOG_FRAGMENT_TAG = "openfile_fragment";
    public static final String PASSWORD_DIALOG_FRAGMENT_TAG = "password_fragment";
    public static final String CHOOSE_APP_DIALOG_FRAGMENT_TAG = "choose_app_fragment";
    public static final String CHARE_LINK_PASSWORD_FRAGMENT_TAG = "share_link_password_fragment";
    public static final String PICK_FILE_DIALOG_FRAGMENT_TAG = "pick_file_fragment";
    public static final String GALLERY_DIALOG_FRAGMENT_TAG = "gallery_fragment";

    public static final int REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_POST_NOTIFICATION = 2;

    public static final String TAG_NEW_REPO_DIALOG_FRAGMENT = "NewRepoDialogFragment";
    public static final String TAG_DELETE_REPO_DIALOG_FRAGMENT = "DeleteRepoDialogFragment";
    public static final String TAG_DELETE_FILE_DIALOG_FRAGMENT = "DeleteFileDialogFragment";
    public static final String TAG_DELETE_FILES_DIALOG_FRAGMENT = "DeleteFilesDialogFragment";
    public static final String TAG_RENAME_REPO_DIALOG_FRAGMENT = "RenameRepoDialogFragment";
    public static final String TAG_RENAME_FILE_DIALOG_FRAGMENT = "RenameFileDialogFragment";
    public static final String TAG_COPY_MOVE_DIALOG_FRAGMENT = "CopyMoveDialogFragment";
    public static final String TAG_SORT_FILES_DIALOG_FRAGMENT = "SortFilesDialogFragment";

    public static final int INDEX_STARRED_TAB = 0;
    public static final int INDEX_ACTIVITIES_TAB = 1;
    public static final int INDEX_TRANSFER_TAB = 2;
    public static final int INDEX_LIBRARY_TAB = 3;
    public static final int INDEX_ACCOUNTS_TAB = 4;
    public static final int INDEX_SETTINGS_TAB = 5;
    public static final int TAB_COUNT = 6;

    private static final int[] ICONS = new int[]{
            R.drawable.ic_bottom_bar_favorites, R.drawable.ic_bottom_bar_activities,
            R.drawable.ic_bottom_bar_activities, R.drawable.ic_bottom_bar_libraries_1,
            R.drawable.ic_bottom_bar_account, R.drawable.ic_bottom_bar_settings
    };

    /***********  Start other activity  ***************/
    public static final int PICK_FILES_REQUEST = 1;
    public static final int PICK_PHOTOS_VIDEOS_REQUEST = 2;
    public static final int PICK_FILE_REQUEST = 3;
    public static final int TAKE_PHOTO_REQUEST = 4;
    public static final int CHOOSE_COPY_MOVE_DEST_REQUEST = 5;
    public static final int DOWNLOAD_FILE_REQUEST = 6;
    public static final int SHARE_DIALOG_REQUEST = 7;
    public static final int DOCUMENT_REQUEST = 8;

    private FetchFileDialog fetchFileDialog = null;
    private SeafileTabsAdapter adapter;
    private View mLayout;
    private FrameLayout mContainer;
    private ViewPager mViewPager;
    private NavContext navContext = new NavContext();
    public CopyMoveContext copyMoveContext;

    private CustomProgressDialog progressDialog;
    private LinearLayout bottomBarLayout;
    private RelativeLayout librariesContainer;
    private CardView starredCard;
    private CardView activitiesCard;
    private CardView librariesCardContainer;
    private CardView librariesCard;
    private CardView plusLibraryCard;
    public CardView accountsCard;
    private CardView settingsCard;
    private ImageView starredImage;
    private ImageView activitiesImage;
    private ImageView librariesImage;
    private ImageView accountsImage;
    private ImageView settingsImage;
    private TextView starredText;
    private TextView activitiesText;
    private TextView librariesText;
    private TextView accountsText;
    private TextView settingsText;

    private LinearLayout activitySelectLayout;
    private CardView activitySelectCard;
    private TextView activitySelectText;
    private CardView transferListSelectCard;
    private TextView transferListSelectText;

    public enum TabType {ACTIVITIES_TAB, TRANSFER_TAB}
    public TabType whichTab = TabType.ACTIVITIES_TAB;

    private DataManager dataManager = null;
    private TransferService txService = null;
    public FolderBackupService mFolderBackupService = null;
    private TransferReceiver mTransferReceiver;
    private AccountManager accountManager;

    public int currentPosition = INDEX_LIBRARY_TAB;
    public Intent copyMoveIntent;
    private Account account;
    private ServerInfo serverInfo = null;

    private Intent mediaObserver;
    private Intent monitorIntent;

    private Executor m_executor;
    private BiometricPrompt m_biometricPrompt;
    private BiometricPrompt.PromptInfo m_promptInfo;

    private Bundle m_savedInstanceState = null;

    public Dialog dialogForBrowserMenu;
    private CardView addRepoCard;
    private CardView addCard;
    private CardView editCard;
    private CardView transferListCard;
    private CardView cancelTransferTasksCard;
    private CardView clearAllTransferTasksCard;
    private CardView supportCard;
    private CardView sortByCard;
    private CardView nameUpCard;
    private CardView nameDownCard;
    private CardView lastModifiedUpCard;
    private CardView lastModifiedDownCard;
    private LinearLayout sortCollapseLayout;
    private ImageView directionImage;
    private ImageView arrowImage;
    private TextView sortContentText;
    private CardView gridByCard;
    private ImageView gridDirectionImage;
    private ImageView gridArrowImage;
    private TextView gridContentText;
    private LinearLayout gridCollapseLayout;
    private ImageView listCheckImage;
    private CardView listCard;
    private ImageView minimalListCheckImage;
    private CardView minimalListCard;
    private ImageView smallTileCheckImage;
    private CardView smallTileCard;
    private ImageView bigTileCheckImage;
    private CardView bigTileCard;
    private CardView transferSelectCard;
    private ImageView transferDirectionImage;
    private ImageView transferSelectImage;
    private TextView transferContentText;
    private LinearLayout transferCollapseLayout;
    private ImageView transferAllCheckImage;
    private CardView transferAllCard;
    private ImageView transferUploadCheckImage;
    private CardView transferUploadCard;
    private ImageView transferDownloadCheckImage;
    private CardView transferDownloadCard;
    private CardView repoSelectCard;
    private ImageView repoDirectionImage;
    private ImageView repoSelectImage;
    private TextView repoContentText;
    private LinearLayout repoCollapseLayout;
    private ImageView repoAllCheckImage;
    private CardView repoAllCard;
    private ImageView repoPersonalCheckImage;
    private CardView repoPersonalCard;
    private ImageView repoGroupCheckImage;
    private CardView repoGroupCard;
    private ImageView repoSharedCheckImage;
    private CardView repoSharedCard;
    private CardView tagsCard;
    private GridView tagsGrid;
    public SeafRepoTagAdapter repoTagAdapter;

    private GalleryDialog galleryDialog;

    private Boolean sortCollapse = true;
    private Boolean gridCollapse = true;
    private Boolean transferCollapse = true;
    private Boolean repoCollapse = true;

    public static final int INDEX_NOT_COLLAPSE = 0;
    public static final int INDEX_SORT_COLLAPSE = 1;
    public static final int INDEX_GRID_COLLAPSE = 2;
    public static final int INDEX_TRANSFER_COLLAPSE = 3;
    public static final int INDEX_REPO_COLLAPSE = 4;

    private TextView toolbarTitleLibrary;
    private TextView toolbarTitleNotLibrary;
    private CardView backMenuCard;
    private CardView personalMenuCard;
    private ImageView personalMenuImage;
    private CardView groupMenuCard;
    private ImageView groupMenuImage;
    private CardView sharedMenuCard;
    private ImageView sharedMenuImage;
    private CardView gridMenuCard;
    private ImageView gridMenuImage;
    private CardView overflowMenuCard;

    public static boolean searchVisibility = false;
    public boolean isSearchMode = false;

    private boolean keyboardListenersAttached = false;
    private boolean isKeyboardShowing = false;
    public boolean isRepoCreated = false;
    private String cropRepoID;
    private String cropRepoName;
    private String cropPath;
    private String cropFileName;
    private List<String> cropFilePaths = new ArrayList<>();
    private int performOcrCount = 0;
    private List<JSONArray> ocrData = new ArrayList<>();
    private OcrProgressDialog ocrProgressDialog;

    private List<String> pdfConvertList = new ArrayList<>();
    private ActivityResultLauncher<IntentSenderRequest> scannerLauncher;
    private PopupWindow mExecuteOcrDropDown = null;
    private View mPopupExecuteOcrView;
    public PopupWindow mOpenOfficeDropDown = null;
    public View mPopupOpenOfficeView;
    public PopupWindow mOpenPdfDropDown = null;
    public View mPopupOpenPdfView;
    private String logRepoName = "luckycloud - Support";
    private ArrayList<String> logNameList = new ArrayList<>();

    public boolean needUpdateThumbsStatusOfEncRepo = false;

    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {

            Rect r = new Rect();
            mContainer.getWindowVisibleDisplayFrame(r);
            int screenHeight = mContainer.getRootView().getHeight();

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
                if (!isKeyboardShowing) {
                    isKeyboardShowing = true;
                    onKeyboardVisibilityChanged(true);
                }
            }
            else {
                // keyboard is closed
                if (isKeyboardShowing) {
                    isKeyboardShowing = false;
                    onKeyboardVisibilityChanged(false);
                }
            }
        }
    };

    public DataManager getDataManager() {
        return dataManager;
    }

    public void newDataManager() {
        dataManager = new DataManager(account);
    }

    private boolean getIsCopyToLocal(String localFilePath) {
        if (cropFileName != null && cropFileName.equals(Utils.fileNameFromPath(localFilePath))) {
            cropFileName = null;
            return false;
        }
        return true;
    }

    public void addUpdateTask(String repoID, String repoName, String targetDir, String localFilePath) {
        if (txService != null) {
            txService.addTaskToUploadQue(account, repoID, repoName, targetDir, localFilePath, true, getIsCopyToLocal(localFilePath));
        } else {
            PendingUploadInfo info = new PendingUploadInfo(repoID, repoName, targetDir, localFilePath, true, getIsCopyToLocal(localFilePath));
            pendingUploads.add(info);
        }
    }

    public void addUpdateBlocksTask(String repoID, String repoName, String targetDir, String localFilePath) {
        if (txService != null) {
            txService.addTaskToUploadQueBlock(account, repoID, repoName, targetDir, localFilePath, true, getIsCopyToLocal(localFilePath));
        } else {
            PendingUploadInfo info = new PendingUploadInfo(repoID, repoName, targetDir, localFilePath, true, getIsCopyToLocal(localFilePath));
            pendingUploads.add(info);
        }
    }

    private int addUploadTask(String repoID, String repoName, String targetDir, String localFilePath) {
        if (txService != null) {
            return txService.addTaskToUploadQue(account, repoID, repoName, targetDir, localFilePath, false, getIsCopyToLocal(localFilePath));
        } else {
            PendingUploadInfo info = new PendingUploadInfo(repoID, repoName, targetDir, localFilePath, false, getIsCopyToLocal(localFilePath));
            pendingUploads.add(info);
            return 0;
        }
    }

    private int addUploadBlocksTask(String repoID, String repoName, String targetDir, String localFilePath) {
        if (txService != null) {
            return txService.addTaskToUploadQueBlock(account, repoID, repoName, targetDir, localFilePath, false, getIsCopyToLocal(localFilePath));
        } else {
            PendingUploadInfo info = new PendingUploadInfo(repoID, repoName, targetDir, localFilePath, false, getIsCopyToLocal(localFilePath));
            pendingUploads.add(info);
            return 0;
        }
    }

    private ArrayList<PendingUploadInfo> pendingUploads = Lists.newArrayList();

    public TransferService getTransferService() {
        return txService;
    }

    public Account getAccount() {
        return account;
    }

    public NavContext getNavContext() {
        return navContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

//        startService(new Intent(this, LogService.class));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(BrowserActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(BrowserActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATION);
            }
        }
        accountManager = new AccountManager(this);

        // restart service should it have been stopped for some reason
        Intent mediaObserver = new Intent(this, MediaObserverService.class);
        startService(mediaObserver);

        Intent dIntent = new Intent(this, FolderBackupService.class);
        startService(dIntent);
        Log.d(DEBUG_TAG, "----start FolderBackupService");

        Intent dirIntent = new Intent(this, FolderBackupService.class);
        bindService(dirIntent, folderBackupConnection, Context.BIND_AUTO_CREATE);
        Log.d(DEBUG_TAG, "----try bind FolderBackupService");

        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            final String intentAction = getIntent().getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }

        setContentView(R.layout.tabs_main);

        mLayout = findViewById(R.id.main_layout);
        mContainer = (FrameLayout) findViewById(R.id.bottom_sheet_container);

        progressDialog = new CustomProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setHeight((int) getResources().getDimension(R.dimen.normal_progress_logo_width));
        progressDialog.show();

        initTopButtonGroup();

        attachKeyboardListeners();

        setToolbarTitle("");

        initBottomButtonGroup();

        initActivitySelectLayout();

        initDialogForBrowserMenu();

        // Get the message from the intent

        m_savedInstanceState = savedInstanceState;

        account = accountManager.getCurrentAccount();
        if (account == null || !account.hasValidToken()) {
            finishAndStartAccountsActivity();
            return;
        } else {
            List<String> loginConfig = SeadroidApplication.getInstance().getLoginConfig();
            if (loginConfig.size() == 3) {
                if (!account.name.equals(loginConfig.get(1)) && !account.email.equals(loginConfig.get(1))) {
                    finishAndStartAccountsActivity();
                    return;
                }
            }
            initFunc();
        }

        //For AutoLock
        HomeWatcher homeWatcher = new HomeWatcher(this);
        homeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                UnlockActivity.g_HOME_PRESSED = true;
            }
            @Override
            public void onHomeLongPressed() {
            }
        });
        homeWatcher.startWatch();

        ScreenOffReceiver receiver = new ScreenOffReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, intentFilter);

        scannerLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), this::handleActivityResult);

        processIntent(getIntent());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        });
    }

    private void processIntent(Intent intent){
        boolean flag = intent.getBooleanExtra(BaseNotificationProvider.TO_TRANSFER_LIST, false);
        if (flag) {
            activitiesCard.callOnClick();
            transferListSelectCard.callOnClick();
        }
    }

    private void initActivitySelectLayout() {
        activitySelectLayout = (LinearLayout) findViewById(R.id.activity_select_layout);
        activitySelectCard = (CardView) findViewById(R.id.activity_select_card);
        activitySelectText = (TextView) findViewById(R.id.activity_select_text);
        transferListSelectCard = (CardView) findViewById(R.id.transfer_list_select_card);
        transferListSelectText = (TextView) findViewById(R.id.transfer_list_select_text);

        activitySelectCard.setOnClickListener(v -> {
            whichTab = TabType.ACTIVITIES_TAB;
            activitiesCard.callOnClick();
        });

        transferListSelectCard.setOnClickListener(v -> {
            whichTab = TabType.TRANSFER_TAB;
            activitiesCard.callOnClick();
        });
    }

    public void updateActivitySelectLayout(int currentPosition) {
        if (currentPosition != INDEX_ACTIVITIES_TAB && currentPosition != INDEX_TRANSFER_TAB) {
            activitySelectLayout.setVisibility(View.GONE);
            return;
        }

        activitySelectLayout.setVisibility(View.VISIBLE);
        if (currentPosition == INDEX_ACTIVITIES_TAB)
            whichTab = TabType.ACTIVITIES_TAB;
        if (currentPosition == INDEX_TRANSFER_TAB)
            whichTab = TabType.TRANSFER_TAB;

        activitySelectText.setBackground(getResources().getDrawable(
                whichTab == TabType.ACTIVITIES_TAB? R.drawable.rounded_transfer_button_on : R.drawable.rounded_transfer_button_off));
        activitySelectText.setTextColor(getResources().getColor(
                whichTab == TabType.ACTIVITIES_TAB? R.color.white : R.color.luckycloud_green));
        transferListSelectText.setBackground(getResources().getDrawable(
                whichTab == TabType.ACTIVITIES_TAB? R.drawable.rounded_transfer_button_off : R.drawable.rounded_transfer_button_on));
        transferListSelectText.setTextColor(getResources().getColor(
                whichTab == TabType.ACTIVITIES_TAB? R.color.luckycloud_green : R.color.white));
    }

    private void initTopButtonGroup() {
        toolbarTitleLibrary = (TextView) findViewById(R.id.toolbar_title_library);
        toolbarTitleNotLibrary = (TextView) findViewById(R.id.toolbar_title_not_library);
        backMenuCard = (CardView) findViewById(R.id.back_menu_card);
        personalMenuCard = (CardView) findViewById(R.id.personal_menu_card);
        personalMenuImage = (ImageView) findViewById(R.id.personal_menu_image);
        groupMenuCard = (CardView) findViewById(R.id.group_menu_card);
        groupMenuImage = (ImageView) findViewById(R.id.group_menu_image);
        sharedMenuCard = (CardView) findViewById(R.id.shared_menu_card);
        sharedMenuImage = (ImageView) findViewById(R.id.shared_menu_image);
        gridMenuCard = (CardView) findViewById(R.id.grid_menu_card);
        gridMenuImage = (ImageView) findViewById(R.id.grid_menu_image);
        overflowMenuCard = (CardView) findViewById(R.id.overflow_menu_card);

        disableUpButton();
        viewRepoMenuCard(false);
        gridMenuCard.setVisibility(View.GONE);
        overflowMenuCard.setVisibility(View.GONE);

        updateGridMenuIcon();
        gridMenuCard.setOnClickListener(v -> {
            switch (SettingsManager.instance().getGridFilesTypePref()) {
                case SettingsManager.GRID_BY_MINIMAL_LIST:
                    changeView(SettingsManager.GRID_BY_SMALL_TILE);
                    break;
                case SettingsManager.GRID_BY_SMALL_TILE:
                    changeView(SettingsManager.GRID_BY_BIG_TILE);
                    break;
                case SettingsManager.GRID_BY_BIG_TILE:
                    changeView(SettingsManager.GRID_BY_LIST);
                    break;
                default:
                    changeView(SettingsManager.GRID_BY_MINIMAL_LIST);
                    break;
            }
        });

        backMenuCard.setOnClickListener(v -> {
            backPressed();
        });
        overflowMenuCard.setOnClickListener(v -> {
            SettingsManager settingsManager = SettingsManager.instance();
            if (serverInfo == null) return;

            int checkedItem = calculateCheckedItem();
            upgradeCollapse(INDEX_NOT_COLLAPSE, false);
            arrowImage.setImageDrawable(getResources().getDrawable((checkedItem % 2 == 0) ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down));
            sortContentText.setText(getResources().getString( (checkedItem < 2) ? R.string.name_hint : R.string.last_modified));

            int imageResource = R.drawable.ic_list;
            int textResource = R.string.list;
            listCheckImage.setVisibility(View.GONE);
            minimalListCheckImage.setVisibility(View.GONE);
            smallTileCheckImage.setVisibility(View.GONE);
            bigTileCheckImage.setVisibility(View.GONE);
            switch (settingsManager.getGridFilesTypePref()) {
                case SettingsManager.GRID_BY_SMALL_TILE:
                    imageResource = R.drawable.ic_small_tile;
                    textResource = R.string.small_tile;
                    smallTileCheckImage.setVisibility(View.VISIBLE);
                    break;
                case SettingsManager.GRID_BY_BIG_TILE:
                    imageResource = R.drawable.ic_big_tile;
                    textResource = R.string.big_tile;
                    bigTileCheckImage.setVisibility(View.VISIBLE);
                    break;
                case SettingsManager.GRID_BY_MINIMAL_LIST:
                    textResource = R.string.minimal_list;
                    minimalListCheckImage.setVisibility(View.VISIBLE);
                    break;
                default:
                    listCheckImage.setVisibility(View.VISIBLE);
                    break;
            }
            gridArrowImage.setImageDrawable(getResources().getDrawable(imageResource));
            gridContentText.setText(getResources().getString(textResource));

            int transferTextResource = R.string.upload_and_download;
            transferAllCheckImage.setVisibility(View.GONE);
            transferUploadCheckImage.setVisibility(View.GONE);
            transferDownloadCheckImage.setVisibility(View.GONE);
            switch (settingsManager.getTransferTypePref()) {
                case SettingsManager.TRANSFER_UPLOAD:
                    transferTextResource = R.string.upload;
                    transferUploadCheckImage.setVisibility(View.VISIBLE);
                    break;
                case SettingsManager.TRANSFER_DOWNLOAD:
                    transferTextResource = R.string.download;
                    transferDownloadCheckImage.setVisibility(View.VISIBLE);
                    break;
                default:
                    transferAllCheckImage.setVisibility(View.VISIBLE);
                    break;
            }
            transferContentText.setText(getResources().getString(transferTextResource));

            updateRepoTypeOfBrowserMenu();

            if (navContext.inRepo() && currentPosition == INDEX_LIBRARY_TAB && !isSearchMode) {
                tagsCard.setVisibility(View.VISIBLE);
                SeafRepo repo = dataManager.getCachedRepoByID(navContext.getRepoID());
                repoTagAdapter = new SeafRepoTagAdapter(this, navContext, repo);
                repoTagAdapter.setItems(dataManager.getCachedRepoByID(navContext.getRepoID()).getRepoTags());
                tagsGrid.setAdapter(repoTagAdapter);
            } else {
                tagsCard.setVisibility(View.GONE);
            }

            dialogForBrowserMenu.show();
        });

        personalMenuCard.setOnClickListener(v -> {
            getReposFragment().repoType(
                    SettingsManager.REPO_TYPE_PERSONAL,
                    !SettingsManager.instance().getRepoTypePersonalPref());
        });

        groupMenuCard.setOnClickListener(v -> {
            getReposFragment().repoType(
                    SettingsManager.REPO_TYPE_GROUP,
                    !SettingsManager.instance().getRepoTypeGroupPref());
        });

        sharedMenuCard.setOnClickListener(v -> {
            getReposFragment().repoType(
                    SettingsManager.REPO_TYPE_SHARED,
                    !SettingsManager.instance().getRepoTypeSharedPref());
        });
    }

    public void updateRepoTypeOfBrowserMenu() {
        SettingsManager settingsManager = SettingsManager.instance();
        int repoTextResource = R.string.all_data;
        repoAllCheckImage.setVisibility(View.GONE);
        repoPersonalCheckImage.setVisibility(View.GONE);
        repoGroupCheckImage.setVisibility(View.GONE);
        repoSharedCheckImage.setVisibility(View.GONE);

        boolean repoPersonal = settingsManager.getRepoTypePersonalPref();
        boolean repoGroup = settingsManager.getRepoTypeGroupPref();
        boolean repoShared = settingsManager.getRepoTypeSharedPref();

        String repoContent = "";

        if (repoPersonal) {
            repoPersonalCheckImage.setVisibility(View.VISIBLE);
            repoContent += getResources().getString(R.string.personal);
        }
        if (repoGroup) {
            repoGroupCheckImage.setVisibility(View.VISIBLE);
            repoContent +=
                    (repoContent.isEmpty() ? "" : ", ") + getResources().getString(R.string.group);
        }
        if (repoShared) {
            repoSharedCheckImage.setVisibility(View.VISIBLE);
            repoContent +=
                    (repoContent.isEmpty() ? "" : ", ") + getResources().getString(R.string.shared);
        }
        if (repoPersonal && repoGroup && repoShared) {
            repoAllCheckImage.setVisibility(View.VISIBLE);
            repoContent = getResources().getString(R.string.all_data);
        }
        repoContentText.setText(repoContent);
    }

    private void initBottomButtonGroup() {
        bottomBarLayout = findViewById(R.id.bottomBar);
        librariesContainer = findViewById(R.id.librariesContainer);
        starredCard = findViewById(R.id.starredCard);
        activitiesCard = findViewById(R.id.activitiesCard);
        librariesCardContainer = findViewById(R.id.librariesCardContainer);
        librariesCard = findViewById(R.id.librariesCard);
        plusLibraryCard = findViewById(R.id.plusLibraryCard);
        accountsCard = findViewById(R.id.accountsCard);
        settingsCard = findViewById(R.id.settingsCard);
        starredImage = findViewById(R.id.starredImage);
        activitiesImage = findViewById(R.id.activitiesImage);
        librariesImage = findViewById(R.id.librariesImage);
        accountsImage = findViewById(R.id.accountsImage);
        settingsImage = findViewById(R.id.settingsImage);
        starredText = findViewById(R.id.starredText);
        activitiesText = findViewById(R.id.activitiesText);
        librariesText = findViewById(R.id.librariesText);
        accountsText = findViewById(R.id.accountsText);
        settingsText = findViewById(R.id.settingsText);

        starredCard.setOnClickListener(v -> {
            if (serverInfo == null) return;
            if (currentPosition == INDEX_STARRED_TAB) {
                getStarredFragmentWithoutInstantiate().stopSupportActionMode();
                return;
            }
            mViewPager.setCurrentItem(INDEX_STARRED_TAB);
        });
        activitiesCard.setOnClickListener(v -> {
            if (serverInfo == null) return;
            mViewPager.setCurrentItem(whichTab == TabType.ACTIVITIES_TAB? INDEX_ACTIVITIES_TAB : INDEX_TRANSFER_TAB);
        });
        librariesCard.setOnClickListener(v -> {
            if (serverInfo == null) return;
            if (currentPosition == INDEX_LIBRARY_TAB) {
                if (isSearchMode) {
                    backPressed();
                    return;
                }
                if (navContext.inRepo()) {
                    getReposFragmentWithoutInstantiate().stopSupportActionMode();
                    setToolbarTitle(getResources().getString(R.string.libraries));
                    navContext.setRepoID(null);
                }
                getReposFragment().clearAdapterData();
                getReposFragment().refreshView(true);
                return;
            }
            mViewPager.setCurrentItem(INDEX_LIBRARY_TAB);
        });
        plusLibraryCard.setOnClickListener(v -> {
            if (serverInfo == null) return;
            if (navContext.inRepo()) {
                if (hasRepoWritePermission()) {
                    addFile();
                } else {
                    showShortToast(this, getString(R.string.cannot_add_change_in_read_only));
                }
            } else {
                showNewRepoDialog();
            }
        });
        accountsCard.setOnClickListener(v -> {
            if (serverInfo == null) return;
            mViewPager.setCurrentItem(INDEX_ACCOUNTS_TAB);
        });
        settingsCard.setOnClickListener(v -> {
            if (serverInfo == null) return;
            mViewPager.setCurrentItem(INDEX_SETTINGS_TAB);
        });

        updateBottomBarSize(SettingsManager.instance().getBottomBarSize());
    }

    private void initDialogForBrowserMenu() {
        dialogForBrowserMenu = Utils.dialogForActionBar(this);
        dialogForBrowserMenu.setContentView(R.layout.dialog_browser_menu);

        addRepoCard = dialogForBrowserMenu.findViewById(R.id.add_repo_card);
        addCard = dialogForBrowserMenu.findViewById(R.id.add_card);
        editCard = dialogForBrowserMenu.findViewById(R.id.edit_card);
        transferListCard = dialogForBrowserMenu.findViewById(R.id.transfer_list_card);
        cancelTransferTasksCard = dialogForBrowserMenu.findViewById(R.id.cancel_transfer_tasks_card);
        clearAllTransferTasksCard = dialogForBrowserMenu.findViewById(R.id.clear_all_transfer_tasks_card);
        supportCard = dialogForBrowserMenu.findViewById(R.id.support_card);
        sortByCard = dialogForBrowserMenu.findViewById(R.id.sort_by_card);
        nameUpCard = dialogForBrowserMenu.findViewById(R.id.name_up_card);
        nameDownCard = dialogForBrowserMenu.findViewById(R.id.name_down_card);
        lastModifiedUpCard = dialogForBrowserMenu.findViewById(R.id.last_modified_up_card);
        lastModifiedDownCard = dialogForBrowserMenu.findViewById(R.id.last_modified_down_card);
        sortCollapseLayout = dialogForBrowserMenu.findViewById(R.id.sort_collapse_layout);
        directionImage = dialogForBrowserMenu.findViewById(R.id.direction_image);
        arrowImage = dialogForBrowserMenu.findViewById(R.id.arrow_image);
        sortContentText = dialogForBrowserMenu.findViewById(R.id.sort_content_text);
        gridByCard = dialogForBrowserMenu.findViewById(R.id.grid_by_card);
        gridDirectionImage = dialogForBrowserMenu.findViewById(R.id.grid_direction_image);
        gridArrowImage = dialogForBrowserMenu.findViewById(R.id.grid_arrow_image);
        gridContentText = dialogForBrowserMenu.findViewById(R.id.grid_content_text);
        gridCollapseLayout = dialogForBrowserMenu.findViewById(R.id.grid_collapse_layout);
        listCheckImage = dialogForBrowserMenu.findViewById(R.id.list_check_image);
        listCard = dialogForBrowserMenu.findViewById(R.id.list_card);
        minimalListCheckImage = dialogForBrowserMenu.findViewById(R.id.minimal_list_check_image);
        minimalListCard = dialogForBrowserMenu.findViewById(R.id.minimal_list_card);
        smallTileCheckImage = dialogForBrowserMenu.findViewById(R.id.small_tile_check_image);
        smallTileCard = dialogForBrowserMenu.findViewById(R.id.small_tile_card);
        bigTileCheckImage = dialogForBrowserMenu.findViewById(R.id.big_tile_check_image);
        bigTileCard = dialogForBrowserMenu.findViewById(R.id.big_tile_card);
        transferSelectCard = dialogForBrowserMenu.findViewById(R.id.transfer_select_card);
        transferDirectionImage = dialogForBrowserMenu.findViewById(R.id.transfer_direction_image);
        transferSelectImage = dialogForBrowserMenu.findViewById(R.id.transfer_select_image);
        transferContentText = dialogForBrowserMenu.findViewById(R.id.transfer_content_text);
        transferCollapseLayout = dialogForBrowserMenu.findViewById(R.id.transfer_collapse_layout);
        transferAllCheckImage = dialogForBrowserMenu.findViewById(R.id.transfer_all_check_image);
        transferAllCard = dialogForBrowserMenu.findViewById(R.id.transfer_all_card);
        transferUploadCheckImage = dialogForBrowserMenu.findViewById(R.id.transfer_upload_check_image);
        transferUploadCard = dialogForBrowserMenu.findViewById(R.id.transfer_upload_card);
        transferDownloadCheckImage = dialogForBrowserMenu.findViewById(R.id.transfer_download_check_image);
        transferDownloadCard = dialogForBrowserMenu.findViewById(R.id.transfer_download_card);
        repoSelectCard = dialogForBrowserMenu.findViewById(R.id.repo_select_card);
        repoDirectionImage = dialogForBrowserMenu.findViewById(R.id.repo_direction_image);
        repoSelectImage = dialogForBrowserMenu.findViewById(R.id.repo_select_image);
        repoContentText = dialogForBrowserMenu.findViewById(R.id.repo_content_text);
        repoCollapseLayout = dialogForBrowserMenu.findViewById(R.id.repo_collapse_layout);
        repoAllCheckImage = dialogForBrowserMenu.findViewById(R.id.repo_all_check_image);
        repoAllCard = dialogForBrowserMenu.findViewById(R.id.repo_all_card);
        repoPersonalCheckImage = dialogForBrowserMenu.findViewById(R.id.repo_personal_check_image);
        repoPersonalCard = dialogForBrowserMenu.findViewById(R.id.repo_personal_card);
        repoGroupCheckImage = dialogForBrowserMenu.findViewById(R.id.repo_group_check_image);
        repoGroupCard = dialogForBrowserMenu.findViewById(R.id.repo_group_card);
        repoSharedCheckImage = dialogForBrowserMenu.findViewById(R.id.repo_shared_check_image);
        repoSharedCard = dialogForBrowserMenu.findViewById(R.id.repo_shared_card);
        tagsCard = dialogForBrowserMenu.findViewById(R.id.tags_card);
        tagsGrid = dialogForBrowserMenu.findViewById(R.id.tags_grid);

        addRepoCard.setOnClickListener(v -> {
            showNewRepoDialog();
            dialogForBrowserMenu.dismiss();
        });
        addCard.setOnClickListener(v -> {
//            addFile();
            if (navContext.inRepo()) {
                if (hasRepoWritePermission()) {
                    addFile();
                } else {
                    showShortToast(this, getString(R.string.cannot_add_change_in_read_only));
                }
            }
            dialogForBrowserMenu.dismiss();
        });

        editCard.setOnClickListener(v -> {
            if (!Utils.isNetworkOn()) {
                showShortToast(this, R.string.network_down);
                dialogForBrowserMenu.dismiss();
                return;
            }
            if (currentPosition == INDEX_LIBRARY_TAB) {
                if (navContext.inRepo()) {
                    SeafRepo repo = dataManager.getCachedRepoByID(navContext.getRepoID());
                    if (repo.encrypted && !dataManager.getRepoPasswordSet(repo.id)) {
                        String password = dataManager.getRepoPassword(repo.id);
                        showPasswordDialog(repo.name, repo.id,
                                new TaskDialog.TaskDialogListener() {
                                    @Override
                                    public void onTaskSuccess() {
                                        getReposFragment().startContextualActionMode();
                                    }
                                }, password);
                        dialogForBrowserMenu.dismiss();
                        return;
                    }
                }

                getReposFragment().startContextualActionMode();
            }
            dialogForBrowserMenu.dismiss();
        });

        transferListCard.setOnClickListener(v -> {
//            Intent newIntent = new Intent(this, TransferActivity.class);
//            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(newIntent);
            dialogForBrowserMenu.dismiss();
        });

        cancelTransferTasksCard.setOnClickListener(v -> {
            getTransferFragment().cancelAllDownloadUploadTasks();
            dialogForBrowserMenu.dismiss();
        });

        clearAllTransferTasksCard.setOnClickListener(v -> {
            getTransferFragment().removeAllTasks();
            dialogForBrowserMenu.dismiss();
        });

        supportCard.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://docs.luckycloud.de/de/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            dialogForBrowserMenu.dismiss();
        });

        sortByCard.setOnClickListener(v -> {
            upgradeCollapse(INDEX_SORT_COLLAPSE, sortCollapse);
        });

        nameUpCard.setOnClickListener(v -> {
            sortFiles(SettingsManager.SORT_BY_NAME, SettingsManager.SORT_ORDER_ASCENDING);
            dialogForBrowserMenu.dismiss();
        });

        nameDownCard.setOnClickListener(v -> {
            sortFiles(SettingsManager.SORT_BY_NAME, SettingsManager.SORT_ORDER_DESCENDING);
            dialogForBrowserMenu.dismiss();
        });

        lastModifiedUpCard.setOnClickListener(v -> {
            sortFiles(SettingsManager.SORT_BY_LAST_MODIFIED_TIME, SettingsManager.SORT_ORDER_ASCENDING);
            dialogForBrowserMenu.dismiss();
        });

        lastModifiedDownCard.setOnClickListener(v -> {
            sortFiles(SettingsManager.SORT_BY_LAST_MODIFIED_TIME, SettingsManager.SORT_ORDER_DESCENDING);
            dialogForBrowserMenu.dismiss();
        });

        gridByCard.setOnClickListener(v -> {
            upgradeCollapse(INDEX_GRID_COLLAPSE, gridCollapse);
        });

        listCard.setOnClickListener(v -> {
            changeView(SettingsManager.GRID_BY_LIST);
            dialogForBrowserMenu.dismiss();
        });

        minimalListCard.setOnClickListener(v -> {
            changeView(SettingsManager.GRID_BY_MINIMAL_LIST);
            dialogForBrowserMenu.dismiss();
        });

        smallTileCard.setOnClickListener(v -> {
            changeView(SettingsManager.GRID_BY_SMALL_TILE);
            dialogForBrowserMenu.dismiss();
        });

        bigTileCard.setOnClickListener(v -> {
            changeView(SettingsManager.GRID_BY_BIG_TILE);
            dialogForBrowserMenu.dismiss();
        });

        transferSelectCard.setOnClickListener(v -> {
            upgradeCollapse(INDEX_TRANSFER_COLLAPSE, transferCollapse);
        });

        transferAllCard.setOnClickListener(v -> {
            getTransferFragment().setTransferType(SettingsManager.TRANSFER_ALL);
            dialogForBrowserMenu.dismiss();
        });

        transferUploadCard.setOnClickListener(v -> {
            getTransferFragment().setTransferType(SettingsManager.TRANSFER_UPLOAD);
            dialogForBrowserMenu.dismiss();
        });

        transferDownloadCard.setOnClickListener(v -> {
            getTransferFragment().setTransferType(SettingsManager.TRANSFER_DOWNLOAD);
            dialogForBrowserMenu.dismiss();
        });

        repoSelectCard.setOnClickListener(v -> {
            upgradeCollapse(INDEX_REPO_COLLAPSE, repoCollapse);
        });

        repoAllCard.setOnClickListener(v -> {
            getReposFragment().repoType(SettingsManager.REPO_TYPE_PERSONAL, true);
            getReposFragment().repoType(SettingsManager.REPO_TYPE_GROUP, true);
            getReposFragment().repoType(SettingsManager.REPO_TYPE_SHARED, true);
        });

        repoPersonalCard.setOnClickListener(v -> {
            getReposFragment().repoType(
                    SettingsManager.REPO_TYPE_PERSONAL,
                    !SettingsManager.instance().getRepoTypePersonalPref());
        });

        repoGroupCard.setOnClickListener(v -> {
            getReposFragment().repoType(
                    SettingsManager.REPO_TYPE_GROUP,
                    !SettingsManager.instance().getRepoTypeGroupPref());
        });

        repoSharedCard.setOnClickListener(v -> {
            getReposFragment().repoType(
                    SettingsManager.REPO_TYPE_SHARED,
                    !SettingsManager.instance().getRepoTypeSharedPref());
        });
    }

    private void upgradeCollapse(int index, boolean collapse) {
        collapse = !collapse;
        sortCollapse = true;
        gridCollapse = true;
        transferCollapse = true;
        repoCollapse = true;
        if (index == INDEX_SORT_COLLAPSE) sortCollapse = collapse;
        if (index == INDEX_GRID_COLLAPSE) gridCollapse = collapse;
        if (index == INDEX_TRANSFER_COLLAPSE) transferCollapse = collapse;
        if (index == INDEX_REPO_COLLAPSE) repoCollapse = collapse;

        TransitionManager.beginDelayedTransition(sortCollapseLayout);
        ViewGroup.LayoutParams sortLayoutParams = sortCollapseLayout.getLayoutParams();
        sortLayoutParams.height = sortCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        sortCollapseLayout.requestLayout();
        directionImage.setImageDrawable(getResources().getDrawable((sortCollapse) ? R.drawable.ic_right : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(gridCollapseLayout);
        ViewGroup.LayoutParams gridLayoutParams = gridCollapseLayout.getLayoutParams();
        gridLayoutParams.height = gridCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        gridCollapseLayout.requestLayout();
        gridDirectionImage.setImageDrawable(getResources().getDrawable((gridCollapse) ? R.drawable.ic_right : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(transferCollapseLayout);
        ViewGroup.LayoutParams transferLayoutParams = transferCollapseLayout.getLayoutParams();
        transferLayoutParams.height = transferCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        transferCollapseLayout.requestLayout();
        transferDirectionImage.setImageDrawable(getResources().getDrawable((transferCollapse) ? R.drawable.ic_right : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(repoCollapseLayout);
        ViewGroup.LayoutParams repoLayoutParams = repoCollapseLayout.getLayoutParams();
        repoLayoutParams.height = repoCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        repoCollapseLayout.requestLayout();
        repoDirectionImage.setImageDrawable(getResources().getDrawable((repoCollapse) ? R.drawable.ic_right : R.drawable.ic_down));
    }

    private void starredCardColorChange(boolean flag) {
        starredCard.setCardBackgroundColor(getResources().getColor(flag? R.color.luckycloud_green : R.color.bottom_bar_background));
        starredImage.setColorFilter(getResources().getColor(flag? R.color.white : R.color.luckycloud_green));
        starredText.setTextColor(getResources().getColor(flag? R.color.white : R.color.luckycloud_green));
    }

    private void activitiesCardColorChange(boolean flag) {
        activitiesCard.setCardBackgroundColor(getResources().getColor(flag? R.color.luckycloud_green : R.color.bottom_bar_background));
        activitiesImage.setColorFilter(getResources().getColor(flag? R.color.white : R.color.luckycloud_green));
        activitiesText.setTextColor(getResources().getColor(flag? R.color.white : R.color.luckycloud_green));
    }

    private void librariesCardColorChange(boolean flag) {
        librariesCard.setCardBackgroundColor(getResources().getColor(flag? R.color.luckycloud_green : R.color.bottom_bar_background));
        librariesCardContainer.setCardBackgroundColor(getResources().getColor(flag? R.color.luckycloud_green : R.color.dots_type_grey));
        plusLibraryCard.setVisibility(flag? View.VISIBLE : View.GONE);
        librariesImage.setImageDrawable(getResources().getDrawable(
                flag? R.drawable.ic_bottom_bar_libraries_0 : R.drawable.ic_bottom_bar_libraries_1));
        librariesText.setTextColor(getResources().getColor(flag? R.color.white : R.color.luckycloud_green));

    }

    private void accountsCardColorChange(boolean flag) {
        accountsCard.setCardBackgroundColor(getResources().getColor(flag? R.color.luckycloud_green : R.color.bottom_bar_background));
        accountsImage.setColorFilter(getResources().getColor(flag? R.color.white : R.color.luckycloud_green));
        accountsText.setTextColor(getResources().getColor(flag? R.color.white : R.color.luckycloud_green));
    }

    private void settingsCardColorChange(boolean flag) {
        settingsCard.setCardBackgroundColor(getResources().getColor(flag? R.color.luckycloud_green : R.color.bottom_bar_background));
        settingsImage.setColorFilter(getResources().getColor(flag? R.color.white : R.color.luckycloud_green));
        settingsText.setTextColor(getResources().getColor(flag? R.color.white : R.color.luckycloud_green));
    }

    private void initBottomButtonGroupColor() {
        starredCardColorChange(false);
        activitiesCardColorChange(false);
        librariesCardColorChange(false);
        accountsCardColorChange(false);
        settingsCardColorChange(false);
    }

    private void initFunc(){
        Intent intent = getIntent();

        // Log.d(DEBUG_TAG, "browser activity onCreate " + account.server + " " + account.email);
        dataManager = new DataManager(account);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        unsetRefreshing();
        disableUpButton();



        if (m_savedInstanceState != null) {
            Log.d(DEBUG_TAG, "savedInstanceState is not null");
            fetchFileDialog = (FetchFileDialog) getSupportFragmentManager().findFragmentByTag(OPEN_FILE_DIALOG_FRAGMENT_TAG);

            AppChoiceDialog appChoiceDialog = (AppChoiceDialog) getSupportFragmentManager().findFragmentByTag(CHOOSE_APP_DIALOG_FRAGMENT_TAG);

            if (appChoiceDialog != null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.detach(appChoiceDialog);
                ft.commit();
            }

            SslConfirmDialog sslConfirmDlg = (SslConfirmDialog) getSupportFragmentManager().findFragmentByTag(SslConfirmDialog.FRAGMENT_TAG);

            if (sslConfirmDlg != null) {
                Log.d(DEBUG_TAG, "sslConfirmDlg is not null");
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.detach(sslConfirmDlg);
                ft.commit();
            } else {
                Log.d(DEBUG_TAG, "sslConfirmDlg is null");
            }

            String repoID = m_savedInstanceState.getString("repoID");
            String repoName = m_savedInstanceState.getString("repoName");
            String path = m_savedInstanceState.getString("path");
            String dirID = m_savedInstanceState.getString("dirID");
            String permission = m_savedInstanceState.getString("permission");
            if (repoID != null) {
                navContext.setRepoID(repoID);
                navContext.setRepoName(repoName);
                navContext.setDir(path, dirID);
                navContext.setDirPermission(permission);
            }
        }

        String repoID = intent.getStringExtra("repoID");
        String repoName = intent.getStringExtra("repoName");
        String path = intent.getStringExtra("path");
        String dirID = intent.getStringExtra("dirID");
        String permission = intent.getStringExtra("permission");
        if (repoID != null) {
            navContext.setRepoID(repoID);
            navContext.setRepoName(repoName);
            navContext.setDir(path, dirID);
            navContext.setDirPermission(permission);
        }


        Intent txIntent = new Intent(this, TransferService.class);
        startService(txIntent);
        Log.d(DEBUG_TAG, "start TransferService");

        // bind transfer service
        Intent bIntent = new Intent(this, TransferService.class);
        bindService(bIntent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d(DEBUG_TAG, "try bind TransferService");

        monitorIntent = new Intent(this, FileMonitorService.class);
        startService(monitorIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestServerInfo();
        } else {
            requestReadExternalStoragePermission();
        }
        Utils.startCameraSyncJob(this);
        syncCamera();

        if (SettingsManager.instance().isDeleteCacheAutomatic()) {
            ConcurrentAsyncTask.execute(new DeleteCacheAutomaticTask());
        }
    }

    private void initTabLayout() {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        adapter = new SeafileTabsAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                updateMenu();
                setToolbarTitleOnSlideTabs(position);
                updateActivitySelectLayout(position);
                initBottomButtonGroupColor();
                switch (position) {
                    case INDEX_STARRED_TAB:
                        starredCardColorChange(true);
                        break;
                    case INDEX_ACTIVITIES_TAB:
                    case INDEX_TRANSFER_TAB:
                        activitiesCardColorChange(true);
                        break;
                    case INDEX_LIBRARY_TAB:
                        librariesCardColorChange(true);
                        break;
                    case INDEX_ACCOUNTS_TAB:
                        accountsCardColorChange(true);
                        break;
                    case INDEX_SETTINGS_TAB:
                        settingsCardColorChange(true);
                        break;
                    default:
                        break;
                }
                if (mViewPager.getOffscreenPageLimit() == TAB_COUNT) {
                    if (position == INDEX_TRANSFER_TAB) {
                        getTransferFragment().startTimer();
                    } else {
                        getTransferFragment().stopTimer();
                    }

                    if (position == INDEX_SETTINGS_TAB) {
                        if (needUpdateThumbsStatusOfEncRepo) {
                            if (getReposFragment() != null) {
                                getReposFragment().getThumbImagesCountInEncryptedRepos();
                            }
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mViewPager.setCurrentItem(INDEX_LIBRARY_TAB);
        mViewPager.setOffscreenPageLimit(TAB_COUNT);
    }

    public void updateMenu() {
        supportCard.setVisibility(View.GONE);
        overflowMenuCard.setVisibility(
                (currentPosition != INDEX_ACCOUNTS_TAB &&
                        currentPosition != INDEX_SETTINGS_TAB &&
                        currentPosition != INDEX_ACTIVITIES_TAB) ?
                        View.VISIBLE : View.GONE
        );

        gridMenuCard.setVisibility(
                (currentPosition != INDEX_ACTIVITIES_TAB &&
                        currentPosition != INDEX_TRANSFER_TAB &&
                        currentPosition != INDEX_ACCOUNTS_TAB &&
                        currentPosition != INDEX_SETTINGS_TAB) ?
                        View.VISIBLE : View.GONE
        );

        // Libraries Tab
        if (currentPosition == INDEX_LIBRARY_TAB) {
            if (navContext.inRepo()) {
                addRepoCard.setVisibility(View.GONE);
                addCard.setVisibility(View.VISIBLE);
                editCard.setVisibility(View.VISIBLE);
                if (hasRepoWritePermission()) {
                    editCard.setClickable(true);
                } else {
                    editCard.setClickable(false);
                }

            } else {
                addRepoCard.setVisibility(View.VISIBLE);
                addCard.setVisibility(View.GONE);
                editCard.setVisibility(View.GONE);
            }
        } else {
            addRepoCard.setVisibility(View.GONE);
            addCard.setVisibility(View.GONE);
            editCard.setVisibility(View.GONE);
        }

        if (currentPosition == INDEX_LIBRARY_TAB || currentPosition == INDEX_STARRED_TAB) {
            sortByCard.setVisibility(View.VISIBLE);
            sortCollapseLayout.setVisibility(View.VISIBLE);
        } else {
            sortByCard.setVisibility(View.GONE);
            sortCollapseLayout.setVisibility(View.GONE);
        }

        if (currentPosition == INDEX_STARRED_TAB || currentPosition == INDEX_LIBRARY_TAB) {
            gridByCard.setVisibility(View.VISIBLE);
            gridCollapseLayout.setVisibility(View.VISIBLE);
        } else {
            gridByCard.setVisibility(View.GONE);
            gridCollapseLayout.setVisibility(View.GONE);
        }

        if (currentPosition == INDEX_LIBRARY_TAB && !navContext.inRepo()) {
            repoSelectCard.setVisibility(View.VISIBLE);
            repoCollapseLayout.setVisibility(View.VISIBLE);

            viewRepoMenuCard(!isSearchMode);

            if (SettingsManager.instance().getRepoTypePersonalPref()) {
                personalMenuImage.setColorFilter(getResources().getColor(R.color.luckycloud_green));
            } else {
                personalMenuImage.setColorFilter(getResources().getColor(R.color.dots_type_grey));
            }
            if (SettingsManager.instance().getRepoTypeGroupPref()) {
                groupMenuImage.setColorFilter(getResources().getColor(R.color.luckycloud_green));
            } else {
                groupMenuImage.setColorFilter(getResources().getColor(R.color.dots_type_grey));
            }
            if (SettingsManager.instance().getRepoTypeSharedPref()) {
                sharedMenuImage.setColorFilter(getResources().getColor(R.color.luckycloud_green));
            } else {
                sharedMenuImage.setColorFilter(getResources().getColor(R.color.dots_type_grey));
            }
        } else {
            repoSelectCard.setVisibility(View.GONE);
            repoCollapseLayout.setVisibility(View.GONE);

            viewRepoMenuCard(false);
        }

        transferListCard.setVisibility(View.GONE);
        if (currentPosition == INDEX_TRANSFER_TAB) {
            cancelTransferTasksCard.setVisibility(View.VISIBLE);
            clearAllTransferTasksCard.setVisibility(View.VISIBLE);

            transferSelectCard.setVisibility(View.VISIBLE);
            transferCollapseLayout.setVisibility(View.VISIBLE);
        } else {
            cancelTransferTasksCard.setVisibility(View.GONE);
            clearAllTransferTasksCard.setVisibility(View.GONE);

            transferSelectCard.setVisibility(View.GONE);
            transferCollapseLayout.setVisibility(View.GONE);
        }

        if (currentPosition != INDEX_LIBRARY_TAB) {
            disableUpButton();
        } else if (navContext.inRepo() || isSearchMode) {
            enableUpButton();
        }
    }

    public FrameLayout getmContainer() {
        return mContainer;
    }

    private void finishAndStartAccountsActivity() {
        Intent newIntent = new Intent(this, AccountsActivity.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(newIntent);
        finish();
    }

    private void requestServerInfo() {
//        if (!checkServerProEdition()) {
//            // hide Activity tab
//            adapter.hideActivityTab();
//            adapter.notifyDataSetChanged();
//            mTabLayout.setupWithViewPager(mViewPager);
//        }

        if (!checkSearchEnabled()) {
            // hide search menu
            searchVisibility = false;
            setVisibilityReposSearch(View.GONE);
        }

        if (m_savedInstanceState != null) {
            try {
                String json = m_savedInstanceState.getString("serverInfo");
                if (json != null) {
                    serverInfo = dataManager.parseServerInfo(json);
                    processServerInfo();
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (!Utils.isNetworkOn())
            return;

        ConcurrentAsyncTask.execute(new RequestServerInfoTask());
    }

    public void completeRemoteWipe() {
        ConcurrentAsyncTask.execute(new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... objects) {
                // clear local caches
                StorageManager storageManager = StorageManager.getInstance();
                storageManager.clearCache();

                // clear cached data from database
                DatabaseHelper dbHelper = DatabaseHelper.getDatabaseHelper();
                dbHelper.delCaches();

                try {
                    // response to server when finished cleaning caches
                    getDataManager().completeRemoteWipe();
                } catch (SeafException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void o) {
                // sign out current account
                logoutWhenTokenExpired();

            }
        });
    }

    /**
     * Token expired, clear current authorized info and redirect user to login page
     */
    public void logoutWhenTokenExpired() {
        AccountManager accountMgr = new AccountManager(this);

        // sign out current account
        Account account = accountMgr.getCurrentAccount();
        accountMgr.signOutAccount(account);

        // then redirect to AccountsActivity
        Intent intent = new Intent(this, AccountsActivity.class);
        startActivity(intent);

        // finish current Activity
        finish();
    }


    private int calculateCheckedItem() {
        switch (SettingsManager.instance().getSortFilesTypePref()) {
            case SettingsManager.SORT_BY_NAME:
                if (SettingsManager.instance().getSortFilesOrderPref() == SettingsManager.SORT_ORDER_ASCENDING)
                    return 0;
                else if (SettingsManager.instance().getSortFilesOrderPref() == SettingsManager.SORT_ORDER_DESCENDING)
                    return 1;
                break;
            case SettingsManager.SORT_BY_LAST_MODIFIED_TIME:
                if (SettingsManager.instance().getSortFilesOrderPref() == SettingsManager.SORT_ORDER_ASCENDING)
                    return 2;
                else if (SettingsManager.instance().getSortFilesOrderPref() == SettingsManager.SORT_ORDER_DESCENDING)
                    return 3;
                break;
        }
        return 0;
    }

    private void updateGridMenuIcon() {
        int imageResource = R.drawable.ic_list;
        switch (SettingsManager.instance().getGridFilesTypePref()) {
            case SettingsManager.GRID_BY_SMALL_TILE:
                imageResource = R.drawable.ic_small_tile;
                break;
            case SettingsManager.GRID_BY_BIG_TILE:
                imageResource = R.drawable.ic_big_tile;
                break;
            default:
                break;
        }
        gridMenuImage.setImageDrawable(getResources().getDrawable(imageResource));
    }

    /**
     * If the user is running Android 6.0 (API level 23) or later, the user has to grant your app its permissions while they are running the app
     * <p>
     * Requests the WRITE_EXTERNAL_STORAGE permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Snackbar.make(mLayout,
                                R.string.permission_read_exteral_storage_rationale,
                                Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(BrowserActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
                            }
                        })
                        .show();

            } else {

                // No explanation needed, we can request the permission.

                // WRITE_EXTERNAL_STORAGE permission has not been granted yet. Request it directly.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            requestServerInfo();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // Log.i(DEBUG_TAG, "Received response for permission request.");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                // Check if the only required permission has been granted
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    requestServerInfo();
                } else {
                    // permission denied
                }
            }
            case REQUEST_POST_NOTIFICATION: {
                SeadroidApplication.getInstance().initNotificationChannel();
            }
        }
    }

    class RequestServerInfoTask extends AsyncTask<Void, Void, ServerInfo> {
        private SeafException err;

        @Override
        protected ServerInfo doInBackground(Void... params) {
            try {
                return dataManager.getServerInfo();
            } catch (SeafException e) {
                err = e;
            } catch (JSONException e) {
                Log.e(DEBUG_TAG, "JSONException " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ServerInfo serverInfoResult) {
            // Check to see whether this activity is in the process of finishing
            // to avoid IllegalStateException when AsyncTasks continue to run after the activity has been destroyed
            // http://stackoverflow.com/a/35729068/3962551
            if (isFinishing()) return;

            serverInfo = serverInfoResult;
            if (serverInfo == null) {
                if (err != null)
                    showShortToast(BrowserActivity.this, err.getMessage());
                return;
            }

            processServerInfo();
        }
    }

    private void processServerInfo() {
        progressDialog.dismiss();
        initTabLayout();
        // Merge unHideActivityTab and hideActivityTab functions
//            if (serverInfo.isProEdition()) {
//                // show Activity tab
//                adapter.unHideActivityTab();
//                adapter.notifyDataSetChanged();
//                mTabLayout.setupWithViewPager(mViewPager);
//            }

        if (serverInfo.isSearchEnabled()) {
            // show search menu
            searchVisibility = true;
            setVisibilityReposSearch(View.VISIBLE);
        }

        accountManager.setServerInfo(account, serverInfo);
    }

    private void setVisibilityReposSearch(int visibility) {
        View searchLayout = getReposFragmentWithoutInstantiate().mSearchLayout;
        if (searchLayout == null) {
            Handler handler = new Handler();
            handler.postDelayed(
                    new Runnable() {
                        public void run() {
                            setVisibilityReposSearch(visibility);
                        }
                    }, 100L);
        } else {
            searchLayout.setVisibility(visibility);
        }
    }

    /**
     * check if server is pro edition
     *
     * @return true, if server is pro edition
     * false, otherwise.
     */
    public boolean checkServerProEdition() {
        if (account == null)
            return false;

        ServerInfo serverInfo = accountManager.getServerInfo(account);

        return serverInfo.isProEdition();
    }

    /**
     * check if server supports searching feature
     *
     * @return true, if search enabled
     * false, otherwise.
     */
    private boolean checkSearchEnabled() {
        if (account == null)
            return false;

        ServerInfo serverInfo = accountManager.getServerInfo(account);

        return serverInfo.isSearchEnabled();
    }

    private class SeafileTabsAdapter extends FragmentPagerAdapter {
        public SeafileTabsAdapter(FragmentManager fm) {
            super(fm);
        }

        private ReposFragment reposFragment = null;
        private ActivitiesFragment activitieFragment = null;
        private TransferFragment transferFragment = null;
        private StarredFragment starredFragment = null;
        private AccountsFragment accountsFragment = null;
        private SettingsFragment settingsFragment = null;
        private boolean isHideActivityTab;

        public void hideActivityTab() {
            this.isHideActivityTab = true;
        }

        public void unHideActivityTab() {
            this.isHideActivityTab = false;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case INDEX_STARRED_TAB:
                    if (starredFragment == null) {
                        starredFragment = new StarredFragment();
                    }
                    return starredFragment;
                case INDEX_ACTIVITIES_TAB:
//                    if (serverInfo.isProEdition()) {
//                        if (activitieFragment == null) {
//                            activitieFragment = new ActivitiesFragment();
//                        }
//                        return activitieFragment;
//                    } else {
//                        if (activitieCEFragment == null) {
//                            activitieCEFragment = new ActivitiesCEFragment();
//                        }
//                        return activitieCEFragment;
//                    }
                    if (activitieFragment == null) {
                        activitieFragment = new ActivitiesFragment();
                    }
                    return activitieFragment;
                case INDEX_TRANSFER_TAB:
                    if (transferFragment == null) {
                        transferFragment = new TransferFragment();
                    }
                    return transferFragment;
                case INDEX_LIBRARY_TAB:
                    if (reposFragment == null) {
                        reposFragment = new ReposFragment();
                    }
                    return reposFragment;
                case INDEX_ACCOUNTS_TAB:
                    if (accountsFragment == null) {
                        accountsFragment = new AccountsFragment();
                    }
                    return accountsFragment;
                case INDEX_SETTINGS_TAB:
                    if (settingsFragment == null) {
                        settingsFragment = new SettingsFragment();
                    }
                    return settingsFragment;
                default:
                    return new Fragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case INDEX_STARRED_TAB:
                    return getString(R.string.tabs_starred).toUpperCase();
                case INDEX_ACTIVITIES_TAB:
                case INDEX_TRANSFER_TAB:
                    return getString(R.string.tabs_activity).toUpperCase();
                case INDEX_LIBRARY_TAB:
                    return getString(R.string.tabs_library).toUpperCase();
                case INDEX_ACCOUNTS_TAB:
                    return getString(R.string.accounts).toUpperCase();
                case INDEX_SETTINGS_TAB:
                    return getString(R.string.settings).toUpperCase();

                default:
                    return null;
            }
        }

//        @Override
//        public int getIconResId(int index) {
//            return ICONS[index];
//        }

        @Override
        public int getCount() {
            if (!isHideActivityTab)
                return ICONS.length;
            else
                return 6; //original 4
        }
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        mViewPager.post(() -> {
            if (this.currentPosition != currentPosition) {
                this.currentPosition = currentPosition;
                mViewPager.setCurrentItem(currentPosition);
                setToolbarTitleOnSlideTabs(currentPosition);
                refreshViewOnSlideTabs(currentPosition);
            }
        });
    }

    public Fragment getFragment(int index) {
        return (Fragment)adapter.instantiateItem(mViewPager,index);
    }

    public ReposFragment getReposFragment() {
        return (ReposFragment) getFragment(INDEX_LIBRARY_TAB);
    }

    public ReposFragment getReposFragmentWithoutInstantiate() {
        return (ReposFragment) adapter.getItem(INDEX_LIBRARY_TAB);
    }

    public StarredFragment getStarredFragment() {
        return (StarredFragment) getFragment(INDEX_STARRED_TAB);
    }

    public StarredFragment getStarredFragmentWithoutInstantiate() {
        return (StarredFragment) adapter.getItem(INDEX_STARRED_TAB);
    }

    public ActivitiesFragment getActivitiesFragment() {
        return (ActivitiesFragment) getFragment(INDEX_ACTIVITIES_TAB);
    }

    public TransferFragment getTransferFragment() {
        return (TransferFragment) getFragment(INDEX_TRANSFER_TAB);
    }

    public AccountsFragment getAccountsFragment() {
        return (AccountsFragment) getFragment(INDEX_ACCOUNTS_TAB);
    }

    public SettingsFragment getSettingsFragment() {
        return (SettingsFragment) getFragment(INDEX_SETTINGS_TAB);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TransferBinder binder = (TransferBinder) service;
            txService = binder.getService();
            Log.d(DEBUG_TAG, "bind TransferService");

            for (PendingUploadInfo info : pendingUploads) {
                txService.addTaskToUploadQue(account,
                        info.repoID,
                        info.repoName,
                        info.targetDir,
                        info.localFilePath,
                        info.isUpdate,
                        info.isCopyToLocal);
            }
            pendingUploads.clear();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            txService = null;
        }
    };

    private final ServiceConnection folderBackupConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            FolderBackupService.FileBackupBinder binder = (FolderBackupService.FileBackupBinder) service;
            mFolderBackupService = binder.getService();
            Log.d(DEBUG_TAG, "-----bind FolderBackupService");
//            backupFolder();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mFolderBackupService = null;
        }
    };

    public void backupFolder() {
        if (mFolderBackupService != null) {
            boolean dirAutomaticUpload = SettingsManager.instance().isFolderAutomaticBackup();
            String backupEmail = SettingsManager.instance().getBackupEmail();
            if (dirAutomaticUpload && mFolderBackupService != null && !TextUtils.isEmpty(backupEmail)) {
                mFolderBackupService.backupFolder(backupEmail);
            }
        }
    }

    @Override
    public void onStart() {
        Log.d(DEBUG_TAG, "onStart");
        super.onStart();
        EventBus.getDefault().register(this);

        if (android.os.Build.VERSION.SDK_INT < 14 && SettingsManager.instance().isGestureLockRequired()) {
            Intent intent = new Intent(this, UnlockGesturePasswordActivity.class);
            startActivity(intent);
        }

        if (mTransferReceiver == null) {
            mTransferReceiver = new TransferReceiver();
        }

        IntentFilter filter = new IntentFilter(TransferManager.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mTransferReceiver, filter);
    }

    @Override
    protected void onPause() {
        Log.d(DEBUG_TAG, "onPause");
//        if (galleryDialog != null)
//            galleryDialog.dismiss();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SettingsManager settingsManager = SettingsManager.instance();
        if (settingsManager.isPasswordAutoClearEnabled()) {
            if (settingsManager.isExceptionEncryptedRepo()) {
                List<String> exceptionIds = Lists.newArrayList();
                if (settingsManager.isFolderAutomaticBackup()) {
                    RepoConfig repoConfig = FolderBackupDBHelper.getDatabaseHelper().getRepoConfig(account.email);
                    if (repoConfig != null) {
                        if (!TextUtils.isEmpty(repoConfig.getRepoID())) {
                            exceptionIds.add(repoConfig.getRepoID());
                        }
                    }
                }
                CameraUploadManager cameraManager = new CameraUploadManager(getApplicationContext());
                if (cameraManager.isCameraUploadEnabled()) {
                    if (settingsManager.getCameraUploadRepoId() != null) {
                        if (!TextUtils.isEmpty(settingsManager.getCameraUploadRepoId())) {
                            exceptionIds.add(settingsManager.getCameraUploadRepoId());
                        }
                    }
                }
                Utils.clearPasswordSilently(exceptionIds);
            } else {
                Utils.clearPasswordSilently(Lists.newArrayList());
            }
        }
//        if (galleryDialog != null)
//            galleryDialog.show(getSupportFragmentManager(), BrowserActivity.GALLERY_DIALOG_FRAGMENT_TAG);
    }

    @Override
    public void onRestart() {

        super.onRestart();

        if (accountManager.getCurrentAccount() == null
                || !accountManager.getCurrentAccount().equals(this.account)
                || !accountManager.getCurrentAccount().getToken().equals(this.account.getToken())) {
            finishAndStartAccountsActivity();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(DEBUG_TAG, "onNewIntent");

        // if the user started the Seadroid app from the Launcher, keep the old Activity
        final String intentAction = intent.getAction();
        if (intent.hasCategory(Intent.CATEGORY_LAUNCHER)
                && intentAction != null
                && intentAction.equals(Intent.ACTION_MAIN)) {
            return;
        }

        Account selectedAccount = accountManager.getCurrentAccount();
        Log.d(DEBUG_TAG, "Current account: " + selectedAccount);
        if (selectedAccount == null
                || !account.equals(selectedAccount)
                || !account.getToken().equals(selectedAccount.getToken())) {
            Log.d(DEBUG_TAG, "Account switched, restarting activity.");
            finish();
            startActivity(intent);
        }

        processIntent(intent);
    }

    public void callOnNewIntent() {
        onNewIntent(getIntent());
    }

    @Override
    protected void onStop() {
        Log.d(DEBUG_TAG, "onStop");
        super.onStop();
        EventBus.getDefault().unregister(this);

        if (mTransferReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mTransferReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(DEBUG_TAG, "onDestroy is called");

        if (txService != null) {
            unbindService(mConnection);
            txService = null;
        }
        if (keyboardListenersAttached) {
            mContainer.getViewTreeObserver().removeGlobalOnLayoutListener(keyboardLayoutListener);
        }
        if (mFolderBackupService != null) {
            unbindService(folderBackupConnection);
            mFolderBackupService = null;
        }
        if (progressDialog != null)
            progressDialog.dismiss();
        if (ocrProgressDialog != null)
            ocrProgressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(DEBUG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if (navContext.getRepoID() != null) {
            outState.putString("repoID", navContext.getRepoID());
            outState.putString("repoName", navContext.getRepoName());
            outState.putString("path", navContext.getDirPath());
            outState.putString("dirID", navContext.getDirID());
            outState.putString("permission", navContext.getDirPermission());
        }
        if (serverInfo != null)
            outState.putString("serverInfo", ServerInfo.getString(serverInfo));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // We can't show the CopyMoveDialog in onActivityResult, this is a
        // workaround found in
        // http://stackoverflow.com/questions/16265733/failure-delivering-result-onactivityforresult/18345899#18345899
        if (copyMoveIntent != null) {
            String dstRepoId, dstDir;
            dstRepoId = copyMoveIntent.getStringExtra(SeafilePathChooserActivity.DATA_REPO_ID);
            dstDir = copyMoveIntent.getStringExtra(SeafilePathChooserActivity.DATA_DIR);
            copyMoveContext.setDest(dstRepoId, dstDir);
            doCopyMove();
            copyMoveIntent = null;
        }
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
//            onKeyboardVisibilityChanged(true);
//        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
//            onKeyboardVisibilityChanged(false);
//        }
//        super.onConfigurationChanged(newConfig);
//    }

    /**
     * grid files by type
     *
     * @param type
     */
    private void changeView(int type) {
        SettingsManager.instance().saveGridFilesPref(type);
        updateGridMenuIcon();
        if (currentPosition == INDEX_LIBRARY_TAB) {
            if (navContext.inRepo()) {
                SeafRepo repo = dataManager.getCachedRepoByID(navContext.getRepoID());
                if (repo.encrypted && !dataManager.getRepoPasswordSet(repo.id)) {
                    String password = dataManager.getRepoPassword(repo.id);
                    showPasswordDialog(repo.name, repo.id,
                            new TaskDialog.TaskDialogListener() {
                                @Override
                                public void onTaskSuccess() {
                                    getReposFragment().gridFiles(type);
                                }
                            }, password);
                }
            }
        }
        getReposFragment().gridFiles(type);
        getStarredFragment().gridFiles(type);
    }

    /**
     * Sort files by type and order
     *
     * @param type
     */
    private void sortFiles(final int type, final int order) {
        if (navContext.inRepo()) {
            SeafRepo repo = dataManager.getCachedRepoByID(navContext.getRepoID());
            if (repo.encrypted && !dataManager.getRepoPasswordSet(repo.id)) {
                String password = dataManager.getRepoPassword(repo.id);
                showPasswordDialog(repo.name, repo.id,
                        new TaskDialog.TaskDialogListener() {
                            @Override
                            public void onTaskSuccess() {
                                getReposFragment().sortFiles(type, order);
                            }
                        }, password);
            }
        }
        getReposFragment().sortFiles(type, order);
        getStarredFragment().sortFiles(type, order);
    }

    /**
     * create a new repo
     */
    private void showNewRepoDialog() {
        final NewRepoDialog dialog = new NewRepoDialog();
        dialog.init(account);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                Utils.utilsEventsLogInfo(
                        getResources().getString(R.string.create) +
                                ", " +
                                dialog.getRepoName());
                showShortToast(
                        BrowserActivity.this,
                        String.format(getResources().getString(R.string.create_new_repo_success), dialog.getRepoName())
                );
                ReposFragment reposFragment = getReposFragment();
                if (currentPosition == INDEX_LIBRARY_TAB && reposFragment != null) {
                    reposFragment.refreshView(true, true);
                }
            }
        });
        dialog.show(getSupportFragmentManager(), TAG_NEW_REPO_DIALOG_FRAGMENT);
    }

    /**
     * add new file/files
     */
    private void addFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.dialog_add_file, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView titleText = view.findViewById(R.id.title_text);
        CardView createFileCard = view.findViewById(R.id.create_file_card);
        CardView createFolderCard = view.findViewById(R.id.create_folder_card);
        CardView uploadFileCard = view.findViewById(R.id.upload_file_card);
        CardView takePhotoCard = view.findViewById(R.id.take_photo_card);
        CardView documentScannerCard = view.findViewById(R.id.document_scanner_card);

        titleText.setText(getString(R.string.add_file));
        createFileCard.setOnClickListener(v -> {
            dialog.dismiss();
//            showNewFileDialog();
            showNewFileFormatDialog();
        });
        createFolderCard.setOnClickListener(v -> {
            dialog.dismiss();
            showNewDirDialog();
        });
        uploadFileCard.setOnClickListener(v -> {
            dialog.dismiss();
            XXPermissions.with(this).permission("android.permission.MANAGE_EXTERNAL_STORAGE").request(new OnPermissionCallback() {

                @Override
                public void onGranted(List<String> permissions, boolean all) {
                    if (all) {
                        pickFile();;
                    }
                }

                @Override
                public void onDenied(List<String> permissions, boolean never) {
                    Context context = BrowserActivity.this;
                    if (never) {
                        Toast.makeText(context, context.getString(R.string.authorization_storage_permission), Toast.LENGTH_LONG).show();
                        XXPermissions.startPermissionActivity(context, permissions);
                    } else {
                        Toast.makeText(context, context.getString(R.string.get_storage_permission_failed), Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
        takePhotoCard.setOnClickListener(v -> {
            dialog.dismiss();
            cameraTakePhoto();
        });

        documentScannerCard.setOnClickListener(v -> {
            dialog.dismiss();
            showDocumentScanner();
        });

        dialog.show();
    }

    private void showDocumentScanner() {
        GmsDocumentScannerOptions.Builder options =
                new GmsDocumentScannerOptions.Builder()
                        .setResultFormats(
                                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                                GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                        .setGalleryImportAllowed(true)
                        //.setPageLimit(1)
                        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL);


        GmsDocumentScanning.getClient(options.build())
                .getStartScanIntent(this)
                .addOnSuccessListener(
                        intentSender ->
                                scannerLauncher.launch(new IntentSenderRequest.Builder(intentSender).build()))
                .addOnFailureListener(
                        e -> showShortToast(this, getString(R.string.error_scan_failed, e.getMessage())));
    }

    private void handleActivityResult(ActivityResult activityResult) {
        int resultCode = activityResult.getResultCode();
        GmsDocumentScanningResult result =
                GmsDocumentScanningResult.fromActivityResultIntent(activityResult.getData());
        if (resultCode == Activity.RESULT_OK && result != null) {
            if (result.getPages() != null) {
                if (!result.getPages().isEmpty()) {
                    String fileName = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(new Date()) + ".pdf";
                    cropRepoID = navContext.getRepoID();
                    cropRepoName = navContext.getRepoName();
                    cropPath = navContext.getDirPath();
                    cropFileName = fileName;
                    for (int i = 0; i < result.getPages().size(); i++) {
                        cropFilePaths.add(result.getPages().get(i).getImageUri().getPath());
                    }

                    checkUseOcrDialog();
                    return;
//                    Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath());
//                    try {
//                        File tempDir = DataManager.createTempDir();
//                        String tempFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".png";
//                        File tempFile = new File(tempDir, tempFileName);
//
//                        FileOutputStream out = new FileOutputStream(tempFile);
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); //100-best quality
//                        out.close();
//
//                        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".pdf";
//
//                        cropRepoID = navContext.getRepoID();
//                        cropRepoName = navContext.getRepoName();
//                        cropPath = navContext.getDirPath();
//                        cropFileName = fileName;
//                        cropFilePath = tempFile.getPath();
//
//                        checkUseOcrDialog();
//                        return;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            }
            showShortToast(this, getString(R.string.error_scan_failed));
//            for (GmsDocumentScanningResult.Page page : result.getPages()) {
//                Uri imageUri = page.getImageUri();
//            }
//            if (result.getPdf() != null) {
//                File file = new File(result.getPdf().getUri().getPath());
//                String newFilePath = file.getParent() + "/" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".pdf";
//                File newFile = new File(newFilePath);
//                file.renameTo(newFile);
//                List<Uri> uriList = Collections.singletonList(Uri.fromFile(newFile));
//                uploadFilesFromLocal(uriList, navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath());
//            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            showShortToast(this, getString(R.string.error_scanner_cancelled));
        } else {
            showShortToast(this, getString(R.string.error_scan_failed));
        }
    }

    private void showNewDirDialog() {
        if (!hasRepoWritePermission()) {
            showShortToast(this, R.string.library_read_only);
            return;
        }

        final NewDirDialog dialog = new NewDirDialog();
        dialog.init(navContext.getRepoID(), navContext.getDirPath(), account, null);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                Utils.utilsEventsLogInfo(
                        getResources().getString(R.string.create) +
                                ", " +
                                dialog.getNewDirName());
                final String message = String.format(getString(R.string.create_new_folder_success), dialog.getNewDirName());
                showShortToast(BrowserActivity.this, message);
                ReposFragment reposFragment = getReposFragment();
                if (currentPosition == INDEX_LIBRARY_TAB && reposFragment != null) {
                    reposFragment.refreshView();
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "NewDirDialogFragment");
    }

    private void showNewFileFormatDialog() {
        if (!hasRepoWritePermission()) {
            showShortToast(this, R.string.library_read_only);
            return;
        }

        final NewFileFormatDialog dialog = new NewFileFormatDialog(this);
        dialog.show(getSupportFragmentManager(), "NewFileTypeDialogFragment");
    }

    public void showNewFileDialog(String extension) {
        if (!hasRepoWritePermission()) {
            showShortToast(this, R.string.library_read_only);
            return;
        }

        final NewFileDialog dialog = new NewFileDialog();
        dialog.init(navContext.getRepoID(), navContext.getDirPath(), account, extension);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                if (Utils.isViewableImage(dialog.getNewFileName())) {
                    if (dataManager.getCachedRepoByID(navContext.getRepoID()).canLocalDecrypt()) {
                        SettingsFragment settingsFragment = getSettingsFragment();
                        if (settingsFragment != null) {
                            settingsFragment.updateThumbImagesCount(
                                    settingsFragment.allEncThumbsCount,
                                    settingsFragment.allEncImagesCount + 1);
                        }
                    }
                }

                Utils.utilsEventsLogInfo(
                        getResources().getString(R.string.create) +
                                ", " +
                                dialog.getNewFileName());
                final String message = String.format(getString(R.string.create_new_file_success), dialog.getNewFileName());
                showShortToast(BrowserActivity.this, message);
                ReposFragment reposFragment = getReposFragment();
                if (currentPosition == INDEX_LIBRARY_TAB && reposFragment != null) {
                    reposFragment.refresh();
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "NewFileDialogFragment");
    }

    public void setRefreshing() {
        setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
    }

    public void unsetRefreshing() {
        setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
    }

    private File takeCameraPhotoTempFile;

    private void cameraTakePhoto() {
        Intent imageCaptureIntent = new Intent("android.media.action.IMAGE_CAPTURE");

        try {
            File ImgDir = DataManager.createTempDir();

            String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";
            takeCameraPhotoTempFile = new File(ImgDir, fileName);

            Uri photo = null;
            if (android.os.Build.VERSION.SDK_INT > 23) {
                photo = FileProvider.getUriForFile(this, getApplicationContext().getPackageName(), takeCameraPhotoTempFile);
            } else {
                photo = Uri.fromFile(takeCameraPhotoTempFile);
            }
            imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photo);
            startActivityForResult(imageCaptureIntent, TAKE_PHOTO_REQUEST);

        } catch (IOException e) {
            showShortToast(BrowserActivity.this, R.string.unknow_error);
        }
    }

    public void enableUpButton() {
        backMenuCard.setVisibility(View.VISIBLE);
    }

    public void disableUpButton() {
        backMenuCard.setVisibility(View.GONE);
    }

    public void setToolbarTitle(String title){
        if (title.equals(getResources().getString(R.string.libraries))) {
            toolbarTitleLibrary.setText(title);
            toolbarTitleNotLibrary.setText("");
        } else {
            toolbarTitleLibrary.setText("");
            title = title.replace("/", " / ");
            toolbarTitleNotLibrary.setText(title);
        }
    }

    /**
     * update up button title when sliding among tabs
     *
     * @param position
     */
    private void setToolbarTitleOnSlideTabs(int position) {
        if (navContext == null)
            return;

        switch (position) {
            case INDEX_LIBRARY_TAB:
                if (isSearchMode) {
                    setToolbarTitle(getResources().getString(R.string.search));
                } else {
                    if (navContext.inRepo()) {
                        if (navContext.getDirPath().equals(BrowserActivity.ACTIONBAR_PARENT_PATH)) {
                            setToolbarTitle(navContext.getRepoName());
                        } else {
                            setToolbarTitle(navContext.getDirPath().substring(
                                    navContext.getDirPath().lastIndexOf(BrowserActivity.ACTIONBAR_PARENT_PATH) + 1));
                        }
                    } else {
                        setToolbarTitle(getResources().getString(R.string.libraries));
                    }
                }
                break;
            case INDEX_STARRED_TAB:
                setToolbarTitle(getResources().getString(R.string.favorites));
                break;
            case INDEX_ACTIVITIES_TAB:
                setToolbarTitle(getResources().getString(R.string.activities));
                break;
            case INDEX_TRANSFER_TAB:
                setToolbarTitle(getResources().getString(R.string.transfer_tasks));
                break;
            case INDEX_ACCOUNTS_TAB:
                setToolbarTitle(getResources().getString(R.string.accounts));
                break;
            case INDEX_SETTINGS_TAB:
                setToolbarTitle(getResources().getString(R.string.settings));
                break;
            default:
                break;
        }
    }

    /**
     * refresh view when sliding among tabs
     *
     * @param position
     */
    private void refreshViewOnSlideTabs(int position) {
        if (navContext == null)
            return;

        if (position == INDEX_LIBRARY_TAB) {
            if (navContext.inRepo()) {
                getReposFragment().refreshView();
            }
        }

    }



    public boolean hasRepoWritePermission() {
        if (navContext == null) {
            return false;
        }
        if (navContext.getDirPermission() == null || navContext.getDirPermission().indexOf('w') == -1) {
            return false;
        }
        return true;
    }

    void pickFile() {
        if (!hasRepoWritePermission()) {
            showShortToast(this, R.string.library_read_only);
            return;
        }

        Intent target = Utils.createGetContentIntent();
        Intent intent = Intent.createChooser(target, getString(R.string.choose_file));
        startActivityForResult(intent, BrowserActivity.PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_FILES_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    if (!data.hasExtra(MULTI_FILES_PATHS)) {
                        return;
                    }
                    String[] paths = data.getStringArrayExtra(MULTI_FILES_PATHS);
                    if (paths == null)
                        return;
                    showShortToast(this, getString(R.string.added_to_upload_tasks));

                    List<SeafDirent> list = dataManager.getCachedDirents(navContext.getRepoID(), navContext.getDirPath());
                    if (list == null) return;

                    for (String path : paths) {
                        boolean duplicate = false;
                        for (SeafDirent dirent : list) {
                            if (dirent.name.equals(Utils.fileNameFromPath(path))) {
                                duplicate = true;
                                break;
                            }
                        }
                        if (!duplicate) {
                            showShortToast(BrowserActivity.this, getString(R.string.added_to_upload_tasks));
                            final SeafRepo repo = dataManager.getCachedRepoByID(navContext.getRepoID());
                            if (repo != null && repo.canLocalDecrypt()) {
                                addUploadBlocksTask(navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath(), path);
                            } else {
                                addUploadTask(navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath(), path);
                            }
                        } else {
                            showFileExistDialog(path, navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath());
                        }
                    }
                }
                break;
            case PICK_PHOTOS_VIDEOS_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> paths = data.getStringArrayListExtra("photos");
                    if (paths == null)
                        return;
                    showShortToast(this, getString(R.string.added_to_upload_tasks));

                    List<SeafDirent> list = dataManager.getCachedDirents(navContext.getRepoID(), navContext.getDirPath());
                    if (list == null) return;

                    for (String path : paths) {
                        boolean duplicate = false;
                        for (SeafDirent dirent : list) {
                            if (dirent.name.equals(Utils.fileNameFromPath(path))) {
                                duplicate = true;
                                break;
                            }
                        }
                        if (!duplicate) {
                            showShortToast(BrowserActivity.this, getString(R.string.added_to_upload_tasks));
                            final SeafRepo repo = dataManager.getCachedRepoByID(navContext.getRepoID());
                            if (repo != null && repo.canLocalDecrypt()) {
                                addUploadBlocksTask(navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath(), path);
                            }else {
                                addUploadTask(navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath(), path);
                            }
                        } else {
                            showFileExistDialog(path, navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath());
                        }
                    }
                }
                break;
            case PICK_FILE_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (!Utils.isNetworkOn()) {
                        showShortToast(this, R.string.network_down);
                        return;
                    }

                    List<Uri> uriList = UtilsJellyBean.extractUriListFromIntent(data);
                    uploadFilesFromLocal(uriList, navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath());
                }
                break;
            case CHOOSE_COPY_MOVE_DEST_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    if (!Utils.isNetworkOn()) {
                        showShortToast(this, R.string.network_down);
                        return;
                    }

                    copyMoveIntent = data;
                }
                isRepoCreated = false;
                if (data != null && data.hasExtra(SeafilePathChooserActivity.IS_REPO_CREATED_ON_PATH_CHOOSER))
                    isRepoCreated = data.getBooleanExtra(SeafilePathChooserActivity.IS_REPO_CREATED_ON_PATH_CHOOSER, false);
                break;
            case TAKE_PHOTO_REQUEST:
                if (resultCode == RESULT_OK) {
                    showShortToast(this, getString(R.string.take_photo_successfully));
                    if (!Utils.isNetworkOn()) {
                        showShortToast(this, R.string.network_down);
                        return;
                    }

                    if(takeCameraPhotoTempFile == null) {
                        showShortToast(this, getString(R.string.saf_upload_path_not_available));
                        Log.i(DEBUG_TAG, "Pick file request did not return a path");
                        return;
                    }
                    showShortToast(this, getString(R.string.added_to_upload_tasks));
                    final SeafRepo repo = dataManager.getCachedRepoByID(navContext.getRepoID());
                    if (repo != null && repo.canLocalDecrypt()) {
                        addUploadBlocksTask(navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath(), takeCameraPhotoTempFile.getAbsolutePath());
                    } else {
                        addUploadTask(navContext.getRepoID(), navContext.getRepoName(), navContext.getDirPath(), takeCameraPhotoTempFile.getAbsolutePath());
                    }
                }
                break;
            case DOWNLOAD_FILE_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    String path = data.getStringExtra("path");
                    if (path != null) {
                        File file = new File(path);
                        int openOfficeValue = data.getIntExtra("open_office", SettingsManager.OPEN_OFFICE_ONLY_OFFICE);
                        boolean isOpenWith = data.getBooleanExtra("is_open_with", false);
                        WidgetUtils.showFile(BrowserActivity.this, file, openOfficeValue, isOpenWith);
                    }
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    handleCropResult(data);
                } else {
                    handleCropError(data);
                }
                break;
            case DOCUMENT_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    String path = data.getStringExtra("path");
                    if (path != null) {
                        try {
                            File srcFile = new File(path);
                            File tempDir = DataManager.createTempDir();
                            File tempFile = new File(tempDir, srcFile.getName());
                            Utils.moveFile(srcFile, tempFile);
                            srcFile.delete();

                            String repoID = navContext.getRepoID();
                            String repoName = navContext.getRepoName();
                            String dirPath = navContext.getDirPath();

                            SeafRepo repo = dataManager.getCachedRepoByID(repoID);
                            if (repo != null && repo.canLocalDecrypt()) {
                                addUpdateBlocksTask(repoID, repoName, dirPath, tempFile.getPath());
                            } else {
                                addUpdateTask(repoID, repoName, dirPath, tempFile.getPath());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    public String getPath(Uri uri) {

        String path = null;
        String[] projection = { MediaStore.Files.FileColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if(cursor == null){
            path = uri.getPath();
        }
        else{
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

    public void uploadFilesFromLocal(List<Uri> uriList, String repoID, String repoName, String targetDir) {
        if (uriList.size() > 0) {
            ConcurrentAsyncTask.execute(new SAFLoadRemoteFileTask(uriList, repoID, repoName, targetDir));
        } else {
            showShortToast(BrowserActivity.this, R.string.saf_upload_path_not_available);
        }
    }

    private class SAFLoadRemoteFileTask extends AsyncTask<Void, Void, Void> {
        private List<Uri> uriList;
        private String repoName;
        private String repoID;
        private String targetDir;
        private List<File> fileList;

        public SAFLoadRemoteFileTask(List<Uri> uriList, String repoID, String repoName, String targetDir) {
            this.uriList = uriList;
            this.repoID = repoID;
            this.repoName = repoName;
            this.targetDir = targetDir;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (uriList == null)
                return null;

            fileList = new ArrayList<File>();
            for (Uri uri : uriList) {
                // Log.d(DEBUG_TAG, "Uploading file from uri: " + uri);
                InputStream in = null;
                OutputStream out = null;

                try {
                    File tempDir = DataManager.createTempDir();
                    File tempFile = new File(tempDir, Utils.getFilenamefromUri(BrowserActivity.this, uri));

                    if (!tempFile.createNewFile()) {
                        throw new RuntimeException("could not create temporary file");
                    }

                    in = getContentResolver().openInputStream(uri);
                    out = new FileOutputStream(tempFile);
                    IOUtils.copy(in, out);

                    if (Utils.isViewableImage(tempFile.getName())) {
                        ParcelFileDescriptor inFd = null;
                        ParcelFileDescriptor outFd = null;
                        try {

                            String originalPath = FileHelper.Companion.getRealPathFromURI(BrowserActivity.this, uri);
                            if (originalPath != null) {
                                File originalFile = new File(originalPath);
                                if (originalFile.exists()) {
                                    inFd = getContentResolver().openFileDescriptor(Uri.fromFile(originalFile), "r");
                                }
                            }

                            if (inFd == null) {
                                inFd = getContentResolver().openFileDescriptor(uri, "r");
                            }
                            ExifInterface originalExif = new ExifInterface(inFd.getFileDescriptor());
                            outFd = getContentResolver().openFileDescriptor(Uri.fromFile(tempFile), "rw");
                            ExifInterface newExif = new ExifInterface(outFd.getFileDescriptor());

                            copyExif(originalExif, newExif);
                        } catch (IOException | RuntimeException e) {
                            Log.d(DEBUG_TAG, "Could not open ExifInterface of requested document", e);
                        } finally {
                            IOUtils.closeQuietly(inFd);
                            IOUtils.closeQuietly(outFd);
                        }
                    }

                    fileList.add(tempFile);
                } catch (IOException | RuntimeException e) {
                    Log.d(DEBUG_TAG, "Could not open requested document", e);
                } finally {
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (fileList == null) return;

            List<SeafDirent> list = dataManager.getCachedDirents(repoID, targetDir);

            for (final File file : fileList) {
                if (file == null) {
                    showShortToast(BrowserActivity.this, R.string.saf_upload_path_not_available);
                } else {
                    if (list == null) {
                        Log.e(DEBUG_TAG, "Seadroid dirent cache is empty in uploadFile. Should not happen, aborting.");
                        return;
                    }

                    boolean duplicate = false;
                    for (SeafDirent dirent : list) {
                        if (dirent.name.equals(file.getName())) {
                            duplicate = true;
                            break;
                        }
                    }

                    if (!duplicate) {
                        final SeafRepo repo = dataManager.getCachedRepoByID(repoID);
                        showShortToast(BrowserActivity.this, getString(R.string.added_to_upload_tasks));
                        if (repo != null && repo.canLocalDecrypt()) {
                            addUploadBlocksTask(repoID, repoName, targetDir, file.getAbsolutePath());
                        } else {
                            addUploadTask(repoID, repoName, targetDir, file.getAbsolutePath());
                        }
                    } else {
                        showFileExistDialog(file, repoID, repoName, targetDir);
                    }
                }
            }

            if (txService == null)
                return;

            if (!txService.hasUploadNotifProvider()) {
                UploadNotificationProvider provider = new UploadNotificationProvider(
                        txService.getUploadTaskManager(),
                        txService);
                txService.saveUploadNotifProvider(provider);
            }
        }
    }

    public static void copyExif(ExifInterface oldExif, ExifInterface newExif) throws IOException {

        String[] attributes = new String[] {
                ExifInterface.TAG_IMAGE_WIDTH,
                ExifInterface.TAG_IMAGE_LENGTH,
                ExifInterface.TAG_BITS_PER_SAMPLE,
                ExifInterface.TAG_COMPRESSION,
                ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION,
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.TAG_SAMPLES_PER_PIXEL,
                ExifInterface.TAG_PLANAR_CONFIGURATION,
                ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING,
                ExifInterface.TAG_Y_CB_CR_POSITIONING,
                ExifInterface.TAG_X_RESOLUTION,
                ExifInterface.TAG_Y_RESOLUTION,
                ExifInterface.TAG_RESOLUTION_UNIT,
                ExifInterface.TAG_STRIP_OFFSETS,
                ExifInterface.TAG_ROWS_PER_STRIP,
                ExifInterface.TAG_STRIP_BYTE_COUNTS,
                ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT,
                ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH,
                ExifInterface.TAG_TRANSFER_FUNCTION,
                ExifInterface.TAG_WHITE_POINT,
                ExifInterface.TAG_PRIMARY_CHROMATICITIES,
                ExifInterface.TAG_Y_CB_CR_COEFFICIENTS,
                ExifInterface.TAG_REFERENCE_BLACK_WHITE,
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_IMAGE_DESCRIPTION,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_SOFTWARE,
                ExifInterface.TAG_ARTIST,
                ExifInterface.TAG_COPYRIGHT,
                ExifInterface.TAG_EXIF_VERSION,
                ExifInterface.TAG_FLASHPIX_VERSION,
                ExifInterface.TAG_COLOR_SPACE,
                ExifInterface.TAG_GAMMA,
                ExifInterface.TAG_PIXEL_X_DIMENSION,
                ExifInterface.TAG_PIXEL_Y_DIMENSION,
                ExifInterface.TAG_COMPONENTS_CONFIGURATION,
                ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL,
                ExifInterface.TAG_MAKER_NOTE,
                ExifInterface.TAG_USER_COMMENT,
                ExifInterface.TAG_RELATED_SOUND_FILE,
                ExifInterface.TAG_DATETIME_ORIGINAL,
                ExifInterface.TAG_DATETIME_DIGITIZED,
                ExifInterface.TAG_OFFSET_TIME,
                ExifInterface.TAG_OFFSET_TIME_ORIGINAL,
                ExifInterface.TAG_OFFSET_TIME_DIGITIZED,
                ExifInterface.TAG_SUBSEC_TIME,
                ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
                ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
                ExifInterface.TAG_EXPOSURE_TIME,
                ExifInterface.TAG_F_NUMBER,
                ExifInterface.TAG_EXPOSURE_PROGRAM,
                ExifInterface.TAG_SPECTRAL_SENSITIVITY,
//                ExifInterface.TAG_ISO_SPEED_RATINGS,
                ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
                ExifInterface.TAG_OECF,
                ExifInterface.TAG_SENSITIVITY_TYPE,
                ExifInterface.TAG_STANDARD_OUTPUT_SENSITIVITY,
                ExifInterface.TAG_RECOMMENDED_EXPOSURE_INDEX,
                ExifInterface.TAG_ISO_SPEED,
                ExifInterface.TAG_ISO_SPEED_LATITUDE_YYY,
                ExifInterface.TAG_ISO_SPEED_LATITUDE_ZZZ,
                ExifInterface.TAG_SHUTTER_SPEED_VALUE,
                ExifInterface.TAG_APERTURE_VALUE,
                ExifInterface.TAG_BRIGHTNESS_VALUE,
                ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
                ExifInterface.TAG_MAX_APERTURE_VALUE,
                ExifInterface.TAG_SUBJECT_DISTANCE,
                ExifInterface.TAG_METERING_MODE,
                ExifInterface.TAG_LIGHT_SOURCE,
                ExifInterface.TAG_FLASH,
                ExifInterface.TAG_SUBJECT_AREA,
                ExifInterface.TAG_FOCAL_LENGTH,
                ExifInterface.TAG_FLASH_ENERGY,
                ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE,
                ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION,
                ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION,
                ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT,
                ExifInterface.TAG_SUBJECT_LOCATION,
                ExifInterface.TAG_EXPOSURE_INDEX,
                ExifInterface.TAG_SENSING_METHOD,
                ExifInterface.TAG_FILE_SOURCE,
                ExifInterface.TAG_SCENE_TYPE,
                ExifInterface.TAG_CFA_PATTERN,
                ExifInterface.TAG_CUSTOM_RENDERED,
                ExifInterface.TAG_EXPOSURE_MODE,
                ExifInterface.TAG_WHITE_BALANCE,
                ExifInterface.TAG_DIGITAL_ZOOM_RATIO,
                ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
                ExifInterface.TAG_SCENE_CAPTURE_TYPE,
                ExifInterface.TAG_GAIN_CONTROL,
                ExifInterface.TAG_CONTRAST,
                ExifInterface.TAG_SATURATION,
                ExifInterface.TAG_SHARPNESS,
                ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION,
                ExifInterface.TAG_SUBJECT_DISTANCE_RANGE,
                ExifInterface.TAG_IMAGE_UNIQUE_ID,
//                ExifInterface.TAG_CAMARA_OWNER_NAME,
                ExifInterface.TAG_CAMERA_OWNER_NAME,
                ExifInterface.TAG_BODY_SERIAL_NUMBER,
                ExifInterface.TAG_LENS_SPECIFICATION,
                ExifInterface.TAG_LENS_MAKE,
                ExifInterface.TAG_LENS_MODEL,
                ExifInterface.TAG_LENS_SERIAL_NUMBER,
                ExifInterface.TAG_GPS_VERSION_ID,
                ExifInterface.TAG_GPS_LATITUDE_REF,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LONGITUDE_REF,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_ALTITUDE_REF,
                ExifInterface.TAG_GPS_ALTITUDE,
                ExifInterface.TAG_GPS_TIMESTAMP,
                ExifInterface.TAG_GPS_SATELLITES,
                ExifInterface.TAG_GPS_STATUS,
                ExifInterface.TAG_GPS_MEASURE_MODE,
                ExifInterface.TAG_GPS_DOP,
                ExifInterface.TAG_GPS_SPEED_REF,
                ExifInterface.TAG_GPS_SPEED,
                ExifInterface.TAG_GPS_TRACK_REF,
                ExifInterface.TAG_GPS_TRACK,
                ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
                ExifInterface.TAG_GPS_IMG_DIRECTION,
                ExifInterface.TAG_GPS_MAP_DATUM,
                ExifInterface.TAG_GPS_DEST_LATITUDE_REF,
                ExifInterface.TAG_GPS_DEST_LATITUDE,
                ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,
                ExifInterface.TAG_GPS_DEST_LONGITUDE,
                ExifInterface.TAG_GPS_DEST_BEARING_REF,
                ExifInterface.TAG_GPS_DEST_BEARING,
                ExifInterface.TAG_GPS_DEST_DISTANCE_REF,
                ExifInterface.TAG_GPS_DEST_DISTANCE,
                ExifInterface.TAG_GPS_PROCESSING_METHOD,
                ExifInterface.TAG_GPS_AREA_INFORMATION,
                ExifInterface.TAG_GPS_DATESTAMP,
                ExifInterface.TAG_GPS_DIFFERENTIAL,
                ExifInterface.TAG_GPS_H_POSITIONING_ERROR,
                ExifInterface.TAG_INTEROPERABILITY_INDEX,
                ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH,
                ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH,
//                ExifInterface.TAG_THUMBNAIL_ORIENTATION,
                ExifInterface.TAG_DNG_VERSION,
                ExifInterface.TAG_DEFAULT_CROP_SIZE,
                ExifInterface.TAG_ORF_THUMBNAIL_IMAGE,
                ExifInterface.TAG_ORF_PREVIEW_IMAGE_START,
                ExifInterface.TAG_ORF_PREVIEW_IMAGE_LENGTH,
                ExifInterface.TAG_ORF_ASPECT_FRAME,
                ExifInterface.TAG_RW2_SENSOR_BOTTOM_BORDER,
                ExifInterface.TAG_RW2_SENSOR_LEFT_BORDER,
                ExifInterface.TAG_RW2_SENSOR_RIGHT_BORDER,
                ExifInterface.TAG_RW2_SENSOR_TOP_BORDER,
                ExifInterface.TAG_RW2_ISO,
                ExifInterface.TAG_RW2_JPG_FROM_RAW,
                ExifInterface.TAG_XMP,
                ExifInterface.TAG_NEW_SUBFILE_TYPE,
                ExifInterface.TAG_SUBFILE_TYPE,
//                ExifInterface.TAG_EXIF_IFD_POINTER,
//                ExifInterface.TAG_GPS_INFO_IFD_POINTER,
//                ExifInterface.TAG_INTEROPERABILITY_IFD_POINTER,
//                ExifInterface.TAG_SUB_IFD_POINTER,
//                ExifInterface.TAG_ORF_CAMERA_SETTINGS_IFD_POINTER,
//                ExifInterface.TAG_ORF_IMAGE_PROCESSING_IFD_POINTER,
                };

        for (String attribute : attributes) {
            String value = oldExif.getAttribute(attribute);
            if (value != null) {
                newExif.setAttribute(attribute, value);
            }
        }
        newExif.saveAttributes();
    }

    private void showFileExistDialog(final String filePath, final String repoID, final String repoName, final String targetDir) {
        showFileExistDialog(new File(filePath), repoID, repoName, targetDir);
    }

    private void showFileExistDialog(final File file, final String repoID, final String repoName, final String targetDir) {
        final SeafRepo repo = dataManager.getCachedRepoByID(repoID);
        Dialog dialog = Utils.CustomDialog(this);
        dialog.setContentView(R.layout.dialog_file_exist);

        TextView messageText1 = (TextView) dialog.findViewById(R.id.message1);
        TextView messageText2 = (TextView) dialog.findViewById(R.id.message2);
        CardView replaceCardView = (CardView) dialog.findViewById(R.id.triple_first_card);
        TextView replaceTextView = (TextView) dialog.findViewById(R.id.triple_first_text);
        CardView cancelCardView = (CardView) dialog.findViewById(R.id.triple_second_card);
        TextView cancelTextView = (TextView) dialog.findViewById(R.id.triple_second_text);
        CardView keepBothCardView = (CardView) dialog.findViewById(R.id.triple_third_card);
        TextView keepBothTextView = (TextView) dialog.findViewById(R.id.triple_third_text);

        messageText1.setText(String.format(getString(R.string.upload_duplicate_found1), file.getName()));
        messageText2.setText(getString(R.string.upload_duplicate_found2));
        replaceTextView.setText(R.string.upload_replace);
        cancelTextView.setText(R.string.cancel);
        keepBothTextView.setText(R.string.upload_keep_both);

        replaceCardView.setOnClickListener(v -> {
            showShortToast(BrowserActivity.this, getString(R.string.added_to_upload_tasks));
            if (repo != null && repo.canLocalDecrypt()) {
                addUpdateBlocksTask(repoID, repoName, targetDir, file.getAbsolutePath());
            } else {
                addUpdateTask(repoID, repoName, targetDir, file.getAbsolutePath());
            }
            dialog.dismiss();
        });
        cancelCardView.setOnClickListener(v -> {
            dialog.dismiss();
        });
        keepBothCardView.setOnClickListener(v -> {
            if (repo != null && repo.canLocalDecrypt()) {
                addUploadBlocksTask(repoID, repoName, targetDir, file.getAbsolutePath());
            } else {
                addUploadTask(repoID, repoName, targetDir, file.getAbsolutePath());
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    public void onItemSelected() {
        // update contextual action bar (CAB) title
        if (currentPosition == INDEX_LIBRARY_TAB) {
            getReposFragment().updateContextualActionBar();
        }
        if (currentPosition == INDEX_STARRED_TAB) {
            getStarredFragmentWithoutInstantiate().updateContextualActionBar();
        }
        if (currentPosition == INDEX_TRANSFER_TAB) {
            getTransferFragment().updateContextualActionBar();
        }
    }

    /***************  Navigation *************/

    public void onFileSelected(SeafDirent dirent, boolean isOpenWith) {
        String fileName;
        long fileSize;
        String repoName;
        String repoID;
        String dirPath;
        String filePath;
        SeafRepo repo;

        if (dirent.isSearchedFile) {
            repoID = dirent.repoID;
            fileName = dirent.getTitle();
            fileSize = dirent.size;
            dirPath = Utils.pathSplit(dirent.path, fileName);
            filePath = dirent.path;
            repo = dataManager.getCachedRepoByID(repoID);
            repoName = repo.getName();
        } else {
            fileName= dirent.name;
            fileSize = dirent.size;
            repoName = navContext.getRepoName();
            repoID = navContext.getRepoID();
            dirPath = navContext.getDirPath();
            filePath = Utils.pathJoin(navContext.getDirPath(), fileName);
            repo = dataManager.getCachedRepoByID(repoID);
        }

        SelectedFileInfo fileInfo = new SelectedFileInfo(
                fileName,
                fileSize,
                dirent.mtime,
                repoName,
                repoID,
                dirPath,
                filePath
        );

        processSelectedFile(isOpenWith, fileInfo, getOpenValue(filePath));
    }

    private void processSelectedFile(boolean isOpenWith, SelectedFileInfo fileInfo, int openOfficeValue) {
        if (Utils.isOfficeMimeType(fileInfo.fileName) && !isOpenWith) {
            if (openOfficeValue == SettingsManager.OPEN_OFFICE_OPTIONAL) {
                showOpenOfficeDialog(false, fileInfo);
                return;
            }
            if (openOfficeValue == SettingsManager.OPEN_OFFICE_IN_APP_OFFICE) {
                startOfficeActivity(fileInfo);
                return;
            }
        }

        // Encrypted repo doesn\`t support gallery,
        // because pic thumbnail under encrypted repo was not supported at the server side
        SeafRepo repo = dataManager.getCachedRepoByID(fileInfo.repoID);
        if (Utils.isViewableImage(fileInfo.fileName) && repo != null) { // && !repo.encrypted
            startGalleryDialog(fileInfo.repoName, fileInfo.repoID, fileInfo.dirPath, fileInfo.fileName);
            return;
        }

        final File localFile = dataManager.getLocalCachedFile(fileInfo.repoName, fileInfo.repoID, fileInfo.filePath);
        if (localFile != null) {
            if (fileInfo.mtime * 1000 < localFile.lastModified()) {
                WidgetUtils.showFile(this, localFile, openOfficeValue, isOpenWith);
                return;
            } else {
                localFile.delete();
            }
        }
        boolean videoFile = Utils.isVideoFile(fileInfo.fileName);
        if (videoFile) { // is video file
            Dialog dialog = Utils.CustomDialog(this);
            dialog.setContentView(R.layout.dialog_video_download);

            TextView titleVideoText = dialog.findViewById(R.id.title_video_text);
            CardView playVideoCard = dialog.findViewById(R.id.play_video_card);
            CardView playVLCCard = dialog.findViewById(R.id.play_vlc_card);
            CardView downloadVideoCard = dialog.findViewById(R.id.download_video_card);

            titleVideoText.setText(fileInfo.fileName);
            playVideoCard.setOnClickListener(v -> {
                dialog.dismiss();
                startPlayActivity(fileInfo.fileName, fileInfo.repoID, fileInfo.filePath);
            });
            playVLCCard.setOnClickListener(v -> {
                dialog.dismiss();
                getReposFragment().mProgressContainer.setVisibility(View.VISIBLE);
                VideoLinkTask task = new VideoLinkTask(account, fileInfo.repoID, fileInfo.filePath, this);
                ConcurrentAsyncTask.execute(task);
            });

            downloadVideoCard.setOnClickListener(v -> {
                dialog.dismiss();
                startFileActivity(fileInfo, openOfficeValue, isOpenWith);
            });

            dialog.show();
            return;
        }
        startFileActivity(fileInfo, openOfficeValue, isOpenWith);
    }

    @Override
    public void onSuccess(String fileLink) {
        getReposFragment().mProgressContainer.setVisibility(View.GONE);
        final String vlcPackageName = "org.videolan.vlc";
        if (Utils.appInstalledOrNot(vlcPackageName)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage(vlcPackageName);
            intent.setDataAndType(Uri.parse(fileLink), "video/*");
            intent.setComponent(new ComponentName(vlcPackageName, vlcPackageName + ".gui.video.VideoPlayerActivity"));
            startActivity(intent);
        } else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + vlcPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + vlcPackageName)));
            }
        }
    }

    @Override
    public void onError(String errMsg) {
        getReposFragment().mProgressContainer.setVisibility(View.GONE);
        showShortToast(this, errMsg);
    }

    @Override
    public void onFileSelected(SeafDirent dirent) {
        onFileSelected(dirent, false);
    }

    public void checkConvertToPdf(String repoName, String repoID, String dir, String fileName, String path) {
        SeafCachedFile cf = getDataManager().getCachedFile(repoName, repoID, path);
        if (cf != null) {
            final File localFile = dataManager.getLocalCachedFile(repoName, repoID, path);
            if (localFile != null) {
                if (Utils.isViewableImage(fileName)) {
                    startCrop(repoID, repoName, dir, fileName, localFile);
                } else {
                    convertToPDF(repoID, repoName, dir, fileName,localFile);
                }
                return;
            }
        }

        downloadFile(repoID, dir, fileName, true);
        String st = repoID + dir + fileName;
        if (!pdfConvertList.contains(st)) {
            pdfConvertList.add(st);
        }
    }

    @Override
    public void loadingProgress(boolean showLoader) {    }

    @Override
    public void onCropFinish(UCropFragment.UCropResult result) {
        switch (result.mResultCode) {
            case RESULT_OK:
                handleCropResult(result.mResultData);
                break;
            case UCrop.RESULT_ERROR:
                handleCropError(result.mResultData);
                break;
        }
    }

    public void startCrop(String repoID, String repoName,String path, String fileName, File localFile) {
        String destinationFileName = Utils.removeExtensionFromFileName(fileName) + ".png";

        UCrop uCrop = UCrop.of(Uri.fromFile(localFile), Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop = uCropConfig(uCrop);

        cropRepoID = repoID;
        cropRepoName = repoName;
        cropPath = path;
        cropFileName = destinationFileName;

        uCrop.start(this);
    }

    private UCrop uCropConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();

        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(100);
        options.setRootViewBackgroundColor(Color.WHITE);
        options.setLogoColor(Color.WHITE);

        return uCrop.withOptions(options);
    }

    private void handleCropResult(@NonNull Intent result) {
        final Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null && resultUri.getPath() != null && cropRepoID != null) {
            cropFilePaths.add(resultUri.getPath());
            checkUseOcrDialog();
        } else {
            Toast.makeText(this, R.string.toast_cannot_retrieve_cropped_image, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCropError(@NonNull Intent result) {
        try {
            final Throwable cropError = UCrop.getError(result);
            if (cropError != null) {
                Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.unknow_error, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {}
    }

    private void convertToPDF(String repoID, String repoName, String path, String fileName, File localFile) {
        try {
            File pdfDir = DataManager.createTempDir();
            String pdfFileName = Utils.removeExtensionFromFileName(fileName) + ".pdf";
            File pdfFile = new File(pdfDir, pdfFileName);

            if (localFile.exists()) {
                BufferedReader reader;
                FileInputStream is = null;

                try {
                    getReposFragment().showLoading(true);
                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream(pdfFile.getPath()));

                    if (Utils.isViewableImage(fileName)) {
                        Image image = Image.getInstance(localFile.getPath());  // Change image's name and extension.

                        float imageMaxWidth = 612;
                        float imageMaxHeight = 792;
                        float pdfHSpace = 10;
                        float pdfVSpace = 20;

                        float imageWidth = image.getWidth();
                        float imageHeight = image.getHeight();


                        if (imageWidth == 0)
                            imageWidth = imageMaxWidth;
                        if (imageHeight == 0)
                            imageHeight = imageMaxHeight;

                        if (imageWidth > imageMaxWidth) {
                            imageHeight = imageHeight * imageMaxWidth / imageWidth;
                            imageWidth = imageMaxWidth;
                        }
                        if (imageHeight > imageMaxHeight) {
                            imageWidth = imageWidth * imageMaxHeight / imageHeight;
                            imageHeight = imageMaxHeight;
                        }
                        image.scaleAbsolute(imageWidth, imageHeight);

                        document.setPageSize(new Rectangle(imageWidth + pdfHSpace * 2, imageHeight + pdfVSpace * 2));
                        document.setMargins(pdfHSpace, pdfHSpace, pdfVSpace, pdfVSpace);

                        document.open();

                        Rectangle documentRect = document.getPageSize();
                        image.setAbsolutePosition(
                                (documentRect.getWidth() - image.getScaledWidth()) / 2,
                                (documentRect.getHeight() - image.getScaledHeight()) / 2);

                        document.add(image);
                    } else {
                        document.open();

                        is = new FileInputStream(localFile);
                        reader = new BufferedReader(new InputStreamReader(is));
                        String line = reader.readLine();
                        while(line != null){
                            Log.d("StackOverflow", line);
                            document.add(new Paragraph(line));
                            line = reader.readLine();
                        }
                    }

                    document.close();
                    getReposFragment().showLoading(false);

                    List<Uri> uriList = Collections.singletonList(Uri.fromFile(pdfFile));
                    uploadFilesFromLocal(uriList, repoID, repoName, path);
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                } catch (DocumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if (is != null) is.close();
                }
            }

        } catch (IOException e) {
            showShortToast(BrowserActivity.this, R.string.unknow_error);
        }
    }

    public void checkUseOcrDialog() {
        switch (SettingsManager.instance().getExecuteOcrValue()) {
            case SettingsManager.EXECUTE_OCR_ACTIVATED:
                prepareImagesOfPDF(true);
                break;
            case SettingsManager.EXECUTE_OCR_DEACTIVATED:
                prepareImagesOfPDF(false);
                break;
            default:
                showUseOcrDialog();
                break;
        }
    }

    private void showUseOcrDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.dialog_use_ocr, null);

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        CardView executeOcrCard = (CardView) view.findViewById(R.id.execute_ocr_card);
        TextView ocrSummaryText = (TextView) view.findViewById(R.id.execute_ocr_summary);
        CardView okCard = (CardView) view.findViewById(R.id.ok_card);
        TextView okText = (TextView) view.findViewById(R.id.ok_text);
        CardView cancelCard = (CardView) view.findViewById(R.id.cancel_card);
        TextView cancelText = (TextView) view.findViewById(R.id.cancel_text);

        okText.setText(R.string.yes);
        cancelText.setText(R.string.no);

        executeOcrCard.setOnClickListener(v -> {
            try {
                LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = mInflater.inflate(R.layout.popup_select_execute_ocr, null);

                layout.measure(View.MeasureSpec.UNSPECIFIED,
                        View.MeasureSpec.UNSPECIFIED);
                mPopupExecuteOcrView = findViewById(R.id.popup_select_execute_ocr_layout);
                mExecuteOcrDropDown = new PopupWindow(layout, mPopupExecuteOcrView.getMeasuredWidth(),
                        mPopupExecuteOcrView.getMeasuredHeight(),true);//FrameLayout.LayoutParams.WRAP_CONTENT

                final CardView askEverytimeCard = layout.findViewById(R.id.ask_everytime_card);
                final CardView activatedCard = layout.findViewById(R.id.activated_card);
                final CardView deactivatedCard = layout.findViewById(R.id.deactivated_card);

                askEverytimeCard.setOnClickListener(_view -> updateExecuteOcr(SettingsManager.EXECUTE_OCR_ASK_EVERYTIME, dialog));
                activatedCard.setOnClickListener(_view -> updateExecuteOcr(SettingsManager.EXECUTE_OCR_ACTIVATED, dialog));
                deactivatedCard.setOnClickListener(_view -> updateExecuteOcr(SettingsManager.EXECUTE_OCR_DEACTIVATED, dialog));

                mExecuteOcrDropDown.showAsDropDown(executeOcrCard, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        okCard.setOnClickListener(v -> {
            prepareImagesOfPDF(true);
            dialog.dismiss();
        });
        cancelCard.setOnClickListener(v -> {
            prepareImagesOfPDF(false);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateExecuteOcr(int executeOcrValue, AlertDialog dialog) {
        mExecuteOcrDropDown.dismiss();
        SettingsManager settingsMgr = SettingsManager.instance();
        if (executeOcrValue != settingsMgr.getExecuteOcrValue()) {
            getSettingsFragment().updateExecuteOcr(executeOcrValue);
            if (executeOcrValue != 0) {
                prepareImagesOfPDF(executeOcrValue == 1);
                dialog.dismiss();
            }
        }
    }

//    private class convertToPDFwithOCR extends AsyncTask<Void, Void, Void> {
//        private String repoID;
//        private String repoName;
//        private String path;
//        private String fileName;
//        private List<String> localFilePaths;
//        private boolean useOcr;
//        public convertToPDFwithOCR(String repoID, String repoName, String path, String fileName, List<String> localFilePaths, boolean useOcr) {
//            this.repoID = repoID;
//            this.repoName = repoName;
//            this.path = path;
//            this.fileName = fileName;
//            this.localFilePaths = localFilePaths;
//            this.useOcr = useOcr;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            progressDialog.show();
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            if (useOcr) {
//                File tessDir = new File(getFilesDir(), "tessdata");
//                if (!tessDir.exists()) {
//                    try {
//                        tessDir.mkdir();
//                    } catch (Exception e) {
//                        showShortToast(BrowserActivity.this, R.string.unknow_error);
//                        return null;
//                    }
//                }
//
//                AssetManager am = getAssets();
//                if (am != null) {
//                    try {
//                        for (String assetName : am.list("")) {
//                            File targetFile;
//                            if (assetName.endsWith(".traineddata")) {
//                                targetFile = new File(tessDir, assetName);
//                                if (!targetFile.exists()) {
//                                    copyTraineddata(am, assetName, targetFile);
//                                }
//                            }
//                        }
//                    } catch (IOException e) {
//                        showShortToast(BrowserActivity.this, R.string.unknow_error);
//                        return null;
//                    }
//                }
//            }
//
//            JSONArray pages = new JSONArray();
//            for (int i = 0; i < localFilePaths.size(); i++) {
//                File localFile = new File(localFilePaths.get(i));
//
//                JSONArray blocks = new JSONArray();
//                if (useOcr) {
//                    final int page = i + 1;
//                    final int count = localFilePaths.size();
//                    TessBaseAPI.ProgressNotifier progressNotifier = new TessBaseAPI.ProgressNotifier() {
//                        @Override
//                        public void onProgressValues(TessBaseAPI.ProgressValues progressValues) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    progressDialog.setMessage(getResources().getString(R.string.detecting_text,
//                                            page,
//                                            count,
//                                            progressValues.getPercent()));
//                                }
//                            });
//                        }
//                    };
//                    TessBaseAPI tess = new TessBaseAPI(progressNotifier);
//                    //TessBaseAPI tess = new TessBaseAPI();
//                    String dataPath = getFilesDir().getAbsolutePath();
//
//                    if (!tess.init(dataPath, "eng+deu")) {
//                        tess.recycle();
//                        showShortToast(BrowserActivity.this, R.string.unknow_error);
//                        return null;
//                    }
//                    tess.setImage(localFile);
//                    String result = tess.getHOCRText(0);
//                    //String result = tess.getUTF8Text();
//
//                    ResultIterator resultIterator = tess.getResultIterator();
//
//                    while (resultIterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD)) {
//                        Rect rect = resultIterator.getBoundingRect(TessBaseAPI.PageIteratorLevel.RIL_WORD);
//                        String text = resultIterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD);
//
//                        try {
//                            JSONObject block = new JSONObject();
//                            block.put("x", (double) rect.left);
//                            block.put("y", (double) rect.top);
//                            block.put("width", (double) rect.width());
//                            block.put("height", (double) rect.height());
//                            block.put("text", text);
//
//                            blocks.put(block);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    tess.recycle();
//                }
//
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeFile(localFile.getAbsolutePath(), options);
//                double imageHeight = options.outHeight;
//                double imageWidth = options.outWidth;
//
//                JSONObject jsonOps = new JSONObject();
//                try {
//                    jsonOps.put("imagePath", localFile.getAbsolutePath());
//                    jsonOps.put("width", imageWidth);
//                    jsonOps.put("height", imageHeight);
//                    jsonOps.put("rotation", 0);
//                    jsonOps.put("ocr_blocks", blocks);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                pages.put(jsonOps);
//            }
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    progressDialog.setMessage(getResources().getString(R.string.converting_pdf));
//                }
//            });
//
//            JSONObject jsonOps = new JSONObject();
//            try {
//                jsonOps.put("compressionLevel", 9);
//                jsonOps.put("paper_size", "full");
//                jsonOps.put("orientation", "portrait");
//                jsonOps.put("color", "color");
//                jsonOps.put("draw_ocr_text", true);
//                jsonOps.put("debug", false);
//                jsonOps.put("page_padding", 0);
//                jsonOps.put("text_scale", 1.0);
//                jsonOps.put("image_page_scale", 1.0);
//                jsonOps.put("items_per_page", 1);
//                jsonOps.put("imageLoadScale", 1.0);
//                jsonOps.put("imageSizeThreshold", 0);
//                jsonOps.put("jpegQuality", 80);
//                jsonOps.put("pages", pages);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            File pdfDir;
//            try {
//                pdfDir = DataManager.createTempDir();
//
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            String pdfFileName = Utils.removeExtensionFromFileName(fileName) + ".pdf";
//            String pdfPath = PDFUtils.Companion.generatePDF(BrowserActivity.this, pdfDir.getAbsolutePath(), pdfFileName, jsonOps);
//            if (!pdfPath.isEmpty()) {
//                List<Uri> uriList = Collections.singletonList(Uri.fromFile(new File(pdfPath)));
//                uploadFilesFromLocal(uriList, repoID, repoName, path);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    progressDialog.setMessage("");
//                }
//            });
//            progressDialog.dismiss();
//        }
//    }

    private void prepareImagesOfPDF(boolean useOcr) {
        progressDialog.show();

        performOcrCount = cropFilePaths.size();
        ocrData = new ArrayList<>();
        for (int i = 0; i < performOcrCount; i++) {
            ocrData.add(new JSONArray());
        }

        if (useOcr) {
            File tessDir = new File(getFilesDir(), "tessdata");
            if (!tessDir.exists()) {
                try {
                    tessDir.mkdir();
                } catch (Exception e) {
                    showShortToast(BrowserActivity.this, R.string.unknow_error);
                    return;
                }
            }

            AssetManager am = getAssets();
            if (am != null) {
                try {
                    for (String assetName : am.list("")) {
                        File targetFile;
                        if (assetName.endsWith(".traineddata")) {
                            targetFile = new File(tessDir, assetName);
                            if (!targetFile.exists()) {
                                copyTraineddata(am, assetName, targetFile);
                            }
                        }
                    }
                } catch (IOException e) {
                    showShortToast(BrowserActivity.this, R.string.unknow_error);
                    return;
                }
            }

            progressDialog.dismiss();
            if (ocrProgressDialog == null) {
                ocrProgressDialog = new OcrProgressDialog(this);
            }
            ocrProgressDialog.init(cropFilePaths.size());
            ocrProgressDialog.show();

            for (int i = 0; i < cropFilePaths.size(); i++) {
                PerformOCR task = new PerformOCR(i, cropFilePaths);
                ConcurrentAsyncTask.execute(task);
            }
        } else {
            convertImagesToPDF(cropRepoID, cropRepoName, cropPath, cropFileName, new ArrayList<>(cropFilePaths));
        }
    }

    private class PerformOCR extends AsyncTask<Void, Void, Void> {
        private int index;
        private List<String> localFilePaths;
        public PerformOCR(int index, List<String> localFilePaths) {
            this.index = index;
            this.localFilePaths = localFilePaths;
        }

        @Override
        protected void onPreExecute() {
//            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            File localFile = new File(localFilePaths.get(index));

            JSONArray blocks = new JSONArray();
            TessBaseAPI.ProgressNotifier progressNotifier = new TessBaseAPI.ProgressNotifier() {
                @Override
                public void onProgressValues(TessBaseAPI.ProgressValues progressValues) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ocrProgressDialog.setProgress(index, progressValues.getPercent());
                        }
                    });
                }
            };
            TessBaseAPI tess = new TessBaseAPI(progressNotifier);
            //TessBaseAPI tess = new TessBaseAPI();
            String dataPath = getFilesDir().getAbsolutePath();

            if (!tess.init(dataPath, "eng+deu")) {
                tess.recycle();
                showShortToast(BrowserActivity.this, R.string.unknow_error);
                return null;
            }
            tess.setImage(localFile);
            String result = tess.getHOCRText(0);
            //String result = tess.getUTF8Text();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ocrProgressDialog.setProgress(index, 100);
                }
            });

            ResultIterator resultIterator = tess.getResultIterator();

            while (resultIterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD)) {
                Rect rect = resultIterator.getBoundingRect(TessBaseAPI.PageIteratorLevel.RIL_WORD);
                String text = resultIterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD);

                try {
                    JSONObject block = new JSONObject();
                    block.put("x", (double) rect.left);
                    block.put("y", (double) rect.top);
                    block.put("width", (double) rect.width());
                    block.put("height", (double) rect.height());
                    block.put("text", text);

                    blocks.put(block);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            tess.recycle();

            ocrData.set(index, blocks);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            performOcrCount -= 1;
            if (performOcrCount == 0) {
                progressDialog.show();
                ocrProgressDialog.dismiss();
                convertImagesToPDF(cropRepoID, cropRepoName, cropPath, cropFileName, new ArrayList<>(cropFilePaths));
            }
        }
    }

    private static void copyTraineddata(@NonNull AssetManager am, @NonNull String assetName, @NonNull File outFile) {
        try (
                InputStream in = am.open(assetName);
                OutputStream out = new FileOutputStream(outFile)
        ) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void convertImagesToPDF(String repoID, String repoName, String path, String fileName, List<String> localFilePaths) {
        progressDialog.setMessage(getResources().getString(R.string.converting_pdf));
        cropRepoID = null;
        cropRepoName = null;
        cropPath = null;
        cropFilePaths.clear();

        JSONArray pages = new JSONArray();
        for (int i = 0; i < localFilePaths.size(); i++) {
            File localFile = new File(localFilePaths.get(i));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(localFile.getAbsolutePath(), options);
            double imageHeight = options.outHeight;
            double imageWidth = options.outWidth;

            JSONObject jsonOps = new JSONObject();
            try {
                jsonOps.put("imagePath", localFile.getAbsolutePath());
                jsonOps.put("width", imageWidth);
                jsonOps.put("height", imageHeight);
                jsonOps.put("rotation", 0);
                JSONArray blocks = new JSONArray();
                if (ocrData.size() > i) {
                    blocks = ocrData.get(i);
                }
                jsonOps.put("ocr_blocks", blocks);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            pages.put(jsonOps);
        }

        JSONObject jsonOps = new JSONObject();
        try {
            jsonOps.put("compressionLevel", 9);
            jsonOps.put("paper_size", "full");
            jsonOps.put("orientation", "portrait");
            jsonOps.put("color", "color");
            jsonOps.put("draw_ocr_text", true);
            jsonOps.put("debug", false);
            jsonOps.put("page_padding", 0);
            jsonOps.put("text_scale", 1.0);
            jsonOps.put("image_page_scale", 1.0);
            jsonOps.put("items_per_page", 1);
            jsonOps.put("imageLoadScale", 1.0);
            jsonOps.put("imageSizeThreshold", 0);
            jsonOps.put("jpegQuality", 80);
            jsonOps.put("pages", pages);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        File pdfDir;
        try {
            pdfDir = DataManager.createTempDir();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String pdfFileName = Utils.removeExtensionFromFileName(fileName) + ".pdf";
        String pdfPath = PDFUtils.Companion.generatePDF(BrowserActivity.this, pdfDir.getAbsolutePath(), pdfFileName, jsonOps);
        if (!pdfPath.isEmpty()) {
            List<Uri> uriList = Collections.singletonList(Uri.fromFile(new File(pdfPath)));
            uploadFilesFromLocal(uriList, repoID, repoName, path);
        }

        progressDialog.dismiss();
    }

    /**
     * Download a file
     *
     * @param dir
     * @param fileName
     */
    public void downloadFile(String repoID, String dir, String fileName, boolean offlineAvailable) {
        downloadFile(repoID, dir, fileName, offlineAvailable, false);
    }

    public void downloadFile(String repoID, String dir, String fileName, boolean offlineAvailable, boolean thumbnail) {
        // txService maybe null if layout orientation has changed
        if (txService == null) {
            return;
        }
        String filePath = Utils.pathJoin(dir, fileName);

        if (!offlineAvailable)
            showShortToast(BrowserActivity.this, getString(R.string.added_to_download_tasks));
        txService.addDownloadTask(account,
                dataManager.getCachedRepoByID(repoID).name,
                repoID,
                filePath, offlineAvailable, thumbnail);

        if (thumbnail)
            return;
        if (!txService.hasDownloadNotifProvider()) {
            DownloadNotificationProvider provider = new DownloadNotificationProvider(txService.getDownloadTaskManager(),
                    txService);
            txService.saveDownloadNotifProvider(provider);
        }

        // update downloading progress
        SeafItemAdapter adapter = getReposFragment().getAdapter();
        List<DownloadTaskInfo> infos = txService.getDownloadTaskInfosByPath(repoID, dir);
        adapter.setDownloadTaskList(infos);
    }

    public void downloadFileonDevice(String repoID, String dir, String fileName, String path) {
        File localFile = dataManager.getLocalRepoFileWithDownload(path);
        if (localFile.exists()) {
            final SeafRepo repo = dataManager.getCachedRepoByID(repoID);
            Dialog duplicateDialog = Utils.CustomDialog(this);
            duplicateDialog.setContentView(R.layout.dialog_file_exist);

            TextView messageText1 = (TextView) duplicateDialog.findViewById(R.id.message1);
            TextView messageText2 = (TextView) duplicateDialog.findViewById(R.id.message2);
            CardView replaceCardView = (CardView) duplicateDialog.findViewById(R.id.triple_first_card);
            TextView replaceTextView = (TextView) duplicateDialog.findViewById(R.id.triple_first_text);
            CardView cancelCardView = (CardView) duplicateDialog.findViewById(R.id.triple_second_card);
            TextView cancelTextView = (TextView) duplicateDialog.findViewById(R.id.triple_second_text);
            CardView keepBothCardView = (CardView) duplicateDialog.findViewById(R.id.triple_third_card);
            TextView keepBothTextView = (TextView) duplicateDialog.findViewById(R.id.triple_third_text);

            messageText1.setText(String.format(getString(R.string.download_duplicate_found1), localFile.getName()));
            messageText2.setText(getString(R.string.download_duplicate_found2));
            replaceTextView.setText(R.string.upload_replace);
            cancelTextView.setText(R.string.cancel);
            keepBothTextView.setText(R.string.upload_keep_both);

            replaceCardView.setOnClickListener(vv -> {
                boolean deleted = localFile.delete();
                if (deleted) {
                    downloadFile(repoID, dir, fileName, false);
                } else {
                    showShortToast(this, getString(R.string.unknow_error));
                }
                duplicateDialog.dismiss();
            });
            cancelCardView.setOnClickListener(vv -> {
                duplicateDialog.dismiss();
            });
            keepBothCardView.setOnClickListener(vv -> {
                downloadFile(repoID, dir, fileName, false);
                duplicateDialog.dismiss();
            });

            duplicateDialog.show();
            return;
        } else {
            downloadFile(repoID, dir, fileName, false);
        }
    }

    /**
     * Download all files (folders) under a given folder
     *
     * @param dirPath
     * @param fileName name of the download folder
     * @param recurse
     */
    public void downloadDir(String dirPath, String fileName, boolean recurse, boolean offlineAvailable, boolean thumbnail) {
        if (!Utils.isNetworkOn()) {
            showShortToast(this, R.string.network_down);
            return;
        }

        ConcurrentAsyncTask.execute(new DownloadDirTask(),
                navContext.getRepoName(),
                navContext.getRepoID(),
                dirPath,
                String.valueOf(recurse),
                fileName,
                String.valueOf(offlineAvailable),
                String.valueOf(thumbnail));
    }

    private class DownloadDirTask extends AsyncTask<String, Void, List<SeafDirent>> {

        private String repoName;
        private String repoID;
        private String fileName;
        private String dirPath;
        private int fileCount;
        private boolean recurse;
        private boolean offlineAvailable;
        private boolean thumbnail;
        private ArrayList<String> dirPaths = Lists.newArrayList();
        private SeafException err = null;

        @Override
        protected List<SeafDirent> doInBackground(String... params) {
            if (params.length != 7) {
                Log.d(DEBUG_TAG, "Wrong params to LoadDirTask");
                return null;
            }

            repoName = params[0];
            repoID = params[1];
            dirPath = params[2];
            recurse = Boolean.parseBoolean(params[3]);
            fileName = params[4];
            offlineAvailable = Boolean.parseBoolean(params[5]);
            thumbnail = Boolean.parseBoolean(params[6]);

            ArrayList<SeafDirent> dirents = Lists.newArrayList();

            dirPaths.add(Utils.pathJoin(dirPath, fileName));

            // don`t use for each loop here
            for (int i = 0; i < dirPaths.size(); i++) {

                List<SeafDirent> currentDirents;
                try {
                    currentDirents = dataManager.getDirentsFromServer(repoID, dirPaths.get(i));
                } catch (SeafException e) {
                    err = e;
                    e.printStackTrace();
                    return null;
                }

                if (currentDirents == null)
                    continue;

                List<String> nameList = new ArrayList<>();
                if (offlineAvailable) {
                    File parentDir = dataManager.getLocalRepoFile(repoName, repoID, dirPaths.get(i));
                    if (!parentDir.exists()) {
                        parentDir.mkdir();
                    }
                    if (parentDir.list() != null) {
                        nameList.addAll(Arrays.asList(parentDir.list()));
                    }
                }
                for (SeafDirent seafDirent : currentDirents) {
                    if (seafDirent.isDir()) {
                        if (recurse) {
                            dirPaths.add(Utils.pathJoin(dirPaths.get(i), seafDirent.name));
                        }
                    } else {
                        File localCachedFile;
                        if (offlineAvailable) {
                            if (thumbnail) {
                                if (!Utils.isViewableImage(seafDirent.name)) {
                                    continue;
                                }
                                String thumbName = Utils.getEncThumbPath(seafDirent.name);
                                int index = nameList.indexOf(thumbName);
                                if (index >= 0) {
                                    nameList.remove(index);
                                    continue;
                                }
                            }

                            int index = nameList.indexOf(seafDirent.name);
                            if (index >= 0) {
                                nameList.remove(index);
                                continue;
                            }
                        } else {
                            localCachedFile = dataManager.getLocalRepoFileWithDownload(seafDirent.name);
                            if (localCachedFile.exists()) {
                                continue;
                            }
                        }

                        // txService maybe null if layout orientation has changed
                        // e.g. landscape and portrait switch
                        if (txService == null)
                            return null;

                        while (txService.getDownloadWaitingListSize() > DownloadTaskManager.DOWNLOAD_LIMIT) {
                            try {
                                Thread.sleep(100); // wait
                            } catch (InterruptedException e) {
                                break;
                            }
                        }

                        txService.addTaskToDownloadQue(account,
                                repoName,
                                repoID,
                                Utils.pathJoin(dirPaths.get(i), seafDirent.name),
                                offlineAvailable,
                                thumbnail,
                                seafDirent.size);

                        if (!thumbnail)
                            fileCount++;
                    }
                }
            }

            return dirents;
        }

        @Override
        protected void onPostExecute(List<SeafDirent> dirents) {
            if (thumbnail)
                return;

            if (dirents == null) {
                if (err != null) {
                    showShortToast(BrowserActivity.this, R.string.transfer_list_network_error);
                }
                return;
            }

            if (fileCount == 0)
                showShortToast(BrowserActivity.this, R.string.transfer_download_no_task);
            else {
                showShortToast(BrowserActivity.this, getResources().getQuantityString(R.plurals.transfer_download_started, fileCount, fileCount));
                if (!txService.hasDownloadNotifProvider()) {
                    DownloadNotificationProvider provider = new DownloadNotificationProvider(txService.getDownloadTaskManager(),
                            txService);
                    txService.saveDownloadNotifProvider(provider);
                }
            }

            // set download tasks info to adapter in order to update download progress in UI thread
            getReposFragment().getAdapter().setDownloadTaskList(txService.getDownloadTaskInfosByPath(repoID, dirPath));
        }
    }

    private void startFileActivity(SelectedFileInfo fileInfo, int openOfficeValue, boolean isOpenWith) {
        // txService maybe null if layout orientation has changed
        if (txService == null) {
            return;
        }
        int taskID = txService.addDownloadTask(account, fileInfo.repoName, fileInfo.repoID, fileInfo.filePath, fileInfo.fileSize, true);
        Intent intent = new Intent(this, FileActivity.class);
        intent.putExtra("repoName", fileInfo.repoName);
        intent.putExtra("repoID", fileInfo.repoID);
        intent.putExtra("filePath", fileInfo.filePath);
        intent.putExtra("account", account);
        intent.putExtra("taskID", taskID);
        intent.putExtra("open_office", openOfficeValue);
        intent.putExtra("is_open_with", isOpenWith);
        startActivityForResult(intent, DOWNLOAD_FILE_REQUEST);
    }

    private void startPlayActivity(String fileName, String repoID, String filePath) {
        Intent intent = new Intent(this, CustomExoVideoPlayerActivity.class);
        intent.putExtra("fileName", fileName);
        intent.putExtra("repoID", repoID);
        intent.putExtra("filePath", filePath);
        intent.putExtra("account", account);
        //DOWNLOAD_PLAY_REQUEST
        startActivity(intent );
    }


    public void onStarredFileSelected(final SeafStarredFile starredFile, boolean isOpenWith) {
        final long fileSize = starredFile.getSize();
        final String repoID = starredFile.getRepoID();
        final SeafRepo repo = dataManager.getCachedRepoByID(repoID);
        if (repo == null)
            return;

        if (repo.encrypted && !dataManager.getRepoPasswordSet(repo.id)) {
            String password = dataManager.getRepoPassword(repo.id);
            showPasswordDialog(repo.name, repo.id,
                    new TaskDialog.TaskDialogListener() {
                        @Override
                        public void onTaskSuccess() {
                            onStarredFileSelected(starredFile);
                        }
                    }, password);

            return;
        }

        final String repoName = repo.getName();
        final String filePath = starredFile.getPath();
        final String dirPath = Utils.getParentPath(filePath);

        SelectedFileInfo fileInfo = new SelectedFileInfo(
                starredFile.getTitle(),
                fileSize,
                starredFile.getMtime(),
                repoName,
                repoID,
                dirPath,
                filePath
        );
        processSelectedFile(isOpenWith, fileInfo, getOpenValue(filePath));
    }

    @Override
    public void onStarredFileSelected(SeafStarredFile starredFile) {
        onStarredFileSelected(starredFile, false);
    }

    private void backPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }

        boolean canFinish = true;
        if (currentPosition == INDEX_LIBRARY_TAB) {
            getReposFragmentWithoutInstantiate().stopSupportActionMode();
            getReposFragmentWithoutInstantiate().clearSearchText();
            if (isSearchMode) {
                setToolbarTitle(getResources().getString(R.string.libraries));
                isSearchMode = false;
                navContext.setRepoID(null);
                getReposFragment().clearAdapterData();
                getReposFragment().refreshView(true);
                getReposFragment().clearSearchText();
                UnlockActivity.g_BACK_PRESSED = true;
                return;
            }

            if (navContext.inRepo()) {
                if (navContext.isRepoRoot()) {
                    setToolbarTitle(getResources().getString(R.string.libraries));
                    navContext.setRepoID(null);
                } else {
                    String parentPath = Utils.getParentPath(navContext
                            .getDirPath());
                    navContext.setDir(parentPath, null);
                    if (parentPath.equals(ACTIONBAR_PARENT_PATH)) {
                        setToolbarTitle(navContext.getRepoName());
                    } else {
                        setToolbarTitle(parentPath.substring(parentPath.lastIndexOf(ACTIONBAR_PARENT_PATH) + 1));
                    }
                }
                getReposFragment().clearAdapterData();
                getReposFragment().refreshView(true);
                canFinish = false;
            }
        } else if (currentPosition == INDEX_ACTIVITIES_TAB) {
            getStarredFragmentWithoutInstantiate().stopSupportActionMode();
        }

        UnlockActivity.g_BACK_PRESSED = true;

        if (canFinish) {
            finish();
        }
    }

    @Override
    public void onBackStackChanged() {
    }

    public void exportLogFile() {
        final File logFile = new File(SeafileLog.getLogDirPath(), SeafileLog.MY_LOG_FILE_NAME);
        final File backupLogFile = new File(SeafileLog.getLogDirPath(), SeafileLog.MY_BACKUP_LOG_FILE_NAME);
        final File eventsLogfile = new File(SeafileLog.getLogDirPath(), SeafileLog.MY_EVENTS_LOG_FILE_NAME);
        if (!logFile.exists() && !backupLogFile.exists() && !eventsLogfile.exists()) {
            showShortToast(this, R.string.file_not_exist);
            return;
        }

        ArrayList<Uri> uriList = new ArrayList<>();
        if (logFile.exists()) {
            Uri uri = null;
            if (android.os.Build.VERSION.SDK_INT > 23) {
                uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName(), logFile);
            } else {
                uri = Uri.fromFile(logFile);
            }
            uriList.add(uri);
        }
        if (backupLogFile.exists()) {
            Uri uri = null;
            if (android.os.Build.VERSION.SDK_INT > 23) {
                uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName(), backupLogFile);
            } else {
                uri = Uri.fromFile(backupLogFile);
            }
            uriList.add(uri);
        }
        if (eventsLogfile.exists()) {
            Uri uri = null;
            if (android.os.Build.VERSION.SDK_INT > 23) {
                uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName(), eventsLogfile);
            } else {
                uri = Uri.fromFile(eventsLogfile);
            }
            uriList.add(uri);
        }


        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        sendIntent.setType(Utils.getFileMimeType(SeafileLog.MY_LOG_FILE_NAME));
        sendIntent.putExtra(Intent.EXTRA_STREAM, uriList);

        // Get a list of apps
        List<ResolveInfo> infos = Utils.getAppsByIntent(sendIntent);

        if (infos.isEmpty()) {
            showShortToast(this, R.string.no_app_available);
            return;
        }

        AppChoiceDialog2 dialog = new AppChoiceDialog2();
        dialog.addCustomAction(0, getResources().getDrawable(R.drawable.ic_logo),
                getString(R.string.app_name));
        dialog.init(getString(R.string.export_file), infos, new AppChoiceDialog2.OnItemSelectedListener() {
            @Override
            public void onCustomActionSelected(AppChoiceDialog2.CustomAction action) {
                String logRepoId = null;
                List<SeafRepo> repos = dataManager.getReposFromCache();
                for (SeafRepo repo : repos) {
                    if (repo.name.equals(logRepoName)) {
                        logRepoId = repo.id;
                    }
                }

                progressDialog.show();
                if (logRepoId == null) {
                    ConcurrentAsyncTask.execute(new CreateLogRepo(uriList));
                    return;
                }

                ConcurrentAsyncTask.execute(new LoadDirTask(logRepoId, uriList, "/"));
            }

            @Override
            public void onAppSelected(ResolveInfo appInfo) {
                String className = appInfo.activityInfo.name;
                String packageName = appInfo.activityInfo.packageName;
                sendIntent.setClassName(packageName, className);

                startActivity(sendIntent);
            }

        });
        dialog.show(getSupportFragmentManager(), CHOOSE_APP_DIALOG_FRAGMENT_TAG);
    }


    /************  Files ************/

    /**
     * Export a file.
     * 1. first ask the user to choose an app
     * 2. then download the latest version of the file
     * 3. start the choosen app
     *
     * @param fileName The name of the file to share in the current navcontext
     */
    public void exportFile(String repoID, String dirPath, String fileName, long fileSize) {
        String repoName = dataManager.getCachedRepoByID(repoID).name;
        String fullPath = Utils.pathJoin(dirPath, fileName);
        chooseExportApp(repoName, repoID, fullPath, fileSize);
    }

    private void chooseExportApp(final String repoName, final String repoID, final String path, final long fileSize) {
        final File file = dataManager.getLocalRepoFile(repoName, repoID, path);
        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT > 23) {
            uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName(), file);
        } else {
            uri = Uri.fromFile(file);
        }
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType(Utils.getFileMimeType(file));
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);

        // Get a list of apps
        List<ResolveInfo> infos = Utils.getAppsByIntent(sendIntent);

        if (infos.isEmpty()) {
            showShortToast(this, R.string.no_app_available);
            return;
        }

        AppChoiceDialog2 dialog = new AppChoiceDialog2();
        dialog.init(getString(R.string.export_file), infos, new AppChoiceDialog2.OnItemSelectedListener() {
            @Override
            public void onCustomActionSelected(AppChoiceDialog2.CustomAction action) {
            }

            @Override
            public void onAppSelected(ResolveInfo appInfo) {
                String className = appInfo.activityInfo.name;
                String packageName = appInfo.activityInfo.packageName;
                sendIntent.setClassName(packageName, className);

                if (Utils.isNetworkOn() && file.exists()) {
                    startActivity(sendIntent);
                    return;
                }
                fetchFileAndExport(appInfo, sendIntent, repoName, repoID, path, fileSize);
            }

        });
        dialog.show(getSupportFragmentManager(), CHOOSE_APP_DIALOG_FRAGMENT_TAG);
    }

    public void fetchFileAndExport(final ResolveInfo appInfo, final Intent intent, final String repoName, final String repoID, final String path, final long fileSize) {

        fetchFileDialog = new FetchFileDialog();
        fetchFileDialog.init(repoName, repoID, path, fileSize, new FetchFileDialog.FetchFileListener() {
            @Override
            public void onSuccess() {
                startActivity(intent);
            }

            @Override
            public void onDismiss() {
                fetchFileDialog = null;
            }

            @Override
            public void onFailure(SeafException err) {
            }
        });
        fetchFileDialog.show(getSupportFragmentManager(), OPEN_FILE_DIALOG_FRAGMENT_TAG);
    }

    public void renameRepo(String repoID, String repoName) {
        final RenameRepoDialog dialog = new RenameRepoDialog();
        dialog.init(repoID, repoName, account);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                Utils.utilsEventsLogInfo(
                        getResources().getString(R.string.rename) +
                                ", " +
                                String.format(getResources().getString(R.string.from_to), repoName, dialog.getNewName()));
                showShortToast(BrowserActivity.this, R.string.rename_successful);
                updateRepoNameFromCache(repoID, dialog.getNewName());
                ReposFragment reposFragment = getReposFragment();
                if (currentPosition == INDEX_LIBRARY_TAB && reposFragment != null) {
                    reposFragment.refreshView(true);
                }
            }
        });
        dialog.show(getSupportFragmentManager(), TAG_RENAME_REPO_DIALOG_FRAGMENT);
    }

    private void updateRepoNameFromCache(String repoID, String newRepoName) {
        List<SeafRepo> repos = dataManager.getReposFromCache();
        List<SeafRepo> newRepos = Lists.newArrayListWithCapacity(0);
        for (SeafRepo repo: repos) {
            if (repo.getID().equals(repoID)) {
                repo.name = newRepoName;
            }
            newRepos.add(repo);
        }

        try {
            File cache = dataManager.getFileForReposCache();
            Utils.writeFile(cache, dataManager.reposToString(newRepos));
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "Could not write repo cache to disk.", e);
        }
    }

    public void deleteRepo(String repoID, String repoName) {
        final DeleteRepoDialog dialog = new DeleteRepoDialog();
        dialog.init(repoID, account);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                updateNeedUpdateThumbsStatusOfEncRepo(repoID);

                Utils.utilsEventsLogInfo(
                        getResources().getString(R.string.delete) +
                                ", " +
                                repoName);
                showShortToast(BrowserActivity.this, R.string.delete_successful);
                deleteRepoFromCache(repoID);
                ReposFragment reposFragment = getReposFragment();
                if (currentPosition == INDEX_LIBRARY_TAB && reposFragment != null) {
                    reposFragment.refreshView(true);
                }

                checkBackupFolder();
            }
        });
        dialog.show(getSupportFragmentManager(), TAG_DELETE_REPO_DIALOG_FRAGMENT);
    }

    private void deleteRepoFromCache(String repoID) {
        List<SeafRepo> repos = dataManager.getReposFromCache();
        List<SeafRepo> newRepos = Lists.newArrayListWithCapacity(0);
        for (SeafRepo repo: repos) {
            if (!repo.getID().equals(repoID)) {
                newRepos.add(repo);
            }
        }

        try {
            File cache = dataManager.getFileForReposCache();
            Utils.writeFile(cache, dataManager.reposToString(newRepos));
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "Could not write repo cache to disk.", e);
        }
    }

    public void checkBackupFolder() {
        boolean dirAutomaticUpload = SettingsManager.instance().isFolderAutomaticBackup();
        String backupEmail = SettingsManager.instance().getBackupEmail();
        if (!dirAutomaticUpload || mFolderBackupService == null || TextUtils.isEmpty(backupEmail)) {
            return;
        }

        RepoConfig repoConfig = mFolderBackupService.backupRepoConfig(backupEmail);
        if (repoConfig == null) {
            return;
        }
        SeafRepo repo = dataManager.getCachedRepoByID(repoConfig.getRepoID());
        if (repo != null) {
            return;
        }

        if (adapter != null && getSettingsFragment() != null) {
            getSettingsFragment().callOnBackupFolderCard(true);
        } else {
            SettingsManager settingsMgr = SettingsManager.instance();
            settingsMgr.saveFolderAutomaticBackup(false);
            settingsMgr.saveBackupPaths("");
            FolderBackupDBHelper.getDatabaseHelper().removeRepoConfig(settingsMgr.getBackupEmail());

            if (mFolderBackupService.fileMonitorRunning)
                mFolderBackupService.stopFolderMonitor();
        }

        String errMsg = String.format(getString(R.string.not_found_backup_repo), repoConfig.getRepoName());
        Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
        NotificationCompat.Builder mBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder = new NotificationCompat.Builder(this, CustomNotificationBuilder.CHANNEL_ID_ERROR);
        } else {
            mBuilder = new NotificationCompat.Builder(this);
        }

        mBuilder.setSmallIcon(R.drawable.ic_logo)
                .setOnlyAlertOnce(true)
                .setContentTitle(getString(R.string.backup_failed))
                .setContentText(errMsg);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());
    }

    /**
     * Share a file. Generating a file share link and send the link or file to someone
     * through some app.
     *
     * @param repoID
     * @param path
     */

    public void showShareDialog(String repoID, String path, String dialogType, long fileSize, String fileName) {
        SeafRepo repo = dataManager.getCachedRepoByID(repoID);
        Intent intent = new Intent(this, ShareDialogActivity.class);
//        intent.putExtra(CAMERA_UPLOAD_BOTH_PAGES, true);
        intent.putExtra(ShareDialogActivity.SHARE_DIALOG_FILE_NAME, fileName);
        intent.putExtra(ShareDialogActivity.SHARE_DIALOG_TYPE, dialogType);
        intent.putExtra(ShareDialogActivity.SHARE_DIALOG_ACCOUNT, account);
        intent.putExtra(ShareDialogActivity.SHARE_DIALOG_REPO, SeafRepo.toString(repo));
        intent.putExtra(ShareDialogActivity.SHARE_DIALOG_PATH, path);
        startActivityForResult(intent, SHARE_DIALOG_REQUEST);

//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        boolean inChina = Utils.isInChina();
//        String[] strings;
//        //if user  in China ，system  add  WeChat  share
//        if (inChina) {
//            strings = getResources().getStringArray(R.array.file_action_share_array_zh);
//        } else {
//            strings = getResources().getStringArray(R.array.file_action_share_array);
//        }
//        builder.setItems(strings, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (!inChina) {
//                    which++;
//                }
//                switch (which) {
//                    case 0:
//                        WidgetUtils.ShareWeChat(BrowserActivity.this, account, repoID, path, fileName, fileSize, isDir);
//                        break;
//                    case 1:
//                        // need input password
//                        WidgetUtils.chooseShareApp(BrowserActivity.this, repoID, path, isDir, account, null, null);
//                        break;
//                    case 2:
//                        WidgetUtils.inputSharePassword(BrowserActivity.this, repoID, path, isDir, account);
//                        break;
//                }
//            }
//        }).show();
    }

    public void renameFile(String repoID, String repoName, String path) {
        doRename(repoID, repoName, path, false);
    }

    public void renameDir(String repoID, String repoName, String path) {
        doRename(repoID, repoName, path, true);
    }

    private void doRename(String repoID, String repoName, String path, boolean isdir) {
        final RenameFileDialog dialog = new RenameFileDialog();
        dialog.init(repoID, path, isdir, account);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                updateNeedUpdateThumbsStatusOfEncRepo(repoID);

                Utils.utilsEventsLogInfo(
                        getResources().getString(R.string.rename) +
                                ", " +
                                String.format(getResources().getString(R.string.from_to), Utils.fileNameFromPath(path), dialog.getNewFileName()));
                showShortToast(BrowserActivity.this, R.string.rename_successful);
                ReposFragment reposFragment = getReposFragment();
                if (isSearchMode)
                    dataManager.updateRefreshPaths(Utils.pathJoin(repoName, Utils.getParentPath(Utils.removeLastPathSeperator(path))));
                if (currentPosition == INDEX_LIBRARY_TAB && reposFragment != null) {
                    reposFragment.refresh();
                }
            }
        });
        dialog.show(getSupportFragmentManager(), TAG_RENAME_FILE_DIALOG_FRAGMENT);
    }

    public void deleteFile(String repoID, String repoName, String path) {
        doDelete(repoID, repoName, path, false);
    }

    public void deleteDir(String repoID, String repoName, String path) {
        doDelete(repoID, repoName, path, true);
    }

    private void doDelete(final String repoID, String repoName, String path, boolean isdir) {
        final DeleteFileDialog dialog = new DeleteFileDialog();
        dialog.init(repoID, path, isdir, account);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                updateNeedUpdateThumbsStatusOfEncRepo(repoID);

                Utils.utilsEventsLogInfo(
                        getResources().getString(R.string.delete) +
                                ", " +
                                Utils.fileNameFromPath(path));
                showShortToast(BrowserActivity.this, R.string.delete_successful);
                if (isSearchMode) {
                    getReposFragment().research();
                    return;
                }
                List<SeafDirent> cachedDirents = getDataManager()
                        .getCachedDirents(repoID, getNavContext().getDirPath());
                getReposFragment().getAdapter().setItems(cachedDirents);
                getReposFragment().getAdapter().notifyDataSetChanged();
            }
        });
        dialog.show(getSupportFragmentManager(), TAG_DELETE_FILE_DIALOG_FRAGMENT);
    }

    public void copyFile(String srcRepoId, String srcRepoName, String srcDir, String srcFn, boolean isdir) {
        chooseCopyMoveDest(srcRepoId, srcRepoName, srcDir, srcFn, isdir, CopyMoveContext.OP.COPY);
    }

    public void moveFile(String srcRepoId, String srcRepoName, String srcDir, String srcFn, boolean isdir) {
        chooseCopyMoveDest(srcRepoId, srcRepoName, srcDir, srcFn, isdir, CopyMoveContext.OP.MOVE);
    }

    public void starFile(String srcRepoId, String srcDir, String srcFn, SeafDirent dirent) {
        getStarredFragment().doStarFile(srcRepoId, srcDir, srcFn, dirent);
    }

    public void unStarFile(String srcRepoId, String path) {
        getStarredFragment().doUnStarFile(srcRepoId, path);
    }

    public void chooseCopyMoveDest(String repoID, String repoName, String path, String filename, boolean isdir, CopyMoveContext.OP op) {
        copyMoveContext = new CopyMoveContext(repoID, repoName, path, filename,
                isdir, op);
        Intent intent = new Intent(this, SeafilePathChooserActivity.class);
        intent.putExtra(SeafilePathChooserActivity.DATA_ACCOUNT, account);
        intent.putExtra(SeafilePathChooserActivity.IS_COPY, op == CopyMoveContext.OP.COPY);
        SeafRepo repo = dataManager.getCachedRepoByID(repoID);
        boolean isShowEncryptDir = false;
        if (repo.encrypted) {
            isShowEncryptDir = true;
            intent.putExtra(SeafilePathChooserActivity.ENCRYPTED_REPO_ID, repoID);
        }
        intent.putExtra(SeafilePathChooserActivity.SHOW_ENCRYPTED_REPOS, isShowEncryptDir);
        startActivityForResult(intent, CHOOSE_COPY_MOVE_DEST_REQUEST);
        return;
    }

    private void doCopyMove() {
        if (!copyMoveContext.checkCopyMoveToSubfolder()) {
            showShortToast(this, copyMoveContext.isCopy()
                    ? R.string.cannot_copy_folder_to_subfolder
                    : R.string.cannot_move_folder_to_subfolder);
            return;
        }
        final CopyMoveDialog dialog = new CopyMoveDialog();
        dialog.init(account, copyMoveContext, isSearchMode);
        dialog.setCancelable(false);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                if (isSearchMode) {
                    needUpdateThumbsStatusOfEncRepo = true;
                } else {
                    updateNeedUpdateThumbsStatusOfEncRepo(navContext.getRepoID());
                }

                showShortToast(BrowserActivity.this, copyMoveContext.isCopy()
                        ? R.string.copied_successfully
                        : R.string.moved_successfully);
                if (isSearchMode) {
                    getReposFragment().research();
                    return;
                }
                if (copyMoveContext.batch) {
                    List<SeafDirent> cachedDirents = getDataManager().getCachedDirents(getNavContext().getRepoID(),
                            getNavContext().getDirPath());

                    // refresh view
                    if (getReposFragment().getAdapter() != null) {
                        getReposFragment().getAdapter().setItems(cachedDirents);
                        getReposFragment().getAdapter().notifyDataSetChanged();
                    }

                    if (cachedDirents.size() == 0)
                        getReposFragment().getEmptyView().setVisibility(View.VISIBLE);
                    return;
                }

                if (copyMoveContext.isMove()) {
                    ReposFragment reposFragment = getReposFragment();
                    if (currentPosition == INDEX_LIBRARY_TAB && reposFragment != null) {
                        reposFragment.refreshView();
                    }
                }
            }
        });
        dialog.show(getSupportFragmentManager(), TAG_COPY_MOVE_DIALOG_FRAGMENT);
    }

    private void onFileDownloadSuccess(int taskID) {
        if (txService == null) {
            return;
        }

        DownloadTaskInfo info = txService.getDownloadTaskInfo(taskID);
        if (info == null)
            return;

        if (info.thumbnail) {
            if (getSettingsFragment() != null) {
                getSettingsFragment().updateThumbImagesCount(
                        getSettingsFragment().allEncThumbsCount + 1,
                        getSettingsFragment().allEncImagesCount
                );
            }
            txService.removeDownloadTask(taskID);
            return;
        }

//        dataManager.addDownloadsToCache(info);

        final String repoName = info.repoName;
        final String repoID = info.repoID;
        final String path = info.pathInRepo;

        String st = repoID + path;
        if (pdfConvertList.contains(st)) {
            pdfConvertList.remove(st);
            final File localFile = dataManager.getLocalCachedFile(repoName, repoID, path);
            final String fileName = Utils.fileNameFromPath(path);
            final String dir = Utils.pathSplit(path, fileName);
            if (Utils.isViewableImage(fileName)) {
                startCrop(repoID, repoName, dir, fileName, localFile);
            } else {
                convertToPDF(repoID, repoName, dir, fileName, localFile);
            }
        }
    }
    private void onFileDownloadFailed(int taskID) {
        if (txService == null) {
            return;
        }

        DownloadTaskInfo info = txService.getDownloadTaskInfo(taskID);
        if (info == null)
            return;
        if (info.thumbnail) {
            txService.removeDownloadTask(taskID);
            return;
        }

//        dataManager.addDownloadsToCache(info);

        final SeafException err = info.err;
        final String repoName = info.repoName;
        final String repoID = info.repoID;
        final String path = info.pathInRepo;

        if (err != null && err.getCode() == SeafConnection.HTTP_STATUS_REPO_PASSWORD_REQUIRED) {
            if (currentPosition == INDEX_LIBRARY_TAB
                    && repoID.equals(navContext.getRepoID())
                    && Utils.getParentPath(path).equals(navContext.getDirPath())) {
                showPasswordDialog(repoName, repoID,
                        new TaskDialog.TaskDialogListener() {
                            @Override
                            public void onTaskSuccess() {
                                txService.addDownloadTask(account,
                                        repoName,
                                        repoID,
                                        path,
                                        true);
                            }
                        });
                return;
            }
        }

        showShortToast(this, getString(R.string.download_failed));
    }

    private void onFileUploaded(int taskID) {
        if (txService == null) {
            progressDialog.dismiss();
            return;
        }

        UploadTaskInfo info = txService.getUploadTaskInfo(taskID);

        if (info == null) {
            progressDialog.dismiss();
            return;
        }
//        dataManager.addUploadsToCache(info);

        String verb = getString(info.isUpdate ? R.string.updated : R.string.uploaded);
        showShortToast(this, verb + " " + Utils.fileNameFromPath(info.localFilePath));

        String repoID = info.repoID;
        String dir = info.parentDir;
        if (currentPosition == INDEX_LIBRARY_TAB && repoID.equals(navContext.getRepoID()) && dir.equals(navContext.getDirPath())) {
            getReposFragment().refreshView(true, true);
            if (info.isUpdate && !info.source.equals(Utils.TRANSFER_FOLDER_TAG)) {
                Utils.utilsEventsLogInfo(
                        getResources().getString(R.string.updated) +
                                ", " +
                                Utils.fileNameFromPath(info.localFilePath));
            }
        }

        if (dataManager.getCachedRepoByID(repoID).canLocalDecrypt() &&
                Utils.isViewableImage(Utils.fileNameFromPath(info.localFilePath))) {
            SettingsFragment settingsFragment = getSettingsFragment();
            if (settingsFragment != null) {
                settingsFragment.updateThumbImagesCount(
                        settingsFragment.allEncThumbsCount + 1,
                        settingsFragment.allEncImagesCount + 1);
            }
        }
    }

    private int intShowErrorTime;

    private void onFileUploadFailed(int taskID) {
        if (++intShowErrorTime <= 1)
            showShortToast(this, getString(R.string.upload_failed));

        UploadTaskInfo info = txService.getUploadTaskInfo(taskID);
        if (info == null) {
            return;
        }
//        dataManager.addUploadsToCache(info);
    }

    public PasswordDialog showPasswordDialog(String repoName, String repoID, TaskDialog.TaskDialogListener listener) {
        return showPasswordDialog(repoName, repoID, listener, null);
    }

    public PasswordDialog showPasswordDialog(String repoName, String repoID, TaskDialog.TaskDialogListener listener, String password) {
        PasswordDialog passwordDialog = new PasswordDialog();
        passwordDialog.setRepo(repoName, repoID, account);
        if (password != null) {
            passwordDialog.setPassword(password);
        }
        passwordDialog.setTaskDialogLisenter(listener);
        passwordDialog.show(getSupportFragmentManager(), PASSWORD_DIALOG_FRAGMENT_TAG);
        return passwordDialog;
    }

    /************  Multiple Files ************/

    /**
     * Delete multiple fiels
     *
     * @param repoID
     * @param path
     * @param dirents
     */
    public void deleteFiles(final String repoID, String path, List<SeafDirent> dirents) {
        final DeleteFileDialog dialog = new DeleteFileDialog();
        dialog.init(repoID, path, dirents, account);
        dialog.setCancelable(false);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                showShortToast(BrowserActivity.this, R.string.delete_successful);
                if (isSearchMode) {
                    needUpdateThumbsStatusOfEncRepo = true;
                    getReposFragment().research();
                    return;
                }
                updateNeedUpdateThumbsStatusOfEncRepo(repoID);
                if (getDataManager() != null) {
                    List<SeafDirent> cachedDirents = getDataManager().getCachedDirents(repoID,
                            getNavContext().getDirPath());
                    getReposFragment().getAdapter().setItems(cachedDirents);
                    getReposFragment().getAdapter().notifyDataSetChanged();
                    // update contextual action bar (CAB) title
                    getReposFragment().updateContextualActionBar();
                    if (cachedDirents.size() == 0)
                        getReposFragment().getEmptyView().setVisibility(View.VISIBLE);
                }
            }
        });
        dialog.show(getSupportFragmentManager(), TAG_DELETE_FILES_DIALOG_FRAGMENT);
    }

    /**
     * Copy multiple files
     *
     * @param srcRepoId
     * @param srcRepoName
     * @param srcDir
     * @param dirents
     */
    public void copyFiles(String srcRepoId, String srcRepoName, String srcDir, List<SeafDirent> dirents) {
        chooseCopyMoveDestForMultiFiles(srcRepoId, srcRepoName, srcDir, dirents, CopyMoveContext.OP.COPY);
    }

    /**
     * Move multiple files
     *
     * @param srcRepoId
     * @param srcRepoName
     * @param srcDir
     * @param dirents
     */
    public void moveFiles(String srcRepoId, String srcRepoName, String srcDir, List<SeafDirent> dirents) {
        chooseCopyMoveDestForMultiFiles(srcRepoId, srcRepoName, srcDir, dirents, CopyMoveContext.OP.MOVE);
    }

    /**
     * Choose copy/move destination for multiple files
     *
     * @param repoID
     * @param repoName
     * @param dirPath
     * @param dirents
     * @param op
     */
    private void chooseCopyMoveDestForMultiFiles(String repoID, String repoName, String dirPath, List<SeafDirent> dirents, CopyMoveContext.OP op) {
        copyMoveContext = new CopyMoveContext(repoID, repoName, dirPath, dirents, op);
        Intent intent = new Intent(this, SeafilePathChooserActivity.class);
        intent.putExtra(SeafilePathChooserActivity.DATA_ACCOUNT, account);
        intent.putExtra(SeafilePathChooserActivity.IS_COPY, op == CopyMoveContext.OP.COPY);
        SeafRepo repo = getDataManager().getCachedRepoByID(repoID);
        boolean isShowEncryptDir = true;
        if (repo != null ) {
            if (repo.encrypted) {
                intent.putExtra(SeafilePathChooserActivity.ENCRYPTED_REPO_ID, repoID);
            }
            intent.putExtra(SeafilePathChooserActivity.REPO_ENCRYPTED, repo.encrypted);
        }
        intent.putExtra(SeafilePathChooserActivity.SHOW_ENCRYPTED_REPOS, isShowEncryptDir);
        startActivityForResult(intent, BrowserActivity.CHOOSE_COPY_MOVE_DEST_REQUEST);
    }

    /**
     * Add selected files (folders) to downloading queue,
     * folders with subfolder will be downloaded recursively.
     *
     * @param repoID
     * @param repoName
     * @param dirPath
     * @param dirents
     */
    public void downloadFiles(
            String repoID,
            String repoName,
            String dirPath,
            List<SeafDirent> dirents,
            boolean thumbnail,
            boolean offlineAvailable) {
        if (!Utils.isNetworkOn()) {
            showShortToast(this, R.string.network_down);
            return;
        }

        DownloadFilesTask task = new DownloadFilesTask(repoID, repoName, dirPath, dirents, thumbnail, offlineAvailable);
        ConcurrentAsyncTask.execute(task);
    }

    /**
     * Task for asynchronously downloading selected files (folders),
     * files wont be added to downloading queue if they have already been cached locally.
     */
    private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {
        private String repoID, repoName, dirPath;
        private List<SeafDirent> dirents;
        private boolean thumbnail;
        private SeafException err;
        private int fileCount;
        private boolean offlineAvailable;

        public DownloadFilesTask(String repoID, String repoName, String dirPath, List<SeafDirent> dirents, boolean thumbnail, boolean offlineAvailable) {
            this.repoID = repoID;
            this.repoName = repoName;
            this.dirPath = dirPath;
            this.dirents = dirents;
            this.thumbnail = thumbnail;
        }

        @Override
        protected void onPreExecute() {
            // getReposFragment().showLoading(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<String> dirPaths = Lists.newArrayList(dirPath);
            ArrayList<String> repoIDs = Lists.newArrayList(repoID);
            for (int i = 0; i < dirPaths.size(); i++) {
                if (i > 0) {
                    try {
                        repoID = repoIDs.get(i);
                        dirents = getDataManager().getDirentsFromServer(repoID, dirPaths.get(i));
                    } catch (SeafException e) {
                        err = e;
                        Log.e(DEBUG_TAG, e.getMessage() + e.getCode());
                    }
                }

                if (dirents == null)
                    continue;

                for (SeafDirent seafDirent : dirents) {
                    if (seafDirent.isDir()) {
                        // download files recursively
                        dirPaths.add(seafDirent.isSearchedFile? Utils.removeLastPathSeperator(seafDirent.path) : Utils.pathJoin(dirPaths.get(i), seafDirent.name));
                        repoIDs.add(seafDirent.isSearchedFile? seafDirent.repoID : repoID);
                    } else {
                        String repoID2 = repoID;
                        String path2 = Utils.pathJoin(dirPaths.get(i), seafDirent.name);
                        if (seafDirent.isSearchedFile) {
                            SeafRepo repo = dataManager.getCachedRepoByID(seafDirent.repoID);
                            repoID2 = repo.getID();
                            path2 = seafDirent.path;
                        }
                        File localCachedFile = getDataManager().getLocalRepoFileWithDownload(path2);
                        if (localCachedFile.exists()) {
                            continue;
                        }

                        // txService maybe null if layout orientation has changed
                        // e.g. landscape and portrait switch
                        if (txService == null)
                            return null;

                        while (txService.getDownloadWaitingListSize() > DownloadTaskManager.DOWNLOAD_LIMIT) {
                            try {
                                Thread.sleep(100); // wait
                            } catch (InterruptedException e) {
                                break;
                            }
                        }

                        txService.addTaskToDownloadQue(account,
                                repoName,
                                repoID2,
                                path2,
                                offlineAvailable,
                                thumbnail,
                                seafDirent.size);

                        fileCount++;
                    }

                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (thumbnail) {
                return;
            }
            // update ui
            // getReposFragment().showLoading(false);

            if (err != null) {
                showShortToast(BrowserActivity.this, R.string.transfer_list_network_error);
                return;
            }

            if (fileCount == 0)
                showShortToast(BrowserActivity.this, R.string.transfer_download_no_task);
            else {
                showShortToast(BrowserActivity.this,
                        getResources().getQuantityString(R.plurals.transfer_download_started,
                                fileCount,
                                fileCount));

                if (!txService.hasDownloadNotifProvider()) {
                    DownloadNotificationProvider provider =
                            new DownloadNotificationProvider(txService.getDownloadTaskManager(),
                                    txService);
                    txService.saveDownloadNotifProvider(provider);
                }

            }

        }
    }

    @Override
    public boolean onKeyUp(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
                if (overflowMenuCard !=null) {
                    overflowMenuCard.callOnClick();
                }
        }

        return super.onKeyUp(keycode, e);
    }

    // for receive broadcast from TransferService
    private class TransferReceiver extends BroadcastReceiver {

        private TransferReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            int taskID = 0;
            if (intent.hasExtra("taskID")) {
                 taskID = intent.getIntExtra("taskID", 0);
            }
            if (progressDialog.isShowing() && type != null && type.contains("upload")) {
                if (taskID != 0) {
                    UploadTaskInfo info = txService.getUploadTaskInfo(taskID);
                    logNameList.remove(Utils.fileNameFromPath(info.localFilePath));
                    if (logNameList.isEmpty()) {
                        ConcurrentAsyncTask.execute(new GetDownloadLinkTask(info.repoID, info.parentDir));
                    }
                }
                return;
            }
            if (type.equals(DownloadTaskManager.BROADCAST_FILE_DOWNLOAD_SUCCESS)) {
                onFileDownloadSuccess(taskID);
            } else if (type.equals(DownloadTaskManager.BROADCAST_FILE_DOWNLOAD_FAILED)) {
                onFileDownloadFailed(taskID);
            } else if (type.equals(UploadTaskManager.BROADCAST_FILE_UPLOAD_SUCCESS)) {
                onFileUploaded(taskID);
            } else if (type.equals(UploadTaskManager.BROADCAST_FILE_UPLOAD_FAILED)) {
                onFileUploadFailed(taskID);
            }
        }

    } // TransferReceiver


    public void showRepoBottomSheet(SeafRepo repo) {
        getReposFragment().showRepoBottomSheet(repo);
    }

    public void showFileBottomSheet(String title, final SeafDirent dirent) {
        getReposFragment().showFileBottomSheet(title, dirent);
    }

    public void showDirBottomSheet(String title, final SeafDirent dirent) {
        getReposFragment().showDirBottomSheet(title, dirent);
    }

    private void syncCamera() {
        SettingsManager settingsManager = SettingsManager.instance();
        CameraUploadManager cameraManager = new CameraUploadManager(getApplicationContext());
        if (cameraManager.isCameraUploadEnabled() && settingsManager.isVideosUploadAllowed()) {
            cameraManager.performFullSync();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CheckUploadServiceEvent result) {
        if (!Utils.isServiceRunning(BrowserActivity.this, "com.seafile.seadroid2.cameraupload.MediaObserverService")) {
            mediaObserver = new Intent(this, MediaObserverService.class);
            startService(mediaObserver);
            syncCamera();
            Log.d(DEBUG_TAG, "onEvent============false ");
        } else {
            Log.d(DEBUG_TAG, "onEvent============true ");
        }

        if (!Utils.isServiceRunning(BrowserActivity.this, "com.seafile.seadroid2.monitor.FileMonitorService")) {
            monitorIntent = new Intent(this, FileMonitorService.class);
            startService(monitorIntent);
            Log.d(DEBUG_TAG, "FileMonitorService============false ");
        }

        if (!Utils.isServiceRunning(BrowserActivity.this, "com.seafile.seadroid2.folderbackup.FolderBackupService")) {
            monitorIntent = new Intent(this, FolderBackupService.class);
            startService(monitorIntent);
            Log.d(DEBUG_TAG, "FolderBackupService============false ");
        }

    }

    public class ScreenOffReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                UnlockActivity.g_SCREEN_OFF = true;
            }
        }

    }

    private void onKeyboardVisibilityChanged(boolean opened) {
        librariesContainer.setVisibility(opened? View.GONE : View.VISIBLE);
        bottomBarLayout.setVisibility(opened? View.GONE : View.VISIBLE);
    }

    private void attachKeyboardListeners() {
        if (keyboardListenersAttached) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mContainer.getRootView().setWindowInsetsAnimationCallback(new WindowInsetsAnimation.Callback(WindowInsetsAnimation.Callback.DISPATCH_MODE_STOP) {

                @Override
                public WindowInsets onProgress(@NonNull WindowInsets insets, @NonNull List<WindowInsetsAnimation> runningAnimations) {
                    boolean showingKeyboard = mContainer.getRootWindowInsets().isVisible(WindowInsets.Type.ime());
                    onKeyboardVisibilityChanged(showingKeyboard);
                    return null;
                }

                @Override
                public void onEnd(@NonNull WindowInsetsAnimation animation) {
                    boolean showingKeyboard = mContainer.getRootWindowInsets().isVisible(WindowInsets.Type.ime());
                    onKeyboardVisibilityChanged(showingKeyboard);
                    super.onEnd(animation);
                }
            });
        }
        keyboardListenersAttached = true;
        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
    }

    public void viewRepoMenuCard(boolean flag) {
        personalMenuCard.setVisibility(flag? View.VISIBLE : View.GONE);
        groupMenuCard.setVisibility(flag? View.VISIBLE : View.GONE);
        sharedMenuCard.setVisibility(flag? View.VISIBLE : View.GONE);
    }

    public void updateBottomBarSize(int bottomBarSize) {
        if (bottomBarSize == SettingsManager.BOTTOM_BAR_NONE) {
            bottomBarLayout.setVisibility(View.GONE);
            librariesContainer.setVisibility(View.GONE);
        } else {
            View starredImage = findViewById(R.id.starredImage);
            View activitiesImage = findViewById(R.id.activitiesImage);
            View accountsImage = findViewById(R.id.accountsImage);
            View settingsImage = findViewById(R.id.settingsImage);
            View librariesImage = findViewById(R.id.librariesImage);

            float density = getResources().getDisplayMetrics().density;

            int[] bottomBarCenterButtonWidths = density > 1.5 ? new int[] {88, 58, 30} : new int[] {66, 48, 30};
            int[] bottomBarSideIconWidths = density > 1.5 ? new int[] {48, 32, 16} : new int[] {36, 26, 16};
            int[] plusLibraryCardWidths = density > 1.5 ? new int[] {30, 30, 23} : new int[] {24, 24, 20};


            int bottomBarSideButtonHeight = Utils.getPixels(this, bottomBarCenterButtonWidths[bottomBarSize] - 3);
            ViewGroup.LayoutParams params = bottomBarLayout.getLayoutParams();
            params.height = bottomBarSideButtonHeight;
            bottomBarLayout.setLayoutParams(params);
            bottomBarLayout.requestLayout();


            int bottomBarSideIconWidth = Utils.getPixels(this, bottomBarSideIconWidths[bottomBarSize]);
            updateViewWidthAndHeight(starredImage, bottomBarSideIconWidth);
            updateViewWidthAndHeight(activitiesImage, bottomBarSideIconWidth);
            updateViewWidthAndHeight(accountsImage, bottomBarSideIconWidth);
            updateViewWidthAndHeight(settingsImage, bottomBarSideIconWidth);
            updateViewWidthAndHeight(librariesImage, bottomBarSideIconWidth);


            int bottomBarCenterButtonWidth = Utils.getPixels(this, bottomBarCenterButtonWidths[bottomBarSize]);
            updateViewWidthAndHeight(librariesCard, bottomBarCenterButtonWidth);
            librariesCard.setRadius(((float) bottomBarCenterButtonWidth) / 2);

            int bottomBarCenterButtonContainerWidth = Utils.getPixels(this, bottomBarCenterButtonWidths[bottomBarSize] + 2);
            float bottomBarCenterButtonContainerRadius = ((float) bottomBarCenterButtonContainerWidth) / 2;
            librariesCardContainer.setRadius(bottomBarCenterButtonContainerRadius);

            int bottomBarCenterContainerHeight = Utils.getPixels(this, bottomBarCenterButtonWidths[bottomBarSize] + 2 + 5);
            updateViewWidthAndHeight(librariesContainer, bottomBarCenterButtonContainerWidth * (bottomBarSize == 2 ? 2 : 1), bottomBarCenterContainerHeight);

            View activitiesCardView = findViewById(R.id.activitiesCardView);
            activitiesCardView.setPadding(0, 0, (int) bottomBarCenterButtonContainerRadius, 0);

            View accountsCardView = findViewById(R.id.accountsCardView);
            accountsCardView.setPadding((int) bottomBarCenterButtonContainerRadius, 0, 0, 0);

            int plusLibraryCardWidth = Utils.getPixels(this, plusLibraryCardWidths[bottomBarSize]);
            updateViewWidthAndHeight(plusLibraryCard, plusLibraryCardWidth);
            plusLibraryCard.setRadius((float) plusLibraryCardWidth / 2);

            bottomBarLayout.setVisibility(View.VISIBLE);
            librariesContainer.setVisibility(View.VISIBLE);
        }
    }

    private void updateViewWidthAndHeight(View view, int value) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = value;
        params.width = value;
        view.setLayoutParams(params);
        view.requestLayout();
    }

    private void updateViewWidthAndHeight(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        params.width = width;
        view.setLayoutParams(params);
        view.requestLayout();
    }

    private void startGalleryDialog(String repoName, String repoId, String path, String fileName) {
        List<SeafDirent> seafDirents = dataManager.getCachedDirents(repoId, path);
        if (seafDirents != null) {
            for (SeafDirent seafDirent : seafDirents) {
                if (!seafDirent.isDir() && Utils.isViewableImage(seafDirent.name)) {
                    for (DownloadTaskInfo info:txService.getAllDownloadTaskInfos()) {
                        if (info.repoID.equals(repoId) && info.pathInRepo.equals(Utils.pathJoin(path, seafDirent.name))) {
                            getTransferFragment().deleteSelectedItem(info);
                        }
                    }
                }
            }
        }

        galleryDialog = new GalleryDialog();
        galleryDialog.init(this, repoId, repoName, path, fileName);
        galleryDialog.show(getSupportFragmentManager(), BrowserActivity.GALLERY_DIALOG_FRAGMENT_TAG);
    }

    public void closeGalleryDialog() {
        if (galleryDialog != null) {
            galleryDialog.dismiss();
            galleryDialog = null;
        }
    }

    private class CreateLogRepo extends AsyncTask<Void, Void, Void> {
        private ArrayList<Uri> uriList;
        private SeafException err;

        public CreateLogRepo(ArrayList<Uri> uriList) {
            this.uriList = uriList;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(String.format(getResources().getString(R.string.creating_log_library), logRepoName));
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                dataManager.createNewRepo(logRepoName, "");
            } catch (SeafException e) {
                err = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (err != null) {
                progressDialog.dismiss();
                showShortToast(BrowserActivity.this, String.format(getResources().getString(R.string.failed_create_log_library), logRepoName));
            }
            ConcurrentAsyncTask.execute(new LoadTask(uriList));
        }
    }

    private class CreateLogDirent extends AsyncTask<Void, Void, Void> {
        private String logRepoId;
        private ArrayList<Uri> uriList;
        private String logDirentName;
        private SeafException err;

        public CreateLogDirent(String repoId, String logDirentName, ArrayList<Uri> uriList) {
            this.logRepoId = repoId;
            this.logDirentName = logDirentName;
            this.uriList = uriList;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(String.format(getResources().getString(R.string.creating_log_folder), logDirentName));
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                dataManager.createNewDir(logRepoId, "/", logDirentName);
            } catch (SeafException e) {
                err = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (err != null) {
                progressDialog.dismiss();
                showShortToast(BrowserActivity.this, R.string.failed_create_log_folder);
                return;
            }

            getReposFragment().refresh();

            ConcurrentAsyncTask.execute(new LoadDirTask(logRepoId, uriList, "/" + logDirentName));
        }
    }

    private class LoadTask extends AsyncTask<Void, Void, List<SeafRepo>> {
        SeafException err = null;
        ArrayList<Uri> uriList;

        public LoadTask(ArrayList<Uri> uriList) {
            this.uriList = uriList;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<SeafRepo> doInBackground(Void... params) {
            try {
                return dataManager.getReposFromServer();
            } catch (SeafException e) {
                err = e;
                return null;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<SeafRepo> rs) {
            if (err != null) {
                progressDialog.dismiss();
                showShortToast(BrowserActivity.this, R.string.error_when_load_repos);
                return;
            }
            for (SeafRepo repo : rs) {
                if (repo.name.equals(logRepoName)) {
                    String logDirentName = new SimpleDateFormat("HH_mm_ss-dd.MM.yyyy").format(new Date());
                    ConcurrentAsyncTask.execute(new CreateLogDirent(repo.id, logDirentName, uriList));
                    return;
                }
            }
        }
    }

    private class LoadDirTask extends AsyncTask<String, Void, List<SeafDirent>> {

        SeafException err = null;
        String logRepoId;
        String logDirentName;
        ArrayList<Uri> uriList;
        String myPath;

        public LoadDirTask(String logRepoId, ArrayList<Uri> uriList, String myPath) {
            this.logRepoId = logRepoId;
            this.uriList = uriList;
            this.myPath = myPath;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(
                    String.format(getResources().getString(R.string.loading_repo_dirent),
                            myPath.equals("/") ?
                                    logRepoName : Utils.removeFirstPathSeperator(myPath)));
        }

        @Override
        protected List<SeafDirent> doInBackground(String... params) {
            try {
                List<SeafDirent> dirents = dataManager.getDirentsFromServer(logRepoId, myPath);
                for (SeafDirent sd : dirents) {
                    if (!sd.isDir()) {
                        SeafCachedFile scf = dataManager.getCachedFile(logRepoName, logRepoId, myPath);
                        if (scf != null && scf.getSize() != sd.getFileSize()) {
                            dataManager.removeCachedFile(scf);
                        }
                    }
                }
                return dirents;
            } catch (SeafException e) {
                err = e;
                return null;
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<SeafDirent> dirents) {
            if (err != null) {
                progressDialog.dismiss();
                showShortToast(BrowserActivity.this, R.string.error_when_load_dirents);
            }

            if (myPath.equals("/")) {
                logDirentName = new SimpleDateFormat("HH_mm_ss-dd.MM.yyyy").format(new Date());
                for (SeafDirent sd : dirents) {
                    if (sd.name.equals(logDirentName)) {
                        ConcurrentAsyncTask.execute(new LoadDirTask(logRepoId, uriList, "/" + logDirentName));
                        return;
                    }
                }

                ConcurrentAsyncTask.execute(new CreateLogDirent(logRepoId, logDirentName, uriList));
                return;
            }

            progressDialog.setMessage(getResources().getString(R.string.uploading_log));
            logNameList.clear();
            for (Uri uri:uriList) {
                logNameList.add(Utils.fileNameFromPath(uri.getPath()));
            }
            uploadFilesFromLocal(uriList, logRepoId, logRepoName, myPath);
        }
    }

    public class GetDownloadLinkTask extends AsyncTask<Void, Long, Void> {
        String repoID;
        String path;
        SeafLink seafLink;
        private Exception err;

        public GetDownloadLinkTask(String repoID, String source) {
            this.repoID = repoID;
            this.path = source;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(String.format(getString(R.string.getting_download_link), Utils.fileNameFromPath(path)));
        }

        @Override
        public Void doInBackground(Void... params) {
            try {
                //get share link
                ArrayList<SeafLink> shareLinks = new ArrayList<SeafLink>();
                shareLinks = dataManager.getShareLink(repoID, path);
                if (shareLinks.size() != 0) {
                    //return to existing link
                    seafLink = shareLinks.get(0);
                }
            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            if (err != null) {
                progressDialog.dismiss();
                showShortToast(BrowserActivity.this, R.string.unknow_error);
                return;
            }
            if (seafLink == null) {
                ConcurrentAsyncTask.execute(new CreateDownloadLinkTask(repoID, path));
                return;
            }
            progressDialog.dismiss();
            showCopyLinkDialog(seafLink, path);
        }
    }

    public class CreateDownloadLinkTask extends AsyncTask<Void, Long, Void> {
        private SeafLink seafLink;
        private String repoID;
        private String path;
        private Exception err;

        public CreateDownloadLinkTask(String repoID, String path) {
            this.repoID = repoID;
            this.path = path;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(String.format(getString(R.string.creating_download_link), Utils.fileNameFromPath(path)));
        }

        @Override
        public Void doInBackground(Void... params) {
            try {
                SeafConnection conn = new SeafConnection(account);
                seafLink = conn.createShareLink(repoID, path, "", "", "", "{\"can_edit\":false,\"can_download\":true,\"can_upload\":false}");
            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            progressDialog.dismiss();
            if (err != null || seafLink == null) {
                showShortToast(BrowserActivity.this, String.format(getResources().getString(R.string.failed_create_download_link), Utils.fileNameFromPath(path)));
                return;
            }
            showCopyLinkDialog(seafLink, path);
        }
    }

    private void showCopyLinkDialog(SeafLink seafLink, String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.dialog_copy_download_link, null);

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        TextView pathText = (TextView) view.findViewById(R.id.path_text);
        CardView copyCard = (CardView) view.findViewById(R.id.copy_card);
        CardView okCard = (CardView) view.findViewById(R.id.single_ok_card);
        TextView okText = (TextView) view.findViewById(R.id.single_ok_text);

        pathText.setText(Utils.pathJoin(logRepoName, path));
        okText.setText(R.string.close);

        copyCard.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(seafLink.getLink());
            showShortToast(this, R.string.link_ready_to_be_pasted);
            dialog.dismiss();
        });
        okCard.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateNeedUpdateThumbsStatusOfEncRepo(String repoID) {
        if (dataManager.getCachedRepoByID(repoID).canLocalDecrypt()) {
            needUpdateThumbsStatusOfEncRepo = true;
        }
    }

    private void startOfficeActivity(SelectedFileInfo fileInfo) {
        Intent intent = new Intent(this, OfficeActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("selectedFileInfo", SelectedFileInfo.toString(fileInfo));
        startActivity(intent);
    }

    private void showOpenOfficeDialog(boolean isOpenWith, SelectedFileInfo fileInfo) {
        final OpenOfficeDialog dialog = new OpenOfficeDialog(this, fileInfo, isOpenWith);
        dialog.show(getSupportFragmentManager(), "OpenOfficeDialog");
    }

    public void selectOpenOffice(int openOfficeValue, AlertDialog dialog, boolean save, SelectedFileInfo fileInfo, boolean isOpenWith) {
        if (mOpenOfficeDropDown != null) {
            if (mOpenOfficeDropDown.isShowing()) {
                mOpenOfficeDropDown.dismiss();
            }
        }
        SettingsManager settingsMgr = SettingsManager.instance();
        if (openOfficeValue != settingsMgr.getOpenOfficeValue()) {
            if (save) {
                getSettingsFragment().updateOpenOffice(openOfficeValue);
            }
            if (openOfficeValue != SettingsManager.OPEN_OFFICE_OPTIONAL) {
                processSelectedFile(isOpenWith, fileInfo, openOfficeValue);
                dialog.dismiss();
            }
        }
    }

    public void showOpenPdfDialog(boolean isOpenWith, File file) {
        final OpenPdfDialog dialog = new OpenPdfDialog(this, file, isOpenWith);
        dialog.show(getSupportFragmentManager(), "OpenPdfDialog");
    }

    public void selectOpenPdf(int openPdfValue, AlertDialog dialog, boolean save, File file, boolean isOpenWith) {
        if (mOpenPdfDropDown != null) {
            if (mOpenPdfDropDown.isShowing()) {
                mOpenPdfDropDown.dismiss();
            }
        }
        SettingsManager settingsMgr = SettingsManager.instance();
        if (openPdfValue != settingsMgr.getOpenPdfValue()) {
            if (save) {
                getSettingsFragment().updateOpenPdf(openPdfValue);
            }
            if (openPdfValue != SettingsManager.OPEN_PDF_OPTIONAL) {
                WidgetUtils.showFile(this, file, openPdfValue, isOpenWith);
                dialog.dismiss();
            }
        }
    }

    private int getOpenValue(String path) {
        if (Utils.isOfficeMimeType(path)) {
            return SettingsManager.instance().getOpenOfficeValue();
        }
        if (Utils.isPdfMimeType(path)) {
            return SettingsManager.instance().getOpenPdfValue();
        }
        return 0;
    }

    private class DeleteCacheAutomaticTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            StorageManager storageManager = StorageManager.getInstance();
            File mediaDir = storageManager.getMediaDir();
            File thumbDir = storageManager.getThumbnailsDir();
            if (!mediaDir.exists() || !thumbDir.exists())
                return null;

            long maxCacheBytes = (long) SettingsManager.instance().getCacheMaximumSize() * 1024 * 1024 * 1024;
            long usedSpace = FileUtils.sizeOfDirectory(mediaDir) + FileUtils.sizeOfDirectory(thumbDir);

            long bytesToFree = usedSpace - maxCacheBytes;
            if (bytesToFree <= 0) return null;

            Comparator<File> FILE_COMPARATOR = (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                PriorityQueue<File> mediaQueue = new PriorityQueue<>(FILE_COMPARATOR);
                PriorityQueue<File> thumbQueue = new PriorityQueue<>(FILE_COMPARATOR);

                collectFilesOverN(mediaDir, mediaQueue);
                collectFilesOverN(thumbDir, thumbQueue);

                while (bytesToFree > 0 && (!mediaQueue.isEmpty() || !thumbQueue.isEmpty())) {

                    File mediaFile = mediaQueue.peek();
                    File thumbFile = thumbQueue.peek();

                    File fileToDelete;

                    if (mediaFile == null) {
                        fileToDelete = thumbQueue.poll();
                    } else if (thumbFile == null) {
                        fileToDelete = mediaQueue.poll();
                    } else {
                        fileToDelete = mediaFile.lastModified() <= thumbFile.lastModified()
                                ? mediaQueue.poll()
                                : thumbQueue.poll();
                    }

                    if (fileToDelete == null) break;

                    long size = fileToDelete.length();
                    if (fileToDelete.delete()) {
                        bytesToFree -= size;
                    }
                }
            } else {
                List<File> allFiles = new ArrayList<>();
                collectFilesBelowN(mediaDir, allFiles);
                collectFilesBelowN(thumbDir, allFiles);

                Collections.sort(allFiles, FILE_COMPARATOR);

                for (File file : allFiles) {
                    if (bytesToFree <= 0) break;

                    long fileSize = file.length();

                    if (file.delete()) {
                        bytesToFree -= fileSize;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {}
    }

    private static void collectFilesOverN(File dir, PriorityQueue<File> queue) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                collectFilesOverN(file, queue);
            } else {
                queue.offer(file);
            }
        }
    }

    private static void collectFilesBelowN(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                collectFilesBelowN(file, fileList);
            } else {
                fileList.add(file);
            }
        }
    }
}