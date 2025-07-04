package org.lby123165.easybot.bridge.model;

import com.google.gson.annotations.SerializedName;
import net.minecraft.server.MinecraftServer;
import org.lby123165.easybot.bridge.FabricClientProfile;

/**
 * 封装了要上报给主程序的服务器详细信息。
 * 这个类可以直接被 Gson 序列化成正确的 JSON 格式。
 */
public class FabricServerInfo {
    // 您说得对，JSON 的键名应该是 "server_name"
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

    public FabricServerInfo(MinecraftServer server) {
        // 将 serverName 的值设置为服务器的 mod 名称，也就是 "fabric"
        this.serverName = server.getServerModName();
        this.serverVersion = server.getVersion();
        this.pluginVersion = FabricClientProfile.getPluginVersion();
        this.isPapiSupported = FabricClientProfile.isPapiSupported();
        this.isCommandSupported = true; // Fabric 服务端总是支持执行命令
        this.hasGeyser = FabricClientProfile.hasGeyser();
        this.isOnlineMode = server.isOnlineMode();
    }
}