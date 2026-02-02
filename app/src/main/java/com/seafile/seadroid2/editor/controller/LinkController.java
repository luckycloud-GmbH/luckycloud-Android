package com.seafile.seadroid2.editor.controller;

import android.app.Dialog;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.util.Utils;
import com.yydcdut.markdown.MarkdownEditText;


public class LinkController {
    private MarkdownEditText mRxMDEditText;

    private Dialog mLinkDialog;

    public LinkController(MarkdownEditText rxMDEditText) {
        mRxMDEditText = rxMDEditText;
    }


    public void doImage() {
        if (mLinkDialog == null) {
            initDialog();
        }
        mLinkDialog.show();
    }

    private void initDialog() {
        mLinkDialog = Utils.CustomDialog(mRxMDEditText.getContext());
        mLinkDialog.setContentView(R.layout.dialog_link);

        CardView mOkCard = mLinkDialog.findViewById(R.id.ok_card);
        TextView mOkText = mLinkDialog.findViewById(R.id.ok_text);
        CardView mCancelCard = mLinkDialog.findViewById(R.id.cancel_card);
        TextView mDesc = mLinkDialog.findViewById(R.id.edit_description_link);
        TextView mLink = mLinkDialog.findViewById(R.id.edit_link);

        mOkText.setText(R.string.confirm);
        mOkCard.setOnClickListener(v -> {
            mLinkDialog.dismiss();
            String description = mDesc.getText().toString();
            String link = mLink.getText().toString();
            doRealLink(description, link);
        });
        mCancelCard.setOnClickListener(v -> {
            mLinkDialog.dismiss();
        });

        mLinkDialog.setCancelable(false);
    }

    private void doRealLink(String description, String link) {
        int start = mRxMDEditText.getSelectionStart();
        if (TextUtils.isEmpty(description)) {
            mRxMDEditText.getText().insert(start, "[](" + link + ")");
            mRxMDEditText.setSelection(start + 2);
        } else {
            mRxMDEditText.getText().insert(start, "[" + description + "](" + link + ")");
        }
    }
}
