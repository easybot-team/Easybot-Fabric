package org.easybot.easybotfabric.bridge.packet;

import com.google.gson.annotations.SerializedName;

// 扁平结构
public class IdentifyPacket extends BasePacket {
    public String token;

    @SerializedName("plugin_version")
    public String pluginVersion;

    @SerializedName("server_description")
    public String serverDescription;

    public IdentifyPacket(String token, String pluginVersion, String serverDescription) {
        this.op = 1; // OpCode for Identify
        this.token = token;
        this.pluginVersion = pluginVersion;
        this.serverDescription = serverDescription;
    }
}