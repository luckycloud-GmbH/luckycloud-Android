package com.seafile.seadroid2.folderbackup.selectfolder;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seafile.seadroid2.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileListAdapter extends RecyclerView.Adapter<FileListViewHolder> {
    private List<FileBean> mListData;
    private Map<String, String> mStorageName = new HashMap<>();
    private Context mContext;
    private OnFileItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnFileItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public FileListAdapter(Activity context, List<FileBean> listData) {
        mListData = listData;
        mContext = context;
    }

    @Override
    public FileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_files_list, parent, false);
        FileListViewHolder fileListViewHolder = new FileListViewHolder(view);
        return fileListViewHolder;
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void onBindViewHolder(final FileListViewHolder holder, int positon) {
        final FileBean fileBean = mListData.get(positon);
        holder.checkBoxFile.setChecked(fileBean.isChecked());
        holder.tvFileName.setText(fileBean.getFileName());
        if (mStorageName.get(fileBean.getFilePath()) != null) {
            holder.tvFileName.setText(mStorageName.get(fileBean.getFilePath()));
            if (mStorageName.get(fileBean.getFilePath()).equals(mContext.getString(R.string.internal_storage))) {
                holder.imgvFiletype.setImageResource(R.drawable.ic_phone_android);
            } else {
                holder.imgvFiletype.setImageResource(R.drawable.ic_sd_storage);
            }
        } else {
            holder.imgvFiletype.setImageResource(fileBean.getFileImgType());
        }
        boolean isFile = fileBean.isFile();
        if (isFile) {
            holder.tvFileDetail.setText(String.format(mContext.getString(R.string.folder_file_item_size), fileBean.getSize()));
        } else {
            holder.tvFileDetail.setText(String.format(mContext.getString(R.string.folder_file_item_describe),
                    fileBean.getChildrenFileNumber(), fileBean.getChildrenDirNumber()));
        }
        if (!isFile && mStorageName.get(fileBean.getFilePath()) == null) {
            holder.checkBoxFile.setVisibility(View.VISIBLE);
        } else {
            holder.checkBoxFile.setVisibility(View.GONE);
        }

        holder.checkBoxFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onCheckBoxClick(holder.checkBoxFile, holder.getAdapterPosition());
                }
            }
        });

        holder.layoutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mListData == null) {
            return 0;
        } else {
            return mListData.size();
        }
    }

    public void updateListData(List<FileBean> mListData, Map<String, String> mStorageName) {
        this.mListData = mListData;
        if (mStorageName != null) {
            this.mStorageName = mStorageName;
        }
    }
}