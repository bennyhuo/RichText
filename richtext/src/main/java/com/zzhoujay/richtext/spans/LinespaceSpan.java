package com.zzhoujay.richtext.spans;

import android.graphics.Paint;
import android.text.style.LineHeightSpan;

/**
 * Created by benny on 1/27/18.
 */

public class LinespaceSpan implements LineHeightSpan {

    private final int linespace;


    Paint.FontMetricsInt savedFm = null;

    public LinespaceSpan(int linespace) {
        this.linespace = linespace;
    }

    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
        if (savedFm == null) {
            savedFm = new Paint.FontMetricsInt();
            savedFm.top = fm.top;
            savedFm.ascent = fm.ascent;
            savedFm.descent = fm.descent;
            savedFm.bottom = fm.bottom;
        }
        fm.top = savedFm.top - linespace / 2;
        fm.ascent = savedFm.ascent - linespace / 2;
        fm.bottom = savedFm.bottom + linespace / 2;
        fm.descent = savedFm.descent + linespace / 2;
    }
}
