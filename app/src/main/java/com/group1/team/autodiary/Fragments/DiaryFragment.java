package com.group1.team.autodiary.Fragments;

import android.graphics.Typeface;
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
import com.group1.team.autodiary.utils.DiaryUtil;


public class DiaryFragment extends Fragment {

    private TextView textView;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            textView.setText((String) msg.obj);
        }
    };

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.fragment_diary, null);
        textView = (TextView) view.findViewById(R.id.diary_textView_diary);
        textView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "NanumPen.ttf"));
        return view;
    }

    public void finishLoadData(DiaryActivity diaryActivity) {
        String str = "";
        str += DiaryUtil.weatherToDiary(getContext(), diaryActivity.getWeathers(), true);
        str += DiaryUtil.placeToDiary(getContext(), diaryActivity.getPlaces());
        str += DiaryUtil.planToDiary(getContext(), diaryActivity.getPlans(), true);
        str += DiaryUtil.planToDiary(getContext(), diaryActivity.getNextPlans(), false);
        str += DiaryUtil.usageToDiary(getContext(), diaryActivity.getUsages());
        str += DiaryUtil.newsToDiary(getContext(), diaryActivity.getNews().subList(0, 3));
        str += DiaryUtil.weatherToDiary(getContext(), diaryActivity.getForecasts(), false);
        Message message = new Message();
        message.obj = str;
        handler.sendMessage(message);
    }
}