/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package name.modid.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import name.modid.client.InputLock;
import name.modid.client.gui.InputLockOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Gui.class})
public class GuiInputLockOverlayMixin {
    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void r3dmenu$drawInputLock(DeltaTracker deltaTracker, boolean renderCrosshair, boolean sleeping, CallbackInfo ci, @Local GuiGraphicsExtractor graphics) {
        if (!InputLock.engaged()) {
            return;
        }
        InputLockOverlay.render(graphics, Minecraft.getInstance().font);
    }
}

