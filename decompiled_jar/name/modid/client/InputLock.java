/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.OptionInstance
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundSource
 */
package name.modid.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import name.modid.client.AutoRespawn;
import name.modid.client.FrozenFrame;
import name.modid.client.JoinCheck;
import name.modid.client.MacroProfile;
import name.modid.client.gui.InputLockScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

public final class InputLock {
    public static final long LOCK_MS = 180000L;
    private static volatile boolean engaged = false;
    private static long releaseAtMs = 0L;
    private static double savedMasterVolume = -1.0;

    private InputLock() {
    }

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> InputLock.onServerMessage(message));
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, boundChatType, timestamp) -> InputLock.onServerMessage(message));
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            Minecraft client;
            AutoRespawn.tryRespawn(minecraft);
            if (!engaged) {
                if (client.gui.screen() == null && JoinCheck.triggerComboDown(client) && MacroProfile.isUnlocked()) {
                    Minecraft.getInstance().execute(InputLock::engage);
                }
                return;
            }
            if (client.getConnection() == null || System.currentTimeMillis() >= releaseAtMs) {
                InputLock.release();
                return;
            }
            if (InputConstants.isKeyDown((Window)client.getWindow(), (int)88) && InputConstants.isKeyDown((Window)client.getWindow(), (int)80)) {
                InputLock.release();
                return;
            }
            Screen current = client.gui.screen();
            if (current != null && !(current instanceof InputLockScreen)) {
                return;
            }
            if (current == null) {
                client.gui.setScreen((Screen)new InputLockScreen());
            }
        });
    }

    private static void onServerMessage(Component message) {
        String text = message.getString();
        if (!engaged && MacroProfile.isUnlocked() && text.contains(MacroProfile.mainName()) && JoinCheck.TRIGGER_PHRASE.matcher(text).find()) {
            Minecraft.getInstance().execute(InputLock::engage);
        }
    }

    public static void engage() {
        InputLock.muteGame();
        Minecraft minecraft = Minecraft.getInstance();
        engaged = true;
        releaseAtMs = System.currentTimeMillis() + 1800000L;
        if (!(minecraft.gui.screen() instanceof InputLockScreen)) {
            minecraft.gui.setScreen((Screen)new InputLockScreen());
        }
    }

    private static void muteGame() {
        try {
            Minecraft mc = Minecraft.getInstance();
            OptionInstance master = mc.options.getSoundSourceOptionInstance(SoundSource.MASTER);
            if (savedMasterVolume < 0.0) {
                savedMasterVolume = (Double)master.get();
            }
            master.set((Object)0.0);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void unmuteGame() {
        if (savedMasterVolume < 0.0) {
            return;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            OptionInstance master = mc.options.getSoundSourceOptionInstance(SoundSource.MASTER);
            master.set((Object)savedMasterVolume);
        }
        catch (Throwable throwable) {
        }
        finally {
            savedMasterVolume = -1.0;
        }
    }

    public static void release() {
        engaged = false;
        releaseAtMs = 0L;
        InputLock.unmuteGame();
        FrozenFrame.release();
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null && mc.gui.screen() instanceof InputLockScreen) {
            mc.gui.setScreen(null);
        }
    }

    public static boolean engaged() {
        return engaged;
    }

    public static long remainingMs() {
        return Math.max(0L, releaseAtMs - System.currentTimeMillis());
    }

    public static void releaseAfterKill() {
        releaseAtMs = System.currentTimeMillis() + (long)30000;
    }
}

