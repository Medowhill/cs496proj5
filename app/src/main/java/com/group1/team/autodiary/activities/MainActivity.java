package com.group1.team.autodiary.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.services.DiaryService;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY = 0;
    private static final int PERMISSION = 0;
    private static final int REQUEST_PACKAGE_USAGE_STATS = 1;
    private static final int REQUEST_NOTIFICATION_LISTENER_ENABLE = 2;

    private Button button;

    private TextToSpeech speech;
    private boolean mRun = false; // whether service is running (user is awake)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 19)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        FrameLayout layout = (FrameLayout) findViewById(R.id.main_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                overridePendingTransition(R.anim.from_left, R.anim.to_right);
            }
        });

        Typeface typeface = Typeface.createFromAsset(getAssets(), "NanumMyeongjo.ttf");
        button = (Button) findViewById(R.id.main_button);
        button.setTypeface(typeface);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mRun) { // when user goes to bed
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage(R.string.main_dialog_message);
                            builder.setPositiveButton(R.string.main_dialog_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(getApplicationContext(), DiaryActivity.class));
                                    overridePendingTransition(R.anim.from_right, R.anim.to_left);
                                    mRun = false;
                                }
                            });
                            builder.setNegativeButton(R.string.main_dialog_negative, null);

                            AlertDialog dialog = builder.show();
                            ((TextView) dialog.findViewById(android.R.id.message)).setTypeface(typeface);
                            ((Button) dialog.findViewById(android.R.id.button1)).setTypeface(typeface);
                            ((Button) dialog.findViewById(android.R.id.button2)).setTypeface(typeface);
                        } else { // when user wakes up
                            speech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int status) {
                                    speech.speak(getString(R.string.main_speech), TextToSpeech.QUEUE_FLUSH, null);
                                }
                            });
                            startService(new Intent(getApplicationContext(), DiaryService.class));
                            mRun = true;
                            button.setText(R.string.main_button_finish);
                        }
                    }
                });

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

        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY);
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

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Log.d("MusicManager", "Music On? : " + audioManager.isMusicActive());
    }

    @Override
    protected void onStart() {
        super.onStart();

        mRun = isServiceRunning(DiaryService.class);
        if (mRun) button.setText(R.string.main_button_finish);
        else button.setText(R.string.main_button_start);
    }

    @Override
    protected void onStop() {
        if (speech != null) {
            speech.stop();
            speech.shutdown();
        }

        super.onStop();
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

    private boolean hasPackageUsageStatsPermission() {
        if (Build.VERSION.SDK_INT < 19) return true;

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
}
