package com.group1.team.autodiary.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.managers.CallLogManager;

/**
 * Created by q on 2017-01-21.
 */

public class StatisticsFragment extends Fragment {
    View view;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup viewGroup, Bundle savedInstanceState) {
        view = layoutInflater.inflate(R.layout.fragment_statistics, null);

        TextView firstMostUsedApp = (TextView) view.findViewById(R.id.first_most_used_app);
        TextView firstMostUsedAppTime = (TextView) view.findViewById(R.id.first_most_used_app_time);
        TextView secondMostUsedApp = (TextView) view.findViewById(R.id.second_most_used_app);
        TextView secondMostUsedAppTime = (TextView) view.findViewById(R.id.second_most_used_app_time);
        TextView thirdMostUsedApp = (TextView) view.findViewById(R.id.third_most_used_app);
        TextView thirdMostUsedAppTime = (TextView) view.findViewById(R.id.third_most_used_app_time);

        CallLogManager callLogManager = new CallLogManager(getContext());

        String[] longestCallPerson = callLogManager.getLongestCallPerson();
        if (longestCallPerson != null)
            ((TextView) view.findViewById(R.id.longest_call_person)).setText(longestCallPerson[0] + " - " + longestCallPerson[1]);
        ((TextView) view.findViewById(R.id.total_call_time)).setText(String.valueOf(callLogManager.getTotalCallTime()));

        return view;
    }
}