package com.seafile.seadroid2.data;

import android.util.Log;

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
public class SeafRepo implements SeafItem {
    public String   id;     // repo id
    public String   name;
    public String   owner;
    public long     mtime;    // the last modification time

    public boolean  isGroupRepo;
    public boolean  isPersonalRepo;
    public boolean  isSharedRepo;
    public boolean  isPublicRepo;
    public String groupName;
    public boolean  encrypted;
    public String   permission;
    public String   magic;
    public String   encKey;
    public long     size;
    public String   root; // the id of root directory
    public boolean  isFolder;
    public List<SeafRepoTag> repoTags;
    public List<String> selectedRepoTagIDs;

    public static SeafRepo fromJson(JSONObject obj) throws JSONException{
        SeafRepo repo = new SeafRepo();
        repo.id = obj.getString("repo_id");
        repo.name = obj.getString("repo_name");
        repo.owner = "";
        if (obj.has("owner_email")) {
            repo.owner = obj.getString("owner_email");
        }
        if (obj.has("owner_contact_email")) {
            repo.owner = obj.getString("owner_contact_email");
        }
        if (obj.has("modifier_contact_email")) {
            repo.owner = obj.getString("modifier_contact_email");
        }
        repo.permission = obj.getString("permission");
        String last_modified = obj.getString("last_modified");
        if (last_modified.isEmpty()) {
            repo.mtime = Utils.now();
        } else {
            repo.mtime = SystemSwitchUtils.parseISODateTime(last_modified);
        }
        repo.encrypted = obj.getBoolean("encrypted");
        repo.root = obj.getString("salt");
        repo.isFolder = obj.has("is_folder") ? obj.optBoolean("is_folder") : true;
        repo.size = obj.getLong("size");
        repo.isGroupRepo = obj.getString("type").equals("group");
        repo.isPersonalRepo = obj.getString("type").equals("mine");
        repo.isSharedRepo = obj.getString("type").equals("shared");
        repo.isPublicRepo = obj.getString("type").equals("public");
        if (repo.isGroupRepo) repo.groupName = obj.getString("group_name");
        repo.magic = obj.optString("magic");
        repo.encKey = obj.optString("random_key");
        repo.repoTags = Lists.newArrayListWithCapacity(0);
        if (obj.has("repo_tags")) {
            JSONArray array = obj.getJSONArray("repo_tags");
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                SeafRepoTag repoTag = SeafRepoTag.fromJson(jsonObject);
                repo.repoTags.add(repoTag);
            }
        }
        repo.selectedRepoTagIDs = Lists.newArrayListWithCapacity(0);
        if (obj.has("selected_repo_tag_ids")) {
            JSONArray array = obj.getJSONArray("selected_repo_tag_ids");
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                if (jsonObject != null)
                    repo.selectedRepoTagIDs.add(jsonObject.toString());
            }
        }
        return repo;
    }

    public static String toString(SeafRepo repo) {
        StringBuilder tagsString = new StringBuilder("[");
        if (repo.repoTags != null) {
            for (int i = 0; i < repo.repoTags.size(); i++) {
                SeafRepoTag tag = repo.repoTags.get(i);
                tagsString.append(
                        "{" +
                                "tag_color:'" + tag.tag_color + '\'' +
                                ", tag_name:'" + tag.tag_name + '\'' +
                                ", repo_id:'" + tag.repo_id + '\'' +
                                ", repo_tag_id:" + tag.repo_tag_id +
                                ", files_count='" + tag.files_count + '\'' +
                                '}'
                );
                if (i < repo.repoTags.size() - 1) {
                    tagsString.append(",");
                }
            }
        }
        tagsString.append("]");

        StringBuilder selectedRepoTagIDsString = new StringBuilder("[");
        for (int i = 0; i < repo.selectedRepoTagIDs.size(); i++) {
            String selectedRepoTagID = repo.selectedRepoTagIDs.get(i);
            tagsString.append(selectedRepoTagID);
            if (i < repo.selectedRepoTagIDs.size() - 1) {
                tagsString.append(",");
            }
        }
        selectedRepoTagIDsString.append("]");

        String typeString = "";
        if (repo.isSharedRepo)
            typeString = "shared";
        if (repo.isPersonalRepo)
            typeString = "mine";
        if (repo.isGroupRepo)
            typeString = "group";
        if (repo.isPublicRepo)
            typeString = "public";

        return  "{" +
                "repo_id:'" + repo.id + '\'' +
                ", repo_name:'" + repo.name + '\'' +
                ", owner_email:'" + repo.owner + '\'' +
                ", permission:'" + repo.permission + '\'' +
                ", last_modified:'" + SystemSwitchUtils.timestamp2ISO(repo.mtime) + '\'' +
                ", encrypted:'" + repo.encrypted + '\'' +
                ", salt:'" + repo.root + '\'' +
                ", is_folder:'" + repo.isFolder + '\'' +
                ", size:'" + repo.size + '\'' +
                ", type:'" + typeString + '\'' +
                (repo.isGroupRepo? ", group_name:'" + repo.groupName + '\'' : "") +
                ", magic:'" + repo.magic + '\'' +
                ", random_key:'" + repo.encKey + '\'' +
                ((repo.repoTags != null)? (", repo_tags:" + tagsString) : "") +
                ", selected_repo_tag_ids:" + selectedRepoTagIDsString +
                '}';
    }

    public SeafRepo() {
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRootDirID() {
        return root;
    }

    public List<SeafRepoTag> getRepoTags() {
        return repoTags;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSubtitle() {
        return Utils.translateCommitTime(mtime * 1000);
    }

    @Override
    public int getIcon() {
        if (isFolder)
            if (!hasWritePermission()) {
                return R.drawable.ic_folder_read_only;
            } else {
                return R.drawable.ic_folder;
            }
        if (encrypted && !hasWritePermission())
            return R.drawable.repo_readonly_encrypted;
        if (encrypted)
            return R.drawable.repo_encrypted;
        if (!hasWritePermission())
            return R.drawable.repo_readonly;

        return R.drawable.repo;
    }

    public boolean canLocalDecrypt() {
        return encrypted && SettingsManager.instance().isEncryptEnabled();
    }

    public boolean hasWritePermission() {
        return permission.indexOf('w') != -1;
    }

    /**
     * Repository last modified time comparator class
     */
    public static class RepoLastMTimeComparator implements Comparator<SeafRepo> {

        @Override
        public int compare(SeafRepo itemA, SeafRepo itemB) {
            return (int) (itemA.mtime - itemB.mtime);
        }
    }

    /**
     * Repository name comparator class
     */
    public static class RepoNameComparator implements Comparator<SeafRepo> {

        @Override
        public int compare(SeafRepo itemA, SeafRepo itemB) {
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

    public void setRepoTags(List<SeafRepoTag> tags) {
        if (repoTags == null) {
            repoTags = Lists.newArrayListWithCapacity(0);
        }
        repoTags.clear();
        repoTags.addAll(tags);
        if (repoTags == null) {
            repoTags = new ArrayList<>();
        }
    }
}
