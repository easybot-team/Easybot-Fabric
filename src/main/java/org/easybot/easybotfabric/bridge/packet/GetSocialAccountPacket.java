package org.easybot.easybotfabric.bridge.packet;

/**
 * 获取社交账号数据包
 */
public class GetSocialAccountPacket {
    public int op = 4;
    public String exec_op = "GET_SOCIAL_ACCOUNT";
    public String callback_id;
    public String player_name;

    public GetSocialAccountPacket(String playerName, String callbackId) {
        this.player_name = playerName;
        this.callback_id = callbackId;
    }
}