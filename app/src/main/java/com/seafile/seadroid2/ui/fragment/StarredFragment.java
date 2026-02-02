package com.seafile.seadroid2.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.data.SeafStarredFile;
import com.seafile.seadroid2.ui.WidgetUtils;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;
import com.seafile.seadroid2.ui.adapter.SeafFileTagAdapter;
import com.seafile.seadroid2.ui.adapter.StarredItemAdapter;
import com.seafile.seadroid2.ui.dialog.PasswordDialog;
import com.seafile.seadroid2.ui.dialog.TaskDialog;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.SupportAsyncTask;
import com.seafile.seadroid2.util.Utils;

import java.util.List;

public class StarredFragment extends Fragment {

    private static final int REFRESH_ON_RESUME = 0;
    private static final int REFRESH_ON_PULL = 1;
    private static final int REFRESH_ON_OVERFLOW_MENU = 2;

    private static int mRefreshType = -1;

    public static final String PASSWORD_DIALOG_STARREDFRAGMENT_TAG = "password_starredfragment";

    private SwipeRefreshLayout refreshLayout;
    private GridView mGridView;
    private TextView mNoStarredView;
    private View mProgressContainer;
    private EditText mSearchText;
    private ImageButton mTextClearBtn;
    private View mListContainer;
    private TextView mErrorText;

    private View mActionModeLayout;
    private TextView mActionModeTitleText;
    private CardView mActionModeDeleteCard;
    private CardView mActionModeCopyCard;
    private CardView mActionModeMoveCard;
    private CardView mActionModeDownloadCard;
    private CardView mActionModeSelectAllCard;
    private CardView mActionModeCloseCard;
    private View mActionModeCopyLayout;
    private View mActionModeMoveLayout;
    private View mActionModeDownloadLayout;

    private BrowserActivity mActivity = null;

    private StarredItemAdapter adapter;
    public List<SeafStarredFile> mStarredFiles;

    private boolean allItemsSelected;
    private boolean mActionMode = false;

    private DataManager getDataManager() {
        return mActivity.getDataManager();
    }

    public StarredItemAdapter getAdapter() {
        return adapter;
    }

    public interface OnStarredFileSelectedListener {
        void onStarredFileSelected(SeafStarredFile starredFile);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BrowserActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.starred_fragment, container, false);
        refreshLayout = root.findViewById(R.id.swiperefresh);
        mGridView = root.findViewById(R.id.grid_view);
        mNoStarredView = root.findViewById(android.R.id.empty);
        mListContainer = root.findViewById(R.id.listContainer);
        mSearchText = root.findViewById(R.id.et_content);
        mTextClearBtn = root.findViewById(R.id.btn_clear);
        mErrorText = root.findViewById(R.id.error_message);
        mProgressContainer = root.findViewById(R.id.progressContainer);
        mActionModeLayout = root.findViewById(R.id.action_mode_layout);
        mActionModeTitleText = mActionModeLayout.findViewById(R.id.action_mode_title_text);
        mActionModeDeleteCard = mActionModeLayout.findViewById(R.id.action_mode_delete_card);
        mActionModeCopyCard = mActionModeLayout.findViewById(R.id.action_mode_copy_card);
        mActionModeMoveCard = mActionModeLayout.findViewById(R.id.action_mode_move_card);
        mActionModeDownloadCard = mActionModeLayout.findViewById(R.id.action_mode_download_card);
        mActionModeSelectAllCard = mActionModeLayout.findViewById(R.id.action_mode_select_all_card);
        mActionModeCloseCard = mActionModeLayout.findViewById(R.id.action_mode_close_card);
        mActionModeCopyLayout = mActionModeLayout.findViewById(R.id.action_mode_copy_layout);
        mActionModeMoveLayout = mActionModeLayout.findViewById(R.id.action_mode_move_layout);
        mActionModeDownloadLayout = mActionModeLayout.findViewById(R.id.action_mode_download_layout);

        refreshLayout.setColorSchemeResources(R.color.luckycloud_green);
        // Set a listener to be invoked when the list should be refreshed.
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshType = REFRESH_ON_PULL;
                refreshView();
            }
        });

        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    mTextClearBtn.setVisibility(View.VISIBLE);
                } else {
                    mTextClearBtn.setVisibility(View.GONE);
                }
                updateAdapterWithStarredFiles(mStarredFiles);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mTextClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchText.getText().clear();
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int gridFilesTypePref = SettingsManager.instance().getGridFilesTypePref();
        int columns = SettingsManager.instance().getGridFilesColumns(gridFilesTypePref);

        adapter = new StarredItemAdapter(mActivity, this);
        adapter.setGridFileType(gridFilesTypePref);
        adapter.setSortValues(SettingsManager.instance().getSortFilesTypePref(),
                SettingsManager.instance().getSortFilesOrderPref());
        mGridView.setNumColumns(columns);
        mGridView.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRefreshType = REFRESH_ON_RESUME;
        refreshView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    public void gridFiles(int type) {
        int columns = SettingsManager.instance().getGridFilesColumns(type);
        mGridView.setNumColumns(columns);
        adapter.setGridFileType(type);
        adapter.notifyChanged();
    }

    public void sortFiles(int type, int order) {
        adapter.setSortValues(type, order);
        adapter.notifyChanged();
        // persist sort settings
        SettingsManager.instance().saveSortFilesPref(type, order);
    }


    public void refresh() {
        mRefreshType = REFRESH_ON_OVERFLOW_MENU;
        refreshView();
    }

    public void refreshView() {

        if (mActivity == null)
            return;

        mErrorText.setVisibility(View.GONE);
        mListContainer.setVisibility(View.VISIBLE);
        if (!Utils.isNetworkOn()) {
            refreshLayout.setRefreshing(false);
            Toast.makeText(mActivity, getString(R.string.network_down), Toast.LENGTH_SHORT).show();
        }
        List<SeafStarredFile> starredFiles = getDataManager().getCachedStarredFiles();
        boolean refreshTimeout = getDataManager().isStarredFilesRefreshTimeout();
        if (mRefreshType == REFRESH_ON_PULL || mRefreshType == REFRESH_ON_OVERFLOW_MENU || starredFiles == null || refreshTimeout)  {
            ConcurrentAsyncTask.execute(new LoadStarredFilesTask(getDataManager()));
        } else {
            updateAdapterWithStarredFiles(starredFiles);
        }
        //mActivity.supportInvalidateOptionsMenu();
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
                refreshView();
            }
        });
    }

    private void showLoading(boolean show) {
        mErrorText.setVisibility(View.GONE);
        if (show) {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_in));
            mListContainer.startAnimation(AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_out));

            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.INVISIBLE);
        } else {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_out));
            mListContainer.startAnimation(AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_in));

            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        }
    }

    private void updateAdapterWithStarredFiles(List<SeafStarredFile> starredFiles) {
        if (starredFiles == null) return;
        mStarredFiles = starredFiles;
        adapter.clear();
        if (starredFiles.size() > 0) {
            for (SeafStarredFile starred : starredFiles) {
                if (starred.getTitle().trim().toLowerCase().contains(mSearchText.getText().toString().trim().toLowerCase())) {
                    adapter.add(starred);
                }
            }
            adapter.notifyChanged();
            mGridView.setVisibility(View.VISIBLE);
            mNoStarredView.setVisibility(View.GONE);
        } else {
            mGridView.setVisibility(View.GONE);
            mNoStarredView.setVisibility(View.VISIBLE);
        }
    }

    public void listItemLongClick(int position) {
        startContextualActionMode(position);
    }

    public void listItemClick(int position) {
        // handle action mode selections
        if (mActionMode) {
            // add or remove selection for current list item
            if (adapter == null) return;

            adapter.toggleSelection(position);
            updateContextualActionBar();
            return;
        }

        final SeafStarredFile starredFile = (SeafStarredFile) adapter.getItem(position);
        if (starredFile.isDir()) {
            onStarredDirSelected(starredFile);
        } else {
            mActivity.onStarredFileSelected(starredFile);
        }
    }

    public void showPasswordDialog(String repoName, String repoID,
                                             TaskDialog.TaskDialogListener listener, String password) {
        PasswordDialog passwordDialog = new PasswordDialog();
        passwordDialog.setRepo(repoName, repoID, mActivity.getAccount());
        if (password != null) {
            passwordDialog.setPassword(password);
        }
        passwordDialog.setTaskDialogLisenter(listener);
        passwordDialog.show(mActivity.getSupportFragmentManager(), PASSWORD_DIALOG_STARREDFRAGMENT_TAG);
    }

    public void onStarredDirSelected(SeafStarredFile starredFile) {
        final String repoID = starredFile.getRepoID();
        final SeafRepo repo = getDataManager().getCachedRepoByID(repoID);
        final String repoName = repo.getName();
        final String filePath = starredFile.getPath();
        final String permission = starredFile.getPermission();

        if (starredFile.isRepo_encrypted()) {
            if (repo.encrypted && !getDataManager().getRepoPasswordSet(repo.id)) {
                String password = getDataManager().getRepoPassword(repo.id);
                showPasswordDialog(repo.name, repo.id,
                        new TaskDialog.TaskDialogListener() {
                            @Override
                            public void onTaskSuccess() {
                                WidgetUtils.showStarredRepo(mActivity, repoID, repoName, filePath, permission);
                            }
                        }, password);

                return;
            } else {
                WidgetUtils.showStarredRepo(mActivity, repoID, repoName, filePath, permission);
            }
        } else {
            WidgetUtils.showStarredRepo(mActivity, repoID, repoName, filePath, permission);
        }
        return;
    }

    private void unStarFiles(List<SeafStarredFile> starredFiles) {
        for (SeafStarredFile seafStarredFile : starredFiles) {
            doUnStarFile(seafStarredFile.getRepoID(), seafStarredFile.getPath());
        }
    }

    public void doUnStarFile(String repoID, String path) {
        if (!Utils.isNetworkOn()) {
            mActivity.showShortToast(mActivity, R.string.network_down);
            return;
        }

        ConcurrentAsyncTask.execute(new UnStarFileTask(repoID, path));

    }

    public void doStarFile(String repoID, String path, String filename, SeafDirent dirent) {

        if (!Utils.isNetworkOn()) {
            mActivity.showShortToast(mActivity, R.string.network_down);
            return;
        }

        String p = Utils.pathJoin(path, filename);
        ConcurrentAsyncTask.execute(new StarFileTask(repoID, p, dirent));
    }

    private class LoadStarredFilesTask extends SupportAsyncTask<BrowserActivity, Void, Void, List<SeafStarredFile>> {

        SeafException err = null;

        DataManager dataManager;

        public LoadStarredFilesTask(DataManager dataManager) {
            super(mActivity);
            this.dataManager = dataManager;
        }

        @Override
        protected void onPreExecute() {
            if (mRefreshType != REFRESH_ON_PULL)
                showLoading(true);
        }

        @Override
        protected List<SeafStarredFile> doInBackground(Void... params) {

            try {
                List<SeafStarredFile> starredFiles = dataManager.getStarredFiles();
//                //TODO Data needs to be fetched from the server
//                if (starredFiles != null && !starredFiles.isEmpty()) {
//                    for (int i = 0; i < starredFiles.size(); i++) {
//                        if (!starredFiles.get(i).isDir()) {
//                            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
//                            File file = new File(root + "" + starredFiles.get(i).getPath());
//                            if (file.exists()){
//                                long l = FileTools.getSimpleSize(root + "" + starredFiles.get(i).getPath());
//                                starredFiles.get(i).setSize(l);
//                            }
//                        }
//                    }
//                }
                return starredFiles;
            } catch (SeafException e) {
                err = e;
                return null;
            }

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<SeafStarredFile> starredFiles) {
            if (getContextParam() == null)
                // this occurs if user navigation to another activity
                return;

            if (mRefreshType == REFRESH_ON_RESUME || mRefreshType == REFRESH_ON_OVERFLOW_MENU) {
                showLoading(false);
            } else if (mRefreshType == REFRESH_ON_PULL) {
                // Call onRefreshComplete when the list has been refreshed.
                //mGridView.onRefreshComplete(getDataManager().getLastPullToRefreshTime(DataManager.PULL_TO_REFRESH_LAST_TIME_FOR_STARRED_FRAGMENT));
                getDataManager().saveLastPullToRefreshTime(System.currentTimeMillis(), DataManager.PULL_TO_REFRESH_LAST_TIME_FOR_STARRED_FRAGMENT);
                refreshLayout.setRefreshing(false);
            }


            if (err != null) {
                if (err == SeafException.remoteWipedException) {
                    if (getContextParam() != null) {
                        getContextParam().completeRemoteWipe();
                    }
                } else {
                    showError(getString(R.string.error_when_load_starred));
                    return;
                }
            }

            if (starredFiles == null) {
                showError(getString(R.string.error_when_load_starred));
                return;
            }

            updateAdapterWithStarredFiles(starredFiles);
        }
    }

    private class StarFileTask extends SupportAsyncTask<BrowserActivity, Void, Void, Void> {
        private String repoId;
        private String path;
        private SeafDirent dirent;
        private SeafException err;

        public StarFileTask(String repoId, String path, SeafDirent dirent) {
            super(mActivity);
            this.repoId = repoId;
            this.path = path;
            this.dirent = dirent;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getContextParam() != null) {
                    getContextParam().getDataManager().star(repoId, path);
                }
            } catch (SeafException e) {
                err = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (err != null) {
                if (getContextParam() != null) {
                    getContextParam().showShortToast(getContextParam(), R.string.star_file_failed);
                }
                return;
            }

            SeafStarredFile starredFile = new SeafStarredFile();
            starredFile.setRepoID(repoId);
            starredFile.setPath(path);
            starredFile.setMtime(dirent.mtime);
            starredFile.setSize(dirent.size);
            starredFile.setObj_name(dirent.name);
            SeafRepo repo = getDataManager().getCachedRepoByID(repoId);
            starredFile.setRepo_encrypted(repo.encrypted);
            starredFile.setType(dirent.isDir()? SeafStarredFile.FileType.DIR : SeafStarredFile.FileType.FILE);
            starredFile.setRepoName(repo.name);
            if (!starredFile.isDir())
                starredFile.fileTags = Lists.newArrayListWithCapacity(0);

            mStarredFiles.add(starredFile);
            saveCachedStarredFiles();
            refresh();

            if (getContextParam() != null) {
                getContextParam().showShortToast(getContextParam(), R.string.star_file_succeed);
            }
        }
    }

    private class UnStarFileTask extends SupportAsyncTask<BrowserActivity, Void, Void, Void> {
        private String repoId;
        private String path;
        private SeafException err;

        public UnStarFileTask(String repoId, String path) {
            super(mActivity);
            this.repoId = repoId;
            this.path = path;
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                if (getContextParam() != null) {
                    getContextParam().getDataManager().unstar(repoId, path);
                }
            } catch (SeafException e) {
                err = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (err != null) {
                if (getContextParam() != null) {
                    getContextParam().showShortToast(getContextParam(), R.string.unstar_file_failed);
                }
                return;
            }

            for (int i = 0; i < mStarredFiles.size(); i++) {
                if (mStarredFiles.get(i).getRepoID().equals(repoId)
                        && Utils.removeLastPathSeperator(mStarredFiles.get(i).getPath()).equals(Utils.removeLastPathSeperator(path))) {
                    mStarredFiles.remove(i);
                    break;
                }
            }

            mRefreshType = REFRESH_ON_RESUME;
            refreshView();
            adapter.deselectAllItems();
            mActionModeTitleText.setText(getResources().
                    getQuantityString(R.plurals.transfer_list_items_selected, 0, 0));
            mActivity.showShortToast(mActivity, R.string.unstar_file_succeed);
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

        if (adapter == null) return;

        adapter.toggleSelection(position);
        updateContextualActionBar();

    }

    public void startContextualActionMode() {
        if (!mActionMode) {
            // start the actionMode
            startSupportActionMode();
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
            mActionModeTitleText.setText(getResources().getQuantityString(
                    R.plurals.transfer_list_items_selected,
                    adapter.getCheckedItemCount(),
                    adapter.getCheckedItemCount()));
        }

    }

    private void startSupportActionMode() {

        initActionModeCards();

        mActionMode = true;
        mActionModeLayout.setVisibility(View.VISIBLE);
        if (adapter == null) return;
        adapter.setActionModeOn(true);
        adapter.notifyDataSetChanged();
    }

    private void initActionModeCards() {
        mActionModeCopyLayout.setVisibility(View.GONE);
        mActionModeMoveLayout.setVisibility(View.GONE);
        mActionModeDownloadLayout.setVisibility(View.GONE);
        mActionModeDeleteCard.setOnClickListener(v -> {
            final List<SeafStarredFile> starredFiles = adapter.getSelectedItemsValues();
            if (starredFiles.size() == 0) {
                mActivity.showShortToast(mActivity, R.string.action_mode_no_items_selected);
                return;
            }
            unStarFiles(starredFiles);
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

    public void showDirBottomSheet(String title, final SeafStarredFile dirent) {
        String repoName;
        String repoID;
        String dir;
        String path;

        SeafRepo repo = getDataManager().getCachedRepoByID(dirent.getRepoID());
        repoName = repo.name;
        repoID = repo.id;
        dir = Utils.pathSplit(dirent.getPath(), dirent.getObj_name());
        path = dirent.getPath();

        final String filename = dirent.getObj_name();

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

        deleteCard.setVisibility(View.GONE);
        copyCard.setVisibility(View.GONE);
        moveCard.setVisibility(View.GONE);
        renameCard.setVisibility(View.GONE);
        downloadCacheCard.setVisibility(View.GONE);
        downloadDeviceCard.setVisibility(View.GONE);
        addFavouritesCard.setVisibility(View.GONE);

        titleText.setText(title);
        pathText.setText(Utils.pathJoin(repoName, path));
        subtitleText.setText(dirent.getSubtitle());

        closeCard.setOnClickListener(v -> {
            dialog.dismiss();
        });
        shareCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.showShareDialog(repoID, path, ShareDialogActivity.SHARE_DIALOG_FOR_DIR, dirent.getSize(), dirent.getObj_name());
        });
        removeFavouritesCard.setOnClickListener(v -> {
            dialog.dismiss();
            doUnStarFile(repoID, path);
        });

        if (repo != null && repo.encrypted) {
            shareCard.setVisibility(View.GONE);
        }

        dialog.show();
    }

    public void showFileBottomSheet(String title, final SeafStarredFile starredFile) {
        String repoName;
        String repoID;
        String dir;
        String path;

        SeafRepo repo = getDataManager().getCachedRepoByID(starredFile.getRepoID());
        repoName = repo.name;
        repoID = repo.id;
        dir = Utils.pathSplit(starredFile.getPath(), starredFile.getObj_name());
        path = starredFile.getPath();

        final String filename = starredFile.getObj_name();

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
        CardView closeCard = dialog.findViewById(R.id.close_card);

        convertToPdfCard.setVisibility(View.GONE);
        downloadCacheCard.setVisibility(View.GONE);
        downloadDeviceCard.setVisibility(View.GONE);
        deleteCard.setVisibility(View.GONE);
        renameCard.setVisibility(View.GONE);
        copyCard.setVisibility(View.GONE);
        moveCard.setVisibility(View.GONE);
        addFavouritesCard.setVisibility(View.GONE);
        openWithCard.setVisibility(View.GONE);
        uploadCard.setVisibility(View.GONE);

        titleText.setText(title);
        pathText.setText(Utils.pathJoin(repoName, path));
        subtitleText.setText(starredFile.getSubtitle());

        closeCard.setOnClickListener(v -> {
            dialog.dismiss();
        });
        shareCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.showShareDialog(repoID, path, ShareDialogActivity.SHARE_DIALOG_FOR_FILE, starredFile.getSize(), starredFile.getObj_name());
        });
        exportCard.setOnClickListener(v -> {
            dialog.dismiss();
            mActivity.exportFile(repoID, dir, starredFile.getObj_name(), starredFile.getSize());
        });
        removeFavouritesCard.setOnClickListener(v -> {
            dialog.dismiss();
            doUnStarFile(repoID, path);
        });
        openParentDirCard.setOnClickListener(v -> {
            dialog.dismiss();
            WidgetUtils.showStarredRepo(mActivity, repoID, repoName, dir, null);
        });
        tagsCard.setOnClickListener(v -> {
            dialog.dismiss();
            SeafDirent dirent = new SeafDirent();
            dirent.repoID = repoID;
            dirent.path = dir;
            dirent.name = filename;
            dirent.isSearchedFile = true;
            dirent.fileTags = Lists.newArrayListWithCapacity(0);
            mActivity.getReposFragment().selectFileTagsDialog(repoID, dirent, SeafFileTagAdapter.FragmentType.Starred);
        });

        if (repo != null && repo.encrypted) {
            shareCard.setVisibility(View.GONE);
        }

        dialog.show();
    }

    public void saveCachedStarredFiles() {
        getDataManager().saveCachedStarredFiles(mStarredFiles);
    }
}
