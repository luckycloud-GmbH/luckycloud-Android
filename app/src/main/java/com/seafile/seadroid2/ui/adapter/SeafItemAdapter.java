package com.seafile.seadroid2.ui.adapter;

import static com.seafile.seadroid2.util.Utils.PUBLIC_REPO;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.config.GlideLoadConfig;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafCachedFile;
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.data.SeafFileTag;
import com.seafile.seadroid2.data.SeafGroup;
import com.seafile.seadroid2.data.SeafItem;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.data.SeafRepoTag;
import com.seafile.seadroid2.transfer.DownloadTaskInfo;
import com.seafile.seadroid2.transfer.TaskState;
import com.seafile.seadroid2.ui.NavContext;
import com.seafile.seadroid2.ui.WidgetUtils;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.fragment.ReposFragment;
import com.seafile.seadroid2.ui.fragment.SettingsFragment;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.GlideApp;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeafItemAdapter extends RecyclerView.Adapter<SeafItemAdapter.ViewHolder> {

    private static final String DEBUG_TAG = "SeafItemAdapter";
    private ArrayList<SeafItem> items;
    public ArrayList<SeafItem> showItems;
    public Map<String, String> imagePathMap;
    private BrowserActivity mActivity;
    private ReposFragment mFragment;
    private boolean repoIsEncrypted;
    private boolean actionModeOn;
    private NavContext nav;
    private DataManager dataManager;
    private NavContext navContext;

    private SparseBooleanArray mSelectedItemsIds;
    private List<Integer> mSelectedItemsPositions = Lists.newArrayList();
    private List<SeafDirent> mSelectedItemsValues = Lists.newArrayList();

    private int gridFileType = SettingsManager.GRID_BY_LIST;
    public int columns = 1;
    private int sortType;
    private int sortOrder;
    private boolean repoPersonal;
    private boolean repoGroup;
    private boolean repoShared;

    /**
     * DownloadTask instance container
     **/
    private List<DownloadTaskInfo> mDownloadTaskInfos;
    private boolean isNotifying = false;

    public SeafItemAdapter(BrowserActivity activity, ReposFragment fragment) {
        mFragment = fragment;
        mActivity = activity;
        items = Lists.newArrayListWithCapacity(0);
        showItems = Lists.newArrayListWithCapacity(0);
        mSelectedItemsIds = new SparseBooleanArray();
        nav = mActivity.getNavContext();
        dataManager = mActivity.getDataManager();
        navContext = mActivity.getNavContext();
        mDownloadTaskInfos = Lists.newArrayList();
        imagePathMap = new HashMap<>();
    }

    /**
     * To refresh downloading status of {@link com.seafile.seadroid2.ui.fragment.ReposFragment#mRecyclerView},
     * use this method to update data set.
     * <p>
     * This method should be called after the "Download folder" menu was clicked.
     *
     */

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = getInflate(viewType);
        return new SeafItemAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        SeafItem item = showItems.get(position);

        viewHolder.viewCard.setVisibility(View.VISIBLE);
        viewHolder.groupLayout.setVisibility(View.GONE);
        viewHolder.nameText.setVisibility(View.GONE);
        viewHolder.tagColorRecycler.setVisibility(View.GONE);
        if (item instanceof SeafRepo) {
            setRepoView((SeafRepo) item, viewHolder, position);
        } else if (item instanceof SeafGroup) {
            setGroupView((SeafGroup) item, viewHolder);
        } else if (item instanceof SeafCachedFile) {
            setCacheView((SeafCachedFile) item, viewHolder, position);
        } else {
            setDirentView((SeafDirent) item, viewHolder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (repoPersonal || !repoGroup || repoShared)
            return position;
        if (showItems.get(position) instanceof SeafGroup)
            return -1;
        int index = 0;
        for (int i = position - 1; i >= 0; i--) {
            if (showItems.get(i) instanceof SeafGroup) {
                break;
            } else {
                index++;
            }
        }
        return index;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return showItems.size();
    }

    public List<DownloadTaskInfo> getDownloadTaskList() {
        return mDownloadTaskInfos;
    }

    public void setDownloadTaskList(List<DownloadTaskInfo> newList) {
        // if (!equalLists(newList, mDownloadTaskInfos)) {
            List<DownloadTaskInfo> originalDownloadTaskInfos = mDownloadTaskInfos;
            this.mDownloadTaskInfos = newList;
            for (DownloadTaskInfo info:newList) {
                if (!originalDownloadTaskInfos.contains(info)) {
                    if (info.thumbnail && info.state == TaskState.FINISHED) {
                        String imagePath = imagePathMap.get(info.pathInRepo);
                        if (imagePath == null) {
                            try {
                                File thumbFile = dataManager.getLocalEncRepoThumbFile(info.repoName, info.repoID, info.pathInRepo);
                                if (thumbFile.exists()) {
                                    imagePathMap.put(info.pathInRepo, thumbFile.getAbsolutePath());
                                    mFragment.thumbnailImagesCountInEnc += 1;
                                    Log.e("ThumbImagesCount: ", String.valueOf(mFragment.thumbnailImagesCountInEnc));
                                }
                            } catch (RuntimeException e) {

                            }
                        }
                    }
                    for (int i = 0; i < showItems.size(); i++) {
                        SeafItem item = showItems.get(i);
                        if (item instanceof SeafDirent) {
                            SeafDirent dirent = (SeafDirent) item;
                            if (!dirent.isDir()) {
                                String repoID;
                                String filePath;
                                SeafRepo repo;

                                if (dirent.isSearchedFile) {
                                    repo = dataManager.getCachedRepoByID(dirent.repoID);
                                    repoID = repo.getID();
                                    filePath = dirent.path;
                                } else {
                                    repoID = nav.getRepoID();
                                    filePath = Utils.pathJoin(nav.getDirPath(), dirent.name);
                                }

                                if (filePath.equals(info.pathInRepo) && repoID.equals(info.repoID)) {
                                    notifyItemChanged(i);
                                }
                            }
                        }
                    }
                }
            }
            // redraw the list
            // notifyChanged();
        // }
    }

    /**
     * Compare two lists
     *
     * @param newList
     * @param oldList
     * @return true if the two lists are equal,
     * false, otherwise.
     */
    public boolean equalLists(List<DownloadTaskInfo> newList, List<DownloadTaskInfo> oldList) {
        if (newList == null && oldList == null)
            return true;

        if ((newList == null && oldList != null)
                || newList != null && oldList == null
                || newList.size() != oldList.size())
            return false;

        return newList.equals(oldList);
    }

    private boolean includingSelectedTagID(SeafItem entry) {
        boolean flag = false;
        if (entry instanceof SeafDirent) {
            SeafDirent dirent = (SeafDirent) entry;
            if (dirent.isDir())
                flag = true;
            else {
                SeafRepo repo = dataManager.getCachedRepoByID(navContext.getRepoID());
                if (repo == null) {
                    flag = true;
                } else {
                    if (repo.selectedRepoTagIDs.size() == 0) {
                        flag = true;
                    } else {
                        for (SeafFileTag fileTag : dirent.getFileTags()) {
                            if (repo.selectedRepoTagIDs.contains(fileTag.getRepo_tag_id())) {
                                flag = true;
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            flag = true;
        }
        return flag;
    }

    public void addEntry(SeafItem entry) {
        items.add(entry);
        if (includingSelectedTagID(entry)) {
            showItems.add(entry);
        }
        // Collections.sort(items);
        notifyChanged();
    }

    public void add(SeafItem entry) {
        items.add(entry);
        if (includingSelectedTagID(entry)) {
            showItems.add(entry);
        }
    }

    public void notifyChanged() {
        if (isNotifying) return;
        isNotifying = true;
        if (!mFragment.mRecyclerView.isComputingLayout()) {
            notifyDataSetChanged();
        }
        isNotifying = false;
    }

    public void setGridFileType(int type) {
        gridFileType = type;
        columns = SettingsManager.instance().getGridFilesColumns(type);
    }

    public void setRepoType(String type, boolean value) {
        switch (type) {
            case SettingsManager.REPO_TYPE_PERSONAL:
                repoPersonal = value;
                SettingsManager.instance().saveRepoTypePersonalPref(value);
                break;
            case SettingsManager.REPO_TYPE_GROUP:
                repoGroup = value;
                SettingsManager.instance().saveRepoTypeGroupPref(value);
                break;
            case SettingsManager.REPO_TYPE_SHARED:
                repoShared = value;
                SettingsManager.instance().saveRepoTypeSharedPref(value);
                break;
            default:
                break;
        }
    }

    public SeafItem getItem(int position) {
        return showItems.get(position);
    }

    public void clearImagePathMap() {
        if (!imagePathMap.isEmpty()) {
            imagePathMap.clear();
            notifyChanged();
        }
    }

    public void removeImagePathMap(String key) {
        if (imagePathMap.containsKey(key)) {
            imagePathMap.remove(key);
            notifyChanged();
        }
    }

    public void setImagePathMap(Map<String, String> _imagePathMap) {
        if (_imagePathMap != null) {
            if (!_imagePathMap.isEmpty()) {
                boolean needUpdate = false;
                for (Map.Entry<String, String> entry : _imagePathMap.entrySet()) {
                    if (!imagePathMap.containsKey(entry.getKey())) {
                        imagePathMap.put(entry.getKey(), entry.getValue());
                        needUpdate = true;
                    }
                }
                if (needUpdate)
                    notifyChanged();
            }
        }
    }

    public void setItems(List<SeafDirent> dirents) {
        items.clear();
        items.addAll(dirents);

        showItems.clear();
        for (SeafDirent dirent: dirents) {
            if (includingSelectedTagID(dirent)) {
                showItems.add(dirent);
            }
        }
        this.mSelectedItemsIds.clear();
        this.mSelectedItemsPositions.clear();
        this.mSelectedItemsValues.clear();
    }

    public void deselectAllItems() {
        mSelectedItemsIds.clear();
        mSelectedItemsPositions.clear();
        mSelectedItemsValues.clear();
        notifyChanged();
    }

    public void selectAllItems() {
        mSelectedItemsIds.clear();
        mSelectedItemsPositions.clear();
        mSelectedItemsValues.clear();
        for (int i = 0; i < showItems.size(); i++) {
            mSelectedItemsIds.put(i, true);
            mSelectedItemsPositions.add(i);
            mSelectedItemsValues.add((SeafDirent) showItems.get(i));
        }
        notifyChanged();
    }

    public void clear() {
        items.clear();
        showItems.clear();
    }

    public boolean isEnable(int position) {
        SeafItem item = showItems.get(position);
        return !(item instanceof SeafGroup);
    }

    private View getInflate(int position) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.list_item_entry, null);
        if (repoGroup && !repoPersonal && !repoShared && position == -1)
            return view;
        switch (gridFileType) {
            case SettingsManager.GRID_BY_SMALL_TILE:
                switch (position % 3) {
                    case 1:
                        view = LayoutInflater.from(mActivity).inflate(R.layout.list_item_entry_3_1, null);
                        break;
                    case 2:
                        view = LayoutInflater.from(mActivity).inflate(R.layout.list_item_entry_3_2, null);
                        break;
                    default:
                        view = LayoutInflater.from(mActivity).inflate(R.layout.list_item_entry_3_0, null);
                        break;
                }
                break;
            case SettingsManager.GRID_BY_BIG_TILE:
                switch (position % 2) {
                    case 1:
                        view = LayoutInflater.from(mActivity).inflate(R.layout.list_item_entry_2_1, null);
                        break;
                    default:
                        view = LayoutInflater.from(mActivity).inflate(R.layout.list_item_entry_2_0, null);
                        break;
                }
                break;
            case SettingsManager.GRID_BY_MINIMAL_LIST:
                view = LayoutInflater.from(mActivity).inflate(R.layout.list_item_minimal_entry, null);
                break;
            default:
                break;
        }
        return view;
    }

    private void setRepoView(final SeafRepo repo, ViewHolder viewHolder, int position) {
        viewHolder.repoTypeImage.setVisibility(View.VISIBLE);
        int repoTypeIcon = R.drawable.ic_repo_personal;
        if (repo.isGroupRepo)
            repoTypeIcon = R.drawable.ic_repo_group;
        if (repo.isSharedRepo || repo.isPublicRepo)
            repoTypeIcon = R.drawable.ic_repo_shared;
        viewHolder.repoTypeImage.setImageDrawable(mActivity.getResources().getDrawable(repoTypeIcon));

        viewHolder.viewCard.setOnClickListener(v -> {
            mFragment.listItemClick(position);
        });

        viewHolder.viewCard.setOnLongClickListener(v -> {
            mFragment.listItemLongClick(position);
            return true;
        });

        viewHolder.action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.showRepoBottomSheet(repo);
            }
        });

        viewHolder.multiSelect.setVisibility(View.GONE);
        viewHolder.downloadStatusIcon.setVisibility(View.GONE);
        viewHolder.progressBar.setVisibility(View.GONE);
        viewHolder.title.setText(repo.getTitle());
        viewHolder.subtitle.setText(repo.getSubtitle());
        viewHolder.icon.setImageResource(repo.getIcon());
        if (repo.hasWritePermission()) {
            viewHolder.action.setVisibility(View.VISIBLE);
        } else {
            viewHolder.action.setVisibility(View.INVISIBLE);
        }
    }

    private void setGroupView(SeafGroup group, ViewHolder viewHolder) {
        viewHolder.viewCard.setVisibility(View.GONE);
        viewHolder.groupLayout.setVisibility(View.VISIBLE);
        String groupTitle = group.getTitle();
        if ("Organization".equals(groupTitle)) {
            groupTitle = mActivity.getString(R.string.shared_with_all);
        }
        viewHolder.groupTitle.setText(groupTitle);
    }

    private void setDirentView(final SeafDirent dirent, ViewHolder viewHolder, final int position) {
        viewHolder.repoTypeImage.setVisibility(View.GONE);
        viewHolder.viewCard.requestLayout();
        viewHolder.viewCard.setOnClickListener(v -> {
            mFragment.listItemClick(position);
        });

        viewHolder.viewCard.setOnLongClickListener(v -> {
            mFragment.listItemLongClick(position);
            return true;
        });

        viewHolder.action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dirent.isDir()) {
                    if (dirent.isSearchedFile && dirent.path.equals("/")) {
                        SeafRepo repo = dataManager.getCachedRepoByID(dirent.repoID);
                        mActivity.showRepoBottomSheet(repo);
                        return;
                    }
                    mActivity.showDirBottomSheet(dirent.getTitle(), (SeafDirent) getItem(position));
                } else
                    mActivity.showFileBottomSheet(dirent.getTitle(), (SeafDirent) getItem(position));
            }
        });

        if (actionModeOn) {
            viewHolder.multiSelect.setVisibility(View.VISIBLE);
            if (mSelectedItemsIds.get(position)) {
                viewHolder.multiSelect.setImageResource(R.drawable.ic_multi_select_item_checked);
                viewHolder.viewCard.setCardBackgroundColor(mActivity.getResources().getColor(R.color.repo_item_select_color));
            } else {
                viewHolder.multiSelect.setImageResource(R.drawable.ic_multi_select_item_unchecked);
                viewHolder.viewCard.setCardBackgroundColor(mActivity.getResources().getColor(R.color.dialog_msg_background));
            }

            viewHolder.multiSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mSelectedItemsIds.get(position)) {
                        viewHolder.multiSelect.setImageResource(R.drawable.ic_multi_select_item_checked);
                        viewHolder.viewCard.setCardBackgroundColor(mActivity.getResources().getColor(R.color.repo_item_select_color));
                        mSelectedItemsIds.put(position, true);
                        mSelectedItemsPositions.add(position);
                        mSelectedItemsValues.add(dirent);
                    } else {
                        viewHolder.multiSelect.setImageResource(R.drawable.ic_multi_select_item_unchecked);
                        viewHolder.viewCard.setCardBackgroundColor(mActivity.getResources().getColor(R.color.dialog_msg_background));
                        mSelectedItemsIds.delete(position);
                        mSelectedItemsPositions.remove(Integer.valueOf(position));
                        mSelectedItemsValues.remove(dirent);
                    }

                    mActivity.onItemSelected();
                }
            });
        } else {
            viewHolder.multiSelect.setVisibility(View.GONE);
            viewHolder.viewCard.setCardBackgroundColor(mActivity.getResources().getColor(R.color.dialog_msg_background));
        }

        viewHolder.title.setText(dirent.isSearchedFile? filePath(dirent) : dirent.getTitle());
        if (dirent.isSearchedFile && gridFileType == SettingsManager.GRID_BY_LIST) {
            viewHolder.nameText.setVisibility(View.VISIBLE);
            viewHolder.nameText.setText(dirent.getTitle());
        }
        viewHolder.icon.setTag(R.id.imageloader_uri, dirent.getTitle());
        if (dirent.isDir()) {
            viewHolder.downloadStatusIcon.setVisibility(View.GONE);
            viewHolder.progressBar.setVisibility(View.GONE);

            viewHolder.subtitle.setText(dirent.getSubtitle());

            if (repoIsEncrypted) {
                viewHolder.action.setVisibility(View.GONE);
            } else
                viewHolder.action.setVisibility(View.VISIBLE);

            setDirentIcon(viewHolder, dirent);
        } else {
            viewHolder.downloadStatusIcon.setVisibility(View.GONE);
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.action.setVisibility(View.VISIBLE);
            // ConcurrentAsyncTask.execute(new SetFileView(dirent, viewHolder, position));
            setFileView(dirent, viewHolder, position);
        }
    }

    private String filePath(SeafDirent searchedFile) {
        String parentPath = Utils.getParentPath(searchedFile.path);
        String nameWithParentPath = Utils.pathJoin(parentPath, searchedFile.isDir()? "" : searchedFile.name);
        if (nameWithParentPath.endsWith("/")) {
            nameWithParentPath = nameWithParentPath.substring(0, nameWithParentPath.length() - 1);
        }
        SeafRepo seafRepo = mActivity.getDataManager().getCachedRepoByID(searchedFile.repoID);
        if (seafRepo != null)
            return Utils.pathJoin(seafRepo.getName(), nameWithParentPath);
        else
            return nameWithParentPath;
    }

    /**
     * use to refresh view of {@link com.seafile.seadroid2.ui.fragment.ReposFragment #mPullRefreshListView}
     * <p>
     * <h5>when to show download status icons</h5>
     * if the dirent is a file and already cached, show cached icon.</br>
     * if the dirent is a file and waiting to download, show downloading icon.</br>
     * if the dirent is a file and is downloading, show indeterminate progressbar.</br>
     * ignore directories and repos.</br>
     *
     * @param dirent
     * @param viewHolder
     * @param position
     */
    private void setFileView(SeafDirent dirent, ViewHolder viewHolder, int position) {
        String repoName = "";
        String repoID = "";
        String filePath = "";

        if (dirent.isSearchedFile) {
            SeafRepo repo = dataManager.getCachedRepoByID(dirent.repoID);
            repoName = repo.name;
            repoID = repo.getID();
            filePath = dirent.path;
        } else {
            repoName = nav.getRepoName();
            repoID = nav.getRepoID();
            filePath = Utils.pathJoin(nav.getDirPath(), dirent.name);
        }
        if (repoName == null || repoID == null)
            return;

        File file = null;
        try {
            file = dataManager.getLocalRepoFile(repoName, repoID, filePath);
        } catch (RuntimeException e) {
            return;
        }
        boolean cacheExists = false;

        if (file.exists() && file.length() == dirent.getFileSize()) {
            SeafCachedFile cf = dataManager.getCachedFile(repoName, repoID, filePath);
            String subtitle = null;
            subtitle = dirent.getSubtitle();
            if (cf != null) {
                cacheExists = true;
            }
            // show file download finished
            viewHolder.downloadStatusIcon.setVisibility(View.VISIBLE);
            viewHolder.downloadStatusIcon.setImageResource(R.drawable.ic_list_item_download_finished);
            viewHolder.subtitle.setText(subtitle);
            viewHolder.progressBar.setVisibility(View.GONE);

        } else {
            int downloadStatusIcon = R.drawable.ic_list_item_download_waiting;
            if (mDownloadTaskInfos != null) {
                for (DownloadTaskInfo downloadTaskInfo : mDownloadTaskInfos) {
                    // use repoID and path to identify the task
                    if (downloadTaskInfo.repoID.equals(repoID)
                            && downloadTaskInfo.pathInRepo.equals(filePath)
                            && !downloadTaskInfo.thumbnail) {
                        switch (downloadTaskInfo.state) {
                            case INIT:
                            case FAILED:
                                downloadStatusIcon = R.drawable.ic_list_item_download_waiting;
                                viewHolder.downloadStatusIcon.setVisibility(View.VISIBLE);
                                viewHolder.progressBar.setVisibility(View.GONE);
                                break;
                            case CANCELLED:
                                viewHolder.downloadStatusIcon.setVisibility(View.GONE);
                                viewHolder.progressBar.setVisibility(View.GONE);
                                break;
                            case TRANSFERRING:
                                viewHolder.downloadStatusIcon.setVisibility(View.GONE);
                                viewHolder.progressBar.setVisibility(View.VISIBLE);
                                break;
                            case FINISHED:
                                downloadStatusIcon = R.drawable.ic_list_item_download_finished;
                                viewHolder.downloadStatusIcon.setVisibility(View.VISIBLE);
                                viewHolder.progressBar.setVisibility(View.GONE);
                                break;
                            default:
                                downloadStatusIcon = R.drawable.ic_list_item_download_waiting;
                                break;
                        }
                    }
                }
            } else {
                viewHolder.downloadStatusIcon.setVisibility(View.GONE);
                viewHolder.progressBar.setVisibility(View.GONE);
            }

            viewHolder.downloadStatusIcon.setImageResource(downloadStatusIcon);
            viewHolder.subtitle.setText(dirent.getSubtitle());
        }
        if (Utils.isViewableImage(file.getName())) {
            String url = dataManager.getImageThumbnailLink(repoName, repoID, filePath, WidgetUtils.getThumbnailWidth(columns));
            if (url == null) {
                setDirentIcon(viewHolder, dirent);
            } else {
                if (mActivity.getDataManager().getCachedRepoByID(repoID).encrypted) {
                    String thumbPath = imagePathMap.get(filePath);
                    if (thumbPath != null) {
                        // ConcurrentAsyncTask.execute(new SetGlideApp(localFilePath, null, viewHolder, dirent));
                        // setGlideApp(localFilePath, null, viewHolder, dirent);
                        setEncThumb(thumbPath, viewHolder);
                    } else {
                        if (file.exists() && file.length() == dirent.getFileSize()) {
                            ConcurrentAsyncTask.execute(new MakeEncThumb(filePath, file.getAbsolutePath(), viewHolder));
                        } else {
                            setDirentIcon(viewHolder, dirent);
                        }
                    }
                } else {
                    // ConcurrentAsyncTask.execute(new SetGlideApp(null, url, viewHolder, dirent));
                    setGlideApp(null, url, viewHolder, dirent);
                }
            }
        } else {
            setDirentIcon(viewHolder, dirent);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
        viewHolder.tagColorRecycler.setLayoutManager(layoutManager);
        SeafTagColorAdapter2 colorAdapter = new SeafTagColorAdapter2(mActivity, repoID, dirent, SeafFileTagAdapter.FragmentType.Repos);
        viewHolder.tagColorRecycler.setAdapter(colorAdapter);
        colorAdapter.notifyChanged();
        viewHolder.tagColorRecycler.setVisibility(dirent.getFileTags().size() == 0 || gridFileType == SettingsManager.GRID_BY_MINIMAL_LIST ? View.GONE : View.VISIBLE);
    }

    private void setEncThumb(String thumbPath, ViewHolder viewHolder) {
        viewHolder.icon.setVisibility(View.INVISIBLE);
        viewHolder.imageLayout.setVisibility(View.VISIBLE);
        viewHolder.fileLayout.setVisibility(View.INVISIBLE);
        if (isTile()) {
            viewHolder.title.setTextColor(mActivity.getResources().getColor(R.color.white));
        }
        viewHolder.image.setImageBitmap(Utils.getBitmapFromFile(new File(thumbPath)));
    }

    private void setGlideApp(String path, String url, ViewHolder viewHolder, SeafDirent dirent) {
        viewHolder.icon.setVisibility(View.INVISIBLE);
        viewHolder.imageLayout.setVisibility(View.VISIBLE);
        viewHolder.fileLayout.setVisibility(View.INVISIBLE);
        if (isTile()) {
            viewHolder.title.setTextColor(mActivity.getResources().getColor(R.color.white));
        }

        GlideApp.with(mActivity)
                .asFile()
                .load(url == null ? new File(path) : GlideLoadConfig.getGlideUrl(url))
                .override(1024, 1024)
                .apply(GlideLoadConfig.getOptions(dirent.size + "", columns).skipMemoryCache(true))
                .thumbnail(0.1f)
                .into(new Target<File>() {
                          @Override
                          public void onLoadStarted(@Nullable Drawable placeholder) {
                          }

                          @Override
                          public void onLoadFailed(@Nullable Drawable errorDrawable) {
                          }

                          @Override
                          public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                              String tag = (String) viewHolder.icon.getTag(R.id.imageloader_uri);
                              if (tag.equals(dirent.getTitle())) {
                                  Bitmap bitmap = Utils.getBitmapFromFile(resource);
                                  if (bitmap != null) {
                                      viewHolder.image.setImageBitmap(bitmap);
                                  }
                              } else {
                                  setDirentIcon(viewHolder, dirent);
                              }
                          }

                          @Override
                          public void onLoadCleared(@Nullable Drawable placeholder) {
                          }

                          @Override
                          public void getSize(@NonNull SizeReadyCallback cb) {
                          }

                          @Override
                          public void removeCallback(@NonNull SizeReadyCallback cb) {
                          }

                          @Override
                          public void setRequest(@Nullable Request request) {
                          }

                          @Nullable
                          @Override
                          public Request getRequest() {
                              return null;
                          }

                          @Override
                          public void onStart() {
                          }

                          @Override
                          public void onStop() {
                          }

                          @Override
                          public void onDestroy() {
                          }
                      }
                );
    }

    private class MakeEncThumb extends AsyncTask<Void, Void, String> {
        private String filePath;
        private String localFilePath;
        private ViewHolder viewHolder;

        public MakeEncThumb(String filePath, String localFilePath, ViewHolder viewHolder) {
            this.filePath = filePath;
            this.localFilePath = localFilePath;
            this.viewHolder = viewHolder;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            String thumbPath = Utils.generateEncThumbPath(localFilePath, Utils.getEncThumbPath(localFilePath));
            return thumbPath;
        }

        @Override
        protected void onPostExecute(String thumbPath) {
            imagePathMap.put(filePath, thumbPath);

            if (mActivity != null) {
                SettingsFragment settingsFragment = mActivity.getSettingsFragment();
                if (settingsFragment != null) {
                    settingsFragment.updateThumbImagesCount(
                            settingsFragment.allEncThumbsCount + 1,
                            settingsFragment.allEncImagesCount
                    );
                }
            }
            setEncThumb(thumbPath, viewHolder);
        }
    }

    private void setDirentIcon(ViewHolder viewHolder, SeafDirent dirent) {
        int fileIcon = dirent.getIcon();
        viewHolder.title.setTextColor(mActivity.getResources().getColor(R.color.text_view_color));
        if (fileIcon == R.drawable.ic_folder_read_only || fileIcon == R.drawable.ic_folder) {
            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.imageLayout.setVisibility(View.INVISIBLE);
            viewHolder.fileLayout.setVisibility(View.INVISIBLE);
            viewHolder.icon.setImageResource(fileIcon);
        } else {
            viewHolder.icon.setVisibility(View.INVISIBLE);
            viewHolder.imageLayout.setVisibility(View.INVISIBLE);
            viewHolder.fileLayout.setVisibility(View.VISIBLE);
            if (fileIcon == R.drawable.ic_file) {
                viewHolder.fileIcon.setVisibility(View.GONE);
                viewHolder.fileText.setVisibility(View.VISIBLE);
                viewHolder.fileText.setText(Utils.getFileExtension(dirent.name));
            } else {
                viewHolder.fileIcon.setVisibility(View.VISIBLE);
                viewHolder.fileText.setVisibility(View.GONE);
                viewHolder.fileIcon.setImageResource(dirent.getIcon());
            }
        }
    }

    private boolean isTile() {
        return gridFileType != SettingsManager.GRID_BY_LIST && gridFileType != SettingsManager.GRID_BY_MINIMAL_LIST;
    }

    private void setCacheView(SeafCachedFile item, ViewHolder viewHolder, int position) {
        viewHolder.repoTypeImage.setVisibility(View.GONE);
        viewHolder.viewCard.setOnClickListener(v -> {
            mFragment.listItemClick(position);
        });

        viewHolder.viewCard.setOnLongClickListener(v -> {
            mFragment.listItemLongClick(position);
            return true;
        });

        viewHolder.downloadStatusIcon.setVisibility(View.VISIBLE);
        viewHolder.downloadStatusIcon.setImageResource(R.drawable.ic_list_item_download_finished);
        viewHolder.progressBar.setVisibility(View.GONE);
        viewHolder.title.setText(item.getTitle());
        viewHolder.subtitle.setText(item.getSubtitle());
        viewHolder.icon.setImageResource(item.getIcon());
        viewHolder.action.setVisibility(View.INVISIBLE);

        viewHolder.icon.setVisibility(View.INVISIBLE);
        viewHolder.fileLayout.setVisibility(View.VISIBLE);
        viewHolder.title.setTextColor(mActivity.getResources().getColor(R.color.text_view_color));

        if (item.getIcon() == R.drawable.ic_file) {
            viewHolder.fileIcon.setVisibility(View.GONE);
            viewHolder.fileText.setVisibility(View.VISIBLE);
            viewHolder.fileText.setText(item.getFileExtension());
        } else {
            viewHolder.fileIcon.setVisibility(View.VISIBLE);
            viewHolder.fileText.setVisibility(View.GONE);
            viewHolder.fileIcon.setImageResource(item.getIcon());
        }
    }

    public void setActionModeOn(boolean actionModeOn) {
        this.actionModeOn = actionModeOn;
    }

    public void toggleSelection(int position) {
        if (mSelectedItemsIds.get(position)) {
            // unselected
            mSelectedItemsIds.delete(position);
            mSelectedItemsPositions.remove(Integer.valueOf(position));
            mSelectedItemsValues.remove(showItems.get(position));
        } else {
            mSelectedItemsIds.put(position, true);
            mSelectedItemsPositions.add(position);
            mSelectedItemsValues.add((SeafDirent) showItems.get(position));
        }

        mActivity.onItemSelected();
        notifyChanged();
    }

    public int getCheckedItemCount() {
        return mSelectedItemsIds.size();
    }

    public List<SeafDirent> getSelectedItemsValues() {
        return mSelectedItemsValues;
    }

    public void setEncryptedRepo(boolean encrypted) {
        repoIsEncrypted = encrypted;
    }

    public void sortFiles() {
        sortFiles(sortType, sortOrder);
    }

    public void sortFiles(int type, int order) {
        sortType = type;
        sortOrder = order;
        List<SeafGroup> groups = Lists.newArrayList();
        List<SeafRepo> repos = Lists.newArrayList();
        List<String> showRepoIds = Lists.newArrayList();
        List<SeafCachedFile> cachedFiles = Lists.newArrayList();
        List<SeafDirent> folders = Lists.newArrayList();
        List<SeafDirent> files = Lists.newArrayList();
        SeafGroup group = null;

        for (SeafItem item : items) {
            if (item instanceof SeafGroup) {
                group = (SeafGroup) item;
                groups.add(group);
            } else if (item instanceof SeafRepo) {
                if (group == null)
                    continue;
                group.addIfAbsent((SeafRepo) item);

                SeafRepo repo = (SeafRepo) item;
                boolean flag = false;
                if (repoPersonal && repo.isPersonalRepo)
                    flag = true;
                if (repoGroup && repo.isGroupRepo)
                    flag = true;
                if (repoShared && (repo.isSharedRepo || repo.isPublicRepo))
                    flag = true;
                if (flag) {
                    if (showRepoIds.contains(repo.id))
                        continue;
                    showRepoIds.add(repo.id);
                    repos.add(repo);
                }
            } else if (item instanceof SeafCachedFile) {
                cachedFiles.add(((SeafCachedFile) item));
            } else {
                if (((SeafDirent) item).isDir())
                    folders.add(((SeafDirent) item));
                else
                    files.add(((SeafDirent) item));
            }
        }

        showItems.clear();

        // sort SeafGroups and SeafRepos
//        for (SeafGroup sg : groups) {
//            sg.sortByType(type, order);
//            items.add(sg);
//            items.addAll(sg.getRepos());
//        }

        for (SeafGroup sg : groups) {
            sg.sortByType(type, order);
            List<SeafRepo> subRepos = Lists.newArrayList();
            subRepos.addAll(sg.getRepos());
            if (type == SettingsManager.SORT_BY_NAME) {
                Collections.sort(subRepos, new SeafRepo.RepoNameComparator());
                if (order == SettingsManager.SORT_ORDER_DESCENDING) {
                    Collections.reverse(subRepos);
                }
            } else if (type == SettingsManager.SORT_BY_LAST_MODIFIED_TIME) {
                Collections.sort(subRepos, new SeafRepo.RepoLastMTimeComparator());
                if (order == SettingsManager.SORT_ORDER_DESCENDING) {
                    Collections.reverse(subRepos);
                }
            }
            if (repoGroup && !repoPersonal && !repoShared && sg.isGroupRepo && !sg.getTitle().equals(PUBLIC_REPO)) {
                showItems.add(sg);
                showItems.addAll(subRepos);

                for (SeafRepo repo: subRepos) {
                    if (showRepoIds.contains(repo.id))
                        continue;
                    showRepoIds.add(repo.id);
                    repos.add(repo);
                }
            }
        }

        // sort SeafDirents
        if (type == SettingsManager.SORT_BY_NAME) {
            // sort by name, in ascending order
            Collections.sort(repos, new SeafRepo.RepoNameComparator());
            Collections.sort(folders, new SeafDirent.DirentNameComparator());
            Collections.sort(files, new SeafDirent.DirentNameComparator());
            if (order == SettingsManager.SORT_ORDER_DESCENDING) {
                Collections.reverse(repos);
                Collections.reverse(folders);
                Collections.reverse(files);
            }
        } else if (type == SettingsManager.SORT_BY_LAST_MODIFIED_TIME) {
            // sort by last modified time, in ascending order
            Collections.sort(repos, new SeafRepo.RepoLastMTimeComparator());
            Collections.sort(folders, new SeafDirent.DirentLastMTimeComparator());
            Collections.sort(files, new SeafDirent.DirentLastMTimeComparator());
            if (order == SettingsManager.SORT_ORDER_DESCENDING) {
                Collections.reverse(repos);
                Collections.reverse(folders);
                Collections.reverse(files);
            }
        }
        // Adds the objects in the specified collection to this ArrayList

        if (repoPersonal || !repoGroup || repoShared)
            showItems.addAll(repos);
        showItems.addAll(cachedFiles);
        showItems.addAll(folders);
        for (SeafDirent item: files) {
            if (includingSelectedTagID(item)) {
                showItems.add(item);
            }
        }
    }

    public void updateItem(SeafDirent dirent, List<SeafFileTag> fileTags) {
        for (int i = 0; i < items.size(); i++) {
            SeafItem item = items.get(i);
            if ( item instanceof SeafDirent) {
                SeafDirent seafDirent = (SeafDirent) item;
                if (seafDirent.equals(dirent)) {
                    dirent.setFileTags(fileTags);
                    items.set(i, dirent);
                }
            }
        }
        sortFiles();
        notifyChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View imageLayout, fileLayout, groupLayout;
        CardView viewCard;
        TextView nameText, fileText, title, subtitle, groupTitle;
        ImageView icon, image, fileIcon, multiSelect, downloadStatusIcon, repoTypeImage; // downloadStatusIcon used to show file downloading status, it is invisible by
        // default
        ProgressBar progressBar;
        CardView action;
        RecyclerView tagColorRecycler;

        ViewHolder(View view) {
            super(view);
            viewCard = (CardView) view.findViewById(R.id.view_card);
            nameText = (TextView) view.findViewById(R.id.list_item_name);
            title = (TextView) view.findViewById(R.id.list_item_title);
            subtitle = (TextView) view.findViewById(R.id.list_item_subtitle);
            icon = (ImageView) view.findViewById(R.id.list_item_icon);
            multiSelect = (ImageView) view.findViewById(R.id.list_item_multi_select_btn);
            imageLayout = (View) view.findViewById(R.id.list_item_icon_image_layout);
            image = (ImageView) view.findViewById(R.id.list_item_icon_image);
            fileLayout = (View) view.findViewById(R.id.list_item_icon_file_layout);
            fileIcon = (ImageView) view.findViewById(R.id.list_item_icon_file_icon);
            fileText = (TextView) view.findViewById(R.id.list_item_icon_file_text);
            action = (CardView) view.findViewById(R.id.expandable_toggle_button);
            downloadStatusIcon = (ImageView) view.findViewById(R.id.list_item_download_status_icon);
            progressBar = (ProgressBar) view.findViewById(R.id.list_item_download_status_progressbar);
            tagColorRecycler = (RecyclerView) view.findViewById(R.id.tag_color_recycler);
            repoTypeImage = (ImageView) view.findViewById(R.id.repo_type_image);
            groupLayout = (LinearLayout) view.findViewById(R.id.group_layout);
            groupTitle = (TextView) view.findViewById(R.id.textview_groupname);
        }
    }
}

