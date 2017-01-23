package com.group1.team.autodiary.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.provider.MediaStore;
import android.util.Log;

import com.group1.team.autodiary.objects.Music;

import java.util.HashMap;
import java.util.List;


public class MusicManager {

    public interface Callback {
        void callback(Music music);
    }

    private static boolean metachangedStack;
    private AudioManager audioManager;

    private Context mContext;
    private BroadcastReceiver mReceiver;

    public MusicManager(Context context) {
        mContext = context;
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        metachangedStack = false;
    }

    public void startMusicReceiver(Callback callback) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.music.metachanged");
        intentFilter.addAction("com.android.music.playstatechanged");
        intentFilter.addAction("com.android.music.playbackcomplete");
        intentFilter.addAction("com.android.music.queuechanged");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String cmd = intent.getStringExtra("command");
                String artist = intent.getStringExtra("artist");
                String album = intent.getStringExtra("album");
                String track = intent.getStringExtra("track");

                if (track != null) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }

                        Music playingMusicInfo = new Music(action, artist, album, track);

                        if (action.equalsIgnoreCase("com.android.music.metachanged")) { // click next music
                            if (metachangedStack) { // second metachange
                                metachangedStack = false;
                                callback.callback(playingMusicInfo);
                            } else // first metacheange
                                metachangedStack = true;
                        } else if (action.equalsIgnoreCase("com.android.music.playstatechanged")) { // play or pause
                            if (!audioManager.isMusicActive())
                                callback.callback(playingMusicInfo);
                        }

                        Log.d("MusicManager", "Action : " + action + " / Music On? : " + audioManager.isMusicActive() + " / Command : " + cmd);
                    }).start();
                }
            }
        };

        mContext.registerReceiver(mReceiver, intentFilter);
    }

    public void stopMusicReceiver() {
        mContext.unregisterReceiver(mReceiver);
    }

    public static String[] getMostFrequentlyPlayedMusic(List<Music> musics) {
        if (musics.size() == 0) return null;

        HashMap<String, Integer> hashMap = new HashMap<>();

        Music mostFrequentlyPlayedMusic = musics.get(0);
        int playedNum = 1;

        for (int i = 0; i < musics.size(); i++) {
            Music temp = musics.get(i);
            String key = temp.getArtist() + temp.getTrack();
            if (hashMap.containsKey(key))
                hashMap.put(key, hashMap.get(key) + 1);
            else
                hashMap.put(key, 1);
            if (playedNum < hashMap.get(key)) {
                mostFrequentlyPlayedMusic = temp;
                playedNum = hashMap.get(key);
            }
        }

        return new String[]{
                mostFrequentlyPlayedMusic.getArtist(),
                mostFrequentlyPlayedMusic.getTrack(),
                playedNum + ""
        };
    }
}