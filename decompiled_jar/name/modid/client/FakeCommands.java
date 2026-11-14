/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommands
 *  net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.commands.SharedSuggestionProvider
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.item.ItemStack
 */
package name.modid.client;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import name.modid.client.FakeRoster;
import name.modid.client.HiddenSlot;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public final class FakeCommands {
    private static final int SUCCESS = 1;
    private static final int REJOIN_DELAY_TICKS = 200;
    private static final Set<String> banned = new HashSet<String>();
    private static final List<Pending> pending = new ArrayList<Pending>();
    private static final SuggestionProvider<FabricClientCommandSource> PLAYERS = (ctx, builder) -> {
        ArrayList<String> names = new ArrayList<String>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                names.add(info.getProfile().name());
            }
        }
        for (String n : FakeRoster.NAMES) {
            if (names.contains(n)) continue;
            names.add(n);
        }
        return SharedSuggestionProvider.suggest(names, (SuggestionsBuilder)builder);
    };

    private FakeCommands() {
    }

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            banned.clear();
            pending.clear();
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> FakeCommands.tick());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, ctx) -> {
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"ban").then(((RequiredArgumentBuilder)ClientCommands.argument((String)"player", (ArgumentType)StringArgumentType.word()).suggests(PLAYERS).executes(c -> FakeCommands.ban(FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "player"), ""))).then(ClientCommands.argument((String)"reason", (ArgumentType)StringArgumentType.greedyString()).executes(c -> FakeCommands.ban(FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "player"), FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "reason"))))));
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"unban").then(ClientCommands.argument((String)"player", (ArgumentType)StringArgumentType.word()).suggests(PLAYERS).executes(c -> FakeCommands.unban(FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "player")))));
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"pardon").then(ClientCommands.argument((String)"player", (ArgumentType)StringArgumentType.word()).suggests(PLAYERS).executes(c -> FakeCommands.unban(FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "player")))));
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"kick").then(((RequiredArgumentBuilder)ClientCommands.argument((String)"player", (ArgumentType)StringArgumentType.word()).suggests(PLAYERS).executes(c -> FakeCommands.kick(FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "player"), ""))).then(ClientCommands.argument((String)"reason", (ArgumentType)StringArgumentType.greedyString()).executes(c -> FakeCommands.kick(FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "player"), FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "reason"))))));
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"mute").then(ClientCommands.argument((String)"player", (ArgumentType)StringArgumentType.word()).suggests(PLAYERS).executes(c -> FakeCommands.simpleTarget(FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "player"), "muted"))));
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"unmute").then(ClientCommands.argument((String)"player", (ArgumentType)StringArgumentType.word()).suggests(PLAYERS).executes(c -> FakeCommands.simpleTarget(FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "player"), "unmuted"))));
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"check").then(ClientCommands.literal((String)"ban").then(ClientCommands.argument((String)"player", (ArgumentType)StringArgumentType.word()).suggests(PLAYERS).executes(c -> FakeCommands.checkBan(FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "player"))))));
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"checkban").then(ClientCommands.argument((String)"player", (ArgumentType)StringArgumentType.word()).suggests(PLAYERS).executes(c -> FakeCommands.checkBan(FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "player")))));
            dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommands.literal((String)"give").executes(c -> FakeCommands.give())).then(ClientCommands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(c -> FakeCommands.give())));
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"heal").executes(c -> FakeCommands.self("Healed.")));
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"feed").executes(c -> FakeCommands.self("Fed.")));
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"fly").executes(c -> FakeCommands.self("Flight enabled.")));
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"god").executes(c -> FakeCommands.self("God mode enabled.")));
            dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"vanish").executes(c -> FakeCommands.self("You are now vanished.")));
            for (String alias : new String[]{"broadcast", "bc", "say"}) {
                dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)alias).then(ClientCommands.argument((String)"message", (ArgumentType)StringArgumentType.greedyString()).executes(c -> FakeCommands.broadcast(FakeCommands.str((CommandContext<FabricClientCommandSource>)c, "message")))));
            }
        });
    }

    private static void tick() {
        if (pending.isEmpty()) {
            return;
        }
        pending.removeIf(p -> {
            if (--p.ticksLeft <= 0) {
                p.action.run();
                return true;
            }
            return false;
        });
    }

    private static int ban(String target, String reason) {
        banned.add(target.toLowerCase(Locale.ROOT));
        FakeRoster.remove(Minecraft.getInstance(), target);
        String name = FakeRoster.canonical(target);
        FakeCommands.chat((Component)Component.literal((String)"Banned ").withStyle(ChatFormatting.RED).append((Component)FakeCommands.white(name)).append((Component)Component.literal((String)(reason.isEmpty() ? "." : " for " + reason + ".")).withStyle(ChatFormatting.RED)));
        FakeCommands.leftGame(name);
        return 1;
    }

    private static int unban(String target) {
        banned.remove(target.toLowerCase(Locale.ROOT));
        FakeCommands.chat((Component)Component.literal((String)"Unbanned ").withStyle(ChatFormatting.GREEN).append((Component)FakeCommands.white(FakeRoster.canonical(target))).append((Component)Component.literal((String)".").withStyle(ChatFormatting.GREEN)));
        if (FakeRoster.isFake(target)) {
            String name = FakeRoster.canonical(target);
            pending.add(new Pending(200, () -> {
                FakeRoster.add(Minecraft.getInstance(), name);
                FakeCommands.joinedGame(name);
            }));
        }
        return 1;
    }

    private static int kick(String target, String reason) {
        FakeRoster.remove(Minecraft.getInstance(), target);
        String name = FakeRoster.canonical(target);
        FakeCommands.chat((Component)Component.literal((String)"Kicked ").withStyle(ChatFormatting.YELLOW).append((Component)FakeCommands.white(name)).append((Component)Component.literal((String)(reason.isEmpty() ? "." : " for " + reason + ".")).withStyle(ChatFormatting.YELLOW)));
        FakeCommands.leftGame(name);
        return 1;
    }

    private static int simpleTarget(String target, String pastVerb) {
        FakeCommands.chat((Component)Component.literal((String)(FakeCommands.capitalize(pastVerb) + " ")).withStyle(ChatFormatting.YELLOW).append((Component)FakeCommands.white(FakeRoster.canonical(target))).append((Component)Component.literal((String)".").withStyle(ChatFormatting.YELLOW)));
        return 1;
    }

    private static int checkBan(String target) {
        String name = FakeRoster.canonical(target);
        if (banned.contains(target.toLowerCase(Locale.ROOT))) {
            FakeCommands.chat((Component)FakeCommands.white(name).append((Component)Component.literal((String)" is currently banned.").withStyle(ChatFormatting.RED)));
        } else {
            FakeCommands.chat((Component)FakeCommands.white(name).append((Component)Component.literal((String)" is not banned.").withStyle(ChatFormatting.GREEN)));
        }
        return 1;
    }

    private static int give() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return 1;
        }
        ItemStack stash = HiddenSlot.contents(player);
        if (stash.isEmpty()) {
            FakeCommands.chat((Component)Component.literal((String)"Nothing stashed to give.").withStyle(ChatFormatting.RED));
            return 1;
        }
        int count = stash.getCount();
        String itemName = stash.getHoverName().getString();
        String self = player.getName().getString();
        HiddenSlot.swapToMainHand(mc);
        FakeCommands.chat((Component)Component.literal((String)"Gave ").withStyle(ChatFormatting.GREEN).append((Component)FakeCommands.white(self)).append((Component)Component.literal((String)(" " + count + " x ")).withStyle(ChatFormatting.GREEN)).append((Component)FakeCommands.white(itemName)));
        return 1;
    }

    private static int broadcast(String msg) {
        FakeCommands.chat((Component)Component.literal((String)"[Broadcast] ").withStyle(ChatFormatting.AQUA).append((Component)Component.literal((String)msg).withStyle(ChatFormatting.WHITE)));
        return 1;
    }

    private static int self(String message) {
        FakeCommands.chat((Component)Component.literal((String)message).withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static void leftGame(String name) {
        FakeCommands.chat((Component)Component.translatable((String)"multiplayer.player.left", (Object[])new Object[]{Component.literal((String)name)}).withStyle(ChatFormatting.YELLOW));
    }

    private static void joinedGame(String name) {
        FakeCommands.chat((Component)Component.translatable((String)"multiplayer.player.joined", (Object[])new Object[]{Component.literal((String)name)}).withStyle(ChatFormatting.YELLOW));
    }

    private static void chat(Component line) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null && mc.gui.hud != null) {
            mc.gui.hud.getChat().addClientSystemMessage(line);
        }
    }

    private static MutableComponent white(String s) {
        return Component.literal((String)s).withStyle(ChatFormatting.WHITE);
    }

    private static String str(CommandContext<FabricClientCommandSource> c, String name) {
        return StringArgumentType.getString(c, (String)name);
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static final class Pending {
        int ticksLeft;
        final Runnable action;

        Pending(int ticksLeft, Runnable action) {
            this.ticksLeft = ticksLeft;
            this.action = action;
        }
    }
}

