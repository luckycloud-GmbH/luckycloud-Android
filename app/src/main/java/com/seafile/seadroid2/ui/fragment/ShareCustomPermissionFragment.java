package com.seafile.seadroid2.ui.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
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
import com.seafile.seadroid2.data.SeafPermission;
import com.seafile.seadroid2.ui.activity.ShareDialogActivity;
import com.seafile.seadroid2.ui.adapter.SeafPermissionAdapter;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;


public class ShareCustomPermissionFragment extends Fragment {


    private CardView backCard;
    private TextView backText;
    private View showPermissionsLayout;
    private View addPermissionLayout;
    private View listViewLayout;
    private ListView permissionList;
    private CardView addPermissionCard;
    private EditText permissionNameText;
    private EditText permissionDescText;
    private SwitchCompat uploadSwitch;
    private SwitchCompat downloadSwitch;
    private SwitchCompat createSwitch;
    private SwitchCompat modifySwitch;
    private SwitchCompat copySwitch;
    private SwitchCompat deleteSwitch;
    private SwitchCompat previewOnlineSwitch;
    private SwitchCompat generateShareLinkSwitch;
    private CardView submitCard;
    private View popupSelectGroupView;

    private PopupWindow mDropdown = null;
    private SeafPermissionAdapter adapter;

    private ShareDialogActivity mShareDialogActivity;
    private Account mAccount;
    private String dialogType;
    private Account account;
    private String repoID;
    private String path;
    private String fileName;
    private SeafConnection conn;
    private int selectedIndex;
    private SeafPermission seafPermission;
    private ArrayList<SeafPermission> seafPermissions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.share_custom_permission_fragment, container, false);
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
        backCard = root.findViewById(R.id.back_card);
        backText = root.findViewById(R.id.back_text);
        showPermissionsLayout = root.findViewById(R.id.share_custom_permission_show_layout);
        addPermissionLayout = root.findViewById(R.id.share_custom_permission_add_layout);
        listViewLayout = root.findViewById(R.id.list_view_layout);
        permissionList = root.findViewById(android.R.id.list);
        addPermissionCard = root.findViewById(R.id.add_permission_card);
        permissionNameText = root.findViewById(R.id.permission_name_text);
        permissionDescText = root.findViewById(R.id.permission_desc_text);
        uploadSwitch = root.findViewById(R.id.upload_switch);
        downloadSwitch = root.findViewById(R.id.download_switch);
        createSwitch = root.findViewById(R.id.create_switch);
        modifySwitch = root.findViewById(R.id.modify_switch);
        copySwitch = root.findViewById(R.id.copy_switch);
        deleteSwitch = root.findViewById(R.id.delete_switch);
        previewOnlineSwitch = root.findViewById(R.id.preview_online_switch);
        generateShareLinkSwitch = root.findViewById(R.id.generate_share_link_switch);
        submitCard = root.findViewById(R.id.submit_card);
        popupSelectGroupView = root.findViewById(R.id.popup_select_group_layout);
    }

    private void initViewAction() {
        permissionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedIndex = i;
                showEditPermissionPopup(view);
            }
        });
        addPermissionCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddPermissionLayout(-1);
            }
        });
        backCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (backText.getText().toString().equals(mShareDialogActivity.getResources().getString(R.string.add_permission))) {
                    closeAddPermissionLayout();
                } else {
                    mShareDialogActivity.closeCustomSharingPermission();
                }
            }
        });
        submitCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (permissionNameText.getText().toString().isEmpty()) {
                    permissionNameText.setError(getActivity().getString(R.string.err_field_empty));
                    return;
                }
                if (permissionDescText.getText().toString().isEmpty()) {
                    permissionDescText.setError(getActivity().getString(R.string.err_field_empty));
                    return;
                }
                CreateCustomPermissionTask task = new CreateCustomPermissionTask();
                ConcurrentAsyncTask.execute(task);
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

        closeAddPermissionLayout();

        adapter = new SeafPermissionAdapter(mShareDialogActivity);
        permissionList.setAdapter(adapter);
//        permissionList.setDivider(null);

        ListCustomPermissions task = new ListCustomPermissions();
        ConcurrentAsyncTask.execute(task);
    }

    private void showAddPermissionLayout(int index) {
        selectedIndex = index;
        permissionNameText.setText("");
        permissionDescText.setText("");
        uploadSwitch.setChecked(false);
        downloadSwitch.setChecked(false);
        createSwitch.setChecked(false);
        modifySwitch.setChecked(false);
        copySwitch.setChecked(false);
        deleteSwitch.setChecked(false);
        previewOnlineSwitch.setChecked(false);
        generateShareLinkSwitch.setChecked(false);
        if (index != -1) { //Update custom permission
            seafPermission = seafPermissions.get(index);
            permissionNameText.setText(seafPermission.getName());
            permissionDescText.setText(seafPermission.getDescription());
            if (!seafPermission.getPermission().isEmpty()) {
                try {
                    JSONObject jsonObject = Utils.parseJsonObject(seafPermission.getPermission());

                    uploadSwitch.setChecked(jsonObject != null && jsonObject.getBoolean("upload"));
                    downloadSwitch.setChecked(jsonObject != null && jsonObject.getBoolean("download"));
                    createSwitch.setChecked(jsonObject != null && jsonObject.getBoolean("create"));
                    modifySwitch.setChecked(jsonObject != null && jsonObject.getBoolean("modify"));
                    copySwitch.setChecked(jsonObject != null && jsonObject.getBoolean("copy"));
                    deleteSwitch.setChecked(jsonObject != null && jsonObject.getBoolean("delete"));
                    previewOnlineSwitch.setChecked(jsonObject != null && jsonObject.getBoolean("preview"));
                    generateShareLinkSwitch.setChecked(jsonObject != null && jsonObject.getBoolean("download_external_link"));
                } catch (Exception e) {

                }

            }
        }

        showPermissionsLayout.setVisibility(View.GONE);
        addPermissionLayout.setVisibility(View.VISIBLE);
        backText.setText(mShareDialogActivity.getResources().getString(R.string.add_permission));
    }

    private void closeAddPermissionLayout() {
        showPermissionsLayout.setVisibility(View.VISIBLE);
        addPermissionLayout.setVisibility(View.GONE);
        backText.setText(mShareDialogActivity.getResources().getString(R.string.back_selection));
    }

    private void sortSeafPermissions() {
        Collections.sort(seafPermissions, new SeafPermission.PermissionNameComparator());
    }

    private void processSeafPermission(boolean isDelete) {
        if (selectedIndex == -1) {
            seafPermissions.add(seafPermission);
        } else {
            seafPermissions.remove(selectedIndex);
            if (!isDelete) {
                seafPermissions.add(seafPermission);
            }
        }
        sortSeafPermissions();
        adapter.setItems(seafPermissions);
        adapter.notifyChanged();
        setListViewHeight();

        closeAddPermissionLayout();

        mShareDialogActivity.getShareGroupFragment().updateSeafPermissions(seafPermissions);
    }

    private void showEditPermissionPopup(View anchor) {

        LayoutInflater mInflater;

        try {

            mInflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = mInflater.inflate(R.layout.popup_select_group, null);

            layout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);
            mDropdown = new PopupWindow(layout, popupSelectGroupView.getWidth(),
                    popupSelectGroupView.getHeight(), true);//FrameLayout.LayoutParams.WRAP_CONTENT

            final CardView editCard = layout.findViewById(R.id.edit_card);
            final CardView deleteCard = layout.findViewById(R.id.delete_card);

            editCard.setOnClickListener(view -> {
                mDropdown.dismiss();
                showAddPermissionLayout(selectedIndex);
            });
            deleteCard.setOnClickListener(view -> {

                ConcurrentAsyncTask.execute(new DeleteCustomPermissionTask());
                mDropdown.dismiss();
            });

//            Drawable background = getResources().getDrawable(android.R.drawable.editbox_dropdown_dark_frame);
//            mDropdown.setBackgroundDrawable(background);
            mDropdown.showAsDropDown(anchor, 5, 5, Gravity.BOTTOM | Gravity.RIGHT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ListCustomPermissions extends AsyncTask<Void, Long, Void> {
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                DataManager dataManager = new DataManager(account);
                seafPermissions = dataManager.listCustomPermissions(repoID);

            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mShareDialogActivity.changeProgress(false);
            if (err == null) {
                sortSeafPermissions();
                adapter.setItems(seafPermissions);
                adapter.notifyChanged();
                setListViewHeight();
            } else {
                Toast.makeText(getActivity(), getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    public class CreateCustomPermissionTask extends AsyncTask<Void, Long, Void> {
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                String name = permissionNameText.getText().toString();
                String desc = permissionDescText.getText().toString();
                JSONObject permissionObject = new JSONObject();
                String permission;

                permissionObject.put("upload", uploadSwitch.isChecked());
                permissionObject.put("download", downloadSwitch.isChecked());
                permissionObject.put("create", createSwitch.isChecked());
                permissionObject.put("modify", modifySwitch.isChecked());
                permissionObject.put("copy", copySwitch.isChecked());
                permissionObject.put("delete", deleteSwitch.isChecked());
                permissionObject.put("preview", previewOnlineSwitch.isChecked());
                permissionObject.put("download_external_link", generateShareLinkSwitch.isChecked());

                permission = permissionObject.toString();

                if (selectedIndex == -1) {
                    seafPermission = conn.createCustomPermission(repoID, name, desc, permission);
                } else {
                    seafPermission = conn.updateCustomPermission(repoID, seafPermission.getId(), name, desc, permission);
                }

            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mShareDialogActivity.changeProgress(false);
            if (err == null) {
                processSeafPermission(false);
            } else {
                Toast.makeText(getActivity(), getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    public class DeleteCustomPermissionTask extends AsyncTask<Void, Long, Void> {
        private Boolean deleteCustomPermissionResult;
        private Exception err;

        @Override
        public Void doInBackground(Void... params) {
            mShareDialogActivity.changeProgress(true);
            try {
                DataManager dataManager = new DataManager(account);
                //delete custom permission
                deleteCustomPermissionResult = dataManager.deleteCustomPermission(repoID, seafPermissions.get(selectedIndex).getId());
            } catch (Exception e) {
                err = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            mShareDialogActivity.changeProgress(false);
            if (err == null) {
                if (deleteCustomPermissionResult) {
                    processSeafPermission(true);
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    private void setListViewHeight() {
        int maxHeight = 0;
        maxHeight = listViewLayout.getMeasuredHeight() - addPermissionCard.getMeasuredHeight() - (int) mShareDialogActivity.getResources().getDimension(R.dimen.margin_large);

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, permissionList);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();

        }
        totalHeight += (permissionList.getDividerHeight() * (adapter.getCount() - 1));
        
        ViewGroup.LayoutParams params = permissionList.getLayoutParams();
        params.height = Math.min(maxHeight, totalHeight);
        permissionList.setLayoutParams(params);
        permissionList.requestLayout();
    }
}
