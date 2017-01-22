
package com.group1.team.autodiary.objects;

import android.graphics.Bitmap;

import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.List;

public class LabelPhoto {

    private Bitmap mBitmap;
    private List<EntityAnnotation> mAnnotations;
    long mTime;

    public LabelPhoto(Bitmap bitmap, List<EntityAnnotation> annotations, long time) {
        mBitmap = bitmap;
        mAnnotations = annotations;
        mTime = time;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public List<EntityAnnotation> getAnnotations() {
        return mAnnotations;
    }

    public long getTime() {
        return mTime;
    }
}
