package org.lby123165.easybot.bridge.model;

import com.google.gson.annotations.SerializedName;

public class PlayerInfoWithRaw extends PlayerInfo {
    @SerializedName("player_name_raw")
    public String playerNameRaw;

    public PlayerInfoWithRaw(PlayerInfo base, String rawName) {
        // FIX: Use the public getters from the base object
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