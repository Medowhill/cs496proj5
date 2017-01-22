package com.group1.team.autodiary.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.activities.DiaryActivity;
import com.group1.team.autodiary.managers.CallLogManager;
import com.group1.team.autodiary.managers.MusicManager;
import com.group1.team.autodiary.objects.AppUsage;
import com.group1.team.autodiary.objects.CallLog;
import com.group1.team.autodiary.objects.Music;
import com.group1.team.autodiary.objects.Place;

import java.util.HashMap;
import java.util.List;


public class StatisticsFragment extends Fragment {
    View view;

    List<AppUsage> mAppUsages;
    CallLogManager mCallLogManager;
    List<Music> mMusics;
    List<Place> mPlaces;

    Handler handler;

    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup viewGroup, Bundle savedInstanceState) {

        view = layoutInflater.inflate(R.layout.fragment_statistics, null);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    // call log part
                    CallLog longestCall = mCallLogManager.getLongestCall();
                    if (longestCall != null)
                        ((TextView) view.findViewById(R.id.single_longest_call)).setText(longestCall.getName() + " - " + longestCall.getCallDuration());
                    String[] longestCallPerson = mCallLogManager.getLongestCallPerson();
                    if (longestCallPerson != null)
                        ((TextView) view.findViewById(R.id.longest_call_person)).setText(longestCallPerson[0] + " - " + longestCallPerson[1]);
                    ((TextView) view.findViewById(R.id.total_call_time)).setText(String.valueOf(mCallLogManager.getTotalCallTime()) + getString(R.string.sec));

                    // total spent money part

                    // app usage part
                    ((TextView) view.findViewById(R.id.first_most_used_app)).setText(mAppUsages.get(0).getName());
                    ((TextView) view.findViewById(R.id.first_most_used_app_time)).setText(String.valueOf(mAppUsages.get(0).getTime() / 1000) + getString(R.string.sec));
                    ((TextView) view.findViewById(R.id.second_most_used_app)).setText(mAppUsages.get(1).getName());
                    ((TextView) view.findViewById(R.id.second_most_used_app_time)).setText(String.valueOf(mAppUsages.get(1).getTime() / 1000) + getString(R.string.sec));
                    ((TextView) view.findViewById(R.id.third_most_used_app)).setText(mAppUsages.get(2).getName());
                    ((TextView) view.findViewById(R.id.third_most_used_app_time)).setText(String.valueOf(mAppUsages.get(2).getTime() / 1000) + getString(R.string.sec));

                    // visited place part
                    HashMap.Entry<String, Long>[] longestVisitedPlaces = getLongestVisitedPlaces();
                    if (longestVisitedPlaces[0] != null) {
                        ((TextView) view.findViewById(R.id.first_longest_visited_place)).setText(longestVisitedPlaces[0].getKey());
                        ((TextView) view.findViewById(R.id.first_longest_visited_place_time)).setText(
                                String.valueOf(longestVisitedPlaces[0].getValue() / 1000) + getString(R.string.sec) + " " + getString(R.string.visited));
                    }
                    if (longestVisitedPlaces[1] != null) {
                        ((TextView) view.findViewById(R.id.first_longest_visited_place)).setText(longestVisitedPlaces[1].getKey());
                        ((TextView) view.findViewById(R.id.first_longest_visited_place_time)).setText(
                                String.valueOf(longestVisitedPlaces[1].getValue() / 1000) + getString(R.string.sec) + " " + getString(R.string.visited));
                    }
                    if (longestVisitedPlaces[2] != null) {
                        ((TextView) view.findViewById(R.id.first_longest_visited_place)).setText(longestVisitedPlaces[2].getKey());
                        ((TextView) view.findViewById(R.id.first_longest_visited_place_time)).setText(
                                String.valueOf(longestVisitedPlaces[2].getValue() / 1000) + getString(R.string.sec) + " " + getString(R.string.visited));
                    }

                    // played music part
                    String[] mostFrequentlyPlayedMusicInfo = MusicManager.getMostFrequentlyPlayedMusic(mMusics);
                    if (mostFrequentlyPlayedMusicInfo != null)
                        ((TextView) view.findViewById(R.id.most_frequently_played_music)).setText(
                                mostFrequentlyPlayedMusicInfo[0] + " - " + mostFrequentlyPlayedMusicInfo[1] + " (" + mostFrequentlyPlayedMusicInfo[2] + getString(R.string.times) + ")");
                }
            }
        };

        return view;
    }

    public void finishLoadData(DiaryActivity diaryActivity) {
        mAppUsages = diaryActivity.getUsages();
        mCallLogManager = diaryActivity.getCallLogManager();
        mMusics = diaryActivity.getMusics();
        mPlaces = diaryActivity.getPlaces();
        handler.sendEmptyMessage(0);
    }

    // Now Place instance has getDuration method
    // Also, places list is sorted due to its duration
    // However, the redundancy is not removed
    // You need to modify this method because of above changes
    public HashMap.Entry<String, Long>[] getLongestVisitedPlaces() {
        if (mPlaces.size() == 0) return new HashMap.Entry[]{null, null, null};

        HashMap<String, Long> hashMap = new HashMap<>();

        for (int i = 0; i < mPlaces.size() - 1; i++) {
            String key = mPlaces.get(i).getName();
            if (hashMap.containsKey(key))
                hashMap.put(key, hashMap.get(key) + mPlaces.get(i + 1).getTime() - mPlaces.get(i).getTime());
            else
                hashMap.put(key, mPlaces.get(i + 1).getTime() - mPlaces.get(i).getTime());
        }

        String key = mPlaces.get(mPlaces.size() - 1).getName();
        if (hashMap.containsKey(key))
            hashMap.put(key, hashMap.get(key) + System.currentTimeMillis() - mPlaces.get(mPlaces.size() - 1).getTime());
        else
            hashMap.put(key, System.currentTimeMillis() - mPlaces.get(mPlaces.size() - 1).getTime());

        HashMap.Entry<String, Long>[] longestVisitedPlaces = new HashMap.Entry[]{null, null, null};

        for (int i = 0; i < 3; i++) {
            for (HashMap.Entry<String, Long> entry : hashMap.entrySet()) {
                if (longestVisitedPlaces[i] == null ||
                        longestVisitedPlaces[i].getValue() < entry.getValue())
                    longestVisitedPlaces[i] = entry;
            }

            if (longestVisitedPlaces[i] == null)
                break;
            hashMap.remove(longestVisitedPlaces[i].getKey());
        }

        return longestVisitedPlaces;
    }
}