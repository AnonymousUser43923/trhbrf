/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.screens.inventory.ContainerScreen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ChestMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 */
package name.modid.client.gui;

import name.modid.client.FakeScoreboard;
import name.modid.client.GhostItems;
import name.modid.client.PhantomItems;
import name.modid.client.SkeletonSpawner;
import name.modid.client.gui.SkeletonSpawnerContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class SkeletonSpawnerScreen
extends ContainerScreen {
    private final SkeletonSpawner.Store store;

    public SkeletonSpawnerScreen(SkeletonSpawner.Store store, Inventory inventory) {
        super(ChestMenu.sixRows((int)0, (Inventory)inventory, (Container)new SkeletonSpawnerContainer(store)), inventory, (Component)Component.literal((String)"1 skeleton spawner"));
        this.store = store;
    }

    protected void slotClicked(Slot slot, int slotId, int button, ContainerInput input) {
        boolean intoLoot;
        LocalPlayer player;
        if (slot != null) {
            slotId = slot.index;
        }
        if ((player = this.minecraft.player) == null) {
            return;
        }
        if (slotId >= 45 && slotId < 54) {
            if (input == ContainerInput.PICKUP && button == 0) {
                switch (slotId) {
                    case 48: {
                        this.sellAll(player);
                        break;
                    }
                    case 50: {
                        this.dropAll(player);
                        break;
                    }
                }
            }
            return;
        }
        ItemStack carried = ((ChestMenu)this.menu).getCarried();
        boolean bl = intoLoot = slotId >= 0 && slotId < 45;
        if (intoLoot && !carried.isEmpty() && !SkeletonSpawner.isLoot(carried)) {
            return;
        }
        if (input == ContainerInput.QUICK_MOVE && slotId >= 54 && slot != null && !SkeletonSpawner.isLoot(slot.getItem())) {
            return;
        }
        if (input == ContainerInput.SWAP && intoLoot) {
            return;
        }
        ((ChestMenu)this.menu).clicked(slotId, button, input, (Player)player);
        this.afterClick(player);
    }

    private void afterClick(LocalPlayer player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack s = inventory.getItem(i);
            if (!SkeletonSpawner.isLoot(s)) continue;
            PhantomItems.track(s);
        }
        ItemStack carried = ((ChestMenu)this.menu).getCarried();
        if (SkeletonSpawner.isLoot(carried)) {
            PhantomItems.track(carried);
        }
        for (ItemStack s : this.store.loot) {
            if (!PhantomItems.isPhantom(s)) continue;
            PhantomItems.forget(s);
            PhantomItems.untag(s);
        }
        this.store.mergePartialStacks();
        SkeletonSpawner.save();
    }

    private void sellAll(LocalPlayer player) {
        int bones = this.store.bones();
        if (bones == 0) {
            return;
        }
        double money = (double)bones * 1.0;
        FakeScoreboard.add(money);
        this.store.clearLoot();
        SkeletonSpawner.save();
        this.minecraft.gui.hud.getChat().addClientSystemMessage((Component)Component.literal((String)("Sold " + bones + " items for $" + FakeScoreboard.format(money))).withStyle(ChatFormatting.GREEN));
    }

    private void dropAll(LocalPlayer player) {
        if (this.minecraft.level == null || this.store.bones() == 0) {
            return;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        for (int i = 0; i < this.store.loot.size(); ++i) {
            ItemStack s = (ItemStack)this.store.loot.get(i);
            if (!SkeletonSpawner.isLoot(s)) continue;
            GhostItems.spawn(this.minecraft.level, eye.x, eye.y - 0.3, eye.z, s, look.x * 0.3 + (Math.random() - 0.5) * 0.05, look.y * 0.3 + 0.1, look.z * 0.3 + (Math.random() - 0.5) * 0.05);
        }
        this.store.clearLoot();
        SkeletonSpawner.save();
    }

    public void onClose() {
        SkeletonSpawner.save();
        this.minecraft.gui.setScreen(null);
    }
}

