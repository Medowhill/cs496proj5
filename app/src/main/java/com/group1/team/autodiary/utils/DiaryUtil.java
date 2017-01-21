package com.group1.team.autodiary.utils;

import android.content.Context;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.AppUsage;
import com.group1.team.autodiary.objects.CallLog;
import com.group1.team.autodiary.objects.Place;
import com.group1.team.autodiary.objects.Weather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DiaryUtil {

    public static String weatherToDiary(Context context, List<Weather> weathers, boolean today) {
        if (weathers == null || weathers.isEmpty())
            return "";

        String[] tempStrings, descriptionStrings, humidityStrings, attitudeStrings;
        tempStrings = context.getResources().getStringArray(R.array.diary_weather_temp);
        descriptionStrings = context.getResources().getStringArray(today ? R.array.diary_weather_description : R.array.diary_forecast_description);
        humidityStrings = context.getResources().getStringArray(R.array.diary_weather_humidity);
        attitudeStrings = context.getResources().getStringArray(today ? R.array.diary_weather_attitude : R.array.diary_forecast_attitude);

        double temp = 0;
        int humidity = 0, cloud = 0;
        boolean snow = false, rain = false, haze = false;
        for (Weather weather : weathers) {
            temp += weather.getTemperature();
            humidity += weather.getHumidity();
            cloud += weather.getClouds();
            if (weather.getDescription().equals("Rain"))
                rain = true;
            else if (weather.getDescription().equals("Snow"))
                snow = true;
            else if (weather.getDescription().equals("Haze"))
                haze = true;
        }
        temp /= weathers.size();
        humidity /= weathers.size();
        cloud /= weathers.size();

        int tempIndex, descriptionIndex, humidityIndex, attitudeIndex;

        if (temp < 0)
            tempIndex = 0;
        else if (temp < 10)
            tempIndex = 1;
        else if (temp < 20)
            tempIndex = 2;
        else if (temp < 30)
            tempIndex = 3;
        else
            tempIndex = 4;

        if (snow && rain)
            descriptionIndex = 0;
        else if (snow)
            descriptionIndex = 1;
        else if (rain)
            descriptionIndex = 2;
        else if (haze)
            descriptionIndex = 3;
        else if (cloud > 50)
            descriptionIndex = 4;
        else
            descriptionIndex = 5;

        if (humidity < 25)
            humidityIndex = 0;
        else if (humidity < 75)
            humidityIndex = 1;
        else
            humidityIndex = 2;

        if (temp < 10 || 25 < temp || snow || rain)
            attitudeIndex = 0;
        else
            attitudeIndex = 1;

        return today
                ? String.format(context.getString(R.string.diary_weather_format), tempStrings[tempIndex], descriptionStrings[descriptionIndex], humidityStrings[humidityIndex], attitudeStrings[attitudeIndex])
                : String.format(context.getString(R.string.diary_forecast_format), tempStrings[tempIndex], descriptionStrings[descriptionIndex], attitudeStrings[attitudeIndex]);
    }

    public static String placeToDiary(Context context, List<Place> places) {
        if (places == null || places.isEmpty())
            return "";

        List<String> extra = new ArrayList<>();
        String major = "", minor = "";
        long time = 0;
        for (Place place : places) {
            long newTime = place.getTime();
            if (newTime - time > 3600 * 1000) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date(newTime));
                major += String.format(context.getString(R.string.diary_place_major_format),
                        context.getString(calendar.get(Calendar.HOUR_OF_DAY) < 12 ? R.string.am : R.string.pm), calendar.get(Calendar.HOUR), place.getName());
                time = newTime;
            } else
                extra.add(place.getName());
        }
        if (!extra.isEmpty()) {
            for (String str : extra)
                minor += str + ", ";
            minor = minor.substring(0, minor.length() - 2);
            minor = String.format(context.getString(R.string.diary_place_extra_format), minor);
        }
        major = major.substring(0, major.length() - 2);

        int attitudeIndex;
        if (places.size() <= 5)
            attitudeIndex = 0;
        else
            attitudeIndex = 1;

        return String.format(context.getString(R.string.diary_place_format), major, minor,
                context.getResources().getStringArray(R.array.diary_place_attitude)[attitudeIndex]);
    }

    public static String planToDiary(Context context, List<String> plans, boolean today) {
        if (plans == null || plans.isEmpty())
            return "";

        String str = "";
        for (String plan : plans)
            str += plan + ", ";
        str = str.substring(0, str.length() - 2);
        str += context.getString(hasLastSound(str) ? R.string.eul : R.string.leul);

        int attitudeIndex;
        if (plans.size() <= 3)
            attitudeIndex = 0;
        else if (plans.size() <= 6)
            attitudeIndex = 1;
        else
            attitudeIndex = 2;

        String[] attitudes = context.getResources().getStringArray(today ? R.array.diary_plan_attitude : R.array.diary_plan_tomorrow_attitude);
        return String.format(context.getResources().getString(today ? R.string.diary_plan_format : R.string.diary_plan_tomorrow_format),
                str, attitudes[attitudeIndex]);
    }

    public static String newsToDiary(Context context, List<String> news) {
        if (news == null || news.isEmpty())
            return "";

        String str = "";
        for (String news_ : news)
            str += news_ + ", ";
        str = str.substring(0, str.length() - 2);
        str += context.getString(hasLastSound(str) ? R.string.gwa : R.string.wa);

        return String.format(context.getString(R.string.diary_news_format), str);
    }

    public static String phoneToDiary(Context context, List<CallLog> calls) {
        if (calls == null || calls.isEmpty())
            return "";

        for (CallLog call : calls) {

        }

        String str = "";
        return str;
    }

    public static String usageToDiary(Context context, List<AppUsage> usages) {
        if (usages == null || usages.isEmpty())
            return "";

        Collections.sort(usages);
        long total = 0;
        for (AppUsage usage : usages)
            total += usage.getTime();

        String str = "";
        SimpleDateFormat format = new SimpleDateFormat(context.getString(R.string.diary_usage_time_format));
        for (int i = 0; i < Math.min(3, usages.size()); i++) {
            AppUsage usage = usages.get(i);
            str += usage.getName() + context.getString(hasLastSound(usage.getName()) ? R.string.eul : R.string.leul) + " " + format.format(usage.getTime()) + ", ";
        }
        str = str.substring(0, str.length() - 2);

        int attitudeIndex;
        if (total < 2L * 3600 * 1000)
            attitudeIndex = 0;
        else if (total < 4L * 3600 * 1000)
            attitudeIndex = 1;
        else
            attitudeIndex = 2;

        return String.format(context.getString(R.string.diary_usage_format), str, format.format(total), context.getResources().getStringArray(R.array.diary_usage_attitude)[attitudeIndex]);
    }

    private static boolean hasLastSound(String str) {
        return ((str.charAt(str.length() - 1) - 0xAC00) % 28) != 0;
    }

}
