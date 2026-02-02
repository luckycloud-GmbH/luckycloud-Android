package com.seafile.seadroid2.folderbackup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.seafile.seadroid2.R;

/**
 * Welcome fragment for camera upload configuration helper
 */
public class FolderBackupWelcomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = getActivity().getLayoutInflater().inflate(R.layout.folder_backup_welcome_fragment, null);

        return rootView;
    }

}

