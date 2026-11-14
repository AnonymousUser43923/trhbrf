/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.ConnectScreen
 *  net.minecraft.client.gui.screens.LevelLoadingScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
 *  net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.multiplayer.resolver.ServerAddress
 *  net.minecraft.network.Connection
 *  net.minecraft.network.PacketListener
 *  net.minecraft.network.chat.Component
 */
package name.modid.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;
import java.util.Properties;
import name.modid.client.mixin.ConnectScreenConnectionAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;

public final class JoinWorldFix {
    public static boolean armed = false;
    public static String command = "rtp eu central";
    public static double delaySeconds = 8.0;
    public static double retrySeconds = 6.0;
    public static double reconnectSeconds = 0.0;
    public static String serverFilter = "donutsmp";
    public static boolean showOverlay = true;
    private static final double MAX_SECONDS = 3600.0;
    private static long stuckSinceMs = -1L;
    private static long lastSentAtMs = -1L;
    private static int sentCount = 0;
    private static boolean reconnectRequested = false;
    private static ServerData pendingReconnect = null;
    private static ServerData lastServer = null;
    private static String status = "";

    private JoinWorldFix() {
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("r3dmenu-joinworldfix.properties");
    }

    public static void load() {
        Path path = JoinWorldFix.configPath();
        if (!Files.exists(path, new LinkOption[0])) {
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path, new OpenOption[0]);){
            props.load(in);
        }
        catch (IOException e) {
            return;
        }
        armed = Boolean.parseBoolean(props.getProperty("armed", "false"));
        command = props.getProperty("command", command);
        delaySeconds = JoinWorldFix.parseSeconds(props.getProperty("delaySeconds"), delaySeconds);
        retrySeconds = JoinWorldFix.parseSeconds(props.getProperty("retrySeconds"), retrySeconds);
        reconnectSeconds = JoinWorldFix.parseSeconds(props.getProperty("reconnectSeconds"), reconnectSeconds);
        serverFilter = props.getProperty("serverFilter", serverFilter);
        showOverlay = Boolean.parseBoolean(props.getProperty("showOverlay", "true"));
        JoinWorldFix.sanitize();
    }

    public static void save() {
        JoinWorldFix.sanitize();
        Properties props = new Properties();
        props.setProperty("armed", Boolean.toString(armed));
        props.setProperty("command", command);
        props.setProperty("delaySeconds", Double.toString(delaySeconds));
        props.setProperty("retrySeconds", Double.toString(retrySeconds));
        props.setProperty("reconnectSeconds", Double.toString(reconnectSeconds));
        props.setProperty("serverFilter", serverFilter);
        props.setProperty("showOverlay", Boolean.toString(showOverlay));
        Path path = JoinWorldFix.configPath();
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            try (OutputStream out = Files.newOutputStream(path, new OpenOption[0]);){
                props.store(out, "r3dmenu JoinWorldFix settings");
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static double parseSeconds(String raw, double fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return JoinWorldFix.clamp(Double.parseDouble(raw));
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static void sanitize() {
        if (command == null) {
            command = "";
        }
        if (serverFilter == null) {
            serverFilter = "";
        }
        delaySeconds = JoinWorldFix.clamp(delaySeconds);
        retrySeconds = JoinWorldFix.clamp(retrySeconds);
        reconnectSeconds = JoinWorldFix.clamp(reconnectSeconds);
    }

    private static double clamp(double seconds) {
        if (Double.isNaN(seconds) || seconds < 0.0) {
            return 0.0;
        }
        return Math.min(seconds, 3600.0);
    }

    public static String cleanCommand() {
        String c = command == null ? "" : command.trim();
        return c.startsWith("/") ? c.substring(1) : c;
    }

    public static String status() {
        return status;
    }

    public static boolean isStuck() {
        return stuckSinceMs != -1L;
    }

    private static void reset() {
        stuckSinceMs = -1L;
        lastSentAtMs = -1L;
        sentCount = 0;
        reconnectRequested = false;
        status = "";
    }

    public static void tick(Minecraft client) {
        if (client.getCurrentServer() != null) {
            lastServer = client.getCurrentServer();
        }
        if (!armed) {
            if (stuckSinceMs != -1L) {
                JoinWorldFix.reset();
            }
            pendingReconnect = null;
            return;
        }
        if (pendingReconnect != null && !(client.gui.screen() instanceof ConnectScreen) && client.level == null) {
            ServerData target = pendingReconnect;
            pendingReconnect = null;
            ServerAddress address = ServerAddress.parseString((String)target.ip);
            ConnectScreen.startConnecting((Screen)new JoinMultiplayerScreen((Screen)new TitleScreen()), (Minecraft)client, (ServerAddress)address, (ServerData)target, (boolean)false, null);
            return;
        }
        Screen screen = client.gui.screen();
        if (!JoinWorldFix.isWaitingScreen(screen)) {
            if (stuckSinceMs != -1L) {
                JoinWorldFix.reset();
            }
            return;
        }
        long now = System.currentTimeMillis();
        if (stuckSinceMs == -1L) {
            stuckSinceMs = now;
        }
        double elapsed = (double)(now - stuckSinceMs) / 1000.0;
        boolean canSend = JoinWorldFix.playListener(client) != null;
        boolean matches = JoinWorldFix.serverMatches(client);
        if (matches && reconnectSeconds > 0.0 && elapsed >= reconnectSeconds && !reconnectRequested) {
            JoinWorldFix.reconnect(client, String.format(Locale.ROOT, "auto after %.0fs", elapsed));
            return;
        }
        if (!canSend) {
            String note = reconnectSeconds > 0.0 ? String.format(Locale.ROOT, ", reconnecting in %.0fs", Math.max(0.0, reconnectSeconds - elapsed)) : "";
            status = "server hasn't handed you to a world yet" + note;
            return;
        }
        if (delaySeconds > 0.0 && matches) {
            if (sentCount == 0) {
                double remaining = delaySeconds - elapsed;
                if (remaining <= 0.0) {
                    JoinWorldFix.sendNow(client, "auto");
                } else {
                    status = String.format(Locale.ROOT, "sending /%s in %.1fs", JoinWorldFix.cleanCommand(), remaining);
                }
            } else if (retrySeconds > 0.0 && (double)(now - lastSentAtMs) >= retrySeconds * 1000.0) {
                JoinWorldFix.sendNow(client, "retry");
            }
        } else if (!matches) {
            status = "server doesn't match filter '" + serverFilter + "'";
        }
    }

    private static boolean isWaitingScreen(Screen screen) {
        if (screen instanceof LevelLoadingScreen) {
            return true;
        }
        if (screen instanceof ConnectScreen) {
            ConnectScreen connectScreen = (ConnectScreen)screen;
            Connection connection = ((ConnectScreenConnectionAccessor)connectScreen).r3dmenu$getConnection();
            if (connection == null) {
                return false;
            }
            return !(connection.getPacketListener() instanceof ClientHandshakePacketListenerImpl);
        }
        return false;
    }

    private static ClientPacketListener playListener(Minecraft client) {
        PacketListener packetListener;
        ConnectScreen connectScreen;
        Connection connection;
        ClientPacketListener listener = client.getConnection();
        if (listener != null) {
            return listener;
        }
        Screen screen = client.gui.screen();
        if (screen instanceof ConnectScreen && (connection = ((ConnectScreenConnectionAccessor)(connectScreen = (ConnectScreen)screen)).r3dmenu$getConnection()) != null && (packetListener = connection.getPacketListener()) instanceof ClientPacketListener) {
            ClientPacketListener play = (ClientPacketListener)packetListener;
            return play;
        }
        return null;
    }

    private static boolean serverMatches(Minecraft client) {
        if (serverFilter.isBlank()) {
            return true;
        }
        ServerData server = JoinWorldFix.currentServer(client);
        if (server == null) {
            return false;
        }
        String needle = serverFilter.toLowerCase(Locale.ROOT);
        return server.ip != null && server.ip.toLowerCase(Locale.ROOT).contains(needle) || server.name != null && server.name.toLowerCase(Locale.ROOT).contains(needle);
    }

    private static ServerData currentServer(Minecraft client) {
        ServerData server = client.getCurrentServer();
        return server != null ? server : lastServer;
    }

    public static void sendNow(Minecraft client, String reason) {
        ClientPacketListener listener = JoinWorldFix.playListener(client);
        if (listener == null) {
            status = "can't send yet - not in the play phase";
            return;
        }
        String cmd = JoinWorldFix.cleanCommand();
        if (cmd.isEmpty()) {
            status = "no command configured";
            return;
        }
        listener.sendCommand(cmd);
        lastSentAtMs = System.currentTimeMillis();
        status = "sent /" + cmd + " (" + reason + ", x" + ++sentCount + ")";
    }

    public static void reconnect(Minecraft client, String reason) {
        ConnectScreen connectScreen;
        Connection connection;
        ServerData target = JoinWorldFix.currentServer(client);
        if (target == null || target.ip == null || target.ip.isBlank()) {
            status = "can't reconnect: unknown server";
            return;
        }
        reconnectRequested = true;
        pendingReconnect = target;
        status = "reconnecting to " + target.ip + " (" + reason + ")";
        Screen screen = client.gui.screen();
        if (screen instanceof ConnectScreen && (connection = ((ConnectScreenConnectionAccessor)(connectScreen = (ConnectScreen)screen)).r3dmenu$getConnection()) != null && connection.isConnected()) {
            connection.disconnect((Component)Component.literal((String)"JoinWorldFix: reconnecting"));
            return;
        }
        client.disconnectFromWorld((Component)Component.literal((String)"JoinWorldFix: reconnecting"));
    }
}

