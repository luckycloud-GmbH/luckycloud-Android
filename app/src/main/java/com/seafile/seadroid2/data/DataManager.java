package com.seafile.seadroid2.data;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.seafile.seadroid2.BuildConfig;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SeafConnection;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.account.AccountInfo;
import com.seafile.seadroid2.crypto.Crypto;
import com.seafile.seadroid2.transfer.DownloadTaskInfo;
import com.seafile.seadroid2.transfer.UploadTaskInfo;
import com.seafile.seadroid2.util.Utils;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DataManager {
    private static final String DEBUG_TAG = "DataManager";
    private static final long SET_PASSWORD_INTERVAL = 59 * 60 * 1000; // 59 min
    // private static final long SET_PASSWORD_INTERVAL = 5 * 1000; // 5s

    // pull to refresh
    public static final String PULL_TO_REFRESH_LAST_TIME_FOR_REPOS_FRAGMENT = "repo fragment last update";
    public static final String PULL_TO_REFRESH_LAST_TIME_FOR_STARRED_FRAGMENT = "starred fragment last update ";
    private static SimpleDateFormat ptrDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static Map<String, PasswordInfo> passwords = Maps.newHashMap();
    private static Map<String, Long> direntsRefreshTimeMap = Maps.newHashMap();
    public static final long REFRESH_EXPIRATION_MSECS = 10 * 60 * 1000; // 10 mins
    public static long repoRefreshTimeStamp = 0;

    public static final int BUFFER_SIZE = 2 * 1024 * 1024;
    public static final int PAGE_SIZE = 25;

    private SeafConnection sc;
    private Account account;
    private static DatabaseHelper dbHelper;
    private static final StorageManager storageManager = StorageManager.getInstance();

    private List<SeafRepo> reposCache = null;
    private List<SeafBackup> backupsCache = null;
    private List<DownloadTaskInfo> downloadTaskInfosCache = null;
    private List<UploadTaskInfo> uploadTaskInfosCache = null;
    public static List<String> refreshPaths = Lists.newArrayListWithCapacity(0);

    public DataManager(Account act) {
        account = act;
        sc = new SeafConnection(act);
        dbHelper = DatabaseHelper.getDatabaseHelper();
    }

    /**
     * Creates and returns a temporary file. It is guarantied that the file is unique and freshly
     * created. The caller has to delete that file himself.
     *
     * @return a newly created file.
     * @throws IOException if the file could not be created.
     */
    public static File createTempFile() throws IOException {
        return File.createTempFile("file-", ".tmp", storageManager.getTempDir());
    }

    /**
     * Creates and returns a temporary directory. It is guarantied that the directory is unique and
     * empty. The caller has to delete that directory himself.
     *
     * @return a newly created directory.
     * @throws IOException if the directory could not be created.
     */
    public static File createTempDir() throws IOException {
        String dirName = "dir-" + UUID.randomUUID();
        File dir = new File(storageManager.getTempDir(), dirName);
        if (dir.mkdir()) {
            return dir;
        } else {
            throw new IOException("Could not create temp directory");
        }
    }

    public String getThumbnailLink(String repoName, String repoID, String filePath, int size) {
        File file = null;
        try {
            file = getLocalRepoFile(repoName, repoID, filePath);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }

        SeafRepo seafRepo = getCachedRepoByID(repoID);
        // encrypted repo doesn\`t support thumbnails
        if (seafRepo != null && seafRepo.canLocalDecrypt())
            return null;

        // use locally cached file if available
        if (file.exists()) {
            return "file://" + file.getAbsolutePath();
        } else {
            try {
                String pathEnc = URLEncoder.encode(filePath, "UTF-8");
                return account.getServer() + String.format("api2/repos/%s/thumbnail/?p=%s&size=%s", repoID, pathEnc, size);
            } catch (UnsupportedEncodingException e) {
                return null;
            }

        }
    }

    public String getImageThumbnailLink(String repoName, String repoID, String filePath, int size) {
        try {
            String pathEnc = URLEncoder.encode(filePath, "UTF-8");
            return account.getServer() + String.format("api2/repos/%s/thumbnail/?p=%s&size=%s", repoID, pathEnc, size);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public String getThumbnailLink(String repoID, String filePath, int size) {
        SeafRepo repo = getCachedRepoByID(repoID);
        if (repo != null)
            return getThumbnailLink(repo.getName(), repoID, filePath, size);
        else
            return null;
    }

    public AccountInfo getAccountInfo() throws SeafException, JSONException {
        String json = sc.getAccountInfo();
        return parseAccountInfo(json);
    }

    private AccountInfo parseAccountInfo(String json) throws JSONException {
        JSONObject object = Utils.parseJsonObject(json);
        if (object == null)
            return null;

        return AccountInfo.fromJson(object, account.getServer());
    }

    public ServerInfo getServerInfo() throws SeafException, JSONException {
        String json = sc.getServerInfo();
        return parseServerInfo(json);
    }

    public ServerInfo parseServerInfo(String json) throws JSONException {
        JSONObject object = Utils.parseJsonObject(json);
        if (object == null)
            return null;

        return ServerInfo.fromJson(object, account.getServer());
    }

    public Account getAccount() {
        return account;
    }

    private File getFile4RepoCache(String repoID) {
        String filename = "repo-" + (account.server + account.email + repoID).hashCode() + ".dat";
        return new File(storageManager.getJsonCacheDir(), filename);
    }

    public File getFileForReposCache() {
        String filename = "repos-" + (account.server + account.email).hashCode() + ".dat";
        return new File(storageManager.getJsonCacheDir(), filename);
    }

    private File getFileForDirentCache(String dirID) {
        String filename = "dirent-" + dirID + ".dat";
        return new File(storageManager.getJsonCacheDir() + "/" + filename);
    }

    private File getFileForBlockCache(String blockId) {
        String filename = "block-" + blockId + ".dat";
        return new File(storageManager.getTempDir() + "/" + filename);
    }

    public File getFileForBackupsCache() {
        String filename = "backups-" + (account.server + account.email).hashCode() + ".dat";
        return new File(storageManager.getJsonCacheDir(), filename);
    }

    public List<SeafBackup> getBackupsFromCache() {
        if (backupsCache != null)
            return backupsCache;

        File cache = getFileForBackupsCache();
        if (cache.exists()) {
            String json = Utils.readFile(cache);
            if (json == null) {
                return null;
            }
            backupsCache = parseBackups(json);
            return backupsCache;
        }
        return null;
    }

    private List<SeafBackup> parseBackups(String json) {
        try {
            // may throw ClassCastException
            JSONArray array = Utils.parseJsonArray(json);
            if (array.length() == 0)
                return Lists.newArrayListWithCapacity(0);

            ArrayList<SeafBackup> backups = Lists.newArrayList();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                SeafBackup backup = SeafBackup.fromJson(obj);
                if (backup != null)
                    backups.add(backup);
            }
            return backups;
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "parse json error");
            return null;
        } catch (Exception e) {
            // other exception, for example ClassCastException
            Log.e(DEBUG_TAG, "parseBackups exception");
            return null;
        }
    }

    public boolean setBackupsToCache(List<SeafBackup> backups) {
        try {
            File cache = getFileForBackupsCache();
            Utils.writeFile(cache, backupsToString(backups));
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "Could not write backup cache to disk.", e);
            return false;
        }
        return true;
    }

    public boolean addBackupsToCache(SeafBackup backup) {
        if (backupsCache == null) {
            backupsCache = getBackupsFromCache();
        }
        if (backupsCache == null) {
            backupsCache = Lists.newArrayList();
        }
        if (!backupsCache.contains(backup)) {
            backupsCache.add(backup);
        }
        return setBackupsToCache(backupsCache);
    }

    public String backupsToString(List<SeafBackup> backups) {
        if (backups == null) {
            backups = Lists.newArrayList();
        }
        StringBuilder reposString = new StringBuilder("[");
        for (int i = 0; i < backups.size(); i++) {
            SeafBackup repo = backups.get(i);
            reposString.append(SeafBackup.toString(repo));
            if (i < backups.size() - 1) {
                reposString.append(",");
            }
        }
        reposString.append("]");
        return reposString.toString();
    }

    public List<DownloadTaskInfo> getDownloadsFromDB() {
        if (downloadTaskInfosCache == null)
            downloadTaskInfosCache = dbHelper.getDownloadItems(account);
        return downloadTaskInfosCache;
    }

    public void addDownloadToDB(DownloadTaskInfo info) {
        dbHelper.saveDownloadItem(account, info);
    }

//    public File getFileForDownloadsCache() {
//        String filename = "downloads-" + (account.server + account.email).hashCode() + ".dat";
//        return new File(storageManager.getJsonCacheDir(), filename);
//    }
//
//    public List<DownloadTaskInfo> getDownloadsFromCache() {
//        if (downloadTaskInfosCache != null)
//            return downloadTaskInfosCache;
//
//        File cache = getFileForDownloadsCache();
//        if (cache.exists()) {
//            String json = Utils.readFile(cache);
//            if (json == null) {
//                return null;
//            }
//            downloadTaskInfosCache = parseDownloads(json);
//            return downloadTaskInfosCache;
//        }
//        return null;
//    }
//
//    private List<DownloadTaskInfo> parseDownloads(String json) {
//        try {
//            // may throw ClassCastException
//            JSONArray array = Utils.parseJsonArray(json);
//            if (array.length() == 0)
//                return Lists.newArrayListWithCapacity(0);
//
//            ArrayList<DownloadTaskInfo> downloads = Lists.newArrayList();
//            for (int i = 0; i < array.length(); i++) {
//                JSONObject obj = array.getJSONObject(i);
//                DownloadTaskInfo info = DownloadTaskInfo.fromJson(account, obj);
//                if (info != null)
//                    downloads.add(info);
//            }
//            return downloads;
//        } catch (JSONException e) {
//            Log.e(DEBUG_TAG, "parse json error");
//            return null;
//        } catch (Exception e) {
//            // other exception, for example ClassCastException
//            Log.e(DEBUG_TAG, "parseDownloads exception");
//            return null;
//        }
//    }
//
//    public boolean setDownloadsToCache(List<DownloadTaskInfo> infos) {
//        try {
//            File cache = getFileForDownloadsCache();
//            Utils.writeFile(cache, downloadsToString(infos));
//        } catch (IOException e) {
//            Log.e(DEBUG_TAG, "Could not write backup cache to disk.", e);
//            return false;
//        }
//        return true;
//    }
//
//    public boolean addDownloadsToCache(DownloadTaskInfo info) {
//        if (downloadTaskInfosCache == null) {
//            downloadTaskInfosCache = getDownloadsFromCache();
//        }
//        if (downloadTaskInfosCache == null) {
//            downloadTaskInfosCache = Lists.newArrayList();
//        }
//        if (!downloadTaskInfosCache.contains(info)) {
//            downloadTaskInfosCache.add(info);
//            return setDownloadsToCache(downloadTaskInfosCache);
//        }
//        return false;
//    }
//
//    public String downloadsToString(List<DownloadTaskInfo> infos) {
//        if (infos == null) {
//            infos = Lists.newArrayList();
//        }
//        StringBuilder infosString = new StringBuilder("[");
//        for (int i = 0; i < infos.size(); i++) {
//            DownloadTaskInfo info = infos.get(i);
//            infosString.append(DownloadTaskInfo.toString(info));
//            if (i < infos.size() - 1) {
//                infosString.append(",");
//            }
//        }
//        infosString.append("]");
//        return infosString.toString();
//    }
//
    public List<UploadTaskInfo> getUploadsFromDB() {
        if (uploadTaskInfosCache == null)
            uploadTaskInfosCache = dbHelper.getUploadItems(account);
        return uploadTaskInfosCache;
    }

    public void addUploadToDB(UploadTaskInfo info) {
        dbHelper.saveUploadItem(account, info);
    }

//    public File getFileForUploadsCache() {
//        String filename = "uploads-" + (account.server + account.email).hashCode() + ".dat";
//        return new File(storageManager.getJsonCacheDir(), filename);
//    }

//    public List<UploadTaskInfo> getUploadsFromCache() {
//        if (uploadTaskInfosCache != null)
//            return uploadTaskInfosCache;
//
//        File cache = getFileForUploadsCache();
//        if (cache.exists()) {
//            String json = Utils.readFile(cache);
//            if (json == null) {
//                return null;
//            }
//            uploadTaskInfosCache = parseUploads(json);
//            return uploadTaskInfosCache;
//        }
//        return null;
//    }
//
//    private List<UploadTaskInfo> parseUploads(String json) {
//        try {
//            // may throw ClassCastException
//            JSONArray array = Utils.parseJsonArray(json);
//            if (array.length() == 0)
//                return Lists.newArrayListWithCapacity(0);
//
//            ArrayList<UploadTaskInfo> uploads = Lists.newArrayList();
//            for (int i = 0; i < array.length(); i++) {
//                JSONObject obj = array.getJSONObject(i);
//                UploadTaskInfo info = UploadTaskInfo.fromJson(account, obj);
//                if (info != null)
//                    uploads.add(info);
//            }
//            return uploads;
//        } catch (JSONException e) {
//            Log.e(DEBUG_TAG, "parse json error");
//            return null;
//        } catch (Exception e) {
//            // other exception, for example ClassCastException
//            Log.e(DEBUG_TAG, "parseUploads exception");
//            return null;
//        }
//    }
//
//    public boolean setUploadsToCache(List<UploadTaskInfo> infos) {
//        try {
//            File cache = getFileForUploadsCache();
//            Utils.writeFile(cache, uploadsToString(infos));
//        } catch (IOException e) {
//            Log.e(DEBUG_TAG, "Could not write backup cache to disk.", e);
//            return false;
//        }
//        return true;
//    }
//
//    public boolean addUploadsToCache(UploadTaskInfo info) {
//        if (uploadTaskInfosCache == null) {
//            uploadTaskInfosCache = getUploadsFromCache();
//        }
//        if (uploadTaskInfosCache == null) {
//            uploadTaskInfosCache = Lists.newArrayList();
//        }
//        if (!uploadTaskInfosCache.contains(info)) {
//            List<UploadTaskInfo> newInfos = new ArrayList<>(uploadTaskInfosCache);
//            newInfos.add(info);
//            return setUploadsToCache(newInfos);
//        }
//        return false;
//    }
//
//    public String uploadsToString(List<UploadTaskInfo> infos) {
//        if (infos == null) {
//            infos = Lists.newArrayList();
//        }
//        StringBuilder infosString = new StringBuilder("[");
//        for (int i = 0; i < infos.size(); i++) {
//            UploadTaskInfo info = infos.get(i);
//            infosString.append(UploadTaskInfo.toString(info));
//            if (i < infos.size() - 1) {
//                infosString.append(",");
//            }
//        }
//        infosString.append("]");
//        return infosString.toString();
//    }

    public void clearDownloadAndUploadCache() {
        downloadTaskInfosCache = null;
        uploadTaskInfosCache = null;

//        setDownloadsToCache(Lists.newArrayList());
//        setUploadsToCache(Lists.newArrayList());
    }

    /**
     * The account directory structure of Seafile is like this:
     * <p>
     * StorageManager.getMediaDir()
     * |__ foo@gmail.com (cloud.seafile.com)
     * |__ Photos
     * |__ Musics
     * |__ ...
     * |__ foo@mycompany.com (seafile.mycompany.com)
     * |__ Documents
     * |__ Manuals
     * |__ ...
     * |__ ...
     * <p>
     * In the above directory, the user has used two accounts.
     * <p>
     * 1. One account has email "foo@gmail.com" and server
     * "cloud.seafile.com". Two repos, "Photos" and "Musics", has been
     * viewed.
     *
     * 2. Another account has email "foo@mycompany.com", and server
     * "seafile.mycompany.com". Two repos, "Documents" and "Manuals", has
     * been viewed.
     */
    public String getAccountDir() {
        String username = account.getEmail();
        String server = Utils.stripSlashes(account.getServerHost());
        // strip port, like :8000 in 192.168.1.116:8000
        if (server.indexOf(":") != -1)
            server = server.substring(0, server.indexOf(':'));
        String p = String.format("%s (%s)", username, server);
        p = p.replaceAll("[^\\w\\d\\.@\\(\\) ]", "_");
        String accountDir = Utils.pathJoin(storageManager.getMediaDir().getAbsolutePath(), p);
        return accountDir;
    }

    /**
     * Get the top dir of a repo. If there are multiple repos with same name,
     * say "ABC", their top dir would be "ABC", "ABC (1)", "ABC (2)", etc. The
     * mapping (repoID, dir) is stored in a database table.
     */
    private synchronized String getRepoDir(String repoName, String repoID) throws RuntimeException {
        File repoDir;

        // Check if there is a record in database
        if (account == null || TextUtils.isEmpty(repoID)) {
            return null;
        }
        String uniqueRepoName = dbHelper.getRepoDir(account, repoID);
        if (uniqueRepoName != null) {
            // Has record in database
            repoDir = new File(getAccountDir(), uniqueRepoName);
            if (!repoDir.exists()) {
                if (!repoDir.mkdirs()) {
                    throw new RuntimeException("Could not create library directory " + repoDir);
                }
            }
            return repoDir.getAbsolutePath();
        }

        int i = 0;
        while (true) {
            if (i == 0) {
                uniqueRepoName = repoName;
            } else {
                uniqueRepoName = repoName + " (" + i + ")";
            }
            repoDir = new File(getAccountDir(), uniqueRepoName);
            if (!repoDir.exists() && !dbHelper.repoDirExists(account, uniqueRepoName)) {
                // This repo dir does not exist yet, we can use it
                break;
            }
            i++;
        }

        if (!repoDir.mkdirs()) {
            throw new RuntimeException("Could not create repo directory " + uniqueRepoName
                    + "Phone storage space is insufficient or too many " + uniqueRepoName + " directory in phone");
        }

        // Save the new mapping in database
        dbHelper.saveRepoDirMapping(account, repoID, uniqueRepoName);

        return repoDir.getAbsolutePath();
    }

    /**
     * Each repo is placed under [account-dir]/[repo-name]. When a
     * file is downloaded, it's placed in its repo, with its full path.
     *
     * @param repoName
     * @param repoID
     * @param path
     */
    public File getLocalRepoFile(String repoName, String repoID, String path) throws RuntimeException {
        if (TextUtils.isEmpty(repoID)) {
            return null;
        }
        String repoDir = getRepoDir(repoName, repoID);
        if (TextUtils.isEmpty(repoDir)) {
            return null;
        }
        String localPath = Utils.pathJoin(repoDir, path);
        File parentDir = new File(Utils.getParentPath(localPath));
        if (!parentDir.exists()) {
            // TODO should check if the directory creation succeeds
            parentDir.mkdirs();
        }

        return new File(localPath);
    }

    public String getLocalRepoFilePath(String repoName, String repoID, String path) throws RuntimeException {
        if (TextUtils.isEmpty(repoID)) {
            return null;
        }
        String repoDir = getRepoDir(repoName, repoID);
        if (TextUtils.isEmpty(repoDir)) {
            return null;
        }
        return Utils.pathJoin(repoDir, path);
    }

    public File getLocalEncRepoThumbFile(String repoName, String repoID, String path) throws RuntimeException {
        if (TextUtils.isEmpty(repoID)) {
            return null;
        }
        String repoDir = getRepoDir(repoName, repoID);
        if (TextUtils.isEmpty(repoDir)) {
            return null;
        }
        String localPath = Utils.pathJoin(repoDir, path);
        localPath = Utils.getEncThumbPath(localPath);
        File parentDir = new File(Utils.getParentPath(localPath));
        if (!parentDir.exists()) {
            // TODO should check if the directory creation succeeds
            parentDir.mkdirs();
        }

        return new File(localPath);
    }

    public File getLocalRepoFileWithDownload(String path) throws RuntimeException {
        String localPath = SettingsManager.instance().getDownloadDataLocation() + "/" + Utils.fileNameFromPath(path);
        File parentDir = new File(Utils.getParentPath(localPath));
        if (!parentDir.exists()) {
            // TODO should check if the directory creation succeeds
            parentDir.mkdirs();
        }
        return new File(localPath);
    }

    public File getLocalRepoFileWithDownloadAndNewName(String path) throws RuntimeException {
        File localFile = getLocalRepoFileWithDownload(path);
        if (!localFile.exists())
            return localFile;

        String fileName = Utils.fileNameFromPath(path);
        String extension = Utils.getFileExtension(fileName);
        if (extension.equals("?"))
            extension = "";
        String nameWithoutExt = fileName.substring(0, fileName.length() - extension.length());
        int i = 1;
        File newLocalFile;
        while (true) {
            String newFileName = String.format("%s (%d)%s", nameWithoutExt, i, extension);
            newLocalFile = getLocalRepoFileWithDownload(newFileName);
            if (!newLocalFile.exists())
                break;
            else
                i += 1;
        }
        return newLocalFile;
    }

    private List<SeafRepo> parseRepos(String json) {
        try {
            // may throw ClassCastException
            JSONArray array = Utils.parseJsonArray(json);
            if (array.length() == 0)
                return Lists.newArrayListWithCapacity(0);

            ArrayList<SeafRepo> repos = Lists.newArrayList();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                SeafRepo repo = SeafRepo.fromJson(obj);
                if (repo != null)
                    repos.add(repo);
            }
            return repos;
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "parse json error");
            return null;
        } catch (Exception e) {
            // other exception, for example ClassCastException
            Log.e(DEBUG_TAG, "parseRepos exception");
            return null;
        }
    }

    public String reposToString(List<SeafRepo> repos) {
        if (reposCache != null) {
            reposCache.clear();
            reposCache.addAll(repos);
        }
        StringBuilder reposString = new StringBuilder("[");
        for (int i = 0; i < repos.size(); i++) {
            SeafRepo repo = repos.get(i);
            reposString.append(SeafRepo.toString(repo));
            if (i < repos.size() - 1) {
                reposString.append(",");
            }
        }
        reposString.append("]");
        return reposString.toString();
    }

    private SeafRepoEncrypt parseRepoEncrypt(String json) {
        try {
            JSONObject object = Utils.parseJsonObject(json);
            return SeafRepoEncrypt.fromJson(object);
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "parse json error");
            return null;
        } catch (Exception e) {
            // other exception, for example ClassCastException
            Log.e(DEBUG_TAG, "parseRepos exception");
            return null;
        }
    }


    public String getBlockPathById(String blkId) {
        final File block = getFileForBlockCache(blkId);
        return block.getAbsolutePath();
    }

    public SeafRepo getCachedRepoByID(String id) {
        List<SeafRepo> cachedRepos = getReposFromCache();
        if (cachedRepos == null) {
            return null;
        }

        for (SeafRepo repo : cachedRepos) {
            if (repo.getID().equals(id)) {
                return repo;
            }
        }

        return null;
    }

    public SeafRepoEncrypt getCachedRepoEncryptByID(String id) {
        File cache = getFile4RepoCache(id);
        if (cache.exists()) {
            String json = Utils.readFile(cache);
            if (!TextUtils.isEmpty(json)) {
                return parseRepoEncrypt(json);
            }
        }
        return null;
    }


    public List<SeafRepo> getReposFromCache() {
        if (reposCache != null)
            return reposCache;

        File cache = getFileForReposCache();
        if (cache.exists()) {
            String json = Utils.readFile(cache);
            if (json == null) {
                return null;
            }
            reposCache = parseRepos(json);
            return reposCache;
        }
        return null;
    }

    public List<SeafRepo> getReposFromServer() throws SeafException {
        // First decide if use cache
        if (!Utils.isNetworkOn()) {
            throw SeafException.networkException;
        }

        String json = sc.getRepos();
        //Log.d(DEBUG_TAG, "get repos from server " + json);
        if (json == null)
            return null;

        try {
            // may throw ClassCastException
            JSONObject object = Utils.parseJsonObject(json);
            JSONArray array = object.getJSONArray("repos");
            json = array.toString();
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "parse json error");
            return null;
        }

        reposCache = parseRepos(json);

        try {
            File cache = getFileForReposCache();
            Utils.writeFile(cache, json);
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "Could not write repo cache to disk.", e);
        }

        return reposCache;
    }

    public List<SeafRepoTag> getRepoTagsFromServer(SeafRepo repo) throws SeafException {

        List<SeafRepoTag> repoTags = Lists.newArrayList();
        // First decide if use cache
        if (!Utils.isNetworkOn()) {
            throw SeafException.networkException;
        }

        String json = sc.getRepoTags(repo);
        //Log.d(DEBUG_TAG, "get repotags from server " + json);
        if (json == null)
            return null;

        try {
            // may throw ClassCastException
            JSONArray array = Utils.parseJsonArrayByKey(json, "repo_tags");
            if (array.length() == 0)
                return repoTags;

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                SeafRepoTag tag = SeafRepoTag.fromJson(obj);
                if (tag != null)
                    repoTags.add(tag);
            }
            return repoTags;
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "parse json error");
            return null;
        } catch (Exception e) {
            // other exception, for example ClassCastException
            Log.e(DEBUG_TAG, "parseRepos exception");
            return null;
        }
    }

    public String addRepoTag(SeafRepo repo, String tagName, String tagColor) throws SeafException {

        String json = sc.addRepoTag(repo, tagName, tagColor);
        if (json == null)
            return null;

        try {
            return json;
        } catch (Exception e) {
            // other exception, for example ClassCastException
            Log.e(DEBUG_TAG, "parseRepos exception");
            return null;
        }
    }

    public List<SeafFileTag> getFileTagsFromServer(SeafRepo repo, String path) throws SeafException {

        List<SeafFileTag> fileTags = Lists.newArrayList();
        // First decide if use cache
        if (!Utils.isNetworkOn()) {
            throw SeafException.networkException;
        }

        String json = sc.getFileTags(repo, path);
        //Log.d(DEBUG_TAG, "get repotags from server " + json);
        if (json == null)
            return null;

        try {
            // may throw ClassCastException
            JSONArray array = Utils.parseJsonArrayByKey(json, "file_tags");
            if (array.length() == 0)
                return fileTags;

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                SeafFileTag tag = SeafFileTag.fromJson(obj);
                if (tag != null)
                    fileTags.add(tag);
            }
            return fileTags;
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "parse json error");
            return null;
        } catch (Exception e) {
            // other exception, for example ClassCastException
            Log.e(DEBUG_TAG, "parseRepos exception");
            return null;
        }
    }

    public String addFileTag(String repoID, String filePath, String fileTagID) throws SeafException {

        String json = sc.addFileTag(repoID, filePath, fileTagID);
        if (json == null)
            return null;

        try {
            return json;
        } catch (Exception e) {
            // other exception, for example ClassCastException
            Log.e(DEBUG_TAG, "parseRepos exception");
            return null;
        }
    }

    public String deleteFileTag(String repoID, String repoTagID) throws SeafException {

        String json = sc.deleteFileTag(repoID, repoTagID);
        if (json == null)
            return null;

        try {
            return json;
        } catch (Exception e) {
            // other exception, for example ClassCastException
            Log.e(DEBUG_TAG, "parseRepos exception");
            return null;
        }
    }

    public void getEncryptRepo(String repoID) throws SeafException {
        String json = sc.getEncryptRepo(repoID);
        //Save to Cache
        if (!TextUtils.isEmpty(json)) {
            try {
                File cache = getFile4RepoCache(repoID);
                Utils.writeFile(cache, json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveDirentContent(String repoID, String parentDir, String dirID, String content) {
        deleteOldDirentContent(repoID, parentDir);
        dbHelper.saveDirents(repoID, parentDir, dirID);

        try {
            File cache = getFileForDirentCache(dirID);
            Utils.writeFile(cache, content);
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "Could not write dirent cache to disk.", e);
        }
    }

    public void deleteDirentContent(String repoID, String parentDir) {
        deleteOldDirentContent(repoID, parentDir);
    }

    /**
     * Clean up old dirent cache for a directory where we have received new data.
     *
     * @param repoID
     * @param dir
     */
    private void deleteOldDirentContent(String repoID, String dir) {
        String dirID = dbHelper.getCachedDirents(repoID, dir);

        // identical directory content results in same dirID. So check if whether
        // the dirID is referenced multiple times before deleting it.
        if (dirID != null && dbHelper.getCachedDirentUsage(dirID) <= 1) {
            File file = getFileForDirentCache(dirID);
            file.delete();
        }
        // and finally delete the entry in the SQL table
        dbHelper.removeCachedDirents(repoID, dir);
    }

    public synchronized File getFile(
            String repoName,
            String repoID,
            String path,
            boolean offlineAvailable,
            boolean thumbnail,
            ProgressMonitor monitor) throws SeafException {
        String cachedFileID = null;
        SeafCachedFile cf = getCachedFile(repoName, repoID, path);
        File localFile;
        if (offlineAvailable)
            localFile = getLocalRepoFile(repoName, repoID, path);
        else
            localFile = getLocalRepoFileWithDownloadAndNewName(path);
        // If local file is up to date, show it
        if (cf != null) {
            if (localFile.exists()) {
                cachedFileID = cf.fileID;
            }
        }

        Pair<String, File> ret = sc.getFile(repoID, path, localFile.getPath(), cachedFileID, monitor);

        String fileID = ret.first;
        if (fileID.equals(cachedFileID)) {
            // cache is valid
            return localFile;
        } else {
            File file = ret.second;
            if (offlineAvailable)
                addCachedFile(repoName, repoID, path, fileID, file, thumbnail);
            return file;
        }
    }

    public synchronized File getFileByBlocks(
            String repoName,
            String repoID,
            String path,
            long fileSize,
            boolean offlineAvailable,
            boolean thumbnail,
            ProgressMonitor monitor) throws SeafException, IOException, JSONException, NoSuchAlgorithmException {

        String cachedFileID = null;
        SeafCachedFile cf = getCachedFile(repoName, repoID, path);
        File localFile;
        if (offlineAvailable)
            localFile = getLocalRepoFile(repoName, repoID, path);
        else
            localFile = getLocalRepoFileWithDownloadAndNewName(path);
        // If local file is up to date, show it
        if (cf != null) {
            if (localFile.exists()) {
                cachedFileID = cf.fileID;
            }
        }

        final String json = sc.getBlockDownloadList(repoID, path);
        JSONObject obj = new JSONObject(json);
        FileBlocks fileBlocks = FileBlocks.fromJson(obj);

        if (fileBlocks.fileID.equals(cachedFileID)) {
            // cache is valid
            Log.d(DEBUG_TAG, "cache is valid");
            return localFile;
        }

        final Pair<String, String> pair = getRepoEncKey(repoID);
        if (pair == null) {
            throw SeafException.decryptException;
        }
        final String encKey = pair.first;
        final String encIv = pair.second;
        if (TextUtils.isEmpty(encKey) || TextUtils.isEmpty(encIv)) {
            throw SeafException.decryptException;
        }

        if (fileBlocks.blocks == null) {
            if (!localFile.createNewFile()) {
                Log.w(DEBUG_TAG, "Failed to create file " + localFile.getName());
                return null;
            }
            Log.d(DEBUG_TAG, String.format("addCachedFile repoName %s, repoId %s, path %s, fileId %s", repoName, repoID, path, fileBlocks.fileID));
            if (offlineAvailable)
                addCachedFile(repoName, repoID, path, fileBlocks.fileID, localFile, thumbnail);
            return localFile;
        }

        for (Block blk : fileBlocks.blocks) {
            File tempBlock = new File(storageManager.getTempDir(), blk.blockId);
            final Pair<String, File> block = sc.getBlock(repoID, fileBlocks, blk.blockId, tempBlock.getPath(), fileSize, monitor);
            final byte[] bytes = FileUtils.readFileToByteArray(block.second);
            final byte[] decryptedBlock = Crypto.decrypt(bytes, encKey, encIv);
            FileUtils.writeByteArrayToFile(localFile, decryptedBlock, true);

            if (tempBlock.exists() && thumbnail) {
                tempBlock.delete();
            }
        }

        Log.d(DEBUG_TAG, String.format("addCachedFile repoName %s, repoId %s, path %s, fileId %s", repoName, repoID, path, fileBlocks.fileID));
        if (offlineAvailable)
            addCachedFile(repoName, repoID, path, fileBlocks.fileID, localFile, thumbnail);
        return localFile;
    }

    public static List<SeafDirent> parseDirents(String json) {
        try {
            JSONArray array = Utils.parseJsonArray(json);
            if (array == null)
                return null;

            ArrayList<SeafDirent> dirents = Lists.newArrayList();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                SeafDirent de = SeafDirent.fromJson(obj);
                if (de != null)
                    dirents.add(de);
            }
            return dirents;
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "Could not parse cached dirent", e);
            return null;
        }
    }

    public static String direntsToString(List<SeafDirent> dirents) {
        StringBuilder direntsString = new StringBuilder("[");
        for (int i = 0; i < dirents.size(); i++) {
            SeafDirent dirent = dirents.get(i);
            direntsString.append(SeafDirent.toString(dirent));
            if (i < dirents.size() - 1) {
                direntsString.append(",");
            }
        }
        direntsString.append("]");
        return direntsString.toString();
    }

    private List<SeafStarredFile> parseStarredFiles(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray array = (JSONArray) jsonObject.opt("starred_item_list");
//            JSONArray array = Utils.parseJsonArray(json);
            if (array == null)
                return null;

            ArrayList<SeafStarredFile> starredFiles = Lists.newArrayList();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                SeafStarredFile sf = SeafStarredFile.fromJson(obj);
                if (sf != null)
                    starredFiles.add(sf);
            }
            return starredFiles;
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "Could not parse cached starred files", e);
            return null;
        }
    }

    private String stringStarredFiles(List<SeafStarredFile> mStarredFiles) {

        String result = "{'starred_item_list':[";
        for (int i = 0; i < mStarredFiles.size(); i++) {
            result += SeafStarredFile.toString(mStarredFiles.get(i));
            if (i < (mStarredFiles.size() - 1))
                result += ",";
        }
        result += "]}";
        return result;
    }

    public List<SeafDirent> getCachedDirents(String repoID, String path) {
        String dirID = dbHelper.getCachedDirents(repoID, path);
        if (dirID == null) {
            return null;
        }

        File cache = getFileForDirentCache(dirID);
        if (!cache.exists()) {
            return null;
        }

        String json = Utils.readFile(cache);
        if (json == null) {
            return null;
        }

        return parseDirents(json);
    }

    /**
     * In four cases we need to visit the server for dirents
     * <p>
     * 1. No cached dirents
     * 2. User clicks "refresh" button.
     * 3. Download all dirents within a folder
     * 4. View starred or searched files in gallery without available local cache
     *
     * In the second case, the local cache may still be valid.
     */
    public List<SeafDirent> getDirentsFromServer(String repoID, String path) throws SeafException {

        // first fetch our cached dirent and read it
        String cachedDirID = dbHelper.getCachedDirents(repoID, path);
        String cachedContent = null;
        File cacheFile = getFileForDirentCache(cachedDirID);
        if (cacheFile.exists()) {
            cachedContent = Utils.readFile(cacheFile);
        }

        // if that didn't work, then we have no cache.
        if (cachedContent == null) {
            cachedDirID = null;
        }

        // fetch new dirents. ret.second will be null if the cache is still valid
        Pair<String, String> ret = sc.getDirents(repoID, path, cachedDirID);

        String content;
        if (ret.second != null) {
            String dirID = ret.first;
            content = ret.second;
            saveDirentContent(repoID, path, dirID, content);
        } else {
            content = cachedContent;
        }

        return parseDirents(content);

//        String json = sc.getDirents(repoID, path, cachedDirID);
//        String content;
//        try {
//            JSONObject jsonObject = new JSONObject(json);
//
//            String dirID = jsonObject.getString("dir_id");
//            content = jsonObject.getString("dirent_list");
//            saveDirentContent(repoID, path, dirID, content);
//        } catch (JSONException e) {
//            Log.e(DEBUG_TAG, "Could not parse cached dirents", e);
//            content = cachedContent;
//        }
//        return parseDirents(content);
    }

    public List<SeafStarredFile> getStarredFiles() throws SeafException {
        String starredFiles = sc.getStarredFiles();
        Log.v(DEBUG_TAG, "Save starred files: " + starredFiles);
        if (starredFiles == null) {
            return null;
        }
        dbHelper.saveCachedStarredFiles(account, starredFiles);
        return parseStarredFiles(starredFiles);
    }

    public List<SeafStarredFile> getCachedStarredFiles() {
        String starredFiles = dbHelper.getCachedStarredFiles(account);
        Log.v(DEBUG_TAG, "Get cached starred files: " + starredFiles);
        if (starredFiles == null) {
            return null;
        }
        return parseStarredFiles(starredFiles);
    }

    public void saveCachedStarredFiles(List<SeafStarredFile> starredFiles) {
        if (starredFiles == null)
            starredFiles = Lists.newArrayListWithCapacity(0);
        dbHelper.saveCachedStarredFiles(account, stringStarredFiles(starredFiles));
    }


    public SeafCachedFile getCachedFile(String repoName, String repoID, String path) {
        SeafCachedFile cf = dbHelper.getFileCacheItem(repoID, path, this);
        return cf;
    }

    public List<SeafCachedFile> getCachedFiles() {
        return dbHelper.getFileCacheItems(this);
    }

    public void addCachedFile(String repoName, String repoID, String path, String fileID, File file, boolean thumbnail) {
        if (file == null) {
            return;
        }
        // notify Android Gallery that a new file has appeared

        // file does not always reside in Seadroid directory structure (e.g. camera upload)
        if (file.exists())
            storageManager.notifyAndroidGalleryFileChange(file);

        if (thumbnail) {
            String fullPath = Utils.pathJoin(repoName, path);
            if (!SeadroidApplication.getInstance().getGalleryPhotos().contains(fullPath)) {
                return;
            }
        }
        SeafCachedFile item = new SeafCachedFile();
        item.repoName = repoName;
        item.repoID = repoID;
        item.path = path;
        item.fileID = fileID;
        item.accountSignature = account.getSignature();
        dbHelper.saveFileCacheItem(item, this);
    }

    public void removeCachedFile(SeafCachedFile cf) {
        // TODO should check if the file deletion succeeds
        cf.file.delete();
        dbHelper.deleteFileCacheItem(cf);
    }

    public void setPassword(String repoID, String passwd) throws SeafException {
        boolean success = sc.setPassword(repoID, passwd);
        //if password is true, to get encrypt repo info
        if (success) {
            getEncryptRepo(repoID);
        }
    }

    public void uploadFile(String repoName, String repoID, String dir, String filePath, ProgressMonitor monitor, boolean isUpdate, boolean isCopyToLocal) throws SeafException, IOException {
        uploadFileCommon(repoName, repoID, dir, filePath, monitor, isUpdate, isCopyToLocal);
    }

    private void uploadFileCommon(String repoName, String repoID, String dir,
                                  String filePath, ProgressMonitor monitor,
                                  boolean isUpdate, boolean isCopyToLocal) throws SeafException, IOException {
        String newFileID = sc.uploadFile(repoID, dir, filePath, monitor,isUpdate);
        if (newFileID == null || newFileID.length() == 0) {
            return;
        }

        File srcFile = new File(filePath);
        String path = Utils.pathJoin(dir, srcFile.getName());
        File fileInRepo = null;
        try {
            fileInRepo = getLocalRepoFile(repoName, repoID, path);
        } catch (RuntimeException e) {
            e.printStackTrace();
            new SeafException(SeafException.OTHER_EXCEPTION, e.getMessage());
        }

        if (isCopyToLocal) {
            if (!isUpdate) {
                // Copy the uploaded file to local repo cache
                try {
                    Utils.copyFile(srcFile, fileInRepo);
                } catch (IOException e) {
                    return;
                }
            }
        }
        // Update file cache entry
        if (isUpdate) {
            SeafCachedFile item = new SeafCachedFile();
            item.repoName = repoName;
            item.repoID = repoID;
            item.path = path;
            item.fileID = newFileID;
            item.accountSignature = account.getSignature();
            SeafCachedFile scf = getCachedFile(repoName, repoID, path);
            if (scf != null) {
                if (scf.file.exists())
                    scf.file.delete();
                dbHelper.deleteFileCacheItem(scf);
            }
        }
        addCachedFile(repoName, repoID, path, newFileID, fileInRepo, false);
    }

    public void createNewRepo(String repoName, String password) throws SeafException {
        sc.createNewRepo(repoName, "", password);
    }

    public void createNewDir(String repoID, String parentDir, String dirName) throws SeafException {
        Pair<String, String> ret = sc.createNewDir(repoID, parentDir, dirName);
        if (ret == null) {
            return;
        }

        String newDirID = ret.first;
        String response = ret.second;

        // The response is the dirents of the parentDir after creating
        // the new dir. We save it to avoid request it again
        saveDirentContent(repoID, parentDir, newDirID, response);
    }

    public void createNewFile(String repoID, String parentDir, String fileName) throws SeafException {
        String ret = sc.createNewFile(repoID, parentDir, fileName);
//        if (ret == null) {
//            return;
//        }

//        String newDirID = ret.first;
//        String response = ret.second;
//
//        // The response is the dirents of the parentDir after creating
//        // the new file. We save it to avoid request it again
//        saveDirentContent(repoID, parentDir, newDirID, response);
    }

    public File getLocalCachedFile(String repoName, String repoID, String filePath) {
        File localFile = getLocalRepoFile(repoName, repoID, filePath);
        if (!localFile.exists()) {
            return null;
        }

        if (!Utils.isNetworkOn()) {
            return localFile;
        }

        SeafCachedFile cf = getCachedFile(repoName, repoID, filePath);
        if (cf != null) {
            return localFile;
        } else {
            return null;
        }
    }

    public void renameRepo(String repoID, String newName) throws SeafException {
        sc.renameRepo(repoID, newName);
    }

    public void deleteRepo(String repoID) throws SeafException {
        sc.deleteRepo(repoID);
    }

    public void star(String repoID, String path) throws SeafException {
        sc.star(repoID, path);
    }

    public void unstar(String repoID, String path) throws SeafException {
        sc.unstar(repoID, path);
    }

    public void rename(String repoID, String path, String newName, boolean isdir) throws SeafException {
        Pair<String, String> ret = sc.rename(repoID, path, newName, isdir);
        if (ret == null) {
            return;
        }

        String newDirID = ret.first;
        String response = ret.second;

        // The response is the dirents of the parentDir after renaming the
        // file/folder. We save it to avoid request it again.
        saveDirentContent(repoID, Utils.getParentPath(path), newDirID, response);

        // TODO: delete or rename cached files, dirent cache, etc.
        /*
         * I think it is more simple and easier if we provide a "clear cache" button in Settings,
         * just like what we have done with thumbnail caches.
         * And hopefully it already exist.
         * Users can manually clear them if they are boring with those temp files.
         */
    }

    public boolean deleteShareLink(String token) {
        try {
            return sc.deleteShareLink(token);
        } catch (SeafException e) {
            e.printStackTrace();
            return false;
        }
    }


    public ArrayList<SeafLink> getShareLink(String repoID, String path) {
        ArrayList<SeafLink> list = Lists.newArrayListWithCapacity(0);
        try {
            String json = sc.getShareLink(repoID, path);
            if (json != null) {
                JSONArray jsonArray = Utils.parseJsonArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = null;
                    object = (JSONObject) jsonArray.get(i);
                    SeafLink seafLink = SeafLink.fromJson(object);
                    list.add(seafLink);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteUploadLink(String token) {
        try {
            return sc.deleteUploadLink(token);
        } catch (SeafException e) {
            e.printStackTrace();
            return  false;
        }
    }

    public ArrayList<SeafLink> getUploadLink(String repoID, String path) {
        ArrayList<SeafLink> list = Lists.newArrayListWithCapacity(0);
        try {
            String json = sc.getUploadLinkNew(repoID, path);
            if (json != null) {
                JSONArray jsonArray = Utils.parseJsonArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = null;
                    object = (JSONObject) jsonArray.get(i);
                    SeafLink seafLink = SeafLink.fromJson(object);
                    list.add(seafLink);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getInternalLink(String repoID, String path, String direntType) {
        String result = "";
        try {
            String json = sc.getInternalLink(repoID, path, direntType);
            if (json != null) {
                JSONObject obj = Utils.parseJsonObject(json);
                result = obj.getString("smart_link");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean deleteCustomPermission(String repoID, String permissionID) {
        try {
            return sc.deleteCustomPermission(repoID, permissionID);
        } catch (SeafException e) {
            e.printStackTrace();
            return  false;
        }
    }

    public ArrayList<SeafShareUser> searchUsers(String query) throws SeafException {
        ArrayList<SeafShareUser> list = Lists.newArrayListWithCapacity(0);
        try {
            String json = sc.searchUsers(query);
            if (json != null) {
                JSONObject jsonObject = Utils.parseJsonObject(json);
                JSONArray jsonArray = jsonObject.getJSONArray("users");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = null;
                    object = (JSONObject) jsonArray.get(i);
                    SeafShareUser seafUser = SeafShareUser.fromJson(object);
                    list.add(seafUser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<SeafShareableGroup> shareableGroups() throws SeafException {
        ArrayList<SeafShareableGroup> list = Lists.newArrayListWithCapacity(0);
        try {
            String json = sc.shareableGroups();
            if (json != null) {
                JSONArray jsonArray = Utils.parseJsonArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = null;
                    object = (JSONObject) jsonArray.get(i);
                    SeafShareableGroup seafShareGroup = SeafShareableGroup.fromJson(object);
                    list.add(seafShareGroup);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<SeafSharedItem> listSharedItems(String repoID, String path, String shareType) throws SeafException {
        ArrayList<SeafSharedItem> list = Lists.newArrayListWithCapacity(0);
        try {
            String json = sc.listSharedItems(repoID, path, shareType);
            if (json != null) {
                JSONArray jsonArray = Utils.parseJsonArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = null;
                    object = (JSONObject) jsonArray.get(i);
                    SeafSharedItem seafShareGroup = SeafSharedItem.fromJson(object);
                    list.add(seafShareGroup);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<SeafSharedItem> shareFolder(String repoID, String path, String shareType, String permission, ArrayList<String> paramsArray) throws SeafException {
        ArrayList<SeafSharedItem> list = Lists.newArrayListWithCapacity(0);
        try {
            String json = sc.shareFolder(repoID, path, shareType, permission, paramsArray);
            if (json != null) {
                JSONObject jsonObject = Utils.parseJsonObject(json);
                JSONArray jsonArray = jsonObject.getJSONArray("success");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = null;
                    object = (JSONObject) jsonArray.get(i);
                    SeafSharedItem seafSharedItem = SeafSharedItem.fromJson(object);
                    list.add(seafSharedItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<SeafPermission> listCustomPermissions(String query) throws SeafException {
        ArrayList<SeafPermission> list = Lists.newArrayListWithCapacity(0);
        try {
            String json = sc.listCustomPermissions(query);
            if (json != null) {
                JSONObject jsonObject = Utils.parseJsonObject(json);
                JSONArray jsonArray = jsonObject.getJSONArray("permission_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = null;
                    object = (JSONObject) jsonArray.get(i);
                    SeafPermission seafPermission = SeafPermission.fromJson(object);
                    list.add(seafPermission);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void delete(String repoID, String path, boolean isdir) throws SeafException {
        Pair<String, String> ret = sc.delete(repoID, path, isdir);
        if (ret == null) {
            return;
        }

        String newDirID = ret.first;
        String response = ret.second;

        // The response is the dirents of the parentDir after deleting the
        // file/folder. We save it to avoid request it again
        saveDirentContent(repoID, Utils.getParentPath(path), newDirID, response);

        // TODO: isdir==true: recursively delete cached files, dirent cache, etc.
        /*
         * I think it is more simple and easier if we provide a "clear cache" button in Settings,
         * just like what we have done with thumbnail caches.
         * And hopefully it already exist.
         * Users can manually clear them if they are boring with those temp files.
         */

    }

    public void copy(String srcRepoId, String srcDir, String srcFn,
                     String dstRepoId, String dstDir) throws SeafException {
        sc.copy(srcRepoId, srcDir, srcFn, dstRepoId, dstDir);

        // After copying, we need to refresh the destination list
        getDirentsFromServer(dstRepoId, dstDir);
    }

    public void move(String srcRepoId, String srcDir, String srcFn, String dstRepoId, String dstDir,
                     boolean batch) throws SeafException {
        Pair<String, String> ret = null;
        if (batch) {
            sc.move(srcRepoId, srcDir, srcFn, dstRepoId, dstDir);
        } else {
            String srcPath = Utils.pathJoin(srcDir, srcFn);
            ret = sc.move(srcRepoId, srcPath, dstRepoId, dstDir);
        }

        // After moving, we need to refresh the destination list
        getDirentsFromServer(dstRepoId, dstDir);

        // We also need to refresh the original list
        getDirentsFromServer(srcRepoId, srcDir);

        if (ret == null) {
            return;
        }

        String newDirID = ret.first;
        String response = ret.second;

        // The response is the list of dst after moving the
        // file/folder. We save it to avoid request it again
        saveDirentContent(dstRepoId, dstDir, newDirID, response);

    }

    public SeafActivities getEvents(int start, boolean useNewActivity) throws SeafException {
        int moreOffset = 0;
        boolean more;
        if (!Utils.isNetworkOn()) {
            throw SeafException.networkException;
        }

        final String json = sc.getEvents(start, useNewActivity);

        if (json == null) return null;

        final List<SeafEvent> events = parseEvents(json, true);
        final JSONObject object = Utils.parseJsonObject(json);
        if (useNewActivity) {
            if (events.size() < PAGE_SIZE) {
                more = false;
            } else {
                moreOffset = start + 1;
                more = true;
            }
        } else {
            moreOffset = object.optInt("more_offset");
            more = object.optBoolean("more");
        }

        return new SeafActivities(events, moreOffset, more);

    }

    public SeafActivities getEvents2(int start, SeafRepo repo) throws SeafException {
        int moreOffset = 0;
        boolean more = false;
        if (!Utils.isNetworkOn()) {
            throw SeafException.networkException;
        }

        final String json = sc.getEvents2(start, repo);

        if (json == null) return null;

        final List<SeafEvent> events = parseEvents(json, false);
        final JSONObject object = Utils.parseJsonObject(json);
        moreOffset = start + 1;
        more = object.optBoolean("more", false);

        return new SeafActivities(events, moreOffset, more);

    }

    public String getHistoryChanges(String repoId, String commitId) throws SeafException {
        return sc.getHistoryChanges(repoId, commitId);
    }

    public List<SeafEvent> parseEvents(String json, boolean isProEdition) {
        try {
            // may throw ClassCastException
            JSONArray array = Utils.parseJsonArrayByKey(json, "events");
            if (array.length() == 0)
                return Lists.newArrayListWithCapacity(0);

            ArrayList<SeafEvent> events = Lists.newArrayList();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                SeafEvent event = isProEdition ? SeafEvent.fromJson(obj) : SeafEvent.fromJson2(obj, null);
                if (event != null)
                    events.add(event);
            }
            return events;
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "parse json error");
            return null;
        } catch (Exception e) {
            // other exception, for example ClassCastException
            Log.e(DEBUG_TAG, "parseEvents exception");
            return null;
        }
    }

    public static void clearPassword() {
        passwords.clear();
    }

    public static void clearPassword(List<String> exceptionIds) {
        for (Map.Entry<String, PasswordInfo> entry:passwords.entrySet()) {
            if (!exceptionIds.contains(entry.getKey())) {
                passwords.remove(entry.getKey());
            }
        }
    }

    public void completeRemoteWipe() throws SeafException {
        sc.completeRemoteWipe(account.token);
    }

    private static class PasswordInfo {
        String password; // password or encKey
        long timestamp;

        public PasswordInfo(String password, long timestamp) {
            this.password = password;
            this.timestamp = timestamp;
        }
    }

    public boolean getRepoPasswordSet(String repoID) {
        final SeafRepoEncrypt seafRepo = getCachedRepoEncryptByID(repoID);
        if (seafRepo != null && seafRepo.canLocalDecrypt()) {
            Pair<String, String> info = dbHelper.getEnckey(repoID);
            return info != null
                    && !TextUtils.isEmpty(info.first)
                    && !TextUtils.isEmpty(info.second);
        }

        PasswordInfo passwordInfo = passwords.get(repoID);
        if (passwordInfo == null) {
            return false;
        }

        if (Utils.now() - passwordInfo.timestamp > SET_PASSWORD_INTERVAL) {
            return false;
        }

        return true;
    }

    public void setRepoPasswordSet(String repoID, String key, String iv) {
        if (!TextUtils.isEmpty(repoID)
                && !TextUtils.isEmpty(key)
                && !TextUtils.isEmpty(iv)) {
            dbHelper.saveEncKey(key, iv, repoID);
        }
    }

    public void setRepoPasswordSet(String repoID, String password) {
        passwords.put(repoID, new PasswordInfo(password, Utils.now()));
    }

    public String getRepoPassword(String repoID) {
        if (repoID == null) {
            return null;
        }

        final SeafRepoEncrypt seafRepo = getCachedRepoEncryptByID(repoID);
        if (seafRepo != null && seafRepo.canLocalDecrypt()) {
            final Pair<String, String> pair = dbHelper.getEnckey(repoID);
            if (pair == null)
                return null;
            else
                return pair.first;
        }

        PasswordInfo info = passwords.get(repoID);
        if (info == null) {
            return null;
        }

        return info.password;
    }

    private Pair<String, String> getRepoEncKey(String repoID) {
        if (repoID == null) {
            return null;
        }

        return dbHelper.getEnckey(repoID);
    }

    /**
     * calculate if refresh time is expired, the expiration is 10 mins
     */
    public boolean isReposRefreshTimeout() {
        if (Utils.now() < repoRefreshTimeStamp + REFRESH_EXPIRATION_MSECS) {
            return false;
        }

        return true;
    }

    public boolean isDirentsRefreshTimeout(String repoID, String path) {
        if (!direntsRefreshTimeMap.containsKey(Utils.pathJoin(repoID, path))) {
            return true;
        }
        long lastRefreshTime = direntsRefreshTimeMap.get(Utils.pathJoin(repoID, path));

        if (Utils.now() < lastRefreshTime + REFRESH_EXPIRATION_MSECS) {
            return false;
        }
        return true;
    }

    public boolean isStarredFilesRefreshTimeout() {
        if (!direntsRefreshTimeMap.containsKey(PULL_TO_REFRESH_LAST_TIME_FOR_STARRED_FRAGMENT)) {
            return true;
        }
        long lastRefreshTime = direntsRefreshTimeMap.get(PULL_TO_REFRESH_LAST_TIME_FOR_STARRED_FRAGMENT);

        if (Utils.now() < lastRefreshTime + REFRESH_EXPIRATION_MSECS) {
            return false;
        }
        return true;
    }

    public void setDirsRefreshTimeStamp(String repoID, String path) {
        direntsRefreshTimeMap.put(Utils.pathJoin(repoID, path), Utils.now());
    }

    public void setReposRefreshTimeStamp(long timeStamp) {
        repoRefreshTimeStamp = timeStamp;
    }

    public void saveLastPullToRefreshTime(long lastUpdateTime, String whichFragment) {
        direntsRefreshTimeMap.put(whichFragment, lastUpdateTime);
    }

    public String getLastPullToRefreshTime(String whichFragment) {

        if (!direntsRefreshTimeMap.containsKey(whichFragment)) {
            return null;
        }

        Long objLastUpdate = direntsRefreshTimeMap.get(whichFragment);
        if (objLastUpdate == null) return null;

        long lastUpdate = direntsRefreshTimeMap.get(whichFragment);

        long diffTime = new Date().getTime() - lastUpdate;
        int seconds = (int) (diffTime / 1000);
        if (diffTime < 0) {
            return null;
        }
        if (seconds <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(SeadroidApplication.getAppContext().getString(R.string.pull_to_refresh_last_update));

        if (seconds < 60) {
            sb.append(SeadroidApplication.getAppContext().getString(R.string.pull_to_refresh_last_update_seconds_ago, seconds));
        } else {
            int minutes = (seconds / 60);
            if (minutes > 60) {
                int hours = minutes / 60;
                if (hours > 24) {
                    Date date = new Date(lastUpdate);
                    sb.append(ptrDataFormat.format(date));
                } else {
                    sb.append(SeadroidApplication.getAppContext().getString(R.string.pull_to_refresh_last_update_hours_ago, hours));
                }

            } else {
                sb.append(SeadroidApplication.getAppContext().getString(R.string.pull_to_refresh_last_update_minutes_ago, minutes));
            }
        }
        return sb.toString();
    }

    /**
     * search on server
     *
     * @param query query text
     * @param page  pass 0 to disable page loading
     * @return json format strings of searched result
     * @throws SeafException
     */
    public String search(String query, int page, int pageSize, String repoID, String path, List<String> ftype, String inputfexts, long startTime, long endTime, long maxSize, long minSize) throws SeafException {
        String json = sc.searchLibraries(query, page, pageSize, repoID, path, ftype, inputfexts, startTime, endTime, maxSize, minSize);
        return json;
    }

    public String search2(String query, SeafRepo repo) throws SeafException {
        String json = sc.searchLibraries2(query, repo);
        return json;
    }

    public ArrayList<SearchedFile> parseSearchResult(String json) {
        if (json == null)
            return null;

        try {
            JSONArray array = Utils.parseJsonArrayByKey(json, "results");
            if (array == null)
                return null;

            ArrayList<SearchedFile> searchedFiles = Lists.newArrayList();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                SearchedFile sf = SearchedFile.fromJson(obj);
                if (sf != null)
                    searchedFiles.add(sf);
            }
            return searchedFiles;
        } catch (JSONException e) {
            return null;
        }
    }

    public ArrayList<SeafDirent> parseSearchResultNew(String json) {
        if (json == null)
            return null;

        try {
            JSONArray array = Utils.parseJsonArrayByKey(json, "results");
            if (array == null)
                return null;

            ArrayList<SeafDirent> searchedFiles = Lists.newArrayList();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                SeafDirent sf = SeafDirent.fromJsonNew(obj);
                if (sf != null)
                    searchedFiles.add(sf);
            }
            return searchedFiles;
        } catch (JSONException e) {
            return null;
        }
    }

    private FileBlocks chunkFile(String encKey, String enkIv, String filePath) throws IOException {
        File file = new File(filePath);
        InputStream in = null;
        DataInputStream dis;
        OutputStream out = null;
        byte[] buffer = new byte[BUFFER_SIZE];
        FileBlocks seafBlock = new FileBlocks();
        try {
            in = new FileInputStream(file);
            dis = new DataInputStream(in);

            // Log.d(DEBUG_TAG, "file size " + file.length());
            int byteRead;
            while ((byteRead = dis.read(buffer, 0, BUFFER_SIZE)) != -1) {
                byte[] cipher;
                if (byteRead < BUFFER_SIZE)
                    cipher = Crypto.encrypt(buffer, byteRead, encKey, enkIv);
                else
                    cipher = Crypto.encrypt(buffer, encKey, enkIv);

                final String blkid = Crypto.sha1(cipher);
                File blk = new File(storageManager.getTempDir(), blkid);
                Block block = new Block(blkid, blk.getAbsolutePath(), blk.length(), 0L);
                seafBlock.blocks.add(block);
                out = new FileOutputStream(blk);
                out.write(cipher);
                out.close();
            }

            in.close();

            return seafBlock;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (out != null) out.close();
            if (in != null) in.close();
        }
    }

    public void uploadByBlocks(String repoName, String repoId, String dir,
                               String filePath, ProgressMonitor monitor,
                               boolean isUpdate, boolean isCopyToLocal) throws NoSuchAlgorithmException, IOException, SeafException {
        uploadByBlocksCommon(repoName, repoId, dir, filePath, monitor, isUpdate, isCopyToLocal);
    }

    private void uploadByBlocksCommon(String repoName, String repoID, String dir, String filePath,
                                      ProgressMonitor monitor, boolean isUpdate, boolean isCopyToLocal) throws NoSuchAlgorithmException, IOException, SeafException {


        final Pair<String, String> pair = getRepoEncKey(repoID);
        if (pair == null) return;
        final String encKey = pair.first;
        final String encIv = pair.second;
        // Log.d(DEBUG_TAG, "encKey " + encKey + " encIv " + encIv);
        if (TextUtils.isEmpty(encKey) || TextUtils.isEmpty(encIv)) {
            // TODO calculate them and continue
            throw SeafException.encryptException;
        }

        final FileBlocks chunkFile = chunkFile(encKey, encIv, filePath);
        if (chunkFile.blocks.isEmpty()) {
            throw SeafException.blockListNullPointerException;
        }

        String newFileID = sc.uploadByBlocks(repoID, dir, filePath, chunkFile.blocks, isUpdate, monitor);
        // Log.d(DEBUG_TAG, "uploadByBlocks " + newFileID);

        if (newFileID == null || newFileID.length() == 0) {
            return;
        }

        File srcFile = new File(filePath);
        String path = Utils.pathJoin(dir, srcFile.getName());
        File fileInRepo = getLocalRepoFile(repoName, repoID, path);

        if (isCopyToLocal) {
            if (!isUpdate) {
                // Copy the uploaded file to local repo cache
                try {
                    Utils.copyFile(srcFile, fileInRepo);
                } catch (IOException e) {
                    return;
                }
            }
        }
        // Update file cache entry
        addCachedFile(repoName, repoID, path, newFileID, fileInRepo, false);
    }

    public void updateRefreshPaths(String newPath) {
        if (!refreshPaths.contains(newPath)) {
            refreshPaths.add(newPath);
        }
    }

    public boolean checkRefreshPaths(String path) {
        if (refreshPaths.contains(path)) {
            refreshPaths.remove(path);
            return true;
        }
        return false;
    }

    private final List<Pair<String, String>> decompressedObjects = new ArrayList<>(); // obj_id, decompressed data
    private List<JSONObject> results = new ArrayList<>(); // List to store download file info
    private List<String> fsIdList = new ArrayList<>();

    public void getRepoDownloadInfo(String repoId) throws SeafException {
        if (!Utils.isNetworkOn()) {
            throw SeafException.networkException;
        }

        try {
            String json = sc.getRepoDownloadInfo(repoId);
            JSONObject object = Utils.parseJsonObject(json);
            String token = object.getString("token");

            json = sc.getCommitHead(repoId, token);
            object = Utils.parseJsonObject(json);
            String head = object.getString("head_commit_id");

            json = sc.getCommitObject(repoId, head, token);
            object = Utils.parseJsonObject(json);
            String rootId = object.getString("root_id");

            json = sc.getNeededFsIdList(repoId, head, token);
            fsIdList = parseFsIdList(json);

            // The server may not return all the objects we requested.
            // So we need to request again with remaining fsIdList.
            while (!fsIdList.isEmpty()) {
                byte[] result = sc.packFs(repoId, token, fsIdList);
                processObjects(result, result.length);
            }

            expandResults(rootId, ".");

        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "parse json error");
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "getRepoDownloadInfo exception", e);
        }
    }

    private List<String> parseFsIdList(String json) throws JSONException {
        List<String> idList = new ArrayList<>();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            idList.add(array.getString(i));
        }
        return idList;
    }

    private int processObjects(byte[] rspContent, int rspSize) {
        int p = 0; // Pointer to current position in rspContent
        int nRecv = 0; // Number of objects received
        int n = 0; // Track the number of bytes processed
        List<String> recvFsIdList = new ArrayList<>();

        while (n < rspSize) {
            // Calculate the size of the object header
            int headerSize = 40 + Integer.BYTES; // Corresponding to "!40sI"

            // Check if there's enough data for the object header
            if (n + headerSize > rspSize) {
                System.out.println("Incomplete object header, stopping.");
                return -1; // Incomplete data
            }

            // Parse the object header from binary data
            ObjectHeader hdr = ObjectHeader.fromBytes(ByteBuffer.wrap(rspContent, p, headerSize));

            // Move the pointer past the header
            p += headerSize;
            n += headerSize;

            // Check if there's enough data for the object body
            if (n + hdr.getObjSize() > rspSize) {
                System.out.println("Incomplete object package received. Expected size: " + hdr.getObjSize() + ", Available: " + (rspSize - n));
                return -1; // Incomplete data
            }

            // Extract the object data
            byte[] objectData = new byte[hdr.getObjSize()];
            System.arraycopy(rspContent, p, objectData, 0, hdr.getObjSize());

            // Increment the number of received objects
            nRecv++;

            byte[] decompressedData = seafDecompress(objectData);
            if (decompressedData != null) {
                String objId = new String(hdr.getObjId(), StandardCharsets.UTF_8);
                String decompressedStr = new String(decompressedData, StandardCharsets.UTF_8);
                decompressedObjects.add(new Pair<>(objId, decompressedStr));
                recvFsIdList.add(objId);
            } else {
                System.out.println("Decompression failed.");
            }

            // Move the pointer past the object data
            p += hdr.getObjSize();
            n += hdr.getObjSize();
        }

        // Remove received objects from fsIdList
        fsIdList.removeAll(recvFsIdList);
        return 0;
    }

    private void expandResults(String dirId, String parentPath) {
        final int MODE_DIR = 16384;
        final int MODE_FILE = 33188;

        String dirData = null;
        for (Pair<String, String> obj : decompressedObjects) {
            if (obj.first.equals(dirId)) {
                dirData = obj.second;
                break;
            }
        }

        if (dirData == null) {
            System.out.println("Directory data not found for id: " + dirId);
            return;
        }

        try {
            JSONObject dirJson = new JSONObject(dirData);
            if (dirJson.has("dirents")) {
                JSONArray dirents = dirJson.getJSONArray("dirents");
                for (int i = 0; i < dirents.length(); i++) {
                    JSONObject dirent = dirents.getJSONObject(i);
                    int mode = dirent.getInt("mode");

                    if (mode == MODE_FILE) {
                        JSONObject dent = new JSONObject(dirent.toString());
                        if (!parentPath.equals(".")) {
                            dent.put("name", parentPath + "/" + dirent.getString("name"));
                        }
                        results.add(dent);
                    } else if (mode == MODE_DIR) {
                        String newPath = parentPath.equals(".") ?
                                dirent.getString("name") :
                                parentPath + "/" + dirent.getString("name");
                        expandResults(dirent.getString("id"), newPath);
                    }
                }
            }
        } catch (JSONException e) {
            System.out.println("JSON decoding failed: " + e.getMessage());
        }
    }

    public static byte[] seafDecompress(byte[] inputData) {
        if (inputData.length == 0) {
            System.out.println("Empty input for zlib, invalid.");
            return null;
        }

        try {
            Inflater inflater = new Inflater();
            inflater.setInput(inputData);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final int CHUNK_SIZE = 16384; // Equivalent to ZLIB_BUF_SIZE

            byte[] buffer = new byte[CHUNK_SIZE];
            while (!inflater.finished()) {
                int decompressedLength = inflater.inflate(buffer);
                if (decompressedLength == 0 && inflater.needsInput()) {
                    break;
                }
                outputStream.write(buffer, 0, decompressedLength);
            }

            inflater.end();
            byte[] outputData = outputStream.toByteArray();
            outputStream.close();
            return outputData;

        } catch (DataFormatException e) {
            System.out.println("Failed to decompress: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            return null;
        }
    }

    public String getToken() throws SeafException {

        String json = sc.getToken();
        if (json == null)
            return null;

        try {
            // may throw ClassCastException
            JSONObject jsonObject = Utils.parseJsonObject(json);
            if (jsonObject != null) {
                String token = jsonObject.getString("token");
                return  token;
            }
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "parse json error");
            return null;
        } catch (Exception e) {
            // other exception, for example ClassCastException
            Log.e(DEBUG_TAG, "parseToken exception");
            return null;
        }

        return null;
    }
}