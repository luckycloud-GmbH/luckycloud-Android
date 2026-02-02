package com.seafile.seadroid2.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.DataManager;

class LogoutTask extends TaskDialog.Task {

    LogoutTask() {}

    @Override
    protected void runTask() {}
}

public class LogoutDialog extends TaskDialog {

    public void init() {}

    @Override
    protected View createDialogContentView(LayoutInflater inflater, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_logout, null);
        return view;
    }

    @Override
    protected void onSaveDialogContentState(Bundle outState) {
        super.onSaveDialogContentState(outState);
    }

    @Override
    protected void onDialogCreated(Dialog dialog) {
        super.onDialogCreated(dialog);
        updateTitleText(getString(R.string.settings_account_sign_out_title));
    }

    @Override
    protected LogoutTask prepareTask() {
        return new LogoutTask();
    }
}
