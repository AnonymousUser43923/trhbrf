/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.LoadingOverlay
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.client.sounds.SoundManager
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package name.modid.client.mixin;

import name.modid.client.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LoadingOverlay.class})
public class LoadingOverlayIntroMixin {
    @Unique
    private static final int R3DMENU_FRAMES = 84;
    @Unique
    private static final long R3DMENU_MS_PER_FRAME = 50L;
    @Unique
    private static final int R3DMENU_SRC_W = 426;
    @Unique
    private static final int R3DMENU_SRC_H = 240;
    @Unique
    private static final long R3DMENU_DURATION = 4200L;
    @Unique
    private static boolean r3dmenu$played = false;
    @Unique
    private long r3dmenu$start = 0L;
    @Unique
    private boolean r3dmenu$active = false;
    @Unique
    private boolean r3dmenu$setup = false;
    @Unique
    private boolean r3dmenu$soundStarted = false;

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void r3dmenu$playIntro(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!this.r3dmenu$setup) {
            this.r3dmenu$setup = true;
            boolean bl = this.r3dmenu$active = !r3dmenu$played;
            if (this.r3dmenu$active) {
                r3dmenu$played = true;
                this.r3dmenu$start = System.currentTimeMillis();
                LoadingOverlayIntroMixin.r3dmenu$preload(Minecraft.getInstance());
            }
        }
        if (!this.r3dmenu$active) {
            return;
        }
        this.r3dmenu$tryPlaySound(Minecraft.getInstance());
        long elapsed = System.currentTimeMillis() - this.r3dmenu$start;
        int frame = (int)(elapsed / 50L % 84L);
        int w = g.guiWidth();
        int h = g.guiHeight();
        g.nextStratum();
        g.fill(0, 0, w, h, -16777216);
        float scale = Math.min((float)w / 426.0f, (float)h / 240.0f);
        int dw = Math.round(426.0f * scale);
        int dh = Math.round(240.0f * scale);
        int x0 = (w - dw) / 2;
        int y0 = (h - dh) / 2;
        g.blit(LoadingOverlayIntroMixin.r3dmenu$frameId(frame), x0, y0, x0 + dw, y0 + dh, 0.0f, 1.0f, 0.0f, 1.0f);
    }

    @Inject(method={"isReadyToFadeOut"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$holdForIntro(CallbackInfoReturnable<Boolean> cir) {
        if (this.r3dmenu$active && this.r3dmenu$start > 0L && System.currentTimeMillis() - this.r3dmenu$start < 4200L) {
            cir.setReturnValue((Object)false);
        }
    }

    @Unique
    private static Identifier r3dmenu$frameId(int frame) {
        return Identifier.fromNamespaceAndPath((String)"r3dmenu", (String)String.format("textures/gui/intro/frame_%03d.png", frame));
    }

    @Unique
    private static void r3dmenu$preload(Minecraft mc) {
        try {
            for (int i = 0; i < 84; ++i) {
                mc.getTextureManager().getTexture(LoadingOverlayIntroMixin.r3dmenu$frameId(i));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Unique
    private void r3dmenu$tryPlaySound(Minecraft mc) {
        if (this.r3dmenu$soundStarted) {
            return;
        }
        try {
            SoundManager sm = mc.getSoundManager();
            if (sm != null && sm.getSoundEvent(ModSounds.INTRO_ID) != null) {
                sm.play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)ModSounds.INTRO, (float)1.0f, (float)1.0f));
                this.r3dmenu$soundStarted = true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

