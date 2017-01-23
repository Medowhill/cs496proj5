package com.group1.team.autodiary.objects;

import android.support.annotation.NonNull;

public class Place implements Comparable<Place> {

    private long mTime, mEndTime;
    private String mName;

    public Place(long time, String name) {
        this.mTime = time;
        this.mName = name;
    }

    public long getTime() {
        return mTime;
    }

    public String getName() {
        return mName;
    }

    public void setEndTime(long endTime) {
        this.mEndTime = endTime;
    }

    public long getDuration() {
        return mEndTime - mTime;
    }

    @Override
    public int compareTo(@NonNull Place o) {
        long d = o.getDuration() - this.getDuration();
        if (d == 0)
            return 0;

        return (int) (d / Math.abs(d));
    }
}
