/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.particle.TerrainParticle
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package name.modid.client.mixin;

import name.modid.client.PlankCommandBlock;
import name.modid.client.SkeletonSpawner;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={TerrainParticle.class})
public class TerrainParticleMixin {
    @ModifyVariable(method={"<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"}, at=@At(value="HEAD"), argsOnly=true)
    private static BlockState r3dmenu$disguisedDust(BlockState state) {
        if (PlankCommandBlock.disguises(state)) {
            return PlankCommandBlock.COMMAND_BLOCK_STATE;
        }
        if (SkeletonSpawner.disguises(state)) {
            return SkeletonSpawner.SPAWNER_STATE;
        }
        return state;
    }
}

