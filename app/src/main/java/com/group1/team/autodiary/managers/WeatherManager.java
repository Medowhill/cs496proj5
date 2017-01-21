package com.group1.team.autodiary.managers;

import android.content.Context;
import android.location.Location;

import com.group1.team.autodiary.utils.HttpRequest;
import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WeatherManager {

    public interface Callback {
        void callback(List<Weather> forecasts);
    }

    private Context mContext;

    public WeatherManager(Context context) {
        this.mContext = context;
    }

    public void getForecast(Location location, long start, long end, Callback callback) {
        List<Weather> forecasts = new ArrayList<>();

        if (location == null) {
            callback.callback(forecasts);
            return;
        }

        new HttpRequest(mContext.getString(R.string.weatherUrl) + "forecast?lat=" + location.getLatitude()
                + "&lon=" + location.getLongitude() + "&APPID=" + mContext.getString(R.string.weatherKey),
                in -> {
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
                        callback.callback(forecasts);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.callback(forecasts);
                    }
                }, IOException::printStackTrace).request();
    }
}
