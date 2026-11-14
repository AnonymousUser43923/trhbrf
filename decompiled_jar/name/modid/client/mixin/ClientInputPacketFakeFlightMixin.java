/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.world.entity.player.Input
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package name.modid.client.mixin;

import name.modid.client.FakeElytraFlight;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientCommonPacketListenerImpl.class})
public class ClientInputPacketFakeFlightMixin {
    @Inject(method={"send(Lnet/minecraft/network/protocol/Packet;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$dropInputsWhileGhosting(Packet<?> packet, CallbackInfo ci) {
        ServerboundPlayerInputPacket input;
        if (FakeElytraFlight.frozen() && packet instanceof ServerboundPlayerInputPacket && !(input = (ServerboundPlayerInputPacket)packet).input().equals((Object)Input.EMPTY)) {
            ci.cancel();
        }
    }
}

