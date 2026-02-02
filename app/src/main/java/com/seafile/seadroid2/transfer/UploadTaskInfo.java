package com.seafile.seadroid2.transfer;

import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.account.Account;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * upload task info
 */
public class UploadTaskInfo extends TransferTaskInfo {

    public final String parentDir;
    public final long uploadedSize, totalSize;
    public final boolean isUpdate, isCopyToLocal;
    public int version;
    public String source;

    /**
     * Constructor of UploadTaskInfo
     *  @param account Current login Account instance
     * @param taskID TransferTask id
     * @param state TransferTask state, value is one of <strong>INIT, TRANSFERRING, FINISHED, CANCELLED, FAILED</strong> of {@link TaskState}
     * @param repoID Repository id
     * @param repoName Repository name
     * @param parentDir Parent directory of the file
     * @param localPath Local path
     * @param isUpdate Force to update files if true
     * @param isCopyToLocal Copy files to SD card if true
     * @param uploadedSize File uploaded size
     * @param totalSize File total size
     * @param err Exception instance of {@link SeafException}
     */
    public UploadTaskInfo(Account account,
                          int taskID,
                          TaskState state,
                          String repoID,
                          String repoName,
                          String parentDir,
                          String localPath,
                          boolean isUpdate,
                          boolean isCopyToLocal,
                          long uploadedSize,
                          long totalSize,
                          long startTime,
                          String source,
                          SeafException err) {

        super(account, taskID, state, repoID, repoName, localPath, startTime, false, err);

        this.parentDir = parentDir;
        this.uploadedSize = uploadedSize;
        this.totalSize = totalSize;
        this.isUpdate = isUpdate;
        this.isCopyToLocal = isCopyToLocal;
        this.source = source;
    }

    public static UploadTaskInfo fromJson(Account account, JSONObject obj) throws JSONException {
        return new UploadTaskInfo(
                account,
                obj.getInt("task_id"),
                TaskState.valueOf(obj.getString("state")),
                obj.getString("repo_id"),
                obj.getString("repo_name"),
                obj.getString("parent_dir"),
                obj.getString("local_file_path"),
                obj.getBoolean("is_update"),
                obj.getBoolean("is_copy_to_local"),
                obj.getLong("uploaded_size"),
                obj.getLong("total_size"),
                obj.getLong("start_time"),
                obj.getString("source"),
                SeafException.fromJsonString(obj.optString("err")));
    }

    public static String toString(UploadTaskInfo info) {
        return  "{" +
                "task_id:'" + info.taskID + '\'' +
                ", state:'" + info.state.name() + '\'' +
                ", repo_id:'" + info.repoID + '\'' +
                ", repo_name:'" + info.repoName + '\'' +
                ", parent_dir:'" + info.parentDir + '\'' +
                ", local_file_path:'" + info.localFilePath + '\'' +
                ", is_update:'" + info.isUpdate + '\'' +
                ", is_copy_to_local:'" + info.isCopyToLocal + '\'' +
                ", uploaded_size:'" + info.uploadedSize + '\'' +
                ", total_size:'" + info.totalSize + '\'' +
                ", start_time:'" + info.startTime + '\'' +
                ", source:'" + info.source + '\'' +
                ", err:" + (info.err == null ? "''" : info.err.toJsonString()) +
                "}";
    }
}
