/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.item.ItemModelResolver
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.CustomModelData
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package name.modid.client.mixin;

import java.util.List;
import name.modid.client.ItemDisguises;
import name.modid.client.MacroProfile;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={ItemModelResolver.class})
public class ItemModelResolverMixin {
    @ModifyVariable(method={"updateForTopItem(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V"}, at=@At(value="HEAD"), argsOnly=true)
    private ItemStack r3dmenu$disguise(ItemStack stack) {
        if (stack.is((Object)Items.LAPIS_LAZULI) && MacroProfile.isUnlocked()) {
            ItemStack copy = stack.copy();
            copy.set(DataComponents.CUSTOM_MODEL_DATA, (Object)new CustomModelData(List.of(), List.of(), List.of(MacroProfile.isOrange() ? "orange" : "red"), List.of()));
            return copy;
        }
        ItemDisguises.Disguise disguise = ItemDisguises.of(stack);
        if (disguise != null && !disguise.as().isEmpty()) {
            return disguise.as().copyWithCount(stack.getCount());
        }
        return stack;
    }
}

