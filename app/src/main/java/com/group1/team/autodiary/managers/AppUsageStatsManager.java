package com.group1.team.autodiary.managers;

import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.group1.team.autodiary.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by q on 2017-01-21.
 */

@TargetApi(21)
public class AppUsageStatsManager  {
    private List<UsageStats> appUsageStats = null;
    private Context mContext = null;

    public AppUsageStatsManager(Context context, long appStartTime) {
        this.mContext = context;

        final UsageStatsManager usageStatsManager =
                (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        appUsageStats =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        appStartTime,
                        System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));

        Collections.sort(this.appUsageStats, new UsageTimeDescCompare());
    }

    static class UsageTimeDescCompare implements Comparator<UsageStats> {
        @Override
        public int compare(UsageStats arg0, UsageStats arg1) {
            return arg0.getTotalTimeInForeground() > arg1.getTotalTimeInForeground() ? -1
                    : arg0.getTotalTimeInForeground() < arg1.getTotalTimeInForeground() ? 1
                    : 0;
        }
    }

    public String getAppName(int position) { return getAppNameFromPackageName(appUsageStats.get(position).getPackageName()); }

    public Long getAppUsageTime(int position) { return appUsageStats.get(position).getTotalTimeInForeground(); }

    private String getAppNameFromPackageName(String packageName) {
        final PackageManager pm = mContext.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "(Unknown Program)");
    }
}
