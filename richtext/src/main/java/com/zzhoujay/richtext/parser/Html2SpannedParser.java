package com.zzhoujay.richtext.parser;

import android.content.Context;
import android.text.Spanned;

import com.pixplicity.htmlcompat.HtmlCompat;

/**
 * Created by zhou on 16-7-27.
 * Html2SpannedParser
 */
public class Html2SpannedParser implements SpannedParser {

    private Context context;
    private HtmlCompat.TagHandler tagHandler;

    public Html2SpannedParser(Context context, HtmlCompat.TagHandler tagHandler) {
        this.context = context;
        this.tagHandler = tagHandler;
    }

    @Override
    public Spanned parse(String source) {
        return HtmlCompat.fromHtml(context, source, 0, null, tagHandler);
    }
}
