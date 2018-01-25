package com.pixplicity.htmlcompat;

import java.util.ArrayList;

/**
 * Created by benny on 1/25/18.
 */

public class HtmlTagInfo {
    final String name;
    final int start;
    ArrayList<Object> tokens = new ArrayList<>();

    public HtmlTagInfo(String name, int start) {
        this.name = name;
        this.start = start;
    }
}
