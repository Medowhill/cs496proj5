package com.group1.team.autodiary.managers;

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

    private String mUrl, mSelection;

    public NewsManager(String url, String selection) {
        mUrl = url;
        mSelection = selection;
    }

    public void getNews(Callback callback) {
        List<String> news = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                {
                    try {
                        Document document = Jsoup.connect(mUrl).get();
                        Elements titles = document.select(mSelection);
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
}
