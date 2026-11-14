/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.HumanoidMobRenderer
 *  net.minecraft.client.renderer.entity.state.HumanoidRenderState
 *  net.minecraft.client.renderer.item.ItemModelResolver
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package name.modid.client.mixin;

import name.modid.client.ElytraSkin;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={HumanoidMobRenderer.class})
public class HumanoidMobRendererMixin {
    @Inject(method={"extractHumanoidRenderState"}, at={@At(value="TAIL")})
    private static void r3dmenu$elytraChest(LivingEntity entity, HumanoidRenderState state, float partialTick, ItemModelResolver resolver, CallbackInfo ci) {
        if (ElytraSkin.disguises(state.chestEquipment)) {
            state.chestEquipment = ElytraSkin.elytraLike(state.chestEquipment);
        }
    }
}

