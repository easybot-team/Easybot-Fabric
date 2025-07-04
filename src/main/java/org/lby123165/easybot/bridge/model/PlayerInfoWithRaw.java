package org.lby123165.easybot.bridge.model;

import com.google.gson.annotations.SerializedName;

public class PlayerInfoWithRaw extends PlayerInfo {
    @SerializedName("player_name_raw")
    public String playerNameRaw;

    public PlayerInfoWithRaw(PlayerInfo base, String rawName) {
        super(base.playerName, base.uuid, base.displayName, base.latency, base.world, base.x, base.y, base.z);
        this.playerNameRaw = rawName;
    }
}