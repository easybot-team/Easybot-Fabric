package org.lby123165.easybot.bridge.packet;

import org.lby123165.easybot.bridge.model.PlayerInfo;
import java.util.List;

// 用于封装 getPlayerList 的结果
public class PlayerListPacket {
    public List<PlayerInfo> list;

    public PlayerListPacket(List<PlayerInfo> list) {
        this.list = list;
    }
}