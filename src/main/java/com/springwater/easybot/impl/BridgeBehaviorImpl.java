package com.springwater.easybot.impl;

import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.bridge.BridgeBehavior;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.bridge.message.Segment;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.model.ServerInfo;
import com.springwater.easybot.utils.LoaderUtil;

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
        info.setServerName(LoaderUtil.isQuilt() ? "Quilt" : "Fabric");
        info.setServerVersion(EasyBotFabric.getServer().getServerVersion());
        info.setPluginVersion(EasyBotFabric.VERSION);
        info.setCommandSupported(ClientProfile.isCommandSupported());
        info.setPapiSupported(ClientProfile.isPapiSupported());
        info.setHasGeyser(ClientProfile.isHasGeyser());
        info.setOnlineMode(ClientProfile.isOnlineMode());
        return info;
    }

    @Override
    public void SyncToChat(String s) {

    }

    @Override
    public void BindSuccessBroadcast(String s, String s1, String s2) {

    }

    @Override
    public void KickPlayer(String s, String s1) {

    }

    @Override
    public void SyncToChatExtra(List<Segment> list, String s) {

    }

    @Override
    public List<PlayerInfo> getPlayerList() {
        return List.of();
    }
}
