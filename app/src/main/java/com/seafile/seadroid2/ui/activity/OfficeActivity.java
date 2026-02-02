package com.seafile.seadroid2.ui.activity;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.SeafRepo;
import com.seafile.seadroid2.data.SelectedFileInfo;
import com.seafile.seadroid2.ui.dialog.CustomProgressDialog;
import com.seafile.seadroid2.util.ConcurrentAsyncTask;
import com.seafile.seadroid2.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class OfficeActivity extends BaseActivity {

    Account account;
    DataManager dataManager;
    SelectedFileInfo fileInfo;
    CustomProgressDialog progressDialog;
    private WebView officeWV;
    String loginUrl;
    String firstUrl;
    String secondUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_office);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        });

        Intent intent = getIntent();

        account = intent.getParcelableExtra("account");
        String info = intent.getStringExtra("selectedFileInfo");
        JSONObject object = Utils.parseJsonObject(info);
        if (object == null)
            finish();

        try {
            fileInfo = SelectedFileInfo.fromJson(object);
        } catch (JSONException e) {
            finish();
        }

        if (account == null || fileInfo == null)
            finish();

        dataManager = new DataManager(account);
        progressDialog = new CustomProgressDialog(this);

        Toolbar toolbar = getActionBarToolbar();
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(Utils.fileNameFromPath(fileInfo.fileName));
        }

        ConcurrentAsyncTask.execute(new GetToken());
    }

    private void backPressed() {
        if (officeWV != null && officeWV.canGoBack()) {
            officeWV.goBack();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null)
            progressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetToken extends AsyncTask<Void, Void, String> {
        private Exception err;

        public GetToken() {
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return dataManager.getToken();
            } catch (SeafException e) {
                err = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result == null) return;

            setOfficeWV(result);
        }
    }

    private void setOfficeWV(String token) {
        loginUrl = String.format(account.server + "client-login?token=" + token);
        firstUrl = String.format(account.server + "lib/" + fileInfo.repoID + "/file" + Utils.removeLastPathSeperator(fileInfo.dirPath) + "/" + fileInfo.fileName);
        secondUrl = "";
        SeafRepo repo = dataManager.getCachedRepoByID(fileInfo.repoID);
        if (repo != null && repo.encrypted) {
            secondUrl = firstUrl;
            firstUrl = String.format(account.server + "library/" + fileInfo.repoID + "/" + fileInfo.repoName);
        }

        officeWV = findViewById(R.id.office_wv);

        officeWV.clearCache(true);
        officeWV.clearHistory();
        officeWV.clearFormData();
        WebStorage.getInstance().deleteAllData();

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(officeWV, true);

        cookieManager.removeAllCookies(null);
        cookieManager.flush();

//        officeWV.getSettings().setUserAgentString(
//                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
//                "AppleWebKit/537.36 (KHTML, like Gecko) " +
//                "Chrome/119.0.0.0 Safari/537.36"
//        );
        officeWV.getSettings().setJavaScriptEnabled(true);
        officeWV.getSettings().setDomStorageEnabled(true);

        officeWV.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (Utils.removeLastPathSeperator(url).equals(Utils.removeLastPathSeperator(firstUrl)) && !"".equals(secondUrl)) {
                    officeWV.loadUrl(secondUrl);
                }
            }
        });
        officeWV.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription(getString(R.string.downloading));
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        URLUtil.guessFileName(url, contentDisposition, mimetype));

                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);

                Toast.makeText(getApplicationContext(), getString(R.string.downloading), Toast.LENGTH_SHORT).show();
            }
        });

        officeWV.loadUrl(String.format(loginUrl + "&next=" + firstUrl));
    }
}