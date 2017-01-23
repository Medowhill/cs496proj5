package com.group1.team.autodiary.objects;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

public class AppUsage implements Comparable<AppUsage> {

    private String mName;
    private long mTime;

    public AppUsage(String name, long time) {
        this.mTime = time;
        this.mName = name;
    }

    public String getName() {
        return mName;
    }

    public long getTime() {
        return mTime;
    }

    @Override
    public int compareTo(@NonNull AppUsage o) {
        long d = -this.mTime + o.mTime;
        if (d == 0)
            return 0;

        return (int) (d / Math.abs(d));
    }
}
