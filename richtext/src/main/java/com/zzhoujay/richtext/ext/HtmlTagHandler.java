package com.zzhoujay.richtext.ext;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
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

    private Stack<Integer> stack;
    private Stack<Boolean> list;
    private int index = 0;
    private SoftReference<TextView> textViewSoftReference;
    private int h1_text_color;
    private int code_background_color;

    public HtmlTagHandler(TextView textView) {
        stack = new Stack<>();
        list = new Stack<>();
        this.textViewSoftReference = new SoftReference<>(textView);

        Context context = textView.getContext();
        TypedArray a = context.obtainStyledAttributes(null, com.zzhoujay.markdown.R.styleable.MarkdownTheme, com.zzhoujay.markdown.R.attr.markdownStyle, 0);
        final boolean failed = !a.hasValue(0);
        if (failed) {
            Log.w("Markdown", "Missing markdownStyle in your theme, using hardcoded color.");
            h1_text_color = 0xdf000000;
            code_background_color = 0x0c37474f;
        } else {
            h1_text_color = a.getColor(com.zzhoujay.markdown.R.styleable.MarkdownTheme_h1TextColor, 0);
            code_background_color = a.getColor(com.zzhoujay.markdown.R.styleable.MarkdownTheme_codeBackgroundColor, 0);
        }

        a.recycle();
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
                if (textViewSoftReference.get() == null) {
                    break;
                }
                DumSpan[] spans = out.getSpans(start, end, DumSpan.class);
                if(spans.length > 0){
                    break;
                }
                int padding = (int) textViewSoftReference.get().getTextSize();
                out.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                out.setSpan(new RelativeSizeSpan(0.8f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                out.setSpan(new LeadingMarginSpan.Standard(padding / 2),  start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                out.setSpan(new RoundedBackgroundSpan(out, start, end, code_background_color, padding / 3), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                out.setSpan(new LinespaceSpan(padding / 2), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                //标记不重复，如果有嵌套，只处理最内层
                out.setSpan(new DumSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                MarkDownBulletSpan bulletSpan = new MarkDownBulletSpan(list.size() - 1, h1_text_color, i);
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
