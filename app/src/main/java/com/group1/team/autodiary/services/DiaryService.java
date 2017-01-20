package com.group1.team.autodiary.services;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.group1.team.autodiary.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DiaryService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static String TAG;

    private final IBinder mBinder = new DiaryBinder();
    private GoogleApiClient mGoogleApiClient;
    private ContentResolver mContentResolver;
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);
    private Location mLocation;

    public class DiaryBinder extends Binder {
        public DiaryService getService() {
            return DiaryService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "create");

        TAG = getString(R.string.tag);
        mContentResolver = getContentResolver();

        if (mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addConnectionCallbacks(this).addOnConnectionFailedListener(this).
                    addApi(LocationServices.API).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "start");

        startForeground(1, new Notification());
        new Thread(() -> {
            mGoogleApiClient.connect();
        }).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "destroy");

        mGoogleApiClient.disconnect();
        stopForeground(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, new LocationRequest(), this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        getPlace();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "bind");

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "unbind");

        return false;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "rebind");
    }

    private void getPlace() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback((likelyPlaces) -> {
                if (likelyPlaces.getCount() > 0)
                    likelyPlaces.get(0).getPlace().getName();
                likelyPlaces.release();
            });
        }
    }
}