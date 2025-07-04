package org.lby123165.easybot.bridge.packet;

import com.google.gson.annotations.SerializedName;

// 用于封装指令执行结果
public class RunCommandResultPacket {
    @SerializedName("text")
    public String text;

    @SerializedName("success")
    public boolean success;

    public RunCommandResultPacket(boolean success, String text) {
        this.success = success;
        this.text = text;
    }
}
