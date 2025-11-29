package org.easybot.easybotfabric.bridge.packet;

import com.google.gson.annotations.SerializedName;

/**
 * 用于封装上报给主程序的服务器信息
 */
public class ServerInfoPacket {
    @SerializedName("server_name")
    public String serverName;

    @SerializedName("server_version")
    public String serverVersion;

    @SerializedName("plugin_version")
    public String pluginVersion;

    @SerializedName("is_papi_supported")
    public boolean isPapiSupported;

    @SerializedName("is_command_supported")
    public boolean isCommandSupported;

    @SerializedName("has_geyser")
    public boolean hasGeyser;

    @SerializedName("is_online_mode")
    public boolean isOnlineMode;
}