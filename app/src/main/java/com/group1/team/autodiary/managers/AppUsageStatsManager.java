package com.group1.team.autodiary.managers;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.group1.team.autodiary.objects.AppUsage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AppUsageStatsManager {

    private List<AppUsage> appUsageStats;

    public AppUsageStatsManager(Context context, long dayStartTime, long dayEndTime) {
        appUsageStats = new ArrayList<>();
        if (Build.VERSION.SDK_INT > 21) {
            final UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> statsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, dayStartTime, dayEndTime);

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            ResolveInfo defaultLauncher = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            String launcherPackageName = defaultLauncher.activityInfo.packageName;

            for (UsageStats stats : statsList)
                if (!stats.getPackageName().equals(launcherPackageName))
                    appUsageStats.add(new AppUsage(context, stats.getPackageName(), stats.getTotalTimeInForeground()));
            Collections.sort(appUsageStats);
        }
    }

    public List<AppUsage> getAllItems() {
        return appUsageStats;
    }
}
