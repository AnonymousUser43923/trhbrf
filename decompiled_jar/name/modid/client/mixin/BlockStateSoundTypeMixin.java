/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package name.modid.client.mixin;

import name.modid.client.PlankCommandBlock;
import name.modid.client.SkeletonSpawner;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockBehaviour.BlockStateBase.class})
public class BlockStateSoundTypeMixin {
    @Inject(method={"getSoundType"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$commandBlockSounds(CallbackInfoReturnable<SoundType> cir) {
        BlockState self = (BlockState)this;
        if (PlankCommandBlock.disguises(self)) {
            cir.setReturnValue((Object)PlankCommandBlock.COMMAND_BLOCK_STATE.getSoundType());
        } else if (SkeletonSpawner.disguises(self)) {
            cir.setReturnValue((Object)SkeletonSpawner.SPAWNER_STATE.getSoundType());
        }
    }
}

