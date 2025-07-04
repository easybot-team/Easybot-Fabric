package org.lby123165.easybot.bridge.packet;

// 极其简单的结构
public class HeartbeatPacket extends BasePacket {
    public HeartbeatPacket() {
        this.op = 2; // OpCode for HeartBeat
    }
}