package com.seafile.seadroid2.folderbackup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import androidx.fragment.app.Fragment;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SettingsManager;

/**
 * How to upload fragment
 */
public class FolderBackupHowToUploadFragment extends Fragment {

    private RadioButton mDataPlanRadioBtn;
    private RadioGroup mRadioGroup;
    private ImageView wifiMobileImage;

    private FolderBackupConfigActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (FolderBackupConfigActivity) getActivity();
        View rootView = mActivity.getLayoutInflater().inflate(R.layout.folder_backup_how_to_upload_fragment, null);

        mRadioGroup = (RadioGroup) rootView.findViewById(R.id.folder_backup_wifi_radio_group);
        mDataPlanRadioBtn = (RadioButton) rootView.findViewById(R.id.folder_backup_wifi_or_data_plan_rb);
        wifiMobileImage = rootView.findViewById(R.id.wifi_mobile_image);

        if (SettingsManager.instance().isFolderBackupDataPlanAllowed()) {
            mDataPlanRadioBtn.setChecked(true);
            wifiMobileImage.setVisibility(View.VISIBLE);
        }

        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.folder_backup_wifi_only_rb:
                        // WiFi only
                        mActivity.saveDataPlanAllowed(false);
                        wifiMobileImage.setVisibility(View.GONE);
                        break;
                    case R.id.folder_backup_wifi_or_data_plan_rb:
                        // WiFi and data plan
                        mActivity.saveDataPlanAllowed(true);
                        wifiMobileImage.setVisibility(View.VISIBLE);
                        break;
                }

            }

        });

        return rootView;
    }

}

