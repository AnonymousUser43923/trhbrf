/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Screenshot
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 */
package name.modid.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class FrozenFrame {
    private static final Identifier ID = Identifier.fromNamespaceAndPath((String)"r3dmenu", (String)"frozen_frame");
    private static volatile boolean ready = false;

    private FrozenFrame() {
    }

    public static Identifier id() {
        return ready ? ID : null;
    }

    public static void capture() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer.mainRenderTarget() == null) {
            return;
        }
        try {
            Screenshot.takeScreenshot((RenderTarget)mc.gameRenderer.mainRenderTarget(), image -> mc.execute(() -> FrozenFrame.install(mc, image)));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void install(Minecraft mc, NativeImage image) {
        try {
            FrozenFrame.releaseTexture(mc);
            DynamicTexture texture = new DynamicTexture(() -> "r3dmenu/frozen_frame", image);
            mc.getTextureManager().register(ID, (AbstractTexture)texture);
            ready = true;
        }
        catch (Throwable t) {
            try {
                image.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            ready = false;
        }
    }

    public static void release() {
        FrozenFrame.releaseTexture(Minecraft.getInstance());
    }

    private static void releaseTexture(Minecraft mc) {
        ready = false;
        try {
            mc.getTextureManager().release(ID);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

