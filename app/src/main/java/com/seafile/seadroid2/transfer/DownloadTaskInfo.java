package com.seafile.seadroid2.transfer;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.data.SeafRepoTag;
import com.seafile.seadroid2.util.SystemSwitchUtils;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *  download task info
 */
public class DownloadTaskInfo extends TransferTaskInfo {

    public final String pathInRepo;
    public final long fileSize, finished;
    public final boolean thumbnail;

    /**
     * Constructor of DownloadTaskInfo
     *  @param account Current login Account instance
     * @param taskID TransferTask id
     * @param state TransferTask state, value is one of <strong>INIT, TRANSFERRING, FINISHED, CANCELLED, FAILED</strong> of {@link TaskState}
     * @param repoID Repository id
     * @param repoName Repository name
     * @param pathInRepo File path in Repository
     * @param localPath Local path
     * @param fileSize File total size
     * @param finished File downloaded size
     * @param err Exception instance of {@link SeafException}
     */
    public DownloadTaskInfo(Account account, int taskID, TaskState state,
                            String repoID, String repoName, String pathInRepo,
                            String localPath, long fileSize, long finished, long startTime, boolean thumbnail, SeafException err) {
        super(account, taskID, state, repoID, repoName, localPath, startTime, true, err);

        this.pathInRepo = pathInRepo;
        this.fileSize = fileSize;
        this.finished = finished;
        this.thumbnail = thumbnail;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || (obj.getClass() != this.getClass()))
            return false;

        DownloadTaskInfo a = (DownloadTaskInfo) obj;
        if (!super.equals(a))
            return false;

        if (a.pathInRepo == null)
            return false;

        return a.pathInRepo.equals(this.pathInRepo) && a.fileSize == this.fileSize && a.finished == this.finished;
    }

    public static DownloadTaskInfo fromJson(Account account, JSONObject obj) throws JSONException {
        return new DownloadTaskInfo(
                account,
                obj.getInt("task_id"),
                TaskState.valueOf(obj.getString("state")),
                obj.getString("repo_id"),
                obj.getString("repo_name"),
                obj.getString("path_in_repo"),
                obj.getString("local_file_path"),
                obj.getLong("file_size"),
                obj.getLong("finished"),
                obj.getLong("start_time"),
                obj.optBoolean("thumbnail", false),
                SeafException.fromJsonString(obj.optString("err")));
    }

    public static String toString(DownloadTaskInfo info) {
        return  "{" +
                "task_id:'" + info.taskID + '\'' +
                ", state:'" + info.state.name() + '\'' +
                ", repo_id:'" + info.repoID + '\'' +
                ", repo_name:'" + info.repoName + '\'' +
                ", path_in_repo:'" + info.pathInRepo + '\'' +
                ", local_file_path:'" + info.localFilePath + '\'' +
                ", file_size:'" + info.fileSize + '\'' +
                ", finished:'" + info.finished + '\'' +
                ", start_time:'" + info.startTime + '\'' +
                ", thumbnail:'" + info.thumbnail + '\'' +
                ", err:" + (info.err == null ? "''" : info.err.toJsonString()) +
                "}";
    }
}
