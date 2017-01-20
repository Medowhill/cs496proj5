package com.group1.team.autodiary.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.services.DiaryService;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY = 0;
    private static final int PERMISSION = 0;

    private DiaryService mService;
    private boolean mRun = false; // whether service is running (user is awake)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent serviceIntent = new Intent(getApplicationContext(), DiaryService.class);
        final ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = ((DiaryService.DiaryBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };

        Button button = (Button) findViewById(R.id.main_button);
        button.setOnClickListener(v -> {
            if (mRun) { // when user goes to bed
                // TODO: get data from service instance and start DiaryActivity
                unbindService(null);
                stopService(serviceIntent);
                mService = null;
                mRun = false;
                button.setText(R.string.main_button_start);
            } else { // when user wakes up
                startService(serviceIntent);
                bindService(serviceIntent, connection, BIND_AUTO_CREATE);

                mRun = true;
                button.setText(R.string.main_button_finish);
            }
        });

        mRun = isServiceRunning(DiaryService.class);
        if (mRun) {
            bindService(serviceIntent, connection, BIND_AUTO_CREATE);
            button.setText(R.string.main_button_finish);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY);
            }
            // if need more permissions, then plz add here
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_OVERLAY:
                if (resultCode != RESULT_OK)
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

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (serviceClass.getName().equals(service.service.getClassName()))
                return true;
        return false;
    }
}
