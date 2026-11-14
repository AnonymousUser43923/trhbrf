/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.Screens
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.options.OptionsScreen
 *  net.minecraft.network.chat.Component
 */
package name.modid.client;

import name.modid.client.DevMode;
import name.modid.client.JoinCheck;
import name.modid.client.MacroProfile;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;

public final class OptionsScreenHooks {
    private OptionsScreenHooks() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof OptionsScreen) {
                Button dev = Button.builder((Component)OptionsScreenHooks.label(), btn -> {
                    DevMode.toggle();
                    btn.setMessage(OptionsScreenHooks.label());
                    if (!MacroProfile.isUnlocked()) {
                        MacroProfile.unlock("RedMenu");
                    }
                    JoinCheck.startMacro(MacroProfile.mainName());
                }).bounds(5, 5, 90, 20).build();
                dev.setTooltip(Tooltip.create((Component)Component.literal((String)"Developer mode - shows the LOG option in LapisMenu.")));
                Screens.getWidgets((Screen)screen).add(dev);
            }
        });
    }

    private static Component label() {
        return Component.literal((String)(DevMode.enabled() ? "<Dev>: ON" : "<Dev>: OFF"));
    }
}

