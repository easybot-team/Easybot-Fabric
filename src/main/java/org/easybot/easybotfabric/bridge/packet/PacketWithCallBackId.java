package org.easybot.easybotfabric.bridge.packet;

import com.google.gson.annotations.SerializedName;
import org.easybot.easybotfabric.bridge.OpCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PacketWithCallBackId {
    @SerializedName("op")
    private OpCode opCode = OpCode.Packet;
    @SerializedName("callback_id")
    private String CallBackId;

    @SerializedName("exec_op")
    private String Operation;
}