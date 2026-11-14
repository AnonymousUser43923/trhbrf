/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSystemChatPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package name.modid.client.mixin;

import name.modid.client.FakeElytraSell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class ClientSaleMessageMixin {
    @Inject(method={"handleSystemChat"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$systemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isSameThread()) {
            return;
        }
        Component fixed = FakeElytraSell.fakeSaleText(packet.content());
        if (fixed == null) {
            return;
        }
        if (packet.overlay()) {
            mc.gui.chatListener().handleOverlay(fixed);
        } else {
            mc.gui.chatListener().handleSystemMessage(fixed, true);
        }
        ci.cancel();
    }

    @Inject(method={"setActionBarText"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$actionBar(ClientboundSetActionBarTextPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isSameThread()) {
            return;
        }
        Component fixed = FakeElytraSell.fakeSaleText(packet.text());
        if (fixed != null) {
            mc.gui.hud.setOverlayMessage(fixed, false);
            ci.cancel();
        }
    }

    @Inject(method={"setTitleText"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$title(ClientboundSetTitleTextPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isSameThread()) {
            return;
        }
        Component fixed = FakeElytraSell.fakeSaleText(packet.text());
        if (fixed != null) {
            mc.gui.hud.setTitle(fixed);
            ci.cancel();
        }
    }

    @Inject(method={"setSubtitleText"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$subtitle(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isSameThread()) {
            return;
        }
        Component fixed = FakeElytraSell.fakeSaleText(packet.text());
        if (fixed != null) {
            mc.gui.hud.setSubtitle(fixed);
            ci.cancel();
        }
    }
}

