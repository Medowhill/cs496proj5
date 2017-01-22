package com.group1.team.autodiary.utils;

import android.content.Context;
import android.util.Log;

import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.AppUsage;
import com.group1.team.autodiary.objects.CallLog;
import com.group1.team.autodiary.objects.LabelPhoto;
import com.group1.team.autodiary.objects.Place;
import com.group1.team.autodiary.objects.Weather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryUtil {

    private Context mContext;

    public DiaryUtil(Context context) {
        this.mContext = context;
    }

    public String dateToDiary() {
        return new SimpleDateFormat(mContext.getString(R.string.diary_text_date_format), Locale.KOREA).format(new Date(System.currentTimeMillis()));
    }

    public String weatherToDiary(List<Weather> weathers, boolean today) {
        if (weathers == null || weathers.isEmpty())
            return "";

        String[] tempStrings, descriptionStrings, humidityStrings, attitudeStrings;
        tempStrings = mContext.getResources().getStringArray(R.array.diary_weather_temp);
        descriptionStrings = mContext.getResources().getStringArray(today ? R.array.diary_weather_description : R.array.diary_forecast_description);
        humidityStrings = mContext.getResources().getStringArray(R.array.diary_weather_humidity);
        attitudeStrings = mContext.getResources().getStringArray(today ? R.array.diary_weather_attitude : R.array.diary_forecast_attitude);

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
                ? String.format(mContext.getString(R.string.diary_weather_format), tempStrings[tempIndex], descriptionStrings[descriptionIndex], humidityStrings[humidityIndex], attitudeStrings[attitudeIndex])
                : String.format(mContext.getString(R.string.diary_forecast_format), tempStrings[tempIndex], descriptionStrings[descriptionIndex], attitudeStrings[attitudeIndex]);
    }

    public String placeToDiary(List<Place> places) {
        if (places == null || places.isEmpty())
            return "";

        SimpleDateFormat format = new SimpleDateFormat(mContext.getString(R.string.diary_place_time_format), Locale.KOREA);
        List<String> extra = new ArrayList<>();
        String major = "", minor = "";
        long time = 0;
        for (Place place : places) {
            long newTime = place.getTime();
            if (newTime - time > 3600 * 1000) {
                major += String.format(mContext.getString(R.string.diary_place_major_format),
                        format.format(new Date(newTime)), place.getName());
                time = newTime;
            } else
                extra.add(place.getName());
        }
        if (!extra.isEmpty()) {
            for (String str : extra)
                minor += str + ", ";
            minor = minor.substring(0, minor.length() - 2);
            minor = String.format(mContext.getString(R.string.diary_place_extra_format), minor);
        }
        major = major.substring(0, major.length() - 2);

        int attitudeIndex;
        if (places.size() <= 5)
            attitudeIndex = 0;
        else
            attitudeIndex = 1;

        return String.format(mContext.getString(R.string.diary_place_format), major, minor,
                mContext.getResources().getStringArray(R.array.diary_place_attitude)[attitudeIndex]);
    }

    public String planToDiary(List<String> plans, boolean today) {
        if (plans == null || plans.isEmpty())
            return "";

        String str = "";
        for (String plan : plans)
            str += plan + ", ";
        str = str.substring(0, str.length() - 2);
        str += mContext.getString(hasLastSound(str) ? R.string.eul_kor : R.string.leul_kor);

        int attitudeIndex;
        if (plans.size() <= 3)
            attitudeIndex = 0;
        else if (plans.size() <= 6)
            attitudeIndex = 1;
        else
            attitudeIndex = 2;

        String[] attitudes = mContext.getResources().getStringArray(today ? R.array.diary_plan_attitude : R.array.diary_plan_tomorrow_attitude);
        return String.format(mContext.getResources().getString(today ? R.string.diary_plan_format : R.string.diary_plan_tomorrow_format),
                str, attitudes[attitudeIndex]);
    }

    public String newsToDiary(List<String> news) {
        if (news == null || news.isEmpty())
            return "";

        String str = "";
        for (String news_ : news) {
            news_ = news_.replace((char) 8230, ' ');
            news_ = news_.replace("\"", "");
            news_ = news_.replaceAll("\'", "");
            int index;
            while ((index = news_.indexOf('(')) != -1) {
                int finIndex = news_.indexOf(')', index);
                if (finIndex == -1)
                    break;
                news_ = news_.substring(0, index) + news_.substring(finIndex + 1);
            }
            while ((index = news_.indexOf('[')) != -1) {
                int finIndex = news_.indexOf(']', index);
                if (finIndex == -1)
                    break;
                news_ = news_.substring(0, index) + news_.substring(finIndex + 1);
            }
            str += news_ + ", ";
        }
        str = str.substring(0, str.length() - 2);
        str += mContext.getString(hasLastSound(str) ? R.string.gwa_kor : R.string.wa_kor);

        return String.format(mContext.getString(R.string.diary_news_format), str);
    }

    public String phoneToDiary(List<CallLog> calls) {
        if (calls == null || calls.isEmpty())
            return "";

        for (CallLog call : calls) {

        }

        String str = "";
        return str;
    }

    public String usageToDiary(List<AppUsage> usages) {
        if (usages == null || usages.isEmpty())
            return "";

        Collections.sort(usages);
        long total = 0;
        for (AppUsage usage : usages)
            total += usage.getTime();

        String str = "";
        for (int i = 0; i < Math.min(3, usages.size()); i++) {
            AppUsage usage = usages.get(i);
            long time = usage.getTime();
            time /= 60000;
            if (time < 1)
                break;

            int hour = (int) time / 60;
            int min = (int) (time - hour * 60);

            str += usage.getName() + mContext.getString(hasLastSound(usage.getName()) ? R.string.eul_kor : R.string.leul_kor) + " " +
                    ((hour > 0) ? (hour + mContext.getString(R.string.hour_kor)) : "") +
                    ((hour > 0 && min > 0) ? " " : "") +
                    ((min > 0) ? (min + mContext.getString(R.string.min_kor)) : "") + ", ";
        }
        if (str.length() > 2)
            str = str.substring(0, str.length() - 2);

        int attitudeIndex;
        if (total < 2L * 3600 * 1000)
            attitudeIndex = 0;
        else if (total < 4L * 3600 * 1000)
            attitudeIndex = 1;
        else
            attitudeIndex = 2;

        total /= 60000;
        int hour = (int) total / 60;
        int min = (int) (total - hour * 60);
        return String.format(mContext.getString(R.string.diary_usage_format), str,
                ((hour > 0) ? (hour + mContext.getString(R.string.hour_kor)) : "") +
                        ((hour > 0 && min > 0) ? " " : "") +
                        ((min > 0) ? (min + mContext.getString(R.string.min_kor)) : ""),
                mContext.getResources().getStringArray(R.array.diary_usage_attitude)[attitudeIndex]);
    }

    public String labelToDiary(LabelPhoto photo) {
        String str = new SimpleDateFormat(mContext.getString(R.string.diary_label_time_format), Locale.KOREA).format(new Date(photo.getTime())) + "\n";
        List<EntityAnnotation> annotations = photo.getAnnotations();
        if (annotations != null) {
            for (EntityAnnotation annotation : annotations) {
                if (annotation != null) {
                    Log.i("cs496test", annotation.getScore() + "");
                    if (annotation.getScore() < 0.5)
                        break;
                    str += annotation.getDescription() + ", ";
                }
            }
        }

        if (str.charAt(str.length() - 1) == ' ')
            str = str.substring(0, str.length() - 2);

        return str;
    }

    private static boolean hasLastSound(String str) {
        return ((str.charAt(str.length() - 1) - 0xAC00) % 28) != 0;
    }

}
