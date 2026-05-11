package io.devtoys.tools.base64;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 纯业务逻辑用于Base64编码/解码。
 *
 * <p>0 UI依赖—可从GUI工具、CLI命令和单元测试待用。保持这个类不包含任何{@code javafx.*}引用。
 */
public final class Base64Core {
    private Base64Core() {
    }

    /**
     * UTF-8将输入字符串编码为Base64字符串。不要返回null。
     */
    public static String encode(String plain) {
        String src = plain == null ? "" : plain;
        return Base64.getEncoder().encodeToString(src.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 将Base64字符串解码为UTF-8文本。首尾空格为
     * 在解码前进行剥离。
     * @throws IllegalArgumentException 如果输入字符串不是有效的Base64字符串。
     */
    public static String decode(String encoded) {
        String src = encoded == null ? "" : encoded.trim();
        byte[] bytes = Base64.getDecoder().decode(src);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
