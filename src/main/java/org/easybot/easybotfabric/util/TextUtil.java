package org.easybot.easybotfabric.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本工具类，用于处理Minecraft文本相关操作
 */
public class TextUtil {
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("§([0-9a-fk-or])");

    /**
     * 将包含旧式颜色代码（§）的字符串精确地解析为可渲染的 Text 组件。
     * 这个版本能正确处理格式的叠加和重置。
     *
     * @param text 包含颜色代码的字符串
     * @return 一个可以被 Minecraft 正确渲染的 Text 对象
     */
    public static Text parseLegacyColor(String text) {
        return parseLegacyColor(text, null);
    }
    
    /**
     * 将包含旧式颜色代码（§）的字符串精确地解析为可渲染的 Text 组件，
     * 并处理占位符（如果提供了玩家对象）。
     *
     * @param text 包含颜色代码的字符串
     * @param player 用于处理占位符的玩家对象，可以为null
     * @return 一个可以被 Minecraft 正确渲染的 Text 对象
     */
    public static Text parseLegacyColor(String text, ServerPlayerEntity player) {
        if (text == null || text.isEmpty()) {
            return Text.empty();
        }
        
        // 如果提供了玩家对象且PAPI可用，先处理占位符
        if (player != null && PapiUtil.isTextPlaceholderApiAvailable()) {
            text = PapiUtil.parsePlaceholders(text, player);
        }

        MutableText resultText = Text.empty();
        Matcher matcher = FORMATTING_CODE_PATTERN.matcher(text);

        int lastEnd = 0;
        Style currentStyle = Style.EMPTY;

        while (matcher.find()) {
            String precedingText = text.substring(lastEnd, matcher.start());
            if (!precedingText.isEmpty()) {
                resultText.append(Text.literal(precedingText).setStyle(currentStyle));
            }

            char formatChar = matcher.group(1).charAt(0);
            Formatting formatting = Formatting.byCode(formatChar);

            if (formatting != null) {
                if (formatting == Formatting.RESET) {
                    currentStyle = Style.EMPTY;
                } else if (formatting.isColor()) {
                    currentStyle = Style.EMPTY.withColor(formatting);
                } else {
                    currentStyle = currentStyle.withFormatting(formatting);
                }
            }
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            String remainingText = text.substring(lastEnd);
            resultText.append(Text.literal(remainingText).setStyle(currentStyle));
        }

        return resultText;
    }

    /**
     * 将 Minecraft 的 Text 组件及其所有子组件递归地转换为 Markdown 格式的字符串。
     * @param text 要转换的 Text 对象
     * @return Markdown 格式的字符串
     */
    public static String toMarkdown(Text text) {
        if (text == null) {
            return "";
        }
        StringBuilder markdownBuilder = new StringBuilder();
        buildMarkdownRecursively(text, markdownBuilder);
        return markdownBuilder.toString();
    }

    /**
     * 递归地构建 Markdown 字符串。
     * @param part 当前处理的 Text 部分
     * @param markdownBuilder 用于构建最终字符串的 StringBuilder
     */
    private static void buildMarkdownRecursively(Text part, StringBuilder markdownBuilder) {
        // 1. 处理当前节点的内容和样式
        String content = "";
        // 直接获取文本内容，避免使用特定的内容类型
        content = part.getString();

        if (!content.isEmpty()) {
            // 暂存当前 builder 的长度，以便在需要时进行包装
            int startIndex = markdownBuilder.length();

            boolean isBold = part.getStyle().isBold();
            boolean isItalic = part.getStyle().isItalic();

            if (isBold) markdownBuilder.append("**");
            if (isItalic) markdownBuilder.append("*");

            markdownBuilder.append(content);

            if (isItalic) markdownBuilder.append("*");
            if (isBold) markdownBuilder.append("**");

            // 处理点击事件 -> 转换为 Markdown 链接
            ClickEvent clickEvent = part.getStyle().getClickEvent();
            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
                // 将刚刚添加的整个部分格式化为 [text](url)
                String styledContent = markdownBuilder.substring(startIndex);
                markdownBuilder.setLength(startIndex); // 回滚
                markdownBuilder.append("[").append(styledContent).append("](").append(clickEvent.getValue()).append(")");
            }

            // 处理悬停事件 -> 转换为括号内的提示信息
            HoverEvent hoverEvent = part.getStyle().getHoverEvent();
            if (hoverEvent != null && hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT) {
                Text hoverText = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
                if (hoverText != null) {
                    markdownBuilder.append(" (悬停提示: ").append(hoverText.getString()).append(")");
                }
            }
        }

        // 2. 递归处理所有兄弟节点
        for (Text sibling : part.getSiblings()) {
            buildMarkdownRecursively(sibling, markdownBuilder);
        }
    }
    
    /**
     * 将Minecraft的Text对象转换为带有颜色代码的文本
     * @param text Minecraft的Text对象
     * @return 带有颜色代码的文本
     */
    public static String toColoredString(Text text) {
        if (text == null) {
            return "";
        }
        
        return text.getString(); // 这里简化处理，实际上需要更复杂的逻辑来保留颜色代码
    }
    
    /**
     * 处理文本中的占位符，并返回处理后的Text对象
     * @param text 原始文本字符串
     * @param player 用于处理占位符的玩家对象
     * @return 处理后的Text对象
     */
    public static Text parsePlaceholders(String text, ServerPlayerEntity player) {
        if (text == null || text.isEmpty() || player == null) {
            return Text.literal(text != null ? text : "");
        }
        
        // 如果PAPI可用，先处理占位符
        if (PapiUtil.isTextPlaceholderApiAvailable()) {
            String processed = PapiUtil.parsePlaceholders(text, player);
            // 如果文本包含颜色代码，使用parseLegacyColor处理
            if (processed.contains("§")) {
                return parseLegacyColor(processed);
            } else {
                return Text.literal(processed);
            }
        }
        
        // 如果PAPI不可用，直接返回原始文本
        return Text.literal(text);
    }
    
    /**
     * 处理Text对象中的占位符
     * @param text Text对象
     * @param player 用于处理占位符的玩家对象
     * @return 处理后的Text对象
     */
    public static Text parsePlaceholders(Text text, ServerPlayerEntity player) {
        if (text == null || player == null) {
            return text;
        }
        
        // 如果PAPI可用，处理占位符
        if (PapiUtil.isTextPlaceholderApiAvailable()) {
            String textString = text.getString();
            String processed = PapiUtil.parsePlaceholders(textString, player);
            
            // 如果文本没有变化，说明没有占位符，直接返回原始Text
            if (textString.equals(processed)) {
                return text;
            }
            
            // 否则，创建新的Text对象，保留原始样式
            MutableText result = Text.literal(processed).setStyle(text.getStyle());
            
            // 处理子组件
            for (Text sibling : text.getSiblings()) {
                result.append(parsePlaceholders(sibling, player));
            }
            
            return result;
        }
        
        return text;
    }
}