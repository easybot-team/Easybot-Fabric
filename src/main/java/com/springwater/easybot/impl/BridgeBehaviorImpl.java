package com.springwater.easybot.impl;

import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.bridge.BridgeBehavior;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.bridge.message.Segment;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.model.ServerInfo;
import com.springwater.easybot.utils.LoaderUtils;
import com.springwater.easybot.utils.PlayerInfoUtils;
import com.springwater.easybot.utils.PlayerUtils;

import java.util.List;

public class BridgeBehaviorImpl implements BridgeBehavior {
    @Override
    public String runCommand(String s, String s1, boolean b) {
        return "";
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

    }

    @Override
    public List<PlayerInfo> getPlayerList() {
        return PlayerInfoUtils.buildPlayerInfoList(EasyBotFabric.getServer().getPlayerList().getPlayers());
    }
}
