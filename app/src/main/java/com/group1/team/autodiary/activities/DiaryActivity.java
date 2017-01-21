package com.group1.team.autodiary.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import com.google.api.services.vision.v1.model.Property;
import com.group1.team.autodiary.HttpRequest;
import com.group1.team.autodiary.ImageRecognitionRequest;
import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.Place;
import com.group1.team.autodiary.objects.ViewPagerAdapter;
import com.group1.team.autodiary.objects.Weather;
import com.group1.team.autodiary.objects.CallLog;
import com.group1.team.autodiary.objects.Music;
import com.group1.team.autodiary.services.DiaryService;
import com.group1.team.autodiary.services.NotificationParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DiaryActivity extends AppCompatActivity {

    private static final long DAY_LENGTH = 1000L * 3600 * 24, LOADING_PERIOD = 100;

    private LinearLayout layoutBg;
    private ViewPager viewPager;
    private TextView textViewLoading;

    private ViewPagerAdapter viewPagerAdapter;

    private List<Place> mPlaces;
    private List<Weather> mWeathers, mForecasts;
    private List<String> mNews, mPlans, mNextPlans;
    private ContentResolver mContentResolver;

    private Drawable mBackground;
    private String[] mLoadings;
    private int mLoadingIndex = 0, mLoadingLength = 0;
    private boolean mLoading = true;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DiaryService diaryService = ((DiaryService.DiaryBinder) service).getService();
            mPlaces = diaryService.getPlaces();
            mWeathers = diaryService.getWeathers();
            getForecast(diaryService.getLocation());
            diaryService.clearData();

            unbindService(connection);
            stopService(new Intent(getApplicationContext(), DiaryService.class));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private final Handler bgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            layoutBg.setBackground(mBackground);
        }
    };

    private final Handler loadingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        if (Build.VERSION.SDK_INT >= 19)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        bindService(new Intent(getApplicationContext(), DiaryService.class), connection, BIND_AUTO_CREATE);

        viewPager = (ViewPager) findViewById(R.id.diary_viewpager);
        layoutBg = (LinearLayout) findViewById(R.id.diary_layout);
        textViewLoading = (TextView) findViewById(R.id.diary_textView_loading);
        mLoadings = getResources().getStringArray(R.array.diary_textView_loading);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        new Thread(() -> {
            try {
                mBackground = getResources().getDrawable(R.drawable.paper);
                bgHandler.sendEmptyMessage(0);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        });//.start();

        new Thread(() -> {
            mPlans = getPlan(getTodayStart());
            mNextPlans = getPlan(getTodayStart() + DAY_LENGTH);
        }).start();

        new Thread(() -> {
            while (mLoading) {

            }
        }).start();

        getNews();
    }

    private long getTodayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime();
    }

    private List<String> getPlan(long start) {
        List<String> plans = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
            ContentUris.appendId(eventsUriBuilder, start);
            ContentUris.appendId(eventsUriBuilder, start + DAY_LENGTH);
            Uri eventsUri = eventsUriBuilder.build();

            Cursor cursor = getContentResolver().query(eventsUri,
                    new String[]{CalendarContract.Instances.TITLE}, null, null, CalendarContract.Instances.DTSTART + " ASC");

            if (cursor != null) {
                while (cursor.moveToNext())
                    plans.add(cursor.getString(0));
                cursor.close();
            }
        }
        return plans;
    }

    private void getNews() {
        mNews = new ArrayList<>();
        new Thread(() -> {
            try {
                Document document = Jsoup.connect(getString(R.string.newsUrl)).get();
                Elements titles = document.select(".commonlist_tx_headline");
                for (Element title : titles)
                    mNews.add(title.text());
            } catch (IOException | OutOfMemoryError e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getPhoto() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            long start = getTodayStart(), end = start + DAY_LENGTH;
            Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Images.Media.DATE_TAKEN,
                            MediaStore.Images.Media.DISPLAY_NAME
                    },
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = \"Camera\" and "
                            + MediaStore.Images.Media.DATE_TAKEN + " >= " + start + " and "
                            + MediaStore.Images.Media.DATE_TAKEN + " <= " + end, null, null
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long date = cursor.getLong(0);
                    String name = cursor.getString(1);
                }
                cursor.close();
            }
        }
    }

    private void detectLabel(final Bitmap bitmap) {
        new Thread(() -> {
            for (Property property : ImageRecognitionRequest.getLabel(
                    new ImageRecognitionRequest(getApplicationContext()).request(bitmap, ImageRecognitionRequest.REQUEST_LABEL)).getProperties()) {
                property.getName();
                property.getValue();
            }
        }).start();
    }

    private void getForecast(Location location) {
        new HttpRequest(getString(R.string.weatherUrl) + "forecast?lat=" + location.getLatitude()
                + "&lon=" + location.getLongitude() + "&APPID=" + getString(R.string.weatherKey),
                in -> {
                    long start = getTodayStart() + DAY_LENGTH, end = start + DAY_LENGTH;
                    mForecasts = new ArrayList<>();
                    try {
                        JSONObject object = new JSONObject(new String(in));
                        JSONArray weathers = object.getJSONArray("list");
                        for (int i = 0; i < weathers.length(); i++) {
                            Weather weather = new Weather(weathers.getJSONObject(i));
                            if (weather.getTime() >= start) {
                                if (weather.getTime() > end)
                                    break;
                                mForecasts.add(weather);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, IOException::printStackTrace).request();
    }

    private List<CallLog> getCallLog() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {
            Cursor callLogCursor = getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI,
                    null, null, null, android.provider.CallLog.Calls.DATE + " DESC");

            int numberIndex = callLogCursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER);
            int typeIndex = callLogCursor.getColumnIndex(android.provider.CallLog.Calls.TYPE);
            int dateIndex = callLogCursor.getColumnIndex(android.provider.CallLog.Calls.DATE);
            int durationIndex = callLogCursor.getColumnIndex(android.provider.CallLog.Calls.DURATION);

            List<CallLog> callLogData = new ArrayList<>();

            if (callLogCursor.getCount() == 0)
                return callLogData;
            else {
                callLogCursor.moveToFirst();

                do {
                    String phoneNumber = callLogCursor.getString(numberIndex);
                    String callType = callLogCursor.getString(typeIndex);
                    String dir = null;
                    int dirCode = Integer.parseInt(callType);
                    switch (dirCode) {
                        case android.provider.CallLog.Calls.OUTGOING_TYPE:
                            dir = "OUTGOING";
                            break;
                        case android.provider.CallLog.Calls.INCOMING_TYPE:
                            dir = "INCOMING";
                            break;
                        case android.provider.CallLog.Calls.MISSED_TYPE:
                            dir = "MISSED";
                            break;
                    }
                    String callDate = callLogCursor.getString(dateIndex);
                    Date callDayTime = new Date(Long.valueOf(callDate));
                    String callDuration = callLogCursor.getString(durationIndex);

                    callLogData.add(new CallLog(phoneNumber, dir, callDayTime, callDuration));
                } while (callLogCursor.moveToNext());

                return callLogData;
            }
        }

        return null;
    }

    @TargetApi(21)
    private List<UsageStats> getAppUsageStats() {
        final UsageStatsManager usageStatsManager =
                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> appUsageStats =
                usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
                        System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));

        return appUsageStats;
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
}
