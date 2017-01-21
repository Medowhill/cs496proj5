package com.group1.team.autodiary.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.group1.team.autodiary.utils.ImageRecognitionRequest;
import com.group1.team.autodiary.objects.Photo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SelfieManager {

    private static String TAG;
    private static final int ON_PERIOD = 30 * 60 * 1000, OFF_PERIOD = 3 * 60 * 1000;

    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private Photo[] mPhotos = new Photo[4];
    private boolean mRun = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mCamera = getCamera();
            if (mCamera != null)
                mWindowManager.addView(mSurfaceView, mParams);
        }
    };

    public SelfieManager(Context context) {
        mContext = context;
    }

    public void start() {
        mRun = true;
        preparePhoto();
        new Thread(() -> {
            while (mRun) {
                PowerManager manager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
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
    }

    public void stop() {
        mRun = false;
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera getCamera() {
        Camera camera = null;
        if (Camera.getNumberOfCameras() > 1)
            camera = Camera.open(1);
        return camera;
    }

    private void preparePhoto() {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams(1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        mSurfaceView = new SurfaceView(mContext);
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
                        Matrix matrix = new Matrix();
                        matrix.postRotate(270);
                        try {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            bitmap.recycle();
                            detectFace(rotatedBitmap);
                        } catch (OutOfMemoryError e) {
                            e.printStackTrace();
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

    private File getFile(long time) {
        if (!Environment.getExternalStorageState().equals("mounted"))
            return null;

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AutoDiary");
        if (!dir.exists())
            if (!dir.mkdirs())
                return null;

        return new File(dir.getPath() + File.separator + time + ".jpg");
    }

    private void detectFace(final Bitmap bitmap) {
        new Thread(() -> {
            FaceAnnotation face = ImageRecognitionRequest.getFace(new ImageRecognitionRequest(mContext).request(bitmap, ImageRecognitionRequest.REQUEST_FACE));
            if (face != null) {
                int anger = Photo.faceToInt(face.getAngerLikelihood());
                int joy = Photo.faceToInt(face.getJoyLikelihood());
                int sorrow = Photo.faceToInt(face.getSorrowLikelihood());
                int surprised = Photo.faceToInt(face.getSurpriseLikelihood());
                Photo photo = new Photo(System.currentTimeMillis(), anger, joy, sorrow, surprised);

                boolean save = false;
                for (int i = 0; i < mPhotos.length; i++) {
                    if (mPhotos[i] == null || photo.getFeeling(i) > mPhotos[i].getFeeling(i)) {
                        mPhotos[i] = photo;
                        save = true;
                        break;
                    }
                }
                if (save) {
                    File pictureFile = getFile(photo.getTime());
                    if (pictureFile != null) {
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                            bitmap.recycle();
                            fileOutputStream.close();
                            Log.i(TAG, "photo");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public Photo[] getPhotos() {
        return mPhotos;
    }
}
