package com.zzhoujay.richtext.ext;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;
import android.util.Log;

/**
 * Created by zhou on 16-7-2.
 * 代码块Span
 */
public class HtmlCodeBlockSpan extends ReplacementSpan implements LineHeightSpan {
    public static final String TAG = HtmlCodeBlockSpan.class.getSimpleName();
    private static final float RADIUS = 10;
    private static final float[] RADIUS_START = new float[]{
            RADIUS, RADIUS,
            RADIUS, RADIUS,
            0, 0,
            0, 0
    };

    private static final float[] RADIUS_END = new float[]{
            0, 0,
            0, 0,
            RADIUS, RADIUS,
            RADIUS, RADIUS
    };

    private static final int PADDING = 16;
    private static final float TEXT_SIZE_SCALE = 0.92f;

    private int mWidth;
    private GradientDrawable mBackground;
    private int mTextColor;
    private int start;
    private int end;

    public HtmlCodeBlockSpan(int width, int backgroundColor, int textColor, int start, int end) {
        mWidth = width;
        this.start = start;
        this.end = end;
        GradientDrawable g = new GradientDrawable();
        g.setColor(backgroundColor);
        //g.setCornerRadius(RADIUS);
        mBackground = g;
        mTextColor = textColor;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        Log.w(TAG, "getSize() called with: paint = [" + paint + "], text = [" + text.subSequence(start, end) + "], fm = [" + fm + "]");
        float size = paint.getTextSize();
        paint.setTextSize(size * TEXT_SIZE_SCALE);
        int width = (int) (paint.measureText(text, start, end) + PADDING * 2);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextSize(size);
        return width;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Log.i(TAG, "draw: canvas = [" + canvas + "], text = [" + text.subSequence(start, end)  + "], x = [" + x + "], top = [" + top + "], y = [" + y + "], bottom = [" + bottom + "], paint = [" + paint + "]");
        float size = paint.getTextSize();
        int color = paint.getColor();

        paint.setTextSize(size * TEXT_SIZE_SCALE);
        paint.setColor(mTextColor);
        paint.setTypeface(Typeface.MONOSPACE);

        mBackground.setBounds((int) x, top, (int) x + mWidth, bottom + PADDING);
        if(this.start == start) {
            mBackground.setCornerRadii(RADIUS_START);
        } else if(this.end == end){
            mBackground.setCornerRadii(RADIUS_END);
        } else {
            mBackground.setCornerRadius(0);
        }
        mBackground.draw(canvas);

        canvas.drawText(text, start, end, x + PADDING, y + PADDING, paint);

        paint.setTextSize(size);
        paint.setColor(color);
    }

    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
        Log.e(TAG, "| chooseHeight: text = " + text.subSequence(start, end) + ", spanstartv = [" + spanstartv + "], v = [" + v + "], fm = [" + fm + "]");
        fm.descent = fm.bottom + PADDING;
    }
}
