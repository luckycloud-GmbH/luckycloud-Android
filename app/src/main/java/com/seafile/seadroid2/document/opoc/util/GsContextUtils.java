/*#######################################################
 *
 * SPDX-FileCopyrightText: 2016-2025 Gregor Santner <gsantner AT mailbox DOT org>
 * SPDX-License-Identifier: Unlicense OR CC0-1.0
 *
 * Written 2016-2025 by Gregor Santner <gsantner AT mailbox DOT org>
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
#########################################################*/
package com.seafile.seadroid2.document.opoc.util;

import static android.graphics.Bitmap.CompressFormat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityManagerCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.text.TextUtilsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;

import com.seafile.seadroid2.document.opoc.format.GsTextUtils;
import com.seafile.seadroid2.document.opoc.wrapper.GsCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@SuppressWarnings({"UnusedReturnValue", "rawtypes", "unused"})
public class GsContextUtils {
    //########################
    //## Constructor
    //########################
    public static final GsContextUtils instance = new GsContextUtils();

    public GsContextUtils() {
    }

    protected <T extends GsContextUtils> T thisp() {
        //noinspection unchecked
        return (T) this;
    }

    //########################
    //## Static fields & members
    //########################
    @SuppressLint("ConstantLocale")
    public final static Locale INITIAL_LOCALE = Locale.getDefault();
    public final static String EXTRA_FILEPATH = "EXTRA_FILEPATH";
    public final static String EXTRA_URI = "EXTRA_URI";
    public final static SimpleDateFormat DATEFORMAT_RFC3339ISH = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss", INITIAL_LOCALE);
    public final static String MIME_TEXT_PLAIN = "text/plain";
    public final static String PREF_KEY__SAF_TREE_URI = "pref_key__saf_tree_uri";
    public final static String CONTENT_RESOLVER_FILE_PROXY_SEGMENT = "CONTENT_RESOLVER_FILE_PROXY_SEGMENT";

    public final static int REQUEST_CAMERA_PICTURE = 50001;
    public final static int REQUEST_PICK_PICTURE = 50002;
    public final static int REQUEST_SAF = 50003;
    public final static int REQUEST_STORAGE_PERMISSION_M = 50004;
    public final static int REQUEST_STORAGE_PERMISSION_R = 50005;
    public final static int REQUEST_RECORD_AUDIO = 50006;
    private final static int BLINK_ANIMATOR_TAG = -1206813720;

    public static int TEXTFILE_OVERWRITE_MIN_TEXT_LENGTH = 2;
    protected static Pair<File, List<Pair<String, String>>> m_cacheLastExtractFileMetadata;
    protected static String _lastCameraPictureFilepath = null;
    protected static WeakReference<GsCallback.a1<String>> _receivePathCallback = null;
    protected static String m_chooserTitle = "➥";


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //########################
    //## Resources
    //########################
    public enum ResType {
        ID, BOOL, INTEGER, COLOR, STRING, ARRAY, DRAWABLE, PLURALS,
        ANIM, ATTR, DIMEN, LAYOUT, MENU, RAW, STYLE, XML,
    }

    /**
     * Find out the numerical resource id by given {@link ResType}
     *
     * @return A valid id if the id could be found, else 0
     */
    @SuppressLint("DiscouragedApi")
    public int getResId(final Context context, final ResType resType, String name) {
        try {
            name = name.toLowerCase(Locale.ROOT).replace("#", "no").replaceAll("[^A-Za-z0-9_]", "_");
            return context.getResources().getIdentifier(name, resType.name().toLowerCase(Locale.ENGLISH), context.getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get String by given string resource id (numeric)
     */
    public String rstr(Context context, @StringRes final int strResId) {
        try {
            return context.getString(strResId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get String by given string resource identifier (textual)
     */
    public String rstr(final Context context, final String strResKey, Object... a0getResKeyAsFallback) {
        try {
            final String s = rstr(context, getResId(context, ResType.STRING, strResKey));
            if (s != null) {
                return s;
            }
        } catch (Exception ignored) {
        }
        return a0getResKeyAsFallback != null && a0getResKeyAsFallback.length > 0 ? strResKey : null;
    }

    /**
     * Get drawable from given resource identifier
     */
    public Drawable rdrawable(final Context context, @DrawableRes final int resId) {
        try {
            return ContextCompat.getDrawable(context, resId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the apps base packagename, which is equal with all build flavors and variants
     */
    public String getAppIdUsedAtManifest(final Context context) {
        String pkg = rstr(context, "manifest_package_id");
        return !TextUtils.isEmpty(pkg) ? pkg : context.getPackageName();
    }

    /**
     * Get this apps package name, returns the flavor specific package name.
     */
    public String getAppIdFlavorSpecific(final Context context) {
        return context.getPackageName();
    }

    /**
     * Load html into a {@link Spanned} object and set the
     * {@link TextView}'s text using {@link TextView#setText(CharSequence)}
     */
    public void setHtmlToTextView(final TextView textView, final String html) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(new SpannableString(htmlToSpanned(html)));
    }

    /**
     * Estimate this device's screen diagonal size in inches
     */
    public double getEstimatedScreenSizeInches(final Context context) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();

        double calc = dm.density * 160d;
        double x = Math.pow(dm.widthPixels / calc, 2);
        double y = Math.pow(dm.heightPixels / calc, 2);
        calc = Math.sqrt(x + y) * 1.16;  // 1.16 = est. Nav/Statusbar
        return Math.min(12, Math.max(4, calc));
    }

    /**
     * Check if the device is currently in portrait orientation
     */
    public boolean isInPortraitMode(final Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * Get an {@link Locale} out of a android language code
     * The {@code androidLC} may be in any of the forms: de, en, de-rAt
     */
    public Locale getLocaleByAndroidCode(String androidLC) {
        if (!TextUtils.isEmpty(androidLC)) {
            return androidLC.contains("-r")
                    ? new Locale(androidLC.substring(0, 2), androidLC.substring(4, 6)) // de-rAt
                    : new Locale(androidLC); // de
        }
        return Resources.getSystem().getConfiguration().locale;
    }

    /**
     * Set the apps language
     * {@code androidLC} may be in any of the forms: en, de, de-rAt
     * If given an empty string, the default (system) locale gets loaded
     */
    public <T extends GsContextUtils> T setAppLanguage(final Context context, final String androidLC) {
        Locale locale = getLocaleByAndroidCode(androidLC);
        locale = (locale != null && !androidLC.isEmpty()) ? locale : Resources.getSystem().getConfiguration().locale;
        setAppLocale(context, locale);
        return thisp();
    }

    public <T extends GsContextUtils> T setAppLocale(final Context context, final Locale locale) {
        Configuration config = context.getResources().getConfiguration();
        config.locale = (locale != null ? locale : Resources.getSystem().getConfiguration().locale);
        context.getResources().updateConfiguration(config, null);
        //noinspection ConstantConditions
        Locale.setDefault(locale);
        return thisp();
    }

    /**
     * Send a {@link Intent#ACTION_VIEW} Intent with given parameter
     * If the parameter is an string a browser will get triggered
     */
    public <T extends GsContextUtils> T openWebpageInExternalBrowser(final Context context, final String url) {
        try {
            startActivity(context, new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thisp();
    }

    /**
     * Try to guess if the color on top of the given {@code colorOnBottomInt}
     * should be light or dark. Returns true if top color should be light
     */
    public boolean shouldColorOnTopBeLight(@ColorInt final int colorOnBottomInt) {
        return 186 > (((0.299 * Color.red(colorOnBottomInt))
                + ((0.587 * Color.green(colorOnBottomInt))
                + (0.114 * Color.blue(colorOnBottomInt)))));
    }

    @ColorInt
    public static int rgb(final int r, final int g, final int b) {
        return argb(255, r, g, b);
    }

    @ColorInt
    public static int argb(final int a, final int r, final int g, final int b) {
        return (Math.max(0, Math.min(255, a)) << 24) | (Math.max(0, Math.min(255, r)) << 16) | (Math.max(0, Math.min(255, g)) << 8) | Math.max(0, Math.min(255, b));
    }


    /**
     * Convert a html string to an android {@link Spanned} object
     */
    public Spanned htmlToSpanned(final String html) {
        Spanned result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    /**
     * Convert pixel unit do android dp unit
     */
    public float convertPxToDp(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    /**
     * Convert android dp unit to pixel unit
     */
    public int convertDpToPx(final Context context, final float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    /**
     * Get public (accessible) appdata folders
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public List<Pair<File, String>> getAppDataPublicDirs(final Context context, boolean internalStorageFolder, boolean sdcardFolders, boolean storageNameWithoutType) {
        List<Pair<File, String>> dirs = new ArrayList<>();
        for (File externalFileDir : ContextCompat.getExternalFilesDirs(context, null)) {
            if (externalFileDir == null || Environment.getExternalStorageDirectory() == null) {
                continue;
            }
            boolean isInt = externalFileDir.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().getAbsolutePath());
            boolean add = (internalStorageFolder && isInt) || (sdcardFolders && !isInt);
            if (add) {
                dirs.add(new Pair<>(externalFileDir, getStorageName(externalFileDir, storageNameWithoutType)));
                if (!externalFileDir.exists() && externalFileDir.mkdirs()) ;
            }
        }
        return dirs;
    }

    public String getStorageName(final File externalFileDir, final boolean storageNameWithoutType) {
        boolean isInt = externalFileDir.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().getAbsolutePath());

        String[] split = externalFileDir.getAbsolutePath().split("/");
        if (split.length > 2) {
            return isInt ? (storageNameWithoutType ? "Internal Storage" : "") : (storageNameWithoutType ? split[2] : ("SD Card (" + split[2] + ")"));
        } else {
            return "Storage";
        }
    }

    public List<Pair<File, String>> getStorages(final Context context, final boolean internalStorageFolder, final boolean sdcardFolders) {
        List<Pair<File, String>> storages = new ArrayList<>();
        for (Pair<File, String> pair : getAppDataPublicDirs(context, internalStorageFolder, sdcardFolders, true)) {
            if (pair.first != null && pair.first.getAbsolutePath().lastIndexOf("/Android/data") > 0) {
                try {
                    storages.add(new Pair<>(new File(pair.first.getCanonicalPath().replaceFirst("/Android/data.*", "")), pair.second));
                } catch (IOException ignored) {
                }
            }
        }
        return storages;
    }

    public File getStorageRootFolder(final Context context, final File file) {
        String filepath;
        try {
            filepath = file.getCanonicalPath();
        } catch (Exception ignored) {
            return null;
        }
        for (Pair<File, String> storage : getStorages(context, false, true)) {
            if (filepath.startsWith(storage.first.getAbsolutePath())) {
                return storage.first;
            }
        }
        return null;
    }

    /**
     * Calculates the scaling factor so the bitmap is maximal as big as the maxDimen
     *
     * @param options  Bitmap-options that contain the current dimensions of the bitmap
     * @param maxDimen Max size of the Bitmap (width or height)
     * @return the scaling factor that needs to be applied to the bitmap
     */
    public int calculateInSampleSize(final BitmapFactory.Options options, final int maxDimen) {
        // Raw height and width of image
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (Math.max(height, width) > maxDimen) {
            inSampleSize = Math.round(1f * Math.max(height, width) / maxDimen);
        }
        return inSampleSize;
    }

    /**
     * Try to tint all {@link Menu}s {@link MenuItem}s with given color
     */
    public void tintMenuItems(final @Nullable Menu menu, final boolean recurse, @ColorInt final int iconColor) {
        if (menu == null) {
            return;
        }

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            try {
                tintDrawable(item.getIcon(), iconColor);
                if (item.hasSubMenu() && recurse) {
                    //noinspection ConstantConditions
                    tintMenuItems(item.getSubMenu(), recurse, iconColor);
                }
            } catch (Exception ignored) {
                // This should not happen at all, but may in bad menu.xml configuration
            }
        }
    }

    /**
     * Loads {@link Drawable} by given {@link DrawableRes} and applies a color
     */
    public Drawable tintDrawable(final Context context, @DrawableRes final int drawableRes, @ColorInt final int color) {
        return tintDrawable(rdrawable(context, drawableRes), color);
    }

    /**
     * Tint a {@link Drawable} with given {@code color}
     */
    public Drawable tintDrawable(@Nullable Drawable drawable, @ColorInt final int color) {
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), color);
        }
        return drawable;
    }

    /**
     * Try to make icons in Toolbar/ActionBars SubMenus visible
     * This may not work on some devices and it maybe won't work on future android updates
     */
    public void setSubMenuIconsVisibility(final Menu menu, final boolean visible) {
        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            return;
        }
        if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                @SuppressLint("PrivateApi") Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, visible);
            } catch (Exception ignored) {
                Log.d(getClass().getName(), "Error: 'setSubMenuIconsVisibility' not supported on this device");
            }
        }
    }

    public String getMimeType(final Context context, final File file) {
        return getMimeType(context, file.getAbsolutePath());
    }

    /**
     * Detect MimeType of given file
     */
    public String getMimeType(final Context context, String uri) {
        String mimeType;
        uri = uri.replaceFirst("\\.jenc$", "");
        if (uri.startsWith(ContentResolver.SCHEME_CONTENT + "://")) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(Uri.parse(uri));
        } else {
            String ext = MimeTypeMap.getFileExtensionFromUrl(uri);
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
        }

        // Next-best try if other methods fail
        if (GsTextUtils.isNullOrEmpty(mimeType) && new File(uri).exists()) {
            mimeType = GsFileUtils.getMimeType(new File(uri));
        }

        if (GsTextUtils.isNullOrEmpty((mimeType))) {
            mimeType = "*/*";
        }
        return mimeType.toLowerCase(Locale.ROOT);
    }

    /**
     * Parse color hex string, using RGBA (instead of {@link Color#parseColor(String)} which uses ARGB)
     *
     * @param hexcolorString Hex color string in RRGGBB or RRGGBBAA format
     * @return {@link ColorInt}
     */
    public @ColorInt
    Integer parseHexColorString(final String hexcolorString) {
        String h = TextUtils.isEmpty(hexcolorString) ? "" : hexcolorString;
        h = h.replaceAll("[^A-Fa-f0-9]", "").trim();
        if (h.isEmpty() || h.length() > 8) {
            return null;
        }
        try {
            if (h.length() > 6) {
                h = h.substring(6) + (h.length() == 8 ? "" : "0") + h.substring(0, 6);
            }
            return Color.parseColor("#" + h);
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean isDeviceGoodHardware(final Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            return !ActivityManagerCompat.isLowRamDevice(activityManager) &&
                    Runtime.getRuntime().availableProcessors() >= 4 &&
                    activityManager.getMemoryClass() >= 128;
        } catch (Exception ignored) {
            return true;
        }
    }

    /**
     * Animate to specified Activity
     *
     * @param to                 The class of the activity
     * @param finishFromActivity true: Finish the current activity
     * @param requestCode        Request code for stating the activity, not waiting for result if null
     */
    public <T extends GsContextUtils> T animateToActivity(final Activity context, final Class to, final Boolean finishFromActivity, final Integer requestCode) {
        return animateToActivity(context, new Intent(context, to), finishFromActivity, requestCode);
    }

    /**
     * Animate to Activity specified in intent
     * Requires animation resources
     *
     * @param intent             Intent to open start an activity
     * @param finishFromActivity true: Finish the current activity
     * @param requestCode        Request code for stating the activity, not waiting for result if null
     */
    public <T extends GsContextUtils> T animateToActivity(final Activity context, final Intent intent, final Boolean finishFromActivity, final Integer requestCode) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (requestCode != null) {
            context.startActivityForResult(intent, requestCode);
        } else {
            context.startActivity(intent);
        }
        context.overridePendingTransition(getResId(context, ResType.DIMEN, "fadein"), getResId(context, ResType.DIMEN, "fadeout"));
        if (finishFromActivity != null && finishFromActivity) {
            context.finish();
        }
        return thisp();
    }

    /**
     * Start activity specified by Intent. Add FLAG_ACTIVITY_NEW_TASK in case passed context is not a {@link Activity}
     * (when a non-Activity {@link Context} is passed a Exception is thrown otherwise)
     *
     * @param context Context, preferably a Activity
     * @param intent  Intent
     */
    public void startActivity(final Context context, final Intent intent) {
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(intent);
    }

    private static File checkPath(final String path) {
        final File f;
        return (!TextUtils.isEmpty(path) && (f = new File(path)).canRead()) ? f : null;
    }

    private static Uri getUriFromIntent(final Intent intent, final @Nullable Context context) {
        Uri uri = intent.getData();

        if (uri == null) {
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }

        if (uri == null && context != null) {
            uri = new ShareCompat.IntentReader(context, intent).getStream();
        }

        return uri;
    }

    /**
     * Try to force extract a absolute filepath from an intent
     *
     * @param receivingIntent The intent from {@link Activity#getIntent()}
     * @return A file or null if extraction did not succeed
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    public static File extractFileFromIntent(final Intent receivingIntent, final Context context) {
        final String action = receivingIntent.getAction();
        final String type = receivingIntent.getType();
        final String extPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        final Uri fileUri = getUriFromIntent(receivingIntent, context);

        String tmps;
        String fileStr;
        File result = null;

        if (Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action) || Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {

            // Màrkor, SimpleMobileTools FileManager
            if (receivingIntent.hasExtra((tmps = EXTRA_FILEPATH))) {
                result = checkPath(receivingIntent.getStringExtra(tmps));
            }

            // Analyze data/Uri
            if (result == null && fileUri != null && (fileStr = fileUri.toString()) != null) {
                // Uri contains file
                if (fileStr.startsWith("file://")) {
                    result = checkPath(fileUri.getPath());
                }

                if (fileStr.startsWith((tmps = "content://"))) {
                    fileStr = fileStr.substring(tmps.length());
                    String fileProvider = fileStr.substring(0, fileStr.indexOf("/"));
                    fileStr = fileStr.substring(fileProvider.length() + 1);

                    // Some file managers dont add leading slash
                    if (fileStr.startsWith("storage/")) {
                        fileStr = "/" + fileStr;
                    }
                    // Some do add some custom prefix
                    for (String prefix : new String[]{"file", "document", "root_files", "name"}) {
                        if (fileStr.startsWith(prefix)) {
                            fileStr = fileStr.substring(prefix.length());
                        }
                    }

                    // prefix for External storage (/storage/emulated/0  ///  /sdcard/) --> e.g. "content://com.amaze.filemanager/storage_root/file.txt" = "/sdcard/file.txt"
                    for (String prefix : new String[]{"external/", "media/", "storage_root/", "external-path/"}) {
                        if (result == null && fileStr.startsWith((tmps = prefix))) {
                            result = checkPath(Uri.decode(extPath + "/" + fileStr.substring(tmps.length())));
                        }
                    }

                    // Next/OwnCloud Fileprovider
                    for (String fp : new String[]{"org.nextcloud.files", "org.nextcloud.beta.files", "org.owncloud.files"}) {
                        if (result == null && fileProvider.equals(fp) && fileStr.startsWith(tmps = "external_files/")) {
                            result = checkPath(Uri.decode("/storage/" + fileStr.substring(tmps.length()).trim()));
                        }
                    }

                    // AOSP File Manager/Documents
                    if (result == null && fileProvider.equals("com.android.externalstorage.documents") && fileStr.startsWith(tmps = "/primary%3A")) {
                        result = checkPath(Uri.decode(extPath + "/" + fileStr.substring(tmps.length())));
                    }

                    // Mi File Explorer
                    if (result == null && fileProvider.equals("com.mi.android.globalFileexplorer.myprovider") && fileStr.startsWith(tmps = "external_files")) {
                        result = checkPath(Uri.decode(extPath + fileStr.substring(tmps.length())));
                    }

                    if (result == null && fileStr.startsWith(tmps = "external_files/")) {
                        for (String prefix : new String[]{extPath, "/storage", ""}) {
                            if (result == null) {
                                result = checkPath(Uri.decode(prefix + "/" + fileStr.substring(tmps.length())));
                            }
                        }
                    }

                    // URI Encoded paths with full path after content://package/
                    if (result == null && fileStr.startsWith("/") || fileStr.startsWith("%2F")) {
                        result = checkPath(Uri.decode(fileStr));
                        if (result == null) {
                            result = checkPath(fileStr);
                        }
                    }
                }
            }

            if (result == null && fileUri != null && !TextUtils.isEmpty(tmps = fileUri.getPath()) && tmps.startsWith("/")) {
                result = checkPath(tmps);
            }

            // Scan MediaStore.MediaColumns
            final String[] sarr = contentColumnData(context, receivingIntent, MediaStore.MediaColumns.DATA, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? MediaStore.MediaColumns.DATA : null));
            if (result == null && sarr[0] != null) {
                result = checkPath(sarr[0]);
            }

            if (result == null && sarr[1] != null) {
                result = checkPath(Environment.getExternalStorageDirectory() + "/" + sarr[1]);
            }
        }

        // Try build proxy by ContentResolver if no file found
        if (result == null) {
            try {
                // Try detect content file & filename in Intent

                final String[] sarr = contentColumnData(context, receivingIntent, OpenableColumns.DISPLAY_NAME);
                tmps = sarr != null && !TextUtils.isEmpty(sarr[0]) ? sarr[0] : fileUri.getLastPathSegment();

                // Proxy file to app-private storage (= java.io.File)
                File f = new File(context.getCacheDir(), CONTENT_RESOLVER_FILE_PROXY_SEGMENT + "/" + tmps);
                f.getParentFile().mkdirs();
                byte[] data = GsFileUtils.readCloseBinaryStream(context.getContentResolver().openInputStream(fileUri));
                GsFileUtils.writeFile(f, data, null);
                f.setReadable(true);
                f.setWritable(true);
                result = checkPath(f.getAbsolutePath());
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    public static String[] contentColumnData(final Context context, final Intent intent, final String... columns) {
        final Uri uri = getUriFromIntent(intent, context);
        final String[] out = (new String[columns.length]);
        final int INVALID = -1;
        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(uri, columns, null, null, null);
        } catch (Exception ignored) {
            cursor = null;
        }
        if (cursor != null && cursor.moveToFirst()) {
            for (int i = 0; i < columns.length; i++) {
                final int coli = TextUtils.isEmpty(columns[i]) ? INVALID : cursor.getColumnIndex(columns[i]);
                out[i] = (coli == INVALID ? null : cursor.getString(coli));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return out;
    }

    private void setPathCallback(final GsCallback.a1<String> callback) {
        _receivePathCallback = new WeakReference<>(callback);
    }

    private void sendPathCallback(final String path) {
        if (!GsTextUtils.isNullOrEmpty(path) && _receivePathCallback != null) {
            final GsCallback.a1<String> cb = _receivePathCallback.get();
            if (cb != null) {
                cb.callback(path);
            }
        }
        // Send only once and once only
        _receivePathCallback = null;
    }

    /**
     * Extract result data from {@link Activity}.onActivityResult.
     * Forward all arguments from context. Only requestCodes as implemented in {@link GsContextUtils} are analyzed.
     * Also may forward results via callback
     */
    @SuppressLint("ApplySharedPref")
    public void extractResultFromActivityResult(final Activity context, final int requestCode, final int resultCode, final Intent intent) {
        switch (requestCode) {
            case REQUEST_CAMERA_PICTURE: {
                sendPathCallback(resultCode == Activity.RESULT_OK ? _lastCameraPictureFilepath : null);
                break;
            }
            case REQUEST_PICK_PICTURE: {
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    Uri selectedImage = intent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    String picturePath = null;

                    Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        for (String column : filePathColumn) {
                            int curColIndex = cursor.getColumnIndex(column);
                            if (curColIndex == -1) {
                                continue;
                            }
                            picturePath = cursor.getString(curColIndex);
                            if (!TextUtils.isEmpty(picturePath)) {
                                break;
                            }
                        }
                        cursor.close();
                    }

                    // Try to grab via file extraction method
                    intent.setAction(Intent.ACTION_VIEW);
                    picturePath = picturePath != null ? picturePath : GsFileUtils.getPath(extractFileFromIntent(intent, context));

                    // Retrieve image from file descriptor / Cloud, e.g.: Google Drive, Picasa
                    if (picturePath == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        try {
                            final ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(selectedImage, "r");
                            if (parcelFileDescriptor != null) {
                                final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                                final FileInputStream input = new FileInputStream(fileDescriptor);

                                // Create temporary file in cache directory
                                final File temp = File.createTempFile("image", "tmp", context.getCacheDir());
                                temp.deleteOnExit();
                                picturePath = temp.getAbsolutePath();

                                GsFileUtils.writeFile(new File(picturePath), GsFileUtils.readCloseBinaryStream(input), null);
                            }
                        } catch (IOException ignored) {
                            // nothing we can do here, null value will be handled below
                        }
                    }

                    // Return path to picture on success, else null
                    sendPathCallback(picturePath);
                }
                break;
            }
            case REQUEST_RECORD_AUDIO: {
                if (resultCode == Activity.RESULT_OK && intent != null && intent.getData() != null) {
                    final Uri uri = intent.getData();
                    final String uriPath = uri.getPath();
                    final String ext = uriPath == null || !uriPath.contains(".") ? "" : uriPath.substring(uriPath.lastIndexOf("."));
                    final String datestr = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss", Locale.ENGLISH).format(new Date());
                    final File temp = new File(context.getCacheDir(), datestr + ext);
                    GsFileUtils.copyUriToFile(context, uri, temp);
                    sendPathCallback(temp.getAbsolutePath());
                }
                break;
            }
            case REQUEST_SAF: {
                if (resultCode == Activity.RESULT_OK && intent != null && intent.getData() != null) {
                    final Uri treeUri = intent.getData();
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_KEY__SAF_TREE_URI, treeUri.toString()).commit();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        final ContentResolver resolver = context.getContentResolver();
                        try {
                            resolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        } catch (SecurityException se) {
                            resolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                    }
                }
                break;
            }
            case REQUEST_STORAGE_PERMISSION_M:
            case REQUEST_STORAGE_PERMISSION_R: {
                checkExternalStoragePermission(context);
                break;
            }
        }
    }

    /**
     * By default Chrome Custom Tabs only uses Chrome Stable to open links
     * There are also other packages (like Chrome Beta, Chromium, Firefox, ..)
     * which implement the Chrome Custom Tab interface. This method changes
     * the customtab intent to use an available compatible browser, if available.
     */
    public void enableChromeCustomTabsForOtherBrowsers(final Context context, final Intent customTabIntent) {
        String[] checkpkgs = new String[]{
                "com.android.chrome", "com.chrome.beta", "com.chrome.dev", "com.google.android.apps.chrome", "org.chromium.chrome",
                "org.mozilla.fennec_fdroid", "org.mozilla.firefox", "org.mozilla.firefox_beta", "org.mozilla.fennec_aurora",
                "org.mozilla.klar", "org.mozilla.focus",
        };

        // Get all intent handlers for web links
        PackageManager pm = context.getPackageManager();
        Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com"));
        List<String> browsers = new ArrayList<>();
        for (ResolveInfo ri : pm.queryIntentActivities(urlIntent, 0)) {
            Intent i = new Intent("android.support.customtabs.action.CustomTabsService");
            i.setPackage(ri.activityInfo.packageName);
            if (pm.resolveService(i, 0) != null) {
                browsers.add(ri.activityInfo.packageName);
            }
        }

        // Check if the user has a "default browser" selected
        ResolveInfo ri = pm.resolveActivity(urlIntent, 0);
        String userDefaultBrowser = (ri == null) ? null : ri.activityInfo.packageName;

        // Select which browser to use out of all installed customtab supporting browsers
        String pkg = null;
        if (browsers.size() == 1) {
            pkg = browsers.get(0);
        } else if (!TextUtils.isEmpty(userDefaultBrowser) && browsers.contains(userDefaultBrowser)) {
            pkg = userDefaultBrowser;
        } else if (!browsers.isEmpty()) {
            for (String checkpkg : checkpkgs) {
                if (browsers.contains(checkpkg)) {
                    pkg = checkpkg;
                    break;
                }
            }
            if (pkg == null) {
                pkg = browsers.get(0);
            }
        }
        if (pkg != null && customTabIntent != null) {
            customTabIntent.setPackage(pkg);
        }
    }

    @SuppressWarnings("deprecation")
    public boolean openWebpageInChromeCustomTab(final Context context, final String url) {
        boolean ok = false;
        try {
            // Use a CustomTabsIntent.Builder to configure CustomTabsIntent.
            // Once ready, call CustomTabsIntent.Builder.build() to create a CustomTabsIntent
            // and launch the desired Url with CustomTabsIntent.launchUrl()
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(ContextCompat.getColor(context, getResId(context, ResType.COLOR, "primary")));
            builder.setSecondaryToolbarColor(ContextCompat.getColor(context, getResId(context, ResType.COLOR, "primary_dark")));
            builder.addDefaultShareMenuItem();
            CustomTabsIntent customTabsIntent = builder.build();
            enableChromeCustomTabsForOtherBrowsers(context, customTabsIntent.intent);
            customTabsIntent.launchUrl(context, Uri.parse(url));
            ok = true;
        } catch (Exception ignored) {
        }
        return ok;
    }

    /***
     * Request storage access. The user needs to press "Select storage" at the correct storage.
     * @param context The {@link Activity} which will receive the result from startActivityForResult
     */
    public void requestStorageAccessFramework(final Activity context) {
        if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            );
            context.startActivityForResult(intent, REQUEST_SAF);
        }
    }

    /**
     * Get storage access framework tree uri. The user must have granted access via {@link #requestStorageAccessFramework(Activity)}
     *
     * @return Uri or null if not granted yet
     */
    public Uri getStorageAccessFrameworkTreeUri(final Context context) {
        String treeStr = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_KEY__SAF_TREE_URI, null);
        if (!TextUtils.isEmpty(treeStr)) {
            try {
                return Uri.parse(treeStr);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Check whether or not a file is under a storage access folder (external storage / SD)
     *
     * @param file The file object (file/folder)
     * @return Whether or not the file is under storage access folder
     */
    public boolean isUnderStorageAccessFolder(final Context context, final File file, boolean isDir) {
        if (file != null) {
            isDir = isDir || (file.exists() && file.isDirectory());
            // When file writeable as is, it's the fastest way to learn SAF isn't required
            if (canWriteFile(context, file, isDir, false)) {
                return false;
            }
            for (Pair<File, String> storage : getStorages(context, false, true)) {
                if (file.getAbsolutePath().startsWith(storage.first.getAbsolutePath())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isContentResolverProxyFile(final File file) {
        return file != null && file.getParentFile() != null && CONTENT_RESOLVER_FILE_PROXY_SEGMENT.equals(file.getParentFile().getName());
    }

    public Collection<File> getCacheDirs(final Context context) {
        final Set<File> dirs = new HashSet<>();
        dirs.add(context.getCacheDir());
        dirs.add(context.getExternalCacheDir());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            dirs.addAll(Arrays.asList(context.getExternalCacheDirs()));
        }
        dirs.removeAll(Collections.singleton(null));
        return dirs;
    }

    /**
     * Check whether or not a file can be written.
     * Requires storage access framework permission for external storage (SD)
     *
     * @param file  The file object (file/folder)
     * @param isDir Whether or not the given file parameter is a directory
     * @return Whether or not the file can be written
     */
    public boolean canWriteFile(final Context context, final File file, final boolean isDir, final boolean trySaf) {
        if (file == null) {
            return false;
        }

        // Try direct file access
        if (GsFileUtils.canCreate(file)) {
            return true;
        }

        // Own AppData directories do not require any special permission or handling
        if (GsCollectionUtils.any(getCacheDirs(context), f -> GsFileUtils.isChild(f, file))) {
            return true;
        }

        if (trySaf) {
            final DocumentFile dof = getDocumentFile(context, file, isDir);
            return dof != null && dof.canWrite();
        }

        return false;
    }

    /**
     * Get a {@link DocumentFile} object out of a normal java {@link File}.
     * When used on a external storage (SD), use {@link #requestStorageAccessFramework(Activity)}
     * first to get access. Otherwise this will fail.
     *
     * @param file  The file/folder to convert
     * @param isDir Whether or not file is a directory. For non-existing (to be created) files this info is not known hence required.
     * @return A {@link DocumentFile} object or null if file cannot be converted
     */
    @SuppressWarnings("RegExpRedundantEscape")
    public DocumentFile getDocumentFile(final Context context, final File file, final boolean isDir) {
        // On older versions use fromFile
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return DocumentFile.fromFile(file);
        }

        // Find storage root folder
        File baseFolderFile = getStorageRootFolder(context, file);
        String baseFolder = baseFolderFile == null ? null : baseFolderFile.getAbsolutePath();
        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }

        String relPath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath)) {
                relPath = fullPath.substring(baseFolder.length() + 1);
            } else {
                originalDirectory = true;
            }
        } catch (IOException e) {
            return null;
        } catch (Exception ignored) {
            originalDirectory = true;
        }
        Uri treeUri;
        if ((treeUri = getStorageAccessFrameworkTreeUri(context)) == null) {
            return null;
        }
        DocumentFile dof = DocumentFile.fromTreeUri(context, treeUri);
        if (originalDirectory) {
            return dof;
        }
        String[] parts = relPath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            @SuppressWarnings("ConstantConditions")
            DocumentFile nextDof = dof.findFile(parts[i]);
            if (nextDof == null) {
                try {
                    nextDof = ((i < parts.length - 1) || isDir) ? dof.createDirectory(parts[i]) : dof.createFile("image", parts[i]);
                } catch (Exception ignored) {
                }
            }
            dof = nextDof;
        }
        return dof;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "StatementWithEmptyBody"})
    public void writeFile(final Activity context, final File file, final boolean isDirectory, final GsCallback.a2<Boolean, OutputStream> writeFileCallback) {
        try {
            OutputStream fileOutputStream = null;
            ParcelFileDescriptor pfd = null;
            final boolean existingEmptyFile = file.canWrite() && file.length() < TEXTFILE_OVERWRITE_MIN_TEXT_LENGTH;
            final boolean nonExistingCreatableFile = !file.exists() && file.getParentFile() != null && file.getParentFile().canWrite();
            if (isContentResolverProxyFile(file)) {
                // File initially read from Activity, Intent & ContentResolver -> write back to it
                try {
                    Intent intent = context.getIntent();
                    Uri uri = new ShareCompat.IntentReader(context, intent).getStream();
                    uri = (uri != null ? uri : intent.getData());
                    fileOutputStream = context.getContentResolver().openOutputStream(uri, "rwt");
                } catch (Exception ignored) {
                }
            } else if (existingEmptyFile || nonExistingCreatableFile) {
                if (isDirectory) {
                    file.mkdirs();
                } else {
                    fileOutputStream = new FileOutputStream(file);
                }
            } else {
                DocumentFile dof = getDocumentFile(context, file, isDirectory);
                if (dof != null && dof.canWrite()) {
                    if (isDirectory) {
                        // Nothing to do
                    } else {
                        pfd = context.getContentResolver().openFileDescriptor(dof.getUri(), "rwt");
                        fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                    }
                }
            }
            if (writeFileCallback != null) {
                writeFileCallback.callback(fileOutputStream != null || (isDirectory && file.exists()), fileOutputStream);
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (Exception ignored) {
                }
            }
            if (pfd != null) {
                pfd.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param locale   {@link Locale} locale
     * @param format   {@link String} text which 'll be used as format for {@link SimpleDateFormat}
     * @param datetime {@link Long}   requested time miliseconds
     * @param fallback {@link String} default fallback value. If the format is incorrect and a default is not provided, return the specified format
     * @return formatted string
     */
    public String formatDateTime(@Nullable final Locale locale, @NonNull final String format, @Nullable final Long datetime, @Nullable final String... fallback) {
        try {
            final Locale l = locale != null ? locale : Locale.getDefault();
            final long t = datetime != null ? datetime : System.currentTimeMillis();
            return new SimpleDateFormat(GsTextUtils.unescapeString(format), l).format(t);
        } catch (Exception err) {
            return (fallback != null && fallback.length > 0) ? fallback[0] : format;
        }
    }

    public void requestExternalStoragePermission(final Activity activity) {
        final int v = Build.VERSION.SDK_INT;

        if (v >= Build.VERSION_CODES.R) {
            try {
                final Uri uri = Uri.parse("package:" + getAppIdFlavorSpecific(activity));
                final Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                activity.startActivityForResult(intent, REQUEST_STORAGE_PERMISSION_R);
            } catch (final Exception ex) {
                final Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(intent, REQUEST_STORAGE_PERMISSION_R);
            }
        }

        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_M);
    }

    public void requestExternalStoragePermission(final Activity activity, @StringRes int description) {
        requestExternalStoragePermission(activity, activity.getString(description));
    }

    public void requestExternalStoragePermission(final Activity activity, final String description) {
        final AlertDialog d = new AlertDialog.Builder(activity)
                .setMessage(description)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> requestExternalStoragePermission(activity))
                .setNegativeButton(android.R.string.no, null)
                .show();
        d.setCanceledOnTouchOutside(false);
    }

    @SuppressWarnings("ConstantConditions")
    public boolean checkExternalStoragePermission(final Context context) {
        final int v = Build.VERSION.SDK_INT;

        // Android R Manage-All-Files permission
        if (v >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }

        // Android M permissions
        if (v >= Build.VERSION_CODES.M && v < Build.VERSION_CODES.R) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        // In case unsure, check if anything is writable at external storage
        for (final File f : Environment.getExternalStorageDirectory() != null ? Environment.getExternalStorageDirectory().listFiles() : new File[0]) {
            if (f.canWrite()) {
                return true;
            }
        }

        return false;
    }

    public static String imageToBase64(Bitmap bitmap, CompressFormat format, int q) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(format, q, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT).replaceAll("\\s+", "");
    }

    public <T extends GsContextUtils> T showSoftKeyboard(final Activity activity, final boolean show, final View... view) {
        if (activity == null) {
            return thisp();
        }

        final Window win = activity.getWindow();
        if (win == null) {
            return thisp();
        }

        View focus = (view != null && view.length > 0) ? view[0] : activity.getCurrentFocus();

        if (focus == null) {
            focus = win.getDecorView();
        }

        if (focus != null) {
            final WindowInsetsControllerCompat ctrl = new WindowInsetsControllerCompat(win, focus);
            if (show) {
                focus.requestFocus();
                ctrl.show(WindowInsetsCompat.Type.ime());
            } else {
                focus.clearFocus();
                ctrl.hide(WindowInsetsCompat.Type.ime());
            }
        }

        return thisp();
    }

    public void showDialogWithHtmlTextView(final Activity context, @StringRes int resTitleId, String html) {
        showDialogWithHtmlTextView(context, resTitleId, html, true, null);
    }

    public void showDialogWithHtmlTextView(final Activity context, @StringRes int resTitleId, String text, boolean isHtml, DialogInterface.OnDismissListener dismissedListener) {
        ScrollView scroll = new ScrollView(context);
        AppCompatTextView textView = new AppCompatTextView(context);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());

        scroll.setPadding(padding, 0, padding, 0);
        scroll.addView(textView);
        textView.setMovementMethod(new LinkMovementMethod());
        textView.setText(isHtml ? new SpannableString(Html.fromHtml(text)) : text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setPositiveButton(android.R.string.ok, null).setOnDismissListener(dismissedListener)
                .setView(scroll);
        if (resTitleId != 0) {
            dialog.setTitle(resTitleId);
        }
        dialogFullWidth(dialog.show(), true, false);
    }

    public <T extends GsContextUtils> T setActivityBackgroundColor(final Activity activity, @ColorInt Integer color) {
        if (color != null) {
            try {
                ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0).setBackgroundColor(color);
            } catch (Exception ignored) {
            }
        }
        return thisp();
    }

    public <T extends GsContextUtils> T setActivityNavigationBarBackgroundColor(final Activity context, @ColorInt Integer color) {
        if (context != null && color != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    final Window window = context.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setNavigationBarColor(color);
                }
            } catch (Exception ignored) {
            }
        }
        return thisp();
    }

    public void setKeepScreenOn(final Activity activity, Boolean keepOn) {
        if (keepOn) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * Show dialog in full width / show keyboard
     *
     * @param dialog Get via dialog.show()
     */
    public void dialogFullWidth(AlertDialog dialog, boolean fullWidth, boolean showKeyboard) {
        try {
            Window w;
            if (dialog != null && (w = dialog.getWindow()) != null) {
                if (fullWidth) {
                    w.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                }
                if (showKeyboard) {
                    w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * A method to determine if current hour is between begin and end.
     * This is especially useful for time-based light/dark mode
     */
    public boolean isCurrentHourOfDayBetween(int begin, int end) {
        begin = (begin >= 23 || begin < 0) ? 0 : begin;
        end = (end >= 23 || end < 0) ? 0 : end;
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return h >= begin && h <= end;
    }

    /**
     * Check if the Dark theme mode is enable in this app currently (or at system if system theme is set)
     *
     * @param context {@link Context}
     * @return true if the dark theme/mode is currently enabled in this app
     */
    public boolean isDarkModeEnabled(final Context context) {
        final int state = AppCompatDelegate.getDefaultNightMode();
        if (state == AppCompatDelegate.MODE_NIGHT_YES) {
            return true;
        } else if (state == AppCompatDelegate.MODE_NIGHT_NO) {
            return false;
        } else {
            switch (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    return true;
                case Configuration.UI_MODE_NIGHT_NO:
                    return false;
            }
        }
        return false;
    }

    public static boolean fadeInOut(final View in, final View out, final boolean animate) {
        // Do nothing if we are already in the correct state
        if (in.getVisibility() == View.VISIBLE && out.getVisibility() == View.INVISIBLE) {
            return false;
        }

        if (animate) {
            out.animate()
                    .alpha(0f)
                    .setDuration(400)
                    .withEndAction(() -> out.setVisibility(View.INVISIBLE));

            in.setAlpha(0f);
            in.setVisibility(View.VISIBLE);
            in.animate()
                    .alpha(1f)
                    .setDuration(400);
        } else {
            out.setVisibility(View.INVISIBLE);
            in.setVisibility(View.VISIBLE);
        }

        return true;
    }
}
