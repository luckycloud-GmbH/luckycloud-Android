package com.seafile.seadroid2.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.folderbackup.selectfolder.BeanListManager;
import com.seafile.seadroid2.folderbackup.selectfolder.FileBean;
import com.seafile.seadroid2.folderbackup.selectfolder.FileListAdapter;
import com.seafile.seadroid2.folderbackup.selectfolder.FileTools;
import com.seafile.seadroid2.folderbackup.selectfolder.OnFileItemClickListener;
import com.seafile.seadroid2.folderbackup.selectfolder.SelectOptions;
import com.seafile.seadroid2.folderbackup.selectfolder.TabBarFileBean;
import com.seafile.seadroid2.folderbackup.selectfolder.TabBarFileListAdapter;
import com.seafile.seadroid2.ui.activity.BaseActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Camera upload configuration helper
 */
public class DownloadLocationDialog extends BaseActivity {
    public static final String NEW_DOWNLOAD_DATA_LOCATION = "new_download_data_location";

    private CardView closeCard, defaultCard, okCard;
    private RecyclerView mTabBarFileRecyclerView, mFileRecyclerView;
    private SelectOptions mSelectOptions;
    private List<String> allPathsList;
    private List<String> mShowFileTypes;
    private int mSortType;
    private List<FileBean> mFileList;
    private List<TabBarFileBean> mTabbarFileList;
    private String mCurrentPath;
    private FileListAdapter mFileListAdapter;
    private TabBarFileListAdapter mTabBarFileListAdapter;
    private List<String> selectPaths;
    private String initialPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        setContentView(R.layout.dialog_download_location);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        });

        closeCard = findViewById(R.id.close_card);
        defaultCard = (CardView) findViewById(R.id.default_card);
        okCard = (CardView) findViewById(R.id.ok_card);

        mFileRecyclerView = (RecyclerView) findViewById(R.id.rcv_files_list);
        mFileRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mFileListAdapter = new FileListAdapter(this, mFileList);
        mFileRecyclerView.setAdapter(mFileListAdapter);

        mTabBarFileRecyclerView = (RecyclerView) findViewById(R.id.rcv_tabbar_files_list);
        mTabBarFileRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mTabBarFileListAdapter = new TabBarFileListAdapter(this, mTabbarFileList);
        mTabBarFileRecyclerView.setAdapter(mTabBarFileListAdapter);

        init();
        initData();
    }

    private void init() {
        selectPaths= Lists.newArrayList(SettingsManager.instance().getDownloadDataLocation());

        closeCard.setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        });
        defaultCard.setOnClickListener(v -> {
            selectPaths.clear();
            okCard.callOnClick();
        });
        okCard.setOnClickListener(v -> {
            String selectPath = "";
            if (selectPaths.size() == 0) {
                selectPath = SettingsManager.DOWNLOAD_DATA_LOCATION_DEFAULT;
            } else {
                selectPath = selectPaths.get(0);
            }
            Intent intent = new Intent();
            intent.putExtra(NEW_DOWNLOAD_DATA_LOCATION, selectPath);
            setResult(RESULT_OK, intent);
            finish();
        });

        mFileListAdapter.setOnItemClickListener(new OnFileItemClickListener() {
            @Override
            public void onItemClick(int position) {
                FileBean item = mFileList.get(position);
                if (item.isFile()) {
                    Toast.makeText(DownloadLocationDialog.this, getString(R.string.selection_file_type), Toast.LENGTH_SHORT).show();
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
                            for (int i = 0; i < selectPaths.size(); i++) {
                                if (item.getFilePath().equals(selectPaths.get(i))) {
                                    selectPaths.remove(i);
                                    i--;
                                }
                            }
                            fb.setChecked(false);

                        } else {
                            if (selectPaths.size() > 0) {
                                for (int i = 0; i < mFileList.size(); i++) {
                                    if (mFileList.get(i).getFilePath().equals(selectPaths.get(0))) {
                                        mFileList.get(i).setChecked(false);
                                    }
                                }
                            }
                            selectPaths.clear();
                            selectPaths.add(item.getFilePath());
                            fb.setChecked(true);
                        }
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

    private void initData() {
        mSelectOptions = SelectOptions.getResetInstance(this);
        allPathsList = initRootPath(this);
        mShowFileTypes = Arrays.asList(mSelectOptions.getShowFileTypes());
        mSortType = mSelectOptions.getSortType();
        mFileList = new ArrayList<>();
        mTabbarFileList = new ArrayList<>();
        refreshFileAndTabBar(BeanListManager.TYPE_INIT_TAB_BAR);
    }

    private List<String> initRootPath(Activity activity) {
        List<String> allPaths = FileTools.getAllPaths(activity);
//        mCurrentPath = mSelectOptions.rootPath;
//        if (mCurrentPath == null) {
//            if (allPaths.isEmpty()) {
//                mCurrentPath = Constants.DEFAULT_ROOTPATH;
//            } else {
//                mCurrentPath = allPaths.get(0);
//            }
//        }
//        initialPath = mCurrentPath;
        mCurrentPath = "";
        return allPaths;
    }

    private void refreshFileAndTabBar(int tabbarType) {
        BeanListManager.upDataFileBeanListByAsyn(this, selectPaths, mFileList, mFileListAdapter,
                mCurrentPath, mShowFileTypes, mSortType);
        BeanListManager.upDataTabbarFileBeanList(mTabbarFileList, mTabBarFileListAdapter,
                mCurrentPath, tabbarType, allPathsList, null);
    }

    private void backPressed() {
        if (mCurrentPath.equals(initialPath) || allPathsList.contains(mCurrentPath)) {
            finish();
        } else {
            mCurrentPath = FileTools.getParentPath(mCurrentPath);
            refreshFileAndTabBar(BeanListManager.TYPE_DEL_TAB_BAR);
        }
    }
}
