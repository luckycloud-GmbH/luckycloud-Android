package com.seafile.seadroid2;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.seafile.seadroid2.util.Utils;

public class LogService extends Service {

    private Handler handler;
    private Runnable runnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Utils.utilsLogInfo(true, "luckycloud is running");
                handler.postDelayed(this, 10000);
            }
        };

        handler.post(runnable);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
