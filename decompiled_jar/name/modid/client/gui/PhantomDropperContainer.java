/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.Container
 *  net.minecraft.world.ContainerHelper
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package name.modid.client.gui;

import name.modid.client.PhantomDropper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

class PhantomDropperContainer
implements Container {
    private final NonNullList<ItemStack> items;

    PhantomDropperContainer(BlockPos pos) {
        this.items = PhantomDropper.contentsAt(pos);
    }

    public int getContainerSize() {
        return 9;
    }

    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (stack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public ItemStack getItem(int slot) {
        return (ItemStack)this.items.get(slot);
    }

    public ItemStack removeItem(int slot, int count) {
        return ContainerHelper.removeItem(this.items, (int)slot, (int)count);
    }

    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, (int)slot);
    }

    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, (Object)stack);
    }

    public void setChanged() {
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public void clearContent() {
        this.items.clear();
    }
}

