package org.easybot.easybotfabric.bridge.packet;

// 极其简单的结构
public class HeartbeatPacket extends BasePacket {
    public HeartbeatPacket() {
        this.op = 2; // 心跳包的OpCode
    }
}