package com.group1.team.autodiary.objects;

import android.graphics.Bitmap;

import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LabelPhoto {

    private Bitmap mBitmap;
    private String mDescription;

    public LabelPhoto(Bitmap bitmap, List<EntityAnnotation> annotations, long time) {
        mBitmap = bitmap;
        mDescription = new SimpleDateFormat("HH:mm").format(new Date(time)) + "\n";
        if (annotations != null) {
            for (EntityAnnotation annotation : annotations)
                mDescription += ", " + annotation.getDescription();
        }
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public String getDescription() {
        return mDescription;
    }
}
