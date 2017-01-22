package com.group1.team.autodiary.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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
import com.group1.team.autodiary.services.NotificationParser;
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

    private CallLogManager mCallLogManager;
    private List<Place> mPlaces;
    private List<Weather> mWeathers, mForecasts;
    private List<AppUsage> mUsages;
    private List<String> mNews, mPlans, mNextPlans;
    private List<Music> mMusics;
    private LabelPhoto mLabelPhoto;
    private Bitmap mFaceBitmap;

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
            long current = System.currentTimeMillis();
            long start = diaryService.getStart();
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
            new PhotoManager(getApplicationContext()).getPhoto(start, current, (bitmap, date, annotations) -> {
                mLabelPhoto = new LabelPhoto(bitmap, annotations, date);
                Log.i(TAG, "load photo");
                synchronized (mLockObject) {
                    if (++mFinishedWork == WORK_NUM)
                        sendDataToFragment();
                }
            });
            new WeatherManager(getApplicationContext()).getForecast(diaryService.getLocation(), current, current + DAY_LENGTH, forecasts -> {
                mForecasts = forecasts;
                Log.i(TAG, "load forecast");
                synchronized (mLockObject) {
                    if (++mFinishedWork == WORK_NUM)
                        sendDataToFragment();
                }
            });

            new Thread(() -> {
                PlanManager planManager = new PlanManager(getApplicationContext());
                mPlans = planManager.getPlan(start, current);
                mNextPlans = planManager.getPlan(current, current + DAY_LENGTH);
                mUsages = new AppUsageStatsManager(getApplicationContext(), start, current).getAllItems();
                if (facePhoto != null) {
                    mFaceBitmap = facePhoto.getBitmap(getApplicationContext());
                    facePhoto.deleteFile(getApplicationContext());
                }

                mCallLogManager = new CallLogManager(getApplicationContext(), start, current);

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

        viewPager = (ViewPager) findViewById(R.id.diary_viewpager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    protected void onDestroy() {
        loadingView.stop();
        super.onDestroy();
    }

    private void sendDataToFragment() {
        Log.i(TAG, "send to fragment");
        loadingView.stop();
        DiaryFragment diaryFragment = (DiaryFragment) viewPagerAdapter.instantiateItem(viewPager, 0);
        StatisticsFragment statisticsFragment = (StatisticsFragment) viewPagerAdapter.instantiateItem(viewPager, 1);
        diaryFragment.finishLoadData(this);
        statisticsFragment.finishLoadData(this);
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
}
