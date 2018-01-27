package com.zzhoujay.richtext.ext;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.widget.TextView;

import com.pixplicity.htmlcompat.HtmlCompat;
import com.zzhoujay.markdown.style.MarkDownBulletSpan;
import com.zzhoujay.richtext.spans.DumSpan;
import com.zzhoujay.richtext.spans.LinespaceSpan;
import com.zzhoujay.richtext.spans.RoundedBackgroundSpan;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import java.lang.ref.SoftReference;
import java.util.Stack;

/**
 * Created by zhou on 16-10-20.
 * 自定义标签的处理
 */
public class HtmlTagHandler implements HtmlCompat.TagHandler {

    private static final int code_color = Color.parseColor("#000000");
    private static final int code_background_color = Color.parseColor("#ffffffff");
    private static final int h1_color = Color.parseColor("#333333");

    private static final int PADDING = 16;

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
        SWITCH:
        switch (tag.toLowerCase()) {
            case "code":
            case "pre":
                DumSpan[] spans = out.getSpans(start, end, DumSpan.class);
                if(spans.length > 0){
                    break;
                }
                out.setSpan(new LeadingMarginSpan.Standard(PADDING),  start, end, Spannable.SPAN_PARAGRAPH);
                out.setSpan(new RoundedBackgroundSpan(start, end, Color.LTGRAY, PADDING), start, end, Spannable.SPAN_PARAGRAPH);
                out.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_PARAGRAPH);
                out.setSpan(new LinespaceSpan(PADDING), start, end, Spanned.SPAN_PARAGRAPH);
                out.setSpan(new RelativeSizeSpan(0.92f), start, end, Spanned.SPAN_PARAGRAPH);

                //标记不重复，如果有嵌套，只处理最内层
                out.setSpan(new DumSpan(), start, end, Spanned.SPAN_PARAGRAPH);
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
