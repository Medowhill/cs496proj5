package com.group1.team.autodiary.objects;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by q on 2017-01-21.
 */

public class Weather {

    private static final double TEMP_OFFSET = 273.15;

    private String description = "";
    private double temperature, wind, volume;
    private long time;
    private int clouds, humidity;

    public Weather(JSONObject object) {
        try {
            time = object.getLong("dt") * 1000;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONObject main = object.getJSONObject("main");
            temperature = main.getDouble("temp") - TEMP_OFFSET;
            humidity = main.getInt("humidity");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            description = ((JSONObject) object.getJSONArray("weather").get(0)).getString("main");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            clouds = object.getJSONObject("clouds").getInt("all");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            wind = object.getJSONObject("wind").getDouble("speed");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (description.equals("Snow"))
                volume = object.getJSONObject("snow").getDouble("3h");
            else if (description.equals("Rain"))
                volume = object.getJSONObject("rain").getDouble("3h");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getDescription() {
        return description;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getWind() {
        return wind;
    }

    public double getVolume() {
        return volume;
    }

    public long getTime() {
        return time;
    }

    public int getClouds() {
        return clouds;
    }

    public int getHumidity() {
        return humidity;
    }
}
