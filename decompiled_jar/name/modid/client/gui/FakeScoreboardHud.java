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
 *  net.minecraft.network.chat.MutableComponent
 */
package name.modid.client.gui;

import name.modid.client.FakeScoreboard;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public class FakeScoreboardHud
implements HudElement {
    private static final int LINE_HEIGHT = 9;

    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!FakeScoreboard.enabled) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Font font = mc.font;
        MutableComponent title = Component.literal((String)mc.player.getName().getString());
        MutableComponent line = Component.literal((String)"$").withStyle(ChatFormatting.GREEN).append((Component)Component.literal((String)(" " + FakeScoreboard.amount)).withStyle(ChatFormatting.WHITE));
        int titleWidth = font.width((FormattedText)title);
        int maxWidth = Math.max(titleWidth, font.width((FormattedText)line));
        int numEntries = 1;
        int contentHeight = numEntries * 9;
        int bottomOfList = graphics.guiHeight() / 2 + contentHeight / 3;
        int nameX = graphics.guiWidth() - maxWidth - 3;
        int rightEdge = graphics.guiWidth() - 1;
        int listTop = bottomOfList - numEntries * 9;
        int bgColor = mc.options.getBackgroundColor(0.3f);
        int bgColorTitle = mc.options.getBackgroundColor(0.4f);
        graphics.fill(nameX - 2, listTop - 9 - 1, rightEdge, listTop - 1, bgColorTitle);
        graphics.fill(nameX - 2, listTop - 1, rightEdge, bottomOfList, bgColor);
        graphics.text(font, (Component)title, nameX + maxWidth / 2 - titleWidth / 2, listTop - 9, -1, false);
        int lineY = bottomOfList - numEntries * 9;
        graphics.text(font, (Component)line, nameX, lineY, -1, false);
    }
}

