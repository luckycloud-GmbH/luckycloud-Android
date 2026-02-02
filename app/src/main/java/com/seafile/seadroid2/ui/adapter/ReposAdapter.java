package com.seafile.seadroid2.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.cameraupload.CameraUploadConfigActivity;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.folderbackup.FolderBackupConfigActivity;
import com.seafile.seadroid2.ui.activity.SeafilePathChooserActivity;

import java.util.List;

/**
 * Base ReposAdapter
 */
public abstract class ReposAdapter extends BaseAdapter {

    public static String Seafile_Path_Chooser_Activity = "SeafilePathChooserActivity";
    public static String Camera_Upload_Config_Activity = "CameraUploadConfigActivity";
    public static String Folder_Backup_Config_Activity = "FolderBackupConfigActivity";

    protected List<SeafRepo> repos = Lists.newArrayList();
    protected boolean onlyShowWritableRepos;
    protected String encryptedRepoId;
    protected Context mContext;
    protected String mCurrentActivity;

    public ReposAdapter(boolean onlyShowWritableRepos, String encryptedRepoId, Context context, String currentActivity) {
        this.onlyShowWritableRepos = onlyShowWritableRepos;
        this.encryptedRepoId = encryptedRepoId;
        this.mContext = context;
        this.mCurrentActivity = currentActivity;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public boolean areAllReposSelectable() {
        return false;
    }

    public  List<SeafRepo> getData() {
        return repos;
    }

    public void setRepos(List<SeafRepo> seafRepos) {
        repos.clear();
        for (SeafRepo repo: seafRepos) {
            if (onlyShowWritableRepos && !repo.hasWritePermission()) {
                continue;
            }
            if (encryptedRepoId != null && !repo.id.equals(encryptedRepoId)) {
                continue;
            }
            repos.add(repo);
        }
        notifyDataSetChanged();
    }

    protected abstract int getChildLayout();

    protected abstract int getChildCardId();

    protected abstract int getChildTitleId();

    protected abstract int getChildSubTitleId();

    protected abstract int getChildIconId();

    protected abstract int getChildActionId();

    protected abstract SeafRepo getChildSeafRepo(int position);

    protected abstract void showRepoSelectedIcon(int position, ImageView imageView);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;

        SeafRepo repo = getChildSeafRepo(position);

        if (convertView == null) {
            view = LayoutInflater.from(mContext)
                    .inflate(getChildLayout(), null);
            CardView viewCard = (CardView) view.findViewById(getChildCardId());
            TextView title = (TextView) view.findViewById(getChildTitleId());
            TextView subtitle = (TextView) view.findViewById(getChildSubTitleId());
            ImageView icon = (ImageView) view.findViewById(getChildIconId());
            ImageView action = (ImageView) view.findViewById(getChildActionId());
            viewHolder = new ViewHolder(viewCard, title, subtitle, icon, action);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(repo.getTitle());
        viewHolder.subtitle.setText(repo.getSubtitle());
        viewHolder.icon.setImageResource(repo.getIcon());
        viewHolder.action.setVisibility(View.INVISIBLE);
        viewHolder.viewCard.setOnClickListener(v -> {
            if (mCurrentActivity.equals(Seafile_Path_Chooser_Activity))
                ((SeafilePathChooserActivity)mContext).onListItemClick(position);
            else if (mCurrentActivity.equals(Camera_Upload_Config_Activity))
                ((CameraUploadConfigActivity)mContext)
                        .getCloudLibraryFragment()
                        .getAccountOrReposSelectionFragment()
                        .onListItemClick(position);
            else
                ((FolderBackupConfigActivity)mContext)
                        .getCloudLibFragment()
                        .onListItemClick(position);
        });

        showRepoSelectedIcon(position, viewHolder.action);
        return view;
    }

    private static class ViewHolder {
        CardView viewCard;
        TextView title, subtitle;
        ImageView icon, action;

        public ViewHolder(CardView viewCard, TextView title, TextView subtitle, ImageView icon, ImageView action) {
            super();
            this.viewCard = viewCard;
            this.icon = icon;
            this.action = action;
            this.title = title;
            this.subtitle = subtitle;
        }
    }
}
