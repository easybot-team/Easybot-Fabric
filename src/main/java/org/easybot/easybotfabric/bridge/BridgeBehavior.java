package org.easybot.easybotfabric.bridge;

import org.easybot.easybotfabric.bridge.message.Segment;
import org.easybot.easybotfabric.bridge.model.FabricServerInfo;
import org.easybot.easybotfabric.bridge.model.PlayerInfo;
import java.util.List;

/**
 * 定义了 Bridge 的行为契约。
 * 所有与游戏服务器具体交互的逻辑都在这个类的实现中。
 */
public abstract class BridgeBehavior {

    /**
     * 当 WebSocket 客户端收到消息时调用。
     * 这是处理所有服务器指令的入口点。
     * @param rawJson 从服务器收到的原始 JSON 字符串。
     */
    public abstract void onMessage(String rawJson);

    /**
     * 当 Bridge 启用时调用。
     */
    public abstract void onEnable();

    /**
     * 当 Bridge 禁用时调用。
     */
    public abstract void onDisable();

    /**
     * 获取服务器上的在线玩家列表。
     * @return 玩家信息列表。
     */
    public abstract List<PlayerInfo> getPlayerList();

    /**
     * 获取服务器的详细信息。
     * @return 服务器信息对象。
     */
    public abstract FabricServerInfo getInfo();

    /**
     * 将来自主程序的富文本消息同步到游戏聊天中。
     * @param segments 解析后的消息片段列表。
     * @param text 消息的纯文本版本，作为后备。
     */
    public abstract void SyncToChatExtra(List<Segment> segments, String text);
}