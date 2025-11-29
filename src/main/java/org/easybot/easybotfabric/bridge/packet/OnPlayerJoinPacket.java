package org.easybot.easybotfabric.bridge.packet;

import com.google.gson.annotations.SerializedName;
import org.easybot.easybotfabric.bridge.model.PlayerInfo;

/**
 * 代表玩家加入事件的数据有效载荷。
 * 该数据包不需要操作码或回调 ID 等传输级细节。
 */
public class OnPlayerJoinPacket {
    @SerializedName("player")
    public PlayerInfo player;

    public OnPlayerJoinPacket(PlayerInfo player) {
        this.player = player;
    }
}