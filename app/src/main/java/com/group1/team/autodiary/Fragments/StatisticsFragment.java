package com.group1.team.autodiary.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.group1.team.autodiary.R;

/**
 * Created by q on 2017-01-21.
 */

public class StatisticsFragment extends Fragment {
    View view;

    @Override
    public View onCreateView(LayoutInflater layoutInflater,
                             ViewGroup viewGroup, Bundle savedInstanceState) {
        view = layoutInflater.inflate(R.layout.fragment_statistics, null);

        return view;
    }
}