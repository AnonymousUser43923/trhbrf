/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.protocol.game.ClientboundSoundPacket
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.Items
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package name.modid.client.mixin;

import name.modid.client.ElytraSkin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public class ClientEquipSoundMixin {
    private static final double OWN_SOUND_RANGE_SQR = 4.0;

    @Inject(method={"handleSoundEvent"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$elytraEquipSound(ClientboundSoundPacket packet, CallbackInfo ci) {
        if (!ElytraSkin.enabled || !packet.getSound().is(((SoundEvent)SoundEvents.ARMOR_EQUIP_COPPER.value()).location())) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.distanceToSqr(packet.getX(), packet.getY(), packet.getZ()) > 4.0 || !ClientEquipSoundMixin.r3dmenu$hasCopperChestplate(player)) {
            return;
        }
        ((ClientPacketListener)this).handleSoundEvent(new ClientboundSoundPacket(SoundEvents.ARMOR_EQUIP_ELYTRA, packet.getSource(), packet.getX(), packet.getY(), packet.getZ(), packet.getVolume(), packet.getPitch(), packet.getSeed()));
        ci.cancel();
    }

    private static boolean r3dmenu$hasCopperChestplate(LocalPlayer player) {
        if (player.getItemBySlot(EquipmentSlot.CHEST).is((Object)Items.COPPER_CHESTPLATE) || player.getMainHandItem().is((Object)Items.COPPER_CHESTPLATE) || player.getOffhandItem().is((Object)Items.COPPER_CHESTPLATE) || player.containerMenu.getCarried().is((Object)Items.COPPER_CHESTPLATE)) {
            return true;
        }
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            if (!inventory.getItem(i).is((Object)Items.COPPER_CHESTPLATE)) continue;
            return true;
        }
        return false;
    }
}

