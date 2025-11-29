package org.easybot.easybotfabric.bridge.packet;

import com.google.gson.annotations.SerializedName;

// 扁平结构
public class IdentifySuccessPacket extends BasePacket {
    @SerializedName("server_name")
    public String serverName;
}