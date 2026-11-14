/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package name.modid.client.mixin;

import name.modid.client.GhostItems;
import name.modid.client.PhantomItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LocalPlayer.class})
public class LocalPlayerDropMixin {
    @Inject(method={"drop(Z)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void r3dmenu$dropPhantom(boolean all, CallbackInfoReturnable<Boolean> cir) {
        Level level;
        LocalPlayer self = (LocalPlayer)this;
        if (!PhantomItems.isPhantom(self.getInventory().getSelectedItem())) {
            return;
        }
        ItemStack dropped = self.getInventory().removeFromSelected(all);
        PhantomItems.forget(dropped);
        ItemStack remaining = self.getInventory().getSelectedItem();
        if (PhantomItems.isPhantom(remaining)) {
            PhantomItems.track(remaining);
        }
        if ((level = self.level()) instanceof ClientLevel) {
            ClientLevel level2 = (ClientLevel)level;
            Vec3 look = self.getLookAngle();
            GhostItems.spawn(level2, self.getX(), self.getEyeY() - 0.3, self.getZ(), dropped, look.x * 0.3, look.y * 0.3 + 0.1, look.z * 0.3);
        }
        cir.setReturnValue((Object)(!dropped.isEmpty() ? 1 : 0));
    }
}

