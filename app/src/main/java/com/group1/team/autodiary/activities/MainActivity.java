package com.group1.team.autodiary.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.services.DiaryService;

import java.sql.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY = 0;
    private static final int PERMISSION = 0;
    private static final int REQUEST_PACKAGE_USAGE_STATS = 1;
    private static final int REQUEST_NOTIFICATION_LISTENER_ENABLE = 2;

    public static long dayStartTime;
    public static long dayEndTime;

    private Button button;

    private boolean mRun = false; // whether service is running (user is awake)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.main_button);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mRun) { // when user goes to bed
                            dayEndTime = System.currentTimeMillis();
                            Log.d("check", "dayEndTime : " + new Date(dayEndTime).toString());
                            startActivity(new Intent(getApplicationContext(), DiaryActivity.class));
                            mRun = false;
                            button.setText(R.string.main_button_start);
                        } else { // when user wakes up
                            startService(new Intent(getApplicationContext(), DiaryService.class));
                            mRun = true;
                            button.setText(R.string.main_button_finish);
                            dayStartTime = System.currentTimeMillis();
                            Log.d("check", "dayStartTime : " + new Date(dayStartTime).toString());
                        }
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY);
            }

            if (!hasPackageUsageStatsPermission()) {
                startActivityForResult(
                        new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                        REQUEST_PACKAGE_USAGE_STATS);
            }

            if (!hasNotificationListenerPermission()) {
                startActivityForResult(
                        new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"),
                        REQUEST_NOTIFICATION_LISTENER_ENABLE);
            }

            // if need more permissions, then plz add here
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.INTERNET
            }, PERMISSION);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mRun = isServiceRunning(DiaryService.class);
        if (mRun) button.setText(R.string.main_button_finish);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_OVERLAY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext()))
                    finish();
                break;
            case REQUEST_PACKAGE_USAGE_STATS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasPackageUsageStatsPermission())
                    finish();
                break;
            case REQUEST_NOTIFICATION_LISTENER_ENABLE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasNotificationListenerPermission())
                    finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION:
                for (int i = 0; i < grantResults.length; i++)
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        finish();
                break;
        }
    }

    @TargetApi(19)
    private boolean hasPackageUsageStatsPermission() {
        if (Build.VERSION.SDK_INT < 19) return false;

        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private boolean hasNotificationListenerPermission() {
        String enabledListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return !TextUtils.isEmpty(enabledListeners)             // check if enableListeners is empty
                && enabledListeners.contains(getPackageName()); // check if enableListeners contains this app
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (serviceClass.getName().equals(service.service.getClassName()))
                return true;
        return false;
    }

    public void sendNotification(View view) {
        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this);
        ncomp.setContentTitle("My Notification");
        ncomp.setContentText("Notification Listener Service Example");
        ncomp.setTicker("Notification Listener Service Example");
        ncomp.setSmallIcon(R.drawable.ic_launcher);
        ncomp.setAutoCancel(true);
        nManager.notify((int)System.currentTimeMillis(),ncomp.build());
    }
}
