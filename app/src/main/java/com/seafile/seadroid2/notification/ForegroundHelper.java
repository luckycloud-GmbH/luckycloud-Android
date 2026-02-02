package com.seafile.seadroid2.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class ForegroundHelper {
    private static final String TAG = "ForegroundHelper";

    public static void safeStartForeground(Service svc, int id, Notification notification, int foregroundServiceType) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                svc.startForeground(id, notification, foregroundServiceType);
            } else {
                svc.startForeground(id, notification);
            }
            Log.i(TAG, "startForeground(): success");
        } catch (RuntimeException re) {
            Log.e(TAG, "startForeground() runtime exception", re);
            fallbackNotifyImmediately(svc, id, notification);
        }
    }

    private static void fallbackNotifyImmediately(Service svc, int id, Notification notification) {
        NotificationManager nm = (NotificationManager) svc.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(id, notification);
            Log.i(TAG, "NotificationManager.notify() used as fallback");
        } else {
            Log.w(TAG, "NotificationManager null, cannot show notification fallback");
        }
    }
}
