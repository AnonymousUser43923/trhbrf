/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package name.modid.client.gui;

import name.modid.client.FakeScoreboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FakeScoreboardScreen
extends Screen {
    private final Screen parent;
    private EditBox amountBox;

    public FakeScoreboardScreen(Screen parent) {
        super((Component)Component.literal((String)"Fake Scoreboard"));
        this.parent = parent;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    protected void init() {
        int w = 200;
        int h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 30;
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)FakeScoreboardScreen.toggleLabel(), btn -> {
            FakeScoreboard.enabled = !FakeScoreboard.enabled;
            btn.setMessage(FakeScoreboardScreen.toggleLabel());
        }).bounds(x, y, w, h).build());
        String previous = this.amountBox == null ? FakeScoreboard.amount : this.amountBox.getValue();
        this.amountBox = new EditBox(this.font, x, y += 24, w, 18, (Component)Component.literal((String)"Amount"));
        this.amountBox.setHint((Component)Component.literal((String)"amount (e.g. 100B)"));
        this.amountBox.setMaxLength(64);
        this.amountBox.setValue(previous);
        this.amountBox.setResponder(text -> {
            FakeScoreboard.amount = text;
        });
        this.addRenderableWidget((GuiEventListener)this.amountBox);
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Back"), btn -> this.onClose()).bounds(x, y += 28, w, h).build());
    }

    private static Component toggleLabel() {
        return Component.literal((String)("Scoreboard: " + (FakeScoreboard.enabled ? "ON" : "OFF")));
    }
}

