/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package name.modid.client.mixin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientPacketListener.class})
public interface ClientPlayerListAccessor {
    @Accessor(value="playerInfoMap")
    public Map<UUID, PlayerInfo> r3dmenu$playerInfoMap();

    @Accessor(value="listedPlayers")
    public Set<PlayerInfo> r3dmenu$listedPlayers();
}

