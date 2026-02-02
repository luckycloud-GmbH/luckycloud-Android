/*#######################################################
 *
 *   Maintained 2017-2025 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package com.seafile.seadroid2.document.markor.model;

import android.content.Context;

import androidx.annotation.ColorInt;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.document.markor.activity.MarkorBaseActivity;
import com.seafile.seadroid2.document.markor.util.MarkorContextUtils;
import com.seafile.seadroid2.document.opoc.model.GsSharedPreferencesPropertyBackend;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"SameParameterValue", "WeakerAccess", "FieldCanBeLocal"})
public class AppSettings extends GsSharedPreferencesPropertyBackend {
    public static Boolean _isDeviceGoodHardware = null;
    private final MarkorContextUtils _cu;

    public static AppSettings get(final Context context) {
        if (context instanceof MarkorBaseActivity) {
            return ((MarkorBaseActivity) context).getAppSettings();
        } else if (context != null) {
            return new AppSettings(context);
        } else {
            return new AppSettings(SeadroidApplication.getAppContext());
        }
    }

    public AppSettings(final Context context) {
        super(context, SHARED_PREF_APP);
        _cu = new MarkorContextUtils(context);
        _isDeviceGoodHardware = _cu.isDeviceGoodHardware(context);
    }

    public String getFontFamily() {
        return "sans-serif-regular";
    }

    private static final String PREF_PREFIX_WRAP_STATE = "PREF_PREFIX_WRAP_STATE";
    private static final String PREF_PREFIX_HIGHLIGHT_STATE = "PREF_PREFIX_HIGHLIGHT_STATE";
    private static final String PREF_PREFIX_LINE_NUM_STATE = "PREF_PREFIX_LINE_NUM_STATE";

    public void setDocumentWrapState(final String path, final boolean state) {
        if (fexists(path)) {
            setBool(PREF_PREFIX_WRAP_STATE + path, state);
        }
    }

    public boolean getDocumentWrapState(final String path) {
        final boolean _default = true;
        if (!fexists(path)) {
            return _default;
        } else {
            return getBool(PREF_PREFIX_WRAP_STATE + path, _default);
        }
    }

    public void setDocumentLineNumbersEnabled(final String path, final boolean enabled) {
        if (fexists(path)) {
            setBool(PREF_PREFIX_LINE_NUM_STATE + path, enabled);
        }
    }

    public boolean getDocumentLineNumbersEnabled(final String path) {
        final boolean _default = false;
        if (!fexists(path)) {
            return _default;
        } else {
            return getBool(PREF_PREFIX_LINE_NUM_STATE + path, _default);
        }
    }

    public void setDocumentHighlightState(final String path, final boolean state) {
        setBool(PREF_PREFIX_HIGHLIGHT_STATE + path, state);
    }

    public boolean getDocumentHighlightState(final String path, final CharSequence chars) {
        final boolean lengthOk = chars != null && chars.length() < (_isDeviceGoodHardware ? 100000 : 35000);
        return getBool(PREF_PREFIX_HIGHLIGHT_STATE + path, lengthOk);
    }

    public @ColorInt int getEditorForegroundColor() {
        return rcolor(R.color.text_view_color);
    }

    public @ColorInt int getEditorBackgroundColor() {
        return rcolor(R.color.window_background);
    }

    private List<String> _extSettingCache = null;

    public synchronized boolean isExtOpenWithThisApp(String ext) {
        if (_extSettingCache == null) {
            String pref = "";
            _extSettingCache = Arrays.asList(pref.toLowerCase()
                    .replace("none", "")   // none == no ext
                    .replace(" ", "")      // remove spaces
                    .replace(",.", ",")    // remove leading dot
                    .split(","));
        }

        ext = ext.trim();
        ext = ext.startsWith(".") ? ext.substring(1) : ext;
        return _extSettingCache.contains(ext) || _extSettingCache.contains("*");
    }

    public String getUnorderedListCharacter() {
        return "-";
    }

    public boolean getSetWebViewFulldrawing(boolean... setValue) {
        final String k = "getSetWebViewFulldrawing";
        if (setValue != null && setValue.length == 1) {
            setBool(k, setValue[0]);
            return setValue[0];
        }
        return getBool(k, false);
    }

    public boolean isOpenLinksWithChromeCustomTabs() {
        return true;
    }
}
