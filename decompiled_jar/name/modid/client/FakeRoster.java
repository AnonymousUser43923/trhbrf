/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.multiplayer.PlayerInfo
 */
package name.modid.client;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import name.modid.client.mixin.ClientPlayerListAccessor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

public final class FakeRoster {
    public static final List<String> NAMES = new ArrayList<String>(List.of("GreenBeanBoy", "xXDirtLordXx", "SethGottaSnipe", "Yqrn"));
    private static final Map<String, UUID> IDS = new HashMap<String, UUID>();

    private FakeRoster() {
    }

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FakeRoster.addAll(client));
    }

    private static UUID idFor(String name) {
        return IDS.computeIfAbsent(name.toLowerCase(Locale.ROOT), k -> UUID.randomUUID());
    }

    public static void addAll(Minecraft client) {
        for (String name : NAMES) {
            FakeRoster.add(client, name);
        }
    }

    public static void add(Minecraft client, String name) {
        ClientPacketListener conn = client.getConnection();
        if (conn == null) {
            return;
        }
        ClientPlayerListAccessor list = (ClientPlayerListAccessor)conn;
        UUID id = FakeRoster.idFor(name);
        if (list.r3dmenu$playerInfoMap().containsKey(id)) {
            return;
        }
        PlayerInfo info = new PlayerInfo(new GameProfile(id, name), true);
        list.r3dmenu$playerInfoMap().put(id, info);
        list.r3dmenu$listedPlayers().add(info);
    }

    public static void remove(Minecraft client, String name) {
        ClientPacketListener conn = client.getConnection();
        if (conn == null) {
            return;
        }
        ClientPlayerListAccessor list = (ClientPlayerListAccessor)conn;
        UUID id = FakeRoster.idFor(name);
        PlayerInfo info = list.r3dmenu$playerInfoMap().remove(id);
        if (info != null) {
            Set<PlayerInfo> listed = list.r3dmenu$listedPlayers();
            listed.remove(info);
        }
    }

    public static boolean isFake(String name) {
        for (String n : NAMES) {
            if (!n.equalsIgnoreCase(name)) continue;
            return true;
        }
        return false;
    }

    public static String canonical(String name) {
        for (String n : NAMES) {
            if (!n.equalsIgnoreCase(name)) continue;
            return n;
        }
        return name;
    }
}

