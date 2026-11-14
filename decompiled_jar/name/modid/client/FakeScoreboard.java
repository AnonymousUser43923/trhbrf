/*
 * Decompiled with CFR 0.152.
 */
package name.modid.client;

import java.util.Locale;

public final class FakeScoreboard {
    public static boolean enabled = false;
    public static String amount = "100B";

    private FakeScoreboard() {
    }

    public static synchronized void add(double delta) {
        amount = FakeScoreboard.format(FakeScoreboard.parse(amount) + delta);
    }

    public static double parse(String s) {
        if (s == null) {
            return 0.0;
        }
        if ((s = s.trim().replace("$", "").replace(",", "").replace(" ", "")).isEmpty()) {
            return 0.0;
        }
        double mult = 1.0;
        int i = "KMBTQ".indexOf(Character.toUpperCase(s.charAt(s.length() - 1)));
        if (i >= 0) {
            mult = Math.pow(1000.0, i + 1);
            s = s.substring(0, s.length() - 1);
        }
        try {
            return Double.parseDouble(s) * mult;
        }
        catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static String format(double v) {
        double a = Math.abs(v);
        if (a >= 1.0E15) {
            return FakeScoreboard.strip(v / 1.0E15) + "Q";
        }
        if (a >= 1.0E12) {
            return FakeScoreboard.strip(v / 1.0E12) + "T";
        }
        if (a >= 1.0E9) {
            return FakeScoreboard.strip(v / 1.0E9) + "B";
        }
        if (a >= 1000000.0) {
            return FakeScoreboard.strip(v / 1000000.0) + "M";
        }
        if (a >= 1000.0) {
            return FakeScoreboard.strip(v / 1000.0) + "K";
        }
        return FakeScoreboard.strip(v);
    }

    private static String strip(double v) {
        String s = String.format(Locale.ROOT, "%.2f", v);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return s;
    }
}

