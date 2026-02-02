package com.seafile.seadroid2.ui.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.common.collect.Lists;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.account.AccountManager;
import com.seafile.seadroid2.avatar.Avatar;
import com.seafile.seadroid2.cameraupload.CameraUploadConfigActivity;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.DatabaseHelper;
import com.seafile.seadroid2.folderbackup.FolderBackupConfigActivity;
import com.seafile.seadroid2.ui.activity.AccountsActivity;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.activity.SeafilePathChooserActivity;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Base account adapter
 */
public class AccountAdapterNew2 extends BaseAdapter {
    private static final String DEBUG_TAG = "AccountAdapter";
    private final String EDIT = "edit";
    private final String DELETE = "delete";
    private final String LOGOUT = "logout";

    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    private DisplayImageOptions options;
    private ArrayList<Account> items;
    private ArrayList<Avatar> avatars;
    private Context context;
    private AccountManager accountManager;
    public enum AccountType {AccountActivity, AccountFragment, PathChooserActivity, CloudLibrarySelectionFragment, CloudLibraryChooserFragment}
    private AccountType accountType;

    public AccountAdapterNew2(Context context, AccountManager accountManager, AccountType accountType) {
        this.context = context;
        this.accountManager = accountManager;
        this.accountType = accountType;
        items = Lists.newArrayList();
        avatars = Lists.newArrayList();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void add(Account entry) {
        items.add(entry);
    }

    public void notifyChanged() {
        notifyDataSetChanged();
    }

    @Override
    public Account getItem(int position) {
        return items.get(position);
    }

    public void setItems(List<Account> items) {
        this.items = (ArrayList<Account>) items;
        notifyDataSetChanged();
        
    }

    public void setAvatars(ArrayList<Avatar> avatars) {
        this.avatars = avatars;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        items.clear();
    }

    private ViewHolder viewHolder;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_account_entry_new, null);
            CardView viewCard = (CardView) view.findViewById(R.id.view_card);
            View viewLayout = (View) view.findViewById(R.id.view_layout);
            TextView title = (TextView) view.findViewById(R.id.list_item_account_title);
            TextView subtitle = (TextView) view.findViewById(R.id.list_item_account_subtitle);
            ImageView icon = (ImageView) view.findViewById(R.id.list_item_account_icon);
            CardView typeCard = (CardView) view.findViewById(R.id.type_card);
            TextView typeText = (TextView) view.findViewById(R.id.type_text);
            CardView moreCard = (CardView) view.findViewById(R.id.more_card);
            ViewGroup moreLayout = (ViewGroup) view.findViewById(R.id.more_layout);
            CardView editCard = (CardView) view.findViewById(R.id.edit_card);
            CardView deleteCard = (CardView) view.findViewById(R.id.delete_card);
            CardView logoutCard = (CardView) view.findViewById(R.id.logout_card);

            viewHolder = new ViewHolder(viewCard, viewLayout, title, subtitle, icon, typeCard, typeText, moreCard, moreLayout, editCard, deleteCard, logoutCard);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (accountType == AccountType.AccountActivity) {
            viewHolder.icon.getDrawable().setColorFilter(context.getResources().getColor(R.color.luckycloud_green), PorterDuff.Mode.SRC_IN );
            viewHolder.subtitle.setTextColor(context.getResources().getColor(R.color.luckycloud_green));
        } else {
            viewHolder.icon.getDrawable().setColorFilter(context.getResources().getColor(R.color.luckycloud_green), PorterDuff.Mode.SRC_IN );
            viewHolder.subtitle.setTextColor(context.getResources().getColor(R.color.text_view_color));
        }

        Account account = items.get(position);
        viewHolder.title.setText(account.getServerHost());
//        viewHolder.subtitle.setText(account.getEmail());
        viewHolder.subtitle.setText(account.getName());
        if (getAvatarUrl(account) != null) {
            options = new DisplayImageOptions.Builder()
                    .extraForDownloader(account)
                    .showStubImage(R.drawable.ic_default_account)
                    // .delayBeforeLoading(1000)
                    .showImageOnLoading(R.drawable.ic_default_account)
                    .showImageForEmptyUri(R.drawable.ic_default_account)
                    .showImageOnFail(R.drawable.ic_default_account)
                    .resetViewBeforeLoading()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .imageScaleType(ImageScaleType.NONE)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new RoundedBitmapDisplayer(1000))
                    .build();
            ImageLoader.getInstance().displayImage(getAvatarUrl(account), viewHolder.icon, options, animateFirstListener);
        }

        TransitionManager.beginDelayedTransition(viewHolder.moreLayout);
        ViewGroup.LayoutParams layoutParamsAccountInfo1 = viewHolder.moreLayout.getLayoutParams();
        layoutParamsAccountInfo1.height = account.collapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        viewHolder.moreLayout.requestLayout();
//        mAccountInfoDirectionImage.setImageDrawable(getResources().getDrawable((accountInfoCollapse) ? R.drawable.ic_left : R.drawable.ic_down));

        viewHolder.viewCard.setOnClickListener(v -> {
            if (accountType == AccountType.AccountActivity) {
                ((AccountsActivity)context).onAccountClick(position);
            } else if (accountType == AccountType.AccountFragment) {
                ((BrowserActivity)context).getAccountsFragment().onAccountClick(position);
            } else if (accountType == AccountType.PathChooserActivity) {
                ((SeafilePathChooserActivity)context).onListItemClick(position);
            } else if (accountType == AccountType.CloudLibrarySelectionFragment) {
                ((CameraUploadConfigActivity)context).getCloudLibraryFragment().getAccountOrReposSelectionFragment().onListItemClick(position);
            } else if (accountType == AccountType.CloudLibraryChooserFragment) {
                ((FolderBackupConfigActivity)context).getCloudLibFragment().onListItemClick(position);
            }
        });

        String type = context.getResources().getString(R.string.enterprise);
        if (account.server.equals(context.getString(R.string.server_url_sync))) {
            type = context.getResources().getString(R.string.home_teams);
            viewHolder.typeCard.setCardBackgroundColor(context.getResources().getColor(R.color.luckycloud_green));
        }
        if (account.server.equals(context.getString(R.string.server_url_seacloud))) {
            type = context.getResources().getString(R.string.business);
            viewHolder.typeCard.setCardBackgroundColor(context.getResources().getColor(R.color.dots_type_grey));
        }
        viewHolder.typeText.setText(type);

        if (accountType == AccountType.PathChooserActivity || accountType == AccountType.CloudLibrarySelectionFragment || accountType == AccountType.CloudLibraryChooserFragment) {
            if (accountType == AccountType.CloudLibrarySelectionFragment || accountType == AccountType.CloudLibraryChooserFragment)
                viewHolder.viewLayout.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.window_background));
            viewHolder.viewLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_account_background));
            viewHolder.moreCard.setVisibility(View.GONE);
            return view;
        }

        viewHolder.moreCard.setOnClickListener(v -> {
            boolean collapse = account.collapse;
            for(Account a: items) {
                a.collapse = true;
            }
            account.collapse = !collapse;
            notifyChanged();
        });

        viewHolder.viewCard.setOnLongClickListener(v -> {
            boolean collapse = account.collapse;
            for(Account a: items) {
                a.collapse = true;
            }
            account.collapse = !collapse;
            notifyChanged();
            return true;
        });

        // if (!account.hasValidToken()) {
        if (account.email.equals(getCurrentUserIdentifier()) && account.server.equals(getCurrentServer())) {
            viewHolder.logoutCard.setVisibility(View.VISIBLE);
            viewHolder.viewLayout.setBackground(context.getDrawable(R.drawable.rounded_account_background_border));
        } else {
            viewHolder.logoutCard.setVisibility(View.GONE);
            viewHolder.viewLayout.setBackground(context.getDrawable(R.drawable.rounded_account_background));
        }

        viewHolder.editCard.setOnClickListener(v -> {
//            showConfirmDialog(EDIT, account);
            if (accountType == AccountType.AccountActivity) {
                ((AccountsActivity)context).onAccountEdit(account);
            } else {
                ((BrowserActivity)context).getAccountsFragment().onAccountEdit(account);
            }
        });

        viewHolder.deleteCard.setOnClickListener(v -> {
            showConfirmDialog(DELETE, account);
        });

        viewHolder.logoutCard.setOnClickListener(v -> {
            showConfirmDialog(LOGOUT, account);
        });

        return view;
    }

    public String getCurrentUserIdentifier() {
        Account account = accountManager.getCurrentAccount();

        if (account == null)
            return "";

        return account.getEmail();
    }

    public String getCurrentServer() {
        Account account = accountManager.getCurrentAccount();

        if (account == null)
            return "";

        return account.server;
    }

    private void showConfirmDialog(String dialogType, Account account) {
        Dialog dialog = Utils.CustomDialog(context);
        dialog.setContentView(R.layout.dialog_account_edit);

        TextView titleText = dialog.findViewById(R.id.title_text);
        TextView infoText = dialog.findViewById(R.id.info_text);
        CardView cancelCard = dialog.findViewById(R.id.cancel_card);
        CardView okCard = dialog.findViewById(R.id.ok_card);
        TextView okText = dialog.findViewById(R.id.ok_text);

        String title = "";

        switch (dialogType) {
            case EDIT:
                title = context.getString(R.string.edit);
                okCard.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (accountType == AccountType.AccountActivity) {
                        ((AccountsActivity)context).onAccountEdit(account);
                    } else {
                        ((BrowserActivity)context).getAccountsFragment().onAccountEdit(account);
                    }
                });
                break;
            case DELETE:
                title = context.getString(R.string.delete);
                okCard.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (accountType == AccountType.AccountActivity) {
                        ((AccountsActivity)context).onAccountDelete(account);
                    } else {
                        ((BrowserActivity)context).getAccountsFragment().onAccountDelete(account);
                    }
                });
                break;
            case LOGOUT:
                title = context.getString(R.string.logout);
                okCard.setOnClickListener(v -> {
                    dialog.dismiss();
//                    if (isActivity) {
//                        ((AccountsActivity)context).onAccountLogout(account);
//                    } else {
//                        ((BrowserActivity)context).getAccountsFragment().onAccountLogout(account);
//                    }
//                    notifyChanged();
                    Account currentAccount = accountManager.getCurrentAccount();

                    // sign out operations
                    accountManager.signOutAccount(currentAccount);

                    // password auto clear
                    if (SettingsManager.instance().isPasswordAutoClearEnabled()) {
                        Utils.clearPasswordSilently(Lists.newArrayList());
                    }

                    // restart BrowserActivity (will go to AccountsActivity)
                    Intent intent = new Intent(context, BrowserActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                    if (accountType == AccountType.AccountActivity) {
                        ((AccountsActivity)context).finish();
                    } else {
                        ((BrowserActivity)context).finish();
                    }
                });
                break;
            default:
                break;
        }

        titleText.setText(title.toLowerCase());
        infoText.setText(context.getString(R.string.account_edit, title.toLowerCase()));
        okText.setText(title);
        cancelCard.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private String getAvatarUrl(Account account) {
        if (avatars == null) {
            return null;
        }
        for (Avatar avatar : avatars) {
            if (avatar.getSignature().equals(account.getSignature())) {
                return avatar.getUrl();
            }
        }

        return null;
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    private static class ViewHolder {
        View viewLayout;
        TextView title, subtitle, typeText;
        ImageView icon;
        CardView viewCard, moreCard, typeCard, editCard, deleteCard, logoutCard;
        ViewGroup moreLayout;

        public ViewHolder(CardView viewCard, View viewLayout, TextView title, TextView subtitle, ImageView icon, CardView typeCard, TextView typeText, CardView moreCard, ViewGroup moreLayout, CardView editCard, CardView deleteCard, CardView logoutCard) {
            super();
            this.viewCard = viewCard;
            this.viewLayout = viewLayout;
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
            this.typeCard = typeCard;
            this.typeText = typeText;
            this.moreLayout = moreLayout;
            this.moreCard = moreCard;
            this.editCard = editCard;
            this.deleteCard = deleteCard;
            this.logoutCard = logoutCard;
        }
    }
}
