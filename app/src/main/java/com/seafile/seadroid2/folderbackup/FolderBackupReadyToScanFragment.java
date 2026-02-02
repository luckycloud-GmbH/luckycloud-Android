package com.seafile.seadroid2.folderbackup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.seafile.seadroid2.R;

public class FolderBackupReadyToScanFragment extends Fragment {

    private CardView continueBtn;
    private FolderBackupConfigActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mActivity = (FolderBackupConfigActivity) getActivity();
        View rootView = mActivity.getLayoutInflater().inflate(R.layout.cuc_ready_to_scan_fragment, null);

        continueBtn = rootView.findViewById(R.id.cuc_click_to_finish_btn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.saveSettings();
                mActivity.finish();
            }
        });

        TextView cucReadyInfoTv = rootView.findViewById(R.id.cuc_ready_info_tv);
        cucReadyInfoTv.setText(R.string.folder_backup_ready_to_scan_detail);

        return rootView;
    }

}

