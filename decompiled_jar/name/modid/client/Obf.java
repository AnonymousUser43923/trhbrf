/*
 * Decompiled with CFR 0.152.
 */
package name.modid.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

final class Obf {
    private static final byte[] KEY = "r3dmenu-keep-this-between-us".getBytes(StandardCharsets.UTF_8);

    private Obf() {
    }

    static String decode(String encoded) {
        byte[] data = Base64.getDecoder().decode(encoded);
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; ++i) {
            out[i] = (byte)(data[i] ^ KEY[i % KEY.length]);
        }
        return new String(out, StandardCharsets.UTF_8);
    }
}

