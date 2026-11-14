/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.extract.LevelExtractor
 *  net.minecraft.client.renderer.state.level.BlockOutlineRenderState
 *  net.minecraft.client.renderer.state.level.LevelRenderState
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package name.modid.client.mixin;

import name.modid.client.GhostBlocks;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelExtractor.class})
public class LevelExtractorOutlineMixin {
    @Inject(method={"extractBlockOutline"}, at={@At(value="RETURN")})
    private void r3dmenu$ghostBlockOutline(Camera camera, LevelRenderState state, CallbackInfo ci) {
        BlockHitResult hit;
        HitResult hitResult;
        if (state.blockOutlineRenderState != null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !((hitResult = mc.hitResult) instanceof BlockHitResult) || (hit = (BlockHitResult)hitResult).getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockState ghost = GhostBlocks.at(hit.getBlockPos());
        if (ghost == null) {
            return;
        }
        state.blockOutlineRenderState = new BlockOutlineRenderState(hit.getBlockPos(), false, ((Boolean)mc.options.highContrastBlockOutline().get()).booleanValue(), ghost.getShape((BlockGetter)mc.level, hit.getBlockPos(), CollisionContext.of((Entity)camera.entity())));
    }
}

