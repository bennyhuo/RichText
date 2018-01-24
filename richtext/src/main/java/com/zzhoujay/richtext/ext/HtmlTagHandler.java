package com.zzhoujay.richtext.ext;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;

import com.pixplicity.htmlcompat.HtmlCompat;
import com.zzhoujay.markdown.style.MarkDownBulletSpan;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by zhou on 16-10-20.
 * 自定义标签的处理
 */
public class HtmlTagHandler implements HtmlCompat.TagHandler {

    private static final int code_color = Color.parseColor("#000000");
    private static final int code_background_color = Color.parseColor("#555555");
    private static final int h1_color = Color.parseColor("#333333");


    private Stack<Integer> stack;
    private Stack<Boolean> list;
    private int index = 0;
    private SoftReference<TextView> textViewSoftReference;

    public HtmlTagHandler(TextView textView) {
        stack = new Stack<>();
        list = new Stack<>();
        this.textViewSoftReference = new SoftReference<>(textView);
    }

    @Override
    public void handleTag(boolean opening, String tag, Attributes attributes, Editable output, XMLReader xmlReader) {
        if (opening) {
            startTag(tag, output, xmlReader);
            stack.push(output.length());
        } else {
            int len;
            if (stack.isEmpty()) {
                len = 0;
            } else {
                len = stack.pop();
            }
            reallyHandler(len, output.length(), tag.toLowerCase(), output, attributes, xmlReader);
        }
    }

    @SuppressWarnings("unused")
    private void startTag(String tag, Editable out, XMLReader reader) {
        switch (tag.toLowerCase()) {
            case "ul":
                list.push(true);
                out.append('\n');
                break;
            case "ol":
                list.push(false);
                out.append('\n');
                break;
            case "pre":
                break;
        }
    }

    @SuppressWarnings("unused")
    private void reallyHandler(int start, int end, String tag, Editable out, Attributes attributes, XMLReader reader) {
        switch (tag.toLowerCase()) {
            case "code":
            case "pre":
                Object[] spans = out.getSpans(start, end, Object.class);
                ArrayList<HtmlCodeBlockSpan.SpanInfo> spanInfos = new ArrayList<>(spans.length);
                for (Object span : spans) {
                    Log.d("span", span.toString());
                    spanInfos.add(new HtmlCodeBlockSpan.SpanInfo(span, out.getSpanStart(span), out.getSpanEnd(span)));
                }
                HtmlCodeBlockSpan cs = new HtmlCodeBlockSpan(getTextViewRealWidth(), code_background_color, code_color, start, end, attributes, spanInfos);
                out.setSpan(cs, start, end, Spanned.SPAN_PARAGRAPH);
                break;
            case "ol":
            case "ul":
                out.append('\n');
                if (!list.isEmpty())
                    list.pop();
                break;
            case "li":
                boolean isUl = list.peek();
                int i;
                if (isUl) {
                    index = 0;
                    i = -1;
                } else {
                    i = ++index;
                }
                out.append('\n');
                MarkDownBulletSpan bulletSpan = new MarkDownBulletSpan(list.size() - 1, h1_color, i);
                out.setSpan(bulletSpan, start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
        }
    }

    private int getTextViewRealWidth() {
        TextView textView = textViewSoftReference.get();
        if (textView != null) {
            return textView.getWidth() - textView.getPaddingRight() - textView.getPaddingLeft();
        }
        return 0;
    }
}
