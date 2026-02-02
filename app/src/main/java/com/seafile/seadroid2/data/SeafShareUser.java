package com.seafile.seadroid2.data;

import org.json.JSONObject;

/**
 * Seafile share link bean
 */
public class SeafShareUser {

    public static final String DEBUG_TAG = SeafShareUser.class.getSimpleName();


    private String email;
    private String avatar_url;
    private String name;
    private String contact_email;


    public static SeafShareUser fromJson(JSONObject obj) {
        SeafShareUser seafUser = new SeafShareUser();
        seafUser.email = obj.optString("email");
        seafUser.avatar_url = obj.optString("avatar_url");
        seafUser.name = obj.optString("name");
        seafUser.contact_email = obj.optString("contact_email");
        return seafUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact_email() {
        return contact_email;
    }

    public void setContact_email(String contact_email) {
        this.contact_email = contact_email;
    }


    @Override
    public String toString() {
        return "SeafUser{" +
                "email='" + email + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                ", name='" + name + '\'' +
                ", contact_email='" + contact_email + '\'' +
                '}';
    }
}
