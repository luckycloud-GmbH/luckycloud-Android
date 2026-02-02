package com.seafile.seadroid2.data;

import android.util.Log;

import com.seafile.seadroid2.ui.fragment.ShareGroupFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

/**
 * Seafile share link bean
 */
public class SeafSharedItem {

    public static final String DEBUG_TAG = SeafSharedItem.class.getSimpleName();


    private String share_type;
    private JSONObject group_info;
    private String group_info_id;
    private String group_info_name;
    private JSONObject user_info;
    private String user_info_name;
    private String user_info_nickname;
    private String user_info_contact_email;
    private String user_info_avatar_url;
    private String permission;
    private String is_admin;

    public static SeafSharedItem fromJson(JSONObject obj) throws JSONException {
        SeafSharedItem seafSharedItems = new SeafSharedItem();
        seafSharedItems.share_type = obj.optString("id");
        seafSharedItems.permission = obj.optString("permission");
        seafSharedItems.is_admin = obj.optString("is_admin");
        try {
            seafSharedItems.group_info = obj.getJSONObject("group_info");
            seafSharedItems.group_info_id = seafSharedItems.group_info.optString("id");
            seafSharedItems.group_info_name = seafSharedItems.group_info.optString("name");
        } catch (Exception ignored) {
        }
        try {
            seafSharedItems.user_info = obj.getJSONObject("user_info");
            seafSharedItems.user_info_name = seafSharedItems.user_info.optString("name");
            seafSharedItems.user_info_nickname = seafSharedItems.user_info.optString("nickname");
            seafSharedItems.user_info_contact_email = seafSharedItems.user_info.optString("contact_email");
            seafSharedItems.user_info_avatar_url = seafSharedItems.user_info.optString("avatar_url");
        } catch (Exception ignored) {
        }
        return seafSharedItems;
    }

    public String getShare_type() {
        return share_type;
    }

    public void setShare_type(String share_type) {
        this.share_type = share_type;
    }

    public JSONObject getGroup_info() {
        return group_info;
    }

    public void setGroup_info(JSONObject group_info) {
        this.group_info = group_info;
    }

    public String getGroup_info_id() {
        return group_info_id;
    }

    public void setGroup_info_id(String group_info_id) {
        this.group_info_id = group_info_id;
    }

    public String getGroup_info_name() {
        return group_info_name;
    }

    public void setGroup_info_name(String group_info_name) {
        this.group_info_name = group_info_name;
    }

    public String getUser_info_name() {
        return user_info_name;
    }

    public void setUser_info_name(String user_info_name) {
        this.user_info_name = user_info_name;
    }

    public String getUser_info_nickname() {
        return user_info_nickname;
    }

    public void setUser_info_nickname(String user_info_nickname) {
        this.user_info_nickname = user_info_nickname;
    }

    public String getUser_info_contact_email() {
        return user_info_contact_email;
    }

    public void setUser_info_contact_email(String user_info_contact_email) {
        this.user_info_contact_email = user_info_contact_email;
    }

    public String getUser_info_avatar_url() {
        return user_info_avatar_url;
    }

    public void setUser_info_avatar_url(String user_info_avatar_url) {
        this.user_info_avatar_url = user_info_avatar_url;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getIs_admin() {
        return is_admin;
    }

    public void setIs_admin(String is_admin) {
        this.is_admin = is_admin;
    }


    @Override
    public String toString() {
        return "SeafSharedItem{" +
                "share_type='" + share_type + '\'' +
                (share_type == ShareGroupFragment.SHARE_TYPE_GROUP?
                        ", group_info: {id='" + group_info_id + '\'' +
                                ", name='" + group_info_name + '\''
                        :
                        ", user_info: {name='" + user_info_name + '\'' +
                                ", nickname='" + user_info_nickname + '\'' +
                                ", contact_email='" + user_info_contact_email + '\'' +
                                ", avatar_url='" + user_info_avatar_url + '\''
                ) +
                "}, permission='" + permission + '\'' +
                ", is_admin='" + is_admin + '\'' +
                '}';
    }

    public static class GroupNameComparator implements Comparator<SeafSharedItem> {

        @Override
        public int compare(SeafSharedItem p1, SeafSharedItem p2) {
            return p1.getGroup_info_name().toLowerCase().compareTo(
                    p2.getGroup_info_name().toLowerCase());
        }
    }

    public static class UserNameComparator implements Comparator<SeafSharedItem> {

        @Override
        public int compare(SeafSharedItem p1, SeafSharedItem p2) {
            return p1.getUser_info_nickname().toLowerCase().compareTo(
                    p2.getUser_info_nickname().toLowerCase());
        }
    }
}
