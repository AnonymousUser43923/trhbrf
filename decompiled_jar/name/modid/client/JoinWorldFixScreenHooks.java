/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.Screens
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
 *  net.minecraft.network.chat.Component
 */
package name.modid.client;

import name.modid.client.JoinWorldFix;
import name.modid.client.gui.JoinWorldFixScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

public final class JoinWorldFixScreenHooks {
    private JoinWorldFixScreenHooks() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof JoinMultiplayerScreen) {
                JoinWorldFixScreenHooks.addButtons(screen);
            }
        });
    }

    private static void addButtons(Screen screen) {
        Button toggle = Button.builder((Component)JoinWorldFixScreenHooks.toggleLabel(), btn -> {
            JoinWorldFix.armed = !JoinWorldFix.armed;
            JoinWorldFix.save();
            btn.setMessage(JoinWorldFixScreenHooks.toggleLabel());
            btn.setTooltip(JoinWorldFixScreenHooks.toggleTooltip());
        }).bounds(5, 5, 70, 20).build();
        toggle.setTooltip(JoinWorldFixScreenHooks.toggleTooltip());
        Screens.getWidgets((Screen)screen).add(toggle);
        Button settings = Button.builder((Component)Component.literal((String)"JWF"), btn -> Minecraft.getInstance().gui.setScreen((Screen)new JoinWorldFixScreen(screen))).bounds(79, 5, 34, 20).build();
        settings.setTooltip(Tooltip.create((Component)Component.literal((String)"JoinWorldFix settings")));
        Screens.getWidgets((Screen)screen).add(settings);
    }

    private static Component toggleLabel() {
        return Component.literal((String)(JoinWorldFix.armed ? "JWF: ON" : "JWF: OFF"));
    }

    private static Tooltip toggleTooltip() {
        return Tooltip.create((Component)Component.literal((String)(JoinWorldFix.armed ? "JoinWorldFix is armed - it will act if you get stuck joining." : "JoinWorldFix is off - click to arm it before joining.")));
    }
}

