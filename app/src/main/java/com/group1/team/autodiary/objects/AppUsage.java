package com.group1.team.autodiary.objects;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

public class AppUsage implements Comparable<AppUsage> {

    private String mName;
    private long mTime;

    public AppUsage(Context context, String packageName, long time) {
        this.mTime = time;

        final PackageManager packageManager = context.getPackageManager();
        try {
            mName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            mName = "unknown";
        }
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
