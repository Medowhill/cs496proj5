package com.group1.team.autodiary.services;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
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
import com.group1.team.autodiary.HttpRequest;
import com.group1.team.autodiary.R;
import com.group1.team.autodiary.managers.SelfieManager;
import com.group1.team.autodiary.objects.Photo;
import com.group1.team.autodiary.objects.Place;
import com.group1.team.autodiary.objects.Weather;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiaryService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static String TAG;
    private static final int WEATHER_PERIOD = 60 * 60 * 1000;

    private final IBinder mBinder = new DiaryBinder();
    private GoogleApiClient mGoogleApiClient;

    private SelfieManager mSelfieManager;
    private List<Place> places = new ArrayList<>();
    private List<Weather> weathers = new ArrayList<>();
    private Location mLocation;
    private long mStart;
    private boolean run = false;

    public class DiaryBinder extends Binder {
        public DiaryService getService() {
            return DiaryService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "create");

        TAG = getString(R.string.tag);

        if (mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addConnectionCallbacks(this).addOnConnectionFailedListener(this).
                    addApi(LocationServices.API).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "start");

        mStart = System.currentTimeMillis();
        startForeground(1, new Notification());
        new Thread(() -> {
            mGoogleApiClient.connect();
        }).start();

        run = true;
        new Thread(() -> {
            while (run) {
                getWeather();
                try {
                    Thread.sleep(WEATHER_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        mSelfieManager = new SelfieManager(getApplicationContext());
        mSelfieManager.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "destroy");

        run = false;
        mSelfieManager.stop();
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
        Log.i(TAG, "location");
        mLocation = location;
        getWeather();
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

    public void clearData() {
        places.clear();
        weathers.clear();
    }

    private void getPlace() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback((likelyPlaces) -> {
                if (likelyPlaces.getCount() > 0) {
                    places.add(new Place(System.currentTimeMillis(), likelyPlaces.get(0).getPlace().getName().toString()));
                    Log.i(TAG, "place");
                }
                likelyPlaces.release();
            });
        }
    }

    private void getWeather() {
        if (mLocation != null)
            new HttpRequest(getString(R.string.weatherUrl) + "weather?lat=" + mLocation.getLatitude()
                    + "&lon=" + mLocation.getLongitude() + "&APPID=" + getString(R.string.weatherKey),
                    in -> {
                        try {
                            weathers.add(new Weather(new JSONObject(new String(in))));
                            Log.i(TAG, "weather");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, IOException::printStackTrace).request();
    }

    public Location getLocation() {
        return mLocation;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public List<Weather> getWeathers() {
        return weathers;
    }

    public Photo[] getPhotos() {
        return mSelfieManager.getPhotos();
    }

    public long getStart() {
        return mStart;
    }
}