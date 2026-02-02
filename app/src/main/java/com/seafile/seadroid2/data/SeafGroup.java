package com.seafile.seadroid2.data;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.SettingsManager;

import java.util.Collections;
import java.util.List;

public class SeafGroup implements SeafItem {
    private String name;
    private List<SeafRepo> repos = Lists.newArrayList();
    public boolean isGroupRepo;
    public boolean isPersonalRepo;
    public boolean isSharedRepo;

    public SeafGroup(String name, boolean isPersonalRepo, boolean isSharedRepo, boolean isGroupRepo) {
        this.name = name;
        this.isPersonalRepo = isPersonalRepo;
        this.isSharedRepo = isSharedRepo;
        this.isGroupRepo = isGroupRepo;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSubtitle() {
        return null;
    }

    @Override
    public int getIcon() {
        return 0;
    }

    public List<SeafRepo> getRepos() {
        return repos;
    }

    public void addIfAbsent(SeafRepo repo) {
        if (!repos.contains(repo))
            this.repos.add(repo);
    }

    /**
     * sort collections by repository name or last modified time
     */
    public void sortByType(int type, int order) {
        if (type == SettingsManager.SORT_BY_NAME) {
            Collections.sort(repos, new SeafRepo.RepoNameComparator());
            if (order == SettingsManager.SORT_ORDER_DESCENDING) {
                Collections.reverse(repos);
            }
        } else if (type == SettingsManager.SORT_BY_LAST_MODIFIED_TIME) {
            Collections.sort(repos, new SeafRepo.RepoLastMTimeComparator());
            if (order == SettingsManager.SORT_ORDER_DESCENDING) {
                Collections.reverse(repos);
            }
        }
    }

}
