/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Registry
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.item.enchantment.ItemEnchantments$Mutable
 *  net.minecraft.world.level.ItemLike
 */
package name.modid.client;

import name.modid.client.FakeScoreboard;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;

public final class ElytraSkin {
    public static boolean enabled = false;
    public static String marketPrice = "342M";
    private static ItemEnchantments fakeEnchantments = null;
    private static ClientLevel fakeEnchantmentsLevel = null;

    private ElytraSkin() {
    }

    public static boolean disguises(ItemStack stack) {
        return enabled && stack.is((Object)Items.COPPER_CHESTPLATE);
    }

    public static double priceValue() {
        return FakeScoreboard.parse(marketPrice);
    }

    public static Component worthLine(double value) {
        return Component.literal((String)"Worth: ").withStyle(ChatFormatting.WHITE).append((Component)Component.literal((String)"$").withStyle(ChatFormatting.GREEN)).append((Component)Component.literal((String)FakeScoreboard.format(value)).withStyle(ChatFormatting.WHITE));
    }

    public static int fakeElytrasIn(ItemStack container) {
        if (!enabled || !container.is(ItemTags.SHULKER_BOXES)) {
            return 0;
        }
        ItemContainerContents contents = (ItemContainerContents)container.get(DataComponents.CONTAINER);
        if (contents == null) {
            return 0;
        }
        int[] count = new int[]{0};
        contents.nonEmptyItemCopyStream().forEach(stack -> {
            if (stack.is((Object)Items.COPPER_CHESTPLATE)) {
                count[0] = count[0] + stack.getCount();
            }
        });
        return count[0];
    }

    public static ItemStack elytraLike(ItemStack chestplate) {
        ItemStack elytra = new ItemStack((ItemLike)Items.ELYTRA);
        ItemEnchantments enchantments = (ItemEnchantments)chestplate.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) {
            enchantments = ElytraSkin.fakeEnchantments();
        }
        if (enchantments != null && !enchantments.isEmpty()) {
            elytra.set(DataComponents.ENCHANTMENTS, (Object)enchantments);
        }
        return elytra;
    }

    private static ItemEnchantments fakeEnchantments() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        if (fakeEnchantments == null || fakeEnchantmentsLevel != level) {
            Registry registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutable.set((Holder)registry.getOrThrow(Enchantments.MENDING), 1);
            mutable.set((Holder)registry.getOrThrow(Enchantments.UNBREAKING), 3);
            fakeEnchantments = mutable.toImmutable();
            fakeEnchantmentsLevel = level;
        }
        return fakeEnchantments;
    }
}

