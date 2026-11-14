/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.ConnectScreen
 *  net.minecraft.network.Connection
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package name.modid.client.mixin;

import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ConnectScreen.class})
public interface ConnectScreenConnectionAccessor {
    @Accessor(value="connection")
    public Connection r3dmenu$getConnection();
}

