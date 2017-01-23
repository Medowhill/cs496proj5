package com.group1.team.autodiary.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.group1.team.autodiary.Fragments.DiaryFragment;
import com.group1.team.autodiary.Fragments.StatisticsFragment;
import com.group1.team.autodiary.R;
import com.group1.team.autodiary.managers.AppUsageStatsManager;
import com.group1.team.autodiary.managers.CallLogManager;
import com.group1.team.autodiary.managers.NewsManager;
import com.group1.team.autodiary.managers.PhotoManager;
import com.group1.team.autodiary.managers.PlanManager;
import com.group1.team.autodiary.managers.WeatherManager;
import com.group1.team.autodiary.objects.AppUsage;
import com.group1.team.autodiary.objects.FacePhoto;
import com.group1.team.autodiary.objects.LabelPhoto;
import com.group1.team.autodiary.objects.Music;
import com.group1.team.autodiary.objects.Place;
import com.group1.team.autodiary.objects.ViewPagerAdapter;
import com.group1.team.autodiary.objects.Weather;
import com.group1.team.autodiary.services.DiaryService;
import com.group1.team.autodiary.views.LoadingView;

import java.util.ArrayList;
import java.util.List;

public class DiaryActivity extends AppCompatActivity {

    private static final String TAG = "cs496test";
    private static int WORK_NUM = 4;
    private static final long DAY_LENGTH = 1000L * 3600 * 24;

    private LoadingView loadingView;
    private ViewPager viewPager;

    private ViewPagerAdapter viewPagerAdapter;
    private TextToSpeech mSpeech;

    private CallLogManager mCallLogManager;
    private List<Place> mPlaces;
    private List<Weather> mWeathers, mForecasts;
    private List<AppUsage> mUsages;
    private List<String> mNews, mPlans, mNextPlans;
    private List<Music> mMusics;
    private LabelPhoto mLabelPhoto;
    private Bitmap mFaceBitmap;
    private long mStart, mFinish, mBestTime;

    final private Object mLockObject = new Object();
    private int mFinishedWork = 0;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mFinishedWork = 0;
            mPlaces = new ArrayList<>();
            mWeathers = new ArrayList<>();
            mMusics = new ArrayList<>();

            DiaryService diaryService = ((DiaryService.DiaryBinder) service).getService();
            mFinish = System.currentTimeMillis();
            mStart = diaryService.getStart();
            mPlaces.addAll(diaryService.getPlaces());
            mWeathers.addAll(diaryService.getWeathers());
            mMusics.addAll(diaryService.getMusics());
            FacePhoto facePhoto = diaryService.getPhoto();
            diaryService.clearData();
            unbindService(connection);
            stopService(new Intent(getApplicationContext(), DiaryService.class));

            new NewsManager(getString(R.string.newsUrl), getString(R.string.newsSelection)).getNews(news -> {
                mNews = news;
                Log.i(TAG, "load news");
                synchronized (mLockObject) {
                    if (++mFinishedWork == WORK_NUM)
                        sendDataToFragment();
                }
            });
            new PhotoManager(getApplicationContext()).getPhoto(mStart, mFinish, (bitmap, date, annotations) -> {
                mLabelPhoto = new LabelPhoto(bitmap, annotations, date);
                Log.i(TAG, "load photo");
                synchronized (mLockObject) {
                    if (++mFinishedWork == WORK_NUM)
                        sendDataToFragment();
                }
            });
            new WeatherManager(getApplicationContext()).getForecast(diaryService.getLocation(), mFinish, mFinish + DAY_LENGTH, forecasts -> {
                mForecasts = forecasts;
                Log.i(TAG, "load forecast");
                synchronized (mLockObject) {
                    if (++mFinishedWork == WORK_NUM)
                        sendDataToFragment();
                }
            });

            new Thread(() -> {
                PlanManager planManager = new PlanManager(getApplicationContext());
                mPlans = planManager.getPlan(mStart, mFinish);
                mNextPlans = planManager.getPlan(mFinish, mFinish + DAY_LENGTH);
                mUsages = new AppUsageStatsManager(getApplicationContext()).getAppUsages(mStart, mFinish);
                if (facePhoto != null) {
                    mFaceBitmap = facePhoto.getBitmap(getApplicationContext());
                    facePhoto.deleteFile(getApplicationContext());
                    mBestTime = facePhoto.getTime();
                }

                mCallLogManager = new CallLogManager(getApplicationContext(), mStart, mFinish);

                Log.i(TAG, "load extra");
                synchronized (mLockObject) {
                    if (++mFinishedWork == WORK_NUM)
                        sendDataToFragment();
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        if (Build.VERSION.SDK_INT >= 19)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        bindService(new Intent(getApplicationContext(), DiaryService.class), connection, BIND_AUTO_CREATE);

        loadingView = (LoadingView) findViewById(R.id.diary_loading);
        loadingView.start();

        viewPager = (ViewPager) findViewById(R.id.diary_viewpager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        mSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mSpeech.speak(getString(R.string.diary_speech), TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        loadingView.stop();
        if (mSpeech != null) {
            mSpeech.stop();
            mSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mFinishedWork == WORK_NUM)
            super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.from_left, R.anim.to_right);
    }

    private void sendDataToFragment() {
        if (loadingView != null)
            loadingView.stop();
        if (viewPagerAdapter != null) {
            DiaryFragment diaryFragment = (DiaryFragment) viewPagerAdapter.instantiateItem(viewPager, 0);
            StatisticsFragment statisticsFragment = (StatisticsFragment) viewPagerAdapter.instantiateItem(viewPager, 1);
            if (diaryFragment != null)
                diaryFragment.finishLoadData(this);
            if (statisticsFragment != null)
                statisticsFragment.finishLoadData(this);
        }
    }

    public List<Place> getPlaces() {
        return mPlaces;
    }

    public List<Weather> getWeathers() {
        return mWeathers;
    }

    public List<Weather> getForecasts() {
        return mForecasts;
    }

    public List<Music> getMusics() {
        return mMusics;
    }

    public List<String> getNews() {
        return mNews;
    }

    public List<String> getPlans() {
        return mPlans;
    }

    public List<String> getNextPlans() {
        return mNextPlans;
    }

    public List<AppUsage> getUsages() {
        return mUsages;
    }

    public CallLogManager getCallLogManager() {
        return mCallLogManager;
    }

    public LabelPhoto getLabelPhoto() {
        return mLabelPhoto;
    }

    public Bitmap getFaceBitmap() {
        return mFaceBitmap;
    }

    public long getStart() {
        return mStart;
    }

    public long getFinish() {
        return mFinish;
    }

    public long getBestTime() {
        return mBestTime;
    }
}
