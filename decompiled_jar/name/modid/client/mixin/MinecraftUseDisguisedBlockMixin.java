/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package name.modid.client.mixin;

import name.modid.client.PlankCommandBlock;
import name.modid.client.SkeletonSpawner;
import name.modid.client.gui.SkeletonSpawnerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Minecraft.class})
public class MinecraftUseDisguisedBlockMixin {
    @Inject(method={"startUseItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$openDisguise(CallbackInfo ci) {
        Minecraft mc = (Minecraft)this;
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) {
            return;
        }
        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult hit = (BlockHitResult)hitResult;
        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (PlankCommandBlock.disguises(state)) {
            PlankCommandBlock.openEditor(mc, pos);
            ci.cancel();
        } else if (SkeletonSpawner.disguises(state)) {
            mc.gui.setScreen((Screen)new SkeletonSpawnerScreen(SkeletonSpawner.storeAt(pos), mc.player.getInventory()));
            ci.cancel();
        }
    }
}

