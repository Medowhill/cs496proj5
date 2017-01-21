package com.group1.team.autodiary.objects;

import android.annotation.TargetApi;
import android.app.usage.UsageStats;
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

/**
 * Created by q on 2017-01-21.
 */

@TargetApi(21)
public class AppUsageStatsAdapter extends ArrayAdapter<UsageStats> {

    private LayoutInflater inflater = null;
    private List<UsageStats> appUsageStats = null;
    private Context mContext = null;

    public AppUsageStatsAdapter(Context context, List<UsageStats> appUsageStats) {
        super(context, R.layout.appusagestats_item, appUsageStats);
        this.inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.appUsageStats = appUsageStats;
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

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public UsageStats getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if(v == null)
            v = inflater.inflate(R.layout.appusagestats_item, null);

        ((TextView)v.findViewById(R.id.appusagestats_package_name))
                .setText(getAppNameFromPackageName(appUsageStats.get(position).getPackageName()));
        ((TextView)v.findViewById(R.id.appusagestats_last_time_stamp))
                .setText(String.valueOf(new Date(appUsageStats.get(position).getLastTimeStamp())));
        ((TextView)v.findViewById(R.id.appusagestats_total_time_in_foreground))
                .setText(String.valueOf(appUsageStats.get(position).getTotalTimeInForeground()) + "secs");

        return v;
    }

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
