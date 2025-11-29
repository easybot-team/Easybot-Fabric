package org.easybot.easybotfabric.bridge.packet;

import org.easybot.easybotfabric.bridge.model.PlayerInfo;
import java.util.List;

// 用于封装 getPlayerList 的结果
public class PlayerListPacket {
    public List<PlayerInfo> list;

    public PlayerListPacket(List<PlayerInfo> list) {
        this.list = list;
    }
}