/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
 *  net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
 *  net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package name.modid.client.mixin;

import name.modid.client.PhantomItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class ClientInventorySyncMixin {
    @Inject(method={"handleContainerContent"}, at={@At(value="TAIL")})
    private void r3dmenu$afterContainerContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        ClientInventorySyncMixin.r3dmenu$restore();
    }

    @Inject(method={"handleContainerSetSlot"}, at={@At(value="TAIL")})
    private void r3dmenu$afterContainerSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        ClientInventorySyncMixin.r3dmenu$restore();
    }

    @Inject(method={"handleSetPlayerInventory"}, at={@At(value="TAIL")})
    private void r3dmenu$afterSetPlayerInventory(ClientboundSetPlayerInventoryPacket packet, CallbackInfo ci) {
        ClientInventorySyncMixin.r3dmenu$restore();
    }

    private static void r3dmenu$restore() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            PhantomItems.reapply(mc.player.getInventory());
        }
    }
}

