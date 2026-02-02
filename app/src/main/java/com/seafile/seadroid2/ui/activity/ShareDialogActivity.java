package com.seafile.seadroid2.ui.activity;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.transition.Visibility;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.account.AccountManager;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.data.ServerInfo;
import com.seafile.seadroid2.ui.WidgetUtils;
import com.seafile.seadroid2.ui.fragment.ShareCustomPermissionFragment;
import com.seafile.seadroid2.ui.fragment.ShareDownloadFragment;
import com.seafile.seadroid2.ui.fragment.ShareGroupFragment;
import com.seafile.seadroid2.ui.fragment.ShareInternalFragment;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


/**
 * Camera upload configuration helper
 */
public class ShareDialogActivity extends BaseActivity {
    public String DEBUG_TAG = "ShareActivity";

    public static final String SHARE_DIALOG_FILE_NAME = "ShareDialogFileName";
    public static final String SHARE_DIALOG_TYPE = "ShareDialogType";
    public static final String SHARE_DIALOG_ACCOUNT = "ShareDialogAccount";
    public static final String SHARE_DIALOG_REPO = "ShareDialogRepo";
    public static final String SHARE_DIALOG_PATH = "ShareDialogPath";
    public static final String SHARE_DIALOG_CAN_LOCAL_DECRYPT = "ShareDialogCanLocalDecrypt";

    public static final String SHARE_DIALOG_FOR_REPO = "shareDialogForRepo";
    public static final String SHARE_DIALOG_FOR_DIR = "shareDialogForDir";
    public static final String SHARE_DIALOG_FOR_FILE = "shareDialogForFile";

    public static final int INDEX_HOME_TAB = 0;
    public static final int INDEX_DOWNLOAD_LINK_TAB = 1;
    public static final int INDEX_UPLOAD_LINK_TAB = 2;
    public static final int INDEX_SHARE_USER_TAB = 3;
    public static final int INDEX_INTERNAL_LINK_TAB = 6;
    public static final int INDEX_SHARE_GROUP_TAB = 4;
    public static final int INDEX_CUSTOM_SHARING_PERMISSION_TAB = 5;

    private ShareGroupFragment shareGroupFragment;

    public CardView progressCard;
    private CardView closeCard;
    private TextView titleText;
    private TextView nameText;
    private TextView whatWantText;
    private View backLayout;
    private CardView backCard;
    private View selectionLayout;
    private View externalShareLayout;
    private CardView externalReleaseCard;
    private CardView internalReleaseCard;
    private ScrollView scrollView;
    private View downloadLinkLayout;
    private CardView downloadLinkCard;
    private View uploadLinkLayout;
    private CardView uploadLinkCard;
    private View shareUserLayout;
    private CardView shareUserCard;
    private View internalLinkLayout;
    private CardView internalLinkCard;
    private View shareGroupLayout;
    private CardView shareGroupCard;
    private View customSharingPermissionLayout;
    private CardView customSharingPermissionCard;
    private ImageView downloadLinkDirectionImage;
    private ImageView uploadLinkDirectionImage;
    private ImageView shareUserDirectionImage;
    private ImageView internalLinkDirectionImage;
    private ImageView shareGroupDirectionImage;
    private ImageView customSharingPermissionDirectionImage;
    private FrameLayout downloadLinkFrame;
    private FrameLayout uploadLinkFrame;
    private FrameLayout shareUserFrame;
    private FrameLayout internalLinkFrame;
    private FrameLayout shareGroupFrame;
    private FrameLayout customSharingPermissionFrame;
    private View mainLayout;
    private FrameLayout customSharingPermissionFrame2;

    private FragmentManager fragmentManager;

    public String fileName;
    public String dialogType;
    public Account account;
    private AccountManager accountManager;
    public SeafRepo repo;
    public String path;
    private ShareDialogActivity mShareDialogActivity;

    private boolean downloadLinkCollapse = true;
    private boolean uploadLinkCollapse = true;
    private boolean shareUserCollapse = true;
    private boolean internalLinkCollapse = true;
    private boolean shareGroupCollapse = true;
    private boolean customSharingPermissionCollapse = true;

    private boolean downloadLinkFirst = true;
    private boolean uploadLinkFirst = true;
    private boolean shareUserFirst = true;
    private boolean internalLinkFirst = true;
    private boolean shareGroupFirst = true;
    private boolean customSharingPermissionFirst = true;
    private boolean customSharingPermissionFirst2 = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        setTheme(R.style.AppTheme_Base);
        setContentView(R.layout.dialog_share);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        });

        initView();
        init();
        initViewAction();
    }

    private void initView() {
        progressCard = findViewById(R.id.progress_card);
        closeCard = findViewById(R.id.close_card);
        titleText = findViewById(R.id.title_text);
        nameText = findViewById(R.id.name_text);
        whatWantText = findViewById(R.id.what_want_text);
        backLayout = findViewById(R.id.back_layout);
        backCard = findViewById(R.id.back_card);
        selectionLayout = findViewById(R.id.selection_layout);
        externalShareLayout = findViewById(R.id.external_share_layout);
        externalReleaseCard = findViewById(R.id.external_share_card);
        internalReleaseCard = findViewById(R.id.internal_share_card);
        scrollView = findViewById(R.id.scroll_view);
        downloadLinkLayout = findViewById(R.id.download_link_layout);
        downloadLinkCard = findViewById(R.id.download_link_card);
        uploadLinkLayout = findViewById(R.id.upload_link_layout);
        uploadLinkCard = findViewById(R.id.upload_link_card);
        shareUserLayout = findViewById(R.id.share_user_layout);
        shareUserCard = findViewById(R.id.share_user_card);
        internalLinkLayout = findViewById(R.id.internal_link_layout);
        internalLinkCard = findViewById(R.id.internal_link_card);
        shareGroupLayout = findViewById(R.id.share_group_layout);
        shareGroupCard = findViewById(R.id.share_group_card);
        customSharingPermissionLayout = findViewById(R.id.custom_sharing_permission_layout);
        customSharingPermissionCard = findViewById(R.id.custom_sharing_permission_card);
        downloadLinkDirectionImage = findViewById(R.id.download_link_direction_image);
        uploadLinkDirectionImage = findViewById(R.id.upload_link_direction_image);
        shareUserDirectionImage = findViewById(R.id.share_user_direction_image);
        internalLinkDirectionImage = findViewById(R.id.internal_link_direction_image);
        shareGroupDirectionImage = findViewById(R.id.share_group_direction_image);
        customSharingPermissionDirectionImage = findViewById(R.id.custom_sharing_permission_direction_image);
        downloadLinkFrame = findViewById(R.id.download_link_frame);
        uploadLinkFrame = findViewById(R.id.upload_link_frame);
        shareUserFrame = findViewById(R.id.share_user_frame);
        internalLinkFrame = findViewById(R.id.internal_link_frame);
        shareGroupFrame = findViewById(R.id.share_group_frame);
        customSharingPermissionFrame = findViewById(R.id.custom_sharing_permission_frame);
        mainLayout = findViewById(R.id.main_layout);
        customSharingPermissionFrame2 = findViewById(R.id.custom_sharing_permission_frame2);

        updateSelection(true);
    }

    private void initViewAction() {
        closeCard.setOnClickListener(v -> {
            finish();
        });
        externalReleaseCard.setOnClickListener(v -> {
            updateScrollView(true);
            updateSelection(false);
        });
        internalReleaseCard.setOnClickListener(v -> {
            updateScrollView(false);
            updateSelection(false);
        });
        backCard.setOnClickListener(v -> {
            upgradeCollapse(0, false);
            updateSelection(true);
        });
        downloadLinkCard.setOnClickListener(v -> {
            upgradeCollapse(INDEX_DOWNLOAD_LINK_TAB, downloadLinkCollapse);
            if (downloadLinkFirst) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.download_link_frame, new ShareDownloadFragment(true), "ShareDownloadFragment");
                transaction.addToBackStack(null);
                transaction.commit();
                downloadLinkFirst = !downloadLinkFirst;
            }
        });
        uploadLinkCard.setOnClickListener(v -> {
            upgradeCollapse(INDEX_UPLOAD_LINK_TAB, uploadLinkCollapse);
            if (uploadLinkFirst) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.upload_link_frame, new ShareDownloadFragment(false), "ShareUploadFragment");
                transaction.addToBackStack(null);
                transaction.commit();
                uploadLinkFirst = !uploadLinkFirst;
            }
        });
        shareUserCard.setOnClickListener(v -> {
            upgradeCollapse(INDEX_SHARE_USER_TAB, shareUserCollapse);
            if (shareUserFirst) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                shareGroupFragment = new ShareGroupFragment(ShareGroupFragment.SHARE_TYPE_USER);
                transaction.add(R.id.share_user_frame, shareGroupFragment, "ShareUserFragment");
                transaction.addToBackStack(null);
                transaction.commit();
                shareUserFirst = !shareUserFirst;
            }
        });
        internalLinkCard.setOnClickListener(v -> {
            upgradeCollapse(INDEX_INTERNAL_LINK_TAB, internalLinkCollapse);
            if (internalLinkFirst) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.internal_link_frame, new ShareInternalFragment(), "ShareInternalFragment");
                transaction.addToBackStack(null);
                transaction.commit();
                internalLinkFirst = !internalLinkFirst;
            }
        });
        shareGroupCard.setOnClickListener(v -> {
            upgradeCollapse(INDEX_SHARE_GROUP_TAB, shareGroupCollapse);
            if (shareGroupFirst) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                shareGroupFragment = new ShareGroupFragment(ShareGroupFragment.SHARE_TYPE_GROUP);
                transaction.add(R.id.share_group_frame, shareGroupFragment, "ShareGroupFragment");
                transaction.addToBackStack(null);
                transaction.commit();
                shareGroupFirst = !shareGroupFirst;
            }
        });
        customSharingPermissionCard.setOnClickListener(v -> {
            upgradeCollapse(INDEX_CUSTOM_SHARING_PERMISSION_TAB, customSharingPermissionCollapse);
            if (customSharingPermissionFirst) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.custom_sharing_permission_frame, new ShareCustomPermissionFragment(), "ShareCustomPermissionFragment");
                transaction.addToBackStack(null);
                transaction.commit();
                customSharingPermissionFirst = !customSharingPermissionFirst;
            }
        });
    }

    private void init() {
        mShareDialogActivity = this;
        fileName = getIntent().getStringExtra(SHARE_DIALOG_FILE_NAME);
        dialogType = getIntent().getStringExtra(SHARE_DIALOG_TYPE);
        account = (Account)getIntent().getParcelableExtra(SHARE_DIALOG_ACCOUNT);
        accountManager = new AccountManager(this);
        String repoString = getIntent().getStringExtra(SHARE_DIALOG_REPO);
        if (repoString != null) {
            JSONObject object = Utils.parseJsonObject(repoString);
            if (object != null) {
                try {
                    repo = SeafRepo.fromJson(object);
                    if (repo.canLocalDecrypt()) {
                        externalShareLayout.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
        if (repo == null) finish();
        path = getIntent().getStringExtra(SHARE_DIALOG_PATH);

        titleText.setText(getResources().getString(R.string.file_share));
        if (fileName != null) nameText.setText(fileName);

        fragmentManager = getSupportFragmentManager();
    }

    private void updateSelection(boolean show) {
        backLayout.setVisibility(show? View.GONE : View.VISIBLE);
        scrollView.setVisibility(show? View.GONE : View.VISIBLE);

        whatWantText.setVisibility(show? View.VISIBLE : View.GONE);
        selectionLayout.setVisibility(show? View.VISIBLE : View.GONE);
    }

    private void updateScrollView(boolean isExternal) {
        downloadLinkLayout.setVisibility(isExternal? View.VISIBLE : View.GONE);
        uploadLinkLayout.setVisibility(isExternal? View.VISIBLE : View.GONE);

        shareUserLayout.setVisibility(isExternal? View.GONE : View.VISIBLE);
        shareGroupLayout.setVisibility(isExternal? View.GONE : View.VISIBLE);
        internalLinkLayout.setVisibility(isExternal? View.GONE : View.VISIBLE);

        customSharingPermissionLayout.setVisibility(View.GONE);

        boolean canLocalDecrypt = repo.canLocalDecrypt();

        if (dialogType.equals(ShareDialogActivity.SHARE_DIALOG_FOR_REPO)) {
            if (canLocalDecrypt) {
                downloadLinkLayout.setVisibility(View.GONE);
                uploadLinkLayout.setVisibility(View.GONE);
            }
            internalLinkLayout.setVisibility(View.GONE);
        }
        if (dialogType.equals(ShareDialogActivity.SHARE_DIALOG_FOR_FILE)) {
            uploadLinkLayout.setVisibility(View.GONE);
            shareUserLayout.setVisibility(View.GONE);
            shareGroupLayout.setVisibility(View.GONE);
            customSharingPermissionLayout.setVisibility(View.GONE);
        }
    }

    private void upgradeCollapse(int index, boolean collapse) {
        collapse = !collapse;
        downloadLinkCollapse = true;
        uploadLinkCollapse = true;
        shareUserCollapse = true;
        internalLinkCollapse = true;
        shareGroupCollapse = true;
        customSharingPermissionCollapse = true;
        if (index == INDEX_DOWNLOAD_LINK_TAB) downloadLinkCollapse = collapse;
        if (index == INDEX_UPLOAD_LINK_TAB) uploadLinkCollapse = collapse;
        if (index == INDEX_SHARE_USER_TAB) shareUserCollapse = collapse;
        if (index == INDEX_INTERNAL_LINK_TAB) internalLinkCollapse = collapse;
        if (index == INDEX_SHARE_GROUP_TAB) shareGroupCollapse = collapse;
        if (index == INDEX_CUSTOM_SHARING_PERMISSION_TAB) customSharingPermissionCollapse = collapse;

        TransitionManager.beginDelayedTransition(downloadLinkFrame);
        ViewGroup.LayoutParams layoutParamsDownloadInfo = downloadLinkFrame.getLayoutParams();
        layoutParamsDownloadInfo.height = downloadLinkCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        downloadLinkFrame.requestLayout();
        downloadLinkDirectionImage.setImageDrawable(getResources().getDrawable((downloadLinkCollapse) ? R.drawable.ic_right : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(uploadLinkFrame);
        ViewGroup.LayoutParams layoutParamsUploadLink = uploadLinkFrame.getLayoutParams();
        layoutParamsUploadLink.height = uploadLinkCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        uploadLinkFrame.requestLayout();
        uploadLinkDirectionImage.setImageDrawable(getResources().getDrawable((uploadLinkCollapse) ? R.drawable.ic_right : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(shareUserFrame);
        ViewGroup.LayoutParams layoutParamsShareUser = shareUserFrame.getLayoutParams();
        layoutParamsShareUser.height = shareUserCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        shareUserFrame.requestLayout();
        shareGroupDirectionImage.setImageDrawable(getResources().getDrawable((shareUserCollapse) ? R.drawable.ic_right : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(internalLinkFrame);
        ViewGroup.LayoutParams layoutParamsInternalLink = internalLinkFrame.getLayoutParams();
        layoutParamsInternalLink.height = internalLinkCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        internalLinkFrame.requestLayout();
        internalLinkDirectionImage.setImageDrawable(getResources().getDrawable((internalLinkCollapse) ? R.drawable.ic_right : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(shareGroupFrame);
        ViewGroup.LayoutParams layoutParamsShareGroup = shareGroupFrame.getLayoutParams();
        layoutParamsShareGroup.height = shareGroupCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        shareGroupFrame.requestLayout();
        shareGroupDirectionImage.setImageDrawable(getResources().getDrawable((shareGroupCollapse) ? R.drawable.ic_right : R.drawable.ic_down));

        TransitionManager.beginDelayedTransition(customSharingPermissionFrame);
        ViewGroup.LayoutParams layoutParamsCache = customSharingPermissionFrame.getLayoutParams();
        layoutParamsCache.height = customSharingPermissionCollapse ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        customSharingPermissionFrame.requestLayout();
        customSharingPermissionDirectionImage.setImageDrawable(getResources().getDrawable((customSharingPermissionCollapse) ? R.drawable.ic_right : R.drawable.ic_down));
    }

    public void showCustomSharingPermission() {
        if (customSharingPermissionFirst2) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.custom_sharing_permission_frame2, new ShareCustomPermissionFragment(), "ShareCustomPermissionFragment");
            transaction.addToBackStack(null);
            transaction.commit();
            customSharingPermissionFirst2 = !customSharingPermissionFirst2;
        }

        customSharingPermissionFrame2.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
    }

    public void closeCustomSharingPermission() {
        customSharingPermissionFrame2.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
    }

    public void changeProgress(Boolean isShow) {
        Thread thread = new Thread(){
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressCard.setVisibility(isShow ? View.VISIBLE : View.GONE);
                    }
                });
            }
        };
        thread.start();
    }

    public void showCopyDialog(final String link) {
        WidgetUtils.chooseShareApp2(this, link, dialogType, fileName);
    }

    public void showQRDialog(final String link) {
        Dialog dialog = Utils.CustomDialog(this);
        dialog.setContentView(R.layout.dialog_qr);

        CardView closeCard = dialog.findViewById(R.id.close_card);
        ImageView mQRImage = dialog.findViewById(R.id.qr_image);
        CardView shareCard = dialog.findViewById(R.id.share_card);

        closeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        shareCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                WidgetUtils.chooseShareApp3(mShareDialogActivity, link, dialogType, fileName);
            }
        });

        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        QRGEncoder qrgEncoder = new QRGEncoder(link, null, QRGContents.Type.TEXT, dimen);
        qrgEncoder.setColorBlack(getResources().getColor(R.color.dialog_msg_background));
        qrgEncoder.setColorWhite(getResources().getColor(R.color.text_view_color));
        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.getBitmap();
            // Setting Bitmap to ImageView
            mQRImage.setImageBitmap(bitmap);
        } catch (Exception e) {
//            Log.v(TAG, e.toString());
        }

        dialog.show();
    }

    private void backPressed() {
        if (customSharingPermissionFrame2.getVisibility() == View.VISIBLE) {
            closeCustomSharingPermission();
            return;
        }
        if (backLayout.getVisibility() == View.VISIBLE) {
            backCard.callOnClick();
            return;
        }
        finish();
    }

    public ShareGroupFragment getShareGroupFragment() {
        return shareGroupFragment;
    }

    public boolean checkServerProEdition() {
        if (account == null)
            return false;

        ServerInfo serverInfo = accountManager.getServerInfo(account);

        return serverInfo.isProEdition();
    }
}