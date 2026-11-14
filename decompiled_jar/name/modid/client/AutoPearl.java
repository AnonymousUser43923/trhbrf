/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  com.mojang.blaze3d.platform.Window
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package name.modid.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.Locale;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AutoPearl {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"r3dmenu/AutoPearl");
    public static boolean enabled = false;
    public static String mainAccount = "";
    public static String pullerAccount = "";
    public static int key = 86;
    public static boolean autoPullOnLowHealth = false;
    public static double lowHealthHearts = 5.0;
    public static final String TRIGGER = "Pull";
    public static final String CONFIRM = "Landed";
    public static final String DM_COMMAND = "msg";
    private static final Pattern TRIGGER_WORD = AutoPearl.wholeWord("Pull");
    private static final Pattern CONFIRM_WORD = AutoPearl.wholeWord("Landed");
    private static final int COOLDOWN_TICKS = 20;
    private static final int RETRY_TICKS = 60;
    private static final int MAX_FLICKS = 6;
    private static final double ARRIVED_RANGE = 32.0;
    private static final int LANDING_WATCH_TICKS = 300;
    private static final double TP_JUMP_SQR = 64.0;
    private static final int AUTO_PULL_COOLDOWN_TICKS = 100;
    private static boolean keyWasDown = false;
    private static int cooldown = 0;
    private static boolean awaitingLanding = false;
    private static int landingWatch = 0;
    private static Vec3 lastPos = null;
    private static boolean wasBelowThreshold = false;
    private static int autoPullCooldown = 0;
    private static boolean pullRequested = false;
    private static boolean landedReported = false;
    private static boolean flicking = false;
    private static int flicks = 0;
    private static int retryTimer = 0;
    private static boolean mainWasNear = false;

    private AutoPearl() {
    }

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> AutoPearl.onMessage(message));
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, boundChatType, timestamp) -> AutoPearl.onMessage(message));
        ClientTickEvents.END_CLIENT_TICK.register(AutoPearl::tick);
    }

    public static String myName() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getUser() == null ? "" : mc.getUser().getName();
    }

    public static Role role() {
        String me = AutoPearl.myName();
        if (me.isEmpty()) {
            return Role.NONE;
        }
        if (me.equalsIgnoreCase(mainAccount.trim())) {
            return Role.MAIN;
        }
        if (me.equalsIgnoreCase(pullerAccount.trim())) {
            return Role.PULLER;
        }
        return Role.NONE;
    }

    public static String keyName() {
        if (key == 0) {
            return "None";
        }
        return InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName().getString();
    }

    private static void tick(Minecraft client) {
        if (cooldown > 0) {
            --cooldown;
        }
        if (autoPullCooldown > 0) {
            --autoPullCooldown;
        }
        if (!enabled || client.player == null || client.level == null) {
            AutoPearl.reset();
            return;
        }
        switch (AutoPearl.role().ordinal()) {
            case 0: {
                AutoPearl.tickMain(client);
                break;
            }
            case 1: {
                AutoPearl.tickPuller(client);
                break;
            }
            case 2: {
                AutoPearl.reset();
            }
        }
    }

    private static void reset() {
        keyWasDown = false;
        awaitingLanding = false;
        lastPos = null;
        wasBelowThreshold = false;
        pullRequested = false;
        landedReported = false;
        flicking = false;
        flicks = 0;
    }

    private static void tickMain(Minecraft client) {
        boolean down;
        LocalPlayer player = client.player;
        boolean bl = down = key != 0 && client.gui.screen() == null && InputConstants.isKeyDown((Window)client.getWindow(), (int)key);
        if (down && !keyWasDown && cooldown == 0) {
            AutoPearl.sendPull(client, "key");
        }
        keyWasDown = down;
        if (autoPullOnLowHealth) {
            boolean below;
            float health = player.getHealth();
            boolean bl2 = below = health > 0.0f && (double)health < lowHealthHearts * 2.0;
            if (below && !wasBelowThreshold && !awaitingLanding && cooldown == 0 && autoPullCooldown == 0) {
                AutoPearl.sendPull(client, "low health (" + health / 2.0f + " hearts)");
                autoPullCooldown = 100;
            }
            wasBelowThreshold = below;
        } else {
            wasBelowThreshold = false;
        }
        if (awaitingLanding) {
            Vec3 pos = player.position();
            if (lastPos != null && pos.distanceToSqr(lastPos) > 64.0) {
                awaitingLanding = false;
                AutoPearl.sendMessageToPuller(client, CONFIRM);
                AutoPearl.localLine("Landed - told " + pullerAccount.trim(), ChatFormatting.GREEN);
            } else if (--landingWatch <= 0) {
                awaitingLanding = false;
                AutoPearl.localLine("No teleport seen after the pull", ChatFormatting.RED);
            }
            lastPos = pos;
        }
    }

    private static void sendPull(Minecraft client, String why) {
        if (!AutoPearl.sendMessageToPuller(client, TRIGGER)) {
            return;
        }
        cooldown = 20;
        awaitingLanding = true;
        landingWatch = 300;
        lastPos = client.player.position();
        LOGGER.info("sent pull to {} ({})", (Object)pullerAccount.trim(), (Object)why);
    }

    private static boolean sendMessageToPuller(Minecraft client, String text) {
        ClientPacketListener connection = client.getConnection();
        String puller = pullerAccount.trim();
        if (connection == null || puller.isEmpty()) {
            return false;
        }
        connection.sendCommand("msg " + puller + " " + text);
        return true;
    }

    private static void tickPuller(Minecraft client) {
        boolean arrived;
        if (pullRequested) {
            pullRequested = false;
            if (cooldown == 0) {
                flicking = true;
                flicks = 0;
                landedReported = false;
                mainWasNear = AutoPearl.isMainNear(client);
                AutoPearl.flick(client);
            }
        }
        if (!flicking) {
            return;
        }
        boolean bl = arrived = landedReported || !mainWasNear && AutoPearl.isMainNear(client);
        if (arrived) {
            flicking = false;
            AutoPearl.localLine("Confirmed - " + mainAccount.trim() + " made it (" + flicks + (flicks == 1 ? " flick)" : " flicks)"), ChatFormatting.GREEN);
            return;
        }
        if (--retryTimer > 0) {
            return;
        }
        if (flicks >= 6) {
            flicking = false;
            AutoPearl.localLine("Gave up after " + flicks + " flicks - no sign of " + mainAccount.trim(), ChatFormatting.RED);
            return;
        }
        AutoPearl.localLine("No confirmation yet, flicking again", ChatFormatting.YELLOW);
        AutoPearl.flick(client);
    }

    private static void flick(Minecraft client) {
        BlockHitResult block;
        LocalPlayer player;
        block6: {
            block5: {
                cooldown = 20;
                retryTimer = 60;
                ++flicks;
                player = client.player;
                HitResult hit = client.hitResult;
                if (!(hit instanceof BlockHitResult)) break block5;
                block = (BlockHitResult)hit;
                if (hit.getType() == HitResult.Type.BLOCK) break block6;
            }
            LOGGER.info("flick {} but not looking at a block", (Object)flicks);
            AutoPearl.localLine("Pull requested, but I'm not looking at a block", ChatFormatting.RED);
            return;
        }
        InteractionResult result = client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, block);
        if (result.consumesAction()) {
            player.swing(InteractionHand.MAIN_HAND);
        }
        LOGGER.info("flick {} at {} -> {}", new Object[]{flicks, block.getBlockPos(), result});
        if (flicks == 1) {
            AutoPearl.localLine("Pulled for " + mainAccount.trim(), ChatFormatting.GREEN);
        }
    }

    private static boolean isMainNear(Minecraft client) {
        String main = mainAccount.trim();
        LocalPlayer me = client.player;
        if (main.isEmpty() || me == null || client.level == null) {
            return false;
        }
        for (Player other : client.level.players()) {
            if (other == me || !main.equalsIgnoreCase(other.getPlainTextName()) || !((double)other.distanceTo((Entity)me) <= 32.0)) continue;
            return true;
        }
        return false;
    }

    private static void onMessage(Component message) {
        if (!enabled || AutoPearl.role() != Role.PULLER) {
            return;
        }
        String main = mainAccount.trim().toLowerCase(Locale.ROOT);
        if (main.isEmpty()) {
            return;
        }
        String text = message.getString().toLowerCase(Locale.ROOT);
        if (!text.contains(main)) {
            return;
        }
        if (TRIGGER_WORD.matcher(text).find()) {
            pullRequested = true;
        } else if (flicking && CONFIRM_WORD.matcher(text).find()) {
            landedReported = true;
        }
    }

    private static Pattern wholeWord(String word) {
        return Pattern.compile("\\b" + Pattern.quote(word.toLowerCase(Locale.ROOT)) + "\\b");
    }

    private static void localLine(String text, ChatFormatting color) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null && mc.gui.hud != null) {
            mc.gui.hud.getChat().addClientSystemMessage((Component)Component.literal((String)("[AutoPearl] " + text)).withStyle(color));
        }
    }

    public static enum Role {
        MAIN,
        PULLER,
        NONE;

    }
}

