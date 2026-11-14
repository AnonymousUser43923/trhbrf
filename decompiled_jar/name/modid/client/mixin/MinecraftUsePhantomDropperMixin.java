/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package name.modid.client.mixin;

import name.modid.client.PhantomDropper;
import name.modid.client.gui.PhantomDropperScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Minecraft.class})
public class MinecraftUsePhantomDropperMixin {
    @Inject(method={"startUseItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$openPhantomDropper(CallbackInfo ci) {
        BlockHitResult hit;
        if (!PhantomDropper.enabled) {
            return;
        }
        Minecraft mc = (Minecraft)this;
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) {
            return;
        }
        if (mc.player.isShiftKeyDown()) {
            return;
        }
        HitResult hitResult = mc.hitResult;
        if (hitResult instanceof BlockHitResult && mc.level.getBlockState((hit = (BlockHitResult)hitResult).getBlockPos()).is((Object)Blocks.DROPPER)) {
            mc.gui.setScreen((Screen)new PhantomDropperScreen(hit.getBlockPos().immutable(), mc.player.getInventory()));
            ci.cancel();
        }
    }
}

