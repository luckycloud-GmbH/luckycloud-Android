package com.seafile.seadroid2.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.config.GlideLoadConfig;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.data.SeafFileTag;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.data.SeafStarredFile;
import com.seafile.seadroid2.ui.WidgetUtils;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.fragment.StarredFragment;
import com.seafile.seadroid2.util.GlideApp;
import com.seafile.seadroid2.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StarredItemAdapter extends BaseAdapter {
    private static final String DEBUG_TAG = "StarredItemAdapter";
    private ArrayList<SeafStarredFile> items;
    private BrowserActivity mActivity;
    private StarredFragment mFragment;
    private DataManager dataManager;

    private boolean actionModeOn;
    private SparseBooleanArray mSelectedItemsIds;
    private List<Integer> mSelectedItemsPositions = Lists.newArrayList();
    private List<SeafStarredFile> mSelectedItemsValues = Lists.newArrayList();

    private int gridFileType = SettingsManager.GRID_BY_LIST;
    private int columns = 1;
    private int sortType;
    private int sortOrder;

    public StarredItemAdapter(BrowserActivity activity, StarredFragment fragment) {
        this.mActivity = activity;
        this.mFragment = fragment;
        items = Lists.newArrayList();
        mSelectedItemsIds = new SparseBooleanArray();
        dataManager = mActivity.getDataManager();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }

    public void add(SeafStarredFile entry) {
        items.add(entry);
    }

    public void notifyChanged() {
        sortFiles();
        if (actionModeOn)
            deselectAllItems();
        else
            notifyDataSetChanged();
    }

    public void setGridFileType(int type) {
        gridFileType = type;
        columns = SettingsManager.instance().getGridFilesColumns(type);
    }

    public void setSortValues(int type, int order) {
        sortType = type;
        sortOrder = order;
    }

    public void sortFiles() {
        if (sortType == SettingsManager.SORT_BY_NAME) {
            // sort by name, in ascending order
            Collections.sort(items, new SeafStarredFile.StarredNameComparator());
            if (sortOrder == SettingsManager.SORT_ORDER_DESCENDING) {
                Collections.reverse(items);
            }
        } else if (sortType == SettingsManager.SORT_BY_LAST_MODIFIED_TIME) {
            // sort by last modified time, in ascending order
            Collections.sort(items, new SeafStarredFile.StarredLastMTimeComparator());
            if (sortOrder == SettingsManager.SORT_ORDER_DESCENDING) {
                Collections.reverse(items);
            }
        }
    }

    @Override
    public SeafStarredFile getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setActionModeOn(boolean actionModeOn) {
        this.actionModeOn = actionModeOn;
    }

    public void toggleSelection(int position) {
        if (mSelectedItemsIds.get(position)) {
            // unselected
            mSelectedItemsIds.delete(position);
            mSelectedItemsPositions.remove(Integer.valueOf(position));
            mSelectedItemsValues.remove(items.get(position));
        } else {
            mSelectedItemsIds.put(position, true);
            mSelectedItemsPositions.add(position);
            mSelectedItemsValues.add((SeafStarredFile) items.get(position));
        }

        mActivity.getStarredFragment().updateContextualActionBar();
        notifyDataSetChanged();
    }

    public int getCheckedItemCount() {
        return mSelectedItemsIds.size();
    }

    public List<SeafStarredFile> getSelectedItemsValues() {
        return mSelectedItemsValues;
    }

    public void setItems(List<SeafStarredFile> starredFiles) {
        items.clear();
        items.addAll(starredFiles);
        notifyChanged();
    }

    public void deselectAllItems() {
        mSelectedItemsIds.clear();
        mSelectedItemsPositions.clear();
        mSelectedItemsValues.clear();
        notifyDataSetChanged();
    }

    public void selectAllItems() {
        mSelectedItemsIds.clear();
        mSelectedItemsPositions.clear();
        mSelectedItemsValues.clear();
        for (int i = 0; i < items.size(); i++) {
            mSelectedItemsIds.put(i, true);
            mSelectedItemsPositions.add(i);
            mSelectedItemsValues.add((SeafStarredFile) items.get(i));
        }
        notifyDataSetChanged();
    }

    private boolean isTile() {
        return gridFileType != SettingsManager.GRID_BY_LIST && gridFileType != SettingsManager.GRID_BY_MINIMAL_LIST;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final SeafStarredFile item = items.get(position);
        View view = convertView;
        final ViewHolder viewHolder;

//        if (convertView == null) {
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
                view = LayoutInflater.from(mActivity).inflate(R.layout.list_item_entry, null);
                break;
        }

        CardView viewCard = view.findViewById(R.id.view_card);
        TextView nameText = (TextView) view.findViewById(R.id.list_item_name);
        TextView title = (TextView) view.findViewById(R.id.list_item_title);
        TextView subtitle = (TextView) view.findViewById(R.id.list_item_subtitle);
        ImageView icon = (ImageView) view.findViewById(R.id.list_item_icon);
        ImageView multiSelect = (ImageView) view.findViewById(R.id.list_item_multi_select_btn);
        View imageLayout = view.findViewById(R.id.list_item_icon_image_layout);
        ImageView image = view.findViewById(R.id.list_item_icon_image);
        View fileLayout = view.findViewById(R.id.list_item_icon_file_layout);
        ImageView fileIcon = view.findViewById(R.id.list_item_icon_file_icon);
        TextView fileText = view.findViewById(R.id.list_item_icon_file_text);
        CardView action = (CardView) view.findViewById(R.id.expandable_toggle_button);
        ImageView downloadStatusIcon = (ImageView) view.findViewById(R.id.list_item_download_status_icon);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.list_item_download_status_progressbar);
        RecyclerView tagColorRecycler = (RecyclerView) view.findViewById(R.id.tag_color_recycler);
        ImageView repoTypeImage = (ImageView) view.findViewById(R.id.repo_type_image);
        viewHolder = new ViewHolder(viewCard, nameText, title, subtitle, multiSelect, icon, imageLayout, image, fileLayout, fileIcon, fileText, action, downloadStatusIcon, progressBar, tagColorRecycler, repoTypeImage);
        view.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }

        viewHolder.nameText.setVisibility(View.GONE);
        viewHolder.repoTypeImage.setVisibility(View.GONE);
        if (item.isDir()) {
            if (viewHolder.tagColorRecycler.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) viewHolder.tagColorRecycler.getLayoutParams();
                lp.height = 0;
                viewHolder.tagColorRecycler.setLayoutParams(lp);
            } else {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewHolder.tagColorRecycler.getLayoutParams();
                lp.height = 0;
                viewHolder.tagColorRecycler.setLayoutParams(lp);
            }
        }

        viewHolder.viewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.listItemClick(position);
            }
        });
        viewHolder.viewCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mFragment.listItemLongClick(position);
                return true;
            }
        });
        viewHolder.title.setText(item.getTitle());
        viewHolder.title.setTextColor(mActivity.getResources().getColor(R.color.text_view_color));
        viewHolder.subtitle.setText(item.getSubtitle());
        viewHolder.icon.setTag(R.id.imageloader_uri, item.getTitle());

        if (Utils.isViewableImage(item.getTitle())) {
            String url = dataManager.getImageThumbnailLink(((SeafStarredFile) item).getRepoName(), ((SeafStarredFile) item).getRepoID(),
                    ((SeafStarredFile) item).getPath(), WidgetUtils.getThumbnailWidth(columns));
            if (url == null) {
                judgeRepo(item, viewHolder);
            } else {
                SeafRepo repo = mActivity.getDataManager().getCachedRepoByID(item.getRepoID());
                if (repo == null) {
                    judgeRepo(item, viewHolder);
                } else {
                    if (repo.encrypted) {
                        judgeRepo(item, viewHolder);
                    } else {
                        viewHolder.icon.setVisibility(View.GONE);
                        viewHolder.imageLayout.setVisibility(View.VISIBLE);
                        viewHolder.fileLayout.setVisibility(View.INVISIBLE);
                        if (isTile()) {
                            viewHolder.title.setTextColor(mActivity.getResources().getColor(R.color.white));
                        }

                        GlideApp.with(mActivity)
                                .asFile()
                                .load(GlideLoadConfig.getGlideUrl(url))
                                .override(1024, 1024)
                                .apply(GlideLoadConfig.getOptions("", columns))
                                .thumbnail(0.1f)
                                .into(new Target<File>() {
                                          @Override
                                          public void onLoadStarted(@Nullable Drawable placeholder) {}
                                          @Override
                                          public void onLoadFailed(@Nullable Drawable errorDrawable) {}
                                          @Override
                                          public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                              String tag = (String) viewHolder.icon.getTag(R.id.imageloader_uri);
                                              if (tag.equals(item.getTitle())) {
                                                  Bitmap bitmap = Utils.getBitmapFromFile(resource);
                                                  if (bitmap != null) {
                                                      viewHolder.image.setImageBitmap(bitmap);
                                                  }
                                              }
                                          }
                                          @Override
                                          public void onLoadCleared(@Nullable Drawable placeholder) {}
                                          @Override
                                          public void getSize(@NonNull SizeReadyCallback cb) {}
                                          @Override
                                          public void removeCallback(@NonNull SizeReadyCallback cb) {}
                                          @Override
                                          public void setRequest(@Nullable Request request) {}
                                          @Nullable
                                          @Override
                                          public Request getRequest() { return null; }
                                          @Override
                                          public void onStart() {}
                                          @Override
                                          public void onStop() {}
                                          @Override
                                          public void onDestroy() {}
                                      }
                                );
                    }
                }
            }
        } else {
            judgeRepo(item, viewHolder);
        }

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
                        mSelectedItemsValues.add((SeafStarredFile) item);
                    } else {
                        viewHolder.multiSelect.setImageResource(R.drawable.ic_multi_select_item_unchecked);
                        viewHolder.viewCard.setCardBackgroundColor(mActivity.getResources().getColor(R.color.dialog_msg_background));
                        mSelectedItemsIds.delete(position);
                        mSelectedItemsPositions.remove(Integer.valueOf(position));
                        mSelectedItemsValues.remove(item);
                    }

                    mActivity.onItemSelected();
                }
            });
        } else {
            viewHolder.multiSelect.setVisibility(View.GONE);
            viewHolder.viewCard.setCardBackgroundColor(mActivity.getResources().getColor(R.color.dialog_msg_background));
        }

        viewHolder.action.setOnClickListener(v -> {
            if (item.isDir())
                mFragment.showDirBottomSheet(item.getTitle(), getItem(position));
            else
                mFragment.showFileBottomSheet(item.getTitle(), getItem(position));
        });

        SeafRepo repo = dataManager.getCachedRepoByID(item.getRepoID());
        viewHolder.action.setVisibility(View.GONE);
        if (repo != null)
            if (!repo.encrypted)
                viewHolder.action.setVisibility(View.VISIBLE);

        viewHolder.tagColorRecycler.setVisibility(View.VISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
        viewHolder.tagColorRecycler.setLayoutManager(layoutManager);
        SeafDirent dirent = new SeafDirent();
        if (!item.isDir()) {
            dirent.name = item.getObj_name();
            dirent.path = Utils.pathSplit(item.getPath(), item.getObj_name());
            dirent.repoID = item.getRepoID();
            dirent.setFileTags(item.fileTags);
            dirent.isSearchedFile = true;
            SeafTagColorAdapter2 colorAdapter = new SeafTagColorAdapter2(mActivity, item.getRepoID(), dirent, SeafFileTagAdapter.FragmentType.Starred);
            viewHolder.tagColorRecycler.setAdapter(colorAdapter);
            colorAdapter.notifyChanged();
            viewHolder.tagColorRecycler.setVisibility(dirent.getFileTags().size() == 0 || gridFileType == SettingsManager.GRID_BY_MINIMAL_LIST ? View.GONE : View.VISIBLE);
        }
        return view;
    }

    private void judgeRepo(SeafStarredFile item, ViewHolder viewHolder) {
        if (item.isRepo_encrypted() && item.isDir() && item.getPath().equals("/")) {
            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.imageLayout.setVisibility(View.INVISIBLE);
            viewHolder.fileLayout.setVisibility(View.INVISIBLE);

            viewHolder.icon.setImageResource(R.drawable.repo_encrypted);
        } else {
            if (item.isDir() && item.getPath().equals("/")) {
                viewHolder.icon.setVisibility(View.VISIBLE);
                viewHolder.imageLayout.setVisibility(View.INVISIBLE);
                viewHolder.fileLayout.setVisibility(View.INVISIBLE);

                viewHolder.icon.setImageResource(R.drawable.repo);
            } else {
                if (item.isDir()) {
                    int fileIcon = item.getIcon();
                    viewHolder.icon.setImageResource(fileIcon);
                } else {
                    viewHolder.icon.setVisibility(View.GONE);
                    viewHolder.imageLayout.setVisibility(View.INVISIBLE);
                    viewHolder.fileLayout.setVisibility(View.VISIBLE);

                    if (item.getIcon() == R.drawable.ic_file) {
                        viewHolder.fileIcon.setVisibility(View.GONE);
                        viewHolder.fileText.setVisibility(View.VISIBLE);
                        viewHolder.fileText.setText(Utils.getFileExtension(item.getTitle()));
                    } else {
                        viewHolder.fileIcon.setVisibility(View.VISIBLE);
                        viewHolder.fileText.setVisibility(View.GONE);
                        viewHolder.fileIcon.setImageResource(item.getIcon());
                    }
                }
            }
        }
    }

    private static class ViewHolder {
        View imageLayout, fileLayout;
        CardView viewCard;
        TextView nameText, fileText, title, subtitle;
        ImageView icon, image, fileIcon, multiSelect, downloadStatusIcon, repoTypeImage; // downloadStatusIcon used to show file downloading status, it is invisible by
        // default
        ProgressBar progressBar;
        CardView action;
        RecyclerView tagColorRecycler;

        public ViewHolder(CardView viewCard,
                          TextView nameText,
                          TextView title,
                          TextView subtitle,
                          ImageView multiSelect,
                          ImageView icon,
                          View imageLayout,
                          ImageView image,
                          View fileLayout,
                          ImageView fileIcon,
                          TextView fileText,
                          CardView action,
                          ImageView downloadStatusIcon,
                          ProgressBar progressBar,
                          RecyclerView tagColorRecycler,
                          ImageView repoTypeImage) {
            super();
            this.viewCard = viewCard;
            this.nameText = nameText;
            this.icon = icon;
            this.imageLayout = imageLayout;
            this.image = image;
            this.fileLayout = fileLayout;
            this.fileIcon = fileIcon;
            this.fileText = fileText;
            this.multiSelect = multiSelect;
            this.action = action;
            this.title = title;
            this.subtitle = subtitle;
            this.downloadStatusIcon = downloadStatusIcon;
            this.progressBar = progressBar;
            this.tagColorRecycler = tagColorRecycler;
            this.repoTypeImage = repoTypeImage;
        }
    }

    public void updateItem(SeafDirent dirent, List<SeafFileTag> fileTags) {
        for (int i = 0; i < items.size(); i++) {
            SeafStarredFile item = items.get(i);
            if (item != null) {
                String dir = Utils.pathSplit(item.getPath(), item.getObj_name());
                if (dirent.repoID.equals(item.getRepoID()) &&
                        dirent.name.equals(item.getObj_name()) &&
                        dirent.path.equals(dir)) {
                    item.setFileTags(fileTags);
                    items.set(i, item);
                }
            }
        }
        sortFiles();
        notifyChanged();
    }
}
