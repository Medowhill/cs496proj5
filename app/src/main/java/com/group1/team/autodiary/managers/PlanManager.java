package com.group1.team.autodiary.managers;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PlanManager {

    private Context mContext;

    public PlanManager(Context context) {
        mContext = context;
    }

    public List<String> getPlan(long start, long end) {
        List<String> plans = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
            ContentUris.appendId(eventsUriBuilder, start);
            ContentUris.appendId(eventsUriBuilder, end);
            Uri eventsUri = eventsUriBuilder.build();

            Cursor cursor = mContext.getContentResolver().query(eventsUri, new String[]{
                    CalendarContract.Instances.TITLE}, null, null, CalendarContract.Instances.DTSTART + " ASC");

            if (cursor != null) {
                while (cursor.moveToNext())
                    plans.add(cursor.getString(0));
                cursor.close();
            }
        }
        return plans;
    }
}
