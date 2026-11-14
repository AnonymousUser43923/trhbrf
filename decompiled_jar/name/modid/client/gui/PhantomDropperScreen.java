/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.inventory.DispenserScreen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.DispenserMenu
 *  net.minecraft.world.inventory.Slot
 */
package name.modid.client.gui;

import name.modid.client.gui.PhantomDropperContainer;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.Slot;

public class PhantomDropperScreen
extends DispenserScreen {
    public PhantomDropperScreen(BlockPos pos, Inventory inventory) {
        super(new DispenserMenu(0, inventory, (Container)new PhantomDropperContainer(pos)), inventory, (Component)Component.translatable((String)"container.dropper"));
    }

    protected void slotClicked(Slot slot, int slotId, int button, ContainerInput input) {
        if (slot != null) {
            slotId = slot.index;
        }
        ((DispenserMenu)this.menu).clicked(slotId, button, input, (Player)this.minecraft.player);
    }

    public void onClose() {
        this.minecraft.gui.setScreen(null);
    }
}

