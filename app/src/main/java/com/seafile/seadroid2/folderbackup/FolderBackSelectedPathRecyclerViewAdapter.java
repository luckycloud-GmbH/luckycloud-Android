package com.seafile.seadroid2.folderbackup;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.CollectionUtils;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.util.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FolderBackSelectedPathRecyclerViewAdapter extends RecyclerView.Adapter<FolderBackSelectedPathRecyclerViewAdapter.SearchItemViewHolder> {
    private final FolderBackupConfigActivity mActivity;
    private final FolderBackupSelectedPathFragment mFragment;
    private final List<String> mItemList = new ArrayList<>();

    private final WeakReference<Context> contextWeakReference;

    public FolderBackSelectedPathRecyclerViewAdapter(FolderBackupConfigActivity activity, FolderBackupSelectedPathFragment fragment) {
        this.mActivity = activity;
        this.mFragment = fragment;
        this.contextWeakReference = new WeakReference<>(activity);
    }

    @NonNull
    @Override
    public SearchItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contextWeakReference.get()).inflate(R.layout.item_text_more, parent, false);
        return new SearchItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderBackSelectedPathRecyclerViewAdapter.SearchItemViewHolder viewHolder, int position) {
        final int p = position;
        viewHolder.title.setText(mItemList.get(p));
        viewHolder.title.setSelected(true);

        viewHolder.icon.setOnClickListener(v -> {
            mFragment.showEditItemPopup(v, p);
        });

        viewHolder.itemView.setOnClickListener(v -> {
            Dialog dialog = Utils.CustomDialog(mActivity);
            dialog.setContentView(R.layout.dialog_backup_folder);
            TextView versionText = dialog.findViewById(R.id.folder_path_text);
            versionText.setText(mItemList.get(p));
            dialog.show();
        });
    }

    public List<String> getItemList() {
        return mItemList;
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public void notifyDataChanged(List<String> list) {
        mItemList.clear();
        if (!CollectionUtils.isEmpty(list)) {
            mItemList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public static class SearchItemViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public View icon;

        public SearchItemViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.title);
            icon = (View) view.findViewById(R.id.more);
        }
    }

    public void deleteBackupPath(int position) {
        mItemList.remove(position);
        notifyDataSetChanged();

        mFragment.selectPaths.remove(position);
        mActivity.setFolderPathList(mItemList);
    }
}
