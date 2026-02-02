package com.seafile.seadroid2.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.seafile.seadroid2.R;
import com.seafile.seadroid2.SettingsManager;
import com.seafile.seadroid2.account.ui.AccountDetailActivity;
import com.seafile.seadroid2.gesturelock.LockPatternUtils;
import com.seafile.seadroid2.gesturelock.LockPatternView;

import java.util.List;
import java.util.concurrent.Executor;

public class UnlockActivity extends BaseActivity {
    private static final String DEBUG_TAG = "BiometricAuthActivity";

    private LockPatternView mLockPatternView;
    private int mFailedPatternAttemptsSinceLastTimeout = 0;
    private TextView mHeadTextView;
    private Animation mShakeAnim;
    private CountDownTimer mCountdownTimer = null;
    private Handler mHandler = new Handler();

    private Executor m_executor;
    private BiometricPrompt m_biometricPrompt;
    private BiometricPrompt.PromptInfo m_promptInfo;

    SettingsManager settingsMgr;

    public static int LOCK_NONE = 0;
    public static int LOCK_GESTURE = 1;
    public static int LOCK_BIOMETRIC = 2;
    public static int LOCK_BOTH = 3;

    private int m_nLockKind = LOCK_NONE;


    public static boolean g_BACK_PRESSED = false;
    public static boolean g_SCREEN_OFF = false;
    public static boolean g_HOME_PRESSED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        setContentView(R.layout.gesturepassword_unlock);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed();
            }
        });

        settingsMgr = SettingsManager.instance();

        if (settingsMgr.isGestureLockEnabled()){
            findViewById(R.id.gesturepwd_root).setBackgroundColor(getResources().getColor(R.color.gesture_background));
            if (settingsMgr.isBiometricAuthEnabled()){
                m_nLockKind = LOCK_BOTH;
            } else {
                m_nLockKind = LOCK_GESTURE;
            }

            mLockPatternView = (LockPatternView) this
                    .findViewById(R.id.gesturepwd_unlock_lockview);
            mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
            mLockPatternView.setTactileFeedbackEnabled(true);
            mHeadTextView = (TextView) findViewById(R.id.gesturepwd_unlock_text);
            mShakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_x);

            settingsMgr = SettingsManager.instance();


        } else {
            if (settingsMgr.isBiometricAuthEnabled()){
                findViewById(R.id.gesturepwd_root).setVisibility(View.GONE);
                m_nLockKind = LOCK_BIOMETRIC;
                biometricPromptDialog();
            } else {
                m_nLockKind = LOCK_NONE;
                finish();
                return;
            }
        }
    }


    protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {

        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            patternInProgress();
        }

        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            if (pattern == null)
                return;
            LockPatternUtils mLockPatternUtils = new LockPatternUtils(getApplicationContext());
            if (mLockPatternUtils.checkPattern(pattern)) {
                mLockPatternView
                        .setDisplayMode(LockPatternView.DisplayMode.Correct);
                settingsMgr.setupGestureLock();
                if (m_nLockKind == LOCK_BOTH){
                    findViewById(R.id.gesturepwd_root).setVisibility(View.GONE);
                    biometricPromptDialog();
                } else {
                    finish();
                }
            } else {
                mLockPatternView
                        .setDisplayMode(LockPatternView.DisplayMode.Wrong);
                if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
                    mFailedPatternAttemptsSinceLastTimeout++;
                    int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT
                            - mFailedPatternAttemptsSinceLastTimeout;
                    if (retry >= 0) {
                        if (retry == 0)
                            Toast.makeText(UnlockActivity.this, getResources().getString(R.string.lockscreen_access_pattern_failure), Toast.LENGTH_SHORT).show();
                        mHeadTextView.setText(getResources().getQuantityString(R.plurals.lockscreen_access_pattern_failure_left_try_times, retry, retry));
                        mHeadTextView.setTextColor(Color.RED);
                        mHeadTextView.startAnimation(mShakeAnim);
                    }

                } else {
                    Toast.makeText(UnlockActivity.this, getResources().getString(R.string.lockscreen_access_pattern_failure_not_long_enough), Toast.LENGTH_SHORT).show();
                }

                if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
                    mHandler.postDelayed(attemptLockout, 2000);
                } else {
                    mLockPatternView.postDelayed(mClearPatternRunnable, 2000);
                }
            }
        }

        public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

        }

        private void patternInProgress() {
        }
    };


    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountdownTimer != null)
            mCountdownTimer.cancel();
    }

    private void backPressed() {
        Intent i = new Intent(this, BrowserActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    Runnable attemptLockout = new Runnable() {

        @Override
        public void run() {
            mLockPatternView.clearPattern();
            mLockPatternView.setEnabled(false);
            mCountdownTimer = new CountDownTimer(
                    LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS + 1, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    int secondsRemaining = (int) (millisUntilFinished / 1000) - 1;
                    if (secondsRemaining > 0) {
                        mHeadTextView.setText(getResources().getQuantityString(R.plurals.lockscreen_access_pattern_failure_left_try_seconds, secondsRemaining, secondsRemaining));
                    } else {
                        mHeadTextView.setText(R.string.lockscreen_access_pattern_hint);
                        mHeadTextView.setTextColor(getResources().getColor(R.color.gesture_foreground));
                    }

                }

                @Override
                public void onFinish() {
                    mLockPatternView.setEnabled(true);
                    mFailedPatternAttemptsSinceLastTimeout = 0;
                }
            }.start();
        }
    };

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
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
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
                settingsMgr.savePreviousGestureLockTimeStamp();

                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED
                        || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {

                } else {
                    Toast.makeText(UnlockActivity.this, errString, Toast.LENGTH_SHORT).show();
                }
                finish();

//                Intent newIntent = new Intent(UnlockActivity.this, AccountsActivity.class);
//                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                newIntent.putExtra("FROM_BIOMETRIC_AUTH", true);
//                startActivity(newIntent);
//                finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                settingsMgr.saveGestureLockTimeStamp();
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

            }


        });

        if (isPossibleBiometricStrong) {
            m_promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getResources().getString(R.string.biometry_login_title))
                    //.setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText(getResources().getString(R.string.cancel))
                    //.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build();
        } else {
            m_promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getResources().getString(R.string.biometry_login_title))
                    //.setSubtitle("Log in using your biometric credential")
                    //.setNegativeButtonText(getResources().getString(R.string.cancel))

                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG|BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build();
        }
        m_biometricPrompt.authenticate(m_promptInfo);
    }
}
