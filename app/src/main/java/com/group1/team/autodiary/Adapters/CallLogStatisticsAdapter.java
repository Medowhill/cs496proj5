package com.group1.team.autodiary.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.CallLog;

import java.util.List;

/**
 * Created by q on 2017-01-24.
 */

public class CallLogStatisticsAdapter extends ArrayAdapter<CallLog> {
    Context mContext;
    List<CallLog> callLogs;
    LayoutInflater inflater;

    public CallLogStatisticsAdapter(Context context, int resource, List<CallLog> callLogs) {
        super(context, resource);
        mContext = context;
        this.callLogs = callLogs;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { return callLogs.size(); }

    @Override
    public CallLog getItem(int position) { return callLogs.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.statistics_call_log_item, viewGroup, false);

        CallLog callLog = callLogs.get(position);

        ((TextView) convertView.findViewById(R.id.call_log_item_name)).setText(callLog.getName());
        ((TextView) convertView.findViewById(R.id.call_log_item_date)).setText(callLog.getCallDayTime().toString());
        ((TextView) convertView.findViewById(R.id.call_log_item_time)).setText(millisecTimeParser(callLog.getCallDuration() * 1000));

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
