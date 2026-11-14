/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import name.modid.client.AutoPearl;
import name.modid.client.DropperSkin;
import name.modid.client.ElytraSkin;
import name.modid.client.FakeChat;
import name.modid.client.FakeScoreboard;
import name.modid.client.GambleGui;
import name.modid.client.HiddenSlot;
import name.modid.client.JoinWorldFix;
import name.modid.client.KeyMining;
import name.modid.client.PhantomDropper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ModSettings {
    private static final int CHECK_INTERVAL = 20;
    private static String pendingFiller = null;
    private static String pendingSkin = null;
    private static boolean deferredApplied = false;
    private static String lastWritten = null;
    private static int checkCountdown = 20;

    private ModSettings() {
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("r3dmenu-settings.properties");
    }

    public static void load() {
        GambleGui.load();
        JoinWorldFix.load();
        Path path = ModSettings.configPath();
        if (!Files.exists(path, new LinkOption[0])) {
            return;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path, new OpenOption[0]);){
            p.load(in);
        }
        catch (IOException e) {
            return;
        }
        ElytraSkin.enabled = ModSettings.bool(p, "elytraSkin.enabled", ElytraSkin.enabled);
        ElytraSkin.marketPrice = p.getProperty("elytraSkin.marketPrice", ElytraSkin.marketPrice);
        FakeChat.enabled = ModSettings.bool(p, "fakeChat.enabled", FakeChat.enabled);
        FakeScoreboard.enabled = ModSettings.bool(p, "fakeScoreboard.enabled", FakeScoreboard.enabled);
        FakeScoreboard.amount = p.getProperty("fakeScoreboard.amount", FakeScoreboard.amount);
        PhantomDropper.enabled = ModSettings.bool(p, "phantomDropper.enabled", PhantomDropper.enabled);
        PhantomDropper.riggedSlot = ModSettings.clamp(ModSettings.integer(p, "phantomDropper.riggedSlot", PhantomDropper.riggedSlot), 0, 8);
        DropperSkin.enabled = ModSettings.bool(p, "dropperSkin.enabled", DropperSkin.enabled);
        pendingFiller = p.getProperty("dropperSkin.fillerItem");
        pendingSkin = p.getProperty("dropperSkin.skinItem");
        KeyMining.hudHidden = ModSettings.bool(p, "keyMining.hudHidden", KeyMining.hudHidden);
        HiddenSlot.enabled = ModSettings.bool(p, "hiddenSlot.enabled", HiddenSlot.enabled);
        AutoPearl.enabled = ModSettings.bool(p, "autoPearl.enabled", AutoPearl.enabled);
        AutoPearl.mainAccount = p.getProperty("autoPearl.mainAccount", AutoPearl.mainAccount);
        AutoPearl.pullerAccount = p.getProperty("autoPearl.pullerAccount", AutoPearl.pullerAccount);
        AutoPearl.key = ModSettings.integer(p, "autoPearl.key", AutoPearl.key);
        AutoPearl.autoPullOnLowHealth = ModSettings.bool(p, "autoPearl.autoPullOnLowHealth", AutoPearl.autoPullOnLowHealth);
        AutoPearl.lowHealthHearts = ModSettings.number(p, "autoPearl.lowHealthHearts", AutoPearl.lowHealthHearts);
    }

    public static void tick() {
        if (!deferredApplied) {
            deferredApplied = true;
            try {
                ModSettings.applyPendingItems();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            lastWritten = ModSettings.fingerprint();
            return;
        }
        if (--checkCountdown > 0) {
            return;
        }
        checkCountdown = 20;
        String now = ModSettings.fingerprint();
        if (!now.equals(lastWritten)) {
            ModSettings.write(now);
        }
    }

    public static void save() {
        ModSettings.write(ModSettings.fingerprint());
    }

    private static Map<String, String> snapshot() {
        LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        m.put("elytraSkin.enabled", Boolean.toString(ElytraSkin.enabled));
        m.put("elytraSkin.marketPrice", ModSettings.text(ElytraSkin.marketPrice));
        m.put("fakeChat.enabled", Boolean.toString(FakeChat.enabled));
        m.put("fakeScoreboard.enabled", Boolean.toString(FakeScoreboard.enabled));
        m.put("fakeScoreboard.amount", ModSettings.text(FakeScoreboard.amount));
        m.put("phantomDropper.enabled", Boolean.toString(PhantomDropper.enabled));
        m.put("phantomDropper.riggedSlot", Integer.toString(PhantomDropper.riggedSlot));
        m.put("dropperSkin.enabled", Boolean.toString(DropperSkin.enabled));
        m.put("dropperSkin.fillerItem", ModSettings.itemId(DropperSkin.fillerItem));
        m.put("dropperSkin.skinItem", ModSettings.itemId(DropperSkin.skinItem));
        m.put("keyMining.hudHidden", Boolean.toString(KeyMining.hudHidden));
        m.put("hiddenSlot.enabled", Boolean.toString(HiddenSlot.enabled));
        m.put("autoPearl.enabled", Boolean.toString(AutoPearl.enabled));
        m.put("autoPearl.mainAccount", ModSettings.text(AutoPearl.mainAccount));
        m.put("autoPearl.pullerAccount", ModSettings.text(AutoPearl.pullerAccount));
        m.put("autoPearl.key", Integer.toString(AutoPearl.key));
        m.put("autoPearl.autoPullOnLowHealth", Boolean.toString(AutoPearl.autoPullOnLowHealth));
        m.put("autoPearl.lowHealthHearts", Double.toString(AutoPearl.lowHealthHearts));
        return m;
    }

    private static String fingerprint() {
        StringBuilder sb = new StringBuilder();
        ModSettings.snapshot().forEach((k, v) -> sb.append((String)k).append('=').append((String)v).append('\n'));
        sb.append("gamble=").append(GambleGui.enabled).append('|').append(ModSettings.text(GambleGui.ign)).append('|').append(ModSettings.text(GambleGui.minBet)).append('|').append(ModSettings.text(GambleGui.maxBet)).append('|').append(ModSettings.text(GambleGui.customText)).append('\n');
        sb.append("joinfix=").append(JoinWorldFix.armed).append('|').append(ModSettings.text(JoinWorldFix.command)).append('|').append(JoinWorldFix.delaySeconds).append('|').append(JoinWorldFix.retrySeconds).append('|').append(JoinWorldFix.reconnectSeconds).append('|').append(ModSettings.text(JoinWorldFix.serverFilter)).append('|').append(JoinWorldFix.showOverlay).append('\n');
        return sb.toString();
    }

    private static void write(String rendered) {
        GambleGui.save();
        JoinWorldFix.save();
        Properties props = new Properties();
        ModSettings.snapshot().forEach(props::setProperty);
        Path path = ModSettings.configPath();
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            try (OutputStream out = Files.newOutputStream(path, new OpenOption[0]);){
                props.store(out, "r3dmenu menu settings");
            }
            lastWritten = rendered;
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static void applyPendingItems() {
        ItemStack skin;
        DropperSkin.ensureInit();
        ItemStack filler = ModSettings.stackFor(pendingFiller);
        if (filler != null) {
            DropperSkin.fillerItem = filler;
        }
        if ((skin = ModSettings.stackFor(pendingSkin)) != null) {
            DropperSkin.skinItem = skin;
        }
        pendingFiller = null;
        pendingSkin = null;
    }

    private static ItemStack stackFor(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        Identifier key = Identifier.tryParse((String)id);
        if (key == null || !BuiltInRegistries.ITEM.containsKey(key)) {
            return null;
        }
        Item item = (Item)BuiltInRegistries.ITEM.getValue(key);
        return item == null ? null : new ItemStack((ItemLike)item);
    }

    private static String itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        return BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).toString();
    }

    private static boolean bool(Properties p, String key, boolean fallback) {
        String raw = p.getProperty(key);
        return raw == null ? fallback : Boolean.parseBoolean(raw);
    }

    private static int integer(Properties p, String key, int fallback) {
        String raw = p.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double number(Properties p, String key, double fallback) {
        String raw = p.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(raw.trim());
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}

