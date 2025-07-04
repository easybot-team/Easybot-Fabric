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
    public String ws = "ws://v4-home.lby123165.cn:1501/bridge";
    public boolean debug = false;
    public int reconnectInterval = 5000;
    public String token = "YOUR_TOKEN_HERE";
    public String serverName = "default_server";
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
        public List<String> prefixes = List.of("Bot_", "BOT_", "bot_");
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
     * Loads the configuration from disk. If the file doesn't exist, it copies the default from resources.
     * @return An instance of EasyBotConfig.
     */
    public static EasyBotConfig load() {
        // Ensure the config file exists. If not, create it from embedded resources.
        if (!Files.exists(CONFIG_PATH)) {
            LOGGER.info("Configuration file not found. Creating a new one from embedded resources...");
            createDefaultConfigFromResources();
        }

        // Now that the file should exist, try to read and parse it.
        try {
            String content = Files.readString(CONFIG_PATH);
            EasyBotConfig loadedConfig = GSON.fromJson(content, EasyBotConfig.class);
            if (loadedConfig != null) {
                LOGGER.info("Successfully loaded configuration from {}.", CONFIG_PATH);
                return loadedConfig;
            }
        } catch (JsonSyntaxException e) {
            LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            LOGGER.error("!!! FAILED TO PARSE CONFIG FILE: {}", CONFIG_PATH);
            LOGGER.error("!!! Your config file has a syntax error (e.g., a missing comma or quote).");
            LOGGER.error("!!! The mod will use default settings for this session, but your file has NOT been overwritten.");
            LOGGER.error("!!! Please fix the error in your config file and restart the server.");
            LOGGER.error("!!! Error details: {}", e.getMessage());
            LOGGER.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } catch (IOException e) {
            LOGGER.error("Could not read config file at {}. Please check file permissions. Using default values for this session.", CONFIG_PATH, e);
        }

        // If we reach here, loading failed. Fallback to in-memory defaults to prevent a crash.
        LOGGER.warn("Using internal default values for this session.");
        return new EasyBotConfig();
    }

    /**
     * Finds the default config file in the mod's resources and copies it to the config directory.
     */
    private static void createDefaultConfigFromResources() {
        // Use the constant from the main class for consistency.
        Optional<Path> resourcePath = FabricLoader.getInstance()
                .getModContainer(EasyBotFabric.MOD_ID)
                .flatMap(container -> container.findPath(CONFIG_FILE_NAME));

        if (resourcePath.isPresent()) {
            try (InputStream stream = Files.newInputStream(resourcePath.get())) {
                Files.createDirectories(CONFIG_PATH.getParent());
                Files.copy(stream, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.error("Failed to copy default config from resources to {}.", CONFIG_PATH, e);
            }
        } else {
            LOGGER.error("Could not find the default config file '{}' inside the mod JAR! A blank one will be created.", CONFIG_FILE_NAME);
            // As a final fallback, create a config from the hardcoded defaults.
            new EasyBotConfig().save();
        }
    }

    /**
     * Saves the current config instance to disk. (Not typically needed unless modified programmatically).
     */
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.error("Failed to save config to {}", CONFIG_PATH, e);
        }
    }

    /**
     * Reloads the configuration from disk.
     * @return A new config instance.
     */
    public static EasyBotConfig reload() {
        LOGGER.info("Reloading configuration...");
        return load();
    }
}