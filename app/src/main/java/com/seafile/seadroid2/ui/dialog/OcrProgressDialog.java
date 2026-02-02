package com.seafile.seadroid2.ui.dialog;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.ui.adapter.OcrProgressItemAdapter;

public class OcrProgressDialog {
    private AlertDialog progressDialog;
    private CardView closeCard;
    private ListView ocrProgressListView;
    private OcrProgressItemAdapter adapter;

    public OcrProgressDialog(Activity activity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.FullScreenDialog);
        LayoutInflater inflater = activity.getLayoutInflater();

        RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.dialog_ocr_progress, null);
        ocrProgressListView = view.findViewById(R.id.ocr_progress_listview);
        closeCard = view.findViewById(R.id.close_card);

        closeCard.setOnClickListener(v -> {
            //dismiss();
        });

        adapter = new OcrProgressItemAdapter(activity);
        ocrProgressListView.setAdapter(adapter);
        ocrProgressListView.setDivider(null);

        builder.setView(view);

        progressDialog = builder.create();

        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public void init(int count) {
        adapter.init(count);
    }

    public void setProgress(int index, int progress) {
        adapter.setItem(index, progress);
    }

    public void show() {
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

    public void dismiss() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }
}
