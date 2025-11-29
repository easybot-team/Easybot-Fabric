package org.easybot.easybotfabric.bridge.model;

import com.google.gson.annotations.SerializedName;

public class PlayerInfoWithRaw extends PlayerInfo {
    @SerializedName("player_name_raw")
    public String playerNameRaw;

    public PlayerInfoWithRaw(PlayerInfo base, String rawName) {
        // 修正：使用基本对象的公共获取器
        super(
                base.getPlayerName(),
                base.getUuid(),
                base.getDisplayName(),
                base.getLatency(),
                base.getWorld(),
                base.getX(),
                base.getY(),
                base.getZ()
        );
        this.playerNameRaw = rawName;
    }
}