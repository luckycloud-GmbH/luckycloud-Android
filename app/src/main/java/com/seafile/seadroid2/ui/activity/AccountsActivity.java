package com.seafile.seadroid2.ui.activity;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OnAccountsUpdateListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SeafConnection;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.account.AccountManager;
import com.seafile.seadroid2.account.Authenticator;
import com.seafile.seadroid2.avatar.Avatar;
import com.seafile.seadroid2.avatar.AvatarManager;
import com.seafile.seadroid2.monitor.FileMonitorService;
import com.seafile.seadroid2.ui.adapter.AccountAdapterNew2;
import com.seafile.seadroid2.ui.dialog.PolicyDialog;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AccountsActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {
    private static final String DEBUG_TAG = "AccountsActivity";

    public static final int DETAIL_ACTIVITY_REQUEST = 1;

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

    private static boolean g_isEnterpriseLogin = false;
    private static boolean g_fromAccountsActivity = false;

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

    public static boolean getIsEnterpriseLogin() {
        return g_isEnterpriseLogin;
    }

    public static void setIsEnterpriseLogin(boolean isEnterpriseLogin) {
        g_isEnterpriseLogin = isEnterpriseLogin;
    }

    public static boolean getFromAccountsActivity() {
        return g_fromAccountsActivity;
    }

    public static void setFromAccountsActivity(boolean fromAccountsActivity) {
        g_fromAccountsActivity = fromAccountsActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        setContentView(R.layout.start);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        });

        m_isFromBiometricAuth = getIntent().getBooleanExtra("FROM_BIOMETRIC_AUTH", false);
        logo = findViewById(R.id.icon);

        mAccountManager = android.accounts.AccountManager.get(this);
        accountsView = (GridView) findViewById(R.id.account_list_view);
        accountManager = new AccountManager(this);
        avatarManager = new AvatarManager();
        currentDefaultAccount = accountManager.getCurrentAccount();

        String DeviceLang = Resources.getSystem().getConfiguration().locale.getLanguage();
        if (DeviceLang.equals("de")) {
            logo.setScaleX(1.05f);
            logo.setScaleY(1.05f);
        }

        m_btnGoLogin = findViewById(R.id.btnGoLogin);
        m_btnGoNewAccount = findViewById(R.id.btnGoNewAccount);

        m_btnGoNewAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View btn) {
//                Uri urilucky = Uri.parse("https://luckycloud.de/de/");
//                //Intent intent = new Intent(Intent.ACTION_VIEW, urilucky);
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(urilucky);
//                startActivity(intent);
                g_fromAccountsActivity = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                            AccountsActivity.this, accountCallback, null);
                }
            }
        });

        m_btnGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View btn) {
//                g_fromAccountsActivity = true;
//                mAccountManager.addAccount(Account.ACCOUNT_TYPE,
//                     Authenticator.AUTHTOKEN_TYPE, null, null,
//                     AccountsActivity.this, accountCallback, null);
            }
        });

        dataPrivacyText = findViewById(R.id.data_privacy_text);
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

        adapter = new AccountAdapterNew2(this, accountManager, AccountAdapterNew2.AccountType.AccountActivity);
        accountsView.setAdapter(adapter);

        mAccountManager.addOnAccountsUpdatedListener(accountsUpdateListener, null, false);

        registerForContextMenu(accountsView);

//        Toolbar toolbar = getActionBarToolbar();
//        toolbar.setOnMenuItemClickListener(this);
//        setSupportActionBar(toolbar);

        accounts = accountManager.getAccountList();
        // updates toolbar back button
//        if (currentDefaultAccount == null || !currentDefaultAccount.hasValidToken()) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        } else {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
//
//        getSupportActionBar().setTitle(R.string.accounts);

        String country = Locale.getDefault().getCountry();
        String language = Locale.getDefault().getLanguage();
        int privacyPolicyConfirmed = SettingsManager.instance().getPrivacyPolicyConfirmed();
        if (country.equals("CN") && language.equals("zh") && (privacyPolicyConfirmed == 0)) {
            showDialog();
        }

        AccountsActivity.setFromAccountsActivity(true);

        if (SeadroidApplication.getInstance().getLoginConfig().size() == 3) {
            m_btnGoNewAccount.callOnClick();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent bIntent = new Intent(this, FileMonitorService.class);
        bindService(bIntent, mMonitorConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMonitorService != null) {
            unbindService(mMonitorConnection);
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
            case android.R.id.home:
                // if the current account sign out and no account was to logged in,
                // then always goes to AccountsActivity
                if (accountManager.getCurrentAccount() == null) {
                    Intent intent = new Intent(this, BrowserActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
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
//        if (newCurrentAccount == null || !newCurrentAccount.hasValidToken()) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        }

        loadAvatarUrls(160);

        adapter.notifyChanged();
    }

    private void startFilesActivity() {
        Intent intent = new Intent(this, BrowserActivity.class);

        // first finish this activity, so the BrowserActivity is again "on top"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
        mAccountManager.updateCredentials(account.getAndroidAccount(), Authenticator.AUTHTOKEN_TYPE, null, this, accountCallback, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DETAIL_ACTIVITY_REQUEST:
                if (resultCode == RESULT_OK) {
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        android.view.MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.account_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
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

    private void backPressed() {
        Account account = accountManager.getCurrentAccount();
        if (account != null && !m_isFromBiometricAuth) {
            // force exit when current account was deleted
            Intent i = new Intent(this, BrowserActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } else
            finish();
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

        LoadAvatarUrlsTask task = new LoadAvatarUrlsTask(avatarSize);

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
        PolicyDialog mDialog = new PolicyDialog(AccountsActivity.this, R.style.PolicyDialog,
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
