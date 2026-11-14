/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 */
package name.modid.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class HiddenSlot {
    public static final int INDEX = 17;
    public static boolean enabled = false;
    private static boolean equalsWasDown = false;

    private HiddenSlot() {
    }

    public static void tick(Minecraft mc) {
        boolean down = InputConstants.isKeyDown((Window)mc.getWindow(), (int)61);
        if (down && !equalsWasDown && HiddenSlot.canToggleFrom(mc.gui.screen())) {
            enabled = !enabled;
            mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value()), (float)(enabled ? 1.6f : 0.8f), (float)0.35f));
        }
        equalsWasDown = down;
    }

    private static boolean canToggleFrom(Screen screen) {
        if (screen == null) {
            return true;
        }
        return screen instanceof AbstractContainerScreen && !(screen.getFocused() instanceof EditBox);
    }

    public static boolean isHidden(Slot slot) {
        Minecraft mc = Minecraft.getInstance();
        return enabled && mc.player != null && slot.container == mc.player.getInventory() && slot.getContainerSlot() == 17;
    }

    public static ItemStack contents(LocalPlayer player) {
        return player.getInventory().getItem(17);
    }

    public static boolean swapToMainHand(Minecraft mc) {
        LocalPlayer player = mc.player;
        ClientPacketListener connection = mc.getConnection();
        if (player == null || connection == null || HiddenSlot.contents(player).isEmpty()) {
            return false;
        }
        Inventory inventory = player.getInventory();
        int hotbar = inventory.getSelectedSlot();
        mc.gameMode.handleContainerInput(player.inventoryMenu.containerId, 17, hotbar, ContainerInput.SWAP, (Player)player);
        return true;
    }
}

