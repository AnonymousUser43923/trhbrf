/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Style
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.ItemLore
 *  net.minecraft.world.level.ItemLike
 */
package name.modid.client.gui;

import java.util.List;
import name.modid.client.SkeletonSpawner;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ItemLike;

class SkeletonSpawnerContainer
implements Container {
    static final int SELL_SLOT = 48;
    static final int INFO_SLOT = 49;
    static final int DROP_SLOT = 50;
    private final SkeletonSpawner.Store store;

    SkeletonSpawnerContainer(SkeletonSpawner.Store store) {
        this.store = store;
    }

    static boolean isButton(int slot) {
        return slot == 48 || slot == 49 || slot == 50;
    }

    public int getContainerSize() {
        return 54;
    }

    public boolean isEmpty() {
        return this.store.bones() == 0;
    }

    public ItemStack getItem(int slot) {
        if (slot < 45) {
            return (ItemStack)this.store.loot.get(slot);
        }
        return switch (slot) {
            case 48 -> SkeletonSpawnerContainer.button(Items.GOLD_INGOT, "Sell All", "Click to sell all loot");
            case 49 -> this.info();
            case 50 -> SkeletonSpawnerContainer.button(Items.DROPPER, "Drop Loot", "Click to drop all loot on the page");
            default -> ItemStack.EMPTY;
        };
    }

    public ItemStack removeItem(int slot, int amount) {
        if (slot >= 45) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = (ItemStack)this.store.loot.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = stack.split(amount);
        if (stack.isEmpty()) {
            this.store.loot.set(slot, (Object)ItemStack.EMPTY);
        }
        SkeletonSpawner.save();
        return taken;
    }

    public ItemStack removeItemNoUpdate(int slot) {
        if (slot >= 45) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = (ItemStack)this.store.loot.get(slot);
        this.store.loot.set(slot, (Object)ItemStack.EMPTY);
        return stack;
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot >= 45) {
            return;
        }
        this.store.loot.set(slot, (Object)stack);
        SkeletonSpawner.save();
    }

    public void setChanged() {
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public void clearContent() {
        this.store.clearLoot();
        SkeletonSpawner.save();
    }

    private static ItemStack button(Item item, String name, String hint) {
        ItemStack stack = new ItemStack((ItemLike)item);
        stack.set(DataComponents.CUSTOM_NAME, (Object)SkeletonSpawnerContainer.plain(name, ChatFormatting.WHITE));
        stack.set(DataComponents.LORE, (Object)new ItemLore(List.of(Component.literal((String)hint).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(Boolean.valueOf(true))))));
        return stack;
    }

    private ItemStack info() {
        ItemStack stack = new ItemStack((ItemLike)Items.SKELETON_SKULL);
        stack.set(DataComponents.CUSTOM_NAME, (Object)SkeletonSpawnerContainer.plain("1 skeleton Spawner", ChatFormatting.GREEN));
        stack.set(DataComponents.LORE, (Object)new ItemLore(List.of(SkeletonSpawnerContainer.plain("(" + this.store.percentFilled() + "% filled)", ChatFormatting.GREEN))));
        return stack;
    }

    private static Component plain(String text, ChatFormatting color) {
        return Component.literal((String)text).withStyle(Style.EMPTY.withColor(color).withItalic(Boolean.valueOf(false)));
    }
}

