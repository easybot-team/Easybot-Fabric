package org.lby123165.easybot.bridge.packet;

// 扁平结构，直接对应 ws.py 中的 SessionInfo
public class HelloPacket extends BasePacket {
    public String version;
    public String system;
    public String dotnet;
    public String session_id;
    public int interval;
}