package com.seafile.seadroid2.data;

import android.util.Log;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.util.SystemSwitchUtils;
import com.seafile.seadroid2.util.Utils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Searched file entity
 */
public class SearchedFile implements SeafItem {
    private static final String DEBUG_TAG = "SearchedFile";

    public enum FileType {DIR, FILE}
    private String name;
    private String repoID;
    private long mtime;
    private String path;
    private FileType type;
    private long size;    // size of file, 0 if type is dir
    private String oid;

    static SearchedFile fromJson(JSONObject obj) {
        SearchedFile searchedFile = new SearchedFile();
        try {
            searchedFile.name = obj.getString("name");
            searchedFile.repoID = obj.getString("repo_id");
            searchedFile.mtime = obj.getLong("last_modified");
            searchedFile.path = obj.getString("fullpath");
            searchedFile.size = obj.optLong("size");
            searchedFile.oid = obj.optString("oid");
            boolean type = obj.getBoolean("is_dir");
            searchedFile.type = type ? FileType.DIR : FileType.FILE;

            return searchedFile;
        } catch (JSONException e) {
            Log.d(DEBUG_TAG, searchedFile.path + e.getMessage());
            return null;
        }
    }

    public static SearchedFile fromJson2(JSONObject obj, SeafRepo repo) {
        SearchedFile searchedFile = new SearchedFile();
        try {
            searchedFile.name = Utils.fileNameFromPath(obj.getString("path"));
            searchedFile.repoID = repo.id;
            searchedFile.mtime = SystemSwitchUtils.parseISODateTime(obj.getString("mtime"));
            searchedFile.path = obj.getString("path");
            searchedFile.size = obj.optLong("size");
            searchedFile.oid = "";
            boolean type = !obj.getString("type").equals("file");
            searchedFile.type = type ? FileType.DIR : FileType.FILE;

            return searchedFile;
        } catch (JSONException e) {
            Log.d(DEBUG_TAG, searchedFile.path + e.getMessage());
            return null;
        }
    }

    public static JSONObject fromString(SearchedFile searchedFile) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", searchedFile.name);
            jsonObject.put("repo_id", searchedFile.repoID);
            jsonObject.put("last_modified", searchedFile.mtime);
            jsonObject.put("fullpath", searchedFile.path);
            jsonObject.put("size", searchedFile.size);
            jsonObject.put("oid", searchedFile.oid);
            jsonObject.put("is_dir", searchedFile.type.equals(FileType.DIR));

            return jsonObject;
        } catch (JSONException e) {
            Log.d(DEBUG_TAG, searchedFile.path + e.getMessage());
            return null;
        }
    }

    public boolean isDir() {
        return (type == FileType.DIR);
    }

    public String getRepoID() {
        return repoID;
    }

    public String getPath() {
        return path;
    }

    public long getMTime() {
        return mtime;
    }

    public long getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSubtitle() {
        String timestamp = Utils.translateCommitTime(mtime * 1000);
        if (isDir())
            return timestamp;
        return Utils.readableFileSize(size) + ", " + timestamp;
    }

    @Override
    public int getIcon() {
        if (isDir())
            return R.drawable.ic_folder;
        return Utils.getFileIcon(getTitle());
    }

}

