/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 */
package name.modid.client.gui;

import java.util.ArrayList;
import name.modid.client.GambleGui;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class GambleGuiHud
implements HudElement {
    private static final int LINE_HEIGHT = 11;
    private static final int PAD = 6;

    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!GambleGui.enabled) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        ArrayList<Object> lines = new ArrayList<Object>();
        Object header = GambleGui.ign.isBlank() ? "GAMBLE" : GambleGui.ign + "  \u2666  GAMBLE";
        lines.add(Component.literal((String)header).withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}));
        if (!GambleGui.minBet.isBlank()) {
            lines.add(GambleGuiHud.keyValue("Min Bet", GambleGui.minBet));
        }
        if (!GambleGui.maxBet.isBlank()) {
            lines.add(GambleGuiHud.keyValue("Max Bet", GambleGui.maxBet));
        }
        if (!GambleGui.customText.isBlank()) {
            lines.add(Component.literal((String)GambleGui.customText).withStyle(ChatFormatting.YELLOW));
        }
        int contentWidth = 0;
        for (Component component : lines) {
            contentWidth = Math.max(contentWidth, font.width((FormattedText)component));
        }
        int boxW = contentWidth + 12;
        int n = lines.size() * 11 + 12 - 2;
        int centerX = graphics.guiWidth() / 2;
        int x = centerX - boxW / 2;
        int y = 6;
        graphics.fill(x, y, x + boxW, y + n, -939524096);
        GambleGuiHud.outline(graphics, x, y, x + boxW, y + n, -16121);
        int textY = y + 6;
        for (Component component : lines) {
            graphics.text(font, component, centerX - font.width((FormattedText)component) / 2, textY, -1, true);
            textY += 11;
        }
    }

    private static Component keyValue(String key, String value) {
        return Component.literal((String)(key + ": ")).withStyle(ChatFormatting.GRAY).append((Component)Component.literal((String)value).withStyle(ChatFormatting.WHITE));
    }

    private static void outline(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1, x2, y1 + 1, color);
        graphics.fill(x1, y2 - 1, x2, y2, color);
        graphics.fill(x1, y1, x1 + 1, y2, color);
        graphics.fill(x2 - 1, y1, x2, y2, color);
    }
}

