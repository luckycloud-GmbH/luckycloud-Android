package com.seafile.seadroid2.document.markor.activity;

import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

import com.seafile.seadroid2.document.markor.model.AppSettings;
import com.seafile.seadroid2.document.markor.util.MarkorContextUtils;
import com.seafile.seadroid2.document.opoc.frontend.base.GsActivityBase;
import com.seafile.seadroid2.document.opoc.frontend.base.GsFragmentBase;

public abstract class MarkorBaseActivity extends GsActivityBase<AppSettings, MarkorContextUtils> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _cu.setAppLanguage(this, "");
    }

    protected boolean onReceiveKeyPress(GsFragmentBase fragment, int keyCode, KeyEvent event) {
        return fragment.onReceiveKeyPress(keyCode, event);
    }

    @Override
    public Integer getNewNavigationBarColor() {
        return null;
    }

    @Override
    public Integer getNewActivityBackgroundColor() {
        return null;
    }

    @Override
    protected AppSettings createAppSettingsInstance() {
        return new AppSettings(this);
    }

    @Override
    protected MarkorContextUtils createContextUtilsInstance() {
        return new MarkorContextUtils(this);
    }
}
