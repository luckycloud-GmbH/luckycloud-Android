package com.seafile.seadroid2.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.seafile.seadroid2.R;

public class SaveDialog extends DialogFragment {

    private final Context mContext;
    private final OnCloseListener listener;

    public static final String FRAGMENT_TAG = "SaveDialog";

    public SaveDialog(@NonNull Context context, OnCloseListener listener) {
        this.mContext = context;
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout view = (LinearLayout)inflater.inflate(R.layout.dialog_save, null);

        CardView mCancelCard = view.findViewById(R.id.cancel_card);
        TextView mCancelText = view.findViewById(R.id.cancel_text);
        CardView mConfirmCard = view.findViewById(R.id.ok_card);
        TextView mConfirmText = view.findViewById(R.id.ok_text);


        mConfirmText.setText(R.string.yes);
        mCancelText.setText(R.string.no);
        mConfirmCard.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(true);
            }
            this.dismiss();
        });
        mCancelCard.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(false);
            }
            this.dismiss();
        });

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        onDialogCreated(dialog);

        return dialog;
    }

    protected void onDialogCreated(Dialog dialog) {
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        listener.onClick(false);
    }

    public interface OnCloseListener {
        void onClick(boolean confirm);
    }
}
