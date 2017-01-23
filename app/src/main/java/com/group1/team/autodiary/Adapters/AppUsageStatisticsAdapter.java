package com.group1.team.autodiary.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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
            convertView = inflater.inflate(R.layout.statistics_call_log_item, viewGroup, false);

        return convertView;
    }
}
