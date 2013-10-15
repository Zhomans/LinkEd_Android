package com.zhomans.linked;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zach on 10/14/13.
 */

public class Group {

    public String string;
    public final List<String> children = new ArrayList<String>();

    public Group(String string) {
        this.string = string;
    }

}