/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.minecraft.resources.Identifier
 */
package name.modid.client;

import java.util.Locale;
import name.modid.client.Obf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.resources.Identifier;

public final class MacroProfile {
    private static volatile Kind current = Kind.NONE;
    private static final String PW_RED = "RedMenu";
    private static final String PW_ORANGE = "OrangeMenu";

    private MacroProfile() {
    }

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            current = Kind.NONE;
        });
    }

    public static boolean unlock(String password) {
        if (password == null) {
            return false;
        }
        if (password.equals(PW_RED)) {
            current = Kind.RED;
            return true;
        }
        if (password.equals(PW_ORANGE)) {
            current = Kind.ORANGE;
            return true;
        }
        return false;
    }

    public static boolean isUnlocked() {
        return current != Kind.NONE;
    }

    public static Kind kind() {
        return current;
    }

    public static String mainName() {
        return switch (current.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> Obf.decode("PnooJiwtPmkkKjcq");
            case 2 -> Obf.decode("JVoCBAAdHkgHABEfQw==");
            case 0 -> "PnooJiwtPmkkKjcq";
        };
    }

    public static String mainNameLower() {
        return MacroProfile.mainName().toLowerCase(Locale.ROOT);
    }

    public static boolean isOrange() {
        return current == Kind.ORANGE;
    }

    public static String brandName() {
        return MacroProfile.isOrange() ? Obf.decode("JVoCBAAdHkgHABEfQw==") : Obf.decode("PnooJiwtPmkkKjcq");
    }

    public static String brandTitle() {
        return MacroProfile.isOrange() ? "ORANGE LAPIS" : "R3D LAPIS";
    }

    public static String skinPath() {
        return MacroProfile.isOrange() ? "gui/orangelapis_skin" : "gui/r3dlapis_skin";
    }

    public static Identifier lapisTexture() {
        return Identifier.fromNamespaceAndPath((String)"r3dmenu", (String)(MacroProfile.isOrange() ? "textures/gui/lapis_orange.png" : "textures/gui/lapis_red.png"));
    }

    public static enum Kind {
        NONE,
        RED,
        ORANGE;

    }
}

