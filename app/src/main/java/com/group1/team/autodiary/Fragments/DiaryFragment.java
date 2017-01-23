package com.group1.team.autodiary.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class DiaryFragment extends Fragment {

    private LinearLayout layout, layoutLabel, layoutFace;
    private TextView textView, textViewDate, textViewLabel, textViewFace;
    private ImageView imageViewLabel, imageViewFace;

    private String mDiary, mLabelDescription, mFaceDescription, mDate, mFileName;
    private Bitmap mBitmapLabel, mBitmapFace;

    private long mLoadTime;
    private boolean mLoad = false;

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

            if (mBitmapFace != null) {
                imageViewFace.setImageBitmap(mBitmapFace);
                textViewFace.setText(mFaceDescription);
            } else
                layoutFace.setVisibility(View.GONE);
        }
    };

    public static DiaryFragment getInstance() {
        return new DiaryFragment();
    }

    public static DiaryFragment getInstance(long time) {
        DiaryFragment fragment = new DiaryFragment();
        fragment.mLoad = true;
        fragment.mLoadTime = time;
        return fragment;
    }

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
        textViewFace = (TextView) view.findViewById(R.id.diary_textView_faceDescription);
        TextView textViewLabelTitle = (TextView) view.findViewById(R.id.diary_textView_labelTitle);
        TextView textViewFaceTitle = (TextView) view.findViewById(R.id.diary_textView_faceTitle);

        textView.setTypeface(typeface);
        textViewDate.setTypeface(typeface);
        textViewLabel.setTypeface(typeface);
        textViewFace.setTypeface(typeface);
        textViewLabelTitle.setTypeface(typeface);
        textViewFaceTitle.setTypeface(typeface);

        if (mLoad)
            load(mLoadTime);
        return view;
    }

    public void finishLoadData(DiaryActivity diaryActivity) {
        mFileName = getFileName(diaryActivity.getFinish());

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
        if (mBitmapFace != null)
            mFaceDescription = util.faceToDiary(diaryActivity.getBestTime());

        handler.sendEmptyMessage(0);

        new Thread(() -> save()).start();
    }

    private void save() {
        try {
            JSONObject object = new JSONObject();
            object.put("date", mDate);
            object.put("diary", mDiary);
            object.put("label", mLabelDescription);
            object.put("face", mFaceDescription);

            FileOutputStream stream = getContext().openFileOutput(mFileName, Context.MODE_PRIVATE);
            stream.write(object.toString().getBytes());
            stream.flush();
            stream.close();

            if (mBitmapLabel != null) {
                stream = getContext().openFileOutput(mFileName + "l", Context.MODE_PRIVATE);
                mBitmapLabel.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
            }

            if (mBitmapFace != null) {
                stream = getContext().openFileOutput(mFileName + "f", Context.MODE_PRIVATE);
                mBitmapFace.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private String getFileName(long time) {
        return new SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(time);
    }

    public void load(long time) {
        String name = getFileName(time);

        try {
            FileInputStream stream = getContext().openFileInput(name);
            byte[] arr = new byte[stream.available()];
            stream.read(arr);

            JSONObject object = new JSONObject(new String(arr));
            mDate = object.getString("date");
            mDiary = object.getString("diary");
            mLabelDescription = object.getString("label");
            mFaceDescription = object.getString("face");
        } catch (IOException e) {
            e.printStackTrace();
            mDate = getString(R.string.diary_no);
            handler.sendEmptyMessage(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            mBitmapLabel = BitmapFactory.decodeStream(getContext().openFileInput(name + "l"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mBitmapFace = BitmapFactory.decodeStream(getContext().openFileInput(name + "f"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler.sendEmptyMessage(0);
    }
}