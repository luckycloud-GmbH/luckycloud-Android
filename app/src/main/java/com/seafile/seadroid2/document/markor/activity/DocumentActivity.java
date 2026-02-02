/*#######################################################
 *
 *   Maintained 2017-2025 by Gregor Santner <gsantner AT mailbox DOT org>
 *   License of this file: Apache 2.0
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package com.seafile.seadroid2.document.markor.activity;

import static com.seafile.seadroid2.ui.activity.BrowserActivity.DOCUMENT_REQUEST;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.document.markor.format.FormatRegistry;
import com.seafile.seadroid2.document.markor.frontend.textview.TextViewUtils;
import com.seafile.seadroid2.document.markor.model.AppSettings;
import com.seafile.seadroid2.document.markor.model.Document;
import com.seafile.seadroid2.document.markor.util.MarkorContextUtils;
import com.seafile.seadroid2.document.opoc.format.GsTextUtils;
import com.seafile.seadroid2.document.opoc.frontend.base.GsFragmentBase;
import com.seafile.seadroid2.document.opoc.util.GsContextUtils;

import java.io.File;

public class DocumentActivity extends MarkorBaseActivity  implements Toolbar.OnMenuItemClickListener {

    public Toolbar toolbar;
    private FragmentManager _fragManager;

    public static void launch(
            final Activity activity,
            final File file,
            final Boolean doPreview,
            final Integer lineNumber
    ) {
        launch(activity, file, doPreview, lineNumber, false);
    }

    private static void launch(
            final Activity activity,
            final File file,
            final Boolean doPreview,
            final Integer lineNumber,
            final boolean forceOpenInThisApp
    ) {
        if (activity == null || file == null) {
            return;
        }

        if (file.isFile() && !FormatRegistry.isFileSupported(file)) {
            return;
        }

        final Intent intent = new Intent(activity, DocumentActivity.class);

        if (lineNumber != null) {
            intent.putExtra(Document.EXTRA_FILE_LINE_NUMBER, lineNumber);
        }

        if (doPreview != null) {
            intent.putExtra(Document.EXTRA_DO_PREVIEW, doPreview);
        }

        intent.putExtra(Document.EXTRA_FILE, file);

        GsContextUtils.instance.animateToActivity(activity, intent, false, DOCUMENT_REQUEST);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppSettings.clearDebugLog();
        setContentView(R.layout.document__activity);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        });

        setSupportActionBar(findViewById(R.id.toolbar));
        _fragManager = getSupportFragmentManager();

        toolbar = getActionBarToolbar();
        toolbar.setOnMenuItemClickListener(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Drawable overflowIcon = toolbar.getOverflowIcon();
        if (overflowIcon != null) {
            overflowIcon.setColorFilter(ContextCompat.getColor(this, R.color.text_view_color), PorterDuff.Mode.SRC_IN);
        }

        handleLaunchingIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleLaunchingIntent(intent);
    }

    private void handleLaunchingIntent(final Intent intent) {
        if (intent == null) return;

        final String intentAction = intent.getAction();
        final Uri intentData = intent.getData();

        // Pull the file from the intent
        // -----------------------------------------------------------------------
        final File file = MarkorContextUtils.getIntentFile(intent, this);

        // Decide what to do with the file
        // -----------------------------------------------------------------------
        if (file == null || !_cu.canWriteFile(this, file, false, true)) {
            finish();
        } else {
            // Open in editor/viewer
            final Document doc = new Document(file);
            Integer startLine = null;
            if (intent.hasExtra(Document.EXTRA_FILE_LINE_NUMBER)) {
                startLine = intent.getIntExtra(Document.EXTRA_FILE_LINE_NUMBER, -1);
            } else if (intentData != null) {
                final String line = intentData.getQueryParameter("line");
                startLine = GsTextUtils.tryParseInt(line, -1);
            }

            // Start in a specific mode if required. Otherwise let the fragment decide
            Boolean startInPreview = null;
            if (startLine != null) {
                // If a line is requested, open in edit mode so the line is shown
                startInPreview = false;
            } else if (intent.getBooleanExtra(Document.EXTRA_DO_PREVIEW, false) || file.getName().startsWith("index.")) {
                startInPreview = true;
            }

            // Three cases
            // 1. We have an editor open and it is the same document - show the requested line
            // 2. We have an editor open and it is a different document - open the new document
            // 3. We do not have a current fragment - open the document here
            final GsFragmentBase<?, ?> frag = getCurrentVisibleFragment();
            if (frag != null) {
                if (frag instanceof DocumentEditAndViewFragment) {
                    final DocumentEditAndViewFragment editFrag = (DocumentEditAndViewFragment) frag;
                    if (editFrag.getDocument().path.equals(doc.path)) {
                        if (startLine != null) {
                            // Same document requested, show the requested line
                            TextViewUtils.selectLines(editFrag.getEditor(), startLine);
                        }
                    } else {
                        // Current document is different - launch the new document
                        launch(this, file, startInPreview, startLine);
                    }
                } else {
                    // Current fragment is not an editor - launch the new document
                    launch(this, file, startInPreview, startLine);
                }
            } else {
                // No fragment open - open the document
                showFragment(DocumentEditAndViewFragment.newInstance(doc, startLine, startInPreview));
            }
        }

        setTitle(file.getName());
    }

    private final RectF point = new RectF(0, 0, 0, 0);
    private static final int SWIPE_MIN_DX = 150;
    private static final int SWIPE_MAX_DY = 90;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        try {
            return super.dispatchTouchEvent(event);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Error in super.dispatchTouchEvent: " + e);
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _cu.extractResultFromActivityResult(this, requestCode, resultCode, data);
    }

    public void setTitle(final CharSequence title) {
        final ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(title);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        _cu.setKeepScreenOn(this, true);
    }

    private void backPressed() {
        final int entryCount = _fragManager.getBackStackEntryCount();
        final GsFragmentBase<?, ?> top = getCurrentVisibleFragment();

        // We pop the stack to go back to the previous fragment
        // if the top fragment does not handle the back press
        // Doesn't actually get called as we have 1 fragment in the stack
        if (top != null && !top.onBackPressed() && entryCount > 1) {
            _fragManager.popBackStack();
            return;
        }

        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onReceiveKeyPress(getCurrentVisibleFragment(), keyCode, event) || super.onKeyDown(keyCode, event);
    }

    public GsFragmentBase<?, ?> showFragment(GsFragmentBase<?, ?> fragment) {
        if (fragment != getCurrentVisibleFragment()) {
            _fragManager.beginTransaction()
                    .replace(R.id.document__placeholder_fragment, fragment, fragment.getFragmentTag())
                    .commit();

            supportInvalidateOptionsMenu();
        }
        return fragment;
    }

    public synchronized GsFragmentBase<?, ?> getExistingFragment(final String fragmentTag) {
        return (GsFragmentBase<?, ?>) getSupportFragmentManager().findFragmentByTag(fragmentTag);
    }

    private GsFragmentBase<?, ?> getCurrentVisibleFragment() {
        return (GsFragmentBase<?, ?>) getSupportFragmentManager().findFragmentById(R.id.document__placeholder_fragment);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}