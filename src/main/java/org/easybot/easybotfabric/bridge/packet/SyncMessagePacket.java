package org.easybot.easybotfabric.bridge.packet;

import com.google.gson.annotations.SerializedName;
import org.easybot.easybotfabric.bridge.model.PlayerInfoWithRaw;

public class SyncMessagePacket extends BasePacket {
    @SerializedName("player")
    public PlayerInfoWithRaw player;

    @SerializedName("message")
    public String message;

    @SerializedName("use_command")
    public boolean useCommand;

    public SyncMessagePacket(PlayerInfoWithRaw player, String message, boolean useCommand) {
        this.op = 4; // Packet
        this.player = player;
        this.message = message;
        this.useCommand = useCommand;
    }
}