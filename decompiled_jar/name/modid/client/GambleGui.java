/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 */
package name.modid.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

public final class GambleGui {
    public static boolean enabled = false;
    public static String ign = "";
    public static String minBet = "";
    public static String maxBet = "";
    public static String customText = "";

    private GambleGui() {
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("r3dmenu-gamblegui.properties");
    }

    public static void load() {
        Path path = GambleGui.configPath();
        if (!Files.exists(path, new LinkOption[0]) && !Files.exists(path = FabricLoader.getInstance().getConfigDir().resolve("connected-textures-gamblegui.properties"), new LinkOption[0])) {
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path, new OpenOption[0]);){
            props.load(in);
        }
        catch (IOException e) {
            return;
        }
        enabled = Boolean.parseBoolean(props.getProperty("enabled", "false"));
        ign = props.getProperty("ign", "");
        minBet = props.getProperty("minBet", "");
        maxBet = props.getProperty("maxBet", "");
        customText = props.getProperty("customText", "");
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("enabled", Boolean.toString(enabled));
        props.setProperty("ign", ign);
        props.setProperty("minBet", minBet);
        props.setProperty("maxBet", maxBet);
        props.setProperty("customText", customText);
        Path path = GambleGui.configPath();
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            try (OutputStream out = Files.newOutputStream(path, new OpenOption[0]);){
                props.store(out, "r3dmenu GAMBLEGUI settings");
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

