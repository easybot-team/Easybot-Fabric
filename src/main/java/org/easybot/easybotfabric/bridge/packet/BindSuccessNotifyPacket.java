package org.easybot.easybotfabric.bridge.packet;

import com.google.gson.annotations.SerializedName;

// 用于解析 BIND_SUCCESS_NOTIFY 指令
public class BindSuccessNotifyPacket extends CommandPacket {
    @SerializedName("player_name")
    public String playerName;

    @SerializedName("account_id")
    public String accountId;

    @SerializedName("account_name")
    public String accountName;
}