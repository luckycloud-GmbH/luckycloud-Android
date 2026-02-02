package com.seafile.seadroid2.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.data.SeafPhoto;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.data.SeafRepoTag;
import com.seafile.seadroid2.data.SeafStarredFile;
import com.seafile.seadroid2.transfer.DownloadStateListener;
import com.seafile.seadroid2.transfer.DownloadTask;
import com.seafile.seadroid2.ui.CopyMoveContext;
import com.seafile.seadroid2.ui.HackyViewPager;
import com.seafile.seadroid2.ui.ZoomOutPageTransformer;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;
import com.seafile.seadroid2.ui.adapter.GalleryAdapter;
import com.seafile.seadroid2.ui.adapter.SeafFileTagAdapter2;
import com.seafile.seadroid2.ui.adapter.SeafTagColorAdapter;
import com.seafile.seadroid2.ui.adapter.SeafTagColorAdapter3;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Choose an app from a list of apps or custom actions
 */
public class GalleryDialog extends DialogFragment {
    public static final String DEBUG_TAG = "GalleryActivity";

    private HackyViewPager mViewPager;
    private LinearLayout mPageIndexContainer;
    private TextView mPageIndexTextView;
    private TextView mPageCountTextView;
    private TextView mPageNameTextView;

    private View mBackLayout;
    private CardView mBackBtn;
    private View mPageNameLayout;
    public CardView mTagsBtn;
    private CardView mStarBtn;
    private ImageView mStarImage;
    private CardView mShareBtn;
    private CardView mExportBtn;
    private CardView mDeleteBtn;
    private CardView mMoreBtn;
    private CardView mDownloadBtn;
    private CardView mCopyBtn;
    private CardView mMoveBtn;
    private View mPopupGalleryMoreView;
    private RecyclerView mTagColorRecycler;
    private SeafTagColorAdapter3 colorAdapter;

    private BrowserActivity mActivity;
    private GalleryDialog mFragment;
    private DataManager dataMgr;
    private Account mAccount;
    private LinearLayout mToolbar;
    private String repoName;
    private String repoID;
    private String dirPath;
    private String fileName;
    private boolean downloadshowstatus;
    private String STATE_FILE_NAME;
    private int mPageIndex;
    private GalleryAdapter mGalleryAdapter;
    private int mStarredIndex = -1;
    private List<SeafPhoto> mPhotos = Lists.newArrayList();
    private SeafPhoto mPhoto;
    public static int taskID;
    private int count;
    private  static final int TALLY = 3;
    private CustomProgressDialog progressDialog;
    private PopupWindow mDropdown = null;
    private List<SeafStarredFile> mStarredFiles;

    private Dialog addRepoTagDialog;
    private SeafFileTagAdapter2 fileTagAdapter;
    public boolean needBrowserActivityRefresh = false;

    /** flag to mark if the tool bar was shown */
    private boolean showToolBar = false;

    public void init(BrowserActivity activity, String repoID, String repoName, String dirPath, String fileName) {
        this.mActivity = activity;
        this.repoID = repoID;
        this.repoName = repoName;
        this.dirPath = dirPath;
        this.fileName = fileName;

        this.mFragment = this;
        this.mAccount = activity.getAccount();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (mActivity == null) {
            dismiss();
            return super.onCreateDialog(savedInstanceState);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.FullScreenDialog);
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View view = (View) inflater.inflate(R.layout.dialog_gallery, null);

        //mDownloadBtn = (CardView) view.findViewById(R.id.gallery_download_card);
        mBackLayout = (View) view.findViewById(R.id.back_layout);
        mBackBtn = (CardView) view.findViewById(R.id.back_card);
        mPageNameLayout = (View) view.findViewById(R.id.page_name_layout);
        mTagsBtn = (CardView) view.findViewById(R.id.gallery_tags_card);
        mStarBtn = (CardView) view.findViewById(R.id.gallery_star_card);
        mStarImage = (ImageView) view.findViewById(R.id.gallery_star_image);
        mShareBtn = (CardView) view.findViewById(R.id.gallery_share_card);
        mExportBtn = (CardView) view.findViewById(R.id.gallery_export_card);
        mDeleteBtn = (CardView) view.findViewById(R.id.gallery_delete_card);
        mMoreBtn = (CardView) view.findViewById(R.id.gallery_more_card);
        mPopupGalleryMoreView = view.findViewById(R.id.popup_gallery_more_layout);

        mToolbar = (LinearLayout) view.findViewById(R.id.gallery_tool_bar);
//        mDownloadBtn.setOnClickListener(v -> {
//            downloadFileOnCache(repoID, dirPath, fileName);
//        });
        mBackBtn.setOnClickListener(v -> {
            dismiss();
        });
        mTagsBtn.setOnClickListener(v -> {
            selectFileTagsDialog(mPhoto);
        });
        mStarBtn.setOnClickListener(v -> {
            starFile(repoID, dirPath, fileName);
        });
        mShareBtn.setOnClickListener(v -> {
            showShareDialog(repoID, Utils.pathJoin(dirPath, fileName), ShareDialogActivity.SHARE_DIALOG_FOR_FILE, mPhoto.getDirent().size, fileName);
        });
        mExportBtn.setOnClickListener(v -> {
            mActivity.exportFile(repoID, dirPath, mPhoto.getName(), mPhoto.getDirent().getFileSize());
        });
        mDeleteBtn.setOnClickListener(v -> {
            deleteFile(repoID, Utils.pathJoin(dirPath, fileName));
        });
        mMoreBtn.setOnClickListener(v -> {
            showMorePopup();
        });

        mViewPager = (HackyViewPager) view.findViewById(R.id.gallery_pager);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.setOffscreenPageLimit(1);

        mViewPager.setOnPageChangeListener(new HackyViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // page index starting from 1 instead of 0 in user interface, so plus one here
                mPageIndex = position;
                setPageValue();


                // fixed IndexOutOfBoundsException when accessing list
                if (mPageIndex == mPhotos.size()) return;
                mPhoto = mPhotos.get(mPageIndex);
                fileName = mPhoto.getName();
                downloadshowstatus = mPhoto.getDownloaded();
//                if (downloadshowstatus) {
//                    mDownloadBtn.setVisibility(View.GONE);
//                } else {
//                    mDownloadBtn.setVisibility(View.VISIBLE);
//                }
                mPageNameTextView.setText(fileName);
                colorAdapter.setDirent(mPhoto.getDirent());
            }

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        mPageIndexContainer = (LinearLayout) view.findViewById(R.id.page_index_container);
        mPageIndexTextView = (TextView) view.findViewById(R.id.gallery_page_index);
        mPageCountTextView = (TextView) view.findViewById(R.id.gallery_page_count);
        mPageNameTextView = (TextView) view.findViewById(R.id.gallery_page_name);

        dataMgr = new DataManager(mAccount);
        mStarredFiles = dataMgr.getCachedStarredFiles();

        displayPhotosInGallery(repoName, repoID, dirPath, false);

        hideOrShowToolBar();

        mTagColorRecycler = view.findViewById(R.id.tag_color_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
        mTagColorRecycler.setLayoutManager(layoutManager);
        colorAdapter = new SeafTagColorAdapter3(mFragment);
        mTagColorRecycler.setAdapter(colorAdapter);
        
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mActivity.closeGalleryDialog();
                }
                return true;
            }
        });

        return dialog;
    }

    private void setPageValue() {
        // page index starting from 1 instead of 0 in user interface, so plus one here
        mPageIndexTextView.setText(String.valueOf(mPageIndex + 1));

        SeafPhoto photo = mPhotos.get(mPageIndex);
        mStarredIndex = -1;
        for (int i = 0; i < mStarredFiles.size(); i++) {
            if (mStarredFiles.get(i).getRepoID().equals(photo.getRepoID())
                    && mStarredFiles.get(i).getPath().equals(Utils.pathJoin(photo.getDirPath(), photo.getName()))) {
                mStarredIndex = i;
                break;
            }
        }
        if (mActivity == null)
            return;
        if (mStarredIndex != -1) {
            mStarImage.setImageDrawable(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.ic_star_full, null));
        } else {
            mStarImage.setImageDrawable(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.ic_star_border, null));
        }
    }

    /**
     * Load thumbnail urls in order to display them in the gallery.
     * Prior to use caches to calculate those urls.
     * If caches are not available, load them asynchronously.
     *
     * NOTE: When user browsing files in "LIBRARY" tab, he has to navigate into a repo in order to open gallery.
     * Method which get called is {@link com.seafile.seadroid2.ui.fragment.ReposFragment#navToReposView(boolean, boolean)} or {@link com.seafile.seadroid2.ui.fragment.ReposFragment#navToDirectory(boolean, boolean)},
     * so seafDirents were already cached and it will always use them to calculate thumbnail urls for displaying photos in gallery.
     * But for browsing "STARRED" tab, caches of starred files may or may not cached, that is where the asynchronous loading code segment comes into use.
     * @param repoID
     * @param dirPath
     */
    private void displayPhotosInGallery(String repoName, String repoID, String dirPath, boolean refresh) {
        // calculate thumbnail urls by cached dirents
        List<SeafDirent> seafDirents = dataMgr.getCachedDirents(repoID, dirPath);
        if (seafDirents != null && !refresh) {
            // sort files by type and order
            seafDirents = sortFiles(seafDirents,
                    SettingsManager.instance().getSortFilesTypePref(),
                    SettingsManager.instance().getSortFilesOrderPref());
            for (SeafDirent seafDirent : seafDirents) {
                if (!seafDirent.isDir()
                        && Utils.isViewableImage(seafDirent.name)) {
                    mPhotos.add(new SeafPhoto(repoName, repoID, dirPath, seafDirent));
                }
            }

            mGalleryAdapter = new GalleryAdapter(mActivity,
                    mFragment,
                    mAccount,
                    mPhotos,
                    dataMgr);
            mViewPager.setAdapter(mGalleryAdapter);

            navToSelectedPage();
        } else {
            if (!Utils.isNetworkOn()) {
                mActivity.showShortToast(mActivity, R.string.network_down);
                // data is not available
                dismiss();
            }

            // load photos asynchronously
            LoadPhotosTask task = new LoadPhotosTask(repoName, repoID, dirPath);
            ConcurrentAsyncTask.execute(task);
        }

    }

    /**
     * Load photos asynchronously, use {@link SeafPhoto} to manage state of each photo instance
     */
    private class LoadPhotosTask extends AsyncTask<String, Void, List<SeafPhoto>> {
        private String repoName, repoID, dirPath;
        private SeafException err = null;

        public LoadPhotosTask(String repoName, String repoID, String dirPath) {
            this.repoName = repoName;
            this.repoID = repoID;
            this.dirPath = dirPath;
        }

        @Override
        protected List<SeafPhoto> doInBackground(String... params) {
            List<SeafPhoto> photos = Lists.newArrayList();
            List<SeafDirent> seafDirents;
            try {
                seafDirents = dataMgr.getDirentsFromServer(repoID, dirPath);
            } catch (SeafException e) {
                err = e;
                return null;
            }

            if (seafDirents == null)
                return null;

            // sort photos according to global sort settings
            seafDirents = sortFiles(seafDirents,
                    SettingsManager.instance().getSortFilesTypePref(),
                    SettingsManager.instance().getSortFilesOrderPref());
            for (SeafDirent seafDirent : seafDirents) {
                if (!seafDirent.isDir()
                        && Utils.isViewableImage(seafDirent.name)) {
                    photos.add(new SeafPhoto(repoName, repoID, dirPath, seafDirent));
                }
            }
            return photos;
        }

        @Override
        protected void onPostExecute(List<SeafPhoto> photos) {
            if (photos.isEmpty()
                    || fileName == null) {
                if (err != null) {
                    mActivity.showShortToast(mActivity, R.string.gallery_load_photos_error);
                    Log.e(DEBUG_TAG, "error message " + err.getMessage() + " error code " + err.getCode());
                }

                return;
            }

            mPhotos = photos;
            mGalleryAdapter = new GalleryAdapter(mActivity, mFragment, mAccount, photos, dataMgr);
            mViewPager.setAdapter(mGalleryAdapter);

            navToSelectedPage();
        }
    }

    /**
     * Sorts the given list by type and order.
     * Sorting type is one of {@link SettingsManager#SORT_BY_NAME} or {@link SettingsManager#SORT_BY_LAST_MODIFIED_TIME}.
     * Sorting order is one of {@link SettingsManager#SORT_ORDER_ASCENDING} or {@link SettingsManager#SORT_ORDER_DESCENDING}.
     *
     * @param dirents
     * @param type
     * @param order
     * @return sorted file list
     */
    public List<SeafDirent> sortFiles(List<SeafDirent> dirents, int type, int order) {
        // sort SeafDirents
        if (type == SettingsManager.SORT_BY_NAME) {
            // sort by name, in ascending order
            Collections.sort(dirents, new SeafDirent.DirentNameComparator());
            if (order == SettingsManager.SORT_ORDER_DESCENDING) {
                Collections.reverse(dirents);
            }
        } else if (type == SettingsManager.SORT_BY_LAST_MODIFIED_TIME) {
            // sort by last modified time, in ascending order
            Collections.sort(dirents,   new SeafDirent.DirentLastMTimeComparator());
            if (order == SettingsManager.SORT_ORDER_DESCENDING) {
                Collections.reverse(dirents);
            }
        }
        return dirents;
    }

    /**
     * Dynamically navigate to the starting page index selected by user
     * by default the starting page index is 0
     *
     */
    private void navToSelectedPage() {
        int size = mPhotos.size();
        for (int i = 0; i < size; i++) {
            if (mPhotos.get(i).getName().equals(fileName)) {
                mViewPager.setCurrentItem(i);
                mPageIndex = i;
                setPageValue();
                mPageNameTextView.setText(fileName);
                break;
            }
        }
        mPageCountTextView.setText(String.valueOf(size));
    }

    /**
     * This method will get called when tapping at the center of a photo,
     * tool bar will auto hide when open the gallery,
     * and will show or hide alternatively when tapping.
     */
    public void hideOrShowToolBar() {
        int marginHorizontal;
        if (showToolBar) {
            mToolbar.setVisibility(View.VISIBLE);
            mBackLayout.setVisibility(View.VISIBLE);
            mPageIndexContainer.setVisibility(View.VISIBLE);
            mPageNameLayout.setVisibility(View.VISIBLE);
            marginHorizontal = (int) mActivity.getResources().getDimension(R.dimen.gallery_margin_horizontal);
        } else {
            mToolbar.setVisibility(View.GONE);
            mBackLayout.setVisibility(View.GONE);
            mPageIndexContainer.setVisibility(View.GONE);
            mPageNameLayout.setVisibility(View.GONE);
            marginHorizontal = 0;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mViewPager.getLayoutParams();
        params.setMargins(marginHorizontal, 0, marginHorizontal, 0);
        mViewPager.setLayoutParams(params);

        showToolBar = !showToolBar;
    }

    private void downloadFileOnCache(String repoID, String dirPath, String fileName) {
        progressDialog = new CustomProgressDialog(mActivity);
        progressDialog.setMessage(getString(R.string.notification_download_started_title));
        progressDialog.show();
        final String filePath = Utils.pathJoin(dirPath, fileName);
        GallerySeeOriginals(repoName, repoID, filePath);
    }

    private void downloadFileOnDevice(String repoID, String dirPath, String fileName) {
        String dir;
        String path;
        SeafDirent dirent = mPhoto.getDirent();
        if (dirent.isSearchedFile) {
            dir = Utils.pathSplit(dirent.path, dirent.name);
            path = dirent.path;
        } else {
            dir = mActivity.getNavContext().getDirPath();
            path = Utils.pathJoin(dir, dirent.name);
        }

        mActivity.downloadFileonDevice(repoID, dir, dirent.name, path);
    }

    private void deleteFile(String repoID, String path) {
        final DeleteFileDialog dialog = new DeleteFileDialog();
        dialog.init(repoID, path, false, mAccount);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                mActivity.showShortToast(mActivity, R.string.delete_successful);
                removePageAndRefreshView();
            }
        });
        dialog.show(mActivity.getSupportFragmentManager(), "DialogFragment");
    }

    private void starFile(String repoId, String dir, String fileName) {
        if (!Utils.isNetworkOn()) {
            mActivity.showShortToast(mActivity, R.string.network_down);
            return;
        }

        String p = Utils.pathJoin(dir, fileName);
        if (mStarredIndex == -1) {
            ConcurrentAsyncTask.execute(new StarFileTask(repoId, p));
        } else {
            ConcurrentAsyncTask.execute(new UnStarFileTask(repoId, p));
        }
    }

    private void renameFile(String repoID, String repoName, String path) {
        final RenameFileDialog dialog = new RenameFileDialog();
        dialog.init(repoID, path, false, mAccount);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                mActivity.showShortToast(mActivity, R.string.rename_successful);
                displayPhotosInGallery(repoName, repoID, dirPath, true);
            }
        });
        dialog.show(mActivity.getSupportFragmentManager(), BrowserActivity.TAG_RENAME_FILE_DIALOG_FRAGMENT);
    }

    public void copyFile(String srcRepoId, String srcRepoName, String srcDir, String srcFn) {
        mActivity.chooseCopyMoveDest(srcRepoId, srcRepoName, srcDir, srcFn, false, CopyMoveContext.OP.COPY);
    }

    public void moveFile(String srcRepoId, String srcRepoName, String srcDir, String srcFn) {
        mActivity.chooseCopyMoveDest(srcRepoId, srcRepoName, srcDir, srcFn, false, CopyMoveContext.OP.MOVE);
    }

    class StarFileTask extends AsyncTask<Void, Void, Void> {
        private String repoId;
        private String path;
        private SeafException err;

        public StarFileTask(String repoId, String path) {
            this.repoId = repoId;
            this.path = path;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (dataMgr == null)
                return null;

            try {
                dataMgr.star(repoId, path);
            } catch (SeafException e) {
                err = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (err != null) {
                mActivity.showShortToast(mActivity, R.string.star_file_failed);
                return;
            }

            SeafStarredFile starredFile = new SeafStarredFile();
            SeafPhoto photo = mPhotos.get(mPageIndex);
            starredFile.setRepoID(photo.getRepoID());
            starredFile.setPath(Utils.pathJoin(photo.getDirPath(), photo.getName()));
            starredFile.setMtime(photo.getDirent().mtime);
            starredFile.setSize(photo.getDirent().size);
            starredFile.setObj_name(photo.getName());
            SeafRepo repo = dataMgr.getCachedRepoByID(photo.getRepoID());
            starredFile.setRepo_encrypted(repo.encrypted);
            starredFile.setType(SeafStarredFile.FileType.FILE);
            starredFile.setRepoName(repo.name);
            starredFile.fileTags = Lists.newArrayListWithCapacity(0);

            mStarredFiles.add(starredFile);

            setPageValue();
            mActivity.showShortToast(mActivity, R.string.star_file_succeed);
        }
    }

    class UnStarFileTask extends AsyncTask<Void, Void, Void> {
        private String repoId;
        private String path;
        private SeafException err;

        public UnStarFileTask(String repoId, String path) {
            this.repoId = repoId;
            this.path = path;
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                dataMgr.unstar(repoId, path);
            } catch (SeafException e) {
                err = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (err != null) {
                mActivity.showShortToast(mActivity, R.string.unstar_file_failed);
                return;
            }

            if (mStarredIndex != -1) {
                mActivity.showShortToast(mActivity, R.string.unstar_file_succeed);
                mStarredFiles.remove(mStarredIndex);
                setPageValue();
            }
        }
    }

    /**
     * slide to next page if there are pages on the right side of the current one,
     * slide to previous page if not,
     * quit the gallery if both cases were not met
     */
    private void removePageAndRefreshView() {
        mPhotos.remove(mPageIndex);
        mGalleryAdapter.setItems(mPhotos);
        mGalleryAdapter.notifyDataSetChanged();
        int size = mPhotos.size();
        mPageCountTextView.setText(String.valueOf(size));

        if (size == 0) {
            dismiss();
            return;
        }

        mPageIndex = mPageIndex > size - 1 ? size -1 : mPageIndex;
        setPageValue();


        // update file name in gallery view
        mPageNameTextView.setText(mPhotos.get(mPageIndex).getName());

    }

    private void GallerySeeOriginals(String repoName, String repoID, String filePath) {
        ConcurrentAsyncTask.execute(new DownloadTask(++taskID, mAccount, repoName, repoID, filePath, true, false, -1, new DownloadStateListener() {
            @Override
            public void onFileDownloadProgress(int taskID) {

            }

            @Override
            public void onFileDownloaded(int taskID) {
                if (mGalleryAdapter != null) {
                    mGalleryAdapter.downloadPhoto();
                }
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFileDownloadFailed(int taskID) {
                count++;
                if (count < TALLY) {
                    downloadFileOnCache(repoID, dirPath, fileName);
                } else {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }

            }
        }));
    }

    private void showMorePopup() {
        if (mActivity == null) {
            return;
        }

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_gallery_more_dialog, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mDropdown = new PopupWindow(layout, mPopupGalleryMoreView.getWidth(),
                    ((downloadshowstatus? 3 : 4) * mPopupGalleryMoreView.getHeight() / 4),true);//FrameLayout.LayoutParams.WRAP_CONTENT

            final CardView renameCard = layout.findViewById(R.id.gallery_rename_card);
            final CardView downloadCacheCard = layout.findViewById(R.id.gallery_download_cache_card);
            final View downloadCacheBottom = layout.findViewById(R.id.download_bottom_view);
            final CardView saveOnDeviceCard = layout.findViewById(R.id.gallery_save_on_device_card);
            final CardView copyCard = layout.findViewById(R.id.gallery_copy_card);
            final CardView moveCard = layout.findViewById(R.id.gallery_move_card);

            downloadCacheCard.setVisibility(downloadshowstatus? View.GONE : View.VISIBLE);
            downloadCacheBottom.setVisibility(downloadshowstatus? View.GONE : View.VISIBLE);

            renameCard.setOnClickListener(v -> {
                renameFile(repoID, repoName, Utils.pathJoin(dirPath, fileName));
            });
            downloadCacheCard.setOnClickListener(v -> {
                downloadFileOnCache(repoID, dirPath, fileName);
                mDropdown.dismiss();
            });
            saveOnDeviceCard.setOnClickListener(v -> {
                downloadFileOnDevice(repoID, dirPath, fileName);
                mDropdown.dismiss();
            });
            copyCard.setOnClickListener(v -> {
                copyFile(repoID, repoName, dirPath, fileName);
                mDropdown.dismiss();
            });
            moveCard.setOnClickListener(v -> {
                moveFile(repoID, repoName, dirPath, fileName);
                mDropdown.dismiss();
            });

//            Drawable background = getResources().getDrawable(android.R.drawable.editbox_dropdown_dark_frame);
//            mDropdown.setBackgroundDrawable(background);
            mDropdown.showAsDropDown(mMoreBtn, 5, 5, Gravity.TOP | Gravity.RIGHT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doCopyMove() {
        if (!mActivity.copyMoveContext.checkCopyMoveToSubfolder()) {
            mActivity.showShortToast(mActivity, mActivity.copyMoveContext.isCopy()
                    ? R.string.cannot_copy_folder_to_subfolder
                    : R.string.cannot_move_folder_to_subfolder);
            return;
        }
        final CopyMoveDialog dialog = new CopyMoveDialog();
        dialog.init(mAccount, mActivity.copyMoveContext, false);
        dialog.setCancelable(false);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                mActivity.showShortToast(mActivity, mActivity.copyMoveContext.isCopy()
                        ? R.string.copied_successfully
                        : R.string.moved_successfully);

                if (mActivity.copyMoveContext.isMove()) {
                    removePageAndRefreshView();
                }
            }
        });
        dialog.show(mActivity.getSupportFragmentManager(), BrowserActivity.TAG_COPY_MOVE_DIALOG_FRAGMENT);
    }

    public void showShareDialog(String repoID, String path, String dialogType, long fileSize, String fileName) {
        SeafRepo repo = dataMgr.getCachedRepoByID(repoID);
        Intent intent = new Intent(mActivity, ShareDialogActivity.class);
//        intent.putExtra(CAMERA_UPLOAD_BOTH_PAGES, true);
        intent.putExtra(ShareDialogActivity.SHARE_DIALOG_FILE_NAME, fileName);
        intent.putExtra(ShareDialogActivity.SHARE_DIALOG_TYPE, dialogType);
        intent.putExtra(ShareDialogActivity.SHARE_DIALOG_ACCOUNT, mAccount);
        intent.putExtra(ShareDialogActivity.SHARE_DIALOG_REPO, SeafRepo.toString(repo));
        intent.putExtra(ShareDialogActivity.SHARE_DIALOG_PATH, path);
        startActivityForResult(intent, BrowserActivity.SHARE_DIALOG_REQUEST);
    }

    public void selectFileTagsDialog(SeafPhoto photo) {

        Dialog dialog = Utils.CustomDialog(mActivity);
        dialog.setContentView(R.layout.dialog_select_file_tag);

        CardView closeCard = dialog.findViewById(R.id.close_card);
        GridView repoTagsGrid = dialog.findViewById(R.id.repo_tags_grid);

        SeafRepo repo = dataMgr.getCachedRepoByID(repoID);
        fileTagAdapter = new SeafFileTagAdapter2(mActivity, mFragment, repo, photo, dirPath);
        repoTagsGrid.setAdapter(fileTagAdapter);
        fileTagAdapter.setRepoTags(repo.getRepoTags());
        fileTagAdapter.setFileTags(photo.getDirent().getFileTags());
        fileTagAdapter.notifyChanged();

        closeCard.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    public DataManager getDataMgr() {
        return dataMgr;
    }

    public void updatePhoto(SeafPhoto photo) {
        mPhotos.set(mPageIndex, photo);
    }

    public SeafTagColorAdapter3 getColorAdapter() {
        return colorAdapter;
    }

    @Override
    public void dismiss() {
        if (mActivity == null) {
            super.dismiss();
            return;
        }
        dataMgr.saveCachedStarredFiles(mStarredFiles);

        mActivity.getStarredFragment().refresh();
        if (needBrowserActivityRefresh)
            mActivity.getReposFragment().refresh();

        mActivity.getReposFragment().refreshOnResume();

        SeadroidApplication.getInstance().clearGalleryPhotos();

        super.dismiss();
    }

    public void showAddRepoTagDialog() {
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

        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
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
            ConcurrentAsyncTask.execute(new AddRepoTag(addTagName.getText().toString(), colorAdapter.getSelectedTagColor()));
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

    private class AddRepoTag extends AsyncTask<Void, Void, String> {
        private SeafException err;
        private String tagName;
        private String tagColor;

        public AddRepoTag(String name, String color) {
            tagName = name;
            tagColor = color;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                showAddRepoTagLoading(true);
                SeafRepo repo = dataMgr.getCachedRepoByID(repoID);
                return dataMgr.addRepoTag(repo, tagName, tagColor);
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
                    ((TextView)addRepoTagDialog.findViewById(R.id.error_text)).setText(getString(R.string.tag_exist));
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

                List<SeafRepo> repos = dataMgr.getReposFromCache();
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
                    File cache = dataMgr.getFileForReposCache();
                    Utils.writeFile(cache, dataMgr.reposToString(newRepos));
                } catch (IOException e) {
                    Log.e(DEBUG_TAG, "Could not write repo cache to disk.", e);
                }
                needBrowserActivityRefresh = true;
                addRepoTagDialog.dismiss();
                if (fileTagAdapter != null) {
                    fileTagAdapter.setRepoTags(repoTags);
                    fileTagAdapter.notifyChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}