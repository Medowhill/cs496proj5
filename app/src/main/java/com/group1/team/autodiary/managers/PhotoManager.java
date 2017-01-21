package com.group1.team.autodiary.managers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;

import com.google.api.services.vision.v1.model.Property;
import com.group1.team.autodiary.utils.ImageRecognitionRequest;

import java.util.ArrayList;
import java.util.List;

public class PhotoManager {

    private final static String PATH = Environment.getExternalStorageDirectory().getPath() + "DCIM/Camera";

    public interface Callback {
        void callback(Bitmap bitmap, long date, List<Property> properties);
    }

    private Context mContext;

    public PhotoManager(Context context) {
        this.mContext = context;
    }

    public void getPhoto(long start, long end, Callback callback) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Images.Media.DATE_TAKEN,
                            MediaStore.Images.Media.DISPLAY_NAME
                    },
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = \"Camera\" and "
                            + MediaStore.Images.Media.DATE_TAKEN + " >= " + start + " and "
                            + MediaStore.Images.Media.DATE_TAKEN + " <= " + end, null, null
            );

            List<Long> dates = new ArrayList<>();
            List<String> names = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    dates.add(cursor.getLong(0));
                    names.add(cursor.getString(1));
                }
                cursor.close();
            }
            if (dates.size() == 0)
                return;

            int rand = (int) (Math.random() * dates.size());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            try {
                final Bitmap bitmap = BitmapFactory.decodeFile(PATH + names.get(rand), options);
                if (bitmap != null) {
                    new Thread(() -> {
                        callback.callback(bitmap, dates.get(rand),
                                ImageRecognitionRequest.getLabel(new ImageRecognitionRequest(mContext).request(bitmap, ImageRecognitionRequest.REQUEST_LABEL)).getProperties());
                    }).start();
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }

        }
    }

}
