package com.group1.team.autodiary.managers;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

import com.group1.team.autodiary.objects.AppUsage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class AppUsageStatsManager {

    private Context mContext;

    public AppUsageStatsManager(Context context) {
        mContext = context;
    }

    private String getName(String packageName) {
        PackageManager packageManager = mContext.getPackageManager();
        String name = null;
        try {
            name = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    public List<AppUsage> getAppUsages(long dayStartTime, long dayEndTime) {
        List<AppUsage> appUsageStats = new ArrayList<>();
        Log.d("AppUsageStatsManager", "start : " + new Date(dayStartTime).toString() + " / end : " + new Date(dayEndTime).toString());

        if (Build.VERSION.SDK_INT > 21) {
            final UsageStatsManager usageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> statsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, dayStartTime, dayEndTime + TimeUnit.DAYS.toMillis(1));

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            ResolveInfo defaultLauncher = mContext.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            String launcherPackageName = defaultLauncher.activityInfo.packageName;

            for (UsageStats stats : statsList) {
                if (!stats.getPackageName().equals(launcherPackageName)) {
                    String name = getName(stats.getPackageName());
                    if (name != null)
                        appUsageStats.add(new AppUsage(name, stats.getTotalTimeInForeground()));
                }
            }
            Collections.sort(appUsageStats);
        }
        return appUsageStats;
    }
}
