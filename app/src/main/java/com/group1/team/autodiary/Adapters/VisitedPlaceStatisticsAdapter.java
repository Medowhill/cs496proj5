package com.group1.team.autodiary.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.AppUsage;
import com.group1.team.autodiary.objects.Place;

import java.util.List;

/**
 * Created by q on 2017-01-24.
 */

public class VisitedPlaceStatisticsAdapter extends ArrayAdapter<Place> {
    Context mContext;
    List<Place> places;
    LayoutInflater inflater;

    public VisitedPlaceStatisticsAdapter(Context context, int resource, List<Place> places) {
        super(context, resource);
        mContext = context;
        this.places = places;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { return places.size(); }

    @Override
    public Place getItem(int position) { return places.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.statistics_call_log_item, viewGroup, false);

        return convertView;
    }
}

