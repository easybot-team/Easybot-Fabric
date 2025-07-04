package org.lby123165.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;

public class PlayerLoginResultPacket extends BasePacket {
    @SerializedName("kick")
    public boolean kick;

    @SerializedName("kick_message")
    public String kickMessage;
}