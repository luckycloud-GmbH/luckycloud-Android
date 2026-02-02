package com.seafile.seadroid2.ui;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.document.markor.activity.DocumentActivity;
import com.seafile.seadroid2.pdfviewer.PdfViewerActivity;
import com.seafile.seadroid2.ui.activity.BaseActivity;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.dialog.AppChoiceDialog;
import com.seafile.seadroid2.ui.dialog.AppChoiceDialog2;
import com.seafile.seadroid2.ui.dialog.GetShareLinkDialog;
import com.seafile.seadroid2.ui.dialog.GetShareLinkEncryptDialog;
import com.seafile.seadroid2.ui.dialog.TaskDialog;
import com.seafile.seadroid2.util.FileExports;
import com.seafile.seadroid2.util.Utils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

/**
 * Activity Utils
 */
public class WidgetUtils {
    public static final String MIME_ANDROID = "application/vnd.android.package-archive";

    public static void chooseShareApp(final BaseActivity activity,
                                      final String repoID,
                                      final String path,
                                      final boolean isdir,
                                      final Account account,
                                      final String password,
                                      final String days) {
        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // Get a list of apps
        List<ResolveInfo> infos = Utils.getAppsByIntent(shareIntent);

        String title = activity.getString(isdir ? R.string.share_dir_link : R.string.share_file_link);

        AppChoiceDialog dialog = new AppChoiceDialog();
        dialog.addCustomAction(0, activity.getResources().getDrawable(R.drawable.copy_link),
                activity.getString(R.string.copy_link));
        dialog.init(title, infos, new AppChoiceDialog.OnItemSelectedListener() {
            @Override
            public void onCustomActionSelected(AppChoiceDialog.CustomAction action) {
                final GetShareLinkDialog gdialog = new GetShareLinkDialog();
                gdialog.init(repoID, path, isdir, account, password, days);
                gdialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
                    @Override
                    @SuppressWarnings("deprecation")
                    public void onTaskSuccess() {
                        ClipboardManager clipboard = (ClipboardManager)
                                activity.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(gdialog.getLink());
                        // ClipData clip = ClipData.newPlainText("seafile shared link", gdialog.getLink());
                        // clipboard.setPrimaryClip(clip);
                        Toast.makeText(activity, R.string.link_ready_to_be_pasted, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onTaskFailed(SeafException e) {
                        super.onTaskFailed(e);
                        gdialog.dismiss();
                        if (e.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                            Toast.makeText(activity, R.string.share_link_no_permission, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                gdialog.show(activity.getSupportFragmentManager(), "DialogFragment");
            }

            @Override
            public void onAppSelected(ResolveInfo appInfo) {
                String className = appInfo.activityInfo.name;
                String packageName = appInfo.activityInfo.packageName;
                shareIntent.setClassName(packageName, className);

                final GetShareLinkDialog gdialog = new GetShareLinkDialog();
                gdialog.init(repoID, path, isdir, account, password, days);
                gdialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
                    @Override
                    public void onTaskSuccess() {
                        shareIntent.putExtra(Intent.EXTRA_TEXT, gdialog.getLink());
                        activity.startActivity(shareIntent);
                    }

                    @Override
                    public void onTaskFailed(SeafException e) {
                        super.onTaskFailed(e);
                        gdialog.dismiss();
                        if (e.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                            Toast.makeText(activity, R.string.share_link_no_permission, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                gdialog.show(activity.getSupportFragmentManager(), "DialogFragment");
            }

        });
        dialog.show(activity.getSupportFragmentManager(), BrowserActivity.CHOOSE_APP_DIALOG_FRAGMENT_TAG);
    }

    public static void chooseShareApp2(final BaseActivity activity,
                                      final String link,
                                      final String dialogType,
                                      final String fileName) {
        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // Get a list of apps
        List<ResolveInfo> infos = Utils.getAppsByIntent(shareIntent);

        AppChoiceDialog2 dialog = new AppChoiceDialog2();
        dialog.addCustomAction(0, activity.getResources().getDrawable(R.drawable.copy_link),
                activity.getString(R.string.copy_link));
        dialog.init(fileName, infos, new AppChoiceDialog2.OnItemSelectedListener() {
            @Override
            public void onCustomActionSelected(AppChoiceDialog2.CustomAction action) {
                ClipboardManager clipboard = (ClipboardManager)
                        activity.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(link);
                // ClipData clip = ClipData.newPlainText("seafile shared link", gdialog.getLink());
                // clipboard.setPrimaryClip(clip);
                Toast.makeText(activity, R.string.link_ready_to_be_pasted, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAppSelected(ResolveInfo appInfo) {
                String className = appInfo.activityInfo.name;
                String packageName = appInfo.activityInfo.packageName;
                shareIntent.setClassName(packageName, className);

                shareIntent.putExtra(Intent.EXTRA_TEXT, link);
                activity.startActivity(shareIntent);
            }

        });
        dialog.show(activity.getSupportFragmentManager(), BrowserActivity.CHOOSE_APP_DIALOG_FRAGMENT_TAG);
    }

    public static void chooseShareApp3(final BaseActivity activity,
                                       final String link,
                                       final String dialogType,
                                       final String fileName) {
        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/png");

        // Get a list of apps
        List<ResolveInfo> infos = Utils.getAppsByIntent(shareIntent);

        AppChoiceDialog2 dialog = new AppChoiceDialog2();
        dialog.init(fileName, infos, new AppChoiceDialog2.OnItemSelectedListener() {
            @Override
            public void onCustomActionSelected(AppChoiceDialog2.CustomAction action) {
                ClipboardManager clipboard = (ClipboardManager)
                        activity.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(link);
                // ClipData clip = ClipData.newPlainText("seafile shared link", gdialog.getLink());
                // clipboard.setPrimaryClip(clip);
                Toast.makeText(activity, R.string.link_ready_to_be_pasted, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAppSelected(ResolveInfo appInfo) {
                String className = appInfo.activityInfo.name;
                String packageName = appInfo.activityInfo.packageName;
                shareIntent.setClassName(packageName, className);
                shareIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);

                WindowManager manager = (WindowManager) activity.getSystemService(WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int width = point.x;
                int height = point.y;
                int dimen = width < height ? width : height;
                dimen = dimen * 3 / 4;

                QRGEncoder qrgEncoder = new QRGEncoder(link, null, QRGContents.Type.TEXT, dimen);
                qrgEncoder.setColorBlack(activity.getResources().getColor(R.color.dialog_msg_background));
                qrgEncoder.setColorWhite(activity.getResources().getColor(R.color.text_view_color));
                try {
                    // Getting QR-Code as Bitmap
                    Bitmap bitmap = qrgEncoder.getBitmap();
                    // Setting Bitmap to ImageView
                    String pathofBmp = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap,"QRCode", null);
                    Uri bmpUri = Uri.parse(pathofBmp);

                    shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                    activity.startActivity(shareIntent);
                } catch (Exception e) {
                    // Log.v(TAG, e.toString());
                }
            }

        });
        dialog.show(activity.getSupportFragmentManager(), BrowserActivity.CHOOSE_APP_DIALOG_FRAGMENT_TAG);
    }

    public static void inputSharePassword(final BaseActivity activity,
                                          final String repoID,
                                          final String path,
                                          final boolean isdir,
                                          final Account account) {
        final GetShareLinkEncryptDialog dialog = new GetShareLinkEncryptDialog();
        dialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
            @Override
            public void onTaskSuccess() {
                String password = dialog.getPassword();
                String days = dialog.getDays();
                chooseShareApp(activity, repoID, path, isdir, account, password, days);
            }
        });
        dialog.show(activity.getSupportFragmentManager(), BrowserActivity.CHARE_LINK_PASSWORD_FRAGMENT_TAG);

    }

    /**
     * if dir will share dir link .
     * if local file ,will share file to wachat app.
     * if server file , it will download file and share file.
     *
     * @param activity
     * @param account
     * @param repoID
     * @param path
     * @param fileName
     * @param fileSize
     * @param isdir
     */
    public static void ShareWeChat(final BaseActivity activity, Account account, String repoID, String path,
                                   String fileName,
                                   long fileSize,
                                   boolean isdir) {

        if (isdir) {//share  link
            final Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            ResolveInfo weChatInfo = Utils.getWeChatIntent(shareIntent);
            if (weChatInfo == null) {
                activity.showShortToast(activity, R.string.no_app_available);
                return;
            }
            String className = weChatInfo.activityInfo.name;
            String packageName = weChatInfo.activityInfo.packageName;
            shareIntent.setClassName(packageName, className);
            final GetShareLinkDialog gdialog = new GetShareLinkDialog();
            gdialog.init(repoID, path, isdir, account, null, null);
            gdialog.setTaskDialogLisenter(new TaskDialog.TaskDialogListener() {
                @Override
                public void onTaskSuccess() {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, gdialog.getLink());
                    activity.startActivity(shareIntent);
                }

                @Override
                public void onTaskFailed(SeafException e) {
                    super.onTaskFailed(e);
                    gdialog.dismiss();
                    if (e.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                        Toast.makeText(activity, R.string.share_link_no_permission, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            gdialog.show(activity.getSupportFragmentManager(), "DialogFragment");
        } else {//share  files
            BrowserActivity browserActivity = ((BrowserActivity) activity);
            String repoName = ((BrowserActivity) activity).getNavContext().getRepoName();
            String dirPath = ((BrowserActivity) activity).getNavContext().getDirPath();

            String fullPath = Utils.pathJoin(dirPath, fileName);
            final File file = browserActivity.getDataManager().getLocalRepoFile(repoName, repoID, fullPath);
            Uri uri = null;
            if (Build.VERSION.SDK_INT > 23) {
                uri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName(), file);
            } else {
                uri = Uri.fromFile(file);
            }
            final Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType(Utils.getFileMimeType(file));
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            ResolveInfo weChatInfo = Utils.getWeChatIntent(sendIntent);
            if (weChatInfo == null) {
                activity.showShortToast(activity, R.string.no_app_available);
                return;
            }
            String className = weChatInfo.activityInfo.name;
            String packageName = weChatInfo.activityInfo.packageName;
            sendIntent.setClassName(packageName, className);
            if (!Utils.isNetworkOn() && file.exists()) {
                activity.startActivity(sendIntent);
                return;
            }
            browserActivity.fetchFileAndExport(weChatInfo, sendIntent, repoName, repoID, path, fileSize);

        }
    }

    /**
     * display the file according to its file type
     *
     * @param file
     */
    public static void showFile(final BrowserActivity activity, File file, int openValue, boolean isOpenWith) {

        String name = file.getName();
        String suffix = name.substring(name.lastIndexOf('.') + 1).toLowerCase();

        //Open markdown and txt files in MarkdownActivity
        boolean isTextMime = Utils.isTextMimeType(name);
        if (isTextMime && !isOpenWith) {
            startMarkdownActivity(activity, file.getPath());
            activity.overridePendingTransition(0, 0);
            return;
        }

        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if (mime == null && isTextMime) {
            mime = "text/*"; // set .md  .markdown .txt file type//
        } else if (mime == null) {
            mime = "*/*"; // forces app chooser dialog on unknown type//
        }

        if (MIME_ANDROID.equals(mime)) {
//            showFileForAndroid(activity,file,name);
            return;
        }

        if (Utils.isOfficeMimeType(name)) {
            if (openValue == SettingsManager.OPEN_OFFICE_ONLY_OFFICE) {
                final String onlyOfficePackage = "com.onlyoffice.documents";
                if (Utils.appInstalledOrNot(onlyOfficePackage)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage(onlyOfficePackage);
                    intent.setDataAndType(FileProvider.getUriForFile(activity, activity.getPackageName(), file), mime);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    activity.startActivity(intent);
                } else {
                    try {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + onlyOfficePackage)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + onlyOfficePackage)));
                    }
                }
                return;
            }
        }

        if (Utils.isPdfMimeType(file.getAbsolutePath())) {
            if (openValue == SettingsManager.OPEN_PDF_OPTIONAL) {
                activity.showOpenPdfDialog(isOpenWith, file);
                return;
            } else if (openValue == SettingsManager.OPEN_PDF_IN_APP_PDF) {
                Intent intent = new Intent(activity, PdfViewerActivity.class);
                intent.putExtra("filePath", file.getAbsolutePath());
                intent.putExtra("fileName", file.getName());
                activity.startActivity(intent);
                return;
            }
        }

        Intent open = new Intent(Intent.ACTION_VIEW);
        open.addFlags(FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT > 23) {
            Uri photoURI = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName(), file);
            open.setDataAndType(photoURI, mime);
            open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            open.setDataAndType((Uri.fromFile(file)), mime);
        }
        if (Build.VERSION.SDK_INT < 30) {
            if (activity.getPackageManager().resolveActivity(open, 0) == null) {
                String message = String.format(activity.getString(R.string.op_exception_suitable_app_not_found), mime);
                activity.showShortToast(activity, message);
                mime = "*/*";
                open.setType(mime);
            }
        }

        try {
            activity.startActivity(open);
        } catch (ActivityNotFoundException e) {
            activity.showShortToast(activity, activity.getString(R.string.no_app_to_open));
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void showFileForAndroid(final BaseActivity activity, File file, String fileName) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver contentResolver = activity.getContentResolver();
                FileExports.exportFileAndroid10AndAbove(fileName, MIME_ANDROID, contentResolver, file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int nId = new Random(10000).nextInt();
        String channelName = "seadroid-downloader";

        NotificationManager manager = (NotificationManager) activity.getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelName, channelName, NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel(channel);


        Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(activity, nId, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(activity, channelName)
                .setContentTitle(fileName + " " + activity.getString(R.string.download_finished))
                .setContentText(activity.getString(R.string.open))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        manager.notify(nId, notification);
    }

    public static void showRepo(Context context, String repoID, String repoName, String path, String dirID) {
        Intent intent = new Intent(context, BrowserActivity.class);
        intent.putExtra("repoID", repoID);
        intent.putExtra("repoName", repoName);
        intent.putExtra("path", path);
        intent.putExtra("dirID", dirID);
        context.startActivity(intent);
    }

    public static void showStarredRepo(BrowserActivity activity, String repoID, String repoName, String path, String permission) {
        activity.getReposFragment().openDir(repoID, repoName, path, permission);
        activity.setCurrentPosition(BrowserActivity.INDEX_LIBRARY_TAB);
    }

    public static void startMarkdownActivity(BrowserActivity context, String path) {
//        Intent intent = new Intent(context, MarkdownActivity.class);
//        intent.putExtra("path", path);
//        context.startActivity(intent);
        DocumentActivity.launch(context, new File(path), true, null);
    }

//    /**
//     * start and pass data to {@link GalleryDialog}
//     *
//     * @param activity
//     * @param repoName
//     * @param repoId
//     * @param path
//     * @param fileName
//     */
//    public static void startGalleryDialog(BrowserActivity activity, String repoName, String repoId, String path, String fileName) {
//        GalleryDialog dialog = new GalleryDialog();
//        dialog.init(activity, repoId, repoName, path, fileName);
//        dialog.show(activity.getSupportFragmentManager(), BrowserActivity.GALLERY_DIALOG_FRAGMENT_TAG);
//    }

    public static int getEncThumbWidth() {
        int thumbnailWidth = (int) SeadroidApplication.getAppContext().getResources().getDimension(R.dimen.lv_icon_width);
        return thumbnailWidth * 4;
    }

    public static int getThumbnailWidth(int columns) {
        int thumbnailWidth = (int) SeadroidApplication.getAppContext().getResources().getDimension(R.dimen.lv_icon_width);
        switch (columns) {
            case SettingsManager.GRID_BY_SMALL_TILE:
                thumbnailWidth = thumbnailWidth * 4;
                break;
            case SettingsManager.GRID_BY_BIG_TILE:
                thumbnailWidth = thumbnailWidth * 8;
                break;
            default:
                break;
        }
        return thumbnailWidth;
    }
}
