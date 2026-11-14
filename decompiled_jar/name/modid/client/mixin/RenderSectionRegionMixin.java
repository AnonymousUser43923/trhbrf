/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.chunk.RenderSectionRegion
 *  net.minecraft.client.renderer.chunk.SectionCopy
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.SectionPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package name.modid.client.mixin;

import name.modid.client.GhostBlocks;
import name.modid.client.PlankCommandBlock;
import name.modid.client.SkeletonSpawner;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCopy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={RenderSectionRegion.class})
public class RenderSectionRegionMixin {
    @Shadow
    @Final
    private ClientLevel level;

    @Shadow
    private SectionCopy getSection(int sectionX, int sectionY, int sectionZ) {
        throw new AssertionError();
    }

    @Inject(method={"getBlockState"}, at={@At(value="RETURN")}, cancellable=true)
    private void r3dmenu$disguise(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        BlockState ghost;
        BlockState real = (BlockState)cir.getReturnValue();
        if (PlankCommandBlock.disguises(real)) {
            cir.setReturnValue((Object)PlankCommandBlock.stateAt(pos));
        } else if (SkeletonSpawner.disguises(real)) {
            cir.setReturnValue((Object)SkeletonSpawner.SPAWNER_STATE);
        } else if (real.canBeReplaced() && (ghost = GhostBlocks.at(pos)) != null) {
            cir.setReturnValue((Object)ghost);
        }
    }

    @Inject(method={"getBlockEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$fakeSpawner(BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        if (SkeletonSpawner.disguises(this.r3dmenu$realState(pos))) {
            cir.setReturnValue((Object)SkeletonSpawner.fakeFor(this.level, pos));
        }
    }

    private BlockState r3dmenu$realState(BlockPos pos) {
        return this.getSection(SectionPos.blockToSectionCoord((int)pos.getX()), SectionPos.blockToSectionCoord((int)pos.getY()), SectionPos.blockToSectionCoord((int)pos.getZ())).getBlockState(pos);
    }
}

