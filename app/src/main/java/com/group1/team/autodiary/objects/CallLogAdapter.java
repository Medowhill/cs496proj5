package com.group1.team.autodiary.objects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.group1.team.autodiary.R;

import java.util.List;

/**
 * Created by q on 2017-01-21.
 */

public class CallLogAdapter extends ArrayAdapter<CallLog> {

    private LayoutInflater inflater = null;
    private List<CallLog> callLogs = null;
    private Context mContext = null;

    public CallLogAdapter(Context c, List<CallLog> arrays) {
        super(c, R.layout.calllog_item, arrays);
        this.inflater = LayoutInflater.from(c);
        this.mContext = c;
        callLogs = arrays;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public CallLog getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertview, ViewGroup parent) {

        View v = convertview;

        if(v == null)
            v = inflater.inflate(R.layout.calllog_item, null);

        ((TextView)v.findViewById(R.id.calllog_item_phone_number)).setText(callLogs.get(position).getPhoneNumber());
        ((TextView)v.findViewById(R.id.calllog_item_dir)).setText(callLogs.get(position).getDir());
        ((TextView)v.findViewById(R.id.calllog_item_call_day_time)).setText(callLogs.get(position).getCallDayTime().toString());
        ((TextView)v.findViewById(R.id.calllog_item_call_duration)).setText(callLogs.get(position).getCallDuration());

        return v;
    }
}
