/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package name.modid.client;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class DropperSkin {
    public static boolean enabled = false;
    public static ItemStack fillerItem = ItemStack.EMPTY;
    public static ItemStack skinItem = ItemStack.EMPTY;

    private DropperSkin() {
    }

    public static void ensureInit() {
        if (fillerItem.isEmpty()) {
            fillerItem = new ItemStack((ItemLike)Items.COBBLESTONE);
        }
        if (skinItem.isEmpty()) {
            skinItem = new ItemStack((ItemLike)Items.DIAMOND_BLOCK);
        }
    }

    public static boolean shouldDisguise(ItemStack stack) {
        if (!enabled) {
            return false;
        }
        DropperSkin.ensureInit();
        return !skinItem.isEmpty() && !fillerItem.isEmpty() && stack.is((Object)fillerItem.getItem()) && !stack.is((Object)skinItem.getItem());
    }
}

