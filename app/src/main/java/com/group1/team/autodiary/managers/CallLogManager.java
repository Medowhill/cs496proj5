package com.group1.team.autodiary.managers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.CallLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by q on 2017-01-21.
 */

public class CallLogManager {

    private LayoutInflater inflater = null;
    private List<CallLog> callLogs = null;
    private Context mContext = null;

    public CallLogManager(Context c, long dayStartTime, long dayEndTime) {
        this.mContext = c;

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {
            Cursor callLogCursor = mContext.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI,
                    null,
                    android.provider.CallLog.Calls.DATE + " >= " + dayStartTime + " and "
                            + android.provider.CallLog.Calls.DATE + " <= " + dayEndTime,
                    null, android.provider.CallLog.Calls.DATE + " DESC");

            int numberIndex = callLogCursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER);
            int typeIndex = callLogCursor.getColumnIndex(android.provider.CallLog.Calls.TYPE);
            int dateIndex = callLogCursor.getColumnIndex(android.provider.CallLog.Calls.DATE);
            int durationIndex = callLogCursor.getColumnIndex(android.provider.CallLog.Calls.DURATION);
            int nameIndex = callLogCursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME);

            callLogs = new ArrayList<>();

            callLogCursor.moveToFirst();
            if (callLogCursor.getCount() != 0) {
                do {
                    String phoneNumber = callLogCursor.getString(numberIndex);
                    String callType = callLogCursor.getString(typeIndex);
                    String dir = null;
                    int dirCode = Integer.parseInt(callType);
                    switch (dirCode) {
                        case android.provider.CallLog.Calls.OUTGOING_TYPE:
                            dir = "OUTGOING";
                            break;
                        case android.provider.CallLog.Calls.INCOMING_TYPE:
                            dir = "INCOMING";
                            break;
                        case android.provider.CallLog.Calls.MISSED_TYPE:
                            dir = "MISSED";
                            break;
                    }
                    String callDate = callLogCursor.getString(dateIndex);
                    Date callDayTime = new Date(Long.valueOf(callDate));
                    String callDuration = callLogCursor.getString(durationIndex);
                    String callName = callLogCursor.getString(nameIndex);

                    callLogs.add(new CallLog(phoneNumber, dir, callDayTime, callDuration, callName));
                } while (callLogCursor.moveToNext());
            }
        }
    }

    public CallLog getLongestCall() {
        if (callLogs.size() == 0) return null;
        CallLog longestCall = callLogs.get(0);

        for (int i = 1; i < callLogs.size(); i++) {
            if (Long.valueOf(longestCall.getCallDuration()) < Long.valueOf(callLogs.get(i).getCallDuration()))
                longestCall = callLogs.get(i);
        }

        return longestCall;
    }

    public String[] getLongestCallPerson() {
        if (callLogs.size() == 0) return null;
        HashMap<String, Long> hashMap = new HashMap<>();

        String longestCallPerson = callLogs.get(0).getName();
        hashMap.put(longestCallPerson, Long.valueOf(callLogs.get(0).getCallDuration()));

        for (int i = 1; i < callLogs.size(); i++) {
            CallLog temp = callLogs.get(i);
            if (hashMap.containsKey(temp.getName()))
                hashMap.put(temp.getName(), hashMap.get(temp.getName()) + Long.valueOf(temp.getCallDuration()));
            else
                hashMap.put(temp.getName(), Long.valueOf(temp.getCallDuration()));

            if (hashMap.get(longestCallPerson) < hashMap.get(temp.getName()))
                longestCallPerson = temp.getName();
        }

        return new String[] {
                longestCallPerson,
                String.valueOf(hashMap.get(longestCallPerson))
        };
    }

    public Long getTotalCallTime() {
        long totalCallTime = 0;
        for (int i = 0; i < callLogs.size(); i++)
            totalCallTime += Long.valueOf(callLogs.get(i).getCallDuration());

        return totalCallTime;
    }

    public List<CallLog> getAllItems() { return callLogs; }
}
