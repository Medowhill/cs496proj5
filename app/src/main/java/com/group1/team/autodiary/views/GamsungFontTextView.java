package com.group1.team.autodiary.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by q on 2017-01-21.
 */

public class GamsungFontTextView extends TextView {
    static Typeface typeface = null;

    public GamsungFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (typeface == null)
            typeface = Typeface.createFromAsset(context.getAssets(), "SDMiSaeng.ttf");
        setTypeface(Typeface.createFromAsset(context.getAssets(), "SDMiSaeng.ttf"));
        setTextSize(18);
    }

    public GamsungFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (typeface == null)
            typeface = Typeface.createFromAsset(context.getAssets(), "SDMiSaeng.ttf");
        setTypeface(Typeface.createFromAsset(context.getAssets(), "SDMiSaeng.ttf"));
        setTextSize(18);
    }

    public GamsungFontTextView(Context context) {
        super(context);
        if (typeface == null)
            typeface = Typeface.createFromAsset(context.getAssets(), "SDMiSaeng.ttf");
        setTypeface(Typeface.createFromAsset(context.getAssets(), "SDMiSaeng.ttf"));
        setTextSize(18);
    }
}
