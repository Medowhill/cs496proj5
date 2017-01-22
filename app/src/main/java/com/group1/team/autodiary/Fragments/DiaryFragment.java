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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.activities.DiaryActivity;
import com.group1.team.autodiary.objects.LabelPhoto;
import com.group1.team.autodiary.utils.DiaryUtil;


public class DiaryFragment extends Fragment {

    private LinearLayout layout, layoutLabel, layoutFace;
    private TextView textView, textViewDate, textViewLabel;
    private ImageView imageViewLabel, imageViewFace;

    private String mDiary, mLabelDescription, mDate;
    private Bitmap mBitmapLabel, mBitmapFace;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            layout.setVisibility(View.VISIBLE);
            textViewDate.setText(mDate);
            textView.setText(mDiary);

            if (mBitmapLabel != null) {
                imageViewLabel.setImageBitmap(mBitmapLabel);
                textViewLabel.setText(mLabelDescription);
            } else
                layoutLabel.setVisibility(View.GONE);

            if (mBitmapFace != null)
                imageViewFace.setImageBitmap(mBitmapFace);
            else
                layoutFace.setVisibility(View.GONE);
        }
    };

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.fragment_diary, null);
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "NanumPen.ttf");

        layout = (LinearLayout) view.findViewById(R.id.diary_layout);
        layoutLabel = (LinearLayout) view.findViewById(R.id.diary_layout_label);
        layoutFace = (LinearLayout) view.findViewById(R.id.diary_layout_face);
        imageViewLabel = (ImageView) view.findViewById(R.id.diary_imageView_labelPhoto);
        imageViewFace = (ImageView) view.findViewById(R.id.diary_imageView_facePhoto);
        textView = (TextView) view.findViewById(R.id.diary_textView_diary);
        textViewDate = (TextView) view.findViewById(R.id.diary_textView_date);
        textViewLabel = (TextView) view.findViewById(R.id.diary_textView_labelDescription);
        TextView textViewLabelTitle = (TextView) view.findViewById(R.id.diary_textView_labelTitle);
        TextView textViewFaceTitle = (TextView) view.findViewById(R.id.diary_textView_faceTitle);

        textView.setTypeface(typeface);
        textViewDate.setTypeface(typeface);
        textViewLabel.setTypeface(typeface);
        textViewLabelTitle.setTypeface(typeface);
        textViewFaceTitle.setTypeface(typeface);
        return view;
    }

    public void finishLoadData(DiaryActivity diaryActivity) {
        DiaryUtil util = new DiaryUtil(getContext());
        mDate = util.dateToDiary(diaryActivity.getFinish());

        mDiary = util.wakeUpToDiary(diaryActivity.getStart());
        mDiary += util.weatherToDiary(diaryActivity.getWeathers(), true);
        mDiary += util.placeToDiary(diaryActivity.getPlaces());
        mDiary += util.planToDiary(diaryActivity.getPlans(), true);
        mDiary += util.planToDiary(diaryActivity.getNextPlans(), false);
        mDiary += util.phoneToDiary(diaryActivity.getCallLogManager());
        mDiary += util.usageToDiary(diaryActivity.getUsages());
        mDiary += util.newsToDiary(diaryActivity.getNews().subList(0, 3));
        mDiary += util.musicToDiary(diaryActivity.getMusics());
        mDiary += util.weatherToDiary(diaryActivity.getForecasts(), false);
        mDiary += util.endToDiary();

        LabelPhoto labelPhoto = diaryActivity.getLabelPhoto();
        if (labelPhoto != null) {
            mBitmapLabel = labelPhoto.getBitmap();
            mLabelDescription = util.labelToDiary(labelPhoto);
        } else
            mLabelDescription = "";

        mBitmapFace = diaryActivity.getFaceBitmap();

        handler.sendEmptyMessage(0);
    }
}