package org.easybot.easybotfabric.bridge.packet;

import org.easybot.easybotfabric.bridge.model.PlayerInfoWithRaw;

/**
 * 跨服消息数据包
 */
public class CrossServerMessagePacket {
    public int op = 4;
    public String exec_op = "CROSS_SERVER_MESSAGE";
    public String callback_id = "0";
    public PlayerInfoWithRaw player;
    public String message;

    public CrossServerMessagePacket(PlayerInfoWithRaw player, String message) {
        this.player = player;
        this.message = message;
    }
}