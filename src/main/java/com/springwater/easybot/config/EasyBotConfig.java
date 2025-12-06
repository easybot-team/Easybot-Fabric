package com.springwater.easybot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class EasyBotConfig {
    private String ws = "ws://127.0.0.1:26990/bridge";
    private boolean debug = false;
    private int reconnectInterval = 5000;
    private String token = "YOUR_TOKEN_HERE";
    private boolean ignoreError = false;
    private boolean updateNotify = true;
    private boolean enableWhiteList = false;


    private Message message = new Message();
    private Command command = new Command();
    private SkipOptions skipOptions = new SkipOptions();
    private Geyser geyser = new Geyser();

    @Getter
    @Setter
    @ToString
    public static class Message {
        private String bindStart = "[!] 绑定开始,请加群12345678输入: \"绑定 #code\" 进行绑定, 请在#time完成绑定!";
        private String bindSuccess = "[!] 绑定 #account (#name) 成功!";
        private String bindFail = "";
        private String syncSuccess = "";
    }

    @Getter
    @Setter
    @ToString
    public static class Command {
        private boolean allowBind = true;
    }

    @Getter
    @Setter
    @ToString
    public static class SkipOptions {
        private boolean skipJoin = false;
        private boolean skipQuit = false;
        private boolean skipChat = false;
        private boolean skipDeath = false;
    }

    @Getter
    @Setter
    @ToString
    public static class Geyser {
        private boolean ignorePrefix = false;
    }
}