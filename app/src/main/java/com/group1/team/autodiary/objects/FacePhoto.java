package com.group1.team.autodiary.objects;

import android.util.Log;

public class FacePhoto {

    public static final int ANGER = 0, JOY = 1, SORROW = 2, SURPRISE = 3;

    private long mTime;
    private int mAnger, mJoy, mSurprise, mSorrow;

    public static int faceToInt(String str) {
        Log.i("cs496test", str);
        if (str.equals("VERY UNLIKELY"))
            return 0;
        else if (str.equals("UNLIKELY"))
            return 1;
        else if (str.equals("LIKELY"))
            return 2;
        else if (str.equals("VERY LIKELY"))
            return 3;
        return -1;
    }

    public FacePhoto(long time, int anger, int joy, int sorrow, int surprise) {
        this.mTime = time;
        this.mAnger = anger;
        this.mJoy = joy;
        this.mSurprise = sorrow;
        this.mSorrow = surprise;
    }

    public long getTime() {
        return mTime;
    }

    public int getFeeling(int feel) {
        switch (feel) {
            case ANGER:
                return mAnger;
            case JOY:
                return mJoy;
            case SORROW:
                return mSorrow;
            case SURPRISE:
                return mSurprise;
            default:
                return 0;
        }
    }
}
