package com.seafile.seadroid2.data;

import android.util.Log;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.util.PinyinUtils;
import com.seafile.seadroid2.util.SystemSwitchUtils;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;

public class SeafStarredFile implements SeafItem {
    public enum FileType { DIR, FILE };
    private static final String DEBUG_TAG = "SeafStarredFile";

    private String repoID;
    private long mtime;
    private String path;
    private String obj_name;
    private boolean repo_encrypted;
    private FileType type;
    private long size;    // size of file, 0 if type is dir
    private String permission;
    private String repoName;
    public List<SeafFileTag> fileTags;

    public static SeafStarredFile fromJson(JSONObject obj) {
        SeafStarredFile starredFile = new SeafStarredFile();
        try {
            starredFile.repoID = obj.optString("repo_id");
            if (obj.has("mISO")) {
                starredFile.mtime = obj.getLong("mISO");
            } else {
                starredFile.mtime = SystemSwitchUtils.parseISODateTime(obj.optString("mtime"));
            }
            starredFile.path = obj.optString("path");
            starredFile.obj_name = obj.optString("obj_name");
            starredFile.size = obj.optLong("size");
            starredFile.repo_encrypted = obj.optBoolean("repo_encrypted");
            starredFile.permission = obj.has("permission") ?  obj.optString("permission") : null;
            starredFile.repoName = obj.optString("repo_name");
            boolean type = obj.optBoolean("is_dir");
            if (!type) {
                starredFile.type = FileType.FILE;
                starredFile.fileTags = Lists.newArrayListWithCapacity(0);
                if (obj.has("file_tags")) {
                    JSONArray jsonArray = obj.getJSONArray("file_tags");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        starredFile.fileTags.add(SeafFileTag.fromJson(jsonObject));
                    }
                }
            } else
                starredFile.type = FileType.DIR;
            return starredFile;
        } catch (Exception e) {
            Log.d(DEBUG_TAG, e.getMessage());
            return null;
        }
    }

    public static String toString(SeafStarredFile file) {
        String typeString = "";
        if (file.type == FileType.DIR) {
            typeString = ", is_dir: 'true'";
        } else {
            StringBuilder tagsString = new StringBuilder("[");
            for (int i = 0; i < file.fileTags.size(); i++) {
                SeafFileTag tag = file.fileTags.get(i);
                tagsString.append(
                        "{" +
                                "tag_color:'" + tag.tag_color + '\'' +
                                ", tag_name:'" + tag.tag_name + '\'' +
                                ", repo_tag_id:'" + tag.repo_tag_id + '\'' +
                                ", file_tag_id='" + tag.file_tag_id + '\'' +
                                '}'
                );
                if (i < file.fileTags.size() - 1) {
                    tagsString.append(",");
                }
            }
            tagsString.append("]");
            typeString = ", is_dir: 'false'" +
                    ", file_tags:" + tagsString;
        }
        return  "{" +
                "repo_id:'" + file.repoID + '\'' +
                ", mISO:'" + file.mtime + '\'' +
                ", path:'" + file.path + '\'' +
                ", obj_name:'" + file.obj_name + '\'' +
                ", size:'" + file.size + '\'' +
                ", repo_encrypted:'" + file.repo_encrypted + '\'' +
                typeString +
                ", repo_name:'" + file.repoName + '\'' +
                ", permission:'" + file.permission + '\'' +
                '}';
    }

    public String getRepoID() {
        return repoID;
    }

    public void setRepoID(String repoID) {
        this.repoID = repoID;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getMtime() {
        return mtime;
    }

    public void setMtime(long mtime) {
        this.mtime = mtime;
    }

    public String getObj_name() {
        return obj_name;
    }

    public void setObj_name(String obj_name) {
        this.obj_name = obj_name;
    }

    public boolean isDir() {
        return (type == FileType.DIR);
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public boolean isRepo_encrypted() {
        return repo_encrypted;
    }

    public void setRepo_encrypted(boolean repo_encrypted) {
        this.repo_encrypted = repo_encrypted;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getTitle() {
//        return path.substring(path.lastIndexOf('/') + 1);
        return getObj_name();
    }

    @Override
    public String getSubtitle() {
        String timestamp = Utils.translateCommitTime(mtime * 1000);
        return timestamp;

//        if (isDir())
//            return timestamp;
//        return Utils.readableFileSize(size) + ", " + timestamp;
    }

    @Override
    public int getIcon() {
        if (isDir())
            return R.drawable.ic_folder;
        return Utils.getFileIcon(getTitle());
    }

    public void setFileTags(List<SeafFileTag> tags) {
        if (fileTags == null)
            fileTags = Lists.newArrayListWithCapacity(0);
        else
            fileTags.clear();
        fileTags.addAll(tags);
    }

    /**
     * SeafStarredFile last modified time comparator class
     */
    public static class StarredLastMTimeComparator implements Comparator<SeafStarredFile> {

        @Override
        public int compare(SeafStarredFile itemA, SeafStarredFile itemB) {
            return (int) (itemA.mtime - itemB.mtime);
        }
    }

    /**
     * SeafStarredFile name comparator class
     */
    public static class StarredNameComparator implements Comparator<SeafStarredFile> {

        @Override
        public int compare(SeafStarredFile itemA, SeafStarredFile itemB) {
            // get the first character unicode from each file name
            int unicodeA = itemA.getTitle().codePointAt(0);
            int unicodeB = itemB.getTitle().codePointAt(0);

            String strA, strB;

            // both are Chinese words
            if ((19968 < unicodeA && unicodeA < 40869) && (19968 < unicodeB && unicodeB < 40869)) {
                strA = PinyinUtils.toPinyin(SeadroidApplication.getAppContext(), itemA.getTitle()).toLowerCase();
                strB = PinyinUtils.toPinyin(SeadroidApplication.getAppContext(), itemB.getTitle()).toLowerCase();
            } else if ((19968 < unicodeA && unicodeA < 40869) && !(19968 < unicodeB && unicodeB < 40869)) {
                // itemA is Chinese and itemB is English
                return 1;
            } else if (!(19968 < unicodeA && unicodeA < 40869) && (19968 < unicodeB && unicodeB < 40869)) {
                // itemA is English and itemB is Chinese
                return -1;
            } else {
                // both are English words
                strA = itemA.getTitle().toLowerCase();
                strB = itemB.getTitle().toLowerCase();
            }

            return strA.compareTo(strB);
        }
    }
}
