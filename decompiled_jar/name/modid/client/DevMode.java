/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 */
package name.modid.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class DevMode {
    private static volatile boolean enabled = false;

    private DevMode() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("lapismenu-dev.flag");
    }

    public static void load() {
        try {
            Path p = DevMode.file();
            if (Files.exists(p, new LinkOption[0])) {
                enabled = "true".equals(Files.readString(p).trim());
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
        DevMode.save();
    }

    private static void save() {
        try {
            Files.writeString(DevMode.file(), (CharSequence)Boolean.toString(enabled), new OpenOption[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

