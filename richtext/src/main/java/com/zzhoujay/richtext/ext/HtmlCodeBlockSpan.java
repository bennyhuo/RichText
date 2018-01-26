package com.zzhoujay.richtext.ext;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;
import android.util.Log;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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

    private String tagName;
    private int mWidth;
    private GradientDrawable mBackground;
    private int mTextColor;
    private int start;
    private int end;
    private Attributes attributes;
    private ArrayList<SpanInfo> spanInfos;

    private Comparator<SpanInfo> spanInfoComparator = new Comparator<SpanInfo>() {
        @Override
        public int compare(SpanInfo o1, SpanInfo o2) {
            return o1.start - o2.start;
        }
    };

    private int singleLineHeight;

    private HashMap<Integer, ArrayList<LineRange>> lineRangesMap = new HashMap();

    public HtmlCodeBlockSpan(String tagName, int width, int backgroundColor, int textColor, int start, int end, Attributes attributes, ArrayList<SpanInfo> spanInfos) {
        this.tagName = tagName;
        mWidth = width;
        this.start = start;
        this.end = end;
        this.attributes = attributes;
        this.spanInfos = spanInfos;
        Collections.sort(this.spanInfos, spanInfoComparator);
        GradientDrawable g = new GradientDrawable();
        g.setColor(backgroundColor);
        //g.setCornerRadius(RADIUS);
        mBackground = g;
        mTextColor = textColor;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        Log.d(TAG, "getSize() :" + tagName + "; text: " + text.subSequence(start, end));

        textPaint.set(paint);
        textPaint.setTextSize(paint.getTextSize() * TEXT_SIZE_SCALE);
        textPaint.setTypeface(Typeface.MONOSPACE);
        saveTextPaint();

        int lineWidth = measureTextLine(text, start, end);
        Log.d(TAG, "lineWidth = " + lineWidth);
        //Log.d(TAG, lineRangesMap.toString());
        return Math.min(lineWidth, mWidth);
    }

    private TextPaint textPaint = new TextPaint();
    private TextPaint savedTextPaint = new TextPaint();



    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Log.d(TAG, "draw() text = [" + text.subSequence(start, end) + "], x=[" + x +"], top = [" + top + "], y = [" + y + "], bottom = [" + bottom + "]");
        //drawBackground(canvas, start, end, (int) x, top, bottom);

        textPaint.set(paint);
        textPaint.setTextSize(paint.getTextSize() * TEXT_SIZE_SCALE);
        textPaint.setTypeface(Typeface.MONOSPACE);
        saveTextPaint();

        ArrayList<LineRange> lineRanges = lineRangesMap.get(start);
        for (int i = 0; i < lineRanges.size(); i++) {
            LineRange lineRange = lineRanges.get(i);
            List<SpanInfo> filteredSpanInfos = lineRange.spanInfos;
//            int lastEnd;
            float lastX = x;
//            if(filteredSpanInfos.isEmpty()){
//                lastEnd = lineRange.end;
//            } else {
//                lastEnd = Math.max(filteredSpanInfos.get(0).start, lineRange.start);
//            }
//            canvas.drawText(text, lineRange.start, lastEnd, lastX + PADDING, y + i * singleLineHeight + PADDING, textPaint);
//            lastX += textPaint.measureText(text, lineRange.start, lastEnd);
//            for (SpanInfo filteredSpanInfo : filteredSpanInfos) {
//                if(filteredSpanInfo.span instanceof ForegroundColorSpan){
//                    if(lastEnd < filteredSpanInfo.start){
//                        restoreTextPaint();
//                        canvas.drawText(text, lastEnd, filteredSpanInfo.start, lastX + PADDING, y + i * singleLineHeight + PADDING, textPaint);
//                        lastX += textPaint.measureText(text, lastEnd, filteredSpanInfo.start);
//                    }
//                    filteredSpanInfo.span.updateDrawState(textPaint);
//
//                    int thisStart = Math.max(lastEnd, filteredSpanInfo.start);
//                    int thisEnd = Math.min(filteredSpanInfo.end, lineRange.end);
//                    canvas.drawText(text, thisStart, thisEnd, lastX + PADDING, y + i * singleLineHeight + PADDING, textPaint);
//                    lastX += textPaint.measureText(text, thisStart, thisEnd);
//                    lastEnd = filteredSpanInfo.end;
//                }
//            }
//            if (lastEnd < lineRange.end){
//                restoreTextPaint();
//                canvas.drawText(text, lastEnd, lineRange.end, lastX + PADDING, y + i * singleLineHeight + PADDING, textPaint);
//            }

            int lastEnd = lineRange.start;
            int thisEnd;
            for (SpanInfo spanInfo : filteredSpanInfos) {
                //测量 span 之前的一段
                restoreTextPaint();
                thisEnd = Math.min(Math.max(spanInfo.start, lastEnd), lineRange.end);
                if (thisEnd != lastEnd) {
                    canvas.drawText(text, lastEnd, thisEnd, lastX + PADDING, y + i * singleLineHeight + PADDING, textPaint);
                    lastX += textPaint.measureText(text, lastEnd, thisEnd);
                    lastEnd = thisEnd;
                }

                thisEnd = Math.max(Math.min(spanInfo.end, lineRange.end), lastEnd);
                if (thisEnd != lastEnd) {
                    spanInfo.span.updateDrawState(textPaint);
                    canvas.drawText(text, lastEnd, thisEnd, lastX + PADDING, y + i * singleLineHeight + PADDING, textPaint);
                    lastX += textPaint.measureText(text, lastEnd, thisEnd);
                    lastEnd = thisEnd;
                }

                if (lastEnd >= lineRange.end) {
                    break;
                }
            }

            if (lastEnd < lineRange.end){
                restoreTextPaint();
                canvas.drawText(text, lastEnd, lineRange.end, lastX + PADDING, y + i * singleLineHeight + PADDING, textPaint);
            }
        }
    }

    private void saveTextPaint() {
        savedTextPaint.set(textPaint);
    }

    private void restoreTextPaint() {
        textPaint.set(savedTextPaint);
    }

    private void drawBackground(Canvas canvas, int start, int end, int x, int top, int bottom) {
        mBackground.setBounds(x, top, x + mWidth, bottom + PADDING);
        if(this.start == start) {
            mBackground.setCornerRadii(RADIUS_START);
        } else if(this.end == end){
            mBackground.setCornerRadii(RADIUS_END);
        } else {
            mBackground.setCornerRadius(0);
        }
        mBackground.draw(canvas);
    }

    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
        Log.d(TAG, "chooseHeight text = [" + text.subSequence(start, end) + "], spanstartv = [" + spanstartv + "], v = [" + v + "]");
        Log.d(TAG, "chooseHeight: " + tagName);
        if(singleLineHeight == 0){
            singleLineHeight = fm.bottom - fm.top;
        }
        fm.bottom = fm.top + singleLineHeight * lineRangesMap.get(start).size();
        fm.descent = fm.bottom + PADDING;
    }

    private int measureText(CharSequence text, int start, int end, ArrayList<SpanInfo> spanInfos) {
        int lastEnd = start;
        int thisEnd;
        int length = 0;
        for (SpanInfo spanInfo : spanInfos) {
            //测量 span 之前的一段
            restoreTextPaint();
            thisEnd = Math.min(Math.max(spanInfo.start, lastEnd), end);
            if (thisEnd != lastEnd) {
                length += textPaint.measureText(text, lastEnd, thisEnd);
                lastEnd = thisEnd;
            }

            thisEnd = Math.max(Math.min(spanInfo.end, end), lastEnd);
            if (thisEnd != lastEnd) {
                spanInfo.span.updateDrawState(textPaint);
                length += textPaint.measureText(text, lastEnd, thisEnd);
                lastEnd = thisEnd;
            }

            if (lastEnd >= end) {
                break;
            }
        }

        if (lastEnd < end) {
            restoreTextPaint();
            length += textPaint.measureText(text, lastEnd, end);
        }

        return length;
    }

    private int[] getTextInLineLen(CharSequence text, int start, int end, ArrayList<SpanInfo> spanInfos) {
        int measuredEnd = start;
        int width;
        while ((width = measureText(text, start, measuredEnd, spanInfos)) <= mWidth - PADDING * 2) {
            measuredEnd++;
            if (measuredEnd > end) {
                break;
            }
        }
        return new int[]{measuredEnd - 1, width};
    }

    private int measureTextLine(CharSequence text, int start, int end) {

        ArrayList<SpanInfo> filteredSpanInfos = new ArrayList<>();
        for (SpanInfo spanInfo : spanInfos) {
            if ((spanInfo.start >= start && spanInfo.start < end) ||
                    (spanInfo.end > start && spanInfo.end < end)) {
                filteredSpanInfos.add(spanInfo);
            }
        }

        int[] result = getTextInLineLen(text, start, end, filteredSpanInfos);
        ArrayList<LineRange> lineRanges = this.lineRangesMap.get(start);
        if(lineRanges == null){
            lineRanges = new ArrayList<>();
            this.lineRangesMap.put(start, lineRanges);
        }
        lineRanges.clear();
        lineRanges.add(new LineRange(start, result[0], filteredSpanInfos));
        int lastEnd = result[0];
        int width = result[1];
        while (lastEnd < end) {
            int[] ret = getTextInLineLen(text, lastEnd, end, filteredSpanInfos);
            lineRanges.add(new LineRange(lastEnd, ret[0], filteredSpanInfos));
            lastEnd = ret[0];
            width += ret[1];
        }

        return width;
    }

    static class LineRange{
        final int start;
        final int end;
        final List<SpanInfo> spanInfos;

        LineRange(int start, int end, List<SpanInfo> spanInfos) {
            this.start = start;
            this.end = end;
            ArrayList<SpanInfo> filtered = new ArrayList<>();
            for (SpanInfo spanInfo : spanInfos) {
                if ((spanInfo.start >= start && spanInfo.start < end) ||
                        (spanInfo.end > start && spanInfo.end < end) ||
                        (spanInfo.start < start && spanInfo.end >= end)) {
                    filtered.add(spanInfo);
                }
            }
            this.spanInfos = filtered;
        }

        @Override
        public String toString() {
            return "LineRange{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

    public static class SpanInfo {
        public final CharacterStyle span;
        public final int start;
        public final int end;

        public SpanInfo(CharacterStyle span, int start, int end) {
            this.span = span;
            this.start = start;
            this.end = end;
        }
    }
}
