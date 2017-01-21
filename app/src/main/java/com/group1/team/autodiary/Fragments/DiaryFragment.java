package com.group1.team.autodiary.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.activities.DiaryActivity;


public class DiaryFragment extends Fragment {

    private TextView textView;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.fragment_diary, null);
        textView = (TextView) view.findViewById(R.id.diary_textView_diary);


        return view;
    }
    public void finishLoadData(DiaryActivity diaryActivity) {

    }
}