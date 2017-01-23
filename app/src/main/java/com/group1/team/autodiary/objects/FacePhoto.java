package com.group1.team.autodiary.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class FacePhoto {

    private long mTime;
    private int mJoy;

    public static int faceToInt(String str) {
        Log.i("cs496test", str);
        if (str.equals("VERY_UNLIKELY"))
            return 0;
        else if (str.equals("UNLIKELY"))
            return 1;
        else if (str.equals("LIKELY"))
            return 2;
        else if (str.equals("VERY_LIKELY"))
            return 3;
        return -1;
    }

    public FacePhoto(long time, int joy) {
        this.mTime = time;
        this.mJoy = joy;
    }

    public long getTime() {
        return mTime;
    }

    public int getJoy() {
        return mJoy;
    }

    public String getFileName() {
        return mTime + ".jpg";
    }

    public Bitmap getBitmap(Context context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.openFileInput(getFileName()), null, options);
        } catch (IOException | OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void deleteFile(Context context) {
        File file = new File(context.getFilesDir().getPath() + File.separator + getFileName());
        if (file.exists())
           file.delete();
    }
}
