package com.group1.team.autodiary.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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

        return convertView;
    }
}
