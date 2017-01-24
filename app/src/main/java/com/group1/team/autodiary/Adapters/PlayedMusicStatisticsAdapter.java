package com.group1.team.autodiary.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.AppUsage;
import com.group1.team.autodiary.objects.Music;

import java.util.List;

/**
 * Created by q on 2017-01-24.
 */

public class PlayedMusicStatisticsAdapter extends ArrayAdapter<Music> {
    Context mContext;
    List<Music> musics;
    LayoutInflater inflater;

    public PlayedMusicStatisticsAdapter(Context context, int resource, List<Music> musics) {
        super(context, resource);
        mContext = context;
        this.musics = musics;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { return musics.size(); }

    @Override
    public Music getItem(int position) { return musics.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.statistics_played_music_item, viewGroup, false);

        return convertView;
    }
}

