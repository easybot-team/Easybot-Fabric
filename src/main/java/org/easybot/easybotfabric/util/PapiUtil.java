package org.easybot.easybotfabric.util;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.easybot.easybotfabric.EasyBotFabric;
import org.easybot.easybotfabric.bridge.FabricClientProfile;

import java.util.regex.Pattern;

/**
 * PAPI工具类，用于处理占位符替换
 */
public class PapiUtil {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
    private static boolean textPlaceholderApiAvailable = false;

    /**
     * 初始化PAPI工具类，检测Text Placeholder API是否可用
     */
    public static void initialize() {
        // 检测Text Placeholder API是否可用
        EasyBotFabric.LOGGER.info("检测FabricPapi");
        if (FabricLoader.getInstance().isModLoaded("placeholder-api")) {
            try {
                textPlaceholderApiAvailable = true;
                EasyBotFabric.LOGGER.info("检测到Text Placeholder API，PAPI功能已启用");
                FabricClientProfile.setPapiSupported(true);
            } catch (Exception e) {
                EasyBotFabric.LOGGER.error("加载Text Placeholder API失败", e);
                textPlaceholderApiAvailable = false;
                FabricClientProfile.setPapiSupported(false);
            }
        } else {
            EasyBotFabric.LOGGER.info("未检测到Text Placeholder API，PAPI功能已禁用");
            textPlaceholderApiAvailable = false;
            FabricClientProfile.setPapiSupported(false);
        }
    }

    /**
     * 替换文本中的占位符
     *
     * @param text   包含占位符的文本
     * @param player 玩家实体
     * @return 替换后的文本
     */
    public static String parsePlaceholders(String text, ServerPlayerEntity player) {
        if (text == null || text.isEmpty()) {
            EasyBotFabric.LOGGER.error("文本为空");
            return text;
        }

        if (!textPlaceholderApiAvailable || player == null) {
            EasyBotFabric.LOGGER.error("PAPI不可用或玩家为空");
            return text;
        }

        try {
            MutableText textToParse = Text.literal(text);
            PlaceholderContext context = PlaceholderContext.of(player);
            Text parsedText = Placeholders.parseText(
                    textToParse,
                    context
            );
            return parsedText.getString();
        } catch (Exception e) {
            EasyBotFabric.LOGGER.error("替换占位符时出错", e);
            return text;
        }
    }

    /**
     * 检查Text Placeholder API是否可用
     *
     * @return 如果可用返回true，否则返回false
     */
    public static boolean isTextPlaceholderApiAvailable() {
        return textPlaceholderApiAvailable;
    }
}