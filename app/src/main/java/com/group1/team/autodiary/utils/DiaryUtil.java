package com.group1.team.autodiary.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;

import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.group1.team.autodiary.R;
import com.group1.team.autodiary.managers.CallLogManager;
import com.group1.team.autodiary.managers.MusicManager;
import com.group1.team.autodiary.objects.AppUsage;
import com.group1.team.autodiary.objects.LabelPhoto;
import com.group1.team.autodiary.objects.Music;
import com.group1.team.autodiary.objects.Place;
import com.group1.team.autodiary.objects.Weather;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryUtil {

    private Context mContext;

    private String wa, gwa, leul, eul, yi;

    public DiaryUtil(Context context) {
        this.mContext = context;

        wa = context.getString(R.string.wa_kor);
        gwa = context.getString(R.string.gwa_kor);
        leul = context.getString(R.string.leul_kor);
        eul = context.getString(R.string.eul_kor);
        yi = context.getString(R.string.yi_kor);
    }

    public String dateToDiary(long time) {
        return new SimpleDateFormat(mContext.getString(R.string.diary_text_date_format), Locale.KOREA).format(new Date(time));
    }

    public String wakeUpToDiary(long time) {
        return new SimpleDateFormat(mContext.getString(R.string.diary_wakeup_date_format), Locale.KOREA).format(new Date(time));
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
        if (places == null)
            return "";

        SimpleDateFormat format = new SimpleDateFormat(mContext.getString(R.string.diary_place_time_format), Locale.KOREA);
        String major = "", minor = "";

        int i;
        for (i = 0; i < places.size(); i++) {
            Place place = places.get(i);
            long duration = place.getDuration();
            if (duration < 60 * 60 * 1000)
                break;

            String name = place.getName();
            boolean redundancy = false;
            for (int j = 0; j < i; j++) {
                if (places.get(j).getName().equals(name)) {
                    redundancy = true;
                    break;
                }
            }
            if (redundancy)
                continue;

            major += String.format(mContext.getString(R.string.diary_place_major_detail_format),
                    format.format(new Date(place.getTime())), name);
        }

        int count = 0;
        for (; i < places.size() && count < 3; i++) {
            Place place = places.get(i);
            boolean redundancy = false;
            for (int j = 0; j < i; j++) {
                if (places.get(j).getName().equals(place.getName())) {
                    redundancy = true;
                    break;
                }
            }
            if (redundancy)
                continue;

            minor += place.getName() + ", ";
            count++;
        }

        if (major.length() > 2)
            major = major.substring(0, major.length() - 2);
        if (minor.length() > 2)
            minor = minor.substring(0, minor.length() - 2);

        int attitudeIndex;
        if (places.size() <= 5)
            attitudeIndex = 0;
        else
            attitudeIndex = 1;

        return ((major.length() > 0) ? String.format(mContext.getString(R.string.diary_place_format), major) : "") +
                ((major.length() > 0 && minor.length() > 0) ? mContext.getString(R.string.diary_place_bothExist) : "") +
                ((minor.length() > 0) ? String.format(mContext.getString(R.string.diary_place_format), minor) : "") +
                String.format(mContext.getString(R.string.diary_place_attitude_format), mContext.getResources().getStringArray(R.array.diary_place_attitude)[attitudeIndex]);
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
        str += hasLastSound(str) ? gwa : wa;

        return String.format(mContext.getString(R.string.diary_news_format), str);
    }

    public String phoneToDiary(CallLogManager manager) {
        String str1 = "", str2 = "";

        String[] callInfo = manager.getLongestCallPerson();
        if (callInfo != null && !callInfo[1].equals("0")) {
            String name = callInfo[0] == null ? mContext.getString(R.string.diary_phone_unknown) : callInfo[0];
            int duration = Integer.parseInt(callInfo[1]);
            duration /= 60;
            if (duration == 0)
                duration = 1;
            str1 = String.format(mContext.getString(R.string.diary_phone_format),
                    name + (hasLastSound(name) ? gwa : wa), duration, pick(R.string.diary_phone_attitude));
        }

        String missedInfo = manager.getMissedCall();
        if (missedInfo != null)
            str2 = String.format(mContext.getString(R.string.diary_phone_missed_format), missedInfo, pick(R.string.diary_phone_missed_attitude));

        return str1 + str2;
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
            if (time < 60000)
                break;

            str += usage.getName() + (hasLastSound(usage.getName()) ? eul : leul) + " " +
                    millisecondToString(time) + ", ";
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

        return String.format(mContext.getString(R.string.diary_usage_format), str, millisecondToString(total),
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

    public String musicToDiary(List<Music> musics) {
        if (musics == null || musics.isEmpty())
            return "";
        String[] music = MusicManager.getMostFrequentlyPlayedMusic(musics);
        String singer = music[0], track = music[1];
        if (singer == null)
            singer = mContext.getString(R.string.diary_music_unknown_singer);
        if (track == null)
            track = mContext.getString(R.string.diary_music_unknown_music);
        return String.format(mContext.getString(R.string.diary_music_format),
                singer, track + (hasLastSound(track) ? yi : ""), pick(R.string.diary_music_attitude));
    }

    public String endToDiary() {
        return pick(R.string.diary_end);
    }

    private static boolean hasLastSound(String str) {
        return ((str.charAt(str.length() - 1) - 0xAC00) % 28) != 0;
    }

    private String millisecondToString(long time) {
        time /= 60000;
        int hour = (int) time / 60;
        int min = (int) (time - hour * 60);
        return ((hour > 0) ? (hour + mContext.getString(R.string.hour_kor)) : "") +
                ((hour > 0 && min > 0) ? " " : "") +
                ((min > 0) ? (min + mContext.getString(R.string.min_kor)) : "");
    }

    private String pick(@StringRes int id) {
        return pick(mContext.getString(id));
    }

    private String pick(@NonNull String str) {
        String[] arr = str.split("=");
        int rand = (int) (Math.random() * arr.length);
        return arr[rand];
    }

}
