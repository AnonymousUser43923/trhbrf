/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package name.modid.client.gui;

import java.util.function.Consumer;
import name.modid.client.JoinWorldFix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class JoinWorldFixScreen
extends Screen {
    private final Screen parent;
    private EditBox commandBox;
    private EditBox delayBox;
    private EditBox retryBox;
    private EditBox reconnectBox;
    private EditBox filterBox;
    private static final int FIELD_W = 200;

    public JoinWorldFixScreen(Screen parent) {
        super((Component)Component.literal((String)"JoinWorldFix"));
        this.parent = parent;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.applyNumberFields();
        JoinWorldFix.save();
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    protected void init() {
        int x = this.width / 2 - 100;
        int y = this.height / 2 - 90;
        this.commandBox = this.field(x, y, "Command (no /)", JoinWorldFix.command, 128, v -> {
            JoinWorldFix.command = v;
        });
        this.delayBox = this.field(x, y += 30, "Auto-send after (s)", JoinWorldFixScreen.fmt(JoinWorldFix.delaySeconds), 8, null);
        this.retryBox = this.field(x, y += 30, "Retry every (s)", JoinWorldFixScreen.fmt(JoinWorldFix.retrySeconds), 8, null);
        this.reconnectBox = this.field(x, y += 30, "Reconnect after (s, 0 = off)", JoinWorldFixScreen.fmt(JoinWorldFix.reconnectSeconds), 8, null);
        this.filterBox = this.field(x, y += 30, "Server filter (blank = any)", JoinWorldFix.serverFilter, 64, v -> {
            JoinWorldFix.serverFilter = v;
        });
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.overlayLabel(), btn -> {
            JoinWorldFix.showOverlay = !JoinWorldFix.showOverlay;
            JoinWorldFix.save();
            btn.setMessage(this.overlayLabel());
        }).bounds(x, y += 38, 200, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Save & Back"), btn -> this.onClose()).bounds(x, y += 24, 200, 20).build());
    }

    private Component overlayLabel() {
        return Component.literal((String)("Overlay: " + (JoinWorldFix.showOverlay ? "ON" : "OFF")));
    }

    private void applyNumberFields() {
        JoinWorldFix.delaySeconds = JoinWorldFixScreen.parse(this.delayBox.getValue(), JoinWorldFix.delaySeconds);
        JoinWorldFix.retrySeconds = JoinWorldFixScreen.parse(this.retryBox.getValue(), JoinWorldFix.retrySeconds);
        JoinWorldFix.reconnectSeconds = JoinWorldFixScreen.parse(this.reconnectBox.getValue(), JoinWorldFix.reconnectSeconds);
    }

    private static double parse(String raw, double fallback) {
        try {
            return Double.parseDouble(raw.trim());
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String fmt(double seconds) {
        return seconds == Math.floor(seconds) ? Long.toString((long)seconds) : Double.toString(seconds);
    }

    private EditBox field(int x, int y, String hint, String value, int maxLen, Consumer<String> onChange) {
        EditBox box = new EditBox(this.font, x, y, 200, 18, (Component)Component.literal((String)hint));
        box.setHint((Component)Component.literal((String)hint));
        box.setMaxLength(maxLen);
        box.setValue(value);
        if (onChange != null) {
            box.setResponder(onChange);
        }
        this.addRenderableWidget((GuiEventListener)box);
        return box;
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, -1342177280);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int labelX = this.width / 2 - 100;
        graphics.text(this.font, "JOINWORLDFIX", labelX, this.height / 2 - 108, -12652289);
        this.label(graphics, "Command", labelX, this.commandBox.getY() - 10);
        this.label(graphics, "Auto-send delay", labelX, this.delayBox.getY() - 10);
        this.label(graphics, "Retry interval", labelX, this.retryBox.getY() - 10);
        this.label(graphics, "Auto-reconnect", labelX, this.reconnectBox.getY() - 10);
        this.label(graphics, "Server filter", labelX, this.filterBox.getY() - 10);
    }

    private void label(GuiGraphicsExtractor graphics, String text, int x, int y) {
        graphics.text(this.font, text, x, y, -5197648);
    }
}

