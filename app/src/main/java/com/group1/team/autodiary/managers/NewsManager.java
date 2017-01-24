package com.group1.team.autodiary.managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.group1.team.autodiary.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewsManager {

    public interface Callback {
        void callback(List<String> news);
    }

    private Context mContext;

    public NewsManager(Context context) {
        mContext = context;
    }

    public void getNews(Callback callback) {
        List<String> news = new ArrayList<>();

        if (!isConnected()) {
            callback.callback(news);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                {
                    try {
                        Document document = Jsoup.connect(mContext.getString(R.string.newsUrl)).get();
                        Elements titles = document.select(mContext.getString(R.string.newsSelection));
                        for (int i = 0; i < titles.size(); i += 3)
                            news.add(titles.get(i).text());
                        callback.callback(news);
                    } catch (IOException | OutOfMemoryError e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
