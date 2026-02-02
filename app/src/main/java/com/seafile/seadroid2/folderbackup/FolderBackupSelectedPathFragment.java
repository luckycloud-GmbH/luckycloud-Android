package com.seafile.seadroid2.folderbackup;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.folderbackup.selectfolder.BeanListManager;
import com.seafile.seadroid2.folderbackup.selectfolder.Constants;
import com.seafile.seadroid2.folderbackup.selectfolder.FileBean;
import com.seafile.seadroid2.folderbackup.selectfolder.FileListAdapter;
import com.seafile.seadroid2.folderbackup.selectfolder.FileTools;
import com.seafile.seadroid2.folderbackup.selectfolder.OnFileItemClickListener;
import com.seafile.seadroid2.folderbackup.selectfolder.SelectOptions;
import com.seafile.seadroid2.folderbackup.selectfolder.TabBarFileBean;
import com.seafile.seadroid2.folderbackup.selectfolder.TabBarFileListAdapter;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.dialog.NewDirDialog;
import com.seafile.seadroid2.ui.dialog.TaskDialog;
import com.seafile.seadroid2.ui.fragment.ReposFragment;
import com.seafile.seadroid2.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FolderBackupSelectedPathFragment extends Fragment {
    private FolderBackupConfigActivity mActivity;

    private View mSelectedPathLayout;
    private View mTitleTextView;
    private CardView mAddBackupFolderCard;
    private View mItemTextMoreView;
    private RecyclerView mRecyclerView;
    private FolderBackSelectedPathRecyclerViewAdapter mAdapter;
    private View popupBackupDeletePathView;
    private View popupBackupPathMoreView;
    private PopupWindow mDropdown = null;

    private View mSelectPathLayout;
    private CardView mCancelCard, mOkCard;
    private RecyclerView mTabBarFileRecyclerView, mFileRecyclerView;
    private CardView mTabBarMoreCard;
    private SelectOptions mSelectOptions;
    private List<String> allPathsList;
    private List<String> mShowFileTypes;
    private int mSortType;
    private List<FileBean> mFileList;
    private List<TabBarFileBean> mTabbarFileList;
    private String mCurrentPath;
    private FileListAdapter mFileListAdapter;
    private TabBarFileListAdapter mTabBarFileListAdapter;
    public List<String> selectPaths;
    private List<String> newSelectPaths;
    private String initialPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (FolderBackupConfigActivity) getActivity();
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.folder_backup_selected_path_fragment, null);

        mSelectedPathLayout = rootView.findViewById(R.id.selected_path_layout);
        popupBackupDeletePathView = rootView.findViewById(R.id.popup_backup_delete_path_layout);
        popupBackupPathMoreView = rootView.findViewById(R.id.popup_backup_path_more_layout);
        mRecyclerView = rootView.findViewById(R.id.lv_search);
        mAdapter = new FolderBackSelectedPathRecyclerViewAdapter(mActivity, this);

        mAddBackupFolderCard = rootView.findViewById(R.id.add_backup_folder_card);
        mAddBackupFolderCard.setOnClickListener(v -> {
//            Intent intent = new Intent(FolderBackupSelectedPathFragment.this, FolderBackupConfigActivity.class);
//            intent.putExtra(FOLDER_BACKUP_REMOTE_PATH, true);
//            startActivity(intent);

//            initSelectPathData();

            mSelectPathLayout.setVisibility(View.VISIBLE);
            mSelectedPathLayout.setVisibility(View.GONE);
            mActivity.showCardLayout(false);
        });
        mTitleTextView = rootView.findViewById(R.id.title_tv);
        mItemTextMoreView = rootView.findViewById(R.id.item_text_more_layout);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false));

        mSelectPathLayout = rootView.findViewById(R.id.select_path_layout);
        mCancelCard = rootView.findViewById(R.id.cancel_card);
        mOkCard = rootView.findViewById(R.id.ok_card);
        mFileRecyclerView = (RecyclerView) rootView.findViewById(R.id.rcv_files_list);
        mFileRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mFileListAdapter = new FileListAdapter(getActivity(), mFileList);
        mFileRecyclerView.setAdapter(mFileListAdapter);

        mTabBarFileRecyclerView = (RecyclerView) rootView.findViewById(R.id.rcv_tabbar_files_list);
        mTabBarMoreCard = (CardView) rootView.findViewById(R.id.tabbar_more_card);
        mTabBarFileRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mTabBarFileListAdapter = new TabBarFileListAdapter(getActivity(), mTabbarFileList);
        mTabBarFileRecyclerView.setAdapter(mTabBarFileListAdapter);

        rootView.post(() -> {
            init();
            initSelectPathData();
        });

        return rootView;
    }

    private void init() {

        selectPaths = mActivity.getSelectFolderPath();
        if (selectPaths == null)
            selectPaths = new ArrayList<>();
        newSelectPaths = new ArrayList<>();
        newSelectPaths.addAll(selectPaths);

        mAdapter.notifyDataChanged(selectPaths);
        setListViewHeight();

        mTabBarMoreCard.setOnClickListener(v -> {
            showMorePopup();
        });

        mCancelCard.setOnClickListener(v -> {
            mSelectPathLayout.setVisibility(View.GONE);
            mSelectedPathLayout.setVisibility(View.VISIBLE);
            mActivity.showCardLayout(true);
        });

        mOkCard.setOnClickListener(v -> {
            selectPaths.clear();
            selectPaths.addAll(newSelectPaths);
            mAdapter.notifyDataChanged(selectPaths);
            setListViewHeight();
            mActivity.setFolderPathList(selectPaths);
            mCancelCard.callOnClick();
        });

        mFileListAdapter.setOnItemClickListener(new OnFileItemClickListener() {
            @Override
            public void onItemClick(int position) {
                FileBean item = mFileList.get(position);
                if (item.isFile()) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.selection_file_type), Toast.LENGTH_SHORT).show();
                } else {
                    mCurrentPath = item.getFilePath();
                    refreshFileAndTabBar(BeanListManager.TYPE_ADD_TAB_BAR);
                }
            }

            @Override
            public void onCheckBoxClick(View view, int position) {
                FileBean item = mFileList.get(position);
                for (FileBean fb : mFileList) {
                    if (item.equals(fb)) {
                        if (fb.isChecked()) {
                            for (int i = 0; i < newSelectPaths.size(); i++) {
                                if (item.getFilePath().equals(newSelectPaths.get(i))) {
                                    newSelectPaths.remove(i);
                                    i--;
                                }
                            }
                            fb.setChecked(false);

                        } else {
                            newSelectPaths.add(item.getFilePath());
                            fb.setChecked(true);
                        }
                        mActivity.setFolderPathList(newSelectPaths);
                    }
                }
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        mFileListAdapter.updateListData(mFileList, null);
                        mFileListAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        mTabBarFileListAdapter.setOnItemClickListener(new OnFileItemClickListener() {
            @Override
            public void onItemClick(int position) {
                TabBarFileBean item = mTabbarFileList.get(position);
                mCurrentPath = item.getFilePath();

                if (mTabbarFileList.size() > 1) {
                    refreshFileAndTabBar(BeanListManager.TYPE_DEL_TAB_BAR);
                }
            }

            @Override
            public void onCheckBoxClick(View view, int position) {

            }
        });
    }

    private void initSelectPathData() {
        mSelectOptions = SelectOptions.getResetInstance(getActivity());
        allPathsList = initRootPath(getActivity());
        mShowFileTypes = Arrays.asList(mSelectOptions.getShowFileTypes());
        mSortType = mSelectOptions.getSortType();
        mFileList = new ArrayList<>();
        mTabbarFileList = new ArrayList<>();
        refreshFileAndTabBar(BeanListManager.TYPE_INIT_TAB_BAR);
    }

    private List<String> initRootPath(Activity activity) {
        List<String> allPaths = FileTools.getAllPaths(activity);
        mCurrentPath = "";
//        mCurrentPath = mSelectOptions.rootPath;
//        if (mCurrentPath == null) {
//            if (allPaths.isEmpty()) {
//                mCurrentPath = Constants.DEFAULT_ROOTPATH;
//            } else {
//                mCurrentPath = allPaths.get(0);
//            }
//        }
//        initialPath = mCurrentPath;
        return allPaths;
    }

    private void refreshFileAndTabBar(int tabbarType) {
        newSelectPaths.clear();
        newSelectPaths.addAll(selectPaths);
        mSortType = mSelectOptions.getSortType();
        BeanListManager.upDataFileBeanListByAsyn(getActivity(), newSelectPaths, mFileList, mFileListAdapter,
                mCurrentPath, mShowFileTypes, mSortType);
        BeanListManager.upDataTabbarFileBeanList(mTabbarFileList, mTabBarFileListAdapter,
                mCurrentPath, tabbarType, allPathsList, null);
    }

    public void showEditItemPopup(View anchor, int position) {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_backup_delete_path, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mDropdown = new PopupWindow(layout, popupBackupDeletePathView.getWidth(),
                    popupBackupDeletePathView.getHeight(), true);//FrameLayout.LayoutParams.WRAP_CONTENT

            final CardView deleteCard = layout.findViewById(R.id.delete_card);

            deleteCard.setOnClickListener(view -> {
                mAdapter.deleteBackupPath(position);
                setListViewHeight();
                mDropdown.dismiss();
            });

//            Drawable background = getResources().getDrawable(android.R.drawable.editbox_dropdown_dark_frame);
//            mDropdown.setBackgroundDrawable(background);
            mDropdown.showAsDropDown(anchor, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setListViewHeight() {
        int maxHeight = mSelectedPathLayout.getMeasuredHeight()
                - mTitleTextView.getMeasuredHeight() - (int) mActivity.getResources().getDimension(R.dimen.margin_big) * 2
                - mAddBackupFolderCard.getMeasuredHeight() - (int) mActivity.getResources().getDimension(R.dimen.tv_title_margin_top) * 2;

        int totalHeight = 0;
        for (int i = 0; i < mAdapter.getItemCount(); i++)
            totalHeight += mItemTextMoreView.getMeasuredHeight();

        ViewGroup.LayoutParams params = mRecyclerView.getLayoutParams();
        params.height = Math.min(maxHeight, totalHeight);
        mRecyclerView.setLayoutParams(params);
        mRecyclerView.requestLayout();
    }

    private void showMorePopup() {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_backup_path_more_layout, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mDropdown = new PopupWindow(layout, popupBackupPathMoreView.getWidth(),
                    popupBackupPathMoreView.getHeight(),true);//FrameLayout.LayoutParams.WRAP_CONTENT

            final CardView addCard = layout.findViewById(R.id.add_card);
            final TextView sortContentText = layout.findViewById(R.id.sort_content_text);
            final ImageView arrowImage = layout.findViewById(R.id.arrow_image);
            final CardView nameUpCard = layout.findViewById(R.id.name_up_card);
            final CardView nameDownCard = layout.findViewById(R.id.name_down_card);
            final CardView lastModifiedUpCard = layout.findViewById(R.id.last_modified_up_card);
            final CardView lastModifiedDownCard = layout.findViewById(R.id.last_modified_down_card);

            int backupSortType = SettingsManager.instance().getBackupSortTypePref();
            arrowImage.setImageDrawable(getResources().getDrawable((backupSortType % 2 == 0) ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down));
            sortContentText.setText(getResources().getString( (backupSortType < 2) ? R.string.name_hint : R.string.last_modified));

            addCard.setOnClickListener(v -> {
                mDropdown.dismiss();
                showNewDirDialog();
            });

            nameUpCard.setOnClickListener(v -> {
                sortFiles(Constants.SORT_NAME_ASC);
                mDropdown.dismiss();
            });

            nameDownCard.setOnClickListener(v -> {
                sortFiles(Constants.SORT_NAME_DESC);
                mDropdown.dismiss();
            });

            lastModifiedUpCard.setOnClickListener(v -> {
                sortFiles(Constants.SORT_TIME_ASC);
                mDropdown.dismiss();
            });

            lastModifiedDownCard.setOnClickListener(v -> {
                sortFiles(Constants.SORT_TIME_DESC);
                mDropdown.dismiss();
            });

            mDropdown.showAsDropDown(mTabBarMoreCard, 5, 5, Gravity.TOP | Gravity.RIGHT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sortFiles(int sortType) {
        SettingsManager.instance().saveBackupSortTypePref(sortType);
        refreshFileAndTabBar(BeanListManager.TYPE_INIT_TAB_BAR);
    }

    private void showNewDirDialog() {
        final NewDirDialog dialog = new NewDirDialog();
        dialog.init(null, null, null, mCurrentPath);
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                final String message = String.format(getString(R.string.create_new_folder_success), dialog.getNewDirName());
                mActivity.showShortToast(mActivity, message);
                refreshFileAndTabBar(BeanListManager.TYPE_INIT_TAB_BAR);
            }
        });
        dialog.show(mActivity.getSupportFragmentManager(), "NewDirDialogFragment");
    }
}
