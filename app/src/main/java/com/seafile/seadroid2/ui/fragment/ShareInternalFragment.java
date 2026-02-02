package com.seafile.seadroid2.ui.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeafConnection;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafLink;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;
import com.seafile.seadroid2.ui.adapter.SeafTimeAdapter;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class ShareInternalFragment extends Fragment {

    private EditText linkText;
    private CardView copyCard;
    private CardView qrCard;

    private ShareDialogActivity mShareDialogActivity;
    private Account mAccount;
    private String dialogType;
    private SeafConnection conn;
    private SeafLink seafLink;
    private Account account;
    private String repoID;
    private String path;
    private String fileName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.share_internal_fragment, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initViewAction();
        init();
    }

    private void initView(View root) {
        linkText = root.findViewById(R.id.link_text);
        copyCard = root.findViewById(R.id.copy_link_card);
        qrCard = root.findViewById(R.id.qr_card);
    }

    private void initViewAction() {
        copyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = linkText.getText().toString();
                if (!link.isEmpty()) {
                    mShareDialogActivity.showCopyDialog(link);
                }
            }
        });

        qrCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = linkText.getText().toString();
                if (!link.isEmpty()) {
                    mShareDialogActivity.showQRDialog(link);
                }
            }
        });
    }

    private void init() {
        mShareDialogActivity = (ShareDialogActivity)getActivity();

        account = mShareDialogActivity.account;
        dialogType = mShareDialogActivity.dialogType;
        repoID = mShareDialogActivity.repo.getID();
        path = mShareDialogActivity.path;
        fileName = mShareDialogActivity.fileName;
        conn = new SeafConnection(account);

        GetInternalLinkTask task = new GetInternalLinkTask();
        ConcurrentAsyncTask.execute(task);
    }

    public class GetInternalLinkTask extends AsyncTask<Void, Long, Void> {
        String resultString = "";
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                DataManager dataManager = new DataManager(account);
                //get internal link
                resultString = dataManager.getInternalLink(repoID, path, dialogType);
            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mShareDialogActivity.changeProgress(false);
            if (err == null) {
                if (!resultString.equals("")) {
                    linkText.setText(resultString);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                    mShareDialogActivity.onBackPressed();
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }
}
