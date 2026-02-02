package com.seafile.seadroid2.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeafConnection;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.account.AccountManager;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafCachedFile;
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.data.SeafGroup;
import com.seafile.seadroid2.data.SeafItem;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.data.SeafRepoTag;
import com.seafile.seadroid2.data.SeafStarredFile;
import com.seafile.seadroid2.play.exoplayer.CustomExoVideoPlayerActivity;
import com.seafile.seadroid2.ssl.CertsManager;
import com.seafile.seadroid2.transfer.DownloadTaskInfo;
import com.seafile.seadroid2.transfer.TransferService;
import com.seafile.seadroid2.ui.FastRecyclerView;
import com.seafile.seadroid2.ui.NavContext;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.activity.FileActivity;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;
import com.seafile.seadroid2.ui.adapter.SeafFileTagAdapter;
import com.seafile.seadroid2.ui.adapter.SeafItemAdapter;
import com.seafile.seadroid2.ui.adapter.SeafTagColorAdapter;
import com.seafile.seadroid2.ui.dialog.SslConfirmDialog;
import com.seafile.seadroid2.ui.dialog.TaskDialog;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReposFragment extends Fragment {

    private static final String DEBUG_TAG = "ReposFragment";
    private static final String KEY_REPO_SCROLL_POSITION = "repo_scroll_position";

    private static final int REFRESH_ON_RESUME = 0;
    private static final int REFRESH_ON_PULL = 1;
    private static final int REFRESH_ON_CLICK = 2;
    private static final int REFRESH_ON_OVERFLOW_MENU = 3;
    private static int mRefreshType = -1;
    /**
     * flag to stop refreshing when nav to other directory
     */
    private static int mPullToRefreshStopRefreshing = 0;

    private SeafItemAdapter adapter;
    private BrowserActivity mActivity = null;
    private Boolean mActionMode = false;
    private Map<String, ScrollState> scrollPostions;

    private SwipeRefreshLayout refreshLayout;
    public FastRecyclerView mRecyclerView;
    private View mNoReposContainer;
    private TextView mNoReposTitle;
    private TextView mNoReposDesc;
    private ImageView mEmptyView;
    public View mProgressContainer;
    private View mListContainer;
    public View mSearchLayout;
    private EditText mSearchText;
    private CardView mSearchBtn;
    private ImageButton mTextClearBtn;
    private TextView mErrorText;
    private View mActionModeLayout;
    private TextView mActionModeTitleText;
    private CardView mActionModeDeleteCard;
    private CardView mActionModeCopyCard;
    private CardView mActionModeMoveCard;
    private CardView mActionModeDownloadCard;
    private CardView mActionModeSelectAllCard;
    private CardView mActionModeCloseCard;
    private boolean allItemsSelected;

    private CardView mSettingCard;
    private LinearLayout mCollapseLayout;
    private ImageView mDirectionImage;
    private SwitchCompat mLocationSwitch;
    private RadioGroup mLocationRadioGroup;
    private RadioButton mSearchAllLibrariesRB;
    private RadioButton mSearchCurrentLibraryRB;
    private RadioButton mSearchCurrentFolderRB;
    private SwitchCompat mAllFileTypesSwitch;
    private SwitchCompat mCustomFileTypesSwitch;
    private LinearLayout mCustomFileTypesLayout;
    private CheckBox mImageCheckBox;
    private CheckBox mPdfCheckBox;
    private CheckBox mVideoCheckBox;
    private CheckBox mMarkdownCheckBox;
    private CheckBox mTextCheckBox;
    private CheckBox mAudioCheckBox;
    private CheckBox mDocumentCheckBox;
    private EditText mCustomFileTypesEdit;
    private SwitchCompat mLastChangesSwitch;
    private LinearLayout mLastChangesLayout;
    private EditText mStartTimeEdit;
    private CardView mStartTimeCard;
    private EditText mEndTimeEdit;
    private CardView mEndTimeCard;
    private SwitchCompat mSizeSwitch;
    private LinearLayout mSizeLayout;
    private LinearLayout mMinSizeLayout;
    private EditText mMinSizeEdit;
    private CardView mMinSizeUnitCard;
    private TextView mMinSizeUnitText;
    private LinearLayout mMaxSizeLayout;
    private EditText mMaxSizeEdit;
    private CardView mMaxSizeUnitCard;
    private TextView mMaxSizeUnitText;
    private View mPopupSelectDatetime2View;
    private View mPopupSelectUnitView;

    private AccountManager accountManager;
    private DataManager dataManager;
    private Account account;
    private List<SeafDirent> mSearchedFiles = new ArrayList<SeafDirent>();
    private int currentReposSize = 0;
    private Boolean collapse = true;
    private PopupWindow mDateDropDown = null;
    private PopupWindow mUnitDropDown = null;
    private Calendar startCalendar;
    private Calendar endCalendar;
    private SimpleDateFormat simpleDateFormatForPhone;

    private Map<String, String> ftypeMap = Maps.newHashMap();
    private Map<String, Long> unitMap = Maps.newHashMap();

    private boolean isTimerStarted;
    private final Handler mTimer = new Handler();

    private DataManager getDataManager() {
        return mActivity.getDataManager();
    }

    private NavContext getNavContext() {
        return mActivity.getNavContext();
    }

    public SeafItemAdapter getAdapter() {
        return adapter;
    }

    public ImageView getEmptyView() {
        return mEmptyView;
    }

    private Dialog addRepoTagDialog;

    private Dialog fileTagsDialog;
    private SeafFileTagAdapter fileTagAdapter;

    private int viewableImagesCountInEnc = 0;
    public int thumbnailImagesCountInEnc = 0;
    private List<String> downloadEncRepoIds = new ArrayList<>();

    public int searchEncReposSize = -1;
    private List<String> mSearchedImages = new ArrayList<String>();
    private List<String> mSearchedThumbImages = null;
    private boolean loadReposFromServer = false;

    public interface OnFileSelectedListener {
        void onFileSelected(SeafDirent fileName);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(DEBUG_TAG, "ReposFragment Attached");
        mActivity = (BrowserActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.repos_fragment, container, false);

        initView(root);
        initViewAction();
        init();

        return root;
    }

    private void initView(View root) {
        refreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swiperefresh);
        mRecyclerView = (FastRecyclerView) root.findViewById(R.id.recycler_view);
        mNoReposContainer = root.findViewById(R.id.no_repos_container);
        mNoReposTitle = root.findViewById(R.id.no_repos_title);
        mNoReposDesc = root.findViewById(R.id.no_repos_desc);
        mEmptyView = (ImageView) root.findViewById(R.id.empty);
        mListContainer = root.findViewById(R.id.listContainer);
        mSearchLayout = root.findViewById(R.id.search_layout);
        mSearchText = root.findViewById(R.id.et_content);
        mSearchBtn = root.findViewById(R.id.btn_search);
        mTextClearBtn = root.findViewById(R.id.btn_clear);
        mErrorText = (TextView) root.findViewById(R.id.error_message);
        mProgressContainer = root.findViewById(R.id.progressContainer);
        mActionModeLayout = root.findViewById(R.id.action_mode_layout);
        mActionModeTitleText = mActionModeLayout.findViewById(R.id.action_mode_title_text);
        mActionModeDeleteCard = mActionModeLayout.findViewById(R.id.action_mode_delete_card);
        mActionModeCopyCard = mActionModeLayout.findViewById(R.id.action_mode_copy_card);
        mActionModeMoveCard = mActionModeLayout.findViewById(R.id.action_mode_move_card);
        mActionModeDownloadCard = mActionModeLayout.findViewById(R.id.action_mode_download_card);
        mActionModeSelectAllCard = mActionModeLayout.findViewById(R.id.action_mode_select_all_card);
        mActionModeCloseCard = mActionModeLayout.findViewById(R.id.action_mode_close_card);

        mSettingCard = (CardView) root.findViewById(R.id.setting_card);
        mCollapseLayout = (LinearLayout) root.findViewById(R.id.collapse_layout);
        mDirectionImage = (ImageView) root.findViewById(R.id.direction_image);
        mLocationSwitch = (SwitchCompat) root.findViewById(R.id.location_switch);
        mLocationRadioGroup = (RadioGroup) root.findViewById(R.id.location_radio_group);
        mSearchAllLibrariesRB = (RadioButton) root.findViewById(R.id.search_all_libraries_rb);
        mSearchCurrentLibraryRB = (RadioButton) root.findViewById(R.id.search_current_library_rb);
        mSearchCurrentFolderRB = (RadioButton) root.findViewById(R.id.search_current_folder_rb);
        mAllFileTypesSwitch = (SwitchCompat) root.findViewById(R.id.all_file_types_switch);
        mCustomFileTypesSwitch = (SwitchCompat) root.findViewById(R.id.custom_file_types_switch);
        mCustomFileTypesLayout = (LinearLayout) root.findViewById(R.id.custom_file_types_layout);
        mImageCheckBox = (CheckBox) root.findViewById(R.id.image_checkbox);
        mPdfCheckBox = (CheckBox) root.findViewById(R.id.pdf_checkbox);
        mVideoCheckBox = (CheckBox) root.findViewById(R.id.video_checkbox);
        mMarkdownCheckBox = (CheckBox) root.findViewById(R.id.markdown_checkbox);
        mTextCheckBox = (CheckBox) root.findViewById(R.id.text_checkbox);
        mAudioCheckBox = (CheckBox) root.findViewById(R.id.audio_checkbox);
        mDocumentCheckBox = (CheckBox) root.findViewById(R.id.document_checkbox);
        mCustomFileTypesEdit = (EditText) root.findViewById(R.id.custom_file_types_edit);
        mLastChangesSwitch = (SwitchCompat) root.findViewById(R.id.last_changes_switch);
        mLastChangesLayout = (LinearLayout) root.findViewById(R.id.last_changes_layout);
        mStartTimeEdit = (EditText) root.findViewById(R.id.start_time_edit);
        mStartTimeCard = (CardView) root.findViewById(R.id.start_time_card);
        mEndTimeEdit = (EditText) root.findViewById(R.id.end_time_edit);
        mEndTimeCard = (CardView) root.findViewById(R.id.end_time_card);
        mSizeSwitch = (SwitchCompat) root.findViewById(R.id.size_switch);
        mSizeLayout = (LinearLayout) root.findViewById(R.id.size_layout);
        mMinSizeLayout = (LinearLayout) root.findViewById(R.id.min_size_layout);
        mMinSizeEdit = (EditText) root.findViewById(R.id.min_size_edit);
        mMinSizeUnitCard = (CardView) root.findViewById(R.id.min_size_unit_card);
        mMinSizeUnitText = (TextView) root.findViewById(R.id.min_size_unit_text);
        mMaxSizeLayout = (LinearLayout) root.findViewById(R.id.max_size_layout);
        mMaxSizeEdit = (EditText) root.findViewById(R.id.max_size_edit);
        mMaxSizeUnitCard = (CardView) root.findViewById(R.id.max_size_unit_card);
        mMaxSizeUnitText = (TextView) root.findViewById(R.id.max_size_unit_text);
        mPopupSelectDatetime2View = (View) root.findViewById(R.id.popup_select_datetime2_layout);
        mPopupSelectUnitView = (View) root.findViewById(R.id.popup_select_unit_layout);
    }

    private void initViewAction() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshType = REFRESH_ON_PULL;
                refreshView(true, true);
            }
        });

        mSearchText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mSearchText.getText().toString().isEmpty()) {
                    mTextClearBtn.setVisibility(View.VISIBLE);
                    mSearchBtn.setVisibility(View.VISIBLE);
                } else {
                    mTextClearBtn.setVisibility(View.GONE);
                    mSearchBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mSearchBtn.callOnClick();
                    return true;
                }
                return false;
            }
        });

        mSearchBtn.setOnClickListener(v -> {
            collapse = true;
            upgradeCollapse();
            handleSearch(0);
        });

        mTextClearBtn.setOnClickListener(v -> mSearchText.getText().clear());

        mSettingCard.setOnClickListener(v -> {
            collapse = !collapse;
            upgradeCollapse();
        });

        mStartTimeCard.setOnClickListener(v -> showSelectDatePopup(true));

        mEndTimeCard.setOnClickListener(v -> showSelectDatePopup(false));

        mMinSizeUnitCard.setOnClickListener(v -> showSelectUnitPopup(true));

        mMaxSizeUnitCard.setOnClickListener(v -> showSelectUnitPopup(false));

        mLocationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mLocationRadioGroup.setVisibility(isChecked? View.VISIBLE : View.GONE);
        });

        mAllFileTypesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mCustomFileTypesSwitch.setChecked(!isChecked);
        });

        mCustomFileTypesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mAllFileTypesSwitch.setChecked(!isChecked);
            mCustomFileTypesLayout.setVisibility(isChecked? View.VISIBLE : View.GONE);
        });

        mLastChangesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mLastChangesLayout.setVisibility(isChecked? View.VISIBLE : View.GONE);
        });

        mSizeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mSizeLayout.setVisibility(isChecked? View.VISIBLE : View.GONE);
        });
    }

    private void init() {
        if (BrowserActivity.searchVisibility) {
            mSearchLayout.setVisibility(View.VISIBLE);
        } else {
            mSearchLayout.setVisibility(View.GONE);
        }

        refreshLayout.setColorSchemeResources(R.color.luckycloud_green);
        setToolbarTitle(getResources().getString(R.string.libraries));

        accountManager = new AccountManager(mActivity);
        account = accountManager.getCurrentAccount();
        dataManager = getDataManager();

        mLocationSwitch.setChecked(true);
        mAllFileTypesSwitch.setChecked(true);
        mCustomFileTypesSwitch.setChecked(false);
        mLastChangesSwitch.setChecked(false);
        mSizeSwitch.setChecked(false);

        Calendar currentCalendar = Calendar.getInstance();
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        startCalendar.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        endCalendar.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        simpleDateFormatForPhone = new SimpleDateFormat("yyyy-MM-dd");

        ftypeMap.put("Text", "'ac', 'am', 'bat', 'c', 'cc', 'cmake', 'cpp', 'cs', 'css', 'diff', 'el', 'h', 'html', 'htm', 'java', 'js', 'json', 'less', 'make', 'org', 'php', 'pl', 'properties', 'py', 'rb', 'scala', 'script', 'sh', 'sql', 'txt', 'text', 'tex', 'vi', 'vim', 'xhtml', 'xml', 'log', 'csv', 'groovy', 'rst', 'patch', 'go'");
        ftypeMap.put("Document", "'doc', 'docx', 'ppt', 'pptx', 'odt', 'fodt', 'odp', 'fodp'");
        ftypeMap.put("Image", "'gif', 'jpeg', 'jpg', 'png', 'ico', 'bmp', 'tif', 'tiff', 'eps'");
        ftypeMap.put("Video", "'mp4', 'ogv', 'webm', 'mov'");
        ftypeMap.put("Audio", "'mp3', 'oga', 'ogg'");
        ftypeMap.put("PDF", "'pdf'");
        ftypeMap.put("Markdown", "'markdown', 'md'");

        unitMap.put("B", (long)1);
        unitMap.put("KB", (long)1024);
        unitMap.put("MB", (long)1024 * 1024);
        unitMap.put("GB", (long)1024 * 1024 * 1024);
        unitMap.put("TB", (long)1024 * 1024 * 1024 * 1024);
    }

    private void setToolbarTitle(String title) {
        if (mActivity.currentPosition == BrowserActivity.INDEX_LIBRARY_TAB) {
            mActivity.setToolbarTitle(title);
        }
    }

    /**
     * Start action mode for selecting and process multiple files/folders.
     * The contextual action mode is a system implementation of ActionMode
     * that focuses user interaction toward performing contextual actions.
     * When a user enables this mode by selecting an item,
     * a contextual action bar appears at the top of the screen
     * to present actions the user can perform on the currently selected item(s).
     * <p>
     * While this mode is enabled,
     * the user can select multiple items (if you allow it), deselect items,
     * and continue to navigate within the activity (as much as you're willing to allow).
     * <p>
     * The action mode is disabled and the contextual action bar disappears
     * when the user deselects all items, presses the BACK button, or selects the Done action on the left side of the bar.
     * <p>
     * see http://developer.android.com/guide/topics/ui/menus.html#CAB
     */
    public void startContextualActionMode(int position) {
        startContextualActionMode();

        NavContext nav = getNavContext();
        if (adapter == null) return;

        if (!nav.inRepo() && !mActivity.isSearchMode) return;

        adapter.toggleSelection(position);

    }

    public void startContextualActionMode() {
        NavContext nav = getNavContext();
        if (!nav.inRepo()) return;

        if (!mActionMode) {
            // start the actionMode
            startSupportActionMode();
        }
    }

    public void downloadRepo(SeafRepo repo) {
        if (repo.encrypted && !getDataManager().getRepoPasswordSet(repo.id)) {
            String password = getDataManager().getRepoPassword(repo.id);
            mActivity.showPasswordDialog(repo.name, repo.id,
                    new TaskDialog.TaskDialogListener() {
                        @Override
                        public void onTaskSuccess() {
                            downloadRepo(repo);
                        }
                    }, password);

            return;
        }

        ConcurrentAsyncTask.execute(new LoadDirTask(getDataManager(), true),
                repo.getName(),
                repo.getID(),
                "/");
    }

    public void showRepoBottomSheet(final SeafRepo repo) {

        Dialog dialog = Utils.CustomDialog(mActivity);
        dialog.setContentView(R.layout.dialog_sheet_op_repo);

        TextView titleRepoText = dialog.findViewById(R.id.title_repo_text);
        CardView renameRepoCard = dialog.findViewById(R.id.rename_repo_card);
        CardView deleteRepoCard = dialog.findViewById(R.id.delete_repo_card);
        CardView shareRepoCard = dialog.findViewById(R.id.share_repo_card);
        CardView downloadCacheCard = dialog.findViewById(R.id.download_cache_card);
        CardView addFavouritesCard = dialog.findViewById(R.id.add_favourites_card);
        CardView removeFavouritesCard = dialog.findViewById(R.id.remove_favourites_card);

        String repoID = repo.getID();
        titleRepoText.setText(repo.getName());
        renameRepoCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.renameRepo(repoID, repo.getName());
        });
        deleteRepoCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.deleteRepo(repoID, repo.getName());
        });
        downloadCacheCard.setOnClickListener(v -> {
            dialog.dismiss();
            downloadRepo(repo);
        });
        shareRepoCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.showShareDialog(repoID, "/", ShareDialogActivity.SHARE_DIALOG_FOR_REPO, 0, repo.getName());
        });

        if (checkStarred(repoID, "/")) {
            addFavouritesCard.setVisibility(View.GONE);
            removeFavouritesCard.setVisibility(View.VISIBLE);
        } else {
            addFavouritesCard.setVisibility(View.VISIBLE);
            removeFavouritesCard.setVisibility(View.GONE);
        }
        addFavouritesCard.setOnClickListener(v -> {
            dialog.dismiss();
            SeafDirent dirent = new SeafDirent();
            dirent.name = repo.name;
            dirent.mtime = repo.mtime;
            dirent.type = SeafDirent.DirentType.DIR;
            dirent.size = 0;
            mActivity.starFile(repoID, "/", "", dirent);
        });
        removeFavouritesCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.unStarFile(repoID, "/");
        });

        dialog.show();
    }

    public void showFileBottomSheet(String title, final SeafDirent dirent) {
        String repoName;
        String repoID;
        String dir;
        String path;
        if (dirent.isSearchedFile) {
            SeafRepo repo = dataManager.getCachedRepoByID(dirent.repoID);
            repoName = repo.name;
            repoID = repo.id;
            dir = Utils.pathSplit(dirent.path, dirent.name);
            path = dirent.path;
        } else {
            repoName = getNavContext().getRepoName();
            repoID = getNavContext().getRepoID();
            dir = getNavContext().getDirPath();
            path = Utils.pathJoin(dir, dirent.name);
        }
        final String filename = dirent.name;
        final String localPath = getDataManager().getLocalRepoFile(repoName, repoID, path).getPath();

        Dialog dialog = Utils.CustomDialog(mActivity);
        dialog.setContentView(R.layout.dialog_sheet_op_file);

        TextView titleText = dialog.findViewById(R.id.title_text);
        TextView pathText = dialog.findViewById(R.id.path_text);
        TextView subtitleText = dialog.findViewById(R.id.subtitle_text);
        CardView convertToPdfCard = dialog.findViewById(R.id.convert_to_pdf_card);
        CardView downloadCacheCard = dialog.findViewById(R.id.download_cache_card);
        CardView downloadDeviceCard = dialog.findViewById(R.id.download_device_card);
        CardView openParentDirCard = dialog.findViewById(R.id.open_parent_card);
        CardView deleteCard = dialog.findViewById(R.id.delete_card);
        CardView renameCard = dialog.findViewById(R.id.rename_card);
        CardView copyCard = dialog.findViewById(R.id.copy_card);
        CardView moveCard = dialog.findViewById(R.id.move_card);
        CardView addFavouritesCard = dialog.findViewById(R.id.add_favourites_card);
        CardView removeFavouritesCard = dialog.findViewById(R.id.remove_favourites_card);
        CardView shareCard = dialog.findViewById(R.id.share_card);
        CardView exportCard = dialog.findViewById(R.id.export_card);
        CardView openWithCard = dialog.findViewById(R.id.open_with_card);
        CardView uploadCard = dialog.findViewById(R.id.upload_card);
        CardView tagsCard = dialog.findViewById(R.id.tags_card);
        TextView tagsText = dialog.findViewById(R.id.tags_text);
        CardView closeCard = dialog.findViewById(R.id.close_card);

        openParentDirCard.setVisibility(dirent.isSearchedFile? View.VISIBLE : View.GONE);
        tagsText.setText(getResources().getString(dirent.isSearchedFile ? R.string.create_add_tag : R.string.tags));

        titleText.setText(title);
        pathText.setText(Utils.pathJoin(repoName, path));
        subtitleText.setText(dirent.getSubtitle());

        closeCard.setOnClickListener(v -> {
            dialog.dismiss();
        });
        convertToPdfCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.checkConvertToPdf(repoName, repoID, dir, dirent.name, path);
        });
        downloadCacheCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.downloadFile(repoID, dir, dirent.name, true);
        });
        downloadDeviceCard.setOnClickListener(v -> {
            dialog.dismiss();
            XXPermissions.with(getActivity()).permission("android.permission.MANAGE_EXTERNAL_STORAGE").request(new OnPermissionCallback() {

                @Override
                public void onGranted(List<String> permissions, boolean all) {
                    if (all) {
                        mActivity.downloadFileonDevice(repoID, dir, dirent.name, path);
                    }
                }

                @Override
                public void onDenied(List<String> permissions, boolean never) {
                    if (never) {
                        Toast.makeText(getActivity(), mActivity.getString(R.string.authorization_storage_permission), Toast.LENGTH_LONG).show();
                        XXPermissions.startPermissionActivity(getActivity(), permissions);
                    } else {
                        Toast.makeText(getActivity(), mActivity.getString(R.string.get_storage_permission_failed), Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
        openParentDirCard.setOnClickListener(v -> {
            dialog.dismiss();
            openDir(repoID, repoName, dir, null);
        });
        deleteCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.deleteFile(repoID, repoName, path);
        });
        renameCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.renameFile(repoID, repoName, path);
        });
        copyCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.copyFile(repoID, repoName, dir, filename, false);
        });
        moveCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.moveFile(repoID, repoName, dir, filename, false);
        });

        if (checkStarred(repoID, Utils.pathJoin(dir, filename))) {
            addFavouritesCard.setVisibility(View.GONE);
            removeFavouritesCard.setVisibility(View.VISIBLE);
        } else {
            addFavouritesCard.setVisibility(View.VISIBLE);
            removeFavouritesCard.setVisibility(View.GONE);
        }
        addFavouritesCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.starFile(repoID, dir, filename, dirent);
        });
        removeFavouritesCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.unStarFile(repoID, Utils.pathJoin(dir, filename));
        });

        shareCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.showShareDialog(repoID, path, ShareDialogActivity.SHARE_DIALOG_FOR_FILE, dirent.size, dirent.name);
        });
        exportCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.exportFile(repoID, dir, dirent.name, dirent.size);
        });
        openWithCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.onFileSelected(dirent, true);
        });
        uploadCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.addUpdateTask(repoID, repoName, dir, localPath);
        });
        tagsCard.setOnClickListener(v -> {
            dialog.dismiss();
            selectFileTagsDialog(repoID, dirent, SeafFileTagAdapter.FragmentType.Repos);
        });

        if (!dirent.hasWritePermission()) {
            renameCard.setVisibility(View.GONE);
            deleteCard.setVisibility(View.GONE);
            moveCard.setVisibility(View.GONE);
        }
        if (!Utils.isTextMimeType(filename)) {
            openWithCard.setVisibility(View.GONE);
        }
        SeafRepo repo = getDataManager().getCachedRepoByID(repoID);
        if (repo != null && repo.encrypted) {
            shareCard.setVisibility(View.GONE);
        }

        if (Utils.getExtensionFromFileName(dirent.name).equals(".txt") || Utils.isViewableImage(dirent.name)) {
            convertToPdfCard.setVisibility(View.VISIBLE);
        } else {
            convertToPdfCard.setVisibility(View.GONE);
        }

        SeafCachedFile cf = getDataManager().getCachedFile(repoName, repoID, path);
        if (cf != null) {
            downloadCacheCard.setVisibility(View.GONE);
            uploadCard.setVisibility(View.VISIBLE);
        } else {
            downloadCacheCard.setVisibility(View.VISIBLE);
            uploadCard.setVisibility(View.GONE);
        }

        dialog.show();
    }

    public void selectFileTagsDialog(String repoID, SeafDirent dirent, SeafFileTagAdapter.FragmentType fragmentType) {
        fileTagsDialog = Utils.CustomDialog(mActivity);
        fileTagsDialog.setContentView(R.layout.dialog_select_file_tag);

        CardView closeCard = fileTagsDialog.findViewById(R.id.close_card);
        GridView repoTagsGrid = fileTagsDialog.findViewById(R.id.repo_tags_grid);

        SeafRepo repo = getDataManager().getCachedRepoByID(repoID);
        fileTagAdapter = new SeafFileTagAdapter(mActivity, repo, dirent, fragmentType);
        repoTagsGrid.setAdapter(fileTagAdapter);
        fileTagAdapter.setRepoTags(repo.getRepoTags());
        fileTagAdapter.setFileTags(dirent.getFileTags());
        if (dirent.isSearchedFile) {
            if (repo.repoTags == null)
                ConcurrentAsyncTask.execute(new LoadTag(getDataManager(), repo, true));
            fileTagAdapter.getFileTags();
        }
        fileTagAdapter.notifyChanged();

        closeCard.setOnClickListener(v -> {
            fileTagsDialog.dismiss();
        });

        fileTagsDialog.show();
    }

    public void showDirBottomSheet(String title, final SeafDirent dirent) {
        String repoName;
        String repoID;
        String dir;
        String path;
        if (dirent.isSearchedFile) {
            SeafRepo repo = dataManager.getCachedRepoByID(dirent.repoID);
            repoName = repo.name;
            repoID = repo.id;
            dir = Utils.pathSplit(dirent.path, dirent.name);
            path = dirent.path;
        } else {
            repoName = getNavContext().getRepoName();
            repoID = getNavContext().getRepoID();
            dir = getNavContext().getDirPath();
            path = Utils.pathJoin(dir, dirent.name);
        }
        final String filename = dirent.name;

        Dialog dialog = Utils.CustomDialog(mActivity);
        dialog.setContentView(R.layout.dialog_sheet_op_dir);

        TextView titleText = dialog.findViewById(R.id.title_text);
        TextView pathText = dialog.findViewById(R.id.path_text);
        TextView subtitleText = dialog.findViewById(R.id.subtitle_text);
        CardView shareCard = dialog.findViewById(R.id.share_card);
        CardView deleteCard = dialog.findViewById(R.id.delete_card);
        CardView copyCard = dialog.findViewById(R.id.copy_card);
        CardView moveCard = dialog.findViewById(R.id.move_card);
        CardView renameCard = dialog.findViewById(R.id.rename_card);
        CardView downloadCacheCard = dialog.findViewById(R.id.download_cache_card);
        CardView downloadDeviceCard = dialog.findViewById(R.id.download_device_card);
        CardView addFavouritesCard = dialog.findViewById(R.id.add_favourites_card);
        CardView removeFavouritesCard = dialog.findViewById(R.id.remove_favourites_card);
        CardView closeCard = dialog.findViewById(R.id.close_card);

        titleText.setText(title);
        pathText.setText(Utils.pathJoin(repoName, path));
        subtitleText.setText(dirent.getSubtitle());

        closeCard.setOnClickListener(v -> {
            dialog.dismiss();
        });
        shareCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.showShareDialog(repoID, path, ShareDialogActivity.SHARE_DIALOG_FOR_DIR, dirent.size, dirent.name);
        });
        deleteCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.deleteDir(repoID, repoName, path);
        });
        copyCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.copyFile(repoID, repoName, dir, filename, true);
        });
        moveCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.moveFile(repoID, repoName, dir, filename, true);
        });
        renameCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.renameDir(repoID, repoName, path);
        });
        downloadCacheCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.downloadDir(dir, dirent.name, true, true, false);
        });
        downloadDeviceCard.setOnClickListener(v -> {
            dialog.dismiss();
            XXPermissions.with(getActivity()).permission("android.permission.MANAGE_EXTERNAL_STORAGE").request(new OnPermissionCallback() {

                @Override
                public void onGranted(List<String> permissions, boolean all) {
                    if (all) {
                        mActivity.downloadDir(dir, dirent.name, true, false, false);
                    }
                }

                @Override
                public void onDenied(List<String> permissions, boolean never) {
                    if (never) {
                        Toast.makeText(getActivity(), mActivity.getString(R.string.authorization_storage_permission), Toast.LENGTH_LONG).show();
                        XXPermissions.startPermissionActivity(getActivity(), permissions);
                    } else {
                        Toast.makeText(getActivity(), mActivity.getString(R.string.get_storage_permission_failed), Toast.LENGTH_LONG).show();
                    }
                }
            });
        });

        if (checkStarred(repoID, path)) {
            addFavouritesCard.setVisibility(View.GONE);
            removeFavouritesCard.setVisibility(View.VISIBLE);
        } else {
            addFavouritesCard.setVisibility(View.VISIBLE);
            removeFavouritesCard.setVisibility(View.GONE);
        }
        addFavouritesCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.starFile(repoID, dir, filename, dirent);
        });
        removeFavouritesCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.unStarFile(repoID, Utils.pathJoin(dir, filename));
        });

        if (!dirent.hasWritePermission()) {
            renameCard.setVisibility(View.GONE);
            deleteCard.setVisibility(View.GONE);
            moveCard.setVisibility(View.GONE);
        }
        SeafRepo repo = getDataManager().getCachedRepoByID(repoID);
        if (repo != null && repo.encrypted) {
            shareCard.setVisibility(View.GONE);
        }

        dialog.show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SettingsManager settingsManager = SettingsManager.instance();
        Log.d(DEBUG_TAG, "ReposFragment onActivityCreated");
        scrollPostions = Maps.newHashMap();
        adapter = new SeafItemAdapter(mActivity, this);

        int gridFilesTypePref = settingsManager.getGridFilesTypePref();
        int columns = settingsManager.getGridFilesColumns(gridFilesTypePref);
        boolean repoPersonal = settingsManager.getRepoTypePersonalPref();
        boolean repoGroup = settingsManager.getRepoTypeGroupPref();
        boolean repoShared = settingsManager.getRepoTypeSharedPref();
        GridLayoutManager manager = new GridLayoutManager(mActivity, columns);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItem(position) instanceof SeafGroup) {
                    return columns;
                }
                return 1;
            }
        });
        mRecyclerView.setLayoutManager(manager);
        adapter.setGridFileType(gridFilesTypePref);
        adapter.setRepoType(SettingsManager.REPO_TYPE_PERSONAL, repoPersonal);
        adapter.setRepoType(SettingsManager.REPO_TYPE_GROUP, repoGroup);
        adapter.setRepoType(SettingsManager.REPO_TYPE_SHARED, repoShared);
        mRecyclerView.setAdapter(adapter);
        // mRecyclerView.setDivider(null);
//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    ConcurrentAsyncTask.execute(new LoadVisiableThumbInEncRepo());
//                }
//            }
//        });
    }

    @Override
    public void onStart() {
        // Log.d(DEBUG_TAG, "ReposFragment onStart");
        super.onStart();
    }

    @Override
    public void onStop() {
        // Log.d(DEBUG_TAG, "ReposFragment onStop");
        super.onStop();
        stopTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Log.d(DEBUG_TAG, "ReposFragment onResume");
        // refresh the view (loading data)
        refreshOnResume();
    }

    public void refreshOnResume() {
        mRefreshType = REFRESH_ON_RESUME;
        refreshView(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        mActivity = null;
        // Log.d(DEBUG_TAG, "ReposFragment detached");
        super.onDetach();
    }

    public void refresh() {
        mRefreshType = REFRESH_ON_OVERFLOW_MENU;
        refreshView(true, false);
    }

    public void refreshView() {
        refreshView(false, false);
    }

    public void refreshView(boolean restorePosition) {
        refreshView(false, restorePosition);
    }

    public void refreshView(boolean forceRefresh, boolean restorePosition) {
        if (mActivity == null)
            return;

        mErrorText.setVisibility(View.GONE);
        mListContainer.setVisibility(View.VISIBLE);

//        gridFiles();
        if (mActivity.isSearchMode) {
            research();
            return;
        }

        NavContext navContext = getNavContext();
        if (navContext.inRepo()) {
            if (mActivity.getCurrentPosition() == BrowserActivity.INDEX_LIBRARY_TAB) {
                mActivity.enableUpButton();
            }
            String parentPath = Utils.pathJoin(navContext.getRepoName(), navContext.getDirPath());
            if (dataManager.checkRefreshPaths(parentPath))
                forceRefresh = true;
            navToDirectory(forceRefresh, restorePosition);
        } else {
            mActivity.disableUpButton();
            navToReposView(forceRefresh, restorePosition);
        }
        mActivity.updateMenu();
    }

    public void navToReposView(boolean forceRefresh, boolean restorePosition) {
        //stopTimer();

        mPullToRefreshStopRefreshing++;

        if (mPullToRefreshStopRefreshing > 1) {
            refreshLayout.setRefreshing(false);
            mPullToRefreshStopRefreshing = 0;
        }

        forceRefresh = forceRefresh || isReposRefreshTimeOut();
        if (!Utils.isNetworkOn() || !forceRefresh) {
            if (mActivity.isRepoCreated) {
                mActivity.isRepoCreated = false;
                mActivity.newDataManager();
            }
            List<SeafRepo> repos = getDataManager().getReposFromCache();
            if (repos != null) {
                if (mRefreshType == REFRESH_ON_PULL) {
                    refreshLayout.setRefreshing(false);
                    mPullToRefreshStopRefreshing = 0;
                }

                updateAdapterWithRepos(repos, restorePosition);
                return;
            }
        }

        ConcurrentAsyncTask.execute(new LoadTask(getDataManager()));
    }

    public void navToDirectory(boolean forceRefresh, boolean restorePosition) {
        startTimer();

        mPullToRefreshStopRefreshing++;

        if (mPullToRefreshStopRefreshing > 1) {
            refreshLayout.setRefreshing(false);
            mPullToRefreshStopRefreshing = 0;
        }

        NavContext nav = getNavContext();
        DataManager dataManager = getDataManager();

//        if (nav.isRepoRoot()) {
//            ConcurrentAsyncTask.execute(new LoadDownloadInfo(getDataManager()),
//                    nav.getRepoID());
//        }

        SeafRepo repo = getDataManager().getCachedRepoByID(nav.getRepoID());
        if (repo != null) {
            adapter.setEncryptedRepo(repo.encrypted);
            if (nav.getDirPath().equals(BrowserActivity.ACTIONBAR_PARENT_PATH)) {
                setToolbarTitle(nav.getRepoName());
            } else
                setToolbarTitle(nav.getRepoName() + nav.getDirPath());
//                setToolbarTitle(nav.getDirPath().substring(
//                        nav.getDirPath().lastIndexOf(BrowserActivity.ACTIONBAR_PARENT_PATH) + 1));
        }

        forceRefresh = forceRefresh || isDirentsRefreshTimeOut(nav.getRepoID(), nav.getDirPath());
        if (!Utils.isNetworkOn() || !forceRefresh) {
            List<SeafDirent> dirents = dataManager.getCachedDirents(nav.getRepoID(), nav.getDirPath());
            if (dirents != null) {
                if (mRefreshType == REFRESH_ON_PULL) {
                    refreshLayout.setRefreshing(false);
                    mPullToRefreshStopRefreshing = 0;
                }

                updateAdapterWithDirents(dirents, restorePosition);
                return;
            }
        }

        if (nav.isRepoRoot()) {
            ConcurrentAsyncTask.execute(new LoadTag(getDataManager(), repo, false));
            return;
        }

        ConcurrentAsyncTask.execute(new LoadDirTask(getDataManager()),
                nav.getRepoName(),
                nav.getRepoID(),
                nav.getDirPath());
    }

    public void deleteCachedDirent(String repoID, String dirPath) {
        NavContext nav = getNavContext();
        DataManager dataManager = getDataManager();

        dataManager.deleteDirentContent(repoID, dirPath);
    }

    // refresh list by mTimer
    public void startTimer() {
        if (isTimerStarted)
            return;

        isTimerStarted = true;
        Log.d(DEBUG_TAG, "timer started");

        ConcurrentAsyncTask.execute(new SetDownloadTaskList());
    }

    public void stopTimer() {
        Log.d(DEBUG_TAG, "timer stopped");
        mTimer.removeCallbacksAndMessages(null);
        isTimerStarted = false;
    }

    private class SetDownloadTaskList extends AsyncTask<Void, Void, List<DownloadTaskInfo>> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<DownloadTaskInfo> doInBackground(Void... params) {
            if (mActivity == null || mProgressContainer.getVisibility() == View.VISIBLE)
                return null;

            TransferService ts = mActivity.getTransferService();
            String repoID = getNavContext().getRepoID();
            String currentDir = getNavContext().getDirPath();
            if (ts != null && repoID != null && currentDir != null) {
                List<DownloadTaskInfo> infos = ts.getDownloadTaskInfosByPath(repoID, currentDir);
                if (!adapter.equalLists(infos, adapter.getDownloadTaskList())) {
                    return infos;
                }
            }
            // Log.d(DEBUG_TAG, "timer post refresh signal " + System.currentTimeMillis());
            return null;
        }

        @Override
        protected void onPostExecute(List<DownloadTaskInfo> infos) {
            if (infos != null)
                adapter.setDownloadTaskList(infos);

            mTimer.postDelayed(new Runnable() {

                @Override
                public void run() {
                    ConcurrentAsyncTask.execute(new SetDownloadTaskList());
                }
            }, 1 * 3500);
        }
    }

    /**
     * calculate if repo refresh time is expired, the expiration is 10 mins
     */
    private boolean isReposRefreshTimeOut() {
        if (getDataManager().isReposRefreshTimeout()) {
            return true;
        }

        return false;
    }

    /**
     * calculate if dirent refresh time is expired, the expiration is 10 mins
     *
     * @param repoID
     * @param path
     * @return true if refresh time expired, false otherwise
     */
    private boolean isDirentsRefreshTimeOut(String repoID, String path) {
        if (getDataManager().isDirentsRefreshTimeout(repoID, path)) {
            return true;
        }

        return false;
    }

    public void gridFiles(int type) {
        int columns = SettingsManager.instance().getGridFilesColumns(type);
        GridLayoutManager manager = new GridLayoutManager(mActivity, columns);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter.getItem(position) instanceof SeafGroup) {
                    return columns;
                }
                return 1;
            }
        });
        mRecyclerView.setLayoutManager(manager);
        adapter.setGridFileType(type);
        mRecyclerView.setAdapter(adapter);
//        adapter.notifyChanged();
    }

    public void sortFiles(int type, int order) {
        adapter.sortFiles(type, order);
        adapter.notifyChanged();
        // persist sort settings
        SettingsManager.instance().saveSortFilesPref(type, order);
    }

    public void repoType(String type, boolean value) {
        adapter.setRepoType(type, value);
        adapter.sortFiles();
        adapter.notifyChanged();
        mActivity.updateRepoTypeOfBrowserMenu();
        mActivity.updateMenu();
        updateNoRepos();
    }

    private void updateAdapterWithRepos(List<SeafRepo> repos, boolean restoreScrollPosition) {
        adapter.clear();
        if (!repos.isEmpty()) {
            addReposToAdapter(repos);
            adapter.sortFiles(SettingsManager.instance().getSortFilesTypePref(),
                    SettingsManager.instance().getSortFilesOrderPref());
            adapter.notifyChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            restoreRepoScrollPosition(restoreScrollPosition);
//            mEmptyView.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
//            mEmptyView.setVisibility(View.VISIBLE);
        }
        mEmptyView.setVisibility(View.GONE);
        updateNoRepos();
        // Collapses the currently open view
        //mRecyclerView.collapse();
    }

    private void updateNoRepos() {
        if (adapter.showItems.isEmpty() && !getNavContext().inRepo() && mErrorText.getVisibility() == View.GONE) {
            SettingsManager settingsManager = SettingsManager.instance();
            boolean repoPersonal = settingsManager.getRepoTypePersonalPref();
            boolean repoGroup = settingsManager.getRepoTypeGroupPref();
            boolean repoShared = settingsManager.getRepoTypeSharedPref();
            mNoReposContainer.setVisibility(View.VISIBLE);
            String title;
            String desc;
            if (repoPersonal) {
                title = getString(R.string.no_directories);
                desc = getString(R.string.no_directories_desc);
            } else {
                if (repoGroup) {
                    if (repoShared) {
                        title = getString(R.string.no_directories);
                        desc = getString(R.string.no_shared_directories_desc) + "\n\n" + getString(R.string.no_groups_desc);
                    } else {
                        title = getString(R.string.no_groups);
                        desc = getString(R.string.no_groups_desc);
                    }
                } else {
                    if (repoShared) {
                        title = getString(R.string.no_shared_directories);
                        desc = getString(R.string.no_shared_directories_desc);
                    } else {
                        title = getString(R.string.no_options);
                        desc = getString(R.string.no_options_desc);
                    }
                }
            }
            mNoReposTitle.setText(title);
            mNoReposDesc.setText(desc);
        } else {
            mNoReposContainer.setVisibility(View.GONE);
        }
    }

    private void updateAdapterWithDirents(final List<SeafDirent> dirents, boolean restoreScrollPosition) {
        adapter.clear();
        if (!dirents.isEmpty()) {
            final String repoID = getNavContext().getRepoID();
            final String dirPath = getNavContext().getDirPath();

            adapter.clearImagePathMap();
            viewableImagesCountInEnc = 0;
            thumbnailImagesCountInEnc = 0;
            adapter.setItems(dirents);

            adapter.sortFiles(SettingsManager.instance().getSortFilesTypePref(),
                    SettingsManager.instance().getSortFilesOrderPref());
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                }
            });
            adapter.notifyChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            restoreDirentScrollPosition(restoreScrollPosition, repoID, dirPath);
            mEmptyView.setVisibility(View.GONE);

            ConcurrentAsyncTask.execute(new LoadAllThumbInEncRepo(repoID, dirPath, dirents));
        } else {
            // Directory is empty
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
        // Collapses the currently open view
        //mRecyclerView.collapse();
    }

    private class LoadAllThumbInEncRepo extends AsyncTask<Void, Void, Map<String, String>> {
        String repoId;
        String dirPath;
        List<SeafDirent> dirents;
        int imagesCount;

        public LoadAllThumbInEncRepo(String repoId, String dirPath, List<SeafDirent> dirents) {
            this.repoId = repoId;
            this.dirPath = dirPath;
            this.dirents = dirents;
            this.imagesCount = 0;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            Map<String, String> imagePathMap = new HashMap<>();

            SeafDirent _dirent = dirents.get(0);
            if (!_dirent.isSearchedFile) {
                SeafRepo repo = dataManager.getCachedRepoByID(repoId);
                if (repo != null && repo.canLocalDecrypt()) {
                    if (TextUtils.isEmpty(repoId)) {
                        return null;
                    }
                    File parentDir = dataManager.getLocalRepoFile(repo.name, repoId, dirPath);
                    if (!parentDir.exists()) {
                        parentDir.mkdir();
                    }
                    List<String> nameList = new ArrayList<>();
                    if (parentDir.list() != null) {
                        nameList.addAll(Arrays.asList(parentDir.list()));
                    }
                    for (SeafDirent dirent:dirents) {
                        if (!(getNavContext().getRepoID().equals(repoId) && getNavContext().getDirPath().equals(dirPath))) {
                            return null;
                        }
                        if (!dirent.isDir() && Utils.isViewableImage(dirent.name)) {
                            imagesCount += 1;

                            String filePath = Utils.pathJoin(dirPath, dirent.name);
                            try {
                                String thumbName = Utils.getEncThumbPath(dirent.name);
                                int index = nameList.indexOf(thumbName);
                                if (index >= 0) {
                                    String thumbPath = Utils.pathJoin(parentDir.getAbsolutePath(), thumbName);
                                    imagePathMap.put(filePath, thumbPath);

                                    nameList.remove(index);
                                }
                            } catch (RuntimeException e) {
                            }
                        }
                    }
                }
            }
            return imagePathMap;
        }

        @Override
        protected void onPostExecute(Map<String, String> imagePathMap) {
            if (imagePathMap != null) {
                adapter.setImagePathMap(imagePathMap);
                if (imagesCount != 0) {
                    viewableImagesCountInEnc = imagesCount;
                    thumbnailImagesCountInEnc += imagePathMap.size();

                    Log.e("ViewableImagesCount: ", String.valueOf(viewableImagesCountInEnc));
                    Log.e("ThumbImagesCount: ", String.valueOf(thumbnailImagesCountInEnc));
                }
            }
        }
    }

    /**
     * update state of contextual action bar (CAB)
     */
    public void updateContextualActionBar() {

        if (!mActionMode) {
            // there are some selected items, start the actionMode
            startSupportActionMode();
        } else {
            // Log.d(DEBUG_TAG, "mActionMode.setTitle " + adapter.getCheckedItemCount());
            if (adapter.getCheckedItemCount() == 0)
                stopSupportActionMode();
            else
                mActionModeTitleText.setText(getResources().getQuantityString(
                        R.plurals.transfer_list_items_selected,
                        adapter.getCheckedItemCount(),
                        adapter.getCheckedItemCount()));
        }

    }

    public void listItemLongClick(int position) {
        startContextualActionMode(position);
    }

    public void listItemClick(int position) {
        if (Utils.isFastTapping()) return;

        // handle action mode selections
        if (mActionMode) {
            // add or remove selection for current list item
            if (adapter == null) return;

            adapter.toggleSelection(position);
            return;
        }

        SeafRepo repo = null;
        final NavContext nav = getNavContext();

        if (mActivity.isSearchMode) {
            onSearchedFileSelected(adapter.getItem(position));
            return;
        }

        if (nav.inRepo()) {
            repo = getDataManager().getCachedRepoByID(nav.getRepoID());
            setToolbarTitle(repo.getName());
        } else {
            SeafItem item = adapter.getItem(position);
            if (item instanceof SeafRepo) {
                repo = (SeafRepo) item;
            }
        }

        if (repo == null) {
            return;
        }

        if (repo.encrypted && !getDataManager().getRepoPasswordSet(repo.id)) {
            String password = getDataManager().getRepoPassword(repo.id);
            mActivity.showPasswordDialog(repo.name, repo.id,
                    new TaskDialog.TaskDialogListener() {
                        @Override
                        public void onTaskSuccess() {
                            listItemClick(position);
                        }
                    }, password);

            return;
        }

        mRefreshType = REFRESH_ON_CLICK;
        if (nav.inRepo()) {
            if (adapter.getItem(position) instanceof SeafDirent) {
                final SeafDirent dirent = (SeafDirent) adapter.getItem(position);
                if (dirent.isDir()) {
                    String currentPath = nav.getDirPath();
                    String newPath = currentPath.endsWith("/") ?
                            currentPath + dirent.name : currentPath + "/" + dirent.name;
                    nav.setDir(newPath, dirent.id);
                    nav.setDirPermission(dirent.permission);
                    saveDirentScrollPosition(repo.getID(), currentPath);
                    refreshView();
//                    setToolbarTitle(dirent.name);
                    setToolbarTitle(repo.getName() + newPath);
                } else {
                    String currentPath = nav.getDirPath();
                    String newPath = currentPath.endsWith("/") ?
                            currentPath + dirent.name : currentPath + "/" + dirent.name;
                    saveDirentScrollPosition(repo.getID(), currentPath);
                    setToolbarTitle(repo.getName() + newPath);
                    mActivity.onFileSelected(dirent);
                }
            } else
                return;
        } else {
            nav.setDirPermission(repo.permission);
            nav.setRepoID(repo.id);
            nav.setRepoName(repo.getName());
            nav.setDir("/", repo.root);
            saveRepoScrollPosition();
            refreshView();

            if (repo.canLocalDecrypt() && !downloadEncRepoIds.contains(repo.id)) {
                SettingsManager settingsManager = SettingsManager.instance();
                if (settingsManager.isThumbInEncRepo() && settingsManager.checkCameraUploadNetworkAvailable()) {
                    downloadEncRepoIds.add(repo.id);
                    mActivity.downloadDir("/", "", true, true, true);
                }
            }
        }
    }

    public void onSearchedFileSelected(SeafItem item) {
        SeafDirent dirent = (SeafDirent) item;
        final String repoID = dirent.repoID;
        final String fileName = dirent.getTitle();
        final SeafRepo repo = dataManager.getCachedRepoByID(repoID);
        final String repoName = repo.getName();
        final String filePath = dirent.path;

        if (dirent.isDir()) {
            if (repo == null) {
                mActivity.showShortToast(mActivity, R.string.search_library_not_found);
                return;
            }
            openDir(repoID, repoName, filePath, dirent.permission);
            return;
        }

        mActivity.onFileSelected(dirent);
    }

    private class ScrollState {
        public int index;
        public int top;

        public ScrollState(int index, int top) {
            this.index = index;
            this.top = top;
        }
    }

    private void saveDirentScrollPosition(String repoId, String currentPath) {
        final String pathJoin = Utils.pathJoin(repoId, currentPath);
        final int index = ((GridLayoutManager)mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        final View v = mRecyclerView.getChildAt(0);
        final int top = (v == null) ? 0 : (v.getTop() - mRecyclerView.getPaddingTop());
        final ScrollState state = new ScrollState(index, top);
        scrollPostions.put(pathJoin, state);
    }

    private void saveRepoScrollPosition() {
        final int index = ((GridLayoutManager)mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        final View v = mRecyclerView.getChildAt(0);
        final int top = (v == null) ? 0 : (v.getTop() - mRecyclerView.getPaddingTop());
        final ScrollState state = new ScrollState(index, top);
        scrollPostions.put(KEY_REPO_SCROLL_POSITION, state);
    }

    private void restoreDirentScrollPosition(boolean restore, String repoId, String dirPath) {
        if (restore) {
            final String pathJoin = Utils.pathJoin(repoId, dirPath);
            ScrollState state = scrollPostions.get(pathJoin);
            if (state != null) {
                mRecyclerView.scrollToPosition(state.index);
                // mRecyclerView.setSelectionFromTop(state.index, state.top);
            } else {
                setSelectionAfterHeaderView();
                // mRecyclerView.setSelectionAfterHeaderView();
            }
        } else {
            setSelectionAfterHeaderView();
            // mRecyclerView.setSelectionAfterHeaderView();
        }
    }

    private void restoreRepoScrollPosition(boolean restore) {
        if (restore) {
            ScrollState state = scrollPostions.get(KEY_REPO_SCROLL_POSITION);
            if (state != null) {
                mRecyclerView.scrollToPosition(state.index);
                // mRecyclerView.setSelectionFromTop(state.index, state.top);
            } else {
                setSelectionAfterHeaderView();
                // mRecyclerView.setSelectionAfterHeaderView();
            }
        } else {
            setSelectionAfterHeaderView();
            // mRecyclerView.setSelectionAfterHeaderView();
        }
    }

    private void setSelectionAfterHeaderView() {
        mRecyclerView.scrollToPosition(0);
    }

    private void addReposToAdapter(List<SeafRepo> repos) {
        if (repos == null)
            return;
        Map<String, List<SeafRepo>> map = Utils.groupRepos(repos);
        List<SeafRepo> personalRepos = map.get(Utils.PERSONAL_REPO);
        if (personalRepos != null) {
            SeafGroup personalGroup = new SeafGroup(mActivity.getResources().getString(R.string.personal), true, false, false);
            adapter.add(personalGroup);
            for (SeafRepo repo : personalRepos)
//                if (repo.getTitle().trim().toLowerCase().contains(mSearchText.getText().toString().trim().toLowerCase())) {
                adapter.add(repo);
//                }
        }

        List<SeafRepo> sharedRepos = map.get(Utils.SHARED_REPO);
        if (sharedRepos != null) {
            SeafGroup sharedGroup = new SeafGroup(mActivity.getResources().getString(R.string.shared), false, true, false);
            adapter.add(sharedGroup);
            for (SeafRepo repo : sharedRepos)
//                if (repo.getTitle().trim().toLowerCase().contains(mSearchText.getText().toString().trim().toLowerCase())) {
                adapter.add(repo);
//                }
        }

        for (Map.Entry<String, List<SeafRepo>> entry : map.entrySet()) {
            String key = entry.getKey();
            if (!key.equals(Utils.PERSONAL_REPO)
                    && !key.endsWith(Utils.SHARED_REPO)) {
                SeafGroup group = new SeafGroup(key, false, false, true);
                adapter.add(group);
                for (SeafRepo repo : entry.getValue()) {
//                    if (repo.getTitle().trim().toLowerCase().contains(mSearchText.getText().toString().trim().toLowerCase())) {
                    adapter.add(repo);
//                    }
                }
            }
        }
    }

    private class LoadTask extends AsyncTask<Void, Void, List<SeafRepo>> {
        SeafException err = null;
        DataManager dataManager;

        public LoadTask(DataManager dataManager) {
            this.dataManager = dataManager;
        }

        @Override
        protected void onPreExecute() {
            if (mRefreshType == REFRESH_ON_CLICK
                    || mRefreshType == REFRESH_ON_OVERFLOW_MENU
                    || mRefreshType == REFRESH_ON_RESUME) {
                showLoading(true);
            } else if (mRefreshType == REFRESH_ON_PULL) {

            }
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

        private void displaySSLError() {
            if (mActivity == null)
                return;

            if (getNavContext().inRepo()) {
                return;
            }

            showError(R.string.ssl_error);
        }

        private void resend() {
            if (mActivity == null)
                return;

            if (getNavContext().inRepo()) {
                return;
            }
            ConcurrentAsyncTask.execute(new LoadTask(dataManager));
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<SeafRepo> rs) {
            if (mActivity == null)
                // this occurs if user navigation to another activity
                return;

            if (mRefreshType == REFRESH_ON_CLICK
                    || mRefreshType == REFRESH_ON_OVERFLOW_MENU
                    || mRefreshType == REFRESH_ON_RESUME) {
                showLoading(false);
            } else if (mRefreshType == REFRESH_ON_PULL) {
                String lastUpdate = getDataManager().getLastPullToRefreshTime(DataManager.PULL_TO_REFRESH_LAST_TIME_FOR_REPOS_FRAGMENT);
                //mRecyclerView.onRefreshComplete(lastUpdate);
                refreshLayout.setRefreshing(false);
                getDataManager().saveLastPullToRefreshTime(System.currentTimeMillis(), DataManager.PULL_TO_REFRESH_LAST_TIME_FOR_REPOS_FRAGMENT);
                mPullToRefreshStopRefreshing = 0;
            }

            if (getNavContext().inRepo()) {
                // this occurs if user already navigate into a repo
                return;
            }

            // Prompt the user to accept the ssl certificate
            if (err == SeafException.sslException) {
                SslConfirmDialog dialog = new SslConfirmDialog(dataManager.getAccount(),
                        new SslConfirmDialog.Listener() {
                            @Override
                            public void onAccepted(boolean rememberChoice) {
                                Account account = dataManager.getAccount();
                                CertsManager.instance().saveCertForAccount(account, rememberChoice);
                                resend();
                            }

                            @Override
                            public void onRejected() {
                                displaySSLError();
                            }
                        });
                dialog.show(getFragmentManager(), SslConfirmDialog.FRAGMENT_TAG);
                return;
            } else if (err == SeafException.remoteWipedException) {
                mActivity.completeRemoteWipe();
            }

            if (err != null) {
                if (err.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // Token expired, should login again
                    mActivity.showShortToast(mActivity, R.string.err_token_expired);
                    mActivity.logoutWhenTokenExpired();
                } else {
                    Log.e(DEBUG_TAG, "failed to load repos: " + err.getMessage());
                    showError(R.string.error_when_load_repos);
                    return;
                }
            }

            if (rs != null) {
                getDataManager().setReposRefreshTimeStamp(Utils.now());
                updateAdapterWithRepos(rs, false);

                getThumbImagesCountInEncryptedRepos();

                if (!loadReposFromServer) {
                    loadReposFromServer = true;
                    mActivity.checkBackupFolder();
                    mActivity.backupFolder();
                }
            } else {
                Log.i(DEBUG_TAG, "failed to load repos");
                showError(R.string.error_when_load_repos);
            }
        }
    }

    private class LoadTag extends AsyncTask<Void, Void, List<SeafRepoTag>> {
        SeafException err = null;
        DataManager dataManager;
        SeafRepo repo;
        boolean fromSearch;

        public LoadTag(DataManager dataManager, SeafRepo repo, boolean fromSearch) {
            this.dataManager = dataManager;
            this.repo = repo;
            this.fromSearch = fromSearch;
        }

        @Override
        protected void onPreExecute() {
            showLoading(true);
        }

        @Override
        protected List<SeafRepoTag> doInBackground(Void... params) {
            try {
                return dataManager.getRepoTagsFromServer(repo);
            } catch (SeafException e) {
                err = e;
                return null;
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<SeafRepoTag> repoTags) {
            if (mActivity == null) {
                // this occurs if user navigation to another activity
                showLoading(false);
                return;
            }

            if (repoTags != null) {
                NavContext nav = getNavContext();
                List<SeafRepo> repos = dataManager.getReposFromCache();
                List<SeafRepo> newRepos = Lists.newArrayListWithCapacity(0);;
                for (SeafRepo seafRepo: repos) {
                    if (seafRepo.getID().equals(repo.getID())) {
                        seafRepo.setRepoTags(repoTags);
                    }
                    newRepos.add(seafRepo);
                }

                try {
                    File cache = dataManager.getFileForReposCache();
                    Utils.writeFile(cache, dataManager.reposToString(newRepos));
                } catch (IOException e) {
                    Log.e(DEBUG_TAG, "Could not write repo cache to disk.", e);
                }
                if (!fromSearch) {
                    ConcurrentAsyncTask.execute(new LoadDirTask(getDataManager()),
                            nav.getRepoName(),
                            nav.getRepoID(),
                            nav.getDirPath());
                } else {
                    showLoading(false);
                    fileTagAdapter.setRepoTags(repoTags);
                    fileTagAdapter.notifyChanged();
                }
            } else {
                Log.i(DEBUG_TAG, "failed to load repos");
                showLoading(false);
                showError(R.string.error_when_load_repos);
            }
        }
    }

    private void showError(int strID) {
        showError(mActivity.getResources().getString(strID));
    }

    private void showError(String msg) {
        mProgressContainer.setVisibility(View.GONE);
        mListContainer.setVisibility(View.GONE);

        adapter.clear();
        adapter.notifyChanged();

        mErrorText.setText(msg);
        mErrorText.setVisibility(View.VISIBLE);
        mErrorText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
    }

    public void showLoading(boolean show) {
        mErrorText.setVisibility(View.GONE);
        if (show) {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                    mActivity, android.R.anim.fade_in));
            mListContainer.startAnimation(AnimationUtils.loadAnimation(
                    mActivity, android.R.anim.fade_out));

            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.INVISIBLE);
        } else {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                    mActivity, android.R.anim.fade_out));
            mListContainer.startAnimation(AnimationUtils.loadAnimation(
                    mActivity, android.R.anim.fade_in));

            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        }
    }

    private class LoadDirTask extends AsyncTask<String, Void, List<SeafDirent>> {

        SeafException err = null;
        String myRepoName;
        String myRepoID;
        String myPath;
        boolean forDownload;

        DataManager dataManager;

        public LoadDirTask(DataManager dataManager) {
            this.dataManager = dataManager;
            this.forDownload = false;
        }

        public LoadDirTask(DataManager dataManager, boolean forDownload) {
            this.dataManager = dataManager;
            this.forDownload = forDownload;
        }

        @Override
        protected void onPreExecute() {
            if (mRefreshType == REFRESH_ON_CLICK
                    || mRefreshType == REFRESH_ON_OVERFLOW_MENU
                    || mRefreshType == REFRESH_ON_RESUME) {
                if (mProgressContainer.getVisibility() == View.GONE) {
                    showLoading(true);
                }
            } else if (mRefreshType == REFRESH_ON_PULL) {
                // mHeadProgress.setVisibility(ProgressBar.VISIBLE);
                showLoading(false);
            }
        }

        @Override
        protected List<SeafDirent> doInBackground(String... params) {
            if (params.length != 3) {
                Log.d(DEBUG_TAG, "Wrong params to LoadDirTask");
                return null;
            }

            myRepoName = params[0];
            myRepoID = params[1];
            myPath = params[2];
            try {
                List<SeafDirent> dirents = dataManager.getDirentsFromServer(myRepoID, myPath);
                for (SeafDirent sd : dirents) {
                    if (!sd.isDir()) {
                        SeafCachedFile scf = dataManager.getCachedFile(myRepoName, myRepoID, myPath);
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

        private void resend() {
            if (mActivity == null)
                return;
            NavContext nav = mActivity.getNavContext();
            if (!myRepoID.equals(nav.getRepoID()) || !myPath.equals(nav.getDirPath())) {
                return;
            }

            ConcurrentAsyncTask.execute(new LoadDirTask(dataManager), myRepoName, myRepoID, myPath);
        }

        private void displaySSLError() {
            if (mActivity == null)
                return;

            NavContext nav = mActivity.getNavContext();
            if (!myRepoID.equals(nav.getRepoID()) || !myPath.equals(nav.getDirPath())) {
                return;
            }
            showError(R.string.ssl_error);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<SeafDirent> dirents) {
            if (forDownload) {
                showLoading(false);
                mActivity.downloadFiles(myRepoID, myRepoName, myPath, dirents, false, true);
                return;
            }
            if (mActivity == null)
                // this occurs if user navigation to another activity
                return;

            if (mRefreshType == REFRESH_ON_CLICK
                    || mRefreshType == REFRESH_ON_OVERFLOW_MENU
                    || mRefreshType == REFRESH_ON_RESUME) {
                showLoading(false);
            } else if (mRefreshType == REFRESH_ON_PULL) {
                String lastUpdate = getDataManager().getLastPullToRefreshTime(DataManager.PULL_TO_REFRESH_LAST_TIME_FOR_REPOS_FRAGMENT);
                //mRecyclerView.onRefreshComplete(lastUpdate);
                refreshLayout.setRefreshing(false);
                getDataManager().saveLastPullToRefreshTime(System.currentTimeMillis(), DataManager.PULL_TO_REFRESH_LAST_TIME_FOR_REPOS_FRAGMENT);
                mPullToRefreshStopRefreshing = 0;
            }

            NavContext nav = mActivity.getNavContext();
            if (!myRepoID.equals(nav.getRepoID()) || !myPath.equals(nav.getDirPath())) {
                return;
            }

            if (err == SeafException.sslException) {
                SslConfirmDialog dialog = new SslConfirmDialog(dataManager.getAccount(),
                        new SslConfirmDialog.Listener() {
                            @Override
                            public void onAccepted(boolean rememberChoice) {
                                Account account = dataManager.getAccount();
                                CertsManager.instance().saveCertForAccount(account, rememberChoice);
                                resend();
                            }

                            @Override
                            public void onRejected() {
                                displaySSLError();
                            }
                        });
                dialog.show(getFragmentManager(), SslConfirmDialog.FRAGMENT_TAG);
                return;
            } else if (err == SeafException.remoteWipedException) {
                mActivity.completeRemoteWipe();
            }

            if (err != null) {
                if (err.getCode() == SeafConnection.HTTP_STATUS_REPO_PASSWORD_REQUIRED) {
                    showPasswordDialog();
                } else if (err.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // Token expired, should login again
                    mActivity.showShortToast(mActivity, R.string.err_token_expired);
                    mActivity.logoutWhenTokenExpired();
                } else if (err.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    final String message = String.format(getString(R.string.op_exception_folder_deleted), myPath);
                    mActivity.showShortToast(mActivity, message);
                } else {
                    Log.d(DEBUG_TAG, "failed to load dirents: " + err.getMessage());
                    err.printStackTrace();
                    showError(R.string.error_when_load_dirents);
                }
                return;
            }

            if (dirents == null) {
                showError(R.string.error_when_load_dirents);
                Log.i(DEBUG_TAG, "failed to load dir");
                return;
            }
            getDataManager().setDirsRefreshTimeStamp(myRepoID, myPath);
            updateAdapterWithDirents(dirents, false);
        }
    }

    private void showPasswordDialog() {
        NavContext nav = mActivity.getNavContext();
        String repoName = nav.getRepoName();
        String repoID = nav.getRepoID();

        mActivity.showPasswordDialog(repoName, repoID, new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                refreshView();
            }
        });
    }

    private void startSupportActionMode() {

        initActionModeCards();

        mActionMode = true;
        mActionModeLayout.setVisibility(View.VISIBLE);
        if (adapter == null) return;
        adapter.setActionModeOn(true);
        // to hidden  "r" permissions  files or folder
        String permission = getNavContext().getDirPermission();
        if (permission != null && permission.indexOf("w") == -1) {
            mActionModeDeleteCard.setVisibility(View.GONE);
            mActionModeMoveCard.setVisibility(View.GONE);
        }
        adapter.notifyChanged();
    }

    private void initActionModeCards() {
        mActionModeDeleteCard.setOnClickListener(v -> {
            NavContext nav = mActivity.getNavContext();
            String repoID = nav.getRepoID();
            String repoName = nav.getRepoName();
            String dirPath = nav.getDirPath();
            final List<SeafDirent> selectedDirents = adapter.getSelectedItemsValues();

            if (selectedDirents.size() == 0
                    || (repoID == null && !mActivity.isSearchMode)
                    || (dirPath == null && !mActivity.isSearchMode)) {
                mActivity.showShortToast(mActivity, R.string.action_mode_no_items_selected);
                return;
            }

            mActivity.deleteFiles(repoID, dirPath, selectedDirents);
        });
        mActionModeCopyCard.setOnClickListener(v -> {
            NavContext nav = mActivity.getNavContext();
            String repoID = nav.getRepoID();
            String repoName = nav.getRepoName();
            String dirPath = nav.getDirPath();
            final List<SeafDirent> selectedDirents = adapter.getSelectedItemsValues();

            if (selectedDirents.size() == 0
                    || (repoID == null && !mActivity.isSearchMode)
                    || (dirPath == null && !mActivity.isSearchMode)) {
                mActivity.showShortToast(mActivity, R.string.action_mode_no_items_selected);
                return;
            }

            mActivity.copyFiles(repoID, repoName, dirPath, selectedDirents);
        });
        mActionModeMoveCard.setOnClickListener(v -> {
            NavContext nav = mActivity.getNavContext();
            String repoID = nav.getRepoID();
            String repoName = nav.getRepoName();
            String dirPath = nav.getDirPath();
            final List<SeafDirent> selectedDirents = adapter.getSelectedItemsValues();

            if (selectedDirents.size() == 0
                    || (repoID == null && !mActivity.isSearchMode)
                    || (dirPath == null && !mActivity.isSearchMode)) {
                mActivity.showShortToast(mActivity, R.string.action_mode_no_items_selected);
                return;
            }

            mActivity.moveFiles(repoID, repoName, dirPath, selectedDirents);
        });
        mActionModeDownloadCard.setOnClickListener(v -> {
            NavContext nav = mActivity.getNavContext();
            String repoID = nav.getRepoID();
            String repoName = nav.getRepoName();
            String dirPath = nav.getDirPath();
            final List<SeafDirent> selectedDirents = adapter.getSelectedItemsValues();

            if (selectedDirents.size() == 0
                    || (repoID == null && !mActivity.isSearchMode)
                    || (dirPath == null && !mActivity.isSearchMode)) {
                mActivity.showShortToast(mActivity, R.string.action_mode_no_items_selected);
                return;
            }

            mActivity.downloadFiles(repoID, repoName, dirPath, selectedDirents, false, false);
        });
        mActionModeSelectAllCard.setOnClickListener(v -> {
            if (!allItemsSelected) {
                if (adapter == null) return;

                adapter.selectAllItems();
                updateContextualActionBar();
            } else {
                if (adapter == null) return;

                adapter.deselectAllItems();
                updateContextualActionBar();
            }

            allItemsSelected = !allItemsSelected;
        });
        mActionModeCloseCard.setOnClickListener(v -> {
            stopSupportActionMode();
        });
    }

    public void stopSupportActionMode() {
        if (adapter == null) return;

        adapter.setActionModeOn(false);
        adapter.deselectAllItems();

        // Here you can make any necessary updates to the activity when
        // the contextual action bar (CAB) is removed. By default, selected items are deselected/unchecked.
        mActionMode = false;
        mActionModeLayout.setVisibility(View.GONE);
    }

    public void clearAdapterData() {
        if (adapter != null && mRecyclerView != null) {
            adapter.clear();
            mRecyclerView.setAdapter(adapter);
        }
    }

    public void showAddRepoTagDialog(String repoID, boolean fromFile) {
        addRepoTagDialog = Utils.CustomDialog(mActivity);
        addRepoTagDialog.setContentView(R.layout.dialog_add_repo_tag);
        addRepoTagDialog.setCancelable(false);

        EditText addTagName = addRepoTagDialog.findViewById(R.id.create_tag_name);
        RecyclerView colorRecycler = addRepoTagDialog.findViewById(R.id.color_recycler);
        CardView cancelCard = addRepoTagDialog.findViewById(R.id.cancel_card);
        CardView addCard = addRepoTagDialog.findViewById(R.id.create_card);
        TextView errorText = addRepoTagDialog.findViewById(R.id.error_text);
        CardView progressCard = addRepoTagDialog.findViewById(R.id.progress_card);

        addTagName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                errorText.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        colorRecycler.setLayoutManager(layoutManager);
        List<String> colors = Arrays.asList(
                "#FF0000",
                "#FF8900",
                "#FFCE00",
                "#FFF500",
                "#BAFF00",
                "#58FF00",
                "#00FFBA",
                "#00C4FF",
                "#004EFF",
                "#2700FF",
                "#8000FF",
                "#C400FF"
        );
        SeafTagColorAdapter colorAdapter = new SeafTagColorAdapter(colors);
        colorRecycler.setAdapter(colorAdapter);
        colorAdapter.notifyChanged();


        cancelCard.setOnClickListener(v1 -> {
            addRepoTagDialog.dismiss();
        });

        addCard.setOnClickListener(v1 -> {
            if (addTagName.getText().toString().isEmpty()) {
                errorText.setText(this.getResources().getString(R.string.tag_name_empty));
                return;
            }
            if (colorAdapter.getSelectedTagColor().isEmpty()) {
                errorText.setText(this.getResources().getString(R.string.tag_color_empty));
                return;
            }
            ConcurrentAsyncTask.execute(new AddRepoTag(repoID, addTagName.getText().toString(), colorAdapter.getSelectedTagColor(), fromFile));
//            dialog.dismiss();
//            mActivity.dialogForBrowserMenu.dismiss();
        });

        addRepoTagDialog.show();
    }

    private void showAddRepoTagLoading(boolean flag) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (addRepoTagDialog.isShowing()) {
                    addRepoTagDialog.findViewById(R.id.progress_card).setVisibility(flag ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    private void upgradeCollapse() {
        TransitionManager.beginDelayedTransition(mCollapseLayout);
        ViewGroup.LayoutParams layoutParams = mCollapseLayout.getLayoutParams();
        layoutParams.height = collapse ? 0 : ViewGroup.LayoutParams.MATCH_PARENT;
        mCollapseLayout.requestLayout();
        mDirectionImage.setImageDrawable(getResources().getDrawable((collapse) ? R.drawable.ic_below : R.drawable.ic_above));
    }

    public void research() {
        refreshLayout.setRefreshing(false);
        mSearchBtn.callOnClick();
    }

    private void handleSearch(int page) {
        // TODO page loading instead of only display top 100 search result
        page = 100;
        if (!Utils.isNetworkOn()) {
            mActivity.showShortToast(mActivity, R.string.network_down);
            return;
        }

        String searchText = mSearchText.getText().toString().trim();
        if (!TextUtils.isEmpty(searchText)) {
            mSearchedFiles.clear();

            search(searchText, page);

            Utils.hideSoftKeyboard(mSearchText);
        } else {
            mActivity.showShortToast(mActivity, R.string.search_txt_empty);
        }
    }

    private void search(String content, int page) {
        // start asynctask
        boolean isSearchIncluded = accountManager.getServerInfo(account).isSearchIncluded();
        String repoID = "";
        String path = "";
        List<String> ftype = Lists.newArrayList();
        String inputfexts = "";
        long startTime = 0;
        long endTime = 0;
        long maxSize = 0;
        long minSize = 0;

        if (mLocationSwitch.isChecked()) {
            if (mSearchCurrentLibraryRB.isChecked()) {
                repoID = getNavContext().getRepoID();
            }
            if (mSearchCurrentFolderRB.isChecked()) {
                repoID = getNavContext().getRepoID();
                path = getNavContext().getDirPath();
            }
        }
        if (repoID == null) repoID = "";
        if (path == null) path = "";

        if (mCustomFileTypesSwitch.isChecked()) {
            if (mImageCheckBox.isChecked()) ftype.add("Image");
            if (mPdfCheckBox.isChecked()) ftype.add("PDF");
            if (mVideoCheckBox.isChecked()) ftype.add("Video");
            if (mMarkdownCheckBox.isChecked()) ftype.add("Markdown");
            if (mTextCheckBox.isChecked()) ftype.add("Text");
            if (mAudioCheckBox.isChecked()) ftype.add("Audio");
            if (mDocumentCheckBox.isChecked()) ftype.add("Document");

            inputfexts = mCustomFileTypesEdit.getText().toString();
        }

        if (mLastChangesSwitch.isChecked()) {
            if (!mStartTimeEdit.getText().toString().isEmpty())
                startTime = startCalendar.getTimeInMillis() / 1000;
            if (!mEndTimeEdit.getText().toString().isEmpty())
                endTime = endCalendar.getTimeInMillis() / 1000;
        }

        if (startCalendar.getTimeInMillis() > endCalendar.getTimeInMillis()) {
            mActivity.showShortToast(mActivity, R.string.select_correct_date);
            return;
        }

        if (mSizeSwitch.isChecked()) {
            if (!mMinSizeEdit.getText().toString().isEmpty())
                minSize = Long.parseLong(mMinSizeEdit.getText().toString()) * unitMap.get(mMinSizeUnitText.getText().toString());

            if (!mMaxSizeEdit.getText().toString().isEmpty())
                maxSize = Long.parseLong(mMaxSizeEdit.getText().toString()) * unitMap.get(mMaxSizeUnitText.getText().toString());
        }

        if (isSearchIncluded) {
            ConcurrentAsyncTask.execute(
                    new SearchLibrariesTask(dataManager, content, page, repoID, path, ftype, inputfexts, startTime, endTime, maxSize, minSize)
            );
        } else {
            currentReposSize = repoID.isEmpty() ? dataManager.getReposFromCache().size() : 1;
            List<String> repoIds = Lists.newArrayList();
            for (SeafRepo repo: dataManager.getReposFromCache()) {
                if (!repoIds.contains(repo.getID())) {
                    repoIds.add(repo.id);
                    if (repoID.isEmpty() || repo.getID().equals(repoID)) {
                        new SearchLibrariesWithRepoTask(dataManager, content, page, repo, path, ftype, inputfexts, startTime, endTime, maxSize, minSize).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }
        }
    }

    private void showSelectDatePopup(boolean isStart) {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_datetime2, null);
            layout.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mDateDropDown = new PopupWindow(layout, mLastChangesLayout.getWidth(),
                    mPopupSelectDatetime2View.getHeight(),true);

            final CalendarView calendarView = layout.findViewById(R.id.calendar_view);
            final TextView todayText = layout.findViewById(R.id.today_text);

            calendarView.setMaxDate(System.currentTimeMillis() - 1000);
            calendarView.setDate(isStart? startCalendar.getTimeInMillis() : endCalendar.getTimeInMillis());
            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                if (isStart) {
                    startCalendar.set(year, month, dayOfMonth, 0, 0, 0);
                    mStartTimeEdit.setText(simpleDateFormatForPhone.format(startCalendar.getTime()));
                } else {
                    endCalendar.set(year, month, dayOfMonth, 23, 59, 59);
                    mEndTimeEdit.setText(simpleDateFormatForPhone.format(endCalendar.getTime()));
                }
            });

            todayText.setOnClickListener(v -> {
                calendarView.setDate(System.currentTimeMillis());
                Calendar currentCalendar = Calendar.getInstance();
                if (isStart) {
                    startCalendar.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                    mStartTimeEdit.setText(simpleDateFormatForPhone.format(startCalendar.getTime()));
                } else {
                    endCalendar.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
                    mEndTimeEdit.setText(simpleDateFormatForPhone.format(endCalendar.getTime()));
                }
            });

            mDateDropDown.showAsDropDown(mLastChangesLayout, 5, 5);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSelectUnitPopup(Boolean isMin) {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_unit, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mUnitDropDown = new PopupWindow(layout, mPopupSelectUnitView.getWidth(),
                    mPopupSelectUnitView.getHeight(),true);//FrameLayout.LayoutParams.WRAP_CONTENT

            final CardView unitBCard = layout.findViewById(R.id.unit_b_card);
            final CardView unitKBCard = layout.findViewById(R.id.unit_kb_card);
            final CardView unitMBCard = layout.findViewById(R.id.unit_mb_card);
            final CardView unitGBCard = layout.findViewById(R.id.unit_gb_card);
            final CardView unitTBCard = layout.findViewById(R.id.unit_tb_card);

            unitBCard.setOnClickListener(view -> updateUnitText(R.string.unit_b, isMin));
            unitKBCard.setOnClickListener(view -> updateUnitText(R.string.unit_kb, isMin));
            unitMBCard.setOnClickListener(view -> updateUnitText(R.string.unit_mb, isMin));
            unitGBCard.setOnClickListener(view -> updateUnitText(R.string.unit_gb, isMin));
            unitTBCard.setOnClickListener(view -> updateUnitText(R.string.unit_tb, isMin));

            mUnitDropDown.showAsDropDown(isMin? mMinSizeLayout : mMaxSizeLayout, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUnitText(int unitID, boolean isMin) {
        mUnitDropDown.dismiss();
        String unit = getResources().getString(unitID);
        if (isMin) {
            mMinSizeUnitText.setText(unit);
        } else {
            mMaxSizeUnitText.setText(unit);
        }
    }

    private class AddRepoTag extends AsyncTask<Void, Void, String> {
        private SeafException err;
        private String repoID;
        private String tagName;
        private String tagColor;
        private boolean fromFile;

        public AddRepoTag(String repoID, String tagName, String tagColor, boolean fromFile) {
            this.repoID = repoID;
            this.tagName = tagName;
            this.tagColor = tagColor;
            this.fromFile = fromFile;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                showAddRepoTagLoading(true);
                SeafRepo repo = getDataManager().getCachedRepoByID(repoID);
                return getDataManager().addRepoTag(repo, tagName, tagColor);
            } catch (SeafException e) {
                err = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // Check to see whether this activity is in the process of finishing
            // to avoid IllegalStateException when AsyncTasks continue to run after the activity has been destroyed
            // http://stackoverflow.com/a/35729068/3962551
            showAddRepoTagLoading(false);
            if (result == null) return;
            if (result.contains("already exist")) {
                if (addRepoTagDialog.isShowing()) {
                    ((TextView)addRepoTagDialog.findViewById(R.id.error_text)).setText(mActivity.getString(R.string.tag_exist));
                }
                return;
            }
            try {
                JSONObject jsonObject = Utils.parseJsonObject(result);
                JSONObject object = jsonObject.getJSONObject("repo_tag");
                SeafRepoTag seafRepoTag = new SeafRepoTag();
                seafRepoTag.setTag_color(object.getString("tag_color"));
                seafRepoTag.setTag_name(object.getString("tag_name"));
                seafRepoTag.setRepo_id(object.getString("repo_id"));
                seafRepoTag.setRepo_tag_id(object.getString("repo_tag_id"));

                NavContext nav = getNavContext();
                DataManager dataManager = getDataManager();
                List<SeafRepo> repos = dataManager.getReposFromCache();
                List<SeafRepo> newRepos = Lists.newArrayListWithCapacity(0);
                List<SeafRepoTag> repoTags = Lists.newArrayListWithCapacity(0);

                for (SeafRepo repo: repos) {
                    if (repo.getID().equals(repoID)) {
                        repoTags.clear();
                        repoTags.addAll(repo.getRepoTags());
                        repoTags.add(seafRepoTag);
                        repo.setRepoTags(repoTags);
                    }
                    newRepos.add(repo);
                }

                try {
                    File cache = dataManager.getFileForReposCache();
                    Utils.writeFile(cache, dataManager.reposToString(newRepos));
                } catch (IOException e) {
                    Log.e(DEBUG_TAG, "Could not write repo cache to disk.", e);
                }
                addRepoTagDialog.dismiss();
                if (fromFile) {
                    if (fileTagsDialog.isShowing() && fileTagAdapter != null) {
                        fileTagAdapter.setRepoTags(repoTags);
                        fileTagAdapter.notifyChanged();
                    }
                } else {
                    if (mActivity.dialogForBrowserMenu.isShowing() && mActivity.repoTagAdapter != null) {
                        mActivity.repoTagAdapter.setItems(repoTags);
                        mActivity.repoTagAdapter.notifyChanged();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class SearchLibrariesTask extends AsyncTask<Void, Void, ArrayList<SeafDirent>> {

        private DataManager dataManager;
        private String query, repoID, path, inputfexts;
        private long startTime, endTime, maxSize, minSize;
        private List<String> ftype;
        private int page;
        private SeafException seafException;

        @Override
        protected void onPreExecute() {
            // show loading view
            showLoading(true);
            mSearchBtn.setEnabled(false);
            mSettingCard.setEnabled(false);
        }

        public SearchLibrariesTask(DataManager dataManager, String query, int page, String repoID, String path, List<String> ftype, String inputfexts, long startTime, long endTime, long maxSize, long minSize) {
            this.dataManager = dataManager;
            this.query = query;
            this.page = page;
            this.repoID = repoID;
            this.path = path;
            this.ftype = ftype;
            this.inputfexts = inputfexts;
            this.startTime = startTime;
            this.endTime = endTime;
            this.maxSize = maxSize;
            this.minSize = minSize;
        }

        @Override
        protected ArrayList<SeafDirent> doInBackground(Void... params) {
            try {
                String mSearchedRlt = dataManager.search(query, page, 100, repoID, path, ftype, inputfexts, startTime, endTime, maxSize, minSize);
                return dataManager.parseSearchResultNew(mSearchedRlt);
            } catch (SeafException e) {
                seafException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<SeafDirent> result) {
            // stop loading view
            showLoading(false);
            mSearchBtn.setEnabled(true);
            mSettingCard.setEnabled(true);

            if (result == null) {
                if (seafException != null) {
                    mErrorText.setVisibility(View.VISIBLE);

                    if (seafException.getCode() == 404)
                        mActivity.showShortToast(mActivity, R.string.search_server_not_support);

                    Log.d(DEBUG_TAG, seafException.getMessage() + " code " + seafException.getCode());
                } else {
                    mErrorText.setVisibility(View.GONE);
                }

                return;
            }

            // update ui
            mSearchedFiles = result;
            adapter.clear();
            mActivity.enableUpButton();
            setToolbarTitle(mActivity.getResources().getString(R.string.search));
            mActivity.isSearchMode = true;
            mActivity.viewRepoMenuCard(false);
            updateAdapterWithDirents(mSearchedFiles, false);
        }
    }

    class SearchLibrariesWithRepoTask extends AsyncTask<Void, Void, ArrayList<SeafDirent>> {

        private DataManager dataManager;
        private SeafRepo repo;
        private String query, path, inputfexts;
        private long startTime, endTime, maxSize, minSize;
        private List<String> ftype;
        private int page;
        private SeafException seafException;

        @Override
        protected void onPreExecute() {
            // show loading view
            showLoading(true);
            mSearchBtn.setEnabled(false);
            mSettingCard.setEnabled(false);
        }

        public SearchLibrariesWithRepoTask(DataManager dataManager, String query, int page, SeafRepo repo, String path, List<String> ftype, String inputfexts, long startTime, long endTime, long maxSize, long minSize) {
            this.dataManager = dataManager;
            this.repo = repo;
            this.query = query;
            this.page = page;
            this.path = path;
            this.ftype = ftype;
            this.inputfexts = inputfexts;
            this.startTime = startTime;
            this.endTime = endTime;
            this.maxSize = maxSize;
            this.minSize = minSize;
        }

        @Override
        protected ArrayList<SeafDirent> doInBackground(Void... params) {
            try {
                String mSearchedRlt = dataManager.search2(query, repo);
                return dataManager.parseSearchResultNew(mSearchedRlt);
            } catch (SeafException e) {
                seafException = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<SeafDirent> result) {
            if (result != null && result.size() > 0) {
                for (SeafDirent item: result) {
                    if (!path.isEmpty()) {
                        if (!Utils.pathSplit(item.path, item.name).contains(path)) {
                            continue;
                        }
                    }
                    if (!item.path.endsWith("/") && item.isDir())
                        item.path = item.path + "/";
                    if (ftype.size() > 0 || !inputfexts.isEmpty()) {
                        if (item.isDir()) continue;
                        boolean flag = false;
                        String ext = Utils.getFileExtension(item.getTitle()).toLowerCase().substring(1);
                        if (ext.isEmpty()) continue;
                        for (String ftypeItem: ftype) {
                            if (ftypeMap.get(ftypeItem) != null) {
                                if (ftypeMap.get(ftypeItem).toLowerCase().contains(ext)) {
                                    flag = true;
                                }
                            }
                        }
                        if (inputfexts.toLowerCase().contains(ext)) {
                            flag = true;
                        }
                        if (!flag) continue;
                    }
                    if (startTime != 0) {
                        if (startTime > item.mtime) {
                            continue;
                        }
                    }
                    if (endTime != 0) {
                        if (endTime < item.mtime) {
                            continue;
                        }
                    }
                    if (minSize != 0) {
                        if (item.isDir()) continue;
                        if (item.size < (minSize)) continue;
                    }
                    if (maxSize != 0) {
                        if (item.isDir()) continue;
                        if (item.size > (maxSize)) continue;
                    }
                    mSearchedFiles.add(item);
                }
            }
            finishSearchLibrariesWithRepoTask();
        }
    }

    private void finishSearchLibrariesWithRepoTask() {
        currentReposSize = currentReposSize - 1;
        if (currentReposSize > 0) return;

        // stop loading view
        showLoading(false);
        mSearchBtn.setEnabled(true);
        mSettingCard.setEnabled(true);

        // update ui
        adapter.clear();
        mActivity.enableUpButton();
        setToolbarTitle(mActivity.getResources().getString(R.string.search));
        mActivity.isSearchMode = true;
        mActivity.viewRepoMenuCard(false);
        updateAdapterWithDirents(mSearchedFiles, false);
    }

    public void clearSearchText() {
        if (mSearchText == null)
            return;
        mSearchText.setText("");
        mSearchText.clearFocus();
    }

    public void openDir(String repoID, String repoName, String path, String permission) {
        mActivity.getNavContext().setRepoID(repoID);
        mActivity.getNavContext().setRepoName(repoName);
        mActivity.getNavContext().setDir(path.equals("/")? path : Utils.removeLastPathSeperator(path), null);
        mActivity.getNavContext().setDirPermission(permission);
        mActivity.isSearchMode = false;
        clearSearchText();

        refresh();
    }

    private boolean checkStarred(String repoID, String path) {
        if (mActivity.getStarredFragment().mStarredFiles != null) {
            for (SeafStarredFile starredFile: mActivity.getStarredFragment().mStarredFiles) {
                if (starredFile.getRepoID().equals(repoID)
                        && Utils.removeLastPathSeperator(starredFile.getPath()).equals(Utils.removeLastPathSeperator(path))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void getThumbImagesCountInEncryptedRepos() {
        mActivity.needUpdateThumbsStatusOfEncRepo = false;
        SettingsManager settingsManager = SettingsManager.instance();

        if (settingsManager.isThumbInEncRepo() && searchEncReposSize != -1) {
            mActivity.showShortToast(mActivity, R.string.thumbnails_calculating);
            return;
        }
        searchEncReposSize = 0;

        mActivity.getSettingsFragment().allEncThumbsCount = 0;
        mActivity.getSettingsFragment().allEncImagesCount = 0;
        mActivity.getSettingsFragment().updateThumbStatus();

        mSearchedImages.clear();
        mSearchedThumbImages = null;

        List<String> repoIds = Lists.newArrayList();
        for (SeafRepo repo: dataManager.getReposFromCache()) {
            if (!repoIds.contains(repo.getID()) && repo.canLocalDecrypt()) {
                repoIds.add(repo.id);
                searchEncReposSize += 1;
                new SearchImagesInEncRepo(dataManager, repo, dataManager.getAccountDir()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        new SearchThumbsInEncRepo(dataManager.getAccountDir()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class SearchImagesInEncRepo extends AsyncTask<Void, Void, Void> {

        private DataManager dataManager;
        private SeafRepo repo;
        private String accountDirPath;

        @Override
        protected void onPreExecute() { }

        public SearchImagesInEncRepo(DataManager dataManager, SeafRepo repo, String accountDirPath) {
            this.dataManager = dataManager;
            this.repo = repo;
            this.accountDirPath = accountDirPath;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String mSearchedRlt = dataManager.search2(".", repo);
                List<SeafDirent> result = dataManager.parseSearchResultNew(mSearchedRlt);
                if (result != null && !result.isEmpty()) {
                    for (SeafDirent item: result) {
                        if (item.isDir()) continue;
                        boolean flag = false;
                        String ext = Utils.getFileExtension(item.getTitle()).toLowerCase().substring(1);
                        if (ext.isEmpty()) continue;
                        if (ftypeMap.get("Image") != null) {
                            if (ftypeMap.get("Image").toLowerCase().contains(ext)) {
                                flag = true;
                            }
                        }
                        if (!flag) continue;
                        String localPath = Utils.pathJoin(accountDirPath, repo.name);
                        localPath = Utils.pathJoin(localPath, item.path);
                        localPath = Utils.getEncThumbPath(localPath);
                        mSearchedImages.add(localPath);
                    }
                }
            } catch (SeafException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            finishSearchImagesInEncRepo();
        }
    }

    private void finishSearchImagesInEncRepo() {
        searchEncReposSize = searchEncReposSize - 1;
        if (searchEncReposSize > 0) {
            return;
        }
        if (mSearchedThumbImages != null) {
            new FinishSearchThumbImagesInEncRepo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    class SearchThumbsInEncRepo extends AsyncTask<Void, Void, ArrayList<String>> {

        private String accountDirPath;
        private SeafException seafException;

        @Override
        protected void onPreExecute() { }

        public SearchThumbsInEncRepo(String accountDirPath) {
            this.accountDirPath = accountDirPath;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> thumbFiles = new ArrayList<>();
            File accountDir = new File(accountDirPath);
            if (!accountDir.exists())
                return null;
            File[] directories = accountDir.listFiles();
            if (directories == null)
                return null;
            List<File> workList = new ArrayList<>(Arrays.asList(directories));
            String thumbSuffix = Utils.getEncThumbSuffix();
            while (!workList.isEmpty()) {
                File dir = workList.remove(0);
                File[] files = dir.listFiles();
                if (files == null)
                    continue;
                for (File file:files) {
                    if (file.isDirectory()) {
                        workList.add(file);
                    } else {
                        String path = file.getAbsolutePath();
                        if (path.contains(thumbSuffix)) {
                            thumbFiles.add(path);
                        }
                    }
                }
            }
            return thumbFiles;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            mSearchedThumbImages = result;
            if (searchEncReposSize == 0) {
                new FinishSearchThumbImagesInEncRepo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    class FinishSearchThumbImagesInEncRepo extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Integer doInBackground(Void... params) {
            int localThumbImages = 0;
            for (String imagePath:mSearchedImages) {
                int index = mSearchedThumbImages.indexOf(imagePath);
                if (index >= 0) {
                    mSearchedThumbImages.remove(index);
                    localThumbImages++;
                }
            }
            return localThumbImages;
        }

        @Override
        protected void onPostExecute(Integer result) {
            searchEncReposSize = -1;
            if (mActivity != null) {
                SettingsFragment settingsFragment = mActivity.getSettingsFragment();
                if (settingsFragment != null) {
                    settingsFragment.updateThumbImagesCount(
                            settingsFragment.allEncThumbsCount + result,
                            settingsFragment.allEncImagesCount + mSearchedImages.size());
                }
            }

            if (mSearchedThumbImages != null) {
                if (!mSearchedThumbImages.isEmpty()) {
                    new DeleteUnnecessaryThumbsInEncRepo(mSearchedThumbImages).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    mSearchedThumbImages.clear();
                }
            }
            mSearchedImages.clear();
        }
    }

    class DeleteUnnecessaryThumbsInEncRepo extends AsyncTask<Void, Void, Void> {

        private List<String> fileList;

        public DeleteUnnecessaryThumbsInEncRepo(List<String> fileList) {
            this.fileList = Lists.newArrayList(fileList);
        }

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {
            while (!fileList.isEmpty()) {
                try {
                    String path = fileList.remove(0);
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception ignored) {
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
        }
    }

    private class LoadDownloadInfo extends AsyncTask<String, Void, Void> {
        SeafException err = null;
        DataManager dataManager;
        String repoId;

        public LoadDownloadInfo(DataManager dataManager) {
            this.dataManager = dataManager;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            repoId = params[0];
            try {
                dataManager.getRepoDownloadInfo(repoId);
            } catch (SeafException e) {
                err = e;
            }
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void v) {

        }
    }
}