package com.zzhoujay.richtext.spans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.text.style.LineBackgroundSpan;

/**
 * Created by benny on 1/27/18.
 */

public class RoundedBackgroundSpan implements LineBackgroundSpan {
    private final int start;
    private final int end;
    private int color;
    private GradientDrawable background;

    private final float[] RADIUS_START;

    private final float[] RADIUS_END;
    private final float[] RADIUS_START_END;

    public RoundedBackgroundSpan(int start, int end, int color, int radius) {
        this.start = start;
        this.end = end;
        this.color = color;

        this.RADIUS_START = new float[]{
                radius, radius,
                radius, radius,
                0, 0,
                0, 0
        };
        this.RADIUS_END = new float[]{
                0, 0,
                0, 0,
                radius, radius,
                radius, radius
        };

        this.RADIUS_START_END = new float[]{
                radius, radius,
                radius, radius,
                radius, radius,
                radius, radius
        };

        background = new GradientDrawable();
        background.setColor(color);
    }

    @Override
    public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
        int origColor = p.getColor();
        p.setColor(color);

        background.setBounds(left, top, right, bottom);
        if (start == this.start && end == this.end) {
            background.setCornerRadii(RADIUS_START_END);
        } else if (start == this.start) {
            background.setCornerRadii(RADIUS_START);
        } else if(end == this.end || end == this.end + 1){
            background.setCornerRadii(RADIUS_END);
        } else {
            background.setCornerRadius(0);
        }
        background.draw(c);

        p.setColor(origColor);
    }
}
