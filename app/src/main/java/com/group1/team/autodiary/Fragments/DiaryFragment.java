package com.group1.team.autodiary.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class DiaryFragment extends Fragment implements TextToSpeech.OnInitListener {

    public static int REQUEST_SHARE_IMAGE = 0;

    private LinearLayout layout, layoutLabel, layoutFace;
    private TextView textView, textViewDate, textViewLabel, textViewFace;
    private ImageView imageViewLabel, imageViewFace;

    private String mDiary, mLabelDescription, mFaceDescription, mDate, mFileName;
    private Bitmap mBitmapLabel, mBitmapFace;

    private TextToSpeech speech;
    private long mLoadTime;
    private boolean mLoad = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            layout.setVisibility(View.VISIBLE);
            textViewDate.setText(mDate);
            textView.setText(mDiary);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (speech != null) {
                        speech.stop();
                        speech.shutdown();
                    }
                    speech = new TextToSpeech(getContext(), DiaryFragment.this);
                }
            });

            if (mBitmapLabel != null) {
                layoutLabel.setVisibility(View.VISIBLE);
                imageViewLabel.setImageBitmap(mBitmapLabel);
                textViewLabel.setText(mLabelDescription);
            } else
                layoutLabel.setVisibility(View.GONE);

            if (mBitmapFace != null) {
                layoutFace.setVisibility(View.VISIBLE);
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onInit(int status) {
        speech.speak(mDiary, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onDestroy() {
        if (speech != null) {
            speech.stop();
            speech.shutdown();
        }

        super.onDestroy();
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

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                shareText(mDiary);
                return true;
            }
        });
        textViewLabel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                shareText(mLabelDescription);
                return true;
            }
        });
        textViewFace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                shareText(mFaceDescription);
                return true;
            }
        });
        imageViewLabel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                shareImage(mBitmapLabel);
                return true;
            }
        });
        imageViewFace.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                shareImage(mBitmapFace);
                return true;
            }
        });

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
        mDiary += util.assetToDiary(diaryActivity.getAssetInfos());
        mDiary += util.newsToDiary(diaryActivity.getNews());
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

    private void shareText(String text) {
        if (text == null || text.length() == 0)
            return;

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.diary_share)));
    }

    private void shareImage(Bitmap bitmap) {
        if (bitmap == null)
            return;

        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.jpg");
        try {
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        getActivity().startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.diary_share)), REQUEST_SHARE_IMAGE);
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
            } else {
                File file = new File(getContext().getFilesDir().getPath() + File.separator + mFileName + "l");
                if (file.exists())
                    file.delete();
            }

            if (mBitmapFace != null) {
                stream = getContext().openFileOutput(mFileName + "f", Context.MODE_PRIVATE);
                mBitmapFace.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
            } else {
                File file = new File(getContext().getFilesDir().getPath() + File.separator + mFileName + "f");
                if (file.exists())
                    file.delete();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private String getFileName(long time) {
        return new SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(time);
    }

    public void deleteTempFile() {
        new File(Environment.getExternalStorageDirectory() + File.separator + "temp").delete();
    }

    public void load(long time) {
        if (speech != null)
            speech.stop();
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
            mDiary = "";
            mBitmapLabel = null;
            mBitmapFace = null;
            handler.sendEmptyMessage(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            FileInputStream stream = getContext().openFileInput(name + "l");
            mBitmapLabel = BitmapFactory.decodeStream(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileInputStream stream = getContext().openFileInput(name + "f");
            mBitmapFace = BitmapFactory.decodeStream(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler.sendEmptyMessage(0);
    }
}