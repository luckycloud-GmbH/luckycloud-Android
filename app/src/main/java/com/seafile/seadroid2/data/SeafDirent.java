package com.seafile.seadroid2.data;

import android.util.Log;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.util.PinyinUtils;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

public class SeafDirent implements SeafItem, Serializable {
    public static final long serialVersionUID = 0L;
    private static final String DEBUG_TAG = "SeafDirent";
    public enum DirentType {DIR, FILE}
    public String permission;
    public String id;
    public DirentType type;
    public String name;
    public long size;// size of file, 0 if type is dir
    public long mtime;// last modified timestamp
    public List<SeafFileTag> fileTags;
    public String repoID;
    public String path;
    public boolean isSearchedFile;

    static SeafDirent fromJson(JSONObject obj) {
        SeafDirent dirent = new SeafDirent();
        try {
            dirent.id = obj.getString("id");
            dirent.name = obj.getString("name");
            dirent.mtime = obj.getLong("mtime");
            dirent.permission = obj.getString("permission");
            String type = obj.getString("type");
            if (type.equals("file")) {
                dirent.type = DirentType.FILE;
                dirent.size = obj.getLong("size");
                dirent.fileTags = Lists.newArrayListWithCapacity(0);
                if (obj.has("file_tags")) {
                    JSONArray jsonArray = obj.getJSONArray("file_tags");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        dirent.fileTags.add(SeafFileTag.fromJson(jsonObject));
                    }
                }
            } else
                dirent.type = DirentType.DIR;
            dirent.repoID = "";
            dirent.path = "";
            dirent.isSearchedFile = false;
            return dirent;
        } catch (JSONException e) {
            Log.d(DEBUG_TAG, e.getMessage());
            return null;
        }
    }

    static SeafDirent fromJsonNew(JSONObject obj) {
        SeafDirent dirent = new SeafDirent();
        try {
            dirent.id = "";
            dirent.name = obj.getString("name");
            dirent.repoID = obj.getString("repo_id");
            dirent.mtime = obj.getLong("last_modified");
            dirent.path = obj.getString("fullpath");
            dirent.permission = "rw";
            boolean type = obj.getBoolean("is_dir");
            if (!type) {
                dirent.type = DirentType.FILE;
                dirent.size = obj.getLong("size");
                dirent.fileTags = Lists.newArrayListWithCapacity(0);
                if (obj.has("file_tags")) {
                    JSONArray jsonArray = obj.getJSONArray("file_tags");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        dirent.fileTags.add(SeafFileTag.fromJson(jsonObject));
                    }
                }
            } else
                dirent.type = DirentType.DIR;
            dirent.isSearchedFile = true;
            return dirent;
        } catch (JSONException e) {
            Log.d(DEBUG_TAG, e.getMessage());
            return null;
        }
    }

    static String toString(SeafDirent dirent) {
        String typeString = "";
        if (dirent.type == DirentType.DIR) {
            typeString = ", type: 'dir', is_dir: 'true'";
        } else {
            StringBuilder tagsString = new StringBuilder("[");
            for (int i = 0; i < dirent.fileTags.size(); i++) {
                SeafFileTag tag = dirent.fileTags.get(i);
                tagsString.append(
                        "{" +
                                "tag_color:'" + tag.tag_color + '\'' +
                                ", tag_name:'" + tag.tag_name + '\'' +
                                ", repo_tag_id:'" + tag.repo_tag_id + '\'' +
                                ", file_tag_id='" + tag.file_tag_id + '\'' +
                                '}'
                );
                if (i < dirent.fileTags.size() - 1) {
                    tagsString.append(",");
                }
            }
            tagsString.append("]");
            typeString = ", type: 'file', is_dir: 'false'" +
                    ", size:'" + dirent.size + '\'' +
                    ", file_tags:" + tagsString;
        }
        return  "{" +
                "id:'" + dirent.id + '\'' +
                ", name:'" + dirent.name + '\'' +
                ", mtime:'" + dirent.mtime + '\'' +
                ", last_modified:'" + dirent.mtime + '\'' +
                ", fullpath: '" + dirent.path + '\'' +
                ", permission:'" + dirent.permission + '\'' +
                typeString +
                '}';
    }

    public boolean isDir() {
        return (type == DirentType.DIR);
    }

    public long getFileSize() {
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
        if (isDir()) {
            if (!hasWritePermission()) {
                return R.drawable.ic_folder_read_only;
            } else {
                return R.drawable.ic_folder;
            }
        }
        return Utils.getFileIcon(name);
    }

    public List<SeafFileTag> getFileTags() {
        return fileTags;
    }

    public void setFileTags(List<SeafFileTag> tags) {
        if (fileTags == null)
            fileTags = Lists.newArrayListWithCapacity(0);
        else
            fileTags.clear();
        fileTags.addAll(tags);
    }

    public boolean hasWritePermission() {
        return permission.indexOf('w') != -1;
    }

    /**
     * SeafDirent last modified time comparator class
     */
    public static class DirentLastMTimeComparator implements Comparator<SeafDirent> {

        @Override
        public int compare(SeafDirent itemA, SeafDirent itemB) {
            return (int) (itemA.mtime - itemB.mtime);
        }
    }

    /**
     * SeafDirent name comparator class
     */
    public static class DirentNameComparator implements Comparator<SeafDirent> {

        @Override
        public int compare(SeafDirent itemA, SeafDirent itemB) {
            // get the first character unicode from each file name
            int unicodeA = itemA.name.codePointAt(0);
            int unicodeB = itemB.name.codePointAt(0);

            String strA, strB;

            // both are Chinese words
            if ((19968 < unicodeA && unicodeA < 40869) && (19968 < unicodeB && unicodeB < 40869)) {
                strA = PinyinUtils.toPinyin(SeadroidApplication.getAppContext(), itemA.name).toLowerCase();
                strB = PinyinUtils.toPinyin(SeadroidApplication.getAppContext(), itemB.name).toLowerCase();
            } else if ((19968 < unicodeA && unicodeA < 40869) && !(19968 < unicodeB && unicodeB < 40869)) {
                // itemA is Chinese and itemB is English
                return 1;
            } else if (!(19968 < unicodeA && unicodeA < 40869) && (19968 < unicodeB && unicodeB < 40869)) {
                // itemA is English and itemB is Chinese
                return -1;
            } else {
                // both are English words
                strA = itemA.name.toLowerCase();
                strB = itemB.name.toLowerCase();
            }

            return strA.compareTo(strB);
        }
    }
}