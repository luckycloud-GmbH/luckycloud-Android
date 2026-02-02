package com.seafile.seadroid2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.transfer.DownloadTaskInfo;
import com.seafile.seadroid2.transfer.TaskState;
import com.seafile.seadroid2.transfer.UploadTaskInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DEBUG_TAG = "DatabaseHelper";
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 10;
    public static final String DATABASE_NAME = "data.db";

    // FileCache table
    private static final String FILECACHE_TABLE_NAME = "FileCache";

    private static final String FILECACHE_COLUMN_ID = "id";
    private static final String FILECACHE_COLUMN_FILEID = "fileid";
    private static final String FILECACHE_COLUMN_REPO_NAME = "repo_name";
    private static final String FILECACHE_COLUMN_REPO_ID = "repo_id";
    private static final String FILECACHE_COLUMN_PATH = "path";
    private static final String FILECACHE_COLUMN_ACCOUNT = "account";

    private static final String STARRED_FILECACHE_TABLE_NAME = "StarredFileCache";

    private static final String STARRED_FILECACHE_COLUMN_ID = "id";
    private static final String STARRED_FILECACHE_COLUMN_ACCOUNT = "account";
    private static final String STARRED_FILECACHE_COLUMN_CONTENT = "content";

    /** Table to lookup the mapping from repository to local cache directory.
     * As there can be multiple repositories with the same name (even on the same server)
     * this mapping has to be remembered.
     */
    private static final String REPODIR_TABLE_NAME = "RepoDir";

    private static final String REPODIR_COLUMN_ID = "id";
    /** Signature of the associated account, E.g. "seacloud.cc (user@example.com)" */
    private static final String REPODIR_COLUMN_ACCOUNT = "account";
    /** Repository ID: E.g.: 41deb3fc-192a-4387-8aa1-2020e0727283 */
    private static final String REPODIR_COLUMN_REPO_ID = "repo_id";
    /** Local directory used for cached files, relative to Account cache directory.
     *  E.g.: "Temp Repository (1)" */
    private static final String REPODIR_COLUMN_REPO_DIR = "repo_dir";

    private static final String DIRENTS_CACHE_TABLE_NAME = "DirentsCache";

    private static final String DIRENTS_CACHE_COLUMN_ID = "id";
    private static final String DIRENTS_CACHE_COLUMN_REPO_ID = "repo_id";
    private static final String DIRENTS_CACHE_COLUMN_PATH = "path";
    private static final String DIRENTS_CACHE_COLUMN_DIR_ID = "dir_id";

    public static final String ENCKEY_TABLE_NAME = "EncKey";

    public static final String ENCKEY_COLUMN_ID = "id";
    public static final String ENCKEY_COLUMN_ENCKEY = "enc_key";
    public static final String ENCKEY_COLUMN_ENCIV = "enc_iv";
    public static final String ENCKEY_COLUMN_REPO_ID = "repo_id";

    public static final String DOWNLOAD_TABLE_NAME = "download";

    public static final String DOWNLOAD_COLUMN_ID = "id";
    private static final String DOWNLOAD_COLUMN_ACCOUNT = "account";
    public static final String DOWNLOAD_COLUMN_TASK_ID = "task_id";
    public static final String DOWNLOAD_COLUMN_STATE = "state";
    public static final String DOWNLOAD_COLUMN_REPO_ID = "repo_id";
    public static final String DOWNLOAD_COLUMN_REPO_NAME = "repo_name";
    public static final String DOWNLOAD_COLUMN_PATH_IN_REPO = "path_in_repo";
    public static final String DOWNLOAD_COLUMN_LOCAL_FILE_PATH = "local_file_path";
    public static final String DOWNLOAD_COLUMN_FILE_SIZE = "file_size";
    public static final String DOWNLOAD_COLUMN_FINISHED = "finished";
    public static final String DOWNLOAD_COLUMN_START_TIME = "start_time";
    public static final String DOWNLOAD_COLUMN_THUMBNAIL = "thumbnail";
    public static final String DOWNLOAD_COLUMN_ERR = "err";

    public static final String UPLOAD_TABLE_NAME = "Upload";

    public static final String UPLOAD_COLUMN_ID = "id";
    private static final String UPLOAD_COLUMN_ACCOUNT = "account";
    public static final String UPLOAD_COLUMN_TASK_ID = "task_id";
    public static final String UPLOAD_COLUMN_STATE = "state";
    public static final String UPLOAD_COLUMN_REPO_ID = "repo_id";
    public static final String UPLOAD_COLUMN_REPO_NAME = "repo_name";
    public static final String UPLOAD_COLUMN_PARENT_DIR = "parent_dir";
    public static final String UPLOAD_COLUMN_LOCAL_FILE_PATH = "local_file_path";
    public static final String UPLOAD_COLUMN_IS_UPDATE = "is_update";
    public static final String UPLOAD_COLUMN_IS_COPY_TO_LOCAL = "is_copy_to_local";
    public static final String UPLOAD_COLUMN_UPLOAD_SIZE = "uploaded_size";
    public static final String UPLOAD_COLUMN_TOTAL_SIZE = "total_size";
    public static final String UPLOAD_COLUMN_START_TIME = "start_time";
    public static final String UPLOAD_COLUMN_SOURCE = "source";
    public static final String UPLOAD_COLUMN_ERR = "err";

    private static final String SQL_CREATE_FILECACHE_TABLE =
        "CREATE TABLE " + FILECACHE_TABLE_NAME + " ("
        + FILECACHE_COLUMN_ID + " INTEGER PRIMARY KEY, "
        + FILECACHE_COLUMN_FILEID + " TEXT NOT NULL, "
        + FILECACHE_COLUMN_PATH + " TEXT NOT NULL, "
        + FILECACHE_COLUMN_REPO_NAME + " TEXT NOT NULL, "
        + FILECACHE_COLUMN_REPO_ID + " TEXT NOT NULL, "
        + FILECACHE_COLUMN_ACCOUNT + " TEXT NOT NULL);";

    private static final String SQL_CREATE_STARRED_FILECACHE_TABLE =
            "CREATE TABLE " + STARRED_FILECACHE_TABLE_NAME + " ("
                    + STARRED_FILECACHE_COLUMN_ID + " INTEGER PRIMARY KEY, "
                    + STARRED_FILECACHE_COLUMN_ACCOUNT + " TEXT NOT NULL, "
                    + STARRED_FILECACHE_COLUMN_CONTENT + " TEXT NOT NULL);";

    private static final String SQL_CREATE_REPODIR_TABLE =
        "CREATE TABLE " + REPODIR_TABLE_NAME + " ("
        + REPODIR_COLUMN_ID + " INTEGER PRIMARY KEY, "
        + REPODIR_COLUMN_ACCOUNT + " TEXT NOT NULL, "
        + REPODIR_COLUMN_REPO_ID + " TEXT NOT NULL, "
        + REPODIR_COLUMN_REPO_DIR + " TEXT NOT NULL);";

    private static final String SQL_CREATE_DIRENTS_CACHE_TABLE =
        "CREATE TABLE " + DIRENTS_CACHE_TABLE_NAME + " ("
        + DIRENTS_CACHE_COLUMN_ID + " INTEGER PRIMARY KEY, "
        + DIRENTS_CACHE_COLUMN_REPO_ID + " TEXT NOT NULL, "
        + DIRENTS_CACHE_COLUMN_PATH + " TEXT NOT NULL, "
        + DIRENTS_CACHE_COLUMN_DIR_ID + " TEXT NOT NULL);";

    private static final String SQL_CREATE_ENCKEY_TABLE =
            "CREATE TABLE " + ENCKEY_TABLE_NAME + " ("
                    + ENCKEY_COLUMN_ID + " INTEGER PRIMARY KEY, "
                    + ENCKEY_COLUMN_ENCKEY + " TEXT NOT NULL, "
                    + ENCKEY_COLUMN_ENCIV + " TEXT NOT NULL, "
                    + ENCKEY_COLUMN_REPO_ID + " TEXT NOT NULL);";

    private static final String SQL_CREATE_DOWNLOAD_TABLE =
            "CREATE TABLE " + DOWNLOAD_TABLE_NAME + " ("
                    + DOWNLOAD_COLUMN_ID + " INTEGER PRIMARY KEY, "
                    + DOWNLOAD_COLUMN_ACCOUNT + " TEXT NOT NULL, "
                    + DOWNLOAD_COLUMN_TASK_ID + " INTEGER NOT NULL, "
                    + DOWNLOAD_COLUMN_STATE + " TEXT NOT NULL, "
                    + DOWNLOAD_COLUMN_REPO_ID + " TEXT NOT NULL, "
                    + DOWNLOAD_COLUMN_REPO_NAME + " TEXT NOT NULL, "
                    + DOWNLOAD_COLUMN_PATH_IN_REPO + " TEXT NOT NULL, "
                    + DOWNLOAD_COLUMN_LOCAL_FILE_PATH + " TEXT NOT NULL, "
                    + DOWNLOAD_COLUMN_FILE_SIZE + " INTEGER NOT NULL, "
                    + DOWNLOAD_COLUMN_FINISHED + " INTEGER NOT NULL, "
                    + DOWNLOAD_COLUMN_START_TIME + " INTEGER NOT NULL, "
                    + DOWNLOAD_COLUMN_THUMBNAIL + " INTEGER NOT NULL DEFAULT 0, "
                    + DOWNLOAD_COLUMN_ERR + " TEXT NOT NULL);";
    private static final String SQL_CREATE_UPLOAD_TABLE =
            "CREATE TABLE " + UPLOAD_TABLE_NAME + " ("
                    + UPLOAD_COLUMN_ID + " INTEGER PRIMARY KEY, "
                    + UPLOAD_COLUMN_ACCOUNT + " TEXT NOT NULL, "
                    + UPLOAD_COLUMN_TASK_ID + " INTEGER NOT NULL, "
                    + UPLOAD_COLUMN_STATE + " TEXT NOT NULL, "
                    + UPLOAD_COLUMN_REPO_ID + " TEXT NOT NULL, "
                    + UPLOAD_COLUMN_REPO_NAME + " TEXT NOT NULL, "
                    + UPLOAD_COLUMN_PARENT_DIR + " TEXT NOT NULL, "
                    + UPLOAD_COLUMN_LOCAL_FILE_PATH + " TEXT NOT NULL, "
                    + UPLOAD_COLUMN_IS_UPDATE + " INTEGER NOT NULL DEFAULT 0, "
                    + UPLOAD_COLUMN_IS_COPY_TO_LOCAL + " INTEGER NOT NULL DEFAULT 0, "
                    + UPLOAD_COLUMN_UPLOAD_SIZE + " INTEGER NOT NULL, "
                    + UPLOAD_COLUMN_TOTAL_SIZE + " INTEGER NOT NULL, "
                    + UPLOAD_COLUMN_START_TIME + " INTEGER NOT NULL, "
                    + UPLOAD_COLUMN_SOURCE + " TEXT NOT NULL, "
                    + UPLOAD_COLUMN_ERR + " TEXT NOT NULL);";

    // Use only single dbHelper to prevent multi-thread issue and db is closed exception
    // Reference http://stackoverflow.com/questions/2493331/what-are-the-best-practices-for-sqlite-on-android
    private static DatabaseHelper dbHelper = null;
    private SQLiteDatabase database = null;

    public static synchronized DatabaseHelper getDatabaseHelper() {
        if (dbHelper != null)
            return dbHelper;
        dbHelper = new DatabaseHelper(SeadroidApplication.getAppContext());
        dbHelper.database = dbHelper.getWritableDatabase();
        return dbHelper;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createFileCacheTable(db);
        createRepoDirTable(db);
        createDirentsCacheTable(db);
        createStarredFilesCacheTable(db);
        createEnckeyTable(db);
        createDownloadTable(db);
        createUploadTable(db);
    }

    private void createFileCacheTable(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FILECACHE_TABLE);
        db.execSQL("CREATE INDEX fileid_index ON " + FILECACHE_TABLE_NAME
                + " (" + FILECACHE_COLUMN_FILEID + ");");
        db.execSQL("CREATE INDEX repoid_index ON " + FILECACHE_TABLE_NAME
                + " (" + FILECACHE_COLUMN_REPO_ID + ");");
        db.execSQL("CREATE INDEX account_index ON " + FILECACHE_TABLE_NAME
                + " (" + FILECACHE_COLUMN_ACCOUNT + ");");
    }
    
    private void createRepoDirTable(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_REPODIR_TABLE);

        // index for getRepoDir()
        String sql;
        sql = String.format("CREATE UNIQUE INDEX account_repoid_index ON %s (%s, %s)",
                            REPODIR_TABLE_NAME,
                            REPODIR_COLUMN_ACCOUNT,
                            REPODIR_COLUMN_REPO_ID);
        db.execSQL(sql);

        // index for repoDirExists()
        sql = String.format("CREATE UNIQUE INDEX account_dir_index ON %s (%s, %s)",
                            REPODIR_TABLE_NAME,
                            REPODIR_COLUMN_ACCOUNT,
                            REPODIR_COLUMN_REPO_DIR);
        db.execSQL(sql);
    }

    private void createDirentsCacheTable(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DIRENTS_CACHE_TABLE);

        String sql;
        sql = String.format("CREATE INDEX repo_path_index ON %s (%s, %s)",
                            DIRENTS_CACHE_TABLE_NAME,
                            DIRENTS_CACHE_COLUMN_REPO_ID,
                            DIRENTS_CACHE_COLUMN_PATH);
        db.execSQL(sql);
    }

    private void createStarredFilesCacheTable(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_STARRED_FILECACHE_TABLE);

        String sql;
        sql = String.format("CREATE INDEX account_content_index ON %s (%s, %s)",
                STARRED_FILECACHE_TABLE_NAME,
                STARRED_FILECACHE_COLUMN_ACCOUNT,
                STARRED_FILECACHE_COLUMN_CONTENT);
        db.execSQL(sql);
    }

    private void createEnckeyTable(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENCKEY_TABLE);

        String sql;
        sql = String.format("CREATE INDEX enckey_repo_index ON %s (%s, %s)",
                ENCKEY_TABLE_NAME,
                ENCKEY_COLUMN_ENCKEY,
                ENCKEY_COLUMN_REPO_ID);
        db.execSQL(sql);
    }

    private void createDownloadTable(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DOWNLOAD_TABLE);
    }

    private void createUploadTable(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_UPLOAD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over

        File dir = StorageManager.getInstance().getJsonCacheDir();
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                f.delete();
            }
        }

        db.execSQL("DROP TABLE IF EXISTS " + FILECACHE_TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + REPODIR_TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + DIRENTS_CACHE_TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + STARRED_FILECACHE_TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + ENCKEY_TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + DOWNLOAD_TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + UPLOAD_TABLE_NAME + ";");
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public SeafCachedFile getFileCacheItem(String repoID,
                                           String path, DataManager dataManager) {
        String[] projection = {
                FILECACHE_COLUMN_ID,
                FILECACHE_COLUMN_FILEID,
                FILECACHE_COLUMN_REPO_NAME,
                FILECACHE_COLUMN_REPO_ID,
                FILECACHE_COLUMN_PATH,
                FILECACHE_COLUMN_ACCOUNT
        };

        Cursor c = database.query(
             FILECACHE_TABLE_NAME,
             projection,
             FILECACHE_COLUMN_REPO_ID
             + "=? and " + FILECACHE_COLUMN_PATH + "=?",
             new String[] { repoID, path },
             null,   // don't group the rows
             null,   // don't filter by row groups
             null    // The sort order
        );

        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        SeafCachedFile item = cursorToFileCacheItem(c, dataManager);
        c.close();
        return item;
    }
    
    // XXX: Here we can use SQLite3  "INSERT OR REPLACE" for convience
    public void saveFileCacheItem(SeafCachedFile item, DataManager dataManager) {
        SeafCachedFile old = getFileCacheItem(item.repoID, item.path, dataManager);
        if (old != null) {
            deleteFileCacheItem(old);
        }

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FILECACHE_COLUMN_FILEID, item.fileID);
        values.put(FILECACHE_COLUMN_REPO_NAME, item.repoName);
        values.put(FILECACHE_COLUMN_REPO_ID, item.repoID);
        values.put(FILECACHE_COLUMN_PATH, item.path);
        values.put(FILECACHE_COLUMN_ACCOUNT, item.accountSignature);

        // Insert the new row, returning the primary key value of the new row
        database.insert(FILECACHE_TABLE_NAME, null, values);
    }
    
    public void deleteFileCacheItem(SeafCachedFile item) {
        if (item.id != -1) {
            database.delete(FILECACHE_TABLE_NAME,  FILECACHE_COLUMN_ID + "=?",
                    new String[] { String.valueOf(item.id) });
        } else
            database.delete(FILECACHE_TABLE_NAME,  FILECACHE_COLUMN_REPO_ID + "=? and " + FILECACHE_COLUMN_PATH + "=?",
                new String[] { item.repoID, item.path });
    }

    public void delCaches() {
        database.delete(REPODIR_TABLE_NAME, null, null);
        database.delete(FILECACHE_TABLE_NAME, null, null);
        database.delete(DIRENTS_CACHE_TABLE_NAME, null, null);
        database.delete(STARRED_FILECACHE_TABLE_NAME, null, null);
        database.delete(DOWNLOAD_TABLE_NAME, null, null);
        database.delete(UPLOAD_TABLE_NAME, null, null);
    }

    public List<SeafCachedFile> getFileCacheItems(DataManager dataManager) {
        List<SeafCachedFile> files = Lists.newArrayList();

        String[] projection = {
                FILECACHE_COLUMN_ID,
                FILECACHE_COLUMN_FILEID,
                FILECACHE_COLUMN_REPO_NAME,
                FILECACHE_COLUMN_REPO_ID,
                FILECACHE_COLUMN_PATH,
                FILECACHE_COLUMN_ACCOUNT
        };

        Cursor c = database.query(
             FILECACHE_TABLE_NAME,
             projection,
             FILECACHE_COLUMN_ACCOUNT + "=?",
             new String[] { dataManager.getAccount().getSignature() },
             null,   // don't group the rows
             null,   // don't filter by row groups
             null    // The sort order
        );

        c.moveToFirst();
        while (!c.isAfterLast()) {
            SeafCachedFile item = cursorToFileCacheItem(c, dataManager);
            files.add(item);
            c.moveToNext();
        }

        c.close();
        return files;
    }

    private SeafCachedFile cursorToFileCacheItem(Cursor cursor, DataManager dataManager) {
        SeafCachedFile item = new SeafCachedFile();
        item.id = cursor.getInt(0);
        item.fileID = cursor.getString(1);
        item.repoName = cursor.getString(2);
        item.repoID = cursor.getString(3);
        item.path = cursor.getString(4);
        item.accountSignature = cursor.getString(5);
        item.file = dataManager.getLocalRepoFile(item.repoName, item.repoID, item.path);
        return item;
    }

    /**
     * Return the directory of a repo on external storage.
     */
    public String getRepoDir(Account account, String repoID) {
        String[] projection = {
            REPODIR_COLUMN_REPO_DIR
        };

        String selectClause = String.format("%s = ? and %s = ?",
                                            REPODIR_COLUMN_ACCOUNT,
                                            REPODIR_COLUMN_REPO_ID);

        String[] selectArgs = { account.getSignature(), repoID };


        Cursor cursor = database.query(
            REPODIR_TABLE_NAME,
            projection,
            selectClause,
            selectArgs,
            null,   // don't group the rows
            null,   // don't filter by row groups
            null);  // The sort order

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        String dir = cursor.getString(0);
        cursor.close();

        return dir;
    }

    public String getCachedStarredFiles(Account account) {
        String[] projection = {
                STARRED_FILECACHE_COLUMN_CONTENT
        };

        String selectClause = String.format("%s = ?",
                STARRED_FILECACHE_COLUMN_ACCOUNT);

        String[] selectArgs = { account.getSignature() };


        Cursor cursor = database.query(
                STARRED_FILECACHE_TABLE_NAME,
                projection,
                selectClause,
                selectArgs,
                null,   // don't group the rows
                null,   // don't filter by row groups
                null);  // The sort order

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        String dir = cursor.getString(0);
        cursor.close();

        return dir;
    }

    /**
     * Tell if a record exists already.
     */
    public boolean repoDirExists(Account account, String dir) {
        String[] projection = {
            REPODIR_COLUMN_REPO_DIR
        };

        String selectClause = String.format("%s = ? and %s = ?",
                                            REPODIR_COLUMN_ACCOUNT,
                                            REPODIR_COLUMN_REPO_DIR);
        String[] selectArgs = { account.getSignature(), dir };

        Cursor cursor = database.query(
            REPODIR_TABLE_NAME,
            projection,
            selectClause,
            selectArgs,
            null,   // don't group the rows
            null,   // don't filter by row groups
            null);  // The sort order

        boolean exist = true;
        if (!cursor.moveToFirst()) {
            exist = false;
        }
        cursor.close();

        return exist;
    }

    public void saveRepoDirMapping(Account account,
                                   String repoID, String dir) {
        String log = String.format("Saving repo dir mapping: account = %s(%s) "
                        + "repoID = %s"
                        + "dir = %s",
                account.getEmail(), account.getServerNoProtocol(),
                repoID, dir);

        Log.d(DEBUG_TAG, log);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(REPODIR_COLUMN_ACCOUNT, account.getSignature());
        values.put(REPODIR_COLUMN_REPO_ID, repoID);
        values.put(REPODIR_COLUMN_REPO_DIR, dir);

        database.insert(REPODIR_TABLE_NAME, null, values);
    }

    public void saveCachedStarredFiles(Account account, String content) {
        removeStarredFiles(account);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(STARRED_FILECACHE_COLUMN_ACCOUNT, account.getSignature());
        values.put(STARRED_FILECACHE_COLUMN_CONTENT, content);

        database.insert(STARRED_FILECACHE_TABLE_NAME, null, values);
    }

    public void saveDirents(String repoID, String path, String dirID) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DIRENTS_CACHE_COLUMN_REPO_ID, repoID);
        values.put(DIRENTS_CACHE_COLUMN_PATH, path);
        values.put(DIRENTS_CACHE_COLUMN_DIR_ID, dirID);

        // Insert the new row, returning the primary key value of the new row
        database.insert(DIRENTS_CACHE_TABLE_NAME, null, values);
    }

    public void removeCachedDirents(String repoID, String path) {
        String whereClause = String.format("%s = ? and %s = ?",
            DIRENTS_CACHE_COLUMN_REPO_ID, DIRENTS_CACHE_COLUMN_PATH);

        database.delete(DIRENTS_CACHE_TABLE_NAME, whereClause, new String[] { repoID, path });
    }

    private void removeStarredFiles(Account account) {
        String whereClause = String.format("%s = ?",
                STARRED_FILECACHE_COLUMN_ACCOUNT);

        database.delete(STARRED_FILECACHE_TABLE_NAME, whereClause, new String[] { account.getSignature() });
    }

    public String getCachedDirents(String repoID, String path) {
        String[] projection = {
            DIRENTS_CACHE_COLUMN_DIR_ID
        };

        String selectClause = String.format("%s = ? and %s = ?",
                                            DIRENTS_CACHE_COLUMN_REPO_ID,
                                            DIRENTS_CACHE_COLUMN_PATH);

        String[] selectArgs = { repoID, path };

        Cursor cursor = database.query(
            DIRENTS_CACHE_TABLE_NAME,
            projection,
            selectClause,
            selectArgs,
            null,   // don't group the rows
            null,   // don't filter by row groups
            null);  // The sort order

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        String dirID = cursor.getString(0);
        cursor.close();

        return dirID;
    }

    /**
     * Return the number of cached dirs that reference a specific dirID.
     * Used for cache cleaning.
     *
     * @param dirID
     * @return
     */
    public int getCachedDirentUsage(String dirID) {
        String[] projection = { DIRENTS_CACHE_COLUMN_DIR_ID };

        String selectClause = String.format("%s = ?",
                DIRENTS_CACHE_COLUMN_DIR_ID);

        String[] selectArgs = { dirID };

        Cursor cursor = database.query(
                DIRENTS_CACHE_TABLE_NAME,
                projection,
                selectClause,
                selectArgs,
                null,   // don't group the rows
                null,   // don't filter by row groups
                null);  // The sort order

        if (!cursor.moveToFirst()) {
            cursor.close();
            return 0;
        }

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public Pair<String, String> getEnckey(@NonNull String repoId) {
        String[] projection = {
                ENCKEY_COLUMN_ENCKEY,
                ENCKEY_COLUMN_ENCIV
        };

        String selectClause = String.format("%s = ?",
                ENCKEY_COLUMN_REPO_ID);

        String [] selectArgs = { repoId };

        Cursor cursor = database.query(
                        ENCKEY_TABLE_NAME,
                        projection,
                        selectClause,
                        selectArgs,
                        null,   // don't group the rows
                        null,   // don't filter by row groups
                        null);  // The sort order

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        final String encKey = cursor.getString(0);
        final String encIv = cursor.getString(1);
        cursor.close();

        return new Pair<>(encKey, encIv);
    }

    public void saveEncKey(@NonNull String encKey, @NonNull String encIv, @NonNull String repoId) {
        Pair<String, String> old = getEnckey(repoId);

        if (old != null && !TextUtils.isEmpty(old.first)) {
            if (old.first.equals(encKey) && old.second.equals(encIv)) {
                return;
            } else {
                delEnckey(repoId);
            }
        }

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ENCKEY_COLUMN_ENCKEY, encKey);
        values.put(ENCKEY_COLUMN_ENCIV, encIv);
        values.put(ENCKEY_COLUMN_REPO_ID, repoId);

        database.insert(ENCKEY_TABLE_NAME, null, values);
    }

    private void delEnckey(String repoId) {
        database.delete(ENCKEY_TABLE_NAME,  ENCKEY_COLUMN_REPO_ID + "=?",
                new String[] { repoId });
    }

    public void clearEnckeys() {
        database.delete(ENCKEY_TABLE_NAME, null, null);
    }

    public void clearEnckeys(List<String> exceptionIds) {
        List<Pair<String, String>> encKeyList = Lists.newArrayList();
        List<String> _exceptionIds = new ArrayList<>(exceptionIds);
        for(String repoId:exceptionIds) {
            Pair<String, String> encKey = getEnckey(repoId);
            if (encKey == null) {
                _exceptionIds.remove(repoId);
            } else {
                encKeyList.add(encKey);
            }
        }

        clearEnckeys();

        for (int i = 0; i < exceptionIds.size(); i++) {
            Pair<String, String> encKey = encKeyList.get(i);
            String repoId = _exceptionIds.get(i);
            saveEncKey(encKey.first, encKey.second, repoId);
        }
    }

    public void saveDownloadItem(Account account, DownloadTaskInfo info) {
        ContentValues values = new ContentValues();
        values.put(DOWNLOAD_COLUMN_ACCOUNT, account.getSignature());
        values.put(DOWNLOAD_COLUMN_TASK_ID, info.taskID);
        values.put(DOWNLOAD_COLUMN_STATE, info.state.name());
        values.put(DOWNLOAD_COLUMN_REPO_ID, info.repoID);
        values.put(DOWNLOAD_COLUMN_REPO_NAME, info.repoName);
        values.put(DOWNLOAD_COLUMN_PATH_IN_REPO, info.pathInRepo);
        values.put(DOWNLOAD_COLUMN_LOCAL_FILE_PATH, info.localFilePath);
        values.put(DOWNLOAD_COLUMN_FILE_SIZE, info.fileSize);
        values.put(DOWNLOAD_COLUMN_FINISHED, info.finished);
        values.put(DOWNLOAD_COLUMN_START_TIME, info.startTime);
        values.put(DOWNLOAD_COLUMN_THUMBNAIL, info.thumbnail ? 1 : 0);
        values.put(DOWNLOAD_COLUMN_ERR, (info.err == null ? "''" : info.err.toJsonString()));

        database.insert(DOWNLOAD_TABLE_NAME, null, values);
    }

    public List<DownloadTaskInfo> getDownloadItems(Account account) {
        List<DownloadTaskInfo> downloads = Lists.newArrayList();

        String[] projection = {
                DOWNLOAD_COLUMN_ID,
                DOWNLOAD_COLUMN_ACCOUNT,
                DOWNLOAD_COLUMN_TASK_ID,
                DOWNLOAD_COLUMN_STATE,
                DOWNLOAD_COLUMN_REPO_ID,
                DOWNLOAD_COLUMN_REPO_NAME,
                DOWNLOAD_COLUMN_PATH_IN_REPO,
                DOWNLOAD_COLUMN_LOCAL_FILE_PATH,
                DOWNLOAD_COLUMN_FILE_SIZE,
                DOWNLOAD_COLUMN_FINISHED,
                DOWNLOAD_COLUMN_START_TIME,
                DOWNLOAD_COLUMN_THUMBNAIL,
                DOWNLOAD_COLUMN_ERR,
        };

        Cursor c = database.query(
                DOWNLOAD_TABLE_NAME,
                projection,
                DOWNLOAD_COLUMN_ACCOUNT + "=?",
                new String[] { account.getSignature() },
                null,   // don't group the rows
                null,   // don't filter by row groups
                null    // The sort order
        );

        c.moveToFirst();
        while (!c.isAfterLast()) {
            DownloadTaskInfo item = cursorToDownloadItem(c, account);
            downloads.add(item);
            c.moveToNext();
        }

        c.close();
        return downloads;
    }

    private DownloadTaskInfo cursorToDownloadItem(Cursor cursor, Account account) {
        return new DownloadTaskInfo(
                account,
                cursor.getInt(2),
                TaskState.valueOf(cursor.getString(3)),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getLong(8),
                cursor.getLong(9),
                cursor.getLong(10),
                cursor.getInt(11) == 1,
                SeafException.fromJsonString(cursor.getString(12)));
    }

    public void saveUploadItem(Account account,
                                   UploadTaskInfo info) {
        ContentValues values = new ContentValues();
        values.put(UPLOAD_COLUMN_ACCOUNT, account.getSignature());
        values.put(UPLOAD_COLUMN_TASK_ID, info.taskID);
        values.put(UPLOAD_COLUMN_STATE, info.state.name());
        values.put(UPLOAD_COLUMN_REPO_ID, info.repoID);
        values.put(UPLOAD_COLUMN_REPO_NAME, info.repoName);
        values.put(UPLOAD_COLUMN_PARENT_DIR, info.parentDir);
        values.put(UPLOAD_COLUMN_LOCAL_FILE_PATH, info.localFilePath);
        values.put(UPLOAD_COLUMN_IS_UPDATE, info.isUpdate ? 1 : 0);
        values.put(UPLOAD_COLUMN_IS_COPY_TO_LOCAL, info.isCopyToLocal ? 1 : 0);
        values.put(UPLOAD_COLUMN_UPLOAD_SIZE, info.uploadedSize);
        values.put(UPLOAD_COLUMN_TOTAL_SIZE, info.totalSize);
        values.put(UPLOAD_COLUMN_START_TIME, info.startTime);
        values.put(UPLOAD_COLUMN_SOURCE, info.source);
        values.put(UPLOAD_COLUMN_ERR, (info.err == null ? "''" : info.err.toJsonString()));

        database.insert(UPLOAD_TABLE_NAME, null, values);
    }

    public List<UploadTaskInfo> getUploadItems(Account account) {
        List<UploadTaskInfo> uploads = Lists.newArrayList();

        String[] projection = {
                UPLOAD_COLUMN_ID,
                UPLOAD_COLUMN_ACCOUNT,
                UPLOAD_COLUMN_TASK_ID,
                UPLOAD_COLUMN_STATE,
                UPLOAD_COLUMN_REPO_ID,
                UPLOAD_COLUMN_REPO_NAME,
                UPLOAD_COLUMN_PARENT_DIR,
                UPLOAD_COLUMN_LOCAL_FILE_PATH,
                UPLOAD_COLUMN_IS_UPDATE,
                UPLOAD_COLUMN_IS_COPY_TO_LOCAL,
                UPLOAD_COLUMN_UPLOAD_SIZE,
                UPLOAD_COLUMN_TOTAL_SIZE,
                UPLOAD_COLUMN_START_TIME,
                UPLOAD_COLUMN_SOURCE,
                UPLOAD_COLUMN_ERR,
        };

        Cursor c = database.query(
                UPLOAD_TABLE_NAME,
                projection,
                UPLOAD_COLUMN_ACCOUNT + "=?",
                new String[] { account.getSignature() },
                null,   // don't group the rows
                null,   // don't filter by row groups
                null    // The sort order
        );

        c.moveToFirst();
        while (!c.isAfterLast()) {
            UploadTaskInfo item = cursorToUploadItem(c, account);
            uploads.add(item);
            c.moveToNext();
        }

        c.close();
        return uploads;
    }

    private UploadTaskInfo cursorToUploadItem(Cursor cursor, Account account) {
        return new UploadTaskInfo(
                account,
                cursor.getInt(2),
                TaskState.valueOf(cursor.getString(3)),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getInt(8) == 1,
                cursor.getInt(9) == 1,
                cursor.getLong(10),
                cursor.getLong(11),
                cursor.getLong(12),
                cursor.getString(13),
                SeafException.fromJsonString(cursor.getString(14)));
    }
}