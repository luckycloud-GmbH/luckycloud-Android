package com.seafile.seadroid2.ui.adapter;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafDirent;
import com.seafile.seadroid2.data.SeafFileTag;
import com.seafile.seadroid2.data.SeafRepoTag;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.data.SeafRepoTag;
import com.seafile.seadroid2.data.SeafRepoTag;
import com.seafile.seadroid2.ui.NavContext;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.fragment.ReposFragment;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SeafFileTagAdapter extends BaseAdapter {

    private static final String DEBUG_TAG = "SeafFileTagAdapter";
    public enum FragmentType {Repos, Starred}
    private FragmentType fragmentType;
    private List<SeafRepoTag> repoTags;
    private List<SeafFileTag> fileTags;
    private BrowserActivity mActivity;
    private DataManager dataManager;
    private String dirPath;
    private SeafRepo repo;
    private SeafDirent dirent;


    public SeafFileTagAdapter(BrowserActivity activity, SeafRepo seafRepo, SeafDirent seafDirent, FragmentType fragmentType) {
        mActivity = activity;
        repo = seafRepo;
        dirent = seafDirent;
        if (dirent.isSearchedFile) {
            dirPath = Utils.pathSplit(dirent.path, dirent.name);
        } else {
            dirPath = mActivity.getNavContext().getDirPath();
        }
        this.fragmentType = fragmentType;
        repoTags = Lists.newArrayListWithCapacity(0);
        fileTags = Lists.newArrayListWithCapacity(0);
        dataManager = mActivity.getDataManager();
        repoTags = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return repoTags.size() + 1;
    }

    @Override
    public boolean isEmpty() {
        return repoTags.isEmpty();
    }

    public boolean isEnabled(int position) {
        return true;
    }

    public void addEntry(SeafRepoTag entry) {
        repoTags.add(entry);
        // Collections.sort(repoTags);
        notifyChanged();
    }

    public void add(SeafRepoTag entry) {
        repoTags.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public SeafRepoTag getItem(int position) {
        return repoTags.get(position);
    }

    public void setRepoTags(List<SeafRepoTag> tags) {
        repoTags.clear();
        if (tags != null)
            repoTags.addAll(tags);
    }

    public void setFileTags(List<SeafFileTag> tags) {
        fileTags.clear();
        if (tags != null)
            fileTags.addAll(tags);
    }

    public void getFileTags() {
        ConcurrentAsyncTask.execute(new ListFileTags());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        repoTags.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Viewholder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.tag_file_item, null);
            CardView fileTagCard = (CardView) view.findViewById(R.id.file_tag_card);
            CardView tagColorCard = (CardView) view.findViewById(R.id.tag_color_card);
            TextView tagNameText = (TextView) view.findViewById(R.id.tag_name_text);
            ImageView tagCheckImage = (ImageView) view.findViewById(R.id.tag_check_image);
            CardView newTagCard = (CardView) view.findViewById(R.id.new_tag_card);
            viewHolder = new Viewholder(fileTagCard, tagColorCard, tagNameText, tagCheckImage, newTagCard);
            view.setTag(viewHolder);
        } else {
            viewHolder = (Viewholder) convertView.getTag();
        }

        if (position == repoTags.size()) {
            viewHolder.fileTagCard.setVisibility(View.GONE);
            viewHolder.newTagCard.setVisibility(View.VISIBLE);
            
            viewHolder.newTagCard.setOnClickListener(v -> {
                mActivity.getReposFragment().showAddRepoTagDialog(repo.getID(), true);
            });
            return view;
        }
        SeafRepoTag item = repoTags.get(position);
        viewHolder.fileTagCard.setVisibility(View.VISIBLE);
        viewHolder.newTagCard.setVisibility(View.GONE);

        viewHolder.tagNameText.setText(item.getTag_name());
        viewHolder.tagColorCard.setCardBackgroundColor(Color.parseColor(item.getTag_color()));
        boolean flag = false;
        for (SeafFileTag fileTag: fileTags) {
            if (item.getRepo_tag_id().equals(fileTag.getRepo_tag_id())) {
                flag = true;
                break;
            }
        }
        viewHolder.tagCheckImage.setVisibility(flag? View.VISIBLE : View.INVISIBLE);
        viewHolder.fileTagCard.setOnClickListener(v -> {
            if (viewHolder.tagCheckImage.getVisibility() == View.INVISIBLE)
                ConcurrentAsyncTask.execute(new AddFileTag(item));
            else {
                for (SeafFileTag fileTag: fileTags) {
                    if (item.getRepo_tag_id().equals(fileTag.getRepo_tag_id())) {
                        ConcurrentAsyncTask.execute(new DeleteFileTag(fileTag));
                        break;
                    }
                }
            }
        });

        return view;
    }

    private class Viewholder {
        CardView fileTagCard, tagColorCard, newTagCard;
        TextView tagNameText;
        ImageView tagCheckImage;

        public Viewholder(CardView fileTagCard, 
                          CardView tagColorCard, 
                          TextView tagNameText, 
                          ImageView tagCheckImage,
                          CardView newTagCard

        ) {
            super();
            this.fileTagCard = fileTagCard;
            this.tagColorCard = tagColorCard;
            this.tagNameText = tagNameText;
            this.tagCheckImage = tagCheckImage;
            this.newTagCard = newTagCard;
        }
    }

    private class ListFileTags extends AsyncTask<Void, Void, List<SeafFileTag>> {
        private SeafException err;

        @Override
        protected List<SeafFileTag> doInBackground(Void... params) {
            try {
                String filePath = Utils.pathJoin(dirPath, dirent.name);
                return dataManager.getFileTagsFromServer(repo, filePath);
            } catch (SeafException e) {
                err = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<SeafFileTag> result) {
            // Check to see whether this activity is in the process of finishing
            // to avoid IllegalStateException when AsyncTasks continue to run after the activity has been destroyed
            // http://stackoverflow.com/a/35729068/3962551

            if (result == null) {
                return;
            }
            setFileTags(result);
            notifyChanged();
        }
    }

    private class AddFileTag extends AsyncTask<Void, Void, String> {
        private SeafException err;
        private SeafRepoTag repoTag;

        public AddFileTag(SeafRepoTag tag) {
            repoTag = tag;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String filePath = Utils.pathJoin(dirPath, dirent.name);
                return dataManager.addFileTag(repo.getID(), filePath, repoTag.getRepo_tag_id());
            } catch (SeafException e) {
                err = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // Check to see whether this activity is in the process of finishing
            // to avoid IllegalStateException when AsyncTasks continue to run after the activity has been destroyed
            // http://stackoverflow.com/a/35729068/3962551

            if (result == null) {
                return;
            }
            if (result.contains("already exist")) {
                mActivity.showShortToast(mActivity, mActivity.getResources().getString(R.string.tag_exist));
                return;
            }
            try {
                JSONObject jsonObject = Utils.parseJsonObject(result);
                JSONObject object = jsonObject.getJSONObject("file_tag");
                SeafFileTag seafFileTag = new SeafFileTag();
                seafFileTag.setTag_color(repoTag.getTag_color());
                seafFileTag.setTag_name(repoTag.getTag_name());
                seafFileTag.setRepo_tag_id(repoTag.getRepo_tag_id());
                seafFileTag.setFile_tag_id(object.getString("file_tag_id"));
                fileTags.add(seafFileTag);
                notifyChangedWithSave();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class DeleteFileTag extends AsyncTask<Void, Void, String> {
        private SeafException err;
        private SeafFileTag fileTag;

        public DeleteFileTag(SeafFileTag tag) {
            fileTag = tag;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return dataManager.deleteFileTag(repo.getID(), fileTag.getFile_tag_id());
            } catch (SeafException e) {
                err = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // Check to see whether this activity is in the process of finishing
            // to avoid IllegalStateException when AsyncTasks continue to run after the activity has been destroyed
            // http://stackoverflow.com/a/35729068/3962551

            if (result == null) return;
            if (result.contains("true")) {
                fileTags.remove(fileTag);
                notifyChangedWithSave();
            }
        }
    }

    private void notifyChangedWithSave() {
        if (fragmentType == FragmentType.Repos) {
            mActivity.getReposFragment().getAdapter().updateItem(dirent, fileTags);
        } else {
            mActivity.getStarredFragment().getAdapter().updateItem(dirent, fileTags);
        }
        mActivity.getReposFragment().deleteCachedDirent(dirent.repoID, dirPath);
        dirent.setFileTags(fileTags);
        notifyChanged();
    }
}

