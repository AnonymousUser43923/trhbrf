/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.item.Items
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package name.modid.client.mixin;

import name.modid.client.FakeElytraFlight;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Minecraft.class})
public class MinecraftUseFireworkMixin {
    @Shadow
    private int rightClickDelay;

    @Inject(method={"startUseItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$fakeElytraFirework(CallbackInfo ci) {
        if (!FakeElytraFlight.active) {
            return;
        }
        Minecraft mc = (Minecraft)this;
        if (mc.player == null) {
            return;
        }
        if (mc.player.getMainHandItem().is((Object)Items.FIREWORK_ROCKET) || mc.player.getOffhandItem().is((Object)Items.FIREWORK_ROCKET)) {
            this.rightClickDelay = 4;
            FakeElytraFlight.fireworkBoost(mc.player);
            ci.cancel();
        }
    }
}

