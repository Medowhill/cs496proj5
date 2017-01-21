package com.group1.team.autodiary.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.activities.DiaryActivity;
import com.group1.team.autodiary.activities.MainActivity;
import com.group1.team.autodiary.managers.AppUsageStatsManager;
import com.group1.team.autodiary.managers.CallLogManager;
import com.group1.team.autodiary.objects.AppUsage;
import com.group1.team.autodiary.objects.CallLog;

import java.util.List;


public class StatisticsFragment extends Fragment {
    View view;

    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup viewGroup, Bundle savedInstanceState) {
        long start = System.currentTimeMillis();
        view = layoutInflater.inflate(R.layout.fragment_statistics, null);
        Log.i("cs496", System.currentTimeMillis() - start + "");

        List<AppUsage> usages = new AppUsageStatsManager(getContext(), MainActivity.dayStartTime, MainActivity.dayEndTime).getAllItems();
        ((TextView) view.findViewById(R.id.first_most_used_app)).setText(usages.get(0).getName());
        ((TextView) view.findViewById(R.id.first_most_used_app_time)).setText(String.valueOf(usages.get(0).getTime() / 1000) + getString(R.string.sec));
        ((TextView) view.findViewById(R.id.second_most_used_app)).setText(usages.get(1).getName());
        ((TextView) view.findViewById(R.id.second_most_used_app_time)).setText(String.valueOf(usages.get(1).getTime() / 1000) + getString(R.string.sec));
        ((TextView) view.findViewById(R.id.third_most_used_app)).setText(usages.get(2).getName());
        ((TextView) view.findViewById(R.id.third_most_used_app_time)).setText(String.valueOf(usages.get(2).getTime() / 1000) + getString(R.string.sec));

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

    public void finishLoadData(DiaryActivity diaryActivity) {

    }
}