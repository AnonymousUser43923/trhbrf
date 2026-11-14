/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.resources.Identifier
 */
package name.modid.client;

import com.mojang.blaze3d.platform.InputConstants;
import name.modid.client.AutoPearl;
import name.modid.client.DevMode;
import name.modid.client.DropperSkin;
import name.modid.client.FakeChat;
import name.modid.client.FakeCommands;
import name.modid.client.FakeElytraFlight;
import name.modid.client.FakeElytraSell;
import name.modid.client.FakeRoster;
import name.modid.client.GambleGui;
import name.modid.client.GhostBlocks;
import name.modid.client.GhostItems;
import name.modid.client.HiddenSlot;
import name.modid.client.InputLock;
import name.modid.client.JoinCheck;
import name.modid.client.JoinWorldFix;
import name.modid.client.JoinWorldFixScreenHooks;
import name.modid.client.KeyMining;
import name.modid.client.MacroProfile;
import name.modid.client.ModSettings;
import name.modid.client.ModSounds;
import name.modid.client.OptionsScreenHooks;
import name.modid.client.PayoutLog;
import name.modid.client.PhantomDropper;
import name.modid.client.PhantomItems;
import name.modid.client.PlankCommandBlock;
import name.modid.client.SkeletonSpawner;
import name.modid.client.WindowCloseGuard;
import name.modid.client.gui.FakeChatScreen;
import name.modid.client.gui.FakeScoreboardHud;
import name.modid.client.gui.GambleGuiHud;
import name.modid.client.gui.JoinWorldFixHud;
import name.modid.client.gui.KeyMiningHud;
import name.modid.client.gui.ModMenuScreen;
import name.modid.client.gui.ProfileGateScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;

public class R3dMenuClient
implements ClientModInitializer {
    private static KeyMapping openMenuKey;
    private static KeyMapping fakeChatKey;
    private static KeyMapping dropperSkinKey;
    private static KeyMapping gambleGuiKey;
    private static KeyMapping elytraLocatorHudKey;
    private static KeyMapping locatorCompleteKey;
    private static KeyMapping elytraFlyKey;
    private static final KeyMapping[] dropperSlotKeys;

    public void onInitializeClient() {
        ModSounds.register();
        ModSettings.load();
        PlankCommandBlock.load();
        SkeletonSpawner.load();
        GhostBlocks.load();
        InputLock.register();
        JoinCheck.register();
        MacroProfile.register();
        DevMode.load();
        OptionsScreenHooks.register();
        PayoutLog.register();
        FakeRoster.register();
        FakeCommands.register();
        AutoPearl.register();
        JoinWorldFixScreenHooks.register();
        HudElementRegistry.attachElementAfter((Identifier)VanillaHudElements.SCOREBOARD, (Identifier)Identifier.fromNamespaceAndPath((String)"r3dmenu", (String)"fake_scoreboard"), (HudElement)new FakeScoreboardHud());
        HudElementRegistry.attachElementAfter((Identifier)VanillaHudElements.SCOREBOARD, (Identifier)Identifier.fromNamespaceAndPath((String)"r3dmenu", (String)"gamble_gui"), (HudElement)new GambleGuiHud());
        HudElementRegistry.attachElementAfter((Identifier)VanillaHudElements.SCOREBOARD, (Identifier)Identifier.fromNamespaceAndPath((String)"r3dmenu", (String)"key_mining"), (HudElement)new KeyMiningHud());
        HudElementRegistry.attachElementAfter((Identifier)VanillaHudElements.SCOREBOARD, (Identifier)Identifier.fromNamespaceAndPath((String)"r3dmenu", (String)"join_world_fix"), (HudElement)new JoinWorldFixHud());
        KeyMapping.Category category = KeyMapping.Category.register((Identifier)Identifier.fromNamespaceAndPath((String)"r3dmenu", (String)"main"));
        openMenuKey = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.r3dmenu.openmenu", InputConstants.Type.KEYSYM, 258, category));
        fakeChatKey = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.r3dmenu.fakechat", InputConstants.Type.KEYSYM, 82, category));
        dropperSkinKey = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.r3dmenu.dropperskin", InputConstants.Type.KEYSYM, 80, category));
        gambleGuiKey = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.r3dmenu.gambleguitoggle", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), category));
        elytraLocatorHudKey = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.r3dmenu.elytralocatorhud", InputConstants.Type.KEYSYM, 72, category));
        locatorCompleteKey = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.r3dmenu.locatorcomplete", InputConstants.Type.KEYSYM, 96, category));
        elytraFlyKey = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.r3dmenu.elytrafly", InputConstants.Type.KEYSYM, 71, category));
        for (int i = 0; i < dropperSlotKeys.length; ++i) {
            R3dMenuClient.dropperSlotKeys[i] = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.r3dmenu.dropperslot" + (i + 1), InputConstants.Type.KEYSYM, 49 + i, category));
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            WindowCloseGuard.install();
            while (openMenuKey.consumeClick()) {
                if (client.gui.screen() != null) continue;
                if (MacroProfile.isUnlocked()) {
                    client.gui.setScreen((Screen)new ModMenuScreen());
                    continue;
                }
                client.gui.setScreen((Screen)new ProfileGateScreen());
            }
            while (fakeChatKey.consumeClick()) {
                if (!FakeChat.enabled || client.gui.screen() != null) continue;
                client.gui.setScreen((Screen)new FakeChatScreen());
            }
            while (dropperSkinKey.consumeClick()) {
                DropperSkin.enabled = !DropperSkin.enabled;
            }
            while (gambleGuiKey.consumeClick()) {
                GambleGui.enabled = !GambleGui.enabled;
            }
            HiddenSlot.tick(client);
            while (elytraLocatorHudKey.consumeClick()) {
                KeyMining.toggleHud();
            }
            while (locatorCompleteKey.consumeClick()) {
                KeyMining.completeNow();
            }
            while (elytraFlyKey.consumeClick()) {
                FakeElytraFlight.toggle();
            }
            for (int i = 0; i < dropperSlotKeys.length; ++i) {
                while (dropperSlotKeys[i].consumeClick()) {
                    PhantomDropper.riggedSlot = i;
                }
            }
            ModSettings.tick();
            JoinWorldFix.tick(client);
            KeyMining.tick();
            if (client.player == null) {
                FakeElytraFlight.forceReset();
            }
            FakeElytraSell.tick(client);
            PhantomDropper.tick();
            GhostItems.tick();
            PlankCommandBlock.tick(client);
            SkeletonSpawner.tick(client);
            GhostBlocks.tick(client);
            if (client.player != null) {
                PhantomItems.reapply(client.player.getInventory());
            } else {
                PhantomItems.clear();
            }
        });
    }

    static {
        dropperSlotKeys = new KeyMapping[9];
    }
}

