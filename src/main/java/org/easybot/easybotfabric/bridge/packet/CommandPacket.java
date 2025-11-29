package org.easybot.easybotfabric.bridge.packet;

import com.google.gson.annotations.SerializedName;

// 用于解析服务器发来的 OpCode 4 指令包
public class CommandPacket extends BasePacket {
    @SerializedName("exec_op")
    public String execOp;

    @SerializedName("callback_id")
    public String callbackId;

    // 其他指令参数将通过原始 JsonObject 访问
}