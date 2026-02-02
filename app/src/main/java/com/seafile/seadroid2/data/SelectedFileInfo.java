package com.seafile.seadroid2.data;

import org.json.JSONException;
import org.json.JSONObject;

public class SelectedFileInfo {
    public String fileName;
    public long fileSize;
    public long mtime;
    public String repoName;
    public String repoID;
    public String dirPath;
    public String filePath;

    public SelectedFileInfo() {
    }

    public SelectedFileInfo(
            String fileName,
            long fileSize,
            long mtime,
            String repoName,
            String repoID,
            String dirPath,
            String filePath) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mtime = mtime;
        this.repoName = repoName;
        this.repoID = repoID;
        this.dirPath = dirPath;
        this.filePath = filePath;
    }

    public static SelectedFileInfo fromJson(JSONObject obj) throws JSONException {
        SelectedFileInfo info = new SelectedFileInfo();
        info.fileName = obj.getString("file_name");
        info.fileSize = obj.getLong("file_size");
        info.mtime = obj.getLong("mtime");
        info.repoName = obj.getString("repo_name");
        info.repoID = obj.getString("repo_id");
        info.dirPath = obj.getString("dir_path");
        info.filePath = obj.getString("file_path");
        return info;
    }

    public static String toString(SelectedFileInfo info) {
        return  "{" +
                "file_name:'" + info.fileName + '\'' +
                ", file_size:'" + info.fileSize + '\'' +
                ", mtime:'" + info.mtime + '\'' +
                ", repo_name:'" + info.repoName + '\'' +
                ", repo_id:'" + info.repoID + '\'' +
                ", dir_path:'" + info.dirPath + '\'' +
                ", file_path:'" + info.filePath + '\'' +
                '}';
    }
}