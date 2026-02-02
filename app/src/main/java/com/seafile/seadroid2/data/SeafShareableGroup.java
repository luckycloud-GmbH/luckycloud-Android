package com.seafile.seadroid2.data;

import org.json.JSONObject;

import java.util.Comparator;

/**
 * Seafile share link bean
 */
public class SeafShareableGroup {

    public static final String DEBUG_TAG = SeafShareableGroup.class.getSimpleName();


    private String id;
    private String parent_group_id;
    private String name;
    private String owner;
    private String created_at;


    public static SeafShareableGroup fromJson(JSONObject obj) {
        SeafShareableGroup seafPermission = new SeafShareableGroup();
        seafPermission.id = obj.optString("id");
        seafPermission.parent_group_id = obj.optString("parent_group_id");
        seafPermission.name = obj.optString("name");
        seafPermission.owner = obj.optString("owner");
        seafPermission.created_at = obj.optString("created_at");
        return seafPermission;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParent_group_id() {
        return parent_group_id;
    }

    public void setParent_group_id(String parent_group_id) {
        this.parent_group_id = parent_group_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }



    @Override
    public String toString() {
        return "SeafPermission{" +
                "id='" + id + '\'' +
                ", parent_group_id='" + parent_group_id + '\'' +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", created_at='" + created_at + '\'' +
                '}';
    }

    public static class ShareGroupNameComparator implements Comparator<SeafShareableGroup> {

        @Override
        public int compare(SeafShareableGroup g1, SeafShareableGroup g2) {
            return g1.getName().toLowerCase().compareTo(
                    g2.getName().toLowerCase());
        }
    }
}
