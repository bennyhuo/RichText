package com.zzhoujay.richtext.ext;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

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

    private int singleLineHeight;

    private HashMap<Integer, ArrayList<LineRange>> lineRangesMap = new HashMap();

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
        Log.w(TAG, "getSize() text = [" + text.subSequence(start, end) + "], fm = [" + fm + "]");
        float size = paint.getTextSize();
        paint.setTextSize(size * TEXT_SIZE_SCALE);
        paint.setTypeface(Typeface.MONOSPACE);

        measureTextLine(text, start, end, paint);

        Log.d(TAG, lineRangesMap.toString());
        paint.setTextSize(size);
        return mWidth;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Log.i(TAG, "draw: text = " + text.subSequence(start, end)  + ", x = [" + x + "], top = [" + top + "], y = [" + y + "], bottom = [" + bottom + "]");
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

        ArrayList<LineRange> lineRanges = lineRangesMap.get(start);
        for (int i = 0; i < lineRanges.size(); i++) {
            canvas.drawText(text, lineRanges.get(i).start, lineRanges.get(i).end, x + PADDING * 2, y + i * singleLineHeight + PADDING, paint);
        }

        paint.setTextSize(size);
        paint.setColor(color);
    }

    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
        Log.e(TAG, "| chooseHeight: text = " + text.subSequence(start, end) + ", spanstartv = [" + spanstartv + "], v = [" + v + "], fm = [" + fm + "]");
        if(singleLineHeight == 0){
            singleLineHeight = fm.bottom - fm.top;
        }
        fm.bottom = fm.top + singleLineHeight * lineRangesMap.get(start).size();
        fm.descent = fm.bottom + PADDING;
    }

    private int getTextInLineLen(CharSequence text, int start, int end, Paint paint) {
        int e = start;
        while (paint.measureText(text, start, e) < mWidth - PADDING * 2) {
            e++;
            if (e > end) {
                break;
            }
        }
        return e - 1;
    }

    private int getTextInLineLenInRange(CharSequence text, int start, int end, int rs, int re, Paint paint) {
        int e = rs;
        if (rs > end) {
            return end;
        }
        while (paint.measureText(text, start, e) < mWidth - PADDING * 2) {
            e++;
            if (e > end || e > re) {
                break;
            }
        }
        return e - 1;
    }

    private void measureTextLine(CharSequence text, int start, int end, Paint paint) {
        int l = getTextInLineLen(text, start, end, paint);
        int count = l;
        ArrayList<LineRange> lineRanges = this.lineRangesMap.get(start);
        if(lineRanges == null){
            lineRanges = new ArrayList<>();
            this.lineRangesMap.put(start, lineRanges);
        }
        lineRanges.add(new LineRange(start, l));
        while (l < end) {
            int temp = l;
            l = getTextInLineLenInRange(text, l, end, l + count - 4, l + count + 4, paint);
            count = l - temp;
            lineRanges.add(new LineRange(temp, l));
        }
    }

    static class LineRange{
        final int start;
        final int end;

        LineRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "LineRange{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }
}
