/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.phys.Vec3
 */
package name.modid.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import name.modid.client.HiddenSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class KeyMining {
    public static final List<Target> TARGETS = List.of(new Target(Items.ELYTRA, "Elytra", 168));
    public static final int MAX_LOG = 9;
    public static volatile boolean active = false;
    public static volatile Target target = null;
    public static volatile boolean hudHidden = false;
    private static long startAtMs = 0L;
    private static long durationMs = 0L;
    private static final Deque<String> LOG = new ArrayDeque<String>();
    private static int tickCounter = 0;
    public static volatile String currentChunk = KeyMining.randomChunk();
    private static final double TP_JUMP_SQR = 64.0;
    private static final int NAVIGATE_TIMEOUT = 600;
    private static volatile NavigateStage navigateStage = NavigateStage.IDLE;
    private static Vec3 navigateFrom = null;
    private static int navigateWait = 0;
    public static final String NAVIGATE_COMMAND = "rtp";

    private KeyMining() {
    }

    public static void toggleHud() {
        hudHidden = !hudHidden;
    }

    public static synchronized void start(Target t) {
        target = t;
        durationMs = (long)t.minutes() * 60000L;
        startAtMs = System.currentTimeMillis();
        active = true;
        tickCounter = 0;
        LOG.clear();
        KeyMining.pushLine("locator start :: target " + KeyMining.assetName(t) + "  mode deep-scan");
        KeyMining.pushLine("loading region cache ... OK");
        KeyMining.pushLine("searching chunks  radius 4096  step 16");
    }

    public static synchronized void stop() {
        active = false;
        target = null;
        LOG.clear();
        navigateStage = NavigateStage.IDLE;
    }

    public static synchronized void completeNow() {
        if (!active) {
            KeyMining.start(TARGETS.get(0));
        }
        startAtMs = System.currentTimeMillis() - durationMs;
        KeyMining.pushLine("match found :: chunk " + currentChunk + "  confidence 99.7%");
        KeyMining.pushLine("route plotted - press NAVIGATE");
    }

    public static boolean navigating() {
        return navigateStage != NavigateStage.IDLE;
    }

    public static synchronized void navigate() {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener connection = mc.getConnection();
        if (connection == null || mc.player == null || KeyMining.navigating()) {
            return;
        }
        navigateFrom = mc.player.position();
        navigateWait = 600;
        navigateStage = NavigateStage.WAIT_FOR_TELEPORT;
        connection.sendCommand(NAVIGATE_COMMAND);
        KeyMining.pushLine("navigating :: rtp ... stand by");
    }

    private static void tickNavigate() {
        if (navigateStage != NavigateStage.WAIT_FOR_TELEPORT) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            navigateStage = NavigateStage.IDLE;
            return;
        }
        if (navigateFrom != null && mc.player.position().distanceToSqr(navigateFrom) > 64.0) {
            navigateStage = NavigateStage.IDLE;
            if (HiddenSlot.swapToMainHand(mc)) {
                KeyMining.pushLine("arrived :: " + currentChunk + "  elytra recovered");
            } else {
                KeyMining.pushLine("arrived :: nothing stashed to recover");
            }
            return;
        }
        if (--navigateWait <= 0) {
            navigateStage = NavigateStage.IDLE;
            KeyMining.pushLine("navigate timed out - no teleport seen");
        }
    }

    public static float progress() {
        if (!active || durationMs <= 0L) {
            return 0.0f;
        }
        float p = (float)(System.currentTimeMillis() - startAtMs) / (float)durationMs;
        if (p < 0.0f) {
            return 0.0f;
        }
        return Math.min(p, 1.0f);
    }

    public static boolean complete() {
        return active && KeyMining.progress() >= 1.0f;
    }

    public static long remainingMs() {
        return Math.max(0L, durationMs - (System.currentTimeMillis() - startAtMs));
    }

    public static synchronized void tick() {
        KeyMining.tickNavigate();
        if (!active) {
            return;
        }
        if (++tickCounter % 3 == 0) {
            currentChunk = KeyMining.randomChunk();
        }
        if (!KeyMining.complete() && tickCounter % 18 == 0) {
            KeyMining.pushLine(KeyMining.randomLogLine());
        }
    }

    public static synchronized List<String> logSnapshot() {
        return new ArrayList<String>(LOG);
    }

    private static void pushLine(String line) {
        LOG.addLast(line);
        while (LOG.size() > 9) {
            LOG.removeFirst();
        }
    }

    private static String assetName(Target t) {
        return t.name().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private static String randomLogLine() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int cx = r.nextInt(60000) - 30000;
        int cz = r.nextInt(60000) - 30000;
        int checked = (int)(KeyMining.progress() * 42000.0f) + r.nextInt(40);
        double rate = 40.0 + r.nextDouble() * 80.0;
        return switch (r.nextInt(7)) {
            case 0 -> String.format(Locale.ROOT, "[scan] searching chunks  %d checked  %.0f chunks/s", checked, rate);
            case 1 -> String.format(Locale.ROOT, "chunk %d, %d  biome %s  no match", cx, cz, KeyMining.randomBiome(r));
            case 2 -> "[region] loading r." + (cx >> 9) + "." + (cz >> 9) + ".mca";
            case 3 -> "[warn] chunk " + cx + " " + cz + " not loaded - queued";
            case 4 -> String.format(Locale.ROOT, "searching chunks %d .. %d", cx, cx + 512);
            case 5 -> "checking end ship at chunk " + cx + ", " + cz + " ... empty";
            default -> "candidate chunk " + cx + ", " + cz + " ... verifying";
        };
    }

    private static String randomBiome(ThreadLocalRandom r) {
        String[] b = new String[]{"plains", "taiga", "desert", "the_end", "savanna", "forest", "ocean", "badlands"};
        return b[r.nextInt(b.length)];
    }

    private static String randomChunk() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return r.nextInt(60000) - 30000 + ", " + (r.nextInt(60000) - 30000);
    }

    public record Target(Item item, String name, int minutes) {
    }

    private static enum NavigateStage {
        IDLE,
        WAIT_FOR_TELEPORT;

    }
}

