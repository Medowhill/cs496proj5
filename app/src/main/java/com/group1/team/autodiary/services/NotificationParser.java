package com.group1.team.autodiary.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by q on 2017-01-21.
 */

@TargetApi(Build.VERSION_CODES.KITKAT)
public class NotificationParser extends NotificationListenerService {
    private static final String TAG = "NotificationParser";
    public static final String GET_NOTIFICATION = "com.daily_reviewer.q.dailyreviewer.NotificationParser.getnotification";

    public interface Callback {
        void callback(Intent intent);
    }

    public void collectNotificationData(Context context, Callback callback) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GET_NOTIFICATION);

        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO : do something
                Log.d("DiaryService", "Get Notification!");
                callback.callback(intent);
            }
        };

        context.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        if (statusBarNotification == null) return;

        Notification mNotification = statusBarNotification.getNotification();
        Log.e(TAG, mNotification.toString());

        if (statusBarNotification.getPackageName().equalsIgnoreCase("com.wr.alrim")) {
            String extra = mNotification.extras.toString();
            Log.d(TAG, extra);

            Intent intent = new Intent(GET_NOTIFICATION);
            intent.putExtra("notification", extra);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {

    }
}
