/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.Rarity
 */
package name.modid.client;

import name.modid.client.DropperSkin;
import name.modid.client.ElytraSkin;
import name.modid.client.PlankCommandBlock;
import name.modid.client.SkeletonSpawner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

public final class ItemDisguises {
    private ItemDisguises() {
    }

    public static Disguise of(ItemStack stack) {
        if (DropperSkin.shouldDisguise(stack)) {
            return new Disguise(DropperSkin.skinItem, null, null, false);
        }
        if (ElytraSkin.disguises(stack)) {
            return new Disguise(ElytraSkin.elytraLike(stack), null, Rarity.EPIC, true);
        }
        if (stack.is((Object)Items.JUNGLE_PLANKS)) {
            return new Disguise(PlankCommandBlock.commandBlockItem(), null, Rarity.EPIC, false);
        }
        if (stack.is((Object)((Item)Items.STAINED_GLASS_PANE.white()))) {
            return new Disguise(SkeletonSpawner.spawnerItem(), SkeletonSpawner.spawnerName(), null, false);
        }
        return null;
    }

    public record Disguise(ItemStack as, Component name, Rarity rarity, boolean minimalTooltip) {
    }
}

