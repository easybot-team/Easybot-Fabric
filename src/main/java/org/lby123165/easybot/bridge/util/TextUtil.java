package org.lby123165.easybot.util;

import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (text == null || text.isEmpty()) {
            return Text.empty();
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
        // FIX: Revert to the classic 'instanceof' check for wider compatibility.
        String content = "";
        if (part.getContent() instanceof LiteralTextContent literalContent) {
            // This is the correct way to get the string from a literal text component.
            content = literalContent.string();
        }

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
}