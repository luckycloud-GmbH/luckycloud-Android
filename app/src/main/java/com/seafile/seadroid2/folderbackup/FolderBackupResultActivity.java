package com.seafile.seadroid2.folderbackup;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafBackup;
import com.seafile.seadroid2.transfer.TransferManager;
import com.seafile.seadroid2.ui.activity.BaseActivity;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.activity.UnlockGesturePasswordActivity;
import com.seafile.seadroid2.ui.adapter.SeafBackupAdapter;
import com.seafile.seadroid2.ui.adapter.SeafBackupExpandableAdapter;
import com.seafile.seadroid2.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class FolderBackupResultActivity extends BaseActivity {
    private static final String DEBUG_TAG = "FolderBackupResultActivity";

    public static final int DETAIL_ACTIVITY_REQUEST = 1;

    private Account mAccount;
    private CardView closeCard;
    private CardView dateCard;
    private TextView dateText;
    private CardView pathCard;
    private TextView pathText;
    private CardView fileCard;
    private TextView fileText;
    private ExpandableListView expandableListView;
    private ListView listView;
    private SeafBackupExpandableAdapter expandableAdapter;
    private SeafBackupAdapter adapter;
    List<SeafBackup> seafBackups;
    List<String> expandableTitleList;
    HashMap<String, List<SeafBackup>> expandableBackups;

    public enum ListType {DATE, PATH, FILE}
    private ListType listType;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        setContentView(R.layout.folder_backup_result_activity_layout);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        Intent intent = getIntent();
        mAccount = intent.getParcelableExtra("account");

        closeCard = findViewById(R.id.close_card);
        dateCard = findViewById(R.id.date_card);
        dateText = findViewById(R.id.date_text);
        pathCard = findViewById(R.id.path_card);
        pathText = findViewById(R.id.path_text);
        fileCard = findViewById(R.id.file_card);
        fileText = findViewById(R.id.file_text);
        expandableListView = findViewById(R.id.expandable_list_view);
        listView = findViewById(R.id.list_view);

        dateCard.setOnClickListener(v -> {
            updateListType(ListType.DATE);
        });

        pathCard.setOnClickListener(v -> {
            updateListType(ListType.PATH);
        });

        fileCard.setOnClickListener(v -> {
            updateListType(ListType.FILE);
        });

        closeCard.setOnClickListener(v -> {
            finish();
        });

        expandableAdapter = new SeafBackupExpandableAdapter(this);
        expandableListView.setAdapter(expandableAdapter);

        adapter = new SeafBackupAdapter(this);
        listView.setAdapter(adapter);

        updateListType(ListType.DATE);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void updateListType(ListType type) {
        listType = type;

        dateText.setBackground(getResources().getDrawable(
                listType == ListType.DATE? R.drawable.rounded_transfer_button_on : R.drawable.rounded_transfer_button_off));
        dateText.setTextColor(getResources().getColor(
                listType == ListType.DATE? R.color.white : R.color.luckycloud_green));

        pathText.setBackground(getResources().getDrawable(
                listType == ListType.PATH? R.drawable.rounded_transfer_button_on : R.drawable.rounded_transfer_button_off));
        pathText.setTextColor(getResources().getColor(
                listType == ListType.PATH? R.color.white : R.color.luckycloud_green));

        fileText.setBackground(getResources().getDrawable(
                listType == ListType.FILE? R.drawable.rounded_transfer_button_on : R.drawable.rounded_transfer_button_off));
        fileText.setTextColor(getResources().getColor(
                listType == ListType.FILE? R.color.white : R.color.luckycloud_green));

        refreshView();
    }

    private void getExpandableData() {
        seafBackups = new DataManager(mAccount).getBackupsFromCache();
        if (seafBackups == null) {
            seafBackups = Lists.newArrayList();
        }
        Collections.sort(seafBackups, (a, b) -> Long.compare(b.endTime, a.endTime));

        HashMap<String, List<SeafBackup>> expandableDetailList = new HashMap<String, List<SeafBackup>>();
        for (SeafBackup backup:seafBackups) {
            List<SeafBackup> backups = expandableDetailList.get(getKey(backup));
            if (backups == null) {
                backups = Lists.newArrayList();
            }
            backups.add(backup);

            Collections.sort(backups, (a, b) -> Long.compare(b.endTime, a.endTime));

            expandableDetailList.put(getKey(backup), backups);
        }

        expandableBackups = expandableDetailList;
        expandableTitleList = new ArrayList<String>(expandableDetailList.keySet());

        Collections.sort(expandableTitleList, (a, b) -> {

            try {
                if (listType != ListType.PATH) {
                    SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    return df.parse(b).compareTo(df.parse(a));
                }
            } catch (Exception e) {}
            return b.compareTo(a);
        });
    }

    private String getKey(SeafBackup backup) {
        if (listType == ListType.PATH) {
            return Utils.getParentPath(backup.sourcePath);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(backup.endTime);
        return new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());
    }

    private void refreshView() {
        getExpandableData();
        expandableAdapter.setData(expandableTitleList, expandableBackups);
        adapter.setData(seafBackups);

        expandableListView.setVisibility(listType == ListType.FILE ? View.GONE : View.VISIBLE);
        listView.setVisibility(listType == ListType.FILE ? View.VISIBLE : View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FolderBackupEvent result) {
        refreshView();
    }
}
