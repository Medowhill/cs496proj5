package com.group1.team.autodiary.objects;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.group1.team.autodiary.Fragments.DiaryFragment;
import com.group1.team.autodiary.Fragments.StatisticsFragment;


public class ViewPagerAdapter extends FragmentPagerAdapter {

    DiaryFragment diaryFragment;
    StatisticsFragment statisticsFragment;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        diaryFragment = DiaryFragment.getInstance();
        statisticsFragment = new StatisticsFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int pos) {
        switch (pos) {
            case 0:
                return diaryFragment;
            case 1:
                return statisticsFragment;
            default:
                return null;
        }
    }

}
