package com.seafile.seadroid2.data;

import android.util.Log;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.util.PinyinUtils;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

/**
 * SeafRepo: A Seafile library
 * @author plt
 */
public class SeafRepoTag {
    public String tag_color;
    public String tag_name;
    public String repo_id;
    public String repo_tag_id;
    public String files_count;

    static SeafRepoTag fromJson(JSONObject obj) throws JSONException{
        SeafRepoTag tag = new SeafRepoTag();
        tag.tag_color = obj.getString("tag_color");
        tag.tag_name = obj.getString("tag_name");
        tag.repo_id = obj.getString("repo_id");
        tag.repo_tag_id = obj.getString("repo_tag_id");
        tag.files_count = obj.getString("files_count");
        return tag;
    }

    public SeafRepoTag() {
    }

    public String getTag_color() {
        return tag_color;
    }

    public void setTag_color(String tag_color) {
        this.tag_color = tag_color;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    public String getRepo_id() {
        return repo_id;
    }

    public void setRepo_id(String repo_id) {
        this.repo_id = repo_id;
    }

    public String getRepo_tag_id() {
        return repo_tag_id;
    }

    public void setRepo_tag_id(String repo_tag_id) {
        this.repo_tag_id = repo_tag_id;
    }

    public String getFiles_count() {
        return files_count;
    }

    public void setFiles_count(String files_count) {
        this.files_count = files_count;
    }

    @Override
    public String toString() {
        return "{" +
                "tag_color='" + tag_color + '\'' +
                ", tag_name='" + tag_name + '\'' +
                ", repo_id='" + repo_id + '\'' +
                ", repo_tag_id='" + repo_tag_id + '\'' +
                ", files_count='" + files_count + '\'' +
                '}';
    }
}
