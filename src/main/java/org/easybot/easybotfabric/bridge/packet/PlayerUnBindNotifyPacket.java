package org.easybot.easybotfabric.bridge.packet;

import com.google.gson.annotations.SerializedName;

// 用于解析 UN_BIND_NOTIFY 指令
public class PlayerUnBindNotifyPacket extends CommandPacket {
    @SerializedName("player_name")
    public String playerName;

    @SerializedName("kick_message")
    public String kickMessage;
}