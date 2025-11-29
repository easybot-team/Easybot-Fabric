package org.easybot.easybotfabric.bridge.packet;

/**
 * 开始绑定数据包
 */
public class StartBindPacket {
    public int op = 4;
    public String exec_op = "START_BIND";
    public String callback_id;
    public String player_name;

    public StartBindPacket(String playerName, String callbackId) {
        this.player_name = playerName;
        this.callback_id = callbackId;
    }
}