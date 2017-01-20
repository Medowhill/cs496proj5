package com.group1.team.autodiary;

import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {

    private static final int GET = 0, POST = 1;

    public interface Callback {
        void callback(byte[] in);
    }

    public interface Callback2 {
        void callback(IOException exception);
    }

    private String mUrl;
    private int mType;
    private byte[] mOut;
    private Callback mCallback;
    private Callback2 mCallback2;

    public HttpRequest(@NonNull String url, @NonNull Callback callback, @NonNull Callback2 callback2) {
        mUrl = url;
        mCallback = callback;
        mCallback2 = callback2;
        mType = GET;
    }

    public HttpRequest(@NonNull String url, @NonNull Callback callback, @NonNull Callback2 callback2, @NonNull byte[] out) {
        mUrl = url;
        mCallback = callback;
        mCallback2 = callback2;
        mOut = out;
        mType = POST;
    }

    public void request() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(mUrl);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                            connection.setRequestMethod((mType == GET) ? "GET" : "POST");
                            connection.setDoOutput(mType != GET);
                            connection.setDoInput(true);
                            connection.setUseCaches(false);
                            connection.setDefaultUseCaches(false);

                            if (mType == POST) {
                                OutputStream outputStream = connection.getOutputStream();
                                outputStream.write(mOut);
                                outputStream.flush();
                                outputStream.close();
                            }

                            InputStream inputStream = connection.getInputStream();
                            ByteArrayOutputStream out = new ByteArrayOutputStream();

                            byte[] buf = new byte[1024 * 8];
                            int length;
                            while ((length = inputStream.read(buf)) != -1) {
                                out.write(buf, 0, length);
                            }
                            byte[] arr = out.toByteArray();
                            inputStream.close();

                            mCallback.callback(arr);
                        } catch (IOException e) {
                            mCallback2.callback(e);
                        }
                    }
                }
        ).start();
    }
}