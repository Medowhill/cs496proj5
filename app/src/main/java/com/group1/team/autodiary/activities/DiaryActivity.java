package com.group1.team.autodiary.activities;

import android.app.usage.UsageStats;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import com.group1.team.autodiary.objects.CallLog;
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
    private static int WORK_NUM = 3;
    private static final long DAY_LENGTH = 1000L * 3600 * 24;

    private LoadingView loadingView;
    private ViewPager viewPager;

    private ViewPagerAdapter viewPagerAdapter;

    private List<CallLog> mCalls;
    private List<Place> mPlaces;
    private List<Weather> mWeathers, mForecasts;
    private List<String> mNews, mPlans, mNextPlans;
    private List<AppUsage> mUsages;

    private int mFinishedWork = 0;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mFinishedWork = 0;
            DiaryService diaryService = ((DiaryService.DiaryBinder) service).getService();
            long current = System.currentTimeMillis();
            long start = diaryService.getStart();
            mPlaces = diaryService.getPlaces();
            mWeathers = diaryService.getWeathers();
            diaryService.clearData();
            unbindService(connection);
            stopService(new Intent(getApplicationContext(), DiaryService.class));

            new NewsManager(getString(R.string.newsUrl), getString(R.string.newsSelection)).getNews(news -> {
                mNews = news;
                if (++mFinishedWork == WORK_NUM)
                    sendDataToFragment();
            });
            new PhotoManager(getApplicationContext()).getPhoto(current, start, (bitmap, date, properties) -> {
            });
            new WeatherManager(getApplicationContext()).getForecast(diaryService.getLocation(), current, current + DAY_LENGTH, forecasts -> {
                mForecasts = forecasts;
                if (++mFinishedWork == WORK_NUM)
                    sendDataToFragment();
            });

            new Thread(() -> {
                PlanManager planManager = new PlanManager(getApplicationContext());
                mPlans = planManager.getPlan(start, current);
                mNextPlans = planManager.getPlan(current, current + DAY_LENGTH);
                mCalls = new CallLogManager(getApplicationContext(), start, current).getAllItems();
                mUsages = new ArrayList<>();
                if (Build.VERSION.SDK_INT >= 21) {
                    List<UsageStats> temp = new AppUsageStatsManager(getApplicationContext(), start, current).getAllItems();
                    for (UsageStats stats : temp)
                        mUsages.add(new AppUsage(getApplicationContext(), stats.getPackageName(), stats.getTotalTimeInForeground()));
                }

                if (++mFinishedWork == WORK_NUM)
                    sendDataToFragment();
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
        loadingView.stop();
        DiaryFragment diaryFragment = (DiaryFragment) viewPagerAdapter.instantiateItem(viewPager, 0);
        StatisticsFragment statisticsFragment = (StatisticsFragment) viewPagerAdapter.instantiateItem(viewPager, 1);
        diaryFragment.finishLoadData(this);
        statisticsFragment.finishLoadData(this);
    }

    private void getPlayingMusicInfo() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.music.metachanged");
        intentFilter.addAction("com.android.music.playstatechanged");
        intentFilter.addAction("com.android.music.playbackcomplete");
        intentFilter.addAction("com.android.music.queuechanged");

        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //String cmd = intent.getStringExtra("command");
                String artist = intent.getStringExtra("artist");
                String album = intent.getStringExtra("album");
                String track = intent.getStringExtra("track");

                Music playingMusicInfo = new Music(action, artist, album, track);
                /*
                if (action.equals("com.android.music.metachanged"))
                    ((TextView) findViewById(R.id.playing_music_info))
                            .setText("Meta Changed; Music Info\nArtist : " + artist +"\nalbum : " + album + "\ntrack : " + track);
                else if (action.equals("com.android.music.playstatechanged"))
                    ((TextView) findViewById(R.id.playing_music_info))
                            .setText("Play State Changed; Music Info\nArtist : " + artist +"\nalbum : " + album + "\ntrack : " + track);
                else if (action.equals("com.android.music.playbackcomplete"))
                    ((TextView) findViewById(R.id.playing_music_info))
                            .setText("Play Back Complete; Music Info\nArtist : " + artist +"\nalbum : " + album + "\ntrack : " + track);
                else if (action.equals("com.android.music.queuechanged"))
                    ((TextView) findViewById(R.id.playing_music_info))
                            .setText("Queue Changed; Music Info\nArtist : " + artist +"\nalbum : " + album + "\ntrack : " + track);
                */
                // TODO : do something when get playing music info
            }
        };

        registerReceiver(mReceiver, intentFilter);
    }

    public void showNotificationData() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotificationParser.GET_NOTIFICATION);

        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO : do something
                Log.d("DiaryActivity", "Get Notification!");
            }
        };

        registerReceiver(mReceiver, intentFilter);
    }

    public List<CallLog> getCalls() {
        return mCalls;
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
}
