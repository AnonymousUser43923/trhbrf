/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package name.modid.client.gui;

import java.util.List;
import java.util.Locale;
import name.modid.client.KeyMining;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class KeyMiningHud
implements HudElement {
    private static final int PANEL_W = 306;
    private static final int PAD = 6;
    private static final int LINE = 9;
    private static final int BG = -435877371;
    private static final int BORDER = -13901749;
    private static final int DIM = -12805300;
    private static final int BRIGHT = -10748038;
    private static final int WHITE = -1048593;
    private static final int GOLD = -15798;

    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        KeyMining.Target target = KeyMining.target;
        if (!KeyMining.active || target == null || KeyMining.hudHidden) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        List<String> log = KeyMining.logSnapshot();
        int rows = 16;
        int panelH = 12 + rows * 9;
        int x = 6;
        int y = Math.max(6, graphics.guiHeight() / 2 - panelH / 2);
        int right = x + 306;
        int bottom = y + panelH;
        graphics.fill(x, y, right, bottom, -435877371);
        KeyMiningHud.outline(graphics, x, y, right, bottom, -13901749);
        int tx = x + 6;
        int textW = 294;
        int ty = y + 6;
        graphics.text(font, "ELYTRA LOCATOR  v2.6.2   [ SCAN ]", tx, ty, -10748038, true);
        graphics.text(font, "target : " + target.name().toUpperCase(Locale.ROOT).replace(' ', '_'), tx, ty += 9, -1048593, true);
        graphics.text(font, "searching chunk  " + KeyMining.currentChunk, tx, ty += 9, -12805300, true);
        ty += 18;
        for (int i = 0; i < 9; ++i) {
            String raw = i < log.size() ? log.get(i) : "";
            boolean newest = i == log.size() - 1;
            graphics.text(font, KeyMiningHud.trimToWidth(font, raw, textW), tx, ty, newest ? -10748038 : -12805300, true);
            ty += 9;
        }
        float p = KeyMining.progress();
        int barY = ty += 9;
        int barH = 7;
        graphics.fill(tx, barY, tx + textW, barY + barH, -15983092);
        graphics.fill(tx, barY, tx + (int)((float)textW * p), barY + barH, -13901749);
        KeyMiningHud.outline(graphics, tx, barY, tx + textW, barY + barH, -12805300);
        ty += 11;
        if (KeyMining.complete()) {
            graphics.text(font, "== ELYTRA LOCATED - 1x " + target.name() + " FOUND ==", tx, ty, -15798, true);
        } else {
            graphics.text(font, String.format(Locale.ROOT, "SCANNING  %.2f%%   ETA %s", Float.valueOf(p * 100.0f), KeyMiningHud.formatDuration(KeyMining.remainingMs())), tx, ty, -1048593, true);
        }
    }

    private static String formatDuration(long ms) {
        long total = ms / 1000L;
        return String.format(Locale.ROOT, "%02d:%02d:%02d", total / 3600L, total % 3600L / 60L, total % 60L);
    }

    private static String trimToWidth(Font font, String s, int maxW) {
        while (!s.isEmpty() && font.width(s) > maxW) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private static void outline(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1, x2, y1 + 1, color);
        graphics.fill(x1, y2 - 1, x2, y2, color);
        graphics.fill(x1, y1, x1 + 1, y2, color);
        graphics.fill(x2 - 1, y1, x2, y2, color);
    }
}

