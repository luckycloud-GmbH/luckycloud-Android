package com.seafile.seadroid2.folderbackup;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.google.gson.Gson;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.cameraupload.NonSwipeableViewPager;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.folderbackup.selectfolder.StringTools;
import com.seafile.seadroid2.ui.activity.BaseActivity;
import com.seafile.seadroid2.ui.fragment.SettingsFragment;
import com.seafile.seadroid2.util.Utils;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Camera upload configuration helper
 */
public class FolderBackupConfigActivity extends BaseActivity {

    public String DEBUG_TAG = FolderBackupConfigActivity.class.getSimpleName();
    public static final String BACKUP_SELECT_REPO = "backup_select_repo";
    public static final String BACKUP_SELECT_PATHS = "backup_select_paths";
    public static final String BACKUP_SELECT_PATHS_SWITCH = "backup_select_paths_switch";
    public static final String BACKUP_RESTART = "backup_restart";

    private NonSwipeableViewPager mViewPager;
    private DotsIndicator magicIndicator;
    private CloudLibraryChooserFragment mCloudLibFragment;
    private FolderBackupSelectedPathFragment mFolderBackupSelectedPathFragment;
    private View cardLayout;
    private CardView backCard;
    private CardView continueCard;
    private CardView confirmCard;
    private CardView closeCard;

    private SettingsManager sm;
    private SeafRepo mSeafRepo;
    private Account mAccount;
    private boolean isChooseAllPages;
    private boolean isChooseModePage;
    private boolean isChooseLibPage;
    private boolean isChooseFolderPage;

    private FolderBackupDBHelper databaseHelper;
    private FolderBackupService mBackupService;
    private List<String> selectFolderPaths;
    private Activity mActivity;
    private String originalBackupPaths;

    private int mCurrentPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        setContentView(R.layout.folder_backup_config_activity_layout);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        });

        isChooseAllPages = getIntent().getBooleanExtra(SettingsFragment.FOLDER_BACKUP_REMOTE_PAGES, false);
        isChooseModePage = getIntent().getBooleanExtra(SettingsFragment.FOLDER_BACKUP_REMOTE_MODE, false);
        isChooseFolderPage = getIntent().getBooleanExtra(SettingsFragment.FOLDER_BACKUP_REMOTE_PATH, false);
        isChooseLibPage = getIntent().getBooleanExtra(SettingsFragment.FOLDER_BACKUP_REMOTE_LIBRARY, false);

        cardLayout = findViewById(R.id.card_layout);
        backCard = findViewById(R.id.back_card);
        continueCard = findViewById(R.id.continue_card);
        confirmCard = findViewById(R.id.confirm_card);

        backCard.setVisibility(View.GONE);
        confirmCard.setVisibility(View.GONE);

        mViewPager = findViewById(R.id.cuc_pager);

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FolderBackupConfigAdapter(fm));
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.addOnPageChangeListener(pageChangeListener);

        magicIndicator = (DotsIndicator) findViewById(R.id.cuc_indicator);
        magicIndicator.setViewPager(mViewPager);

        if (!isChooseAllPages) {
            magicIndicator.setVisibility(View.GONE);
        }

        closeCard = findViewById(R.id.close_card);
        closeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(SettingsFragment.FOLDER_BACKUP_REMOTE_PAGES, isChooseAllPages);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        backCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentPosition != 0) {
                    mCurrentPosition -= 1;
                }
                mViewPager.setCurrentItem(mCurrentPosition);
            }
        });

        continueCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isContinueCardEnable()) {
                    if (mCurrentPosition != 4) {
                        mCurrentPosition += 1;
                    }
                    mViewPager.setCurrentItem(mCurrentPosition);
                } else
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.select_library), Toast.LENGTH_SHORT).show();
            }
        });

        confirmCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                finish();
            }
        });

        sm = SettingsManager.instance();

        databaseHelper = FolderBackupDBHelper.getDatabaseHelper();

        //bind service
        Intent bindIntent = new Intent(this, FolderBackupService.class);
        bindService(bindIntent, mFolderBackupConnection, Context.BIND_AUTO_CREATE);

        mActivity = this;

        originalBackupPaths = SettingsManager.instance().getBackupPaths();

        if (isChooseFolderPage && !TextUtils.isEmpty(originalBackupPaths)) {
            selectFolderPaths = StringTools.getJsonToList(originalBackupPaths);
        }
    }

    /**
     * Page scroll listener.
     */
    private final OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageScrollStateChanged(int scrollState) {}

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            mCurrentPosition = position;
            showCardLayout(position != 3);
            switch (mCurrentPosition) {
                case 0:
                    backCard.setVisibility(View.GONE);
                    continueCard.setVisibility(isChooseAllPages? View.VISIBLE : View.GONE);
                    confirmCard.setVisibility(isChooseAllPages? View.GONE : View.VISIBLE);
                    break;
                case 1:
                case 2:
                case 3:
                    backCard.setVisibility(View.VISIBLE);
                    continueCard.setVisibility(View.VISIBLE);
                    confirmCard.setVisibility(View.GONE);
                    break;
                case 4:
                    backCard.setVisibility(View.GONE);
                    continueCard.setVisibility(View.GONE);
                    confirmCard.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPageSelected(int page){}
    };

    public void saveBackupLibrary(Account account, SeafRepo seafRepo) {
        mSeafRepo = seafRepo;
        mAccount = account;
    }

    public FolderBackupDBHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public void setFolderPathList(List<String> selectFileList) {
        this.selectFolderPaths = selectFileList;
    }

    public List<String> getSelectFolderPath() {
        return selectFolderPaths;
    }

    public void saveSettings() {

        if (isChooseModePage)
            return;

        Intent intent = new Intent();

        if (isChooseLibPage || isChooseAllPages) {
            //FIX an issue: When no folder or library is selected, a crash occurs
            if (null == mSeafRepo || null == mAccount) {
                Utils.utilsLogInfo(false, "----------No repo is selected");
                return;
            }

            // update cloud library data
            if (mSeafRepo != null && mAccount != null) {
//                intent.putExtra(SeafilePathChooserActivity.DATA_REPO_NAME, mSeafRepo.name);
//                intent.putExtra(SeafilePathChooserActivity.DATA_REPO_ID, mSeafRepo.id);
//                intent.putExtra(SeafilePathChooserActivity.DATA_ACCOUNT, mAccount);
                intent.putExtra(BACKUP_SELECT_REPO, true);
                SettingsManager.instance().saveBackupEmail(mAccount.getEmail());
                try {
                    RepoConfig repoConfig = databaseHelper.getRepoConfig(mAccount.getEmail());
                    if (repoConfig != null) {
                        databaseHelper.updateRepoConfig(mAccount.getEmail(), mSeafRepo.getID(), mSeafRepo.getName());
                    } else {
                        databaseHelper.saveRepoConfig(mAccount.getEmail(), mSeafRepo.getID(), mSeafRepo.getName());
                    }
                    Toast.makeText(mActivity, mActivity.getString(R.string.folder_backup_select_repo_update), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Utils.utilsLogInfo(true, "saveRepoConfig\nError: " + e);
                }
            }

            boolean automaticBackup = SettingsManager.instance().isFolderAutomaticBackup();
            if (automaticBackup && mBackupService != null) {
                mBackupService.backupFolder(mAccount.getEmail());
            }
        }

        if (isChooseFolderPage || isChooseAllPages) {
            //FIX an issue: When no folder or library is selected, a crash occurs
            if (selectFolderPaths == null || selectFolderPaths.isEmpty()) {
                Utils.utilsLogInfo(false, "----------No folder is selected");

                //clear local storage
                SettingsManager.instance().saveBackupPaths("");

                intent.putExtra(BACKUP_SELECT_PATHS_SWITCH, true);
            } else {
                String backupEmail = SettingsManager.instance().getBackupEmail();
                String strJsonPath = new Gson().toJson(selectFolderPaths);

                if ((TextUtils.isEmpty(originalBackupPaths) && !TextUtils.isEmpty(strJsonPath)) || !originalBackupPaths.equals(strJsonPath)) {
                    mBackupService.startFolderMonitor(selectFolderPaths);
                    intent.putExtra(BACKUP_RESTART, true);
                    Utils.utilsLogInfo(false, "----------Restart monitoring FolderMonitor");
                }

                if (!TextUtils.isEmpty(originalBackupPaths) && TextUtils.isEmpty(strJsonPath)) {
                    mBackupService.stopFolderMonitor();
                    intent.putExtra(BACKUP_RESTART, false);
                }

                SettingsManager.instance().saveBackupPaths(strJsonPath);
                if (selectFolderPaths != null) {
                    intent.putStringArrayListExtra(BACKUP_SELECT_PATHS, (ArrayList<String>) selectFolderPaths);
                    intent.putExtra(BACKUP_SELECT_PATHS_SWITCH, true);
                }

                boolean folderAutomaticBackup = SettingsManager.instance().isFolderAutomaticBackup();
                if (folderAutomaticBackup && mBackupService != null) {
                    mBackupService.backupFolder(backupEmail);
                }
            }
        }

        setResult(RESULT_OK, intent);
    }

    private void backPressed() {
        if (mCurrentPosition == 0) {
            closeCard.callOnClick();
        } else {
            // navigate to previous page when press back button
            mCurrentPosition -= 1;
            mViewPager.setCurrentItem(mCurrentPosition);
        }
    }

    public void saveDataPlanAllowed(boolean isAllowed) {
        sm.saveFolderBackupDataPlanAllowed(isAllowed);
    }

    @Override
    protected void onDestroy() {
        if (mBackupService != null) {
            unbindService(mFolderBackupConnection);
            mBackupService = null;
        }
        super.onDestroy();
    }

    private final ServiceConnection mFolderBackupConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            FolderBackupService.FileBackupBinder fileBackupBinder = (FolderBackupService.FileBackupBinder) binder;
            mBackupService = fileBackupBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBackupService = null;
        }

    };

    class FolderBackupConfigAdapter extends FragmentStatePagerAdapter {

        public FolderBackupConfigAdapter(FragmentManager fm) {
            super(fm);
        }

        // This method controls which fragment should be shown on a specific screen.
        @Override
        public Fragment getItem(int position) {

            if (isChooseModePage) {
                return position == 0 ? new FolderBackupHowToUploadFragment() : null;
            }

            if (isChooseLibPage) {
                mCloudLibFragment = new CloudLibraryChooserFragment();
                return position == 0 ? mCloudLibFragment : null;
            }

            if (isChooseFolderPage) {
                mFolderBackupSelectedPathFragment = new FolderBackupSelectedPathFragment();
                return position == 0 ? mFolderBackupSelectedPathFragment : null;
            }

            // Assign the appropriate screen to the fragment object, based on which screen is displayed.
            switch (position) {
                case 0:
                    return new FolderBackupWelcomeFragment();
                case 1:
                    return new FolderBackupHowToUploadFragment();
                case 2:
                    mCloudLibFragment = new CloudLibraryChooserFragment();
                    return mCloudLibFragment;
                case 3:
                    mFolderBackupSelectedPathFragment = new FolderBackupSelectedPathFragment();
                    return mFolderBackupSelectedPathFragment;
                case 4:
                    return new FolderBackupReadyToScanFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            if (isChooseModePage || isChooseLibPage || isChooseFolderPage)
                return 1;
            else
                return 5;
        }

    }

    public CloudLibraryChooserFragment getCloudLibFragment() {
        if (mCloudLibFragment == null) {
            mCloudLibFragment = new CloudLibraryChooserFragment();
        }

        return mCloudLibFragment;
    }

    private boolean isContinueCardEnable() {
        return mCurrentPosition != 2 || (mSeafRepo != null && mAccount != null);
    }

    public void showCardLayout(boolean flag) {
        cardLayout.setVisibility(flag ? View.VISIBLE : View.GONE);
    }
}
