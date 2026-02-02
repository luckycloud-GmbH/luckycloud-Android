package com.seafile.seadroid2.data;

import android.util.Log;

import com.seafile.seadroid2.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * SeafRepo: A Seafile library
 * @author plt
 */
public class SeafFileTag {
    public String tag_color;
    public String tag_name;
    public String repo_tag_id;
    public String file_tag_id;

    static SeafFileTag fromJson(JSONObject obj) throws JSONException{
        SeafFileTag tag = new SeafFileTag();
        tag.tag_color = obj.getString("tag_color");
        tag.tag_name = obj.getString("tag_name");
        tag.repo_tag_id = obj.getString("repo_tag_id");
        tag.file_tag_id = obj.getString("file_tag_id");
        return tag;
    }

    public SeafFileTag() {
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

    public String getRepo_tag_id() {
        return repo_tag_id;
    }

    public void setRepo_tag_id(String repo_tag_id) {
        this.repo_tag_id = repo_tag_id;
    }

    public String getFile_tag_id() {
        return file_tag_id;
    }

    public void setFile_tag_id(String file_tag_id) {
        this.file_tag_id = file_tag_id;
    }

    @Override
    public String toString() {
        return "{" +
                "tag_color='" + tag_color + '\'' +
                ", tag_name='" + tag_name + '\'' +
                ", repo_tag_id='" + repo_tag_id + '\'' +
                ", file_tag_id='" + file_tag_id + '\'' +
                '}';
    }
}
