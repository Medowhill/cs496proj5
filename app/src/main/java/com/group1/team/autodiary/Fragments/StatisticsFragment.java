package com.group1.team.autodiary.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.activities.MainActivity;
import com.group1.team.autodiary.managers.AppUsageStatsManager;
import com.group1.team.autodiary.managers.CallLogManager;
import com.group1.team.autodiary.objects.CallLog;

import static android.support.v7.recyclerview.R.styleable.RecyclerView;

/**
 * Created by q on 2017-01-21.
 */

public class StatisticsFragment extends Fragment {
    View view;

    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup viewGroup, Bundle savedInstanceState) {
        view = layoutInflater.inflate(R.layout.fragment_statistics, null);

        AppUsageStatsManager appUsageStatsManager = new AppUsageStatsManager(getContext(), MainActivity.dayStartTime, MainActivity.dayEndTime);
        ((TextView) view.findViewById(R.id.first_most_used_app)).setText(appUsageStatsManager.getAppName(0));
        ((TextView) view.findViewById(R.id.first_most_used_app_time)).setText(String.valueOf(appUsageStatsManager.getAppUsageTime(0) / 1000) + getString(R.string.sec));
        ((TextView) view.findViewById(R.id.second_most_used_app)).setText(appUsageStatsManager.getAppName(1));
        ((TextView) view.findViewById(R.id.second_most_used_app_time)).setText(String.valueOf(appUsageStatsManager.getAppUsageTime(1) / 1000) + getString(R.string.sec));
        ((TextView) view.findViewById(R.id.third_most_used_app)).setText(appUsageStatsManager.getAppName(2));
        ((TextView) view.findViewById(R.id.third_most_used_app_time)).setText(String.valueOf(appUsageStatsManager.getAppUsageTime(2) / 1000) + getString(R.string.sec));

        CallLogManager callLogManager = new CallLogManager(getContext(), MainActivity.dayStartTime, MainActivity.dayEndTime);
        CallLog longestCall = callLogManager.getLongestCall();
        if (longestCall != null)
            ((TextView) view.findViewById(R.id.single_longest_call)).setText(longestCall.getName() + " - " + longestCall.getCallDuration());
        String[] longestCallPerson = callLogManager.getLongestCallPerson();
        if (longestCallPerson != null)
            ((TextView) view.findViewById(R.id.longest_call_person)).setText(longestCallPerson[0] + " - " + longestCallPerson[1]);
        ((TextView) view.findViewById(R.id.total_call_time)).setText(String.valueOf(callLogManager.getTotalCallTime()) + getString(R.string.sec));



        return view;
    }
}