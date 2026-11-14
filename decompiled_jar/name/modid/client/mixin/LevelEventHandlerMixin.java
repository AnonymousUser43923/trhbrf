/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.LevelEventHandler
 *  net.minecraft.core.BlockPos
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package name.modid.client.mixin;

import name.modid.client.PhantomDropper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelEventHandler.class})
public class LevelEventHandlerMixin {
    @Shadow
    @Final
    private ClientLevel level;

    @Inject(method={"levelEvent"}, at={@At(value="HEAD")})
    private void r3dmenu$phantomDrop(int eventType, BlockPos pos, int data, CallbackInfo ci) {
        if (eventType == 1001 && PhantomDropper.enabled && PhantomDropper.hasContents(pos)) {
            PhantomDropper.fire(this.level, pos.immutable());
        }
    }
}

