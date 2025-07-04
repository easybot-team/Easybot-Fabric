package org.lby123165.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import org.lby123165.easybot.bridge.model.PlayerInfo;

/**
 * Represents the data payload for a player join event.
 * This packet does not need transport-level details like opcodes or callback IDs.
 */
public class OnPlayerJoinPacket {
    @SerializedName("player")
    public PlayerInfo player;

    public OnPlayerJoinPacket(PlayerInfo player) {
        this.player = player;
    }
}