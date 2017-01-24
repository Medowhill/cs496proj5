package com.group1.team.autodiary.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.Place;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class VisitedPlaceStatisticsAdapter extends ArrayAdapter<Place> {

    private Context mContext;
    private List<Place> places;
    private LayoutInflater inflater;

    private SimpleDateFormat mFormat;

    public VisitedPlaceStatisticsAdapter(Context context, int resource, List<Place> places) {
        super(context, resource);
        mContext = context;
        this.places = places;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mFormat = new SimpleDateFormat(context.getString(R.string.visited_places_time_format), Locale.KOREA);
    }

    @Override
    public int getCount() {
        return places.size();
    }

    @Override
    public Place getItem(int position) {
        return places.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.statistics_visited_place_item, viewGroup, false);

        Place place = places.get(position);
        ((TextView) convertView.findViewById(R.id.visited_place_item_time)).setText(mFormat.format(new Date(place.getTime()))
                + " - " + mFormat.format(new Date(place.getTime() + place.getDuration())));
        ((TextView) convertView.findViewById(R.id.visited_place_item_place)).setText(place.getName());

        return convertView;
    }
}

