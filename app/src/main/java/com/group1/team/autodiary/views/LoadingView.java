package com.group1.team.autodiary.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.group1.team.autodiary.R;

public class LoadingView extends SurfaceView implements SurfaceHolder.Callback {

    private static final int LOADING_PERIOD = 200;

    private TextThread mThread;
    private String[] mLoadings;
    private int mLoadingIndex, mLoadingLength;
    private int mWidth, mHeight;
    private Paint paint;

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(this);

        mLoadings = context.getResources().getStringArray(R.array.diary_textView_loading);
        paint = new Paint();
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.diary_loading_textSize));
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "NanumBrush.ttf"));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread = new TextThread(getHolder());
        mThread.mRun = true;
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    public void stop() {
        if (mThread != null)
            mThread.mClear = true;
    }

    private int getTextWidth(String str) {
        Rect bounds = new Rect();
        paint.getTextBounds(str, 0, str.length(), bounds);
        return bounds.width();
    }

    private class TextThread extends Thread {

        SurfaceHolder mHolder;
        boolean mRun, mClear;

        TextThread(SurfaceHolder holder) {
            mHolder = holder;
        }

        @Override
        public void run() {
            Canvas canvas;
            while (mRun) {
                canvas = null;
                try {
                    canvas = mHolder.lockCanvas(null);
                    synchronized (mHolder) {
                        if (++mLoadingLength > mLoadings[mLoadingIndex].length()) {
                            mLoadingLength = 0;
                            if (++mLoadingIndex >= mLoadings.length)
                                mLoadingIndex = 0;
                        }
                        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                        if (mClear)
                            break;
                        canvas.drawText(mLoadings[mLoadingIndex].substring(0, mLoadingLength), (mWidth - getTextWidth(mLoadings[mLoadingIndex])) / 2, mHeight / 2, paint);
                    }
                } finally {
                    if (canvas != null)
                        mHolder.unlockCanvasAndPost(canvas);
                }

                try {
                    Thread.sleep(LOADING_PERIOD);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
