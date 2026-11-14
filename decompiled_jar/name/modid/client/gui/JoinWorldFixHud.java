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

import name.modid.client.JoinWorldFix;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class JoinWorldFixHud
implements HudElement {
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!JoinWorldFix.showOverlay || !JoinWorldFix.isStuck()) {
            return;
        }
        String status = JoinWorldFix.status();
        if (status.isEmpty()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        String line = "[JWF] " + status;
        int x = 6;
        int y = 6;
        int w = font.width(line);
        graphics.fill(x - 3, y - 2, x + w + 3, y + 10, -1610612736);
        graphics.text(font, line, x, y, -12652289, true);
    }
}

