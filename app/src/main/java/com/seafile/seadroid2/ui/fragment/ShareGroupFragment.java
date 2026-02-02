package com.seafile.seadroid2.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeafConnection;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.account.Authenticator;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafPermission;
import com.seafile.seadroid2.data.SeafShareUser;
import com.seafile.seadroid2.data.SeafShareableGroup;
import com.seafile.seadroid2.data.SeafSharedItem;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;
import com.seafile.seadroid2.ui.adapter.SeafPermission2Adapter;
import com.seafile.seadroid2.ui.adapter.SeafShareUserAdapter;
import com.seafile.seadroid2.ui.adapter.SeafShareableGroupAdapter;
import com.seafile.seadroid2.ui.adapter.SeafSharedItemAdapter;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import java.util.ArrayList;
import java.util.Collections;


public class ShareGroupFragment extends Fragment {
    
    public static String SHARE_TYPE_GROUP = "group";
    public static String SHARE_TYPE_USER = "user";

    private View container;
    private View userAdapterLayout;
    private int userAdapterHeight;
    private View selectGroupsLayout;
    private EditText selectGroupText;
    private CardView selectGroupCard;
    private View selectPermissionLayout;
    private EditText selectPermissionText;
    private CardView selectPermissionCard;
    private CardView submitCard;
    private TextView shareTypeText;
    private TextView permissionCationText;
    private ListView sharedListView;
    private View popupSelectGroupView;

    private PopupWindow mSelectGroupsDropdown = null;
    private PopupWindow mSelectUsersDropdown = null;
    private PopupWindow mSelectPermissionDropdown = null;
    private PopupWindow mDropdown = null;
    private SeafSharedItemAdapter adapter;

    private ShareDialogActivity mShareDialogActivity;
    private FragmentActivity mActivity;
    private android.accounts.AccountManager mAccountManager;
    private Account mAccount;
    private String dialogType;
    private Account account;
    private String repoID;
    private String path;
    private String fileName;
    private SeafConnection conn;
    private String shareType;
    private Boolean isShareGroup;

    private ArrayList<SeafShareableGroup> seafShareGroups;
    private ArrayList<SeafShareableGroup> selectedShareGroups;
    private ArrayList<SeafShareUser> selectedShareUsers;
    private ArrayList<SeafPermission> seafPermissions;
    private SeafPermission selectedPermission;
    private ArrayList<SeafSharedItem> seafSharedItems;
    private int selectedIndex;
    private int screenHeight;

    public ShareGroupFragment(String type) {
        shareType = type;
        isShareGroup = shareType.equals(SHARE_TYPE_GROUP);
        seafShareGroups = Lists.newArrayList();
        selectedShareGroups = Lists.newArrayList();
        selectedShareUsers = Lists.newArrayList();
        seafPermissions = Lists.newArrayList();
        seafSharedItems = Lists.newArrayList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.share_group_fragment, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initViewAction();
        init();
    }

    private void initView(View view) {
        container = view.findViewById(R.id.container);
        userAdapterLayout = view.findViewById(R.id.user_adapter_layout);
        selectGroupsLayout = view.findViewById(R.id.select_group_layout);
        selectGroupText = view.findViewById(R.id.select_group_text);
        selectGroupCard = view.findViewById(R.id.select_group_card);
        selectPermissionLayout = view.findViewById(R.id.select_permission_layout);
        selectPermissionText = view.findViewById(R.id.select_permission_text);
        selectPermissionCard = view.findViewById(R.id.select_permission_card);
        submitCard = view.findViewById(R.id.submit_card);
        shareTypeText = view.findViewById(R.id.share_type_text);
        permissionCationText = view.findViewById(R.id.permission_cation_text);
        sharedListView = view.findViewById(android.R.id.list);
        popupSelectGroupView = view.findViewById(R.id.popup_select_group_layout);
    }

    private void initViewAction() {
        selectGroupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectGroupCard.callOnClick();
            }
        });
        selectGroupCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShareGroup) {
//                    showSelectGroupsDialog();
                    showSelectGroupsPopup();
                } else {
//                    showSelectUsersDialog();
                    showSelectUsersPopup();
                }
            }
        });
        selectPermissionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPermissionCard.callOnClick();
            }
        });
        selectPermissionCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedIndex = -1;
//                showSelectPermissionDialog();
                showSelectPermissionPopup(null);
            }
        });
        submitCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShareGroup) {
                    if (selectedShareGroups.size() == 0) {
                        Toast.makeText(
                                mActivity,
                                mActivity.getResources().getString(R.string.select_group),
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }
                } else {
                    if (selectedShareUsers.size() == 0) {
                        Toast.makeText(
                                mActivity,
                                mActivity.getResources().getString(R.string.select_user),
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }
                }
                ConcurrentAsyncTask.execute(new ShareFolder());
            }
        });
        sharedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedIndex = i;
                showEditItemPopup(view);
            }
        });
    }

    private void init() {
        userAdapterLayout.post(new Runnable() {
            @Override
            public void run() {
                userAdapterHeight = userAdapterLayout.getMeasuredHeight();
                userAdapterLayout.setVisibility(View.GONE);
            }
        });

        mActivity = getActivity();
        mShareDialogActivity = (ShareDialogActivity)mActivity;

        mAccountManager = android.accounts.AccountManager.get(mShareDialogActivity.getBaseContext());
        account = mShareDialogActivity.account;
        dialogType = mShareDialogActivity.dialogType;
        repoID = mShareDialogActivity.repo.getID();
        path = mShareDialogActivity.path;
        fileName = mShareDialogActivity.fileName;
        conn = new SeafConnection(account);

        adapter = new SeafSharedItemAdapter(mShareDialogActivity);
        sharedListView.setAdapter(adapter);
        sharedListView.setDivider(null);
        
        selectGroupText.setHint(mActivity.getResources().getString(isShareGroup? R.string.select_group : R.string.select_user));
        shareTypeText.setText(mActivity.getResources().getString(isShareGroup? R.string.group : R.string.user));

        if (isShareGroup) {
            ConcurrentAsyncTask.execute(new ShareableGroups());
        } else {
            ConcurrentAsyncTask.execute(new ListAllPermissions());
        }
    }

    private void initInputs() {
        selectedShareGroups.clear();
        selectedShareUsers.clear();
        setGroupText();
    }

    private void showSelectGroupsPopup() {

        LayoutInflater mInflater;

        try {
            mInflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_groups, null);
            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mSelectGroupsDropdown = new PopupWindow(layout, selectGroupsLayout.getWidth(), (int)mActivity.getResources().getDimension(R.dimen.share_select_group_height), true);//FrameLayout.LayoutParams.WRAP_CONTENT
            mSelectGroupsDropdown.showAsDropDown(selectGroupsLayout, 5, 5, Gravity.BOTTOM);

            EditText mSearchText = layout.findViewById(R.id.et_content);
            ListView groupListView = layout.findViewById(android.R.id.list);
            CardView cancelCard = layout.findViewById(R.id.cancel_card);
            CardView okCard = layout.findViewById(R.id.ok_card);

            screenHeight = getScreenHeight();

            SeafShareableGroupAdapter groupAdapter = new SeafShareableGroupAdapter(mShareDialogActivity);
            groupListView.setAdapter(groupAdapter);

            groupAdapter.setItems(seafShareGroups, selectedShareGroups);

            mSearchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    groupAdapter.setSearchString(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            mSearchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    mSearchText.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager inputMethodManager= (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(mSearchText, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            new Handler().postDelayed(mSearchText::requestFocus, 200);

            cancelCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideKeyboard();
                    mSelectGroupsDropdown.dismiss();
                }
            });

            okCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedShareGroups.clear();
                    selectedShareGroups.addAll(groupAdapter.getSelectedShareableGroups());
                    setGroupText();
                    hideKeyboard();
                    mSelectGroupsDropdown.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void showSelectGroupsDialog() {
//        Dialog dialog = Utils.CustomDialog(mActivity);
//        dialog.setContentView(R.layout.dialog_select_groups);
//
//        EditText mSearchText = dialog.findViewById(R.id.et_content);
//        ListView groupListView = dialog.findViewById(android.R.id.list);
//        CardView cancelCard = dialog.findViewById(R.id.cancel_card);
//        CardView okCard = dialog.findViewById(R.id.ok_card);
//
//        SeafShareableGroupAdapter groupAdapter = new SeafShareableGroupAdapter(mShareDialogActivity);
//        groupListView.setAdapter(groupAdapter);
//        groupListView.setDivider(null);
//
//        groupAdapter.setItems(seafShareGroups, selectedShareGroups);
//
//        mSearchText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                groupAdapter.setSearchString(s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//
//        cancelCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//
//        okCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                selectedShareGroups.clear();
//                selectedShareGroups.addAll(groupAdapter.getSelectedShareableGroups());
//                setGroupText();
//                dialog.dismiss();
//            }
//        });
//
//        dialog.show();
//    }

    private void showSearchUserProgress(View view, boolean flag){
        try {
            if (view != null) {
                view.setVisibility(flag? View.VISIBLE : View.GONE);
            }
        } catch (Exception err) {

        }
    }

    private void showSelectUsersPopup() {

        LayoutInflater mInflater;

        try {
            mInflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_groups, null);
            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            if (userAdapterHeight == 0) userAdapterHeight = (int) mActivity.getResources().getDimension(R.dimen.share_select_user_item_height);
            mSelectUsersDropdown = new PopupWindow(layout, selectGroupsLayout.getWidth(), userAdapterHeight * Math.min(selectedShareUsers.size(), 5)  + (int) mActivity.getResources().getDimension(R.dimen.share_select_user_another_height), true);//FrameLayout.LayoutParams.WRAP_CONTENT//800
            mSelectUsersDropdown.showAsDropDown(selectGroupsLayout, 5, 5, Gravity.BOTTOM);

            View searchUserProgressLayout = layout.findViewById(R.id.progress_layout);
            EditText mSearchText = layout.findViewById(R.id.et_content);
            CardView mSearchBtn = layout.findViewById(R.id.btn_search);
            ImageView mTextClearBtn = layout.findViewById(R.id.btn_clear);
            ListView groupListView = layout.findViewById(android.R.id.list);
            CardView cancelCard = layout.findViewById(R.id.cancel_card);
            CardView okCard = layout.findViewById(R.id.ok_card);

            screenHeight = getScreenHeight();
            String server = mAccountManager.getUserData(account.getAndroidAccount(), Authenticator.KEY_SERVER_URI);
            if (server.contains("storage.luckycloud") || server.contains("sync.luckycloud")) {
                mSearchText.setHint(mShareDialogActivity.getResources().getString(R.string.enter_full_email));
            }

            SeafShareUserAdapter userAdapter = new SeafShareUserAdapter(mShareDialogActivity, mSelectUsersDropdown, userAdapterHeight, selectGroupsLayout);
            groupListView.setAdapter(userAdapter);
            groupListView.setDivider(null);

            userAdapter.setItems(selectedShareUsers);

            class SearchUsersTask extends AsyncTask<Void, Void, ArrayList<SeafShareUser>> {

                private DataManager dataManager;
                private String query;
                private ArrayList<SeafShareUser> mSearchedRlt;
                private SeafException seafException;

                @Override
                protected void onPreExecute() {
                    // show loading view
                    showSearchUserProgress(searchUserProgressLayout, true);
//            showLoading(true);
//            mSearchBtn.setEnabled(false);
//            mMessageContainer.setVisibility(View.GONE);
                }

                public SearchUsersTask(DataManager dataManager, String query) {
                    this.dataManager = dataManager;
                    this.query = query;
                }

                @Override
                protected ArrayList<SeafShareUser> doInBackground(Void... params) {
                    try {
                        mSearchedRlt = dataManager.searchUsers(query);
                        return mSearchedRlt;
                    } catch (SeafException e) {
                        seafException = e;
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(ArrayList<SeafShareUser> result) {
                    userAdapter.addItems(mSearchedRlt);
                    // stop loading view
                    showSearchUserProgress(searchUserProgressLayout, false);
                }
            }

            mSearchText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    if (mSearchText.getText().toString().length() > 0) {
//                        mTextClearBtn.setVisibility(View.VISIBLE);
//                        mSearchBtn.setVisibility(View.VISIBLE);
//                    } else {
//                        mTextClearBtn.setVisibility(View.GONE);
//                        mSearchBtn.setVisibility(View.GONE);
//                    }
                    if (mSearchText.getText().toString().length() > 0) {
                        DataManager dataManager = new DataManager(account);
                        ConcurrentAsyncTask.execute(new SearchUsersTask(dataManager, mSearchText.getText().toString()));
                    } else {
                        userAdapter.clear();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            mSearchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    mSearchText.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager inputMethodManager= (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(mSearchText, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            new Handler().postDelayed(mSearchText::requestFocus, 200);

            mSearchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DataManager dataManager = new DataManager(account);
                    ConcurrentAsyncTask.execute(new SearchUsersTask(dataManager, mSearchText.getText().toString()));
                }
            });

            mTextClearBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSearchText.getText().clear();
                }
            });

            cancelCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideKeyboard();
                    mSelectUsersDropdown.dismiss();
                }
            });

            okCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedShareUsers.clear();
                    selectedShareUsers.addAll(userAdapter.getSelectedShareUsers());
                    setGroupText();
                    hideKeyboard();
                    mSelectUsersDropdown.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void showSelectUsersDialog() {
//        Dialog dialog = Utils.CustomDialog(mActivity);
//        dialog.setContentView(R.layout.dialog_select_groups);
//
//        View searchUserProgressLayout = dialog.findViewById(R.id.progress_layout);
//        EditText mSearchText = dialog.findViewById(R.id.et_content);
//        CardView mSearchBtn = dialog.findViewById(R.id.btn_search);
//        ImageView mTextClearBtn = dialog.findViewById(R.id.btn_clear);
//        ListView groupListView = dialog.findViewById(android.R.id.list);
//        CardView cancelCard = dialog.findViewById(R.id.cancel_card);
//        CardView okCard = dialog.findViewById(R.id.ok_card);
//
//        SeafShareUserAdapter userAdapter = new SeafShareUserAdapter(mShareDialogActivity);
//        groupListView.setAdapter(userAdapter);
//        groupListView.setDivider(null);
//
//        userAdapter.setItems(selectedShareUsers);
//
//        class SearchUsersTask extends AsyncTask<Void, Void, ArrayList<SeafShareUser>> {
//
//            private DataManager dataManager;
//            private String query;
//            private ArrayList<SeafShareUser> mSearchedRlt;
//            private SeafException seafException;
//
//            @Override
//            protected void onPreExecute() {
//                // show loading view
//                showSearchUserProgress(searchUserProgressLayout, true);
////            showLoading(true);
////            mSearchBtn.setEnabled(false);
////            mMessageContainer.setVisibility(View.GONE);
//            }
//
//            public SearchUsersTask(DataManager dataManager, String query) {
//                this.dataManager = dataManager;
//                this.query = query;
//            }
//
//            @Override
//            protected ArrayList<SeafShareUser> doInBackground(Void... params) {
//                try {
//                    mSearchedRlt = dataManager.searchUsers(query);
//                    return mSearchedRlt;
//                } catch (SeafException e) {
//                    seafException = e;
//                    return null;
//                }
//            }
//
//            @Override
//            protected void onPostExecute(ArrayList<SeafShareUser> result) {
//                userAdapter.addItems(mSearchedRlt);
//                // stop loading view
//                showSearchUserProgress(searchUserProgressLayout, false);
//            }
//        }
//
//        mSearchText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
////                groupAdapter.setSearchString(s.toString());
//                if (mSearchText.getText().toString().length() > 0) {
//                    mTextClearBtn.setVisibility(View.VISIBLE);
//                    mSearchBtn.setVisibility(View.VISIBLE);
//                } else {
//                    mTextClearBtn.setVisibility(View.GONE);
//                    mSearchBtn.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//
//        mSearchBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DataManager dataManager = new DataManager(account);
//                ConcurrentAsyncTask.execute(new SearchUsersTask(dataManager, mSearchText.getText().toString()));
//            }
//        });
//
//        mTextClearBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mSearchText.getText().clear();
//            }
//        });
//
//        cancelCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//
//        okCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                selectedShareUsers.clear();
//                selectedShareUsers.addAll(userAdapter.getSelectedShareUsers());
//                setGroupText();
//                dialog.dismiss();
//            }
//        });
//
//        dialog.show();
//    }

    private void showSelectPermissionPopup(View anchor) {

        LayoutInflater mInflater;

        try {
            mInflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_permission2, null);
            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);

            ListView permissionListView = layout.findViewById(android.R.id.list);
            CardView addPermissionCard = layout.findViewById(R.id.add_permission_card);
            View addPermissionDivider = layout.findViewById(R.id.add_permission_divider);

            addPermissionCard.setVisibility(mShareDialogActivity.checkServerProEdition()? View.VISIBLE : View.GONE);
            addPermissionDivider.setVisibility(addPermissionCard.getVisibility());

            SeafPermission2Adapter permission2Adapter = new SeafPermission2Adapter(mShareDialogActivity);
            permissionListView.setAdapter(permission2Adapter);
//            permissionListView.setDivider(null);

            permission2Adapter.setItems(seafPermissions, selectedIndex == -1 ? selectedPermission.getId() : seafSharedItems.get(selectedIndex).getPermission());

            permissionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedPermission = seafPermissions.get(i);
                    if (selectedIndex == -1) {
                        selectPermissionText.setText(selectedPermission.getName());
                    } else {
                        ConcurrentAsyncTask.execute(new UpdateShareToGroupItemPermission());
                    }

                    mSelectPermissionDropdown.dismiss();
                }
            });

            addPermissionCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSelectPermissionDropdown.dismiss();
                    mShareDialogActivity.showCustomSharingPermission();
                }
            });

            mSelectPermissionDropdown = new PopupWindow(layout, selectPermissionLayout.getWidth(), getListViewHeightBasedOnChildren(permissionListView, permission2Adapter, addPermissionCard) , true);//FrameLayout.LayoutParams.WRAP_CONTENT
            mSelectPermissionDropdown.showAsDropDown(selectedIndex == -1 ? selectPermissionLayout : anchor, 5, 5, Gravity.BOTTOM);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getListViewHeightBasedOnChildren(ListView listView, SeafPermission2Adapter adapter, CardView addPermissionCard) {
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View item1 = adapter.getView(i, null, listView);
            item1.measure(0, 0);
            if (i < 5) {
                totalHeight += item1.getMeasuredHeight();
            }
        }
//        + (listView.getDividerHeight() * (adapter.getCount() - 1))
        int height = totalHeight + (mShareDialogActivity.checkServerProEdition() ? addPermissionCard.getMeasuredHeight() : 0);
        return height;
    }

    private void setGroupText() {
//        StringBuilder names = new StringBuilder();
        String text = "";
        if (isShareGroup) {
//            for (int i = 0; i < selectedShareGroups.size(); i++) {
//                names.append(selectedShareGroups.get(i).getName());
//                if (i != selectedShareGroups.size() -1) {
//                    names.append(", ");
//                }
//            }
            int groupSize = selectedShareGroups.size();
            switch (groupSize) {
                case 0:
                    text = getString(R.string.select_group);
                    break;
                case 1:
                    text = getString(R.string.group_selected, groupSize);
                    break;
                default:
                    text = getString(R.string.groups_selected, groupSize);
                    break;
            }
        } else {
//            for (int i = 0; i < selectedShareUsers.size(); i++) {
//                names.append(selectedShareUsers.get(i).getName());
//                if (i != selectedShareUsers.size() -1) {
//                    names.append(", ");
//                }
//            }
            int userSize = selectedShareUsers.size();
            switch (userSize) {
                case 0:
                    text = getString(R.string.select_user);
                    break;
                case 1:
                    text = getString(R.string.user_selected, userSize);
                    break;
                default:
                    text = getString(R.string.users_selected, userSize);
                    break;
            }
        }
        selectGroupText.setText(text);
    }

    private void sortSeafSharedItems() {
        Collections.sort(seafSharedItems, isShareGroup? new SeafSharedItem.GroupNameComparator() : new SeafSharedItem.UserNameComparator());
    }

    private void refreshSharedListView() {
        sortSeafSharedItems();
        adapter.setItems(seafSharedItems, seafPermissions, isShareGroup);
        adapter.notifyChanged();
        setListViewHeightBasedOnChildren(sharedListView, adapter);
    }

    private void setListViewHeightBasedOnChildren(ListView listView, SeafSharedItemAdapter adapter) {
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View item1 = adapter.getView(i, null, listView);
            item1.measure(0, 0);
            totalHeight += item1.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        if (height > 300) height = 300;
        params.height = height;
        listView.setLayoutParams(params);
    }

    public void updateSeafPermissions(ArrayList<SeafPermission> seafPermissionsResult) {
        seafPermissions.clear();
        if (mShareDialogActivity.checkServerProEdition()) {
            seafPermissions.addAll(Utils.getNormalSeafPermissions(mActivity));
            Collections.sort(seafPermissionsResult, new SeafPermission.PermissionNameComparator());
            seafPermissions.addAll(seafPermissionsResult);
        } else
            seafPermissions.addAll(Utils.getCESeafPermissions(mActivity));

        SeafPermission newSelectedPermission = null;
        if (selectedPermission != null) {
            for(SeafPermission permission: seafPermissions) {
                if (selectedPermission.getId().equals(permission.getId())) {
                    newSelectedPermission = permission;
                }
            }
        }
        if (newSelectedPermission == null) {
            selectedPermission = seafPermissions.get(0);
        } else {
            selectedPermission = newSelectedPermission;
        }
        selectPermissionText.setText(selectedPermission.getName());
    }

    private void showEditItemPopup(View anchor) {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_group, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mDropdown = new PopupWindow(layout, permissionCationText.getWidth(),
                    (int)popupSelectGroupView.getHeight(), true);//FrameLayout.LayoutParams.WRAP_CONTENT

            final CardView editCard = layout.findViewById(R.id.edit_card);
            final CardView deleteCard = layout.findViewById(R.id.delete_card);

            editCard.setOnClickListener(view -> {
                mDropdown.dismiss();
//                showSelectPermissionDialog();
                showSelectPermissionPopup(anchor);
            });
            deleteCard.setOnClickListener(view -> {
                ConcurrentAsyncTask.execute(new DeleteShareToGroupItem());
                mDropdown.dismiss();
            });

//            Drawable background = getResources().getDrawable(android.R.drawable.editbox_dropdown_dark_frame);
//            mDropdown.setBackgroundDrawable(background);
            mDropdown.showAsDropDown(anchor, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ShareableGroups extends AsyncTask<Void, Long, Void> {
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                DataManager dataManager = new DataManager(account);
                seafShareGroups = dataManager.shareableGroups();
            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            if (err == null) {
                ConcurrentAsyncTask.execute(new ListAllPermissions());
            } else {
                Toast.makeText(mActivity, getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                mActivity.finish();
            }
        }
    }

    public class ListAllPermissions extends AsyncTask<Void, Long, Void> {
        private ArrayList<SeafPermission> seafPermissionsResult = Lists.newArrayList();
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                DataManager dataManager = new DataManager(account);
                seafPermissionsResult = dataManager.listCustomPermissions(repoID);

            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            if (err == null) {
                ConcurrentAsyncTask.execute(new ListSharedItems());

                updateSeafPermissions(seafPermissionsResult);
                initInputs();
            } else {
                Toast.makeText(mActivity, getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                mActivity.finish();
            }
        }
    }

    public class ListSharedItems extends AsyncTask<Void, Long, Void> {
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                DataManager dataManager = new DataManager(account);
                //getting sharedItems
                seafSharedItems = dataManager.listSharedItems(repoID, path, shareType);

            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mShareDialogActivity.changeProgress(false);
            if (err == null) {
                refreshSharedListView();
            } else {
                Toast.makeText(mActivity, getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                mActivity.finish();
            }
        }
    }

    public class ShareFolder extends AsyncTask<Void, Long, Void> {
        private ArrayList<SeafSharedItem> resultItems;
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                DataManager dataManager = new DataManager(account);
                dataManager.listSharedItems(repoID, path, shareType);
                //creating a Share Folder
                ArrayList<String> paramsArray = Lists.newArrayList();
                if (isShareGroup) {
                    for (SeafShareableGroup item: selectedShareGroups) {
                        paramsArray.add(item.getId());
                    }
                } else {
                    for (SeafShareUser item: selectedShareUsers) {
                        paramsArray.add(item.getEmail());
                    }
                }

                resultItems = dataManager.shareFolder(repoID, path, shareType, selectedPermission.getId(), paramsArray);
            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mShareDialogActivity.changeProgress(false);
            if (err == null) {
                for (SeafSharedItem item: resultItems) {
                    if (!seafSharedItems.contains(item)) {
                        seafSharedItems.add(item);
                    }
                }
                initInputs();
                refreshSharedListView();
            } else {
                Toast.makeText(mActivity, getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                mActivity.finish();
            }
        }
    }

    public class UpdateShareToGroupItemPermission extends AsyncTask<Void, Long, Void> {
        private Boolean resultUpdate;
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                DataManager dataManager = new DataManager(account);
                //updating a Shared Folder
                resultUpdate = conn.updateShareToGroupItemPermission(repoID, path, shareType,
                        shareType.equals(ShareGroupFragment.SHARE_TYPE_GROUP)? seafSharedItems.get(selectedIndex).getGroup_info_id() : seafSharedItems.get(selectedIndex).getUser_info_name(),
                        selectedPermission.getId());
            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mShareDialogActivity.changeProgress(false);
            if (err == null) {
                if (resultUpdate) {
                    SeafSharedItem seafSharedItem = seafSharedItems.get(selectedIndex);
                    seafSharedItem.setPermission(selectedPermission.getId());
                    seafSharedItems.set(selectedIndex, seafSharedItem);
                }
                refreshSharedListView();
            } else {
                Toast.makeText(mActivity, getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                mActivity.finish();
            }
        }
    }

    public class DeleteShareToGroupItem extends AsyncTask<Void, Long, Void> {
        private Boolean resultDelete;
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                DataManager dataManager = new DataManager(account);
                //updating a Shared Folder
                resultDelete = conn.deleteShareToGroupItem(repoID, path, shareType,
                        isShareGroup? seafSharedItems.get(selectedIndex).getGroup_info_id() : seafSharedItems.get(selectedIndex).getUser_info_name());
            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mShareDialogActivity.changeProgress(false);
            if (err == null) {
                if (resultDelete) {
                    seafSharedItems.remove(selectedIndex);
                }
                refreshSharedListView();
            } else {
                Toast.makeText(mActivity, getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                mActivity.finish();
            }
        }
    }

    private int getScreenHeight() {
        Rect r = new Rect();
        container.getWindowVisibleDisplayFrame(r);
        return container.getRootView().getHeight();
    }

    private void hideKeyboard() {
        if (screenHeight > getScreenHeight()) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }
}
