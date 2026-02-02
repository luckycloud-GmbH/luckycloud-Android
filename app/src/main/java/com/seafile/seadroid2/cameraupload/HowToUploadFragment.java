package com.seafile.seadroid2.cameraupload;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SettingsManager;

/**
 * How to upload fragment
 */
public class HowToUploadFragment extends Fragment {

    private RadioButton mDataPlanRadioBtn;
    private RadioGroup mRadioGroup;
    private ImageView wifiMobileImage;

    private CameraUploadConfigActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (CameraUploadConfigActivity) getActivity();
        View rootView = mActivity.getLayoutInflater().inflate(R.layout.cuc_how_to_upload_fragment, null);

        mRadioGroup = (RadioGroup) rootView.findViewById(R.id.cuc_wifi_radio_group);
        mDataPlanRadioBtn = (RadioButton) rootView.findViewById(R.id.cuc_wifi_or_data_plan_rb);
        wifiMobileImage = rootView.findViewById(R.id.wifi_mobile_image);

        if (SettingsManager.instance().isDataPlanAllowed()) {
            mDataPlanRadioBtn.setChecked(true);
            wifiMobileImage.setVisibility(View.VISIBLE);
        }

        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.cuc_wifi_only_rb:
                        // WiFi only
                        mActivity.saveDataPlanAllowed(false);
                        wifiMobileImage.setVisibility(View.GONE);
                        break;
                    case R.id.cuc_wifi_or_data_plan_rb:
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

