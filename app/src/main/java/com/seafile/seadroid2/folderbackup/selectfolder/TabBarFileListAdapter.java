package com.seafile.seadroid2.folderbackup.selectfolder;

import android.app.Activity;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seafile.seadroid2.R;

import java.util.List;
import java.util.Map;

public class TabBarFileListAdapter extends RecyclerView.Adapter<TabbarFileViewHolder> {

    private List<TabBarFileBean> mListData;
    private Map<String, String> mStorageNames;
    private Context mContext;
    private OnFileItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnFileItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public TabBarFileListAdapter(Activity context, List<TabBarFileBean> listData) {
        mListData = listData;
        mContext = context;
    }

    @Override
    public TabbarFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_tabbar_files_list, parent, false);
        return new TabbarFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TabbarFileViewHolder holder, int positon) {
        final TabBarFileBean entity = mListData.get(positon);
        String fileName = entity.getFileName().replaceAll("[^\\p{Print}]", "");
        if (mStorageNames.get(entity.getFilePath()) != null) {
            fileName = mStorageNames.get(entity.getFilePath());
        }
        holder.tvName.setText(fileName);
        holder.llRoot.setOnClickListener(new View.OnClickListener() {
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

    public void updateListData(List<TabBarFileBean> mListData, Map<String, String> mStorageNames) {
        this.mListData = mListData;
        this.mStorageNames = mStorageNames;
    }
}
