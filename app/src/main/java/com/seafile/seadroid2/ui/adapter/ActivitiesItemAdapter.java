package com.seafile.seadroid2.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.google.common.collect.Lists;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.config.GlideLoadConfig;
import com.seafile.seadroid2.data.EventDetailsFileItem;
import com.seafile.seadroid2.data.SeafEvent;
import com.seafile.seadroid2.data.SeafItem;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.fragment.ActivitiesFragment;
import com.seafile.seadroid2.ui.widget.CircleImageView;
import com.seafile.seadroid2.util.GlideApp;
import com.seafile.seadroid2.util.SystemSwitchUtils;
import com.seafile.seadroid2.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter for Activities tab
 */
public class ActivitiesItemAdapter extends BaseAdapter {
    public static final String DEBUG_TAG = ActivitiesItemAdapter.class.getSimpleName();

    public static final int REFRESH_ON_NONE = 0;
    public static final int REFRESH_ON_PULL_DOWN = 1;
    public static final int REFRESH_ON_PULL_UP = 2;
    private int state = REFRESH_ON_NONE;

    private ArrayList<SeafEvent> items;
    private BrowserActivity mActivity;
    private ImageLoader loader;
    private DisplayImageOptions options;
    private ActivitiesFragment mFragment;

    private boolean isProEdition;
    private boolean useNewActivity;
    private SeafEvent selectedEvent;
    private List<EventDetailsFileItem> detailsFileItems;

    public ActivitiesItemAdapter(BrowserActivity activity, ActivitiesFragment fragment, Boolean isProEdition) {
        this.mActivity = activity;
        this.mFragment = fragment;
        this.isProEdition = isProEdition;
        items = Lists.newArrayList();
        loader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .extraForDownloader(mActivity.getAccount())
                .showStubImage(R.drawable.ic_default_account)
                .showImageOnLoading(R.drawable.ic_default_account)
                .showImageForEmptyUri(R.drawable.ic_default_account)
                .showImageOnFail(R.drawable.ic_default_account)
                .resetViewBeforeLoading()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        detailsFileItems = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return items.size() + 1;
    }

    public void clear() {
        items.clear();
    }

    public void add(SeafEvent entry) {
        items.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public SeafItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(List<SeafEvent> events, boolean useNewActivity) {
        this.useNewActivity = useNewActivity;
        items.clear();
        items.addAll(events);
    }

    public void setSelectedEvent(SeafEvent selectedEvent, List<EventDetailsFileItem> detailsFileItems) {
        this.selectedEvent = selectedEvent;
        this.detailsFileItems.clear();
        this.detailsFileItems.addAll(detailsFileItems);
        notifyChanged();
    }

    public SeafEvent getSelectedEvent() {
        return selectedEvent;
    }

    private LinearLayout mFooterView;

    public void setFooterViewLoading(boolean more) {
        if (mFooterView == null) {
            return;
        }
        mFooterView.setVisibility(more ? View.VISIBLE : View.GONE);
    }

    public void setState(int state) {
        this.state = state;
    }

    public View getFooterView() {
        return this.mFooterView;
    }

    private void setListViewHeightBasedOnChildren(ListView listView, EventDetailAdapter adapter) {
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = Utils.dip2px(mActivity, 30) * adapter.getCount() + (listView.getDividerHeight() * (adapter.getCount() - 1));
        if (height > 300) height = 300;
        params.height = height;
        listView.setLayoutParams(params);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == getCount() - 1) {
            this.mFooterView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_load_more, null);
            switch (state) {
                case REFRESH_ON_NONE:
                case REFRESH_ON_PULL_DOWN:
                    setFooterViewLoading(false);
                    break;
                case REFRESH_ON_PULL_UP:
                    setFooterViewLoading(true);
                    break;
            }
            return mFooterView;
        }
        if (position < 0) {
            position = 0;
        }

        final SeafEvent item = items.get(position);
        View view = convertView;
        // TODO optimize by setting tags
        final ViewHolder viewHolder;

        view = LayoutInflater.from(mActivity).inflate(R.layout.list_item_activities, null);
        RelativeLayout rl_old = (RelativeLayout) view.findViewById(R.id.rl_activities_old);

        CardView viewCard = view.findViewById(R.id.view_card);
        TextView title = (TextView) view.findViewById(R.id.tv_activities_mod_desc);
        TextView nick = (TextView) view.findViewById(R.id.tv_activities_nick);
        TextView date = (TextView) view.findViewById(R.id.tv_activities_date);
        TextView repoName = (TextView) view.findViewById(R.id.tv_activities_repo_name);
        CircleImageView icon = (CircleImageView) view.findViewById(R.id.iv_activities_avatar);

        View rl_new = (View) view.findViewById(R.id.rl_activities_new);
        CircleImageView icon_url = (CircleImageView) view.findViewById(R.id.iv_activities_avatar_url);
        TextView tv_name = (TextView) view.findViewById(R.id.tv_activities_name);
        CardView tv_state_card = (CardView) view.findViewById(R.id.tv_activities_state_card);
        TextView tv_state = (TextView) view.findViewById(R.id.tv_activities_state);
        View tv_icon_layout = (View) view.findViewById(R.id.tv_activities_icon_layout);
        ImageView tv_icon = (ImageView) view.findViewById(R.id.tv_activities_icon);
        TextView tv_desc = (TextView) view.findViewById(R.id.tv_activities_desc);
        TextView tv_time = (TextView) view.findViewById(R.id.tv_activities_time);
        TextView tv_mod = (TextView) view.findViewById(R.id.tv_activities_mod);
        CardView tv_history_changes_card = (CardView) view.findViewById(R.id.tv_history_changes_card);
        ImageView tv_history_changes_image = (ImageView) view.findViewById(R.id.tv_history_changes_image);
        ListView tv_history_changes_list = (ListView) view.findViewById(R.id.tv_history_changes_list);
        View rl_ce = view.findViewById(R.id.rl_activities_ce);
        TextView ce_author_text = (TextView)view.findViewById(R.id.ce_author_text);
        TextView ce_overview_text = (TextView)view.findViewById(R.id.ce_overview_text);
        TextView ce_overview_again_text = (TextView)view.findViewById(R.id.ce_overview_again_text);
        TextView ce_time_text = (TextView)view.findViewById(R.id.ce_time_text);
        TextView ce_repo_text = (TextView)view.findViewById(R.id.ce_repo_text);
        CardView ce_history_changes_card = (CardView)view.findViewById(R.id.ce_history_changes_card);
        ImageView ce_history_changes_image = (ImageView) view.findViewById(R.id.ce_history_changes_image);
        ListView ce_history_changes_list = (ListView)view.findViewById(R.id.ce_history_changes_list);
        viewHolder = new ViewHolder(viewCard, title, nick, date, repoName, icon, tv_name, tv_state_card, tv_state, tv_icon_layout, tv_icon, tv_desc, tv_time, tv_mod, tv_history_changes_card, tv_history_changes_image, tv_history_changes_list, icon_url, rl_old, rl_new, rl_ce, ce_author_text, ce_overview_text, ce_overview_again_text, ce_time_text, ce_repo_text, ce_history_changes_card, ce_history_changes_image, ce_history_changes_list);
        view.setTag(viewHolder);

        if (!isProEdition) {
            rl_old.setVisibility(View.GONE);
            rl_new.setVisibility(View.GONE);
            rl_ce.setVisibility(View.VISIBLE);
            viewHolder.ce_author_text.setText(item.getAuthor_name());
            viewHolder.ce_overview_text.setText(item.getDesc());
            viewHolder.ce_overview_again_text.setText(item.getDesc());
            viewHolder.ce_overview_again_text.setVisibility(View.GONE);
            viewHolder.ce_time_text.setText(SystemSwitchUtils.parseDateTime(item.getV_time()));
            viewHolder.ce_repo_text.setText(item.getRepo_name());
            viewHolder.ce_history_changes_card.setOnClickListener(v -> mFragment.listItemClick(item));
            viewHolder.ce_history_changes_image.setImageResource(R.drawable.ic_down);
            if (selectedEvent != null) {
                if (item.equals(selectedEvent)) {
                    viewHolder.ce_history_changes_image.setImageResource(R.drawable.ic_up);
                    final EventDetailAdapter adapter = new EventDetailAdapter(mActivity, detailsFileItems);
                    viewHolder.ce_history_changes_list.setVisibility(View.VISIBLE);
                    viewHolder.ce_history_changes_list.setAdapter(adapter);
                    viewHolder.ce_history_changes_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final EventDetailsFileItem fileItem = detailsFileItems.get(position);
                            mFragment.onItemClicked(fileItem);
                        }
                    });
                    viewHolder.ce_overview_again_text.setVisibility(View.VISIBLE);
                    setListViewHeightBasedOnChildren(viewHolder.ce_history_changes_list, adapter);
                } else {
                    viewHolder.ce_overview_again_text.setVisibility(View.GONE);
                    viewHolder.ce_history_changes_list.setVisibility(View.GONE);
                }
            }
            return view;
        }
        rl_ce.setVisibility(View.GONE);
        viewHolder.viewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mFragment.listItemClick(item);
            }
        });
        if (useNewActivity) {
            rl_old.setVisibility(View.GONE);
            rl_new.setVisibility(View.VISIBLE);
            item.setAvatar(item.getAvatar_url());
            viewHolder.tv_name.setText(item.getAuthor_name());
            loader.displayImage(item.getAvatar_url(), viewHolder.icon_url, options);
            viewHolder.tv_time.setText(SystemSwitchUtils.parseDateTime(item.getV_time()));
            viewHolder.tv_mod.setText(item.getRepo_name());
            if (item.getName().isEmpty()) {
                viewHolder.tv_icon_layout.setVisibility(View.GONE);
                viewHolder.tv_icon.setImageResource(android.R.color.transparent);
            } else {
                viewHolder.tv_icon_layout.setVisibility(View.VISIBLE);
                viewHolder.tv_icon.setImageResource(Utils.getFileIcon(item.getName()));
            }
            viewHolder.tv_desc.setText(item.getName());
            viewHolder.tv_state.setText(SystemSwitchUtils.obj_type(mActivity, item.getObj_type(), item.getOp_type()));
            viewHolder.tv_state_card.setCardBackgroundColor(SystemSwitchUtils.op_type(mActivity, item.getObj_type(), item.getOp_type()));
            if (viewHolder.tv_state.getText().toString().isEmpty()) {
                tv_state_card.setVisibility(View.INVISIBLE);
            } else {
                tv_state_card.setVisibility(View.VISIBLE);
            }
            viewHolder.tv_history_changes_card.setOnClickListener(v -> mFragment.listItemClick(item));
            viewHolder.tv_history_changes_card.setVisibility(
                    mActivity.getDataManager().getCachedRepoByID(item.getRepo_id()) == null?
                            View.GONE : View.VISIBLE
            );
            viewHolder.tv_history_changes_image.setImageResource(R.drawable.ic_down);
            if (selectedEvent != null) {
                if (item.equals(selectedEvent)) {
                    viewHolder.tv_history_changes_image.setImageResource(R.drawable.ic_up);
                    final EventDetailAdapter adapter = new EventDetailAdapter(mActivity, detailsFileItems);
                    viewHolder.tv_history_changes_list.setVisibility(View.VISIBLE);
                    viewHolder.tv_history_changes_list.setAdapter(adapter);
                    viewHolder.tv_history_changes_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final EventDetailsFileItem fileItem = detailsFileItems.get(position);
                            mFragment.onItemClicked(fileItem);
                        }
                    });
                    setListViewHeightBasedOnChildren(viewHolder.tv_history_changes_list, adapter);
                } else {
                    viewHolder.ce_overview_again_text.setVisibility(View.GONE);
                    viewHolder.tv_history_changes_list.setVisibility(View.GONE);
                }
            }
        } else {
            rl_old.setVisibility(View.VISIBLE);
            rl_new.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(item.getAvatar())) {
            final String avatar = parseAvatar(item.getAvatar());
            loader.displayImage(avatar, viewHolder.icon, options);
        } else {
            // show a place holder indicating the error
            loader.displayImage(item.getAvatar(), viewHolder.icon, options);
        }

        viewHolder.title.setText(item.getDesc());
        viewHolder.nick.setText(item.getNick());

        if (!TextUtils.isEmpty(item.getTime_relative())) {
            final String relative = parseRelativeTime(item.getTime_relative());
            viewHolder.date.setText(relative);
            viewHolder.date.setVisibility(View.VISIBLE);
        } else {
            viewHolder.date.setVisibility(View.GONE);
        }
        viewHolder.repoName.setText(item.getRepo_name());
        return view;
    }

    private String parseAvatar(@NonNull String avatar) {
        // <img src="/seahub/image-view/avatars/7/9/dc411b7a64a20963ccff32563e38d6/resized/36/bamboo_5.png" width="36" height="36" class="avatar" />
        String re1 = ".*?";   // Non-greedy match on filler
        String re2 = "(src)"; // Variable Name 1
        String re3 = ".*?";   // Non-greedy match on filler
        String re4 = "((?:\\/[\\w\\.\\-]+)+)";    // Unix Path 1

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(avatar);
        if (m.find()) {
            String avatarPath = m.group(2);
            return Utils.pathJoin(mActivity.getAccount().getServer(), avatarPath);
        } else return avatar;
    }

    private String parseRelativeTime(@NonNull String relativeTime) {
        String regex = "(<[^>]+>)";
        final String[] split = relativeTime.split(regex);
        if (split.length > 1) {
            return split[1];
        } else return relativeTime;
    }

    private static class ViewHolder {
        CardView viewCard;
        TextView title, nick, date, repoName, ce_author_text, ce_overview_text, ce_overview_again_text, ce_time_text, ce_repo_text;
        ImageView icon, icon_url, tv_icon, tv_history_changes_image, ce_history_changes_image;
        TextView tv_name, tv_state, tv_desc, tv_mod, tv_time;
        CardView tv_state_card, tv_history_changes_card, ce_history_changes_card;
        View rl_old, rl_new, rl_ce, tv_icon_layout;
        ListView tv_history_changes_list, ce_history_changes_list;
        public ViewHolder(CardView viewCard, TextView title, TextView nick, TextView date, TextView repoName, ImageView icon, TextView tv_name,
                          CardView tv_state_card, TextView tv_state, View tv_icon_layout, ImageView tv_icon, TextView tv_desc, TextView tv_time,
                          TextView tv_mod, CardView tv_history_changes_card, ImageView tv_history_changes_image, ListView tv_history_changes_list, ImageView icon_url, RelativeLayout rl_old, View rl_new, View rl_ce,
                          TextView ce_author_text, TextView ce_overview_text, TextView ce_overview_again_text, TextView ce_time_text, TextView ce_repo_text, CardView ce_history_changes_card, ImageView ce_history_changes_image, ListView ce_history_changes_list) {
            super();
            this.viewCard = viewCard;
            this.icon = icon;
            this.title = title;
            this.nick = nick;
            this.date = date;
            this.repoName = repoName;
            this.icon_url = icon_url;
            this.tv_name = tv_name;
            this.tv_state_card = tv_state_card;
            this.tv_state = tv_state;
            this.tv_icon_layout = tv_icon_layout;
            this.tv_icon = tv_icon;
            this.tv_desc = tv_desc;
            this.tv_time = tv_time;
            this.tv_mod = tv_mod;
            this.tv_history_changes_card = tv_history_changes_card;
            this.tv_history_changes_image = tv_history_changes_image;
            this.tv_history_changes_list = tv_history_changes_list;
            this.rl_old = rl_old;
            this.rl_new = rl_new;
            this.rl_ce = rl_ce;
            this.ce_author_text = ce_author_text;
            this.ce_overview_text = ce_overview_text;
            this.ce_overview_again_text = ce_overview_again_text;
            this.ce_time_text = ce_time_text;
            this.ce_repo_text = ce_repo_text;
            this.ce_history_changes_card = ce_history_changes_card;
            this.ce_history_changes_image = ce_history_changes_image;
            this.ce_history_changes_list = ce_history_changes_list;
        }
    }

}
