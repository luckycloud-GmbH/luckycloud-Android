package com.seafile.seadroid2.cameraupload;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.Toast;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.ui.activity.BaseActivity;
import com.seafile.seadroid2.ui.activity.SeafilePathChooserActivity;
import com.seafile.seadroid2.ui.fragment.SettingsFragment;
import com.seafile.seadroid2.util.SystemSwitchUtils;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.List;

/**
 * Camera upload configuration helper
 */
public class CameraUploadConfigActivity extends BaseActivity {
    public  String DEBUG_TAG = "CameraUploadConfigActivity";

    private NonSwipeableViewPager mViewPager;
    private DotsIndicator magicIndicator;
    private BucketsFragment mBucketsFragment;
    private CloudLibraryFragment mCloudLibFragment;
    private WhatToUploadFragment whatToUploadFragment;
    private CardView backCard;
    private CardView continueCard;
    private CardView confirmCard;
    private SettingsManager sm;
    private SeafRepo mSeafRepo;
    private Account mAccount;
    /** handling data from configuration helper */
    private boolean isChooseBothPages;
    /** handling data from cloud library page */
    private boolean isChooseLibPage;
    /** handling data from local directory page */
    private boolean isChooseDirPage;
    private int mCurrentPosition;
    public boolean isContinueCardEnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        setContentView(R.layout.cuc_activity_layout);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        });

        isChooseBothPages = getIntent().getBooleanExtra(SettingsFragment.CAMERA_UPLOAD_BOTH_PAGES, false);
        isChooseLibPage = getIntent().getBooleanExtra(SettingsFragment.CAMERA_UPLOAD_REMOTE_LIBRARY, false);
        isChooseDirPage = getIntent().getBooleanExtra(SettingsFragment.CAMERA_UPLOAD_LOCAL_DIRECTORIES, false);

        mViewPager = findViewById(R.id.cuc_pager);

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new CameraUploadConfigAdapter(fm));
        mViewPager.setOffscreenPageLimit(6);
        mViewPager.addOnPageChangeListener(pageChangeListener);

        magicIndicator = (DotsIndicator) findViewById(R.id.cuc_indicator);
        magicIndicator.setViewPager(mViewPager);

        sm = SettingsManager.instance();

        if (isChooseLibPage || isChooseDirPage) {
            magicIndicator.setVisibility(View.GONE);
            findViewById(R.id.card_layout).setVisibility(View.GONE);
        }

        CardView closeCard = findViewById(R.id.close_card);
        closeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        backCard = findViewById(R.id.back_card);
        continueCard = findViewById(R.id.continue_card);
        confirmCard = findViewById(R.id.confirm_card);

        backCard.setVisibility(View.GONE);
        confirmCard.setVisibility(View.GONE);

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
                if (isContinueCardEnable) {
                    if (mCurrentPosition != 5) {
                        mCurrentPosition += 1;
                    }
                    mViewPager.setCurrentItem(mCurrentPosition);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.select_library), Toast.LENGTH_SHORT).show();
                }
            }
        });

        confirmCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                finish();
            }
        });
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
            if (mCurrentPosition == 4) {
                isContinueCardEnable = (
                        getCloudLibraryFragment().getAccountOrReposSelectionFragment().getReposAdapter().selectedRepo != null
                );
            } else {
                isContinueCardEnable = true;
            }
            switch (mCurrentPosition) {
                case 0:
                    backCard.setVisibility(View.GONE);
                    continueCard.setVisibility(View.VISIBLE);
                    confirmCard.setVisibility(View.GONE);
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                    backCard.setVisibility(View.VISIBLE);
                    continueCard.setVisibility(View.VISIBLE);
                    confirmCard.setVisibility(View.GONE);
                    break;
                case 5:
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

    public void saveCameraUploadInfo(Account account, SeafRepo seafRepo) {
        mSeafRepo = seafRepo;
        mAccount = account;
    }

    public void saveSettings() {
        SystemSwitchUtils.getInstance(this).syncSwitchUtils();
        if (isChooseBothPages || isChooseDirPage) {

            SettingsManager settingsManager = SettingsManager.instance();
            List<String> selectedBuckets = mBucketsFragment.getSelectedBuckets();
            if (mBucketsFragment.isAutoScanSelected()) {
                selectedBuckets.clear();
            }
            // this is the only setting that is safed here. all other are returned to caller
            // and safed there...
            settingsManager.setCameraUploadBucketList(selectedBuckets);
        }

        Intent intent = new Intent();
        // update cloud library data
        if (mSeafRepo != null && mAccount != null) {
            intent.putExtra(SeafilePathChooserActivity.DATA_REPO_NAME, mSeafRepo.name);
            intent.putExtra(SeafilePathChooserActivity.DATA_REPO_ID, mSeafRepo.id);
            intent.putExtra(SeafilePathChooserActivity.DATA_ACCOUNT, mAccount);
        }

        setResult(RESULT_OK, intent);

    }

    private void backPressed() {
        if (mCurrentPosition == 0) {
            setResult(RESULT_CANCELED);
            finish();
        } else {
            // navigate to previous page when press back button
            mCurrentPosition -= 1;
            mViewPager.setCurrentItem(mCurrentPosition);
        }
    }

    public boolean isChooseLibPage() {
        return isChooseLibPage;
    }

    public boolean isChooseDirPage() {
        return isChooseDirPage;
    }

    public void saveDataPlanAllowed(boolean isAllowed) {
        sm.saveDataPlanAllowed(isAllowed);
    }

    public void saveVideosAllowed(boolean isAllowed) {
        sm.saveVideosAllowed(isAllowed);
    }

    class CameraUploadConfigAdapter extends FragmentStatePagerAdapter {

        public CameraUploadConfigAdapter(FragmentManager fm) {
            super(fm);
        }

        // This method controls which fragment should be shown on a specific screen.
        @Override
        public Fragment getItem(int position) {

            if (isChooseLibPage) {
                return position == 0 ? new CloudLibraryFragment() : null;
            }

            if (isChooseDirPage) {
                switch (position) {
                    case 0:
                        mBucketsFragment = new BucketsFragment();
                        return mBucketsFragment;
                    default:
                        return null;
                }

            }

            // Assign the appropriate screen to the fragment object, based on which screen is displayed.
            switch (position) {
                case 0:
                    return new ConfigWelcomeFragment();
                case 1:
                    return new HowToUploadFragment();
                case 2:
                    whatToUploadFragment = new WhatToUploadFragment();
                    return whatToUploadFragment;
                case 3:
                    mBucketsFragment = new BucketsFragment();
                    return mBucketsFragment;
                case 4:
                    mCloudLibFragment = new CloudLibraryFragment();
                    return mCloudLibFragment;
                case 5:
                    return new ReadyToScanFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            if (isChooseLibPage || isChooseDirPage)
                return 1;
            else
                return 6;
        }

    }

    public CloudLibraryFragment getCloudLibraryFragment() {
        if (mCloudLibFragment == null) {
            mCloudLibFragment = new CloudLibraryFragment();
        }

        return mCloudLibFragment;
    }
}
