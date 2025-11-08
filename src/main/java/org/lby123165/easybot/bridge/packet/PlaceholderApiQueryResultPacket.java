package org.lby123165.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceholderApiQueryResultPacket {
    @SerializedName("text")
    private String text;
    @SerializedName("success")
    private boolean success;
}