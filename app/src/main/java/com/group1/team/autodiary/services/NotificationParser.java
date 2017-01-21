package com.group1.team.autodiary.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
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
