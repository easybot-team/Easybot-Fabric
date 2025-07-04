package org.lby123165.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import org.lby123165.easybot.bridge.model.PlayerInfoWithRaw;

public class SyncDeathMessagePacket extends BasePacket {
    @SerializedName("player")
    public PlayerInfoWithRaw player;

    @SerializedName("raw")
    public String raw; // The full death message text

    @SerializedName("killer")
    public String killer; // The name of the killer, if available

    public SyncDeathMessagePacket(PlayerInfoWithRaw player, String raw, String killer) {
        this.op = 4; // Packet
        this.player = player;
        this.raw = raw;
        this.killer = killer;
    }
}