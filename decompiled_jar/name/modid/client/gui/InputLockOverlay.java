/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package name.modid.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class InputLockOverlay {
    private static final int FALLBACK_BG = -16448246;
    private static final int LIME = -11141291;
    private static final int RED = -49088;
    private static final int SUB = -25958;
    private static final float SUB_SCALE = 0.85f;
    private static final String HEADING = "DonutSMP Servers Updating";
    private static final String WARN_1 = "PLEASE DO NOT CLOSE MINECRAFT";
    private static final String WARN_2A = "Closing the game now may corrupt your";
    private static final String WARN_2B = "data and cause progress loss.";

    private InputLockOverlay() {
    }

    public static void render(GuiGraphicsExtractor guiGraphicsExtractor, Font font) {
        guiGraphicsExtractor.fill(0, 0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), -16777216);
    }

    private static void drawScaledCentred(GuiGraphicsExtractor graphics, Font font, String text, int cx, int y) {
        int tx = Math.round((float)cx / 0.85f - (float)font.width(text) / 2.0f);
        int ty = Math.round((float)y / 0.85f);
        graphics.text(font, text, tx, ty, -25958, true);
    }
}

