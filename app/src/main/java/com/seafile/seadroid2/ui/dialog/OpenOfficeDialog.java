package com.seafile.seadroid2.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.data.SelectedFileInfo;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.util.Utils;


public class OpenOfficeDialog extends DialogFragment {
    public static final String DEBUG_TAG = "OpenOfficeDialog";

    private BrowserActivity activity;
    private SelectedFileInfo fileInfo;
    private boolean isOpenWith;

    public OpenOfficeDialog() {
    }

    @SuppressLint("ValidFragment")
    public OpenOfficeDialog(BrowserActivity activity, SelectedFileInfo fileInfo, boolean isOpenWith) {
        this.activity = activity;
        this.fileInfo = fileInfo;
        this.isOpenWith = isOpenWith;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.dialog_open_office, null);

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        CardView closeCard = (CardView) view.findViewById(R.id.close_card);
        CardView onlyOfficeAppCard = (CardView) view.findViewById(R.id.onlyoffice_app_card);
        CardView inAppOfficeCard = (CardView) view.findViewById(R.id.in_app_office_card);
        ImageView inAppOfficeImage = (ImageView) view.findViewById(R.id.in_app_office_image);
        CardView externalAppCard = (CardView) view.findViewById(R.id.external_app_card);
        CardView openOfficeCard = (CardView) view.findViewById(R.id.open_office_card);


        int drawable = R.drawable.file_ms_word;
        if (Utils.isExcelMimeType(fileInfo.fileName)) {
            drawable = R.drawable.file_ms_excel;
        } else if (Utils.isPPTMimeType(fileInfo.fileName)) {
            drawable = R.drawable.file_ms_ppt;
        }
        inAppOfficeImage.setImageDrawable(ResourcesCompat.getDrawable(
                activity.getResources(),
                drawable,
                activity.getTheme()
        ));

        closeCard.setOnClickListener(v -> {
            dialog.dismiss();
        });
        onlyOfficeAppCard.setOnClickListener(_view -> activity.selectOpenOffice(SettingsManager.OPEN_OFFICE_ONLY_OFFICE, dialog, false, fileInfo, isOpenWith));
        inAppOfficeCard.setOnClickListener(_view -> activity.selectOpenOffice(SettingsManager.OPEN_OFFICE_IN_APP_OFFICE, dialog, false, fileInfo, isOpenWith));
        externalAppCard.setOnClickListener(_view -> activity.selectOpenOffice(SettingsManager.OPEN_OFFICE_EXTERNAL_APP, dialog, false, fileInfo, isOpenWith));

        openOfficeCard.setOnClickListener(v -> {
            try {
                LayoutInflater mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = mInflater.inflate(R.layout.popup_open_office, null);

                layout.measure(View.MeasureSpec.UNSPECIFIED,
                        View.MeasureSpec.UNSPECIFIED);
                activity.mPopupOpenOfficeView = activity.findViewById(R.id.popup_open_office_layout);
                activity.mOpenOfficeDropDown = new PopupWindow(layout, activity.mPopupOpenOfficeView.getMeasuredWidth(),
                        activity.mPopupOpenOfficeView.getMeasuredHeight(),true);//FrameLayout.LayoutParams.WRAP_CONTENT

                final CardView useOptionCard = layout.findViewById(R.id.optional_card);
                final CardView useOnlyOfficeAppCard = layout.findViewById(R.id.onlyoffice_app_card);
                final CardView useInAppOfficeCard = layout.findViewById(R.id.in_app_office_card);
                final CardView useOpenOfficeCard = layout.findViewById(R.id.external_app_card);

                useOptionCard.setOnClickListener(_view -> activity.selectOpenOffice(SettingsManager.OPEN_OFFICE_OPTIONAL, dialog, true, fileInfo, isOpenWith));
                useOnlyOfficeAppCard.setOnClickListener(_view -> activity.selectOpenOffice(SettingsManager.OPEN_OFFICE_ONLY_OFFICE, dialog, true, fileInfo, isOpenWith));
                useInAppOfficeCard.setOnClickListener(_view -> activity.selectOpenOffice(SettingsManager.OPEN_OFFICE_IN_APP_OFFICE, dialog, true, fileInfo, isOpenWith));
                useOpenOfficeCard.setOnClickListener(_view -> activity.selectOpenOffice(SettingsManager.OPEN_OFFICE_EXTERNAL_APP, dialog, true, fileInfo, isOpenWith));

                activity.mOpenOfficeDropDown.showAsDropDown(openOfficeCard, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        onDialogCreated(dialog);

        return dialog;
    }

    protected void onDialogCreated(Dialog dialog) {
    }

    @Override
    public void onCancel(DialogInterface dialog) {
    }
}
