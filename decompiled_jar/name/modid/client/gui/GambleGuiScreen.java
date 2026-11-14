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
import name.modid.client.GambleGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GambleGuiScreen
extends Screen {
    private final Screen parent;
    private EditBox ignBox;
    private EditBox minBox;
    private EditBox maxBox;
    private EditBox textBox;
    private static final int FIELD_W = 180;

    public GambleGuiScreen(Screen parent) {
        super((Component)Component.literal((String)"GAMBLEGUI"));
        this.parent = parent;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        GambleGui.save();
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    protected void init() {
        int x = this.width / 2 - 90;
        int y = this.height / 2 - 66;
        this.ignBox = this.field(x, y, "Your IGN", GambleGui.ign, 32, v -> {
            GambleGui.ign = v;
        });
        this.minBox = this.field(x, y += 34, "Minimum bet", GambleGui.minBet, 32, v -> {
            GambleGui.minBet = v;
        });
        this.maxBox = this.field(x, y += 34, "Maximum bet", GambleGui.maxBet, 32, v -> {
            GambleGui.maxBet = v;
        });
        this.textBox = this.field(x, y += 34, "Custom text", GambleGui.customText, 128, v -> {
            GambleGui.customText = v;
        });
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Apply"), btn -> {
            GambleGui.enabled = true;
            this.onClose();
        }).bounds(x, y += 42, 86, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Hide"), btn -> {
            GambleGui.enabled = false;
            GambleGui.save();
        }).bounds(x + 90 + 4, y, 86, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Back"), btn -> this.onClose()).bounds(x, y += 24, 180, 20).build());
    }

    private EditBox field(int x, int y, String hint, String value, int maxLen, Consumer<String> onChange) {
        EditBox box = new EditBox(this.font, x, y, 180, 18, (Component)Component.literal((String)hint));
        box.setHint((Component)Component.literal((String)hint));
        box.setMaxLength(maxLen);
        box.setValue(value);
        box.setResponder(onChange);
        this.addRenderableWidget((GuiEventListener)box);
        return box;
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, -1342177280);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int labelX = this.width / 2 - 90;
        graphics.text(this.font, "GAMBLEGUI", labelX, this.height / 2 - 84, -16121);
        this.label(graphics, "IGN", labelX, this.ignBox.getY() - 10);
        this.label(graphics, "Min bet", labelX, this.minBox.getY() - 10);
        this.label(graphics, "Max bet", labelX, this.maxBox.getY() - 10);
        this.label(graphics, "Custom text", labelX, this.textBox.getY() - 10);
    }

    private void label(GuiGraphicsExtractor graphics, String text, int x, int y) {
        graphics.text(this.font, text, x, y, -5197648);
    }
}

