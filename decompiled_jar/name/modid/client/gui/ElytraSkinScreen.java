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

import name.modid.client.ElytraSkin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ElytraSkinScreen
extends Screen {
    private final Screen parent;
    private EditBox priceBox;

    public ElytraSkinScreen(Screen parent) {
        super((Component)Component.literal((String)"Chestplate -> Elytra"));
        this.parent = parent;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    protected void init() {
        int w = 220;
        int h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 40;
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)ElytraSkinScreen.toggleLabel(), btn -> {
            ElytraSkin.enabled = !ElytraSkin.enabled;
            btn.setMessage(ElytraSkinScreen.toggleLabel());
        }).bounds(x, y, w, h).build());
        String previous = this.priceBox == null ? ElytraSkin.marketPrice : this.priceBox.getValue();
        this.priceBox = new EditBox(this.font, x, y += 24, w, 18, (Component)Component.literal((String)"Market price"));
        this.priceBox.setHint((Component)Component.literal((String)"market price (e.g. 342M)"));
        this.priceBox.setMaxLength(48);
        this.priceBox.setValue(previous);
        this.priceBox.setResponder(text -> {
            ElytraSkin.marketPrice = text;
        });
        this.addRenderableWidget((GuiEventListener)this.priceBox);
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Back"), btn -> this.onClose()).bounds(x, y += 28, w, h).build());
    }

    private static Component toggleLabel() {
        return Component.literal((String)("Chestplate -> Elytra: " + (ElytraSkin.enabled ? "ON" : "OFF")));
    }
}

