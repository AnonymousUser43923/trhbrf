/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 */
package name.modid.client;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import name.modid.client.ElytraSkin;
import name.modid.client.FakeScoreboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FakeElytraSell {
    public static final Pattern PRICE = Pattern.compile("\\$\\s?[0-9][0-9,]*(?:\\.[0-9]+)?\\s?[KkMmBbTtQq]?");
    private static boolean sellOpen = false;
    private static int lastCount = 0;
    private static int tickCounter = 0;
    private static int lastSaleTick = -10000;
    private static double lastSaleFakeAmount = 0.0;

    private FakeElytraSell() {
    }

    public static void tick(Minecraft mc) {
        boolean open;
        ++tickCounter;
        boolean bl = open = mc.player != null && FakeElytraSell.isSellScreen(mc.gui.screen());
        if (open) {
            int now = FakeElytraSell.fakeElytraCount(mc.player.containerMenu);
            if (!sellOpen || now > lastCount) {
                lastCount = now;
            } else if (now < lastCount) {
                int sold = lastCount - now;
                double fake = (double)sold * ElytraSkin.priceValue();
                if (ElytraSkin.enabled && fake > 0.0) {
                    FakeScoreboard.add(fake);
                    lastSaleFakeAmount = fake;
                    lastSaleTick = tickCounter;
                }
                lastCount = now;
            }
        }
        sellOpen = open;
    }

    public static boolean isSellScreen(Screen s) {
        return s != null && s.getTitle().getString().toLowerCase(Locale.ROOT).contains("sell");
    }

    public static int fakeElytrasInOpenMenu() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !FakeElytraSell.isSellScreen(mc.gui.screen())) {
            return 0;
        }
        return FakeElytraSell.fakeElytraCount(mc.player.containerMenu);
    }

    private static int fakeElytraCount(AbstractContainerMenu menu) {
        int n = 0;
        for (Slot slot : menu.slots) {
            ItemStack st = slot.getItem();
            if (!st.is((Object)Items.COPPER_CHESTPLATE)) continue;
            n += st.getCount();
        }
        return n;
    }

    public static Component fakeSaleText(Component original) {
        if (tickCounter - lastSaleTick > 100 || lastSaleFakeAmount <= 0.0) {
            return null;
        }
        String s = original.getString();
        String lower = s.toLowerCase(Locale.ROOT);
        if (!lower.contains("$")) {
            return null;
        }
        if (!(lower.contains("sold") || lower.contains("sale") || lower.contains("sell") || lower.contains("receiv") || lower.contains("earn") || lower.contains("+"))) {
            return null;
        }
        Matcher m = PRICE.matcher(s);
        if (!m.find()) {
            return null;
        }
        String replaced = m.replaceAll(Matcher.quoteReplacement("$" + FakeScoreboard.format(lastSaleFakeAmount)));
        return Component.literal((String)replaced);
    }
}

