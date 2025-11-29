package org.easybot.easybotfabric.bridge.packet;

import com.google.gson.annotations.SerializedName;
import org.easybot.easybotfabric.bridge.model.PlayerInfoWithRaw;

public class SyncEnterExitMessagePacket extends BasePacket {
    @SerializedName("player")
    public PlayerInfoWithRaw player;

    @SerializedName("is_enter")
    public boolean isEnter;

    public SyncEnterExitMessagePacket(PlayerInfoWithRaw player, boolean isEnter) {
        this.op = 4; // Packet
        this.player = player;
        this.isEnter = isEnter;
    }
}