package com.seafile.seadroid2.data;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.util.PinyinUtils;
import com.seafile.seadroid2.util.SystemSwitchUtils;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * SeafRepo: A Seafile library
 * @author plt
 */
public class SeafBackup {
    public String   id;     // repo id
    public String   name;
    public String   sourcePath;
    public String   repoName;
    public String   parentDir;
    public long     totalSize;
    public long     startTime;
    public long     endTime;
    public String   state;
    public String   error;

    public static SeafBackup fromJson(JSONObject obj) throws JSONException{
        SeafBackup backup = new SeafBackup();
        backup.id = obj.getString("id");
        backup.name = obj.getString("name");
        backup.sourcePath = obj.getString("source_path");
        backup.repoName = obj.getString("repo_name");
        backup.parentDir = obj.getString("parent_dir");
        backup.totalSize = obj.getLong("total_size");
        backup.startTime = obj.getLong("start_time");
        backup.endTime = obj.getLong("end_time");
        backup.state = obj.getString("state");
        backup.error = obj.optString("error");
        return backup;
    }

    public static String toString(SeafBackup backup) {
        return  "{" +
                "id:'" + backup.id + '\'' +
                ", name:'" + backup.name + '\'' +
                ", source_path:'" + backup.sourcePath + '\'' +
                ", repo_name:'" + backup.repoName + '\'' +
                ", parent_dir:'" + backup.parentDir + '\'' +
                ", total_size:'" + backup.totalSize + '\'' +
                ", start_time:'" + backup.startTime + '\'' +
                ", end_time:'" + backup.endTime + '\'' +
                ", state:'" + backup.state + '\'' +
                ", error:'" + backup.error + '\'' +
                '}';
    }

    public SeafBackup() {
    }

    public SeafBackup(
            String id,
            String name,
            String sourcePath,
            String repoName,
            String parentDir,
            long totalSize,
            long startTime,
            long endTime,
            String state,
            String error) {
        this.id = id;
        this.name = name;
        this.sourcePath = sourcePath;
        this.repoName = repoName;
        this.parentDir = parentDir;
        this.totalSize = totalSize;
        this.startTime = startTime;
        this.endTime = endTime;
        this.state = state;
        this.error = error;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSourcePath() {
        return sourcePath;
    }
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getRepoName() {
        return repoName;
    }
    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getParentDir() {
        return parentDir;
    }
    public void setParentDir(String parentDir) {
        this.parentDir = parentDir;
    }

    public long getTotalSize() {
        return totalSize;
    }
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getStartTime() {
        return startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
}
