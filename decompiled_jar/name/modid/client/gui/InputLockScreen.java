/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 */
package name.modid.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import name.modid.client.InputLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class InputLockScreen
extends Screen {
    private static final int BG = -16448246;
    private boolean xHeld;
    private boolean pHeld;

    public InputLockScreen() {
        super((Component)Component.literal((String)"Syncing with server"));
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public void tick() {
        if (!InputLock.engaged() || InputLock.remainingMs() <= 0L) {
            InputLock.release();
            return;
        }
        Window window = Minecraft.getInstance().getWindow();
        boolean x = InputConstants.isKeyDown((Window)window, (int)88);
        boolean p = InputConstants.isKeyDown((Window)window, (int)80);
        if (x && p) {
            InputLock.release();
        }
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Window window = Minecraft.getInstance().getWindow();
        int w = Math.max(this.width, window.getGuiScaledWidth());
        int h = Math.max(this.height, window.getGuiScaledHeight());
        graphics.fill(-64, -64, w + 64, h + 64, -16777216);
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 88) {
            this.xHeld = true;
        }
        if (event.key() == 80) {
            this.pHeld = true;
        }
        if (this.xHeld && this.pHeld) {
            InputLock.release();
        }
        return true;
    }

    public boolean keyReleased(KeyEvent event) {
        if (event.key() == 88) {
            this.xHeld = false;
        }
        if (event.key() == 80) {
            this.pHeld = false;
        }
        return true;
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        return true;
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        return true;
    }

    public boolean mouseDragged(MouseButtonEvent click, double dragX, double dragY) {
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return true;
    }
}

