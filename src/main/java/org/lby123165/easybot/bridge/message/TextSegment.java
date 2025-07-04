package org.lby123165.easybot.bridge.message;

import com.google.gson.annotations.SerializedName;

public class TextSegment extends Segment {
    @SerializedName("text")
    public String text;
}