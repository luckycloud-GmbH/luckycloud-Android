package com.seafile.seadroid2.document.markor.activity;

import android.content.Context;

import com.seafile.seadroid2.document.markor.model.AppSettings;
import com.seafile.seadroid2.document.markor.util.MarkorContextUtils;
import com.seafile.seadroid2.document.opoc.frontend.base.GsFragmentBase;

public abstract class MarkorBaseFragment extends GsFragmentBase<AppSettings, MarkorContextUtils> {
    @Override
    public AppSettings createAppSettingsInstance(Context context) {
        return AppSettings.get(context);
    }

    @Override
    public MarkorContextUtils createContextUtilsInstance(Context context) {
        return new MarkorContextUtils(context);
    }
}
