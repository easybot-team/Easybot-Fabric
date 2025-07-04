package org.lby123165.easybot.bridge.packet;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

// 用于解析 SEND_TO_CHAT 指令
public class SendToChatPacket extends CommandPacket {
    @SerializedName("text")
    public String text;

    @SerializedName("extra")
    public JsonArray extra;
}