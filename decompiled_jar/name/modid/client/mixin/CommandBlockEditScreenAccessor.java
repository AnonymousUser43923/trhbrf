/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen
 *  net.minecraft.world.level.block.entity.CommandBlockEntity$Mode
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package name.modid.client.mixin;

import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={CommandBlockEditScreen.class})
public interface CommandBlockEditScreenAccessor {
    @Accessor(value="mode")
    public CommandBlockEntity.Mode r3dmenu$mode();

    @Accessor(value="conditional")
    public boolean r3dmenu$conditional();

    @Accessor(value="autoexec")
    public boolean r3dmenu$autoexec();
}

