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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.group1.team.autodiary.R;

public class LoadingView extends SurfaceView implements SurfaceHolder.Callback {

    private static final int LOADING_PERIOD = 50, WAITING_PERIOD = 600;

    private TextThread mThread;
    private String[] mLoadings;
    private int mLoadingIndex, mLoadingLength;
    private int mWidth, mHeight;
    private boolean mStart;
    private Paint paint;

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(this);

        mLoadings = context.getResources().getStringArray(R.array.diary_text_loading);
        paint = new Paint();
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.diary_loading_textSize));
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "NanumBrush.ttf"));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread = new TextThread(getHolder());
        if (mStart) {
            mThread.mRun = true;
            mThread.start();
        }
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

    public void start() {
        mStart = true;
        if (mThread != null) {
            mThread.mRun = true;
            mThread.start();
        }
    }

    public void stop() {
        mStart = false;
        if (mThread != null) {
            mThread.mClear = true;
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mThread = null;
        }
    }

    private int getTextWidth(String str) {
        Rect bounds = new Rect();
        paint.getTextBounds(str, 0, str.length(), bounds);
        return bounds.width();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mThread == null)
            return false;

        return mStart && !mThread.mClear;
    }

    private class TextThread extends Thread {

        final SurfaceHolder mHolder;
        boolean mRun, mClear;

        TextThread(SurfaceHolder holder) {
            mHolder = holder;
        }

        @Override
        public void run() {
            Canvas canvas;
            while (mRun) {
                canvas = null;
                boolean wait = false;
                try {
                    canvas = mHolder.lockCanvas(null);
                    synchronized (mHolder) {
                        if (++mLoadingLength > mLoadings[mLoadingIndex].length()) {
                            mLoadingLength = 0;
                            wait = true;
                            if (++mLoadingIndex >= mLoadings.length)
                                mLoadingIndex = 0;
                        }
                        if (canvas != null)
                            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                        if (mClear && mLoadingLength == 0)
                            break;
                        if (canvas != null)
                            canvas.drawText(mLoadings[mLoadingIndex].substring(0, mLoadingLength), (mWidth - getTextWidth(mLoadings[mLoadingIndex])) / 2, mHeight / 2, paint);
                    }
                } finally {
                    if (canvas != null)
                        mHolder.unlockCanvasAndPost(canvas);
                }

                try {
                    Thread.sleep(wait ? WAITING_PERIOD : LOADING_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
