package com.springwater.easybot.impl;

import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.bridge.BridgeBehavior;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.bridge.message.Segment;
import com.springwater.easybot.bridge.message.TextSegment;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.model.ServerInfo;
import com.springwater.easybot.utils.LoaderUtils;
import com.springwater.easybot.utils.PlayerInfoUtils;
import com.springwater.easybot.utils.PlayerUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class BridgeBehaviorImpl implements BridgeBehavior {
    private static final CommandImpl COMMAND_IMPL = new CommandImpl();

    @Override
    public String runCommand(String playerName, String command, boolean enableRcon) {
        return COMMAND_IMPL.DispatchCommand(command);
    }

    @Override
    public String papiQuery(String s, String s1) {
        return "";
    }

    @Override
    public ServerInfo getInfo() {
        // 主程序真正获取服务器信息的地方
        ServerInfo info = new ServerInfo();
        info.setServerName(LoaderUtils.isQuilt() ? "Quilt" : "Fabric");
        info.setServerVersion(EasyBotFabric.getServer().getServerVersion());
        info.setPluginVersion(EasyBotFabric.VERSION);
        info.setCommandSupported(ClientProfile.isCommandSupported());
        info.setPapiSupported(ClientProfile.isPapiSupported());
        info.setHasGeyser(ClientProfile.isHasGeyser());
        info.setOnlineMode(ClientProfile.isOnlineMode());
        return info;
    }

    @Override
    public void SyncToChat(String message) {
        EasyBotFabric.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }

    @Override
    public void BindSuccessBroadcast(String s, String s1, String s2) {

    }

    @Override
    public void KickPlayer(String playerName, String reason) {
        PlayerUtils.kickPlayerAsync(playerName, reason); // 这里调用多线程版本的踢出玩家 (走这里的基本上是解绑踢出)
    }

    @Override
    public void SyncToChatExtra(List<Segment> segments, String text) {
        List<Segment> segmentsToAdd = new ArrayList<>();
        StringBuilder currentText = new StringBuilder();

        // 需要将连在一起的纯文本字段合并到一起 别问，干就完了
        for (Segment segment : segments) {
            if (segment instanceof TextSegment) {
                currentText.append(segment.getText());
            } else {
                if (!currentText.isEmpty()) {
                    TextSegment combinedTextSegment = new TextSegment();
                    combinedTextSegment.setText(currentText.toString());
                    segmentsToAdd.add(combinedTextSegment);
                    // 清空缓冲区
                    currentText.setLength(0);
                }
                // 直接添加这个非文本片段
                segmentsToAdd.add(segment);
            }
        }

        // 循环结束后，处理缓冲区里剩余的文本
        if (!currentText.isEmpty()) {
            TextSegment combinedTextSegment = new TextSegment();
            combinedTextSegment.setText(currentText.toString());
            segmentsToAdd.add(combinedTextSegment);
        }
        MutableComponent root = ComponentBuilderImpl.build(segmentsToAdd);
        EasyBotFabric.getServer().getPlayerList().broadcastSystemMessage(root, false);
    }

    @Override
    public List<PlayerInfo> getPlayerList() {
        return PlayerInfoUtils.buildPlayerInfoList(EasyBotFabric.getServer().getPlayerList().getPlayers());
    }
}
