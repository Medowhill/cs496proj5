package com.group1.team.autodiary.services;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.group1.team.autodiary.HttpRequest;
import com.group1.team.autodiary.ImageRecognitionRequest;
import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.Place;
import com.group1.team.autodiary.objects.Weather;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiaryService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static String TAG;
    private static final int ON_PERIOD = 10000, OFF_PERIOD = 3000;

    private final IBinder mBinder = new DiaryBinder();
    private GoogleApiClient mGoogleApiClient;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private boolean run = false;

    private List<Place> places = new ArrayList<>();
    private List<Weather> weathers = new ArrayList<>();
    private Location mLocation;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mCamera = getCamera();
            if (mCamera != null)
                mWindowManager.addView(mSurfaceView, mParams);
        }
    };

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

        startForeground(1, new Notification());
        new Thread(() -> {
            mGoogleApiClient.connect();
        }).start();

        run = true;
        preparePhoto();
        new Thread(() -> {
            while (run) {
                PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                boolean on = (Build.VERSION.SDK_INT >= 20 && manager.isInteractive()) ||
                        (Build.VERSION.SDK_INT < 20 && manager.isScreenOn());
                if (on)
                    mHandler.sendEmptyMessage(0);
                try {
                    Thread.sleep(on ? ON_PERIOD : OFF_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "destroy");

        run = false;
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

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

    public void clearData() {
        places.clear();
        weathers.clear();
    }

    private void getPlace() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback((likelyPlaces) -> {
                if (likelyPlaces.getCount() > 0)
                    places.add(new Place(System.currentTimeMillis(), likelyPlaces.get(0).getPlace().getName().toString()));
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, IOException::printStackTrace).request();
    }


    private Camera getCamera() {
        Camera camera = null;
        if (Camera.getNumberOfCameras() > 1)
            camera = Camera.open(1);
        return camera;
    }

    private void preparePhoto() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams(1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        mSurfaceView = new SurfaceView(this);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        mSurfaceView.setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);

                try {
                    mCamera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mCamera.startPreview();
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        File pictureFile = getFile();
                        if (pictureFile != null) {
                            Matrix matrix = new Matrix();
                            matrix.postRotate(270);
                            try {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                                bitmap.recycle();
                                FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
                                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                fileOutputStream.close();
                                Log.i(TAG, "saved");
                                detectFace(rotatedBitmap);
                            } catch (OutOfMemoryError | IOException e) {
                                e.printStackTrace();
                            }
                        }
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = null;
                        mWindowManager.removeView(mSurfaceView);
                    }
                });
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    private File getFile() {
        if (!Environment.getExternalStorageState().equals("mounted"))
            return null;

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!dir.exists())
            if (!dir.mkdirs())
                return null;

        return new File(dir.getPath() + File.separator + "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg");
    }

    private void detectFace(final Bitmap bitmap) {
        new Thread(() -> {
            FaceAnnotation face = ImageRecognitionRequest.getFace(new ImageRecognitionRequest(getApplicationContext()).request(bitmap, ImageRecognitionRequest.REQUEST_FACE));
            if (face != null) {
                Log.i(TAG, face.getAngerLikelihood());
                face.getJoyLikelihood();
                face.getSorrowLikelihood();
                face.getSurpriseLikelihood();
            }
        }).start();
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
}