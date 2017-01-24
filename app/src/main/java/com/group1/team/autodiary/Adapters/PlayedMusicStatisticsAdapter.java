package com.group1.team.autodiary.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.Music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlayedMusicStatisticsAdapter extends ArrayAdapter<Map.Entry<String, Integer>> {

    private Context mContext;
    private List<Map.Entry<String, Integer>> musics;
    private LayoutInflater inflater;

    public PlayedMusicStatisticsAdapter(Context context, int resource, List<Music> musics) {
        super(context, resource);
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        HashMap<String, Integer> hashMap = new HashMap<>();
        for (Music music : musics) {
            String key = music.getTrack();
            if (hashMap.containsKey(key))
                hashMap.put(key, hashMap.get(key) + 1);
            else
                hashMap.put(key, 1);
        }

        this.musics = new ArrayList<>(hashMap.entrySet());
        Collections.sort(this.musics, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });
    }

    @Override
    public int getCount() {
        return musics.size();
    }

    @Override
    public Map.Entry<String, Integer> getItem(int position) {
        return musics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.statistics_played_music_item, viewGroup, false);

        Map.Entry<String, Integer> entry = musics.get(position);
        ((TextView) convertView.findViewById(R.id.played_music_item_name)).setText(entry.getKey());
        ((TextView) convertView.findViewById(R.id.played_music_item_time))
                .setText(String.format(mContext.getString(R.string.played_musics_format), entry.getValue()));

        return convertView;
    }
}

