package com.seafile.seadroid2.account.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.AsyncTask;
import android.os.Bundle;

import com.androidutillibrary.otputil.OtpView;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.cardview.widget.CardView;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
*/

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeadroidApplication;
import com.seafile.seadroid2.SeafConnection;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.account.AccountInfo;
import com.seafile.seadroid2.account.AccountManager;
import com.seafile.seadroid2.account.Authenticator;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.ssl.CertsManager;
import com.seafile.seadroid2.ui.dialog.CustomProgressDialog;
import com.seafile.seadroid2.ui.EmailAutoCompleteTextView;
import com.seafile.seadroid2.ui.activity.AccountsActivity;
import com.seafile.seadroid2.ui.activity.BaseActivity;
import com.seafile.seadroid2.ui.dialog.SslConfirmDialog;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class AccountDetailActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {
    private static final String DEBUG_TAG = "AccountDetailActivity";

    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";
    public static final String TWO_FACTOR_AUTH = "two_factor_auth";

    public static final int REQ_BIOMETRIC = 108;
    private static final int REQ_SSO = 1;

    private TextView mStatusTv;
    private Button mLoginBtn;
    private EditText mServerEt;
    private CustomProgressDialog mProgressDialog;
    private EmailAutoCompleteTextView mEmailEt;
    private EditText mPasswdEt;
    private ImageView mPasswordEyeIv;
    private CheckBox mHttpsCheckBox;
//    private TextView mSeahubUrlHintTv;    
    private ImageView mClearEmailIv, mCcearPasswordIv, mEyeClickIv;
    private RelativeLayout mEyeContainer;
    private LinearLayout mAuthTokenInputLayout;
    private OtpView mAuthTokenOv;
    private TextView mAuthTokenErrorTv;
    private Button mBiometricAuthBtn;
    private CheckBox mRemDeviceCheckBox;
    private CheckBox enterprise_checkbox;
    private Button mSingleSignOnBtn;

    private android.accounts.AccountManager mAccountManager;
    private boolean mServerEtHasFocus;
    private boolean isPasswordVisible;
    private String mSessionKey;
    private Executor m_executor;
    private BiometricPrompt m_biometricPrompt;
    private BiometricPrompt.PromptInfo m_promptInfo;
    private String defaultServerUri;

    /**
     * Called when the activity is first created.
     * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_detail);

        mAccountManager = android.accounts.AccountManager.get(getBaseContext());

        mStatusTv = (TextView) findViewById(R.id.status_view);
        mLoginBtn = (Button) findViewById(R.id.login_button);
        mServerEt = (EditText) findViewById(R.id.server_url);
        mEmailEt = (EmailAutoCompleteTextView) findViewById(R.id.email_address);
        mEmailEt.setDropDownBackgroundDrawable(getResources().getDrawable(R.drawable.shadow_share_background));
        mPasswdEt = (EditText) findViewById(R.id.password);
        mPasswordEyeIv = (ImageView) findViewById(R.id.iv_eye_click);
        mHttpsCheckBox = (CheckBox) findViewById(R.id.https_checkbox);
//        mSeahubUrlHintTv = (TextView) findViewById(R.id.seahub_url_hint);
        mClearEmailIv = (ImageView) findViewById(R.id.iv_delete_email);
        mCcearPasswordIv = (ImageView) findViewById(R.id.iv_delete_pwd);
        mEyeClickIv = (ImageView) findViewById(R.id.iv_eye_click);
        mEyeContainer = (RelativeLayout) findViewById(R.id.rl_layout_eye);
        mAuthTokenInputLayout = (LinearLayout) findViewById(R.id.auth_token_layout);
        mAuthTokenOv= (OtpView) findViewById(R.id.auth_token_text);
        mAuthTokenErrorTv = (TextView) findViewById(R.id.auth_token_error_text);
        mBiometricAuthBtn = (Button)findViewById(R.id.btnBio);
        mRemDeviceCheckBox = findViewById(R.id.remember_device);
        enterprise_checkbox = findViewById(R.id.enterprise_checkbox);
        mSingleSignOnBtn = (Button)findViewById(R.id.single_sign_on_next_btn);

        mAuthTokenInputLayout.setVisibility(View.GONE);
        mRemDeviceCheckBox.setVisibility(View.GONE);

        setupServerText();

        AccountManager accountManager = new AccountManager(AccountDetailActivity.this);
        Account curAccount = accountManager.getCurrentAccount();
        if (curAccount != null && curAccount.hasValidToken() && SettingsManager.instance().isBiometricAuthEnabled()) {
//            mBiometricAuthBtn.setVisibility(View.VISIBLE);
            mBiometricAuthBtn.setVisibility(View.GONE);
        } else {
            mBiometricAuthBtn.setVisibility(View.GONE);
        }

        Intent intent = getIntent();

        defaultServerUri = intent.getStringExtra(SeafileAuthenticatorActivity.ARG_SERVER_URI);
        boolean enterpriseLogin = intent.getExtras().getBoolean("enterprise_login", false);

        if (intent.getBooleanExtra("isEdited", false)) {
            String account_name = intent.getStringExtra(SeafileAuthenticatorActivity.ARG_ACCOUNT_NAME);
            String account_type = intent.getStringExtra(SeafileAuthenticatorActivity.ARG_ACCOUNT_TYPE);
            android.accounts.Account account = new android.accounts.Account(account_name, account_type);

            String server = mAccountManager.getUserData(account, Authenticator.KEY_SERVER_URI);
            String email = mAccountManager.getUserData(account, Authenticator.KEY_EMAIL);
            String name = mAccountManager.getUserData(account, Authenticator.KEY_NAME);
            mSessionKey = mAccountManager.getUserData(account, Authenticator.SESSION_KEY);
            // isFromEdit = mAccountManager.getUserData(account, Authenticator.KEY_EMAIL);

            if (server.startsWith(HTTPS_PREFIX))
                mHttpsCheckBox.setChecked(true);

            mServerEt.setText(server);
            mEmailEt.setText(email);
            mEmailEt.requestFocus();

            mSingleSignOnBtn.setVisibility(View.VISIBLE);
            mServerEt.setVisibility(View.VISIBLE);
            enterprise_checkbox.setVisibility(View.INVISIBLE);
            enterprise_checkbox.setChecked(true);
        } else if (defaultServerUri != null) {
            if (defaultServerUri.startsWith(HTTPS_PREFIX))
                mHttpsCheckBox.setChecked(true);

            if (enterpriseLogin) {
//                mServerEt.setText(mServerEt.getText().toString());
                mSingleSignOnBtn.setVisibility(View.VISIBLE);
                mServerEt.setVisibility(View.VISIBLE);
                enterprise_checkbox.setChecked(true);
            }
            mServerEt.setText(defaultServerUri);
        } else {
            mServerEt.setText(HTTP_PREFIX);
            int prefixLen = HTTP_PREFIX.length();
            mServerEt.setSelection(prefixLen, prefixLen);
        }
        Toolbar toolbar = getActionBarToolbar();
        toolbar.setOnMenuItemClickListener(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.login);

        LinearLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        TextView toolbarText1 = toolbarLayout.findViewById(R.id.toolbar_text_1);
        TextView toolbarText2 = toolbarLayout.findViewById(R.id.toolbar_text_2);
        CardView menuCard = toolbarLayout.findViewById(R.id.toolbar_menu_card);
        CardView cancelCard = toolbarLayout.findViewById(R.id.toolbar_cancel_card);

        toolbarText1.setText(getResources().getString(R.string.app_name));
        toolbarText2.setText(getResources().getString(R.string.account));
        menuCard.setVisibility(View.GONE);
        cancelCard.setVisibility(View.VISIBLE);
        cancelCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initListener();

        checkLoginConfig();
    }

    private void initListener() {
        mEmailEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && mEmailEt.getText().toString().trim().length() > 0) {
                    mClearEmailIv.setVisibility(View.VISIBLE);
                } else {
                    mClearEmailIv.setVisibility(View.INVISIBLE);
                }
            }
        });

        mPasswdEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && mPasswdEt.getText().toString().trim().length() > 0) {
                    mCcearPasswordIv.setVisibility(View.VISIBLE);
                } else {
                    mCcearPasswordIv.setVisibility(View.INVISIBLE);
                }
            }
        });

        mEmailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mEmailEt.getText().toString().trim().length() > 0) {
                    mClearEmailIv.setVisibility(View.VISIBLE);
                } else {
                    mClearEmailIv.setVisibility(View.INVISIBLE);
                }
                mStatusTv.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        mPasswdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mPasswdEt.getText().toString().trim().length() > 0) {
                    mCcearPasswordIv.setVisibility(View.VISIBLE);
                } else {
                    mCcearPasswordIv.setVisibility(View.INVISIBLE);
                }
                mStatusTv.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mClearEmailIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmailEt.setText("");
            }
        });

        mCcearPasswordIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswdEt.setText("");
            }
        });

        mEyeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPasswordVisible) {
                    mEyeClickIv.setImageResource(R.drawable.ic_eye_open);
                    mPasswdEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    mEyeClickIv.setImageResource(R.drawable.ic_eye_close);
                    mPasswdEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                isPasswordVisible = !isPasswordVisible;
                mPasswdEt.postInvalidate();
                String input = mPasswdEt.getText().toString().trim();
                if (!TextUtils.isEmpty(input)) {
                    mPasswdEt.setSelection(input.length());
                }
            }
        });

        mBiometricAuthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountManager accountManager = new AccountManager(AccountDetailActivity.this);
                Account account = accountManager.getCurrentAccount();
                if (account != null && account.hasValidToken()) {
                    biometricPromptDialog();
                }
            }
        });

        enterprise_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSingleSignOnBtn.setVisibility(View.VISIBLE);
                    mServerEt.setVisibility(View.VISIBLE);
                    mServerEt.setText(HTTPS_PREFIX);
                } else {
                    mSingleSignOnBtn.setVisibility(View.INVISIBLE);
                    mServerEt.setVisibility(View.INVISIBLE);
                    if(defaultServerUri != null) {
                        mServerEt.setText(defaultServerUri);
                    }
                }
            }
        });
    }

    private void checkLoginConfig() {
        List<String> loginConfig = SeadroidApplication.getInstance().getLoginConfig();
        if (loginConfig.size() == 3) {
            mServerEt.setText(loginConfig.get(0));
            mEmailEt.setText(loginConfig.get(1));
            mPasswdEt.setText(loginConfig.get(2));

            SeadroidApplication.getInstance().setLoginConfig(new ArrayList<>());

            if (!loginConfig.get(0).isEmpty() && !loginConfig.get(1).isEmpty() && !loginConfig.get(2).isEmpty()) {
                mLoginBtn.callOnClick();
            }
        }
    }

    private void setAuthTokenErrorText(String errorText) {
        mAuthTokenErrorTv.setText(errorText);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAuthTokenErrorTv.setText("");
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("email", mEmailEt.getText().toString());
        savedInstanceState.putString("password", mPasswdEt.getText().toString());
        savedInstanceState.putBoolean("rememberDevice", mRemDeviceCheckBox.isChecked());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mEmailEt.setText((String) savedInstanceState.get("email"));
        mPasswdEt.setText((String) savedInstanceState.get("password"));
        mRemDeviceCheckBox.setChecked((boolean) savedInstanceState.get("rememberDevice"));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                if (!AccountsActivity.getFromAccountsActivity()) {
                    finish();
                } else {
                    /* FYI {@link http://stackoverflow.com/questions/13293772/how-to-navigate-up-to-the-same-parent-state?rq=1} */
                    Intent upIntent = new Intent(this, AccountsActivity.class);
                    if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                        // This activity is NOT part of this app's task, so create a new task
                        // when navigating up, with a synthesized back stack.
                        TaskStackBuilder.create(this)
                                // Add all of this activity's parents to the back stack
                                .addNextIntentWithParentStack(upIntent)
                                // Navigate up to the closest parent
                                .startActivities();
                    } else {
                        // This activity is part of this app's task, so simply
                        // navigate up to the logical parent activity.
                        // NavUtils.navigateUpTo(this, upIntent);
                        upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(upIntent);
                        finish();
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onHttpsCheckboxClicked(View view) {
        refreshServerUrlPrefix();
    }

    private void refreshServerUrlPrefix() {
        boolean isHttps = mHttpsCheckBox.isChecked();
        String url = mServerEt.getText().toString();
        String prefix = isHttps ? HTTPS_PREFIX : HTTP_PREFIX;

        String urlWithoutScheme = url.replace(HTTPS_PREFIX, "").replace(HTTP_PREFIX, "");

        int oldOffset = mServerEt.getSelectionStart();

        // Change the text
        mServerEt.setText(prefix + urlWithoutScheme);

        if (mServerEtHasFocus) {
            // Change the cursor position since we changed the text
            if (isHttps) {
                int offset = oldOffset + 1;
                mServerEt.setSelection(offset, offset);
            } else {
                int offset = Math.max(0, oldOffset - 1);
                mServerEt.setSelection(offset, offset);
            }
        }
    }

    private void setupServerText() {
        mServerEt.setOnFocusChangeListener(new View.OnFocusChangeListener () {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(DEBUG_TAG, "mServerEt has focus: " + (hasFocus ? "yes" : "no"));
                mServerEtHasFocus = hasFocus;
            }
        });

        mServerEt.addTextChangedListener(new TextWatcher() {
            private String old;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mStatusTv.setText("");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                old = mServerEt.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Don't allow the user to edit the "https://" or "http://" part of the mServerEt
                String url = mServerEt.getText().toString();
                boolean isHttps = mHttpsCheckBox.isChecked();
                String prefix = isHttps ? HTTPS_PREFIX : HTTP_PREFIX;
                if (!url.startsWith(prefix)) {
                    int oldOffset = Math.max(prefix.length(), mServerEt.getSelectionStart());
                    mServerEt.setText(old);
                    mServerEt.setSelection(oldOffset, oldOffset);
                }
            }
        });
    }

    /**
     * Called when the user clicks the Login button
     * */
    public void login(View view) {
        String serverURL = mServerEt.getText().toString().trim();
        String email = mEmailEt.getText().toString().trim();
        String passwd = mPasswdEt.getText().toString();

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
//            if (!NetworkUtils.isConnected()) {
            mStatusTv.setText(R.string.network_down);
            return;
        }
        if (serverURL.length() == 0) {
            mStatusTv.setText(R.string.err_server_andress_empty);
            return;
        }
        if (email.length() == 0) {
            mEmailEt.setError(getResources().getString(R.string.err_email_empty));
            return;
        }
        if (passwd.length() == 0) {
            mPasswdEt.setError(getResources().getString(R.string.err_passwd_empty));
            return;
        }

        String authToken = null;
        if (mAuthTokenInputLayout.getVisibility() == View.VISIBLE) {
            authToken = mAuthTokenOv.getOtp();
            if (TextUtils.isEmpty(authToken)) {
                setAuthTokenErrorText(getResources().getString(R.string.two_factor_auth_token_empty));
                return;
            }
        }

        boolean rememberDevice = false;
        if (mRemDeviceCheckBox.getVisibility() == View.VISIBLE) {
            rememberDevice = mRemDeviceCheckBox.isChecked();
        }
        try {
            serverURL = Utils.cleanServerURL(serverURL);
        } catch (MalformedURLException e) {
            mStatusTv.setText(R.string.invalid_server_address);
            Log.d(DEBUG_TAG, "Invalid URL " + serverURL);
            return;
        }

        // force the keyboard to be hidden in all situations
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        mLoginBtn.setEnabled(false);
        Account tmpAccount = new Account(null, serverURL, email, null, false, mSessionKey);
        mProgressDialog = new CustomProgressDialog(this);
        // mProgressDialog.setMessage(getString(R.string.settings_cuc_loading));
        mProgressDialog.setCancelable(false);
        ConcurrentAsyncTask.execute(new LoginTask(tmpAccount, passwd, authToken, rememberDevice));
    }

    private class LoginTask extends AsyncTask<Void, Void, String> {
        Account loginAccount;
        SeafException err = null;
        String passwd;
        String authToken;
        boolean rememberDevice;

        public LoginTask(Account loginAccount, String passwd, String authToken, boolean rememberDevice) {
            this.loginAccount = loginAccount;
            this.passwd = passwd;
            this.authToken = authToken;
            this.rememberDevice = rememberDevice;
        }

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            if (params.length != 0)
                return "Error number of parameter";

            return checkLogin();
        }

        private void resend() {
            ConcurrentAsyncTask.execute(new LoginTask(loginAccount, passwd, authToken, rememberDevice));
        }

        @Override
        protected void onPostExecute(final String result) {
            mProgressDialog.dismissWithDelay();
            if (err == SeafException.sslException) {
                mAuthTokenInputLayout.setVisibility(View.GONE);
                mRemDeviceCheckBox.setVisibility(View.GONE);
                SslConfirmDialog dialog = new SslConfirmDialog(loginAccount,
                        new SslConfirmDialog.Listener() {
                            @Override
                            public void onAccepted(boolean rememberChoice) {
                                CertsManager.instance().saveCertForAccount(loginAccount, rememberChoice);
                                resend();
                            }

                            @Override
                            public void onRejected() {
                                mStatusTv.setText(result);
                                mLoginBtn.setEnabled(true);
                            }
                        });
                dialog.show(getSupportFragmentManager(), SslConfirmDialog.FRAGMENT_TAG);
                return;
            } else if (err == SeafException.twoFactorAuthTokenMissing) {
                // show auth token input box
                mAuthTokenInputLayout.setVisibility(View.VISIBLE);
                mRemDeviceCheckBox.setVisibility(View.VISIBLE);
                mRemDeviceCheckBox.setChecked(false);
                setAuthTokenErrorText(getResources().getString(R.string.two_factor_auth_error));
            } else if (err == SeafException.twoFactorAuthTokenInvalid) {
                // show auth token input box
                mAuthTokenInputLayout.setVisibility(View.VISIBLE);
                mRemDeviceCheckBox.setVisibility(View.VISIBLE);
                mRemDeviceCheckBox.setChecked(false);
                setAuthTokenErrorText(getResources().getString(R.string.two_factor_auth_invalid));
            } else {
                mAuthTokenInputLayout.setVisibility(View.GONE);
                mRemDeviceCheckBox.setVisibility(View.GONE);
            }

            if (result != null && result.equals("Success")) {
                Intent retData = new Intent();
                retData.putExtras(getIntent());
                retData.putExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME, loginAccount.getSignature());
                retData.putExtra(android.accounts.AccountManager.KEY_AUTHTOKEN, loginAccount.getToken());
                retData.putExtra(android.accounts.AccountManager.KEY_ACCOUNT_TYPE, getIntent().getStringExtra(SeafileAuthenticatorActivity.ARG_ACCOUNT_TYPE));
                retData.putExtra(SeafileAuthenticatorActivity.ARG_EMAIL, loginAccount.getEmail());
                retData.putExtra(SeafileAuthenticatorActivity.ARG_NAME, loginAccount.getName());
                retData.putExtra(SeafileAuthenticatorActivity.ARG_AUTH_SESSION_KEY, loginAccount.getSessionKey());
                retData.putExtra(SeafileAuthenticatorActivity.ARG_SERVER_URI, loginAccount.getServer());
                retData.putExtra(TWO_FACTOR_AUTH, mRemDeviceCheckBox.isChecked());
                setResult(RESULT_OK, retData);
                finish();
            } else {
                mStatusTv.setText(result);
            }
            mLoginBtn.setEnabled(true);
        }

        private String checkLogin() {
            try {
                String url = enterprise_checkbox.isChecked() ? mServerEt.getText().toString() : getString(R.string.server_url_seacloud);
                if(!url.endsWith("/")){
                    url += "/";
                }
                return doLogin(url);
            }
            catch (SeafException e) {
                err = e;
                if (e == SeafException.sslException) {
                    return getString(R.string.ssl_error);
                } else if (e == SeafException.twoFactorAuthTokenMissing) {
                    return getString(R.string.two_factor_auth_error);
                } else if (e == SeafException.twoFactorAuthTokenInvalid) {
                    return getString(R.string.two_factor_auth_invalid);
                } else {
                    // try again with 2nd server url
                    e.printStackTrace();
                    Log.e(DEBUG_TAG, getResources().getString(R.string.auto_server_url_error));
                    if (enterprise_checkbox.isChecked()) {
                        switch (e.getCode()) {
                            case HttpURLConnection.HTTP_BAD_REQUEST:
                                return getString(R.string.err_wrong_user_or_passwd);
                            case HttpURLConnection.HTTP_NOT_FOUND:
                                return getString(R.string.invalid_server_address);
                            default:
                                return e.getMessage();
                        }
                    }
                }
            }
            try {
                return doLogin(getResources().getString(R.string.server_url_sync));
            }
            catch (SeafException e) {
                err = e;
                if (e == SeafException.sslException) {
                    return getString(R.string.ssl_error);
                } else if (e == SeafException.twoFactorAuthTokenMissing) {
                    return getString(R.string.two_factor_auth_error);
                } else if (e == SeafException.twoFactorAuthTokenInvalid) {
                    return getString(R.string.two_factor_auth_invalid);
                }
                switch (e.getCode()) {
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        return getString(R.string.err_wrong_user_or_passwd);
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        return getString(R.string.invalid_server_address);
                    default:
                        return e.getMessage();
                }
            }
        }

        private String doLogin(String serverURL) throws SeafException{
            loginAccount.setServerURL(serverURL);
            SeafConnection sc = new SeafConnection(loginAccount);

            try {
                // if successful, this will place the auth token into "loginAccount"
                if (!sc.doLogin(passwd, authToken, rememberDevice))
                    return getString(R.string.err_login_failed);

                // fetch email address from the server
                DataManager manager = new DataManager(loginAccount);
                AccountInfo accountInfo = manager.getAccountInfo();

                if (accountInfo == null)
                    return "Unknown error";

                // replace email address/username given by the user with the address known by the server.
//                loginAccount = new Account(loginAccount.server, accountInfo.getEmail(), loginAccount.token, false, loginAccount.sessionKey);
                loginAccount = new Account(accountInfo.getName(), loginAccount.server, accountInfo.getEmail(), loginAccount.token, false, loginAccount.sessionKey);

                return "Success";

            }
            catch (JSONException e) {
                return e.getMessage();
            }
        }
    }

    private boolean isPossibleBiometricStrong(){
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
        }
        return false;
    }

    private void biometricPromptDialog() {
        boolean isPossibleBiometricStrong = isPossibleBiometricStrong();

        BiometricManager biometricManager = BiometricManager.from(this);
        int nCanAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        switch (nCanAuth) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
                startActivityForResult(enrollIntent, AccountDetailActivity.REQ_BIOMETRIC);
                break;
        }

        m_executor = ContextCompat.getMainExecutor(this);

        m_biometricPrompt = new BiometricPrompt(this, m_executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED
                        || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {

                } else {
                    Toast.makeText(AccountDetailActivity.this, errString, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                //Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        if (isPossibleBiometricStrong) {
            m_promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getResources().getString(R.string.biometry_login_title))
                    .setNegativeButtonText(getResources().getString(R.string.cancel))
                    .build();
        } else {
            m_promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getResources().getString(R.string.biometry_login_title))
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG|BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build();
        }

        m_biometricPrompt.authenticate(m_promptInfo);
    }

    public void ssoLogin(View view) {
        Intent intent = new Intent(this, SingleSignOnActivity.class);
        intent.putExtras(getIntent());
        startActivityForResult(intent, REQ_SSO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SSO && resultCode == RESULT_OK) {
            setResult(resultCode, data);
            finish();
        }
    }
}
