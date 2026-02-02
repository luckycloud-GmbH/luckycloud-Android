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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.data.SelectedFileInfo;
import com.seafile.seadroid2.ui.activity.BrowserActivity;

import java.io.File;


public class OpenPdfDialog extends DialogFragment {
    public static final String DEBUG_TAG = "OpenPdfDialog";

    private BrowserActivity activity;
    private File file;
    private boolean isOpenWith;

    public OpenPdfDialog() {
    }

    @SuppressLint("ValidFragment")
    public OpenPdfDialog(BrowserActivity activity, File file, boolean isOpenWith) {
        this.activity = activity;
        this.file = file;
        this.isOpenWith = isOpenWith;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.dialog_open_pdf, null);

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        CardView closeCard = (CardView) view.findViewById(R.id.close_card);
        CardView inAppPdfCard = (CardView) view.findViewById(R.id.in_app_pdf_card);
        CardView externalAppCard = (CardView) view.findViewById(R.id.external_app_card);
        CardView openPdfCard = (CardView) view.findViewById(R.id.open_pdf_card);

        closeCard.setOnClickListener(v -> {
            dialog.dismiss();
        });
        inAppPdfCard.setOnClickListener(_view -> activity.selectOpenPdf(SettingsManager.OPEN_PDF_IN_APP_PDF, dialog, false, file, isOpenWith));
        externalAppCard.setOnClickListener(_view -> activity.selectOpenPdf(SettingsManager.OPEN_PDF_EXTERNAL_APP, dialog, false, file, isOpenWith));

        openPdfCard.setOnClickListener(v -> {
            try {
                LayoutInflater mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = mInflater.inflate(R.layout.popup_open_pdf, null);

                layout.measure(View.MeasureSpec.UNSPECIFIED,
                        View.MeasureSpec.UNSPECIFIED);
                activity.mPopupOpenPdfView = activity.findViewById(R.id.popup_open_pdf_layout);
                activity.mOpenPdfDropDown = new PopupWindow(layout, activity.mPopupOpenPdfView.getMeasuredWidth(),
                        activity.mPopupOpenPdfView.getMeasuredHeight(),true);//FrameLayout.LayoutParams.WRAP_CONTENT

                final CardView useOptionCard = layout.findViewById(R.id.optional_card);
                final CardView useInAppPdfCard = layout.findViewById(R.id.in_app_pdf_card);
                final CardView useOpenPdfCard = layout.findViewById(R.id.external_app_card);

                useOptionCard.setOnClickListener(_view -> activity.selectOpenPdf(SettingsManager.OPEN_PDF_OPTIONAL, dialog, true, file, isOpenWith));
                useInAppPdfCard.setOnClickListener(_view -> activity.selectOpenPdf(SettingsManager.OPEN_PDF_IN_APP_PDF, dialog, true, file, isOpenWith));
                useOpenPdfCard.setOnClickListener(_view -> activity.selectOpenPdf(SettingsManager.OPEN_PDF_EXTERNAL_APP, dialog, true, file, isOpenWith));

                activity.mOpenPdfDropDown.showAsDropDown(openPdfCard, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);
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
