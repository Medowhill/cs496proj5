package com.group1.team.autodiary.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.group1.team.autodiary.HttpRequest;
import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.Place;
import com.group1.team.autodiary.objects.Weather;
import com.group1.team.autodiary.services.DiaryService;

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
import java.util.List;

public class DiaryActivity extends AppCompatActivity {

    private static final long DAY_LENGTH = 1000L * 3600 * 24;

    private List<Place> places;
    private List<Weather> weathers, forecasts;
    private List<String> news, plans, nextPlans;
    private ContentResolver mContentResolver;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DiaryService diaryService = ((DiaryService.DiaryBinder) service).getService();
            places = diaryService.getPlaces();
            weathers = diaryService.getWeathers();
            getForecast(diaryService.getLocation());
            diaryService.clearData();

            unbindService(connection);
            stopService(new Intent(getApplicationContext(), DiaryService.class));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindService(new Intent(getApplicationContext(), DiaryService.class), connection, BIND_AUTO_CREATE);

        plans = getPlan(getTodayStart());
        nextPlans = getPlan(getTodayStart() + DAY_LENGTH);
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
        new Thread(() -> {
            try {
                Document document = Jsoup.connect(getString(R.string.newsUrl)).get();
                Elements titles = document.select(".commonlist_tx_headline");
                for (Element title : titles)
                    news.add(title.text());
            } catch (IOException e) {
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

    private void getForecast(Location location) {
        new HttpRequest(getString(R.string.weatherUrl) + "forecast?lat=" + location.getLatitude()
                + "&lon=" + location.getLongitude() + "&APPID=" + getString(R.string.weatherKey),
                in -> {
                    long start = getTodayStart() + DAY_LENGTH, end = start + DAY_LENGTH;
                    forecasts = new ArrayList<>();
                    try {
                        JSONObject object = new JSONObject(new String(in));
                        JSONArray weathers = object.getJSONArray("list");
                        for (int i = 0; i < weathers.length(); i++) {
                            Weather weather = new Weather(weathers.getJSONObject(i));
                            if (weather.getTime() >= start) {
                                if (weather.getTime() > end)
                                    break;
                                forecasts.add(weather);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, IOException::printStackTrace).request();
    }
}
