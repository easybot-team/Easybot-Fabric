package org.lby123165.easybot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import org.lby123165.easybot.EasyBotFabric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

/**
 * Manages the complex JSON configuration using Gson, mirroring the Bukkit version's design.
 */
public class EasyBotConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(EasyBotConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "easybotfabric.json";
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);

    //<editor-fold desc="Config Fields">
    // --- Config fields (these now serve only as a final fallback in code) ---
    public String ws = "ws://127.0.0.1:26990/bridge";
    public boolean debug = false;
    public int reconnectInterval = 5000;
    public String token = "YOUR_TOKEN_HERE";
    public String serverName = "fabric_server";
    public boolean ignoreError = false;
    public boolean updateNotify = true;
    public boolean enableWhiteList = false;
    public MessageSync messageSync = new MessageSync();
    public Message message = new Message();
    public Events events = new Events();
    public BotFilter botFilter = new BotFilter();
    public Command command = new Command();
    public SkipOptions skipOptions = new SkipOptions();
    public Adapter adapter = new Adapter();
    public Geyser geyser = new Geyser();
    //</editor-fold>

    //<editor-fold desc="Nested Config POJOs">
    // --- Nested configuration classes (POJOs) ---
    public static class MessageSync {
        public boolean ignoreMcdrCommand = true;
    }

    public static class Message {
        public String bindStart = "[!] 绑定开始,请加群12345678输入: \"绑定 #code\" 进行绑定, 请在#time完成绑定!";
        public String bindSuccess = "[!] 绑定 #account (#name) 成功!";
        public String bindFail = "";
        public String syncSuccess = "";
    }

    public static class Events {
        public boolean enableSuccessEvent = true;
        public List<String> bindSuccess = List.of("say 玩家#player绑定账号#name（#account）成功");
        public At at = new At();
    }

    public static class At {
        public boolean enable = true;
        public String title = "§a有人@你";
        public String subTitle = "§a请及时处理";
        public boolean find = true;
        public boolean playSound = true;
        public int sound = 0;
    }

    public static class BotFilter {
        public boolean enabled = true;
        public List<String> prefixes = List.of("Bot_", "BOT_", "bot_", "player_");
        public boolean filterCarpetBots = true;
        public List<String> carpetPrefixes = List.of("bot_", "player_", "fake_");
    }

    public static class Command {
        public boolean allowBind = true;
    }

    public static class SkipOptions {
        public boolean skipJoin = false;
        public boolean skipQuit = false;
        public boolean skipChat = false;
        public boolean skipDeath = false;
    }

    public static class Adapter {
        public boolean useNativeRcon = false;
        public String rconAddress = "127.0.0.1";
        public int rconPort = 25575;
        public String rconPassword = "";
    }

    public static class Geyser {
        public boolean ignorePrefix = false;
    }
    //</editor-fold>

    /**
     * 从磁盘加载配置。如果文件不存在，则从资源中复制默认配置。
     * 返回 EasyBotConfig 的实例
     */
    public static EasyBotConfig load() {
        // Ensure the config file exists. If not, create it from embedded resources.
        if (!Files.exists(CONFIG_PATH)) {
            LOGGER.info("未找到配置文件。从嵌入式资源中创建一个新文件...");
            createDefaultConfigFromResources();
        }

        // Now that the file should exist, try to read and parse it.
        try {
            String content = Files.readString(CONFIG_PATH);
            EasyBotConfig loadedConfig = GSON.fromJson(content, EasyBotConfig.class);
            if (loadedConfig != null) {
                LOGGER.info("成功从 {} 加载配置.", CONFIG_PATH);
                return loadedConfig;
            }
        } catch (JsonSyntaxException e) {
            LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            LOGGER.error("!!! 解析配置文件失败: {}", CONFIG_PATH);
            LOGGER.error("!!! 您的配置文件存在语法错误（如缺少逗号或引号）。");
            LOGGER.error("!!! MOD 将在此会话中使用默认设置，但您的文件并未被覆盖。");
            LOGGER.error("!!! 请修复配置文件中的错误并重新启动服务器。");
            LOGGER.error("!!! 错误详情： {}", e.getMessage());
            LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } catch (IOException e) {
            LOGGER.error("无法读取位于的配置文件 {}. 请检查文件权限。此会话使用默认值。", CONFIG_PATH, e);
        }

        // 如果我们到达这里，则加载失败。回退到内存中的默认值，以防崩溃。   ——DEEPL翻译
        LOGGER.warn("使用该会话的内部默认值。");
        return new EasyBotConfig();
    }

    /**
     * 在 MOD 资源中查找默认配置文件，并将其复制到配置目录。
     */
    private static void createDefaultConfigFromResources() {
        // 使用主类中的常量，以保持一致性。
        Optional<Path> resourcePath = FabricLoader.getInstance()
                .getModContainer(EasyBotFabric.MOD_ID)
                .flatMap(container -> container.findPath(CONFIG_FILE_NAME));

        if (resourcePath.isPresent()) {
            try (InputStream stream = Files.newInputStream(resourcePath.get())) {
                Files.createDirectories(CONFIG_PATH.getParent());
                Files.copy(stream, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.error("将默认配置从资源复制到 {}.", CONFIG_PATH, e);
            }
        } else {
            LOGGER.error("找不到默认配置文件 '{}' 在 mod JAR 内！将创建一个空白的 JAR。", CONFIG_FILE_NAME);
            // As a final fallback, create a config from the hardcoded defaults.
            new EasyBotConfig().save();
        }
    }

    /**
     * 将当前配置实例保存到磁盘。(除非以编程方式修改，否则一般不需要）。    ——DEEPL翻译
     */
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.error("未能将配置保存到 {}", CONFIG_PATH, e);
        }
    }

    /**
     * 从磁盘重新读取配置。
     * @return 一个新的配置实例。    ——DEEPL翻译
     */
    public static EasyBotConfig reload() {
        LOGGER.info("正在重新加载配置...");
        return load();
    }
}