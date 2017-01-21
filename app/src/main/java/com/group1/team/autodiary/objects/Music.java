package com.group1.team.autodiary.objects;

/**
 * Created by q on 2017-01-21.
 */

public class Music {
    private String action;
    private String artist;
    private String album;
    private String track;

    public Music(String action, String artist, String album, String track) {
        this.action = action;
        this.artist = artist;
        this.album = album;
        this.track = track;
    }

    public String getAction() { return action; }

    public String getArtist() { return artist; }

    public String getAlbum() { return album; }

    public String getTrack() { return track; }
}
