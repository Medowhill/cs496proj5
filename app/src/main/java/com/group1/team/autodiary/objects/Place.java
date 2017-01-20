package com.group1.team.autodiary.objects;

/**
 * Created by q on 2017-01-21.
 */

public class Place {

    private long time;
    private String name;

    public Place(long time, String name) {
        this.time = time;
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public String getName() {
        return name;
    }
}
