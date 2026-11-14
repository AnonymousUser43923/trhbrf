/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.LocalPlayer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package name.modid.client.mixin;

import name.modid.client.FakeElytraFlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LocalPlayer.class})
public class LocalPlayerFakeFlightMixin {
    @Inject(method={"aiStep"}, at={@At(value="HEAD")})
    private void r3dmenu$fakeFlightTick(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer)this;
        if (self == Minecraft.getInstance().player) {
            FakeElytraFlight.clientTick(self);
        }
    }

    @Inject(method={"sendPosition"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$freezeWhileFakeFlying(CallbackInfo ci) {
        if (!FakeElytraFlight.frozen()) {
            return;
        }
        LocalPlayer self = (LocalPlayer)this;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            FakeElytraFlight.sendGhostPosition(connection, self);
        }
        ci.cancel();
    }
}

