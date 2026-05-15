package io.devtoys.tools.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HashCore {

    public static final List<String> DEFAULT_ALGORITHMS = List.of(
            "MD5",
            "SHA-1",
            "SHA-256",
            "SHA-512"
    );

    private HashCore() {
    }

    public static String digest(String algorithm, String data) {
        return digest(algorithm, data, false);
    }

    private static String digest(String algorithm, String data, boolean upper) {
        String safeData = data == null ? "" : data;
        byte[] bytes = safeData.getBytes(StandardCharsets.UTF_8);
        return digest(algorithm, bytes, upper);
    }

    private static String digest(String algorithm, byte[] data, boolean upper) {
        if (data == null) {
            data = new byte[0];
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm: " + algorithm, e);
        }
        byte[] d = md.digest(data);
        StringBuilder sb = new StringBuilder(d.length * 2);
        for (byte b : d) {
            String hexString = Integer.toHexString(b & 0xff);
            if (hexString.length() == 1) {
                sb.append('0');
            }
            sb.append(hexString);
        }
        return upper ? sb.toString().toUpperCase() : sb.toString();
    }

    public static Map<String, String> digestAll(String data, boolean upper) {
        byte[] bytes = (data == null ? "" : data).getBytes(StandardCharsets.UTF_8);
        Map<String, String> out = new LinkedHashMap<>();
        for (String algorithm : DEFAULT_ALGORITHMS) {
            out.put(algorithm, digest(algorithm, bytes, upper));
        }
        return out;
    }
}
