package com.modnmetl.modnvote.util;

import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class Integrity {
    private Integrity() {}

    public static String canonicalString(int round, int yes, int no, List<UUID> uuids) {
        StringBuilder sb = new StringBuilder();
        sb.append("round=").append(round).append('\n');
        sb.append("yes=").append(yes).append('\n');
        sb.append("no=").append(no).append('\n');
        sb.append("uuids=");
        for (int i = 0; i < uuids.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(uuids.get(i).toString());
        }
        return sb.toString();
    }

    public static String hmacSha256Hex(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("HMAC failed: " + e.getMessage(), e);
        }
    }

    private static String toHex(byte[] data) {
        Formatter f = new Formatter();
        for (byte b : data) f.format("%02x", b);
        String out = f.toString();
        f.close();
        return out;
    }
}
