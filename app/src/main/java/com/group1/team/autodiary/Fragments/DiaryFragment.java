package com.group1.team.autodiary.Fragments;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.activities.DiaryActivity;
import com.group1.team.autodiary.objects.LabelPhoto;
import com.group1.team.autodiary.utils.DiaryUtil;


public class DiaryFragment extends Fragment {

    private TextView textView, textViewLabel;
    private ImageView imageViewLabel;

    private String mDiary, mLabelDescription;
    private Bitmap mBitmapLabel;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            textView.setText(mDiary);
            imageViewLabel.setImageBitmap(mBitmapLabel);
            textViewLabel.setText(mLabelDescription);
        }
    };

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.fragment_diary, null);
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "NanumPen.ttf");
        imageViewLabel = (ImageView) view.findViewById(R.id.diary_imageView_labelPhoto);
        textView = (TextView) view.findViewById(R.id.diary_textView_diary);
        textViewLabel = (TextView) view.findViewById(R.id.diary_textView_labelDescription);
        textView.setTypeface(typeface);
        textViewLabel.setTypeface(typeface);
        return view;
    }

    public void finishLoadData(DiaryActivity diaryActivity) {
        mDiary = "";
        mDiary += DiaryUtil.weatherToDiary(getContext(), diaryActivity.getWeathers(), true);
        mDiary += DiaryUtil.placeToDiary(getContext(), diaryActivity.getPlaces());
        mDiary += DiaryUtil.planToDiary(getContext(), diaryActivity.getPlans(), true);
        mDiary += DiaryUtil.planToDiary(getContext(), diaryActivity.getNextPlans(), false);
        mDiary += DiaryUtil.usageToDiary(getContext(), diaryActivity.getUsages());
        mDiary += DiaryUtil.newsToDiary(getContext(), diaryActivity.getNews().subList(0, 3));
        mDiary += DiaryUtil.weatherToDiary(getContext(), diaryActivity.getForecasts(), false);

        LabelPhoto labelPhoto = diaryActivity.getLabelPhoto();
        if (labelPhoto != null) {
            mBitmapLabel = labelPhoto.getBitmap();
            mLabelDescription = labelPhoto.getDescription();
        } else
            mLabelDescription = "";

        handler.sendEmptyMessage(0);
    }
}