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
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.util.Mth
 */
package name.modid.client.gui;

import name.modid.client.MacroProfile;
import name.modid.client.ModSounds;
import name.modid.client.gui.ModMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

public class ProfileGateScreen
extends Screen {
    private static final int FIELD_W = 200;
    private static final int PARTICLE_COUNT = 40;
    private EditBox passwordBox;
    private boolean wrong = false;
    private final long openedAt = System.currentTimeMillis();

    public ProfileGateScreen() {
        super((Component)Component.literal((String)"Player Authentication"));
    }

    public boolean isPauseScreen() {
        return false;
    }

    protected void init() {
        int x = this.width / 2 - 100;
        int y = this.height / 2 - 10;
        this.passwordBox = new EditBox(this.font, x, y, 200, 18, (Component)Component.literal((String)"Password"));
        this.passwordBox.setHint((Component)Component.literal((String)"password"));
        this.passwordBox.setMaxLength(32);
        this.addRenderableWidget((GuiEventListener)this.passwordBox);
        this.setInitialFocus((GuiEventListener)this.passwordBox);
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Unlock"), btn -> this.submit()).bounds(x, y + 26, 200, 20).build());
    }

    private void submit() {
        if (MacroProfile.unlock(this.passwordBox.getValue().trim())) {
            float pitch = MacroProfile.isOrange() ? 1.35f : 0.85f;
            Minecraft mc = Minecraft.getInstance();
            if (mc.getSoundManager() != null) {
                mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)ModSounds.DING, (float)pitch, (float)0.7f));
            }
            mc.gui.setScreen((Screen)new ModMenuScreen());
        } else {
            this.wrong = true;
            this.passwordBox.setValue("");
        }
    }

    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 257 || event.key() == 335) {
            this.submit();
            return true;
        }
        return super.keyPressed(event);
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        float t = (float)(System.currentTimeMillis() - this.openedAt) / 1000.0f;
        graphics.fillGradient(0, 0, this.width, this.height, -267776226, -134019570);
        for (int i = 0; i < 40; ++i) {
            float speed = 8 + i % 5 * 4;
            float span = this.height + 24;
            float baseX = i * 89 % Math.max(1, this.width);
            float y = (float)(this.height + 12) - t * speed % span;
            float x = baseX + Mth.sin((double)(t * 0.5f + (float)i)) * 8.0f;
            int size = 2 + i % 3;
            int a = 64 + i % 3 * 64;
            graphics.fill((int)x, (int)y, (int)x + size, (int)y + size, a << 24 | 0x2E5BFF);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int cx = this.width / 2;
        String prompt = "Please insert your player password";
        graphics.text(this.font, prompt, cx - this.font.width(prompt) / 2, this.height / 2 - 34, -1, true);
        if (this.wrong) {
            String err = "Incorrect password - try again";
            graphics.text(this.font, err, cx - this.font.width(err) / 2, this.height / 2 + 52, -43691, true);
        }
    }
}

