package org.lby123165.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;

// 用于解析 RUN_COMMAND 指令
public class RunCommandPacket extends CommandPacket {
    @SerializedName("player_name")
    public String playerName;

    @SerializedName("command")
    public String command;

    @SerializedName("enable_papi")
    public boolean enablePapi;
}