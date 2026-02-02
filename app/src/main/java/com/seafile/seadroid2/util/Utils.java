package com.seafile.seadroid2.util;

import static android.app.Activity.RESULT_OK;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.documentfile.provider.DocumentFile;
import androidx.exifinterface.media.ExifInterface;

import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.seafile.seadroid2.BuildConfig;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.cameraupload.MediaSchedulerService;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.DatabaseHelper;
import com.seafile.seadroid2.data.SeafPermission;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.fileschooser.SelectableFile;
import com.seafile.seadroid2.ui.WidgetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class Utils {
    public static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".documents";
    public static final String PATH_SEPERATOR = "/";
    // public static final String NOGROUP = "$nogroup";
    public static final String PERSONAL_REPO = "personal_repo";
    public static final String SHARED_REPO = "shared_repo";
    public static final String PUBLIC_REPO = "public_repo";
    public static final String TRANSFER_PHOTO_TAG = "camera_upload";
    public static final String TRANSFER_FOLDER_TAG = "folder_backup";
    private static final String DEBUG_TAG = "Utils";
    private static final String HIDDEN_PREFIX = ".";
    private static HashMap<String, Integer> suffixIconMap = null;
    private static final int JOB_ID = 0;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");

    private Utils() {}

    public static JSONObject parseJsonObject(String json) {
        if (json == null) {
            // the caller should not give null
            Log.w(DEBUG_TAG, "null in parseJsonObject");
            return null;
        }

        try {
            return (JSONObject) new JSONTokener(json).nextValue();
        } catch (Exception e) {
            return null;
        }
    }

    public static JSONArray parseJsonArrayByKey(@NonNull String json, @NonNull String key) throws JSONException {
        String value = new JSONObject(json).optString(key);
        if (!TextUtils.isEmpty(value))
            return parseJsonArray(value);
        else
            return null;
    }

    public static JSONArray parseJsonArray(@NonNull String json) {
        try {
            return (JSONArray) new JSONTokener(json).nextValue();
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Could not parse json file", e);
            return null;
        }
    }

    public static String readFile(File file) {
        Reader reader = null;
        try {
            try {
                // TODO: detect a file's encoding
                reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return null;
            }

            char[] buffer = new char[1024];
            StringBuilder responseStrBuilder = new StringBuilder();

            while (true) {
                int len = reader.read(buffer, 0, 1024);
                if (len == -1)
                    break;
                responseStrBuilder.append(buffer, 0, len);
            }
            return responseStrBuilder.toString();
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception e) {

            }
        }
    }

    public static String getParentPath(String path) {
        if (path == null) {
            // the caller should not give null
            Log.w(DEBUG_TAG, "null in getParentPath");
            return null;
        }

        if (!path.contains("/")) {
            return "/";
        }

        String parent = path.substring(0, path.lastIndexOf("/"));
        if (parent.equals("")) {
            return "/";
        } else
            return parent;
    }

    public static String fileNameFromPath(String path) {
        if (path == null) {
            // the caller should not give null
            Log.w(DEBUG_TAG, "null in fileNameFromPath");
            return null;
        }

        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String removeExtensionFromFileName(String name) {
        if (name == null) {
            // the caller should not give null
            Log.w(DEBUG_TAG, "null in removeExtensionFromFileName");
            return null;
        }
        if (name.indexOf(".") > 0)
            name = name.substring(0, name.lastIndexOf("."));

        return name;
    }

    public static String getExtensionFromFileName(String name) {
        if (name == null) {
            // the caller should not give null
            Log.w(DEBUG_TAG, "null in getExtensionFromFileName");
            return null;
        }
        int lastIndex = name.lastIndexOf(".");
        if (lastIndex == -1) {
            return "";
        }
        return name.substring(lastIndex);
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0 KB";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1000));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1000, digitGroups)) + " " + units[digitGroups];
    }

    public static void writeFile(File file, String content) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(content.getBytes("UTF-8"));
        } finally {
            try {
                if (os != null)
                    os.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public static TreeMap<String, List<SeafRepo>> groupRepos(List<SeafRepo> repos) {
        TreeMap<String, List<SeafRepo>> map = new TreeMap<String, List<SeafRepo>>();
        String groupName = null;
        for (SeafRepo repo : repos) {
            List<SeafRepo> l;
            if (repo.isGroupRepo)
                groupName = repo.groupName;
            else if (repo.isPersonalRepo)
                groupName = PERSONAL_REPO;
            else if (repo.isSharedRepo)
                groupName = SHARED_REPO;
            else if (repo.isPublicRepo)
                groupName = PUBLIC_REPO;

            if (groupName == null) continue;
            l = map.get(groupName);
            if (l == null) {
                l = Lists.newArrayList();
                map.put(groupName, l);
            }
            l.add(repo);
        }
        return map;
    }

    public static int getResIdforMimetype(String mimetype) {
        if (mimetype == null)
            return R.drawable.ic_file;

        if (mimetype.contains("pdf")) {
            return R.drawable.file_pdf;
        } else if (mimetype.contains("image/")) {
            return R.drawable.file_image;
        } else if (mimetype.contains("text")) {
            return R.drawable.file_text;
        } else if (mimetype.contains("audio")) {
            return R.drawable.file_audio;
        } else if (mimetype.contains("video")) {
            return R.drawable.file_video;
        } if (mimetype.contains("pdf")) {
            return R.drawable.file_pdf;
        } else if (mimetype.contains("msword") || mimetype.contains("ms-word")) {
            return R.drawable.file_ms_word;
        } else if (mimetype.contains("mspowerpoint") || mimetype.contains("ms-powerpoint")) {
            return R.drawable.file_ms_ppt;
        } else if (mimetype.contains("msexcel") || mimetype.contains("ms-excel")) {
            return R.drawable.file_ms_excel;
        } else if (mimetype.contains("openxmlformats-officedocument")) {
            // see http://stackoverflow.com/questions/4212861/what-is-a-correct-mime-type-for-docx-pptx-etc
            if (mimetype.contains("wordprocessingml")) {
                return R.drawable.file_ms_word;
            } else if (mimetype.contains("spreadsheetml")) {
                return R.drawable.file_ms_excel;
            } else if (mimetype.contains("presentationml")) {
                return R.drawable.file_ms_ppt;
            }
            // } else if (mimetype.contains("application")) {
            //     return R.drawable.file_binary;
        }

        return R.drawable.ic_file;
    }

    private static synchronized HashMap<String, Integer> getSuffixIconMap() {
        if (suffixIconMap != null)
            return suffixIconMap;

        suffixIconMap = Maps.newHashMap();
        suffixIconMap.put("pdf", R.drawable.file_pdf);
        suffixIconMap.put("doc", R.drawable.file_ms_word);
        suffixIconMap.put("docx", R.drawable.file_ms_word);
        suffixIconMap.put("md", R.drawable.file_text);
        suffixIconMap.put("markdown", R.drawable.file_text);
        return suffixIconMap;
    }

    public static int getFileIcon(String name) {
        String suffix = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        if (suffix.length() == 0) {
            return R.drawable.ic_file;
        }

        HashMap<String, Integer> map = getSuffixIconMap();
        Integer i = map.get(suffix);
        if (i != null)
            return i;

        if (suffix.equals("flv")) {
            return R.drawable.file_video;
        }
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        return getResIdforMimetype(mime);
    }

    public static int getFileIconSuffix(String suffix) {
        if (suffix.length() == 0) {
            return R.drawable.ic_file;
        }

        HashMap<String, Integer> map = getSuffixIconMap();
        Integer i = map.get(suffix);
        if (i != null)
            return i;

        if (suffix.equals("flv")) {
            return R.drawable.file_video;
        }
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        return getResIdforMimetype(mime);
    }

    public static String getFileExtension(String name) {
        String suffix = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        if (suffix.length() == 0 || suffix.length() == name.length() || suffix.length() > 4) {
            return "?";
        }
        return "." + suffix;
    }

    public static boolean isViewableImage(String name) {
        String suffix = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        if (suffix.length() == 0)
            return false;
        if (suffix.equals("svg"))
            // don't support svg preview
            return false;

        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if (mime == null)
            return false;
        return mime.contains("image/");
    }

    public static boolean isVideoFile(String name) {
        if (name == null)
            return false;
        String suffix = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        if (TextUtils.isEmpty(suffix))
            return false;
        if (suffix.equals("flv")) {
            return true;
        }
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if (mime == null)
            return false;
        return mime.contains("video/");
    }

    public static boolean isTextFile(File file) {
        if (file != null) {
            String fileName = file.getName();
            if (!TextUtils.isEmpty(fileName)) {
                String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
                if (!TextUtils.isEmpty(suffix)) {
                    String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
                    if (!TextUtils.isEmpty(mime)) {
                        return mime.contains("text/") || FileMimeUtils.isOfficeOrTextFile(mime);
                    }
                }
            }
        }
        return false;
    }

    public static boolean isNetworkOn() {
        ConnectivityManager connMgr = (ConnectivityManager) SeadroidApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!TextUtils.isEmpty(extraInfo)) {
                return true;
            }
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    public static boolean isWiFiOn() {
        ConnectivityManager connMgr = (ConnectivityManager) SeadroidApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi != null && wifi.isAvailable() && wifi.getDetailedState() == DetailedState.CONNECTED) {
            return true;
        }
        return false;
    }
    public static String pathJoin (String first, String... rest) {
        StringBuilder result = new StringBuilder(first);
        for (String b: rest) {
            boolean resultEndsWithSlash = result.toString().endsWith("/");
            boolean bStartWithSlash = b.startsWith("/");
            if (resultEndsWithSlash && bStartWithSlash) {
                result.append(b.substring(1));
            } else if (resultEndsWithSlash || bStartWithSlash) {
                result.append(b);
            } else {
                result.append("/");
                result.append(b);
            }
        }

        return result.toString();
    }

    public static String pathSplit (String first, String rest) {
        String result = "/";
        if (first.endsWith("/")) {
            first = first.substring(0, first.length() - 1);
        }
        if (rest.startsWith("/")) {
            rest = rest.substring(1);
        }
        if (rest.endsWith("/")) {
            rest = rest.substring(0, rest.length() - 1);
        }
        if (first.length() >= rest.length()) {
            result = first.substring(0, first.length() - rest.length());
        }
        return result;
    }

    public static String pathSplit2(String first, String remove) {
        String result = "/";
        if (first.startsWith("/")) {
            first = first.substring(1);
        }
        if (first.endsWith("/")) {
            first = first.substring(0, first.length() - 1);
        }
        if (remove.startsWith("/")) {
            remove = remove.substring(1);
        }
        if (remove.endsWith("/")) {
            remove = remove.substring(0, remove.length() - 1);
        }
        if (first.length() >= remove.length()) {
            result = first.substring(remove.length());
        }
        return result;
    }

    public static String removeFirstPathSeperator(String path) {
        if (TextUtils.isEmpty(path)) return null;

        int size = path.length();
        if (path.startsWith("/")) {
            return path.substring(1, size);
        } else
            return path;
    }

    public static String removeLastPathSeperator(String path) {
        if (TextUtils.isEmpty(path)) return null;

        int size = path.length();
        if (path.endsWith("/")) {
            return path.substring(0, size - 1);
        } else
            return path;
    }
    /**
     * Strip leading and trailing slashes
     */
    public static String stripSlashes(String a) {
        return a.replaceAll("^[/]*|[/]*$", "");
    }

    public static String getCurrentHourMinute() {
        return (String) DateFormat.format("hh:mm", new Date());
    }

    /**
     * Translate commit time to human readable time description
     */
    public static String translateCommitTime(long timestampInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestampInMillis);

        Calendar now = Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();

        String date = simpleDateFormat.format(calendar.getTime());

        Context context = SeadroidApplication.getAppContext();
        String dateString = context.getString(R.string.just_now);

        if (nowMillis > timestampInMillis) {
            long year_diff = now.get(Calendar.YEAR) - calendar.get(Calendar.YEAR);
            long month_diff = now.get(Calendar.MONTH) - calendar.get(Calendar.MONTH) + 12 * year_diff;

            long years = month_diff / 12;
            long months = month_diff % 12;

            long delta = (nowMillis - timestampInMillis) / 1000;

            long secondsPerDay = 24 * 60 * 60;

            long days = delta / secondsPerDay;
            long seconds = delta % secondsPerDay;
            long hours = seconds / 3600;
            long minutes = seconds / 60;

            if (years > 0) {
                dateString =  context.getString(years == 1 ? R.string.year_ago : R.string.years_ago, years);
            } else if (months > 0) {
                if (months == 1) {
                    if (now.get(Calendar.DAY_OF_MONTH) >= calendar.get(Calendar.DAY_OF_MONTH)) {
                        dateString =  context.getString(R.string.month_ago, months);
                    } else {
                        dateString =  context.getString(days == 1 ? R.string.day_ago : R.string.days_ago, days);
                    }
                } else {
                    dateString =  context.getString(R.string.months_ago, months);
                }
            } else if (days > 0) {
                dateString =  context.getString(days == 1 ? R.string.day_ago : R.string.days_ago, days);
            } else if (hours > 0) {
                dateString =  context.getString(hours == 1 ? R.string.hour_ago : R.string.hours_ago, hours);
            } else if (minutes > 0) {
                dateString =  context.getString(minutes == 1 ? R.string.minute_ago : R.string.minutes_ago, minutes);
            } else if (seconds > 0) {
                dateString =  context.getString(seconds == 1 ? R.string.second_ago : R.string.seconds_ago, seconds);
            }
        }
        return dateString + "\n" + date;
    }

    /**
     * Translate create time
     */
    public static String translateTime(long timestampInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestampInMillis);
        String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }

    public static String translateLuckyTime(long timestampInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestampInMillis);
        String date = new SimpleDateFormat("HH:mm:ss - dd.MM.yyyy").format(calendar.getTime());
        return date;
    }

    public static long now() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static String getFileMimeType(String path) {
        String name = fileNameFromPath(path);
        String suffix = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        if (suffix.length() == 0) {
            return MIME_APPLICATION_OCTET_STREAM;
        } else {
            String mime =  MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
            if (mime != null) {
                return mime;
            } else {
                return MIME_APPLICATION_OCTET_STREAM;
            }
        }
    }

    public static String getFileMimeType(File file) {
        return getFileMimeType(file.getPath());
    }

    public static void copyFile(File src, File dst) throws IOException {
        if (src == null || dst == null) {
            return;
        }
        InputStream in = new BufferedInputStream(new FileInputStream(src));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(dst));

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static boolean moveFile(File src, File dst) throws IOException {
        if (src == null || dst == null) {
            return false;
        }
        copyFile(src, dst);
        return src.delete();
    }

    /************ MutiFileChooser ************/
    private static Comparator<SelectableFile> mComparator = new Comparator<SelectableFile>() {
        public int compare(SelectableFile f1, SelectableFile f2) {
            // Sort alphabetically by lower case, which is much cleaner
            return f1.getName().toLowerCase().compareTo(
                    f2.getName().toLowerCase());
        }
    };

    private static FileFilter mFileFilter = new FileFilter() {
        public boolean accept(File file) {
            final String fileName = file.getName();
            // Return files only (not directories) and skip hidden files
            return file.isFile() && !fileName.startsWith(HIDDEN_PREFIX);
        }
    };

    private static FileFilter mDirFilter = new FileFilter() {
        public boolean accept(File file) {
            final String fileName = file.getName();
            // Return directories only and skip hidden directories
            return file.isDirectory() && !fileName.startsWith(HIDDEN_PREFIX);
        }
    };

    public static List<SelectableFile> getFileList(String path, List<File> selectedFile) {
        ArrayList<SelectableFile> list = Lists.newArrayList();

        // Current directory File instance
        final SelectableFile pathDir = new SelectableFile(path);

        // List file in this directory with the directory filter
        final SelectableFile[] dirs = pathDir.listFiles(mDirFilter);
        if (dirs != null) {
            // Sort the folders alphabetically
            Arrays.sort(dirs, mComparator);
            // Add each folder to the File list for the list adapter
            for (SelectableFile dir : dirs) list.add(dir);
        }

        // List file in this directory with the file filter
        final SelectableFile[] files = pathDir.listFiles(mFileFilter);
        if (files != null) {
            // Sort the files alphabetically
            Arrays.sort(files, mComparator);
            // Add each file to the File list for the list adapter
            for (SelectableFile file : files) {
                if (selectedFile != null) {
                    if (selectedFile.contains(file.getFile())) {
                        file.setSelected(true);
                    }
                }
                list.add(file);
            }
        }

        return list;
    }

    public static Intent createGetContentIntent() {
        // Implicitly allow the user to select a particular kind of data
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // The MIME data type filter
        intent.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Allow user to select multiple files
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            // only show local document providers
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        }
        return intent;
    }

    public static Intent createGetImageIntent() {
        // Implicitly allow the user to select a particular kind of data
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // The MIME data type filter
        intent.setType("image/*");
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    public static String getFilenamefromUri(Context context, Uri uri) {

        ContentResolver resolver =context.getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        String displayName = null;
        if (cursor != null && cursor.moveToFirst()) {

            // Note it's called "Display Name".  This is
            // provider-specific, and might not necessarily be the file name.
            displayName = cursor.getString(
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            if (uri.getPath() == null) {
                displayName = "unknown filename";
            } else {
                displayName = uri.getPath().replaceAll(".*/", "");
            }
        } else {
            DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
            if (documentFile == null) {
                displayName = "unknown document filename";
            } else {
                displayName = documentFile.getName();
            }
        };
        return displayName;
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor
                        .getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getStackTrace(Exception e) {
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);
        e.printStackTrace(writer);
        return buffer.toString();
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromStream(InputStream stream,
                                                       int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        // return BitmapFactory.decodeResource(res, resId, options);
        return BitmapFactory.decodeStream(stream, null, options);
    }

    public static String assembleUserName(String name, String email, String server) {
        if (name == null || email == null || server == null)
            return null;

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(server))
            return "";

        // strip port, like :8000 in 192.168.1.116:8000
        if (server.indexOf(":") != -1)
            server = server.substring(0, server.indexOf(':'));
//        String info = String.format("%s (%s)", email, server);//settingFragmeng set account name
        String info = String.format("%s (%s)", name, server);
        info = info.replaceAll("[^\\w\\d\\.@\\(\\) ]", "_");
        return info;
    }

    public static void hideSoftKeyboard(View view) {
        if (view == null)
            return;
        ((InputMethodManager) SeadroidApplication.getAppContext().getSystemService(
                Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                view.getWindowToken(), 0);
    }

    public static String cleanServerURL(String serverURL) throws MalformedURLException {
        if (!serverURL.endsWith("/")) {
            serverURL = serverURL + "/";
        }

        // XXX: android 4.0.3 ~ 4.0.4 can't handle urls with underscore (_) in the host field.
        // See https://github.com/nostra13/Android-Universal-Image-Loader/issues/256 , and
        // https://code.google.com/p/android/issues/detail?id=24924
        //
        new URL(serverURL); // will throw MalformedURLException if serverURL not valid
        return serverURL;
    }

    public static ResolveInfo getWeChatIntent(Intent intent) {
        PackageManager pm = SeadroidApplication.getAppContext().getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo info : infos) {
            if (info.activityInfo.packageName.equals("com.tencent.mm")) {
                return info;
            }
        }

        return null;
    }


    /**
     * use compare user system  is chinese
     */
    public static boolean isInChina() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        String language = locale.getCountry();
        return TextUtils.equals("CN",language)||TextUtils.equals("TW",language);
    }
    public static List<ResolveInfo> getAppsByIntent(Intent intent) {
        PackageManager pm = SeadroidApplication.getAppContext().getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);

        // Remove seafile app from the list
        String seadroidPackageName = SeadroidApplication.getAppContext().getPackageName();
        ResolveInfo info;
        Iterator<ResolveInfo> iter = infos.iterator();
        while (iter.hasNext()) {
            info = iter.next();
            if (info.activityInfo.packageName.equals(seadroidPackageName)) {
                iter.remove();
            }
        }

        return infos;
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isTextMimeType(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        //file is markdown or  txt
        String[] array = {"ac", "am", "bat", "c", "cc", "cmake", "conf", "cpp", "cs", "css", "csv", "diff",
                "el", "go", "groovy", "h", "htm", "html", "java", "js", "json", "less", "log", "make",
                "markdown", "md", "org", "patch", "pde", "php", "pl", "properties", "py", "rb", "rst",
                "sc", "scala", "scd", "schelp", "script", "sh", "sql", "text", "tex", "txt", "vi", "vim",
                "xhtml", "xml", "yml", "adoc"};
        boolean flag = Arrays.asList(array).contains(suffix);
        return flag;
    }

    public static boolean isOfficeMimeType(String fileName) {
        if (isWordMimeType(fileName) || isExcelMimeType(fileName) || isPPTMimeType(fileName)) {
            return true;
        }
        return false;
    }

    public static boolean isWordMimeType(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        String[] array = {"docx", "doc", "odt"};
        boolean flag = Arrays.asList(array).contains(suffix);
        return flag;
    }

    public static boolean isExcelMimeType(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        String[] array = {"xlsx", "xls", "ods"};
        boolean flag = Arrays.asList(array).contains(suffix);
        return flag;
    }

    public static boolean isPPTMimeType(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        String[] array = {"pptx", "ppt", "odp"};
        boolean flag = Arrays.asList(array).contains(suffix);
        return flag;
    }

    public static boolean isPdfMimeType(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        String[] array = {"pdf"};
        boolean flag = Arrays.asList(array).contains(suffix);
        return flag;
    }

    private static long lastClickTime;

    /**
     * check if click event is a fast tapping
     * @return
     */
    public static boolean isFastTapping() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /*
     * SslCertificate class does not has a public getter for the underlying
     * X509Certificate, we can only do this by hack. This only works for andorid 4.0+
     * @see https://groups.google.com/forum/#!topic/android-developers/eAPJ6b7mrmg
     */
    public static X509Certificate getX509CertFromSslCertHack(SslCertificate sslCert) {
        X509Certificate x509Certificate = null;

        Bundle bundle = SslCertificate.saveState(sslCert);
        byte[] bytes = bundle.getByteArray("x509-certificate");

        if (bytes == null) {
            x509Certificate = null;
        } else {
            try {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                Certificate cert = certFactory.generateCertificate(new ByteArrayInputStream(bytes));
                x509Certificate = (X509Certificate) cert;
            } catch (CertificateException e) {
                x509Certificate = null;
            }
        }

        return x509Certificate;
    }

    public static boolean isSameCert(SslCertificate sslCert, X509Certificate x509Cert) {
        if (sslCert == null || x509Cert == null) {
            return false;
        }

        X509Certificate realCert = getX509CertFromSslCertHack(sslCert);
        if (realCert != null) {
            // for android 4.0+
            return realCert.equals(x509Cert);
        } else {
            // for andorid < 4.0
            return SslCertificateComparator.compare(sslCert,
                    new SslCertificate(x509Cert));
        }
    }

    /**
     * Compare SslCertificate objects for android before 4.0
     */
    public static class SslCertificateComparator {
        private SslCertificateComparator() {
        }

        public static boolean compare(SslCertificate cert1, SslCertificate cert2) {
            return isSameDN(cert1.getIssuedTo(), cert2.getIssuedTo())
                    && isSameDN(cert1.getIssuedBy(), cert2.getIssuedBy())
                    && isSameDate(cert1.getValidNotBeforeDate(), cert2.getValidNotBeforeDate())
                    && isSameDate(cert1.getValidNotAfterDate(), cert2.getValidNotAfterDate());
        }

        private static boolean isSameDate(Date date1, Date date2) {
            if (date1 == null && date2 == null) {
                return true;
            } else if (date1 == null || date2 == null) {
                return false;
            }

            return date1.equals(date2);
        }

        private static boolean isSameDN(SslCertificate.DName dName1, SslCertificate.DName dName2) {
            if (dName1 == null && dName2 == null) {
                return true;
            } else if (dName1 == null || dName2 == null) {
                return false;
            }

            return dName1.getDName().equals(dName2.getDName());
        }
    }

    public static int dip2px(Context context, float dip) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static void hideSystemNavigationBar(Activity activity) {
        if (activity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < 19) {
            View view = activity.getWindow().getDecorView();
            view.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public static int getThumbnailWidth() {
        return (int) SeadroidApplication.getAppContext().getResources().getDimension(R.dimen.gallery_icon_show);
    }

    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (TextUtils.isEmpty(ServiceName)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    public static void startCameraSyncJob(Context context) {
        JobScheduler mJobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(context.getPackageName(), MediaSchedulerService.class.getName()));
        builder.setMinimumLatency(5 * 60 * 1000);// Set to execute after at least 5 minutes delay
        builder.setOverrideDeadline(60 * 60 * 1000);// The setting is delayed by 60 minutes,
        builder.setRequiresCharging(false);
        builder.setRequiresDeviceIdle(false);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPersisted(true);
        mJobScheduler.schedule(builder.build());
    }

    public static String getSyncCompletedTime() {
        Date date = new Date(System.currentTimeMillis());
        String completedTime = simpleDateFormat.format(date);
        return completedTime;
    }

    public static String getSyncCompletedTime(long time) {
        Date date = new Date(time);
        String completedTime = simpleDateFormat.format(date);
        return completedTime;
    }

    public static String getUploadStateShow(Context context) {
        String results = null;
        int scanUploadStatus = SeadroidApplication.getInstance().getScanUploadStatus();
        int waitingNumber = SeadroidApplication.getInstance().getWaitingNumber();
        int totalNumber = SeadroidApplication.getInstance().getTotalNumber();
        switch (scanUploadStatus) {
            case CameraSyncStatus.SCANNING:
                results = context.getString(R.string.is_scanning);
                break;
            case CameraSyncStatus.NETWORK_UNAVAILABLE:
                results = context.getString(R.string.network_unavailable);
                break;
            case CameraSyncStatus.UPLOADING:
                results = context.getString(R.string.is_uploading) + " " + (totalNumber - waitingNumber) + " / " + totalNumber;
                break;
            case CameraSyncStatus.SCAN_END:
                results = context.getString(R.string.Upload_completed) + " " + SettingsManager.instance().getUploadCompletedTime();
                break;
            default:
                results = context.getString(R.string.folder_backup_waiting_state);
                break;
        }
        return results;
    }

    public static String toURLEncoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            return "";
        }
        try {
            String str = new String(paramString.getBytes(), "UTF-8");
            str = URLEncoder.encode(str, "UTF-8");
            return str;
        } catch (Exception localException) {
        }
        return "";
    }

    public static String getRealPathFromURI(Context context, Uri contentUri, String media) {
        Cursor cursor = null;
        try {
            if (media.equals("images")) {//image
                String[] proj = {MediaStore.Images.Media.DATA};
                cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } else {//Video
                String[] proj = {MediaStore.Video.Media.DATA};
                cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void logPhoneModelInfo() {
        SeafileLog.d(DEBUG_TAG, "phoneModelInfo-------" + SeafileLog.getDeviceBrand() + "/" + SeafileLog.getSystemModel() + "/" + SeafileLog.getSystemVersion());
    }

    public static void utilsLogInfo(boolean b, String info) {
        if (b) {
            SeafileLog.d(DEBUG_TAG, info);
        } else {
            Log.d(DEBUG_TAG, info);
        }
    }

    public static void utilsBackupLogInfo(String info) {
        SeafileLog.writeBackupLogToFile("d", DEBUG_TAG, info);
    }

    public static void utilsEventsLogInfo(String info) {
        SeafileLog.writeEventsLogToFile("d", DEBUG_TAG, info);
    }

    public static Dialog dialogForActionBar(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();

        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            layoutParams.y = actionBarHeight;
        }

        window.setAttributes(layoutParams);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.transparent)));
        return dialog;
    }

    public static Dialog CustomDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();

        layoutParams.copyFrom(window.getAttributes());
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.CENTER;
        window.setAttributes(layoutParams);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.transparent)));
        return dialog;
    }

    public static ArrayList<SeafPermission> getNormalSeafPermissions(Context context) {
        ArrayList<SeafPermission> normalSeafPermissions = Lists.newArrayList();
        SeafPermission permission1 = new SeafPermission();
        permission1.setId("rw");
        permission1.setName(context.getResources().getString(R.string.permission_rw_name));
        permission1.setDescription(context.getResources().getString(R.string.permission_rw_desc));
        normalSeafPermissions.add(permission1);
        SeafPermission permission2 = new SeafPermission();
        permission2.setId("r");
        permission2.setName(context.getResources().getString(R.string.permission_r_name));
        permission2.setDescription(context.getResources().getString(R.string.permission_r_desc));
        normalSeafPermissions.add(permission2);
        SeafPermission permission3 = new SeafPermission();
        permission3.setId("admin");
        permission3.setName(context.getResources().getString(R.string.permission_admin_name));
        permission3.setDescription(context.getResources().getString(R.string.permission_admin_desc));
        normalSeafPermissions.add(permission3);
        SeafPermission permission4 = new SeafPermission();
        permission4.setId("cloud-edit");
        permission4.setName(context.getResources().getString(R.string.permission_cloud_edit_name));
        permission4.setDescription(context.getResources().getString(R.string.permission_cloud_edit_desc));
        normalSeafPermissions.add(permission4);
        SeafPermission permission5 = new SeafPermission();
        permission5.setId("preview");
        permission5.setName(context.getResources().getString(R.string.permission_preview_name));
        permission5.setDescription(context.getResources().getString(R.string.permission_preview_desc));
        normalSeafPermissions.add(permission5);
        return normalSeafPermissions;
    }

    public static ArrayList<SeafPermission> getCESeafPermissions(Context context) {
        ArrayList<SeafPermission> ceSeafPermissions = Lists.newArrayList();
        SeafPermission permission1 = new SeafPermission();
        permission1.setId("rw");
        permission1.setName(context.getResources().getString(R.string.permission_rw_name));
        permission1.setDescription(context.getResources().getString(R.string.permission_rw_desc));
        ceSeafPermissions.add(permission1);
        SeafPermission permission2 = new SeafPermission();
        permission2.setId("r");
        permission2.setName(context.getResources().getString(R.string.permission_r_name));
        permission2.setDescription(context.getResources().getString(R.string.permission_r_desc));
        ceSeafPermissions.add(permission2);
        return ceSeafPermissions;
    }

    public static String permissionNameToJson(Context context, String name) {
        JSONObject permissionObject = new JSONObject();
        boolean can_download = false;
        boolean can_upload = false;
        boolean can_edit =false;

        if (name.equals(context.getResources().getString(R.string.preview_and_download))) {
            can_download = true;
            can_upload = false;
        }
        if (name.equals(context.getResources().getString(R.string.preview_only))) {
            can_download = false;
            can_upload = false;
        }
        if (name.equals(context.getResources().getString(R.string.download_and_upload))) {
            can_download = true;
            can_upload = true;
        }
        if (name.equals(context.getResources().getString(R.string.edit_on_cloud_and_download))) {
            can_edit = true;
            can_download = true;
            can_upload = true;
        }

        try {
            permissionObject.put("can_edit", can_edit);
            permissionObject.put("can_download", can_download);
            permissionObject.put("can_upload", can_upload);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return permissionObject.toString();
    }

    public static String permissionJsonToName(Context context, String json) {
        String name = "";
        JSONObject permissionObject = Utils.parseJsonObject(json);
        boolean can_edit = false;
        boolean can_download = false;
        boolean can_upload = false;
        try {
            can_edit = permissionObject.getBoolean("can_edit");
            can_download = permissionObject.getBoolean("can_download");
            can_upload = permissionObject.getBoolean("can_upload");
            if (can_edit) {
                if (can_download && !can_upload) {
                    name = context.getResources().getString(R.string.edit_on_cloud_and_download);
                }
            } else {
                if (can_download) {
                    if (can_upload) {
                        name = context.getResources().getString(R.string.download_and_upload);
                    } else {
                        name = context.getResources().getString(R.string.preview_and_download);
                    }
                } else {
                    if (!can_upload) {
                        name = context.getResources().getString(R.string.preview_only);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (name.isEmpty()) {
            name = context.getResources().getString(R.string.preview_and_download);
        }
        return name;
    }

    public static int getPixels(Context context, int valueInDp) {
        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                (float) valueInDp,
                r.getDisplayMetrics()
        );
        return px;
    }

    public static int getPixels(Context context, float valueInDp) {
        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, r.getDisplayMetrics());
        return px;
    }

    public static Bitmap getBitmapFromFile(File file) {
        try {
            String filePath = file.getPath();
            return BitmapFactory.decodeFile(filePath);
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap getResizedBitmap(Bitmap image, int newWidth) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (newWidth < width) {
            return image;
        }
        int newHeight = height / width * newWidth;
        return Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
    }

    public static void setAppearance() {
        if (SettingsManager.instance().isForceDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        if (SettingsManager.instance().isForceLightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        if (!SettingsManager.instance().isForceDarkMode() && !SettingsManager.instance().isForceLightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }



    public static String getEncThumbPath(String path) {
        return Utils.removeExtensionFromFileName(path) + getEncThumbSuffix();
    }

    public static String getEncThumbSuffix() {
        return "-thumb-" + WidgetUtils.getEncThumbWidth() + ".jpg";
    }

    public static void clearPasswordSilently(List<String> exceptionIds) {
        ConcurrentAsyncTask.submit(new Runnable() {
            @Override
            public void run() {
                DataManager.clearPassword(exceptionIds);

                // clear cached data from database
                DatabaseHelper dbHelper = DatabaseHelper.getDatabaseHelper();
                dbHelper.clearEnckeys(exceptionIds);
            }
        });
    }

    public static String generateEncThumbPath(String filePath, String thumbFilePath) {
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      the image's max dimention is gonna be: 512x512
//        float maxSize = 512.0f;
        int thumbnailWidth = WidgetUtils.getEncThumbWidth();
        float maxSize = (actualWidth > thumbnailWidth ? actualWidth : thumbnailWidth) / 4;

//      width and height values are set maintaining the aspect ratio of the image
        float ratio = actualWidth > actualHeight ? maxSize / actualWidth : maxSize / actualHeight;
        actualWidth = (int) (actualWidth * ratio);
        actualHeight = (int) (actualHeight * ratio);

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            return null;
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            return null;
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            return null;
        }

        FileOutputStream out = null;

        try {
            out = new FileOutputStream(thumbFilePath);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            return null;
        }

        return thumbFilePath;

    }

    public static final String EXCEPTION_TYPE_CRASH = "crash_exception";

    public static boolean isManaged(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);

        List<ComponentName> admins = devicePolicyManager.getActiveAdmins();
        if (admins == null) return false;
        for (ComponentName admin : admins) {
            String adminPackageName = admin.getPackageName();
            if (devicePolicyManager.isDeviceOwnerApp(adminPackageName)
                    || devicePolicyManager.isProfileOwnerApp(adminPackageName)) {
                return true;
            }
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static File getVolumeDirectory(StorageVolume volume) {
        try {
            Field f = StorageVolume.class.getDeclaredField("mPath");
            f.setAccessible(true);
            return (File) f.get(volume);
        } catch (Exception e) {
            // This shouldn't fail, as mPath has been there in every version
            throw new RuntimeException(e);
        }
    }

    public static boolean appInstalledOrNot(String uri) {
        PackageManager pm = SeadroidApplication.getAppContext().getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }
}

