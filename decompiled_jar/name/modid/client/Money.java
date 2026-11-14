/*
 * Decompiled with CFR 0.152.
 */
package name.modid.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public final class Money {
    private static final BigDecimal K = BigDecimal.valueOf(1000L);
    private static final BigDecimal M = BigDecimal.valueOf(1000000L);
    private static final BigDecimal B = BigDecimal.valueOf(1000000000L);
    private static final BigDecimal T = BigDecimal.valueOf(1000000000000L);

    private Money() {
    }

    public static Optional<BigDecimal> parse(String raw) {
        BigDecimal multiplier;
        if (raw == null) {
            return Optional.empty();
        }
        String s = raw.trim().replace("$", "").replace(",", "").replace("_", "").trim();
        if (s.isEmpty()) {
            return Optional.empty();
        }
        char last = Character.toUpperCase(s.charAt(s.length() - 1));
        switch (last) {
            case 'K': {
                BigDecimal bigDecimal = K;
                break;
            }
            case 'M': {
                BigDecimal bigDecimal = M;
                break;
            }
            case 'B': {
                BigDecimal bigDecimal = B;
                break;
            }
            case 'T': {
                BigDecimal bigDecimal = T;
                break;
            }
            default: {
                BigDecimal bigDecimal = multiplier = null;
            }
        }
        if (multiplier != null) {
            if ((s = s.substring(0, s.length() - 1).trim()).isEmpty()) {
                return Optional.empty();
            }
        } else {
            multiplier = BigDecimal.ONE;
        }
        try {
            return Optional.of(new BigDecimal(s).multiply(multiplier));
        }
        catch (NumberFormatException notANumber) {
            return Optional.empty();
        }
    }

    public static String format(BigDecimal value) {
        BigDecimal divisor;
        String suffix;
        if (value.signum() == 0) {
            return "0";
        }
        BigDecimal abs = value.abs();
        if (abs.compareTo(T) >= 0) {
            suffix = "T";
            divisor = T;
        } else if (abs.compareTo(B) >= 0) {
            suffix = "B";
            divisor = B;
        } else if (abs.compareTo(M) >= 0) {
            suffix = "M";
            divisor = M;
        } else if (abs.compareTo(K) >= 0) {
            suffix = "K";
            divisor = K;
        } else {
            return value.stripTrailingZeros().toPlainString();
        }
        BigDecimal scaled = value.divide(divisor, 2, RoundingMode.HALF_UP).stripTrailingZeros();
        return scaled.toPlainString() + suffix;
    }
}

