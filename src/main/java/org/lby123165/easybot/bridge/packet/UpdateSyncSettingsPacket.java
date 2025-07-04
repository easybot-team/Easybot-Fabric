package org.lby123165.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;

// 用于解析 SYNC_SETTINGS_UPDATED 指令
public class UpdateSyncSettingsPacket extends CommandPacket {
    @SerializedName("sync_mode")
    public int syncMode;

    @SerializedName("sync_money")
    public int syncMoney;
}