package com.seafile.seadroid2.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SeafileLog {

    private static Boolean MYLOG_SWITCH = true; // Main switch
    private static Boolean MYLOG_WRITE_TO_FILE = true;// log switch
    private static char MYLOG_TYPE = 'v';
    private static int SDCARD_LOG_FILE_SAVE_DAYS = 0;
    public static String MY_LOG_FILE_NAME = SeadroidApplication.getAppContext().getString(R.string.app_name) + " - Log.txt";
    public static String MY_BACKUP_LOG_FILE_NAME = SeadroidApplication.getAppContext().getString(R.string.app_name) + " - backup - Log.txt";
    public static String MY_EVENTS_LOG_FILE_NAME = SeadroidApplication.getAppContext().getString(R.string.app_name) + " - events - Log.txt";
    private static SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// The output format of the log
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// Log file format
    public Context context;

    public static void w(String tag, Object msg) { // Warning message
        log(tag, msg.toString(), 'w');
    }

    public static void e(String tag, Object msg) { // The error message
        log(tag, msg.toString(), 'e');
    }

    public static void d(String tag, Object msg) {// Debugging information
        log(tag, msg.toString(), 'd');
    }

    public static void i(String tag, Object msg) {//
        log(tag, msg.toString(), 'i');
    }

    public static void v(String tag, Object msg) {
        log(tag, msg.toString(), 'v');
    }

    public static void w(String tag, String text) {
        log(tag, text, 'w');
    }

    public static void e(String tag, String text) {
        log(tag, text, 'e');
    }

    public static void d(String tag, String text) {
        log(tag, text, 'd');
    }

    public static void i(String tag, String text) {
        log(tag, text, 'i');
    }

    public static void v(String tag, String text) {
        log(tag, text, 'v');
    }

    private static void log(String tag, String msg, char level) {
        if (MYLOG_SWITCH) {//Log file master switch
            if ('e' == level && ('e' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
                Log.e(tag, msg);
            } else if ('w' == level && ('w' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
                Log.w(tag, msg);
            } else if ('d' == level && ('d' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
                Log.d(tag, msg);
            } else if ('i' == level && ('d' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
                Log.i(tag, msg);
            } else {
                Log.v(tag, msg);
            }
            if (MYLOG_WRITE_TO_FILE)//Log write file switch
                writeLogtoFile(String.valueOf(level), tag, msg);
        }
    }

    /**
     * Open the log file and write to the log
     *
     * @param mylogtype
     * @param tag
     * @param text
     */
    private static void writeLogtoFile(String mylogtype, String tag, String text) {
        if (text.contains(MY_LOG_FILE_NAME) && text.contains(MY_BACKUP_LOG_FILE_NAME) && text.contains(MY_EVENTS_LOG_FILE_NAME))
            return;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Date nowtime = new Date();
//                String needWriteFile = logfile.format(nowtime);
                String needWriteMessage = "==============================\n" + myLogSdf.format(nowtime) + "    " + mylogtype + "    " + tag + "\n" + text;
//                File dirsFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Seafile/");
//                String rootPath = SeadroidApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
                File dirsFile = new File(getLogDirPath());
                if (!dirsFile.exists()) {
                    dirsFile.mkdirs();
                }
                File file = new File(dirsFile.toString(), MY_LOG_FILE_NAME);// MYLOG_PATH_SDCARD_DIR
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (Exception e) {
                    }
                }

                try {
                    FileWriter filerWriter = new FileWriter(file, true);
                    BufferedWriter bufWriter = new BufferedWriter(filerWriter);
                    bufWriter.write(needWriteMessage);
                    bufWriter.newLine();
                    bufWriter.close();
                    filerWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public static void writeBackupLogToFile(String myLogType, String tag, String text) {
        if (text.contains(MY_LOG_FILE_NAME) && text.contains(MY_BACKUP_LOG_FILE_NAME) && text.contains(MY_EVENTS_LOG_FILE_NAME))
            return;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String needWriteMessage = "==============================\n" + myLogSdf.format(new Date()) + "    " + myLogType + "    " + tag + "\n" + text;
                File dirsFile = new File(getLogDirPath());
                if (!dirsFile.exists()) {
                    dirsFile.mkdirs();
                }
                File file = new File(dirsFile.toString(), MY_BACKUP_LOG_FILE_NAME);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (Exception e) {
                    }
                }

                try {
                    FileWriter filerWriter = new FileWriter(file, true);
                    BufferedWriter bufWriter = new BufferedWriter(filerWriter);
                    bufWriter.write(needWriteMessage);
                    bufWriter.newLine();
                    bufWriter.close();
                    filerWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public static void writeEventsLogToFile(String myLogType, String tag, String text) {
        if (text.contains(MY_LOG_FILE_NAME) && text.contains(MY_BACKUP_LOG_FILE_NAME) && text.contains(MY_EVENTS_LOG_FILE_NAME))
            return;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String needWriteMessage = "==============================\n" + myLogSdf.format(new Date()) + "    " + myLogType + "    " + tag + "\n" + text;
                File dirsFile = new File(getLogDirPath());
                if (!dirsFile.exists()) {
                    dirsFile.mkdirs();
                }
                File file = new File(dirsFile.toString(), MY_EVENTS_LOG_FILE_NAME);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (Exception e) {
                    }
                }

                try {
                    FileWriter filerWriter = new FileWriter(file, true);
                    BufferedWriter bufWriter = new BufferedWriter(filerWriter);
                    bufWriter.write(needWriteMessage);
                    bufWriter.newLine();
                    bufWriter.close();
                    filerWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public static String getLogDirPath() {
        File[] externalMediaDirs = SeadroidApplication.getAppContext().getExternalMediaDirs();
        String rootPath = externalMediaDirs[0].getAbsolutePath();
        return rootPath + "/" + SeadroidApplication.getAppContext().getString(R.string.app_name) + "/";
    }

    /**
     * Delete the specified log file
     */
    public static void delFile() {
        String needDelFile = logfile.format(getDateBefore());
        File dirPath = new File(getLogDirPath());
        File file = new File(dirPath, needDelFile + " - " + MY_LOG_FILE_NAME);// MYLOG_PATH_SDCARD_DIR
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Use to get the file name of the log to delete
     */
    private static Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }

    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

}
