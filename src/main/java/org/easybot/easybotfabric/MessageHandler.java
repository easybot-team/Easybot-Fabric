package org.easybot.easybotfabric;

@FunctionalInterface
public interface MessageHandler {
    /**
     * 处理收到的消息
     * @param message 收到的原始消息
     * @return 是否处理了该消息
     */
    boolean handleMessage(String message);
}
