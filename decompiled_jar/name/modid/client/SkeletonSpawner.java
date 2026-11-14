/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.SpawnerBlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import name.modid.client.FakeScoreboard;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkeletonSpawner {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"r3dmenu/SkeletonSpawner");
    public static final BlockState SPAWNER_STATE = Blocks.SPAWNER.defaultBlockState();
    public static final EntityType<?> MOB = EntityTypes.SKELETON;
    public static final long BONES_EVERY_MS = 30000L;
    public static final int LOOT_SLOTS = 45;
    public static final int MENU_SLOTS = 54;
    public static final int CAPACITY = 2880;
    public static final double BONE_PRICE = 1.0;
    private static final String LOOT_TAG = "r3dmenu_spawner_loot";
    private static final Map<String, Store> all = new ConcurrentHashMap<String, Store>();
    private static final Map<String, String> pendingRaw = new ConcurrentHashMap<String, String>();
    private static final Map<Long, Store> active = new ConcurrentHashMap<Long, Store>();
    private static final Map<Long, SpawnerBlockEntity> fakes = new ConcurrentHashMap<Long, SpawnerBlockEntity>();
    private static ClientLevel boundLevel = null;
    private static String boundPrefix = null;
    private static ItemStack spawnerItem = ItemStack.EMPTY;
    public static final double ITEM_WORTH_EACH = 1000.0;

    private SkeletonSpawner() {
    }

    public static ItemStack spawnerItem() {
        if (spawnerItem.isEmpty()) {
            spawnerItem = new ItemStack((ItemLike)Items.SPAWNER);
        }
        return spawnerItem;
    }

    public static Component spawnerName() {
        return Component.literal((String)"Spawner").withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    public static List<Component> itemTooltip(int count) {
        ArrayList<Component> lines = new ArrayList<Component>();
        lines.add(SkeletonSpawner.spawnerName());
        lines.add((Component)Component.literal((String)"Skeleton").withStyle(ChatFormatting.YELLOW));
        lines.add((Component)Component.literal((String)"$").withStyle(ChatFormatting.GREEN).append((Component)Component.literal((String)(" " + FakeScoreboard.format(1000.0 * (double)Math.max(1, count)))).withStyle(ChatFormatting.WHITE)));
        lines.add((Component)Component.empty());
        lines.add((Component)Component.literal((String)"Interact with Spawn Egg:").withStyle(ChatFormatting.GRAY));
        lines.add((Component)Component.literal((String)" Sets Mob Type").withStyle(ChatFormatting.BLUE));
        return lines;
    }

    public static boolean disguises(BlockState state) {
        return state.is((Object)((Block)Blocks.STAINED_GLASS_PANE.white()));
    }

    public static int spawnersIn(ItemStack container) {
        if (!container.is(ItemTags.SHULKER_BOXES)) {
            return 0;
        }
        ItemContainerContents contents = (ItemContainerContents)container.get(DataComponents.CONTAINER);
        if (contents == null) {
            return 0;
        }
        int[] count = new int[]{0};
        contents.nonEmptyItemCopyStream().forEach(stack -> {
            if (stack.is((Object)((Item)Items.STAINED_GLASS_PANE.white()))) {
                count[0] = count[0] + stack.getCount();
            }
        });
        return count[0];
    }

    public static SpawnerBlockEntity fakeFor(ClientLevel level, BlockPos pos) {
        return fakes.computeIfAbsent(pos.asLong(), key -> {
            BlockPos immovable = pos.immutable();
            SpawnerBlockEntity be = new SpawnerBlockEntity(immovable, SPAWNER_STATE);
            be.setLevel((Level)level);
            be.getSpawner().setEntityId(MOB, (Level)level, level.getRandom(), immovable);
            return be;
        });
    }

    public static Store storeAt(BlockPos pos) {
        Store store = active.get(pos.asLong());
        if (store == null) {
            store = new Store();
            active.put(pos.asLong(), store);
            if (boundPrefix != null) {
                all.put(boundPrefix + SkeletonSpawner.posKey(pos), store);
            }
        }
        return store;
    }

    public static ItemStack newBone(int count) {
        ItemStack bone = new ItemStack((ItemLike)Items.BONE, count);
        CustomData.update((DataComponentType)DataComponents.CUSTOM_DATA, (ItemStack)bone, tag -> tag.putBoolean(LOOT_TAG, true));
        return bone;
    }

    public static boolean isLoot(ItemStack stack) {
        if (stack.isEmpty() || !stack.is((Object)Items.BONE)) {
            return false;
        }
        CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBooleanOr(LOOT_TAG, false);
    }

    public static void tick(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level != boundLevel) {
            SkeletonSpawner.bind(mc, level);
        }
        if (level == null) {
            return;
        }
        long now = System.currentTimeMillis();
        boolean changed = false;
        for (Store store : active.values()) {
            while (now - store.lastBoneMs >= 30000L) {
                store.lastBoneMs += 30000L;
                if (!store.addBone()) continue;
                changed = true;
            }
        }
        if (changed) {
            SkeletonSpawner.save();
        }
        Iterator<Map.Entry<Long, SpawnerBlockEntity>> it = fakes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, SpawnerBlockEntity> entry = it.next();
            BlockPos pos = BlockPos.of((long)entry.getKey());
            if (!level.isLoaded(pos)) continue;
            if (!SkeletonSpawner.disguises(level.getBlockState(pos))) {
                it.remove();
                if (active.remove(entry.getKey()) == null) continue;
                all.remove(boundPrefix + SkeletonSpawner.posKey(pos));
                SkeletonSpawner.save();
                continue;
            }
            SpawnerBlockEntity.clientTick((Level)level, (BlockPos)pos, (BlockState)SPAWNER_STATE, (SpawnerBlockEntity)entry.getValue());
        }
    }

    private static void bind(Minecraft mc, ClientLevel level) {
        boundLevel = level;
        active.clear();
        fakes.clear();
        if (level == null) {
            boundPrefix = null;
            return;
        }
        boundPrefix = SkeletonSpawner.serverId(mc) + "|" + String.valueOf(level.dimension().identifier()) + "|";
        Iterator<Map.Entry<String, String>> raw = pendingRaw.entrySet().iterator();
        while (raw.hasNext()) {
            Map.Entry<String, String> e = raw.next();
            if (!e.getKey().startsWith(boundPrefix)) continue;
            Store store = SkeletonSpawner.decode(e.getValue());
            if (store != null) {
                all.put(e.getKey(), store);
            }
            raw.remove();
        }
        for (Map.Entry<String, Store> e : all.entrySet()) {
            BlockPos pos;
            if (!e.getKey().startsWith(boundPrefix) || (pos = SkeletonSpawner.parsePos(e.getKey().substring(boundPrefix.length()))) == null) continue;
            active.put(pos.asLong(), e.getValue());
        }
        if (!active.isEmpty()) {
            LOGGER.info("loaded {} spawner(s) for {}", (Object)active.size(), (Object)boundPrefix);
        }
    }

    private static String serverId(Minecraft mc) {
        ServerData server = mc.getCurrentServer();
        if (server == null || server.ip == null || server.ip.isBlank()) {
            return "local";
        }
        return server.ip.toLowerCase();
    }

    private static String posKey(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static BlockPos parsePos(String key) {
        String[] parts = key.split(",");
        if (parts.length != 3) {
            return null;
        }
        try {
            return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("r3dmenu-spawners.properties");
    }

    public static void load() {
        Path path = SkeletonSpawner.configPath();
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
        for (String key : p.stringPropertyNames()) {
            pendingRaw.put(key, p.getProperty(key));
        }
    }

    public static void save() {
        Properties p = new Properties();
        pendingRaw.forEach(p::setProperty);
        all.forEach((key, store) -> p.setProperty((String)key, SkeletonSpawner.encode(store)));
        Path path = SkeletonSpawner.configPath();
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            try (OutputStream out = Files.newOutputStream(path, new OpenOption[0]);){
                p.store(out, "r3dmenu fake spawners: server|dimension|x,y,z = lastBoneMs,bones per loot slot...");
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static String encode(Store store) {
        StringBuilder sb = new StringBuilder().append(store.lastBoneMs);
        for (ItemStack s : store.loot) {
            sb.append(',').append(SkeletonSpawner.isLoot(s) ? s.getCount() : 0);
        }
        return sb.toString();
    }

    private static Store decode(String raw) {
        String[] f = raw.split(",");
        if (f.length < 1) {
            return null;
        }
        try {
            Store store = new Store();
            store.lastBoneMs = Long.parseLong(f[0]);
            for (int i = 0; i < 45 && i + 1 < f.length; ++i) {
                int count = Integer.parseInt(f[i + 1]);
                if (count <= 0) continue;
                store.loot.set(i, (Object)SkeletonSpawner.newBone(Math.min(count, 64)));
            }
            return store;
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    public static final class Store {
        public final NonNullList<ItemStack> loot = NonNullList.withSize((int)45, (Object)ItemStack.EMPTY);
        public long lastBoneMs = System.currentTimeMillis();

        public int bones() {
            int n = 0;
            for (ItemStack s : this.loot) {
                if (!SkeletonSpawner.isLoot(s)) continue;
                n += s.getCount();
            }
            return n;
        }

        public int percentFilled() {
            return (int)Math.min(100L, (long)this.bones() * 100L / 2880L);
        }

        boolean addBone() {
            for (ItemStack s : this.loot) {
                if (!SkeletonSpawner.isLoot(s) || s.getCount() >= s.getMaxStackSize()) continue;
                s.grow(1);
                return true;
            }
            for (int i = 0; i < this.loot.size(); ++i) {
                if (!((ItemStack)this.loot.get(i)).isEmpty()) continue;
                this.loot.set(i, (Object)SkeletonSpawner.newBone(1));
                return true;
            }
            return false;
        }

        public void clearLoot() {
            for (int i = 0; i < this.loot.size(); ++i) {
                this.loot.set(i, (Object)ItemStack.EMPTY);
            }
        }

        public void mergePartialStacks() {
            for (int i = 0; i < this.loot.size(); ++i) {
                ItemStack into = (ItemStack)this.loot.get(i);
                if (!SkeletonSpawner.isLoot(into) || into.getCount() >= into.getMaxStackSize()) continue;
                for (int j = i + 1; j < this.loot.size() && into.getCount() < into.getMaxStackSize(); ++j) {
                    ItemStack from = (ItemStack)this.loot.get(j);
                    if (!SkeletonSpawner.isLoot(from)) continue;
                    int move = Math.min(from.getCount(), into.getMaxStackSize() - into.getCount());
                    into.grow(move);
                    from.shrink(move);
                    if (!from.isEmpty()) continue;
                    this.loot.set(j, (Object)ItemStack.EMPTY);
                }
            }
        }
    }
}

