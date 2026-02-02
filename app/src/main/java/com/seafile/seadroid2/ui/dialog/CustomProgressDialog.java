package com.seafile.seadroid2.ui.dialog;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.seafile.seadroid2.R;

public class CustomProgressDialog {
    private AlertDialog progressDialog;
    private CardView closeCard;
    private ProgressBar progressBar;
    private TextView progressText;
    private boolean cancelable;
    private long startTime;

    public CustomProgressDialog(Activity activity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.FullScreenDialog);
        LayoutInflater inflater = activity.getLayoutInflater();

        RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.dialog_progress, null);
        progressBar = view.findViewById(R.id.progress_bar);
        progressText = view.findViewById(R.id.title_text);
        closeCard = view.findViewById(R.id.close_card);

        closeCard.setOnClickListener(v -> {
            if (cancelable) {
                dismiss();
            }
        });

        builder.setView(view);

        progressDialog = builder.create();

        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        cancelable = true;
        startTime = System.currentTimeMillis();
    }

    public void setHeight(int value) {
        if (progressBar != null) {
            progressBar.getLayoutParams().width = value;
            progressBar.getLayoutParams().height = value;
        }
    }

    public void setMessage(String message) {
        if (progressText != null) {
            progressText.setVisibility(View.VISIBLE);
            progressText.setText(message);
        }
    }

    public void show() {
        if (progressDialog != null) {
            startTime = System.currentTimeMillis();
            progressDialog.setMessage("");
            progressDialog.show();
        }
    }

    public boolean isShowing() {
        if (progressDialog != null) {
            return progressDialog.isShowing();
        }
        return false;
    }

    public void dismissWithDelay() {
        if (progressDialog != null) {
            long diff = System.currentTimeMillis() - startTime;
            if (diff >= 3000)
                progressDialog.dismiss();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            }, 3000 - diff);
        }
    }

    public void dismiss() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    public void setCancelable(boolean flag) {
        if (progressDialog != null) {
            progressDialog.setCancelable(flag);
            cancelable = flag;
        }
    }
}
