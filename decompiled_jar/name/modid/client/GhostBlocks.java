/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderGetter
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.TagParser
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import name.modid.client.GhostItems;
import name.modid.client.PhantomItems;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GhostBlocks {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"r3dmenu/GhostBlocks");
    private static final Map<String, String> all = new ConcurrentHashMap<String, String>();
    private static final Map<Long, BlockState> active = new ConcurrentHashMap<Long, BlockState>();
    private static ClientLevel boundLevel = null;
    private static String boundPrefix = null;

    private GhostBlocks() {
    }

    public static BlockState at(BlockPos pos) {
        return active.get(pos.asLong());
    }

    public static boolean has(BlockPos pos) {
        return active.containsKey(pos.asLong());
    }

    public static boolean targeted(Minecraft mc) {
        BlockHitResult hit;
        HitResult hitResult = mc.hitResult;
        return hitResult instanceof BlockHitResult && (hit = (BlockHitResult)hitResult).getType() == HitResult.Type.BLOCK && GhostBlocks.has(hit.getBlockPos());
    }

    public static boolean tryPlace(Minecraft mc, InteractionHand hand, BlockHitResult hit) {
        BlockPos pos;
        Item item;
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) {
            return false;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!PhantomItems.isPhantom(stack) || !((item = stack.getItem()) instanceof BlockItem)) {
            return false;
        }
        BlockItem blockItem = (BlockItem)item;
        BlockPos clicked = hit.getBlockPos();
        BlockState clickedState = GhostBlocks.has(clicked) ? GhostBlocks.at(clicked) : level.getBlockState(clicked);
        BlockPos blockPos = pos = clickedState.canBeReplaced() && !GhostBlocks.has(clicked) ? clicked : clicked.relative(hit.getDirection());
        if (GhostBlocks.has(pos) || !level.getBlockState(pos).canBeReplaced()) {
            return false;
        }
        BlockHitResult placeHit = new BlockHitResult(hit.getLocation(), hit.getDirection(), pos, false);
        BlockState state = blockItem.getBlock().getStateForPlacement(new BlockPlaceContext((Player)player, hand, stack, placeHit));
        if (state == null || !state.canSurvive((LevelReader)level, pos) || !level.isUnobstructed(state, pos, CollisionContext.of((Entity)player))) {
            return false;
        }
        GhostBlocks.put(level, pos, state);
        SoundType sound = state.getSoundType();
        level.playLocalSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0f) / 2.0f, sound.getPitch() * 0.8f, false);
        player.swing(hand);
        PhantomItems.forget(stack);
        stack.shrink(1);
        if (!stack.isEmpty()) {
            PhantomItems.track(stack);
        }
        return true;
    }

    public static void breakAt(Minecraft mc, BlockPos pos) {
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        BlockState state = GhostBlocks.at(pos);
        if (level == null || player == null || state == null) {
            return;
        }
        GhostBlocks.remove(level, pos);
        level.addDestroyBlockEffect(pos, state);
        SoundType sound = state.getSoundType();
        level.playLocalSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0f) / 2.0f, sound.getPitch() * 0.8f, false);
        player.swing(InteractionHand.MAIN_HAND);
        ItemStack drop = new ItemStack((ItemLike)state.getBlock().asItem());
        if (!drop.isEmpty()) {
            PhantomItems.track(drop);
            PhantomItems.forget(drop);
            GhostItems.spawn(level, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, drop, (level.getRandom().nextDouble() - 0.5) * 0.1, 0.2, (level.getRandom().nextDouble() - 0.5) * 0.1);
        }
    }

    private static void put(ClientLevel level, BlockPos pos, BlockState state) {
        active.put(pos.asLong(), state);
        if (boundPrefix != null) {
            all.put(boundPrefix + GhostBlocks.posKey(pos), NbtUtils.writeBlockState((BlockState)state).toString());
        }
        GhostBlocks.remesh(level, pos);
        GhostBlocks.save();
    }

    private static void remove(ClientLevel level, BlockPos pos) {
        active.remove(pos.asLong());
        if (boundPrefix != null) {
            all.remove(boundPrefix + GhostBlocks.posKey(pos));
        }
        GhostBlocks.remesh(level, pos);
        GhostBlocks.save();
    }

    private static void remesh(ClientLevel level, BlockPos pos) {
        BlockState real = level.getBlockState(pos);
        level.setBlocksDirty(pos, real, real);
    }

    public static BlockHitResult pick(Entity viewer, float partialTick, double reach) {
        if (active.isEmpty()) {
            return null;
        }
        Vec3 from = viewer.getEyePosition(partialTick);
        Vec3 to = from.add(viewer.getViewVector(partialTick).scale(reach));
        double reachSqr = reach * reach;
        BlockHitResult best = null;
        double bestDist = Double.MAX_VALUE;
        for (Map.Entry<Long, BlockState> e : active.entrySet()) {
            double d;
            BlockHitResult hit;
            BlockPos pos = BlockPos.of((long)e.getKey());
            if (from.distanceToSqr(Vec3.atCenterOf((Vec3i)pos)) > reachSqr + 3.0 || (hit = e.getValue().getShape((BlockGetter)viewer.level(), pos, CollisionContext.of((Entity)viewer)).clip(from, to, pos)) == null || hit.getType() != HitResult.Type.BLOCK || !((d = from.distanceToSqr(hit.getLocation())) < bestDist)) continue;
            bestDist = d;
            best = hit;
        }
        return best;
    }

    public static void tick(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level != boundLevel) {
            GhostBlocks.bind(mc, level);
        }
        if (level == null || active.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<Long, BlockState>> it = active.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, BlockState> e = it.next();
            BlockPos pos = BlockPos.of((long)e.getKey());
            if (!level.isLoaded(pos) || level.getBlockState(pos).canBeReplaced()) continue;
            it.remove();
            all.remove(boundPrefix + GhostBlocks.posKey(pos));
            GhostBlocks.save();
        }
    }

    private static void bind(Minecraft mc, ClientLevel level) {
        boundLevel = level;
        active.clear();
        if (level == null) {
            boundPrefix = null;
            return;
        }
        boundPrefix = GhostBlocks.serverId(mc) + "|" + String.valueOf(level.dimension().identifier()) + "|";
        for (Map.Entry<String, String> e : all.entrySet()) {
            if (!e.getKey().startsWith(boundPrefix)) continue;
            BlockPos pos = GhostBlocks.parsePos(e.getKey().substring(boundPrefix.length()));
            BlockState state = GhostBlocks.parseState(e.getValue());
            if (pos == null || state == null) continue;
            active.put(pos.asLong(), state);
        }
        if (!active.isEmpty()) {
            LOGGER.info("loaded {} ghost block(s) for {}", (Object)active.size(), (Object)boundPrefix);
        }
    }

    private static BlockState parseState(String snbt) {
        try {
            return NbtUtils.readBlockState((HolderGetter)BuiltInRegistries.BLOCK, (CompoundTag)TagParser.parseCompoundFully((String)snbt));
        }
        catch (Exception e) {
            return null;
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
        return FabricLoader.getInstance().getConfigDir().resolve("r3dmenu-ghostblocks.properties");
    }

    public static void load() {
        Path path = GhostBlocks.configPath();
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
            all.put(key, p.getProperty(key));
        }
    }

    private static void save() {
        Properties p = new Properties();
        all.forEach(p::setProperty);
        Path path = GhostBlocks.configPath();
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            try (OutputStream out = Files.newOutputStream(path, new OpenOption[0]);){
                p.store(out, "r3dmenu ghost blocks: server|dimension|x,y,z = block state");
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

