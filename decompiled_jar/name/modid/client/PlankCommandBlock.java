/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.CommandBlock
 *  net.minecraft.world.level.block.entity.CommandBlockEntity
 *  net.minecraft.world.level.block.entity.CommandBlockEntity$Mode
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import name.modid.client.gui.FakeCommandBlockScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlankCommandBlock {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"r3dmenu/PlankCommandBlock");
    public static final BlockState SOURCE = Blocks.JUNGLE_PLANKS.defaultBlockState();
    public static final BlockState COMMAND_BLOCK_STATE = Blocks.COMMAND_BLOCK.defaultBlockState();
    private static final int REPEAT_INTERVAL = 20;
    private static final int MIN_SEND_GAP = 10;
    private static final int MAX_CHAIN = 16;
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Map<String, Entry> all = new ConcurrentHashMap<String, Entry>();
    private static final Map<Long, Entry> active = new ConcurrentHashMap<Long, Entry>();
    private static final Map<Long, Runtime> runtime = new ConcurrentHashMap<Long, Runtime>();
    private static ClientLevel boundLevel = null;
    private static String boundPrefix = null;
    private static int sendGap = 0;
    private static ItemStack commandBlockItem = ItemStack.EMPTY;

    private PlankCommandBlock() {
    }

    public static ItemStack commandBlockItem() {
        if (commandBlockItem.isEmpty()) {
            commandBlockItem = new ItemStack((ItemLike)Items.COMMAND_BLOCK);
        }
        return commandBlockItem;
    }

    public static boolean disguises(BlockState state) {
        return state.is((Object)Blocks.JUNGLE_PLANKS);
    }

    public static BlockState stateAt(BlockPos pos) {
        Entry entry = active.get(pos.asLong());
        return entry == null ? COMMAND_BLOCK_STATE : entry.state();
    }

    public static void openEditor(Minecraft mc, BlockPos pos) {
        Entry entry = active.get(pos.asLong());
        BlockState state = entry == null ? COMMAND_BLOCK_STATE : entry.state();
        CommandBlockEntity fake = new CommandBlockEntity(pos.immutable(), state);
        if (mc.level != null) {
            fake.setLevel((Level)mc.level);
        }
        if (entry != null) {
            fake.getCommandBlock().setCommand(entry.command());
            fake.getCommandBlock().setTrackOutput(entry.trackOutput());
            fake.setAutomatic(entry.auto());
            Runtime rt = runtime.get(pos.asLong());
            if (rt != null && rt.lastOutput != null) {
                fake.getCommandBlock().setLastOutput(rt.lastOutput);
            }
        }
        mc.gui.setScreen((Screen)new FakeCommandBlockScreen(fake));
    }

    public static void configure(BlockPos pos, String command, CommandBlockEntity.Mode mode, boolean conditional, boolean auto, boolean trackOutput) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || boundPrefix == null) {
            return;
        }
        long key = pos.asLong();
        Entry previous = active.get(key);
        Direction facing = previous != null ? previous.facing() : (mc.player != null ? mc.player.getDirection() : Direction.NORTH);
        Entry entry = new Entry(command.strip(), mode, conditional, auto, trackOutput, facing);
        active.put(key, entry);
        all.put(boundPrefix + PlankCommandBlock.posKey(pos), entry);
        runtime.computeIfAbsent(key, k -> new Runtime());
        BlockState real = mc.level.getBlockState(pos);
        mc.level.setBlocksDirty(pos, real, real);
        LOGGER.info("configured {} as {}{} facing {}: \"{}\"", new Object[]{pos, mode, conditional ? " (conditional)" : "", facing, entry.command()});
        PlankCommandBlock.save();
        if (auto && mode == CommandBlockEntity.Mode.REDSTONE) {
            PlankCommandBlock.run(mc, pos, entry, 0);
        }
    }

    public static void tick(Minecraft mc) {
        ClientLevel level;
        if (sendGap > 0) {
            --sendGap;
        }
        if ((level = mc.level) != boundLevel) {
            PlankCommandBlock.bind(mc, level);
        }
        if (level == null || active.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<Long, Entry>> it = active.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Entry> e = it.next();
            BlockPos pos = BlockPos.of((long)e.getKey());
            if (!level.isLoaded(pos)) continue;
            if (!PlankCommandBlock.disguises(level.getBlockState(pos))) {
                it.remove();
                runtime.remove(e.getKey());
                all.remove(boundPrefix + PlankCommandBlock.posKey(pos));
                PlankCommandBlock.save();
                continue;
            }
            Entry entry = e.getValue();
            Runtime rt = runtime.computeIfAbsent(e.getKey(), k -> new Runtime());
            boolean powered = entry.auto() || level.hasNeighborSignal(pos);
            switch (entry.mode()) {
                case REDSTONE: {
                    if (!powered || rt.wasPowered) break;
                    PlankCommandBlock.run(mc, pos, entry, 0);
                    break;
                }
                case AUTO: {
                    if (!powered || rt.wasPowered && --rt.repeatCountdown > 0) break;
                    PlankCommandBlock.run(mc, pos, entry, 0);
                    rt.repeatCountdown = 20;
                    break;
                }
            }
            rt.wasPowered = powered;
        }
    }

    private static void run(Minecraft mc, BlockPos pos, Entry entry, int depth) {
        String command = entry.command();
        if (command.startsWith("/")) {
            command = command.substring(1).strip();
        }
        if (!command.isEmpty()) {
            if (sendGap > 0) {
                LOGGER.info("skipped {} - too soon after the last command", (Object)pos);
                return;
            }
            ClientPacketListener connection = mc.getConnection();
            if (connection == null) {
                return;
            }
            connection.sendCommand(command);
            sendGap = 10;
            LOGGER.info("ran {} at {}: /{}", new Object[]{entry.mode(), pos, command});
        }
        Runtime rt = runtime.computeIfAbsent(pos.asLong(), k -> new Runtime());
        if (entry.trackOutput()) {
            rt.lastOutput = Component.literal((String)("[" + LocalTime.now().format(STAMP) + "] Executed as " + (mc.player != null ? mc.player.getPlainTextName() : "player")));
        }
        if (depth >= 16 || mc.level == null) {
            return;
        }
        BlockPos next = pos.relative(entry.facing());
        Entry chained = active.get(next.asLong());
        if (chained != null && chained.mode() == CommandBlockEntity.Mode.SEQUENCE && (chained.auto() || mc.level.hasNeighborSignal(next))) {
            PlankCommandBlock.run(mc, next, chained, depth + 1);
        }
    }

    private static void bind(Minecraft mc, ClientLevel level) {
        boundLevel = level;
        active.clear();
        runtime.clear();
        if (level == null) {
            boundPrefix = null;
            return;
        }
        boundPrefix = PlankCommandBlock.serverId(mc) + "|" + String.valueOf(level.dimension().identifier()) + "|";
        for (Map.Entry<String, Entry> e : all.entrySet()) {
            BlockPos pos;
            if (!e.getKey().startsWith(boundPrefix) || (pos = PlankCommandBlock.parsePos(e.getKey().substring(boundPrefix.length()))) == null) continue;
            active.put(pos.asLong(), e.getValue());
        }
        if (!active.isEmpty()) {
            LOGGER.info("loaded {} command block(s) for {}", (Object)active.size(), (Object)boundPrefix);
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
        return FabricLoader.getInstance().getConfigDir().resolve("r3dmenu-commandblocks.properties");
    }

    public static void load() {
        Path path = PlankCommandBlock.configPath();
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
            Entry entry = PlankCommandBlock.decode(p.getProperty(key));
            if (entry == null) continue;
            all.put(key, entry);
        }
    }

    private static void save() {
        Properties p = new Properties();
        all.forEach((key, entry) -> p.setProperty((String)key, PlankCommandBlock.encode(entry)));
        Path path = PlankCommandBlock.configPath();
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            try (OutputStream out = Files.newOutputStream(path, new OpenOption[0]);){
                p.store(out, "r3dmenu fake command blocks: server|dimension|x,y,z = mode\\tconditional\\tauto\\ttrackOutput\\tfacing\\tcommand");
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static String encode(Entry e) {
        return e.mode().name() + "\t" + e.conditional() + "\t" + e.auto() + "\t" + e.trackOutput() + "\t" + e.facing().getName() + "\t" + e.command();
    }

    private static Entry decode(String raw) {
        String[] f = raw.split("\t", 6);
        if (f.length < 6) {
            return null;
        }
        try {
            Direction facing = Direction.byName((String)f[4]);
            return new Entry(f[5], CommandBlockEntity.Mode.valueOf((String)f[0]), Boolean.parseBoolean(f[1]), Boolean.parseBoolean(f[2]), Boolean.parseBoolean(f[3]), facing == null ? Direction.NORTH : facing);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static List<BlockPos> configured() {
        ArrayList<BlockPos> out = new ArrayList<BlockPos>();
        active.keySet().forEach(k -> out.add(BlockPos.of((long)k)));
        return out;
    }

    public record Entry(String command, CommandBlockEntity.Mode mode, boolean conditional, boolean auto, boolean trackOutput, Direction facing) {
        public BlockState state() {
            return (BlockState)((BlockState)Entry.blockFor(this.mode).defaultBlockState().setValue((Property)CommandBlock.FACING, (Comparable)this.facing)).setValue((Property)CommandBlock.CONDITIONAL, (Comparable)Boolean.valueOf(this.conditional));
        }

        private static Block blockFor(CommandBlockEntity.Mode mode) {
            return switch (mode) {
                default -> throw new MatchException(null, null);
                case CommandBlockEntity.Mode.AUTO -> Blocks.REPEATING_COMMAND_BLOCK;
                case CommandBlockEntity.Mode.SEQUENCE -> Blocks.CHAIN_COMMAND_BLOCK;
                case CommandBlockEntity.Mode.REDSTONE -> Blocks.COMMAND_BLOCK;
            };
        }
    }

    private static final class Runtime {
        boolean wasPowered = false;
        int repeatCountdown = 0;
        Component lastOutput = null;

        private Runtime() {
        }
    }
}

