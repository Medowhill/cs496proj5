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
import com.group1.team.autodiary.R;
import com.group1.team.autodiary.managers.MusicManager;
import com.group1.team.autodiary.managers.SelfieManager;
import com.group1.team.autodiary.managers.WeatherManager;
import com.group1.team.autodiary.objects.FacePhoto;
import com.group1.team.autodiary.objects.Music;
import com.group1.team.autodiary.objects.Place;
import com.group1.team.autodiary.objects.Weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiaryService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static String TAG;
    private static final int WEATHER_PERIOD = 60 * 60 * 1000;

    private final IBinder mBinder = new DiaryBinder();
    private GoogleApiClient mGoogleApiClient;

    private MusicManager mMusicManager;
    private SelfieManager mSelfieManager;
    private List<Place> places = new ArrayList<>();
    private List<Weather> weathers = new ArrayList<>();
    private List<Music> musics = new ArrayList<>();
    private List<String> notifications = new ArrayList<>();
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
        TAG = getString(R.string.tag);
        Log.i(TAG, "create");

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

        mMusicManager = new MusicManager(getApplicationContext());
        collectPlayingMusic();
        collectNotificationData();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "destroy");

        run = false;
        mSelfieManager.stop();
        mMusicManager.stopMusicReceiver();
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

    public void clearData() {
        places.clear();
        weathers.clear();
    }

    private void getPlace() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback((likelyPlaces) -> {
                if (likelyPlaces.getCount() > 0) {
                    String place = likelyPlaces.get(0).getPlace().getName().toString();
                    if (places.isEmpty() || !places.get(places.size() - 1).getName().equals(place)) {
                        long time = System.currentTimeMillis();
                        if (!places.isEmpty())
                            places.get(places.size() - 1).setEndTime(time);
                        places.add(new Place(time, place));
                        Log.i(TAG, "place");
                    }
                }
                likelyPlaces.release();
            });
        }
    }

    private void getWeather() {
        new WeatherManager(getApplicationContext()).getWeather(mLocation, weather -> {
            weathers.add(weather);
            Log.i(TAG, "weather");
        });
    }

    private void collectPlayingMusic() {
        mMusicManager.startMusicReceiver(music -> {
            musics.add(music);
            Log.i(TAG, "music");
        });
    }

    private void collectNotificationData() {
        new NotificationParser().collectNotificationData(getApplicationContext(), notification -> {
            notifications.add(notification.getStringExtra("notification"));
            Log.i(TAG, "notification");
        });
    }

    public Location getLocation() {
        return mLocation;
    }

    public List<Place> getPlaces() {
        if (!places.isEmpty())
            places.get(places.size() - 1).setEndTime(System.currentTimeMillis());
        Collections.sort(places);
        return places;
    }

    public List<Weather> getWeathers() {
        return weathers;
    }

    public List<Music> getMusics() {
        return musics;
    }

    public FacePhoto getPhoto() {
        return mSelfieManager.getPhoto();
    }

    public long getStart() {
        return mStart;
    }
}