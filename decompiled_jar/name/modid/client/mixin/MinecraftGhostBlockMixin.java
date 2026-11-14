/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package name.modid.client.mixin;

import name.modid.client.GhostBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Minecraft.class})
public class MinecraftGhostBlockMixin {
    @Inject(method={"pick(F)V"}, at={@At(value="RETURN")})
    private void r3dmenu$pickGhostBlocks(float partialTick, CallbackInfo ci) {
        Minecraft mc = (Minecraft)this;
        if (mc.player == null || mc.level == null || mc.getCameraEntity() == null) {
            return;
        }
        BlockHitResult ghost = GhostBlocks.pick(mc.getCameraEntity(), partialTick, mc.player.blockInteractionRange());
        if (ghost == null) {
            return;
        }
        HitResult vanilla = mc.hitResult;
        if (vanilla == null || vanilla.getType() == HitResult.Type.MISS || mc.getCameraEntity().getEyePosition(partialTick).distanceToSqr(ghost.getLocation()) < mc.getCameraEntity().getEyePosition(partialTick).distanceToSqr(vanilla.getLocation())) {
            mc.hitResult = ghost;
        }
    }

    @Inject(method={"startAttack"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$breakGhostBlock(CallbackInfoReturnable<Boolean> cir) {
        HitResult hitResult;
        Minecraft mc = (Minecraft)this;
        if (GhostBlocks.targeted(mc) && (hitResult = mc.hitResult) instanceof BlockHitResult) {
            BlockHitResult hit = (BlockHitResult)hitResult;
            GhostBlocks.breakAt(mc, hit.getBlockPos());
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(method={"continueAttack"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$holdOnGhostBlock(boolean leftClick, CallbackInfo ci) {
        Minecraft mc = (Minecraft)this;
        if (leftClick && GhostBlocks.targeted(mc)) {
            ci.cancel();
        }
    }

    @Inject(method={"startUseItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$placeGhostBlock(CallbackInfo ci) {
        BlockHitResult hit;
        Minecraft mc = (Minecraft)this;
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) {
            return;
        }
        InteractionHand[] interactionHandArray = mc.hitResult;
        if (!(interactionHandArray instanceof BlockHitResult) || (hit = (BlockHitResult)interactionHandArray).getType() != HitResult.Type.BLOCK) {
            return;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            if (!GhostBlocks.tryPlace(mc, hand, hit)) continue;
            ci.cancel();
            return;
        }
        if (GhostBlocks.has(hit.getBlockPos())) {
            ci.cancel();
        }
    }
}

