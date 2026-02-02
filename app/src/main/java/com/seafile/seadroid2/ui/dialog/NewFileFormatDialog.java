package com.seafile.seadroid2.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.ui.activity.BrowserActivity;


public class NewFileFormatDialog extends DialogFragment {
    public interface Listener {
        void onAccepted(boolean rememberChoice);
        void onRejected();
    }

    public static final String FRAGMENT_TAG = "NewFileTypeDialog";
    public static final String DEBUG_TAG = "NewFileTypeDialog";

    private BrowserActivity activity;

    private CardView closeCard;
    private CardView fileCard;
    private CardView txtCard;
    private CardView mdCard;
    private CardView wordCard;
    private CardView excelCard;
    private CardView pptCard;

    public NewFileFormatDialog() {
    }

    @SuppressLint("ValidFragment")
    public NewFileFormatDialog(BrowserActivity activity) {
        this.activity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout view = (LinearLayout)inflater.inflate(R.layout.dialog_new_format_type, null);

        closeCard = (CardView) view.findViewById(R.id.close_card);
        fileCard = (CardView) view.findViewById(R.id.file_card);
        txtCard = (CardView) view.findViewById(R.id.txt_card);
        mdCard = (CardView) view.findViewById(R.id.md_card);
        wordCard = (CardView) view.findViewById(R.id.word_card);
        excelCard = (CardView) view.findViewById(R.id.excel_card);
        pptCard = (CardView) view.findViewById(R.id.ppt_card);

        closeCard.setOnClickListener(v -> {
            dismiss();
        });
        fileCard.setOnClickListener(v -> {
            activity.showNewFileDialog("");
            dismiss();
        });
        txtCard.setOnClickListener(v -> {
            activity.showNewFileDialog(".txt");
            dismiss();
        });
        mdCard.setOnClickListener(v -> {
            activity.showNewFileDialog(".md");
            dismiss();
        });
        wordCard.setOnClickListener(v -> {
            activity.showNewFileDialog(".docx");
            dismiss();
        });
        excelCard.setOnClickListener(v -> {
            activity.showNewFileDialog(".xlsx");
            dismiss();
        });
        pptCard.setOnClickListener(v -> {
            activity.showNewFileDialog(".pptx");
            dismiss();
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
    }
}
