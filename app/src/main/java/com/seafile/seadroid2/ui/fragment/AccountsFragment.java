package com.seafile.seadroid2.ui.fragment;

import android.Manifest;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeafConnection;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.account.AccountManager;
import com.seafile.seadroid2.account.Authenticator;
import com.seafile.seadroid2.avatar.Avatar;
import com.seafile.seadroid2.avatar.AvatarManager;
import com.seafile.seadroid2.monitor.FileMonitorService;
import com.seafile.seadroid2.ui.activity.AccountsActivity;
import com.seafile.seadroid2.ui.activity.BrowserActivity;
import com.seafile.seadroid2.ui.adapter.AccountAdapterNew2;
import com.seafile.seadroid2.ui.dialog.PolicyDialog;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountsFragment extends Fragment {
    private static final String DEBUG_TAG = "AccountsFragment";

    public static final int DETAIL_ACTIVITY_REQUEST = 41;

    private BrowserActivity mActivity;

    private GridView accountsView;

    CardView m_btnGoLogin;
    CardView m_btnGoNewAccount;
    TextView dataPrivacyText;

    private ImageView logo;
    private android.accounts.AccountManager mAccountManager;
    private AccountManager accountManager;
    private AvatarManager avatarManager;
    private AccountAdapterNew2 adapter;
    private List<Account> accounts;
    private FileMonitorService mMonitorService;
    private Account currentDefaultAccount;

    private boolean m_isFromBiometricAuth = false;

    private final OnAccountsUpdateListener accountsUpdateListener = new OnAccountsUpdateListener() {
        @Override
        public void onAccountsUpdated(android.accounts.Account[] accounts) {
            refreshView();
        }
    };

    private final ServiceConnection mMonitorConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            FileMonitorService.MonitorBinder monitorBinder = (FileMonitorService.MonitorBinder) binder;
            mMonitorService = monitorBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mMonitorService = null;
        }

    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Log.d(DEBUG_TAG, "AccountsFragment Attached");
        mActivity = (BrowserActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.accounts_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        m_isFromBiometricAuth = mActivity.getIntent().getBooleanExtra("FROM_BIOMETRIC_AUTH", false);
        logo = view.findViewById(R.id.icon);

        mAccountManager = android.accounts.AccountManager.get(mActivity);
        accountsView = (GridView) view.findViewById(R.id.account_list_view);
        accountManager = new AccountManager(mActivity);
        avatarManager = new AvatarManager();
        currentDefaultAccount = accountManager.getCurrentAccount();

        String DeviceLang = Resources.getSystem().getConfiguration().locale.getLanguage();
        if (DeviceLang.equals("de")) {
            logo.setScaleX(1.05f);
            logo.setScaleY(1.05f);
        }

        m_btnGoLogin = view.findViewById(R.id.btnGoLogin);
        m_btnGoNewAccount = view.findViewById(R.id.btnGoNewAccount);

        m_btnGoNewAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View btn) {
//                Uri urilucky = Uri.parse("https://luckycloud.de/de/");
//                //Intent intent = new Intent(Intent.ACTION_VIEW, urilucky);
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(urilucky);
//                startActivity(intent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Utils.isManaged(mActivity)) {
                        Intent intent = android.accounts.AccountManager.newChooseAccountIntent(
                                null,
                                null,
                                new String[]{Account.ACCOUNT_TYPE},
                                null,
                                Authenticator.AUTHTOKEN_TYPE,
                                null,
                                null);
                        startActivityForResult(intent, DETAIL_ACTIVITY_REQUEST);
                    } else {
                        mAccountManager.addAccount(Account.ACCOUNT_TYPE,
                                Authenticator.AUTHTOKEN_TYPE, null, null,
                                mActivity, accountCallback, null);
                    }
                } else {
                    mAccountManager.addAccount(Account.ACCOUNT_TYPE,
                            Authenticator.AUTHTOKEN_TYPE, null, null,
                            mActivity, accountCallback, null);
                }
            }
        });

        m_btnGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View btn) {
//                AccountsActivity.setIsEnterpriseLogin(false);
//                mAccountManager.addAccount(Account.ACCOUNT_TYPE,
//                        Authenticator.AUTHTOKEN_TYPE, null, null,
//                        mActivity, accountCallback, null);
            }
        });

        dataPrivacyText = view.findViewById(R.id.data_privacy_text);
        dataPrivacyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri urilucky = Uri.parse(getResources().getString(R.string.data_privacy_url));
                //Intent intent = new Intent(Intent.ACTION_VIEW, urilucky);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(urilucky);
                startActivity(intent);
            }
        });

        adapter = new AccountAdapterNew2(mActivity, accountManager, AccountAdapterNew2.AccountType.AccountFragment);
        accountsView.setAdapter(adapter);

        accountsView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Dialog dialog = Utils.CustomDialog(mActivity);
                dialog.setContentView(R.layout.dialog_account);

                TextView titleText = dialog.findViewById(R.id.title_text);
                CardView editCard = dialog.findViewById(R.id.edit_card);
                CardView deleteCard = dialog.findViewById(R.id.delete_card);

                Account account = accounts.get(position);
                titleText.setText(account.getName());
                editCard.setOnClickListener(v -> {
                    dialog.dismiss();
                    startEditAccountActivity(account);
                });
                deleteCard.setOnClickListener(v -> {
                    dialog.dismiss();
                    mAccountManager.removeAccount(account.getAndroidAccount(), null, null);

                    if (mMonitorService != null) {
                        mMonitorService.removeAccount(account);
                    }
                });

                dialog.show();

                return true;
            }
        });

        mAccountManager.addOnAccountsUpdatedListener(accountsUpdateListener, null, false);

        registerForContextMenu(accountsView);

//        Toolbar toolbar = mActivity.getActionBarToolbar();
//        toolbar.setOnMenuItemClickListener(mActivity);
//        mActivity.setSupportActionBar(toolbar);

        accounts = accountManager.getAccountList();
        // updates toolbar back button
//        if (currentDefaultAccount == null || !currentDefaultAccount.hasValidToken()) {
//            mActivity.disableUpButton();
//        } else {
//            mActivity.enableUpButton();
//        }

        String country = Locale.getDefault().getCountry();
        String language = Locale.getDefault().getLanguage();
        int privacyPolicyConfirmed = SettingsManager.instance().getPrivacyPolicyConfirmed();
        if (country.equals("CN") && language.equals("zh") && (privacyPolicyConfirmed == 0)) {
            showDialog();
        }
        AccountsActivity.setFromAccountsActivity(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent bIntent = new Intent(mActivity, FileMonitorService.class);
        mActivity.bindService(bIntent, mMonitorConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMonitorService != null) {
            mActivity.unbindService(mMonitorConnection);
            mMonitorService = null;
        }
        mAccountManager.removeOnAccountsUpdatedListener(accountsUpdateListener);
    }

    // Always reload accounts on resume, so that when user add a new account,
    // it will be shown.
    @Override
    public void onResume() {
        super.onResume();

        refreshView();
    }

    public void onAccountClick(int position) {
        Account account = accounts.get(position);
        if (!account.hasValidToken()) {
            // user already signed out, input password first
            startEditAccountActivity(account);
        } else {
            // update current Account info from SharedPreference
            accountManager.saveCurrentAccount(account.getSignature());
            SettingsManager.gIsLoginedUser = true;
            startFilesActivity();
        }
    }

    public void onAccountEdit(Account account) {
        startEditAccountActivity(account);
    }

    public void onAccountDelete(Account account) {
        Log.d(DEBUG_TAG, "removing account "+account);
        mAccountManager.removeAccount(account.getAndroidAccount(), null, null);

        if (mMonitorService != null) {
            mMonitorService.removeAccount(account);
        }
    }

    public void onAccountLogout(Account account) {
        account.token = "";
    }

    private void refreshView() {
        Log.d(DEBUG_TAG, "refreshView");
        accounts = accountManager.getAccountList();
        adapter.clear();
        adapter.setItems(accounts);

        // if the user switched default account while we were in background,
        // switch to BrowserActivity
        Account newCurrentAccount = accountManager.getCurrentAccount();
        if (newCurrentAccount != null && !newCurrentAccount.equals(currentDefaultAccount)) {
            startFilesActivity();
        }

        // updates toolbar back button
        if (newCurrentAccount == null || !newCurrentAccount.hasValidToken()) {
            mActivity.disableUpButton();
        }

        loadAvatarUrls(160);

        adapter.notifyChanged();
    }

    private void startFilesActivity() {
        if (mActivity != null) {
            SettingsFragment settingsFragment = mActivity.getSettingsFragment();
            if (settingsFragment != null) {
                settingsFragment.updateThumbImagesCount(-1, -1);
            }
        }

        mActivity.getDataManager().setReposRefreshTimeStamp(0);
        Intent intent = new Intent(mActivity, BrowserActivity.class);

        // first finish this activity, so the BrowserActivity is again "on top"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        mActivity.finish();
    }

    final AccountManagerCallback<Bundle> accountCallback = new AccountManagerCallback<Bundle>() {

        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            if (future.isCancelled())
                return;

            try {
                Bundle b = future.getResult();

                if (b.getBoolean(android.accounts.AccountManager.KEY_BOOLEAN_RESULT)) {
                    String accountName = b.getString(android.accounts.AccountManager.KEY_ACCOUNT_NAME);
                    Log.d(DEBUG_TAG, "switching to account " + accountName);
                    accountManager.saveCurrentAccount(accountName);
                    startFilesActivity();
                }
            } catch (Exception e) {
                Log.e(DEBUG_TAG, "unexpected error: " + e);
            }
        }
    };

    private void startEditAccountActivity(Account account) {
        mAccountManager.updateCredentials(account.getAndroidAccount(), Authenticator.AUTHTOKEN_TYPE, null, mActivity, accountCallback, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DETAIL_ACTIVITY_REQUEST:
                if (resultCode == mActivity.RESULT_OK) {
                    String accountName = data.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME);
                    Log.d(DEBUG_TAG, "switching to account " + accountName);
                    accountManager.saveCurrentAccount(accountName);
                    startFilesActivity();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
//        android.view.MenuInflater inflater = mActivity.getMenuInflater();
//        inflater.inflate(R.menu.account_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Account account;
        switch (item.getItemId()) {
            case R.id.edit:
                account = adapter.getItem((int)info.id);
                onAccountEdit(account);
                return true;
            case R.id.delete:
                account = adapter.getItem((int)info.id);
                onAccountDelete(account);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * asynchronously load avatars
     *
     * @param avatarSize set a avatar size in one of 24*24, 32*32, 48*48, 64*64, 72*72, 96*96
     */
    public void loadAvatarUrls(int avatarSize) {
        List<Avatar> avatars;

        if (!Utils.isNetworkOn() || !avatarManager.isNeedToLoadNewAvatars()) {
            // Toast.makeText(AccountsActivity.this, getString(R.string.network_down), Toast.LENGTH_SHORT).show();

            // use cached avatars
            avatars = avatarManager.getAvatarList();

            if (avatars == null) {
                return;
            }

            // set avatars url to adapter
            adapter.setAvatars((ArrayList<Avatar>) avatars);

            // notify adapter data changed
            adapter.notifyDataSetChanged();

            return;
        }

        AccountsFragment.LoadAvatarUrlsTask task = new AccountsFragment.LoadAvatarUrlsTask(avatarSize);

        ConcurrentAsyncTask.execute(task);

    }

    private class LoadAvatarUrlsTask extends AsyncTask<Void, Void, List<Avatar>> {

        private List<Avatar> avatars;
        private int avatarSize;
        private SeafConnection httpConnection;

        public LoadAvatarUrlsTask(int avatarSize) {
            this.avatarSize = avatarSize;
            this.avatars = Lists.newArrayList();
        }

        @Override
        protected List<Avatar> doInBackground(Void... params) {
            // reuse cached avatars
            avatars = avatarManager.getAvatarList();

            // contains accounts who don`t have avatars yet
            List<Account> acts = avatarManager.getAccountsWithoutAvatars();

            // contains new avatars in order to persist them to database
            List<Avatar> newAvatars = new ArrayList<Avatar>(acts.size());

            // load avatars from server
            for (Account account : acts) {
                httpConnection = new SeafConnection(account);

                String avatarRawData = null;
                try {
                    avatarRawData = httpConnection.getAvatar(account.getEmail(), avatarSize);
                } catch (SeafException e) {
                    e.printStackTrace();
                    return avatars;
                }

                Avatar avatar = avatarManager.parseAvatar(avatarRawData);
                if (avatar == null)
                    continue;

                avatar.setSignature(account.getSignature());

                avatars.add(avatar);

                newAvatars.add(avatar);
            }

            // save new added avatars to database
            avatarManager.saveAvatarList(newAvatars);

            return avatars;
        }

        @Override
        protected void onPostExecute(List<Avatar> avatars) {
            if (avatars == null) {
                return;
            }

            // set avatars url to adapter
            adapter.setAvatars((ArrayList<Avatar>) avatars);

            // notify adapter data changed
            adapter.notifyDataSetChanged();
        }
    }

    private void showDialog() {
        PolicyDialog mDialog = new PolicyDialog(mActivity, R.style.PolicyDialog,
                new PolicyDialog.OnCloseListener() {
                    @Override
                    public void onClick(boolean confirm) {
                        if (confirm) {
                            // TODO:
                            SettingsManager.instance().savePrivacyPolicyConfirmed(1);
                        } else {
                            // TODO:
                            System.exit(0);
                        }
                    }
                });
        mDialog.show();
        mDialog.setCancelable(false);

    }

}
