/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package name.modid.client.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import name.modid.client.ElytraSkin;
import name.modid.client.FakeElytraSell;
import name.modid.client.FakeScoreboard;
import name.modid.client.ItemDisguises;
import name.modid.client.SkeletonSpawner;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ItemStack.class})
public class ItemStackDisplayMixin {
    private static final Pattern WORTH_AMOUNT = Pattern.compile("\\$\\s*[0-9][0-9,._]*(?:\\.[0-9]+)?\\s*[kKmMbBtT]?");

    @Inject(method={"getHoverName"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$disguiseName(CallbackInfoReturnable<Component> cir) {
        ItemDisguises.Disguise disguise = ItemDisguises.of((ItemStack)this);
        if (disguise != null && disguise.name() != null) {
            cir.setReturnValue((Object)disguise.name());
        } else if (disguise != null && !disguise.as().isEmpty()) {
            cir.setReturnValue((Object)disguise.as().getHoverName());
        }
    }

    @Inject(method={"getRarity"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$disguiseRarity(CallbackInfoReturnable<Rarity> cir) {
        ItemDisguises.Disguise disguise = ItemDisguises.of((ItemStack)this);
        if (disguise != null && disguise.rarity() != null) {
            cir.setReturnValue((Object)disguise.rarity());
        }
    }

    @Inject(method={"getTooltipLines"}, at={@At(value="RETURN")}, cancellable=true)
    private void r3dmenu$trimTooltip(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
        ArrayList<Component> src;
        boolean confirmButton;
        int staged;
        ItemStack self = (ItemStack)this;
        ItemDisguises.Disguise disguise = ItemDisguises.of(self);
        ArrayList<Component> lines = null;
        if (self.is((Object)((Item)Items.STAINED_GLASS_PANE.white()))) {
            cir.setReturnValue(SkeletonSpawner.itemTooltip(self.getCount()));
            return;
        }
        if (disguise != null && disguise.minimalTooltip() && !disguise.as().isEmpty() && !(lines = new ArrayList<Component>(disguise.as().getTooltipLines(context, player, flag))).isEmpty()) {
            lines.set(0, self.getStyledHoverName());
        }
        double worth = -1.0;
        if (ElytraSkin.disguises(self)) {
            worth = ElytraSkin.priceValue();
        } else {
            int boxedElytras = ElytraSkin.fakeElytrasIn(self);
            int boxedSpawners = SkeletonSpawner.spawnersIn(self);
            if (boxedElytras > 0) {
                worth = (double)boxedElytras * ElytraSkin.priceValue();
            }
            if (boxedSpawners > 0) {
                worth = Math.max(worth, 0.0) + (double)boxedSpawners * 1000.0;
            }
        }
        if (worth >= 0.0) {
            if (lines == null) {
                lines = new ArrayList((Collection)cir.getReturnValue());
            }
            lines.removeIf(c -> {
                String text = c.getString();
                return text.toLowerCase(Locale.ROOT).contains("worth") || WORTH_AMOUNT.matcher(text).find();
            });
            while (!lines.isEmpty() && ((Component)lines.get(lines.size() - 1)).getString().isBlank()) {
                lines.remove(lines.size() - 1);
            }
            lines.add(ElytraSkin.worthLine(worth));
        }
        if (!ElytraSkin.disguises(self) && (staged = FakeElytraSell.fakeElytrasInOpenMenu()) > 0 && (confirmButton = (src = lines != null ? lines : (List)cir.getReturnValue()).stream().anyMatch(c -> c.getString().toLowerCase(Locale.ROOT).contains("sell")))) {
            if (lines == null) {
                lines = new ArrayList((Collection)cir.getReturnValue());
            }
            String fake = "$" + FakeScoreboard.format((double)staged * ElytraSkin.priceValue());
            for (int i = 0; i < lines.size(); ++i) {
                Matcher m = FakeElytraSell.PRICE.matcher(((Component)lines.get(i)).getString());
                if (!m.find()) continue;
                lines.set(i, (Component)Component.literal((String)m.replaceAll(Matcher.quoteReplacement(fake))).withStyle(ChatFormatting.GREEN));
            }
        }
        if (lines != null) {
            cir.setReturnValue(lines);
        }
    }
}

