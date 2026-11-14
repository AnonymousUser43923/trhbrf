/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.level.ItemLike
 */
package name.modid.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import name.modid.client.Obf;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ItemLike;

public final class PayoutLog {
    public static final long THRESHOLD = 100000000L;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern PAID = Pattern.compile("(\\w{3,16})\\s+paid you\\s+\\$?\\s*([0-9][0-9,.]*)\\s*([kKmMbBtT])?", 2);
    private static final String RED_NAME = Obf.decode("IAAAIQQeHF4=");
    private static final String ORANGE_NAME = Obf.decode("PUEFAwILOUwbDBY=");
    private static String loadedProfile = null;
    private static List<Entry> entries = new ArrayList<Entry>();

    private PayoutLog() {
    }

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> PayoutLog.onMessage(message));
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, type, ts) -> PayoutLog.onMessage(message));
        ClientTickEvents.END_CLIENT_TICK.register(client -> PayoutLog.ensureLoaded());
    }

    private static String profileFor(Minecraft mc) {
        if (mc.getUser() == null) {
            return null;
        }
        String name = mc.getUser().getName();
        if (RED_NAME.equalsIgnoreCase(name)) {
            return "red";
        }
        if (ORANGE_NAME.equalsIgnoreCase(name)) {
            return "orange";
        }
        return null;
    }

    public static boolean active() {
        PayoutLog.ensureLoaded();
        return loadedProfile != null;
    }

    private static Path file(String profile) {
        return FabricLoader.getInstance().getConfigDir().resolve("lapismenu-log-" + profile + ".json");
    }

    private static void ensureLoaded() {
        String profile = PayoutLog.profileFor(Minecraft.getInstance());
        if (Objects.equals(profile, loadedProfile)) {
            return;
        }
        loadedProfile = profile;
        entries = new ArrayList<Entry>();
        if (profile == null) {
            return;
        }
        Path p = PayoutLog.file(profile);
        if (!Files.exists(p, new LinkOption[0])) {
            return;
        }
        try (BufferedReader r = Files.newBufferedReader(p);){
            List loaded = (List)GSON.fromJson((Reader)r, new TypeToken<List<Entry>>(){}.getType());
            if (loaded != null) {
                entries = loaded;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void save() {
        if (loadedProfile == null) {
            return;
        }
        try (BufferedWriter w = Files.newBufferedWriter(PayoutLog.file(loadedProfile), new OpenOption[0]);){
            GSON.toJson(entries, (Appendable)w);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static void onMessage(Component message) {
        PlayerInfo info;
        PayoutLog.ensureLoaded();
        if (loadedProfile == null) {
            return;
        }
        Matcher m = PAID.matcher(message.getString());
        if (!m.find()) {
            return;
        }
        long amount = PayoutLog.parseAmount(m.group(2), m.group(3));
        if (amount < 100000000L) {
            return;
        }
        Entry e = new Entry();
        e.payer = m.group(1);
        e.amount = amount;
        e.time = System.currentTimeMillis();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null && (info = mc.getConnection().getPlayerInfoIgnoreCase(e.payer)) != null && info.getProfile() != null) {
            e.uuid = info.getProfile().id().toString();
        }
        entries.add(0, e);
        PayoutLog.save();
    }

    static long parseAmount(String number, String suffix) {
        double base;
        try {
            base = Double.parseDouble(number.replace(",", ""));
        }
        catch (NumberFormatException ex) {
            return 0L;
        }
        double mult = 1.0;
        if (suffix != null && !suffix.isEmpty()) {
            switch (Character.toLowerCase(suffix.charAt(0))) {
                case 'k': {
                    mult = 1000.0;
                    break;
                }
                case 'm': {
                    mult = 1000000.0;
                    break;
                }
                case 'b': {
                    mult = 1.0E9;
                    break;
                }
                case 't': {
                    mult = 1.0E12;
                    break;
                }
                default: {
                    mult = 1.0;
                }
            }
        }
        return (long)(base * mult);
    }

    public static List<Entry> entries() {
        PayoutLog.ensureLoaded();
        return entries;
    }

    public static void delete(Entry e) {
        entries.remove(e);
        PayoutLog.save();
    }

    public static void logEchest(Entry entry) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Inventory inv = mc.player.getInventory();
        ArrayList<Item> items = new ArrayList<Item>();
        for (ItemStack stack : inv.getNonEquipmentItems()) {
            if (stack.isEmpty()) continue;
            items.add(PayoutLog.toRecord(stack));
        }
        entry.echest = items;
        PayoutLog.save();
    }

    private static Item toRecord(ItemStack stack) {
        ItemContainerContents contents;
        Item rec = new Item(BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).toString(), stack.getCount());
        if (stack.is(ItemTags.SHULKER_BOXES) && (contents = (ItemContainerContents)stack.get(DataComponents.CONTAINER)) != null) {
            ArrayList<Item> inner = new ArrayList<Item>();
            contents.nonEmptyItemCopyStream().forEach(s -> inner.add(new Item(BuiltInRegistries.ITEM.getKey((Object)s.getItem()).toString(), s.getCount())));
            if (!inner.isEmpty()) {
                rec.contents = inner;
            }
        }
        return rec;
    }

    public static ItemStack toStack(Item rec) {
        try {
            net.minecraft.world.item.Item item = (net.minecraft.world.item.Item)BuiltInRegistries.ITEM.getValue(Identifier.parse((String)rec.id));
            ItemStack stack = new ItemStack((ItemLike)item, Math.max(1, rec.count));
            if (rec.contents != null && !rec.contents.isEmpty()) {
                ArrayList<ItemStack> inner = new ArrayList<ItemStack>();
                for (Item c : rec.contents) {
                    net.minecraft.world.item.Item ci = (net.minecraft.world.item.Item)BuiltInRegistries.ITEM.getValue(Identifier.parse((String)c.id));
                    inner.add(new ItemStack((ItemLike)ci, Math.max(1, c.count)));
                }
                stack.set(DataComponents.CONTAINER, (Object)ItemContainerContents.fromItems(inner));
            }
            return stack;
        }
        catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public static String shortAmount(long amount) {
        if (amount >= 1000000000000L) {
            return PayoutLog.trim((double)amount / 1.0E12) + "T";
        }
        if (amount >= 1000000000L) {
            return PayoutLog.trim((double)amount / 1.0E9) + "B";
        }
        if (amount >= 1000000L) {
            return PayoutLog.trim((double)amount / 1000000.0) + "M";
        }
        if (amount >= 1000L) {
            return PayoutLog.trim((double)amount / 1000.0) + "k";
        }
        return Long.toString(amount);
    }

    private static String trim(double v) {
        if (v == Math.floor(v)) {
            return String.format(Locale.ROOT, "%.0f", v);
        }
        return String.format(Locale.ROOT, "%.1f", v);
    }

    public static final class Entry {
        public String payer;
        public String uuid;
        public long amount;
        public long time;
        public List<Item> echest;
    }

    public static final class Item {
        public String id;
        public int count;
        public List<Item> contents;

        public Item() {
        }

        public Item(String id, int count) {
            this.id = id;
            this.count = count;
        }
    }
}

