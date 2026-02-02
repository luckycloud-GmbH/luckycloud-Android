package com.seafile.seadroid2.data;

import org.json.JSONObject;
import java.util.Comparator;

/**
 * Seafile share link bean
 */
public class SeafPermission {

    public static final String DEBUG_TAG = SeafPermission.class.getSimpleName();


    private String id;
    private String name;
    private String description;
    private String permission;

    public static SeafPermission fromJson(JSONObject obj) {
        SeafPermission seafPermission = new SeafPermission();
        seafPermission.id = obj.optString("id");
        seafPermission.name = obj.optString("name");
        seafPermission.description = obj.optString("description");
        seafPermission.permission = obj.optString("permission");
        return seafPermission;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }


    @Override
    public String toString() {
        return "SeafPermission{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", permission='" + permission + '\'' +
                '}';
    }

    public static class PermissionNameComparator implements Comparator<SeafPermission> {

        @Override
        public int compare(SeafPermission p1, SeafPermission p2) {
            return p1.getName().toLowerCase().compareTo(
                    p2.getName().toLowerCase());
        }
    }
}
