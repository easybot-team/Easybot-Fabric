package org.lby123165.easybot.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.lby123165.easybot.EasyBotFabric;
import org.lby123165.easybot.bridge.FabricClientProfile;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * PAPI工具类，用于处理占位符替换
 */
public class PapiUtil {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
    private static boolean textPlaceholderApiAvailable = false;
    private static Object textPlaceholderApi = null;
    private static Method parsePlaceholdersMethod = null;
    
    /**
     * 初始化PAPI工具类，检测Text Placeholder API是否可用
     */
    public static void initialize() {
        // 检测Text Placeholder API是否可用
        if (FabricLoader.getInstance().isModLoaded("text-placeholder-api")) {
            try {
                // 尝试通过反射获取Text Placeholder API
                Class<?> apiClass = Class.forName("eu.pb4.placeholders.api.PlaceholderAPI");
                parsePlaceholdersMethod = apiClass.getMethod("parsePlaceholders", 
                    String.class, Object.class, Object.class);
                
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
     * @param text 包含占位符的文本
     * @param player 玩家实体
     * @return 替换后的文本
     */
    public static String parsePlaceholders(String text, ServerPlayerEntity player) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        if (!textPlaceholderApiAvailable || player == null) {
            return text;
        }
        
        try {
            // 使用Text Placeholder API替换占位符
            Text result = (Text) parsePlaceholdersMethod.invoke(null, text, player, null);
            return result.getString();
        } catch (Exception e) {
            EasyBotFabric.LOGGER.error("替换占位符时出错", e);
            return text;
        }
    }
    
    /**
     * 检查Text Placeholder API是否可用
     * @return 如果可用返回true，否则返回false
     */
    public static boolean isTextPlaceholderApiAvailable() {
        return textPlaceholderApiAvailable;
    }
}