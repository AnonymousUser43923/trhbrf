/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
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
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class ClientTeleportFakeFlightMixin {
    @Inject(method={"handleMovePlayer"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$teleportToGhost(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (!FakeElytraFlight.frozen()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !mc.isSameThread()) {
            return;
        }
        if (FakeElytraFlight.onServerTeleport(packet, player, (ClientPacketListener)this)) {
            ci.cancel();
        }
    }

    @Inject(method={"handleSetEntityMotion"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$velocityToGhost(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        if (!FakeElytraFlight.frozen()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !mc.isSameThread()) {
            return;
        }
        if (FakeElytraFlight.onServerVelocity(packet, player)) {
            ci.cancel();
        }
    }
}

