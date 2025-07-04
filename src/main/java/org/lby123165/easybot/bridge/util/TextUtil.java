package org.lby123165.easybot.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    // 正则表达式，用于查找 § 符号后跟一个有效的格式代码字符 (0-9, a-f, k-o, r)
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

        // 循环查找每一个格式代码
        while (matcher.find()) {
            // 1. 将此格式代码之前的文本，应用上一个样式，并追加到结果中
            String precedingText = text.substring(lastEnd, matcher.start());
            if (!precedingText.isEmpty()) {
                resultText.append(Text.literal(precedingText).setStyle(currentStyle));
            }

            // 2. 获取新的格式代码
            char formatChar = matcher.group(1).charAt(0);
            Formatting formatting = Formatting.byCode(formatChar);

            // 3. 根据代码类型更新当前样式
            if (formatting != null) {
                if (formatting == Formatting.RESET) {
                    // 如果是重置代码，清空所有样式
                    currentStyle = Style.EMPTY;
                } else if (formatting.isColor()) {
                    // 如果是颜色代码，清空所有样式并应用新颜色
                    currentStyle = Style.EMPTY.withColor(formatting);
                } else { // 如果是粗体、斜体等修饰符
                    // 在现有样式上添加修饰符
                    currentStyle = currentStyle.withFormatting(formatting);
                }
            }

            // 4. 更新下一次查找的起始位置
            lastEnd = matcher.end();
        }

        // 5. 将最后一个格式代码之后的所有剩余文本，应用最终的样式
        if (lastEnd < text.length()) {
            String remainingText = text.substring(lastEnd);
            resultText.append(Text.literal(remainingText).setStyle(currentStyle));
        }

        return resultText;
    }
}