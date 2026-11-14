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
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.network.chat.Component
 */
package name.modid.client.gui;

import name.modid.client.AutoPearl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public class AutoPearlScreen
extends Screen {
    private final Screen parent;
    private EditBox mainBox;
    private EditBox pullerBox;
    private EditBox heartsBox;
    private Button keyButton;
    private Button autoPullButton;
    private Button enabledButton;
    private boolean listeningForKey = false;
    private static final int FIELD_W = 200;

    public AutoPearlScreen(Screen parent) {
        super((Component)Component.literal((String)"Auto Pearl"));
        this.parent = parent;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.applyFields();
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    private void applyFields() {
        AutoPearl.mainAccount = this.mainBox.getValue().trim();
        AutoPearl.pullerAccount = this.pullerBox.getValue().trim();
        try {
            double hearts = Double.parseDouble(this.heartsBox.getValue().trim());
            AutoPearl.lowHealthHearts = Math.max(0.5, Math.min(hearts, 20.0));
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
    }

    protected void init() {
        if (AutoPearl.mainAccount.isEmpty() && AutoPearl.pullerAccount.isEmpty()) {
            AutoPearl.mainAccount = AutoPearl.myName();
        }
        int x = this.width / 2 - 100;
        int y = this.height / 2 - 96;
        this.mainBox = this.field(x, y, "Main account (the one that teleports)", AutoPearl.mainAccount, 16);
        this.pullerBox = this.field(x, y += 30, "Puller account (stands at the trapdoor)", AutoPearl.pullerAccount, 16);
        this.keyButton = Button.builder((Component)this.keyLabel(), btn -> {
            this.listeningForKey = true;
            btn.setMessage((Component)Component.literal((String)"Press a key... (Esc to cancel)"));
        }).bounds(x, y += 34, 200, 20).build();
        this.addRenderableWidget((GuiEventListener)this.keyButton);
        int heartsW = 44;
        this.autoPullButton = Button.builder((Component)this.autoPullLabel(), btn -> {
            AutoPearl.autoPullOnLowHealth = !AutoPearl.autoPullOnLowHealth;
            btn.setMessage(this.autoPullLabel());
        }).bounds(x, y += 24, 200 - heartsW - 4, 20).build();
        this.addRenderableWidget((GuiEventListener)this.autoPullButton);
        this.heartsBox = new EditBox(this.font, x + 200 - heartsW, y + 1, heartsW, 18, (Component)Component.literal((String)"Hearts"));
        this.heartsBox.setHint((Component)Component.literal((String)"hearts"));
        this.heartsBox.setMaxLength(4);
        this.heartsBox.setValue(AutoPearlScreen.fmt(AutoPearl.lowHealthHearts));
        this.addRenderableWidget((GuiEventListener)this.heartsBox);
        this.enabledButton = Button.builder((Component)this.enabledLabel(), btn -> {
            AutoPearl.enabled = !AutoPearl.enabled;
            btn.setMessage(this.enabledLabel());
        }).bounds(x, y += 24, 200, 20).build();
        this.addRenderableWidget((GuiEventListener)this.enabledButton);
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Save & Back"), btn -> this.onClose()).bounds(x, y += 28, 200, 20).build());
    }

    public boolean keyPressed(KeyEvent event) {
        if (this.listeningForKey) {
            this.listeningForKey = false;
            if (event.key() != 256) {
                AutoPearl.key = event.key();
            }
            this.keyButton.setMessage(this.keyLabel());
            return true;
        }
        return super.keyPressed(event);
    }

    private Component keyLabel() {
        return Component.literal((String)("Pull key: " + AutoPearl.keyName()));
    }

    private Component autoPullLabel() {
        return Component.literal((String)("Auto-pull below: " + (AutoPearl.autoPullOnLowHealth ? "ON" : "OFF")));
    }

    private Component enabledLabel() {
        return Component.literal((String)("Auto Pearl: " + (AutoPearl.enabled ? "ON" : "OFF")));
    }

    private static String fmt(double hearts) {
        return hearts == Math.floor(hearts) ? Long.toString((long)hearts) : Double.toString(hearts);
    }

    private EditBox field(int x, int y, String hint, String value, int maxLen) {
        EditBox box = new EditBox(this.font, x, y, 200, 18, (Component)Component.literal((String)hint));
        box.setHint((Component)Component.literal((String)hint));
        box.setMaxLength(maxLen);
        box.setValue(value);
        this.addRenderableWidget((GuiEventListener)box);
        return box;
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int color;
        String status;
        graphics.fill(0, 0, this.width, this.height, -1342177280);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int labelX = this.width / 2 - 100;
        int top = this.height / 2 - 96;
        graphics.text(this.font, "AUTO PEARL", labelX, top - 30, -12652289);
        String me = AutoPearl.myName();
        String main = this.mainBox.getValue().trim();
        String puller = this.pullerBox.getValue().trim();
        if (me.equalsIgnoreCase(main) && !main.isEmpty()) {
            status = "You're " + me + " = MAIN. Press " + AutoPearl.keyName() + " to pull.";
            color = -10027128;
        } else if (me.equalsIgnoreCase(puller) && !puller.isEmpty()) {
            status = "You're " + me + " = PULLER. Face the trapdoor and wait.";
            color = -10027128;
        } else {
            status = "You're " + me + " - not the main or the puller yet.";
            color = -21948;
        }
        graphics.text(this.font, status, labelX, top - 16, color);
        this.label(graphics, "Main account", labelX, this.mainBox.getY() - 10);
        this.label(graphics, "Puller account", labelX, this.pullerBox.getY() - 10);
    }

    private void label(GuiGraphicsExtractor graphics, String text, int x, int y) {
        graphics.text(this.font, text, x, y, -5197648);
    }
}

