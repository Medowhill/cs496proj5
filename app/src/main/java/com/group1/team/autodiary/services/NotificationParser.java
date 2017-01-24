package com.group1.team.autodiary.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.group1.team.autodiary.objects.AssetInfo;

/**
 * Created by q on 2017-01-21.
 */

@TargetApi(Build.VERSION_CODES.KITKAT)
public class NotificationParser extends NotificationListenerService {
    private static final String TAG = "NotificationParser";
    public static final String GET_NOTIFICATION = "com.daily_reviewer.q.dailyreviewer.NotificationParser.getnotification";

    public interface Callback {
        void callback(String[] parsedText);
    }

    public void collectAssetInfo(Context context, Callback callback) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GET_NOTIFICATION);

        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("DiaryService", "Get Notification!");
                Bundle extras = intent.getBundleExtra("notification");

                String[] parsedText = extras.getString(Notification.EXTRA_TEXT).split(" ");

                callback.callback(parsedText);
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
            Bundle extras = mNotification.extras;
            Log.d(TAG, extras.toString());

            Intent intent = new Intent(GET_NOTIFICATION);
            intent.putExtra("notification", extras);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {

    }
}
