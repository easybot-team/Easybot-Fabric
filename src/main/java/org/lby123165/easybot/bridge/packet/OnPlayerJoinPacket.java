package org.lby123165.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;
import org.lby123165.easybot.bridge.model.PlayerInfo;

public class OnPlayerJoinPacket extends BasePacket {
    @SerializedName("player")
    public PlayerInfo player;

    @SerializedName("callback_id")
    public String callbackId;

    public OnPlayerJoinPacket(PlayerInfo player, String callbackId) {
        this.op = 4; // Packet
        this.player = player;
        this.callbackId = callbackId;
        // 在 Bukkit/MCDR 中，这个操作的 exec_op 是 PLAYER_JOIN
        // 但为了简化，我们可以在发送时动态添加
    }
}