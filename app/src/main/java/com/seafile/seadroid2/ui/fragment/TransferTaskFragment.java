package com.seafile.seadroid2.ui.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.ListFragment;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.transfer.TransferService;
import com.seafile.seadroid2.transfer.TransferTaskInfo;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.adapter.TransferTaskAdapter;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;

import java.util.List;

/**
 * Base class for transfer task fragments
 *
 */
public abstract class TransferTaskFragment extends ListFragment {
    private String DEBUG_TAG = "TransferTaskFragment";

    protected TransferTaskAdapter adapter;
    protected BrowserActivity mActivity = null;
    protected ListView mTransferTaskListView;
    protected TextView emptyView;
    private View mListContainer;
    private View mProgressContainer;
    protected Handler mTimer;
    protected TransferService txService = null;
    private boolean mActionMode = false;

    private View mActionModeLayout;
    private TextView mActionModeTitleText;
    private CardView mActionModeDeleteCard;
    private CardView mActionModeCopyCard;
    private CardView mActionModeMoveCard;
    private CardView mActionModeDownloadCard;
    private CardView mActionModeSelectAllCard;
    private CardView mActionModeCloseCard;
    private View mActionModeCopyLayout;
    private View mActionModeMoveLayout;
    private View mActionModeDownloadLayout;

    private boolean allItemsSelected;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (BrowserActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.transfer_task_fragment, container, false);
        mTransferTaskListView = (ListView) root.findViewById(android.R.id.list);
        mTransferTaskListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                listItemLongClick(position);

                return true;
            }
        });

        mListContainer =  root.findViewById(R.id.listContainer);
        mProgressContainer = root.findViewById(R.id.progressContainer);
        emptyView = (TextView) root.findViewById(R.id.empty);

        mActionModeLayout = root.findViewById(R.id.action_mode_layout);
        mActionModeTitleText = mActionModeLayout.findViewById(R.id.action_mode_title_text);
        mActionModeDeleteCard = mActionModeLayout.findViewById(R.id.action_mode_delete_card);
        mActionModeCopyCard = mActionModeLayout.findViewById(R.id.action_mode_copy_card);
        mActionModeMoveCard = mActionModeLayout.findViewById(R.id.action_mode_move_card);
        mActionModeDownloadCard = mActionModeLayout.findViewById(R.id.action_mode_download_card);
        mActionModeSelectAllCard = mActionModeLayout.findViewById(R.id.action_mode_select_all_card);
        mActionModeCloseCard = mActionModeLayout.findViewById(R.id.action_mode_close_card);
        mActionModeCopyLayout = mActionModeLayout.findViewById(R.id.action_mode_copy_layout);
        mActionModeMoveLayout = mActionModeLayout.findViewById(R.id.action_mode_move_layout);
        mActionModeDownloadLayout = mActionModeLayout.findViewById(R.id.action_mode_download_layout);

        return root;
    }

    private List<TransferTaskInfo> convertToTasks(List<Integer> positions) {
        List<TransferTaskInfo> tasks = Lists.newArrayList();
        for (int position : positions) {
            TransferTaskInfo tti = adapter.getItem(position);
            tasks.add(tti);
        }

        return tasks;
    }

    /**
     * deselect all items
     */
    public void deselectItems() {
        if (adapter == null) return;

        adapter.deselectAllItems();
        updateContextualActionBar();
    }

    protected abstract void deleteSelectedItems(List<TransferTaskInfo> tasks);

    protected abstract void restartSelectedItems(List<TransferTaskInfo> tasks);

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showLoading(true);

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Toast.makeText(mActivity, "Stop loading animations", Toast.LENGTH_LONG).show();
            showLoading(false);

            TransferService.TransferBinder binder = (TransferService.TransferBinder) service;
            txService = binder.getService();
//            if (isNeedUpdateProgress()) {
                setUpTransferList();
                // startTimer();
//            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            txService = null;
        }
    };

    protected abstract List<? extends TransferTaskInfo> getTransferTaskInfos();

    protected abstract void setUpTransferList();

    @Override
    public void onResume() {
        super.onResume();
        showEmptyView(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        // bind transfer service
        Intent bIntent = new Intent(mActivity, TransferService.class);
        mActivity.bindService(bIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    protected abstract boolean isNeedUpdateProgress();

    @Override
    public void onStop() {
        super.onStop();
        stopTimer();
        if (txService != null) {
            mActivity.unbindService(mConnection);
            txService = null;
        }
    }

    // refresh list by mTimer
    public void startTimer() {
        Log.d(DEBUG_TAG, "timer started");
        if (mTimer == null) {
            mTimer = new Handler();
            ConcurrentAsyncTask.execute(new SetTransferTaskInfos());
        }
    }

    public void stopTimer() {
        if (mTimer != null) {
            mTimer.removeCallbacksAndMessages(null);
            mTimer = null;
        }
    }

    private class SetTransferTaskInfos extends AsyncTask<Void, Void, List<? extends TransferTaskInfo>> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<? extends TransferTaskInfo> doInBackground(Void... params) {
            List<? extends TransferTaskInfo> infos = getTransferTaskInfos();

            if (!adapter.equalLists(infos, adapter.getTransferTaskInfos())) {
                return infos;
            }

            //Log.d(DEBUG_TAG, "timer post refresh signal " + System.currentTimeMillis());
            return null;
        }

        @Override
        protected void onPostExecute(List<? extends TransferTaskInfo> infos) {
            if (infos != null) {
                adapter.setTransferTaskInfos(infos);
                adapter.notifyChanged();
            }

            if (mTimer != null) {
                mTimer.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        ConcurrentAsyncTask.execute(new SetTransferTaskInfos());
                    }
                }, 1 * 1000);
            }
        }
    }

    private void showLoading(boolean show) {
        if (mActivity == null)
            return;

        if (show) {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                    mActivity, android.R.anim.fade_in));
            mListContainer.startAnimation(AnimationUtils.loadAnimation(
                    mActivity, android.R.anim.fade_out));

            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.INVISIBLE);
        } else {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                    mActivity, android.R.anim.fade_out));
            mListContainer.startAnimation(AnimationUtils.loadAnimation(
                    mActivity, android.R.anim.fade_in));

            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
//        listItemClick(position);
    }

    public void listItemClick(int position) {
        if (mActionMode) {
            // add or remove selection for current list item
            if (adapter == null) return;

            adapter.toggleSelection(position);
            updateContextualActionBar();
        }
    }

    public void listItemLongClick(int position) {
        startContextualActionMode(position);
    }

    public void startContextualActionMode(int position) {
        startContextualActionMode();

        if (adapter == null) return;

        adapter.toggleSelection(position);
        updateContextualActionBar();

    }

    public void startContextualActionMode() {
        if (!mActionMode) {
            // start the actionMode
            startSupportActionMode();
        }

    }

    /**
     *  update state of contextual action bar
     */
    public void updateContextualActionBar() {
        if (!mActionMode) {
            // there are some selected items, start the actionMode
            startSupportActionMode();
        } else {
            // Log.d(DEBUG_TAG, "mActionMode.setTitle " + adapter.getCheckedItemCount());
            mActionModeTitleText.setText(getResources().getQuantityString(
                    R.plurals.transfer_list_items_selected,
                    adapter.getCheckedItemCount(),
                    adapter.getCheckedItemCount()));
        }
    }

    private void startSupportActionMode() {

        initActionModeCards();

        mActionMode = true;
        mActionModeLayout.setVisibility(View.VISIBLE);
        if (adapter == null) return;
        adapter.actionModeOn();
    }

    private void initActionModeCards() {
        mActionModeCopyLayout.setVisibility(View.GONE);
        mActionModeMoveLayout.setVisibility(View.GONE);
        mActionModeDownloadLayout.setVisibility(View.GONE);

        mActionModeDownloadCard.setOnClickListener(v -> {
            List<Integer> restartIds = adapter.getSelectedIds();
            if (restartIds != null) {
                if (restartIds.size() == 0) {
                    mActivity.showShortToast(mActivity, R.string.action_mode_no_items_selected);
                    return;
                }

                restartSelectedItems(convertToTasks(restartIds));
                deselectItems();
            }
        });
        mActionModeDeleteCard.setOnClickListener(v -> {
            List<Integer> ids = adapter.getSelectedIds();
            if (ids != null) {
                if (ids.size() == 0) {
                    mActivity.showShortToast(mActivity, R.string.action_mode_no_items_selected);
                    return;
                }

                deleteSelectedItems(convertToTasks(ids));
                deselectItems();
            }
        });
        mActionModeSelectAllCard.setOnClickListener(v -> {
            if (!allItemsSelected) {
                if (adapter == null) return;

                adapter.selectAllItems();
                updateContextualActionBar();
            } else {
                if (adapter == null) return;

                adapter.deselectAllItems();
                updateContextualActionBar();
            }

            allItemsSelected = !allItemsSelected;
        });
        mActionModeCloseCard.setOnClickListener(v -> {
            stopSupportActionMode();
        });
    }

    public void stopSupportActionMode() {
        if (adapter == null) return;

        adapter.actionModeOff();
        adapter.deselectAllItems();

        // Here you can make any necessary updates to the activity when
        // the contextual action bar (CAB) is removed. By default, selected items are deselected/unchecked.
        mActionMode = false;
        mActionModeLayout.setVisibility(View.GONE);
    }

    public void setTransferType(int type) {
        adapter.setTransferType(type);
        adapter.notifyChanged();
        // persist sort settings
        SettingsManager.instance().saveTransferPref(type);
    }
    
    public void showEmptyView(boolean flag) {
        if (mTransferTaskListView != null)
            mTransferTaskListView.setVisibility(flag ? View.GONE : View.VISIBLE);

        if (emptyView != null)
            emptyView.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    /**
     * Represents a contextual mode of the user interface.
     * Action modes can be used to provide alternative interaction modes and replace parts of the normal UI until finished.
     * A Callback configures and handles events raised by a user's interaction with an action mode.
     */
}
