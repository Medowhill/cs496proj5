package com.group1.team.autodiary.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.AppUsage;
import com.group1.team.autodiary.objects.CallLog;

import java.util.List;

/**
 * Created by q on 2017-01-24.
 */

public class AppUsageStatisticsAdapter extends ArrayAdapter<AppUsage> {
    Context mContext;
    List<AppUsage> appUsages;
    LayoutInflater inflater;

    public AppUsageStatisticsAdapter(Context context, int resource, List<AppUsage> appUsages) {
        super(context, resource);
        mContext = context;
        this.appUsages = appUsages;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { return appUsages.size(); }

    @Override
    public AppUsage getItem(int position) { return appUsages.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.statistics_app_usage_item, viewGroup, false);

        AppUsage appUsage = appUsages.get(position);
        ((TextView) convertView.findViewById(R.id.app_usage_item_name)).setText(appUsage.getName());
        ((TextView) convertView.findViewById(R.id.app_usage_item_time)).setText(millisecTimeParser(appUsage.getTime()));

        return convertView;
    }

    private String millisecTimeParser(long millisec) {
        long sec = millisec/1000;
        if (sec < 60) return sec + mContext.getString(R.string.sec);
        else if (sec < 3600) return sec/60 + mContext.getString(R.string.min)
                + " " + sec%60 + mContext.getString(R.string.sec);
        else return sec/3600 + mContext.getString(R.string.hour)
                    + " " + (sec%3600)/60 + mContext.getString(R.string.min)
                    + " " + (sec%3600)%60 + mContext.getString(R.string.sec);
    }
}
