/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.resources.sounds.ElytraOnPlayerSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
 *  net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
 *  net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Rot
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$StatusOnly
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.PositionMoveRotation
 *  net.minecraft.world.entity.player.Input
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.Fireworks
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package name.modid.client;

import java.util.List;
import java.util.Set;
import name.modid.client.ElytraSkin;
import name.modid.client.mixin.LocalPlayerSendStateAccessor;
import name.modid.client.mixin.PlayerJumpTriggerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.ElytraOnPlayerSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FakeElytraFlight {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"r3dmenu/FakeElytraFlight");
    private static final boolean TRACE = true;
    public static volatile boolean active = false;
    private static LocalPlayer owner = null;
    private static int ticksActive = 0;
    private static int boostTicks = 0;
    private static int groundTicks = 0;
    private static boolean jumpWasDown = false;
    private static int teleportsAbsorbed = 0;
    private static final int TAKEOFF_TICKS = 20;
    private static final int LAND_TICKS = 6;
    private static final int MAX_TICKS = 6000;
    private static final double GROUND_POP = 0.45;
    private static final double REAL_TELEPORT_DIST = 24.0;
    private static Vec3 gPos = null;
    private static Vec3 gVel = Vec3.ZERO;
    private static boolean gOnGround = false;
    private static boolean gHorizontalCollision = false;
    private static Vec3 sentPos = null;
    private static float sentYRot = 0.0f;
    private static float sentXRot = 0.0f;
    private static boolean sentOnGround = false;
    private static boolean sentHorizontalCollision = false;
    private static int positionReminder = 0;
    private static int snapTicks = 0;

    private FakeElytraFlight() {
    }

    public static boolean frozen() {
        return active || snapTicks > 0;
    }

    public static Vec3 frozenAnchor() {
        return gPos;
    }

    public static boolean canStart(Player p) {
        return ElytraSkin.enabled && p.getItemBySlot(EquipmentSlot.CHEST).is((Object)Items.COPPER_CHESTPLATE) && !p.isInWater() && !p.isPassenger();
    }

    public static void toggle() {
        if (active) {
            FakeElytraFlight.stop();
            return;
        }
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null) {
            LOGGER.info("[r3dmenu] toggle: canStart={} snapTicks={} chest={} inWater={} enabled={}", new Object[]{FakeElytraFlight.canStart((Player)p), snapTicks, p.getItemBySlot(EquipmentSlot.CHEST), p.isInWater(), ElytraSkin.enabled});
        }
        if (p != null && FakeElytraFlight.canStart((Player)p) && snapTicks == 0) {
            FakeElytraFlight.startFrom(p);
        }
    }

    private static void startFrom(LocalPlayer p) {
        ticksActive = 0;
        boostTicks = 0;
        groundTicks = 0;
        snapTicks = 0;
        active = true;
        FakeElytraFlight.keepCreativeFlightOff(p);
        FakeElytraFlight.beginGhost(p);
        Minecraft.getInstance().getSoundManager().play((SoundInstance)new ElytraOnPlayerSoundInstance(p));
        Vec3 look = p.getLookAngle();
        double horiz = Math.sqrt(look.x * look.x + look.z * look.z);
        double fx = horiz > 1.0E-4 ? look.x / horiz : 0.0;
        double fz = horiz > 1.0E-4 ? look.z / horiz : 0.0;
        Vec3 m = p.getDeltaMovement();
        p.setDeltaMovement(m.x * 0.2 + fx * 0.7, Math.max(m.y, 0.0) * 0.3 + 0.52, m.z * 0.2 + fz * 0.7);
    }

    public static void stop() {
        if (!active) {
            return;
        }
        active = false;
        boostTicks = 0;
        groundTicks = 0;
        ticksActive = 0;
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null && gPos != null) {
            snapTicks = 4;
            p.setDeltaMovement(Vec3.ZERO);
            p.resetFallDistance();
        } else {
            snapTicks = 0;
            gPos = null;
        }
    }

    public static void forceReset() {
        active = false;
        boostTicks = 0;
        groundTicks = 0;
        ticksActive = 0;
        snapTicks = 0;
        gPos = null;
        gVel = Vec3.ZERO;
        sentPos = null;
    }

    public static void fireworkBoost(LocalPlayer p) {
        ItemStack rocket;
        ItemStack itemStack = p.getMainHandItem().is((Object)Items.FIREWORK_ROCKET) ? p.getMainHandItem() : (rocket = p.getOffhandItem().is((Object)Items.FIREWORK_ROCKET) ? p.getOffhandItem() : ItemStack.EMPTY);
        if (rocket.isEmpty()) {
            return;
        }
        int flight = 1;
        Fireworks fw = (Fireworks)rocket.get(DataComponents.FIREWORKS);
        if (fw != null) {
            flight = Math.max(1, fw.flightDuration());
        }
        boostTicks = Math.max(boostTicks, 10 * flight + 5);
        rocket.shrink(1);
        p.level().playLocalSound(p.getX(), p.getY(), p.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0f, 1.0f, false);
    }

    public static void clientTick(LocalPlayer p) {
        boolean jumpDown;
        if (owner != p) {
            FakeElytraFlight.forceReset();
            owner = p;
        }
        if (snapTicks > 0 && gPos != null) {
            Vec3 cur = p.position();
            Vec3 next = snapTicks == 1 ? gPos : cur.add(gPos.subtract(cur).scale(1.0 / (double)snapTicks));
            p.setPos(next.x, next.y, next.z);
            p.setDeltaMovement(Vec3.ZERO);
            if (--snapTicks == 0) {
                FakeElytraFlight.endGhost(p);
            }
        }
        boolean jumpPressed = (jumpDown = Minecraft.getInstance().options.keyJump.isDown()) && !jumpWasDown;
        jumpWasDown = jumpDown;
        if (!active && jumpPressed && !p.onGround() && snapTicks == 0 && FakeElytraFlight.canStart((Player)p)) {
            FakeElytraFlight.startFrom(p);
        }
        if (active) {
            FakeElytraFlight.flightTick(p);
        }
        if (FakeElytraFlight.frozen() && gPos != null) {
            FakeElytraFlight.ghostTick(p);
        }
    }

    private static void flightTick(LocalPlayer p) {
        boolean boosting;
        if (!ElytraSkin.enabled || p.isInWater()) {
            LOGGER.info("[r3dmenu] flight stopped: enabled={} inWater={}", (Object)ElytraSkin.enabled, (Object)p.isInWater());
            FakeElytraFlight.stop();
            return;
        }
        if (++ticksActive > 6000) {
            FakeElytraFlight.stop();
            return;
        }
        FakeElytraFlight.keepCreativeFlightOff(p);
        boolean takingOff = ticksActive <= 20;
        boolean bl = boosting = boostTicks > 0;
        if (boosting) {
            --boostTicks;
            Vec3 look = p.getLookAngle();
            Vec3 m = p.getDeltaMovement();
            double nx = m.x + look.x * 0.1 + (look.x * 1.5 - m.x) * 0.5;
            double ny = m.y + look.y * 0.1 + (look.y * 1.5 - m.y) * 0.5;
            double nz = m.z + look.z * 0.1 + (look.z * 1.5 - m.z) * 0.5;
            if (takingOff) {
                ny = Math.max(ny, 0.1);
            }
            if (p.onGround()) {
                ny = Math.max(ny, 0.45);
            }
            p.setDeltaMovement(nx, ny, nz);
        }
        if (!takingOff && !boosting && p.onGround()) {
            if (++groundTicks >= 6) {
                FakeElytraFlight.stop();
            }
        } else {
            groundTicks = 0;
        }
        Minecraft mc = Minecraft.getInstance();
        Vec3 v = p.getDeltaMovement();
        LOGGER.info("[r3dmenu] flight t={} pos=({}, {}, {}) vel=({}, {}, {}) speed={} xRot={} yRot={} ground={} boost={} creativeFly={} pose={} screen={} jump={} use={} fwd={} ghost=({}, {}, {}) ghostVel=({}, {}, {}) ghostGround={}", new Object[]{ticksActive, FakeElytraFlight.f(p.getX()), FakeElytraFlight.f(p.getY()), FakeElytraFlight.f(p.getZ()), FakeElytraFlight.f(v.x), FakeElytraFlight.f(v.y), FakeElytraFlight.f(v.z), FakeElytraFlight.f(v.length()), FakeElytraFlight.f(p.getXRot()), FakeElytraFlight.f(p.getYRot()), p.onGround(), boostTicks, p.getAbilities().flying, p.getPose(), mc.gui.screen() == null ? "none" : mc.gui.screen().getClass().getSimpleName(), mc.options.keyJump.isDown(), mc.options.keyUse.isDown(), mc.options.keyUp.isDown(), FakeElytraFlight.f(FakeElytraFlight.gPos.x), FakeElytraFlight.f(FakeElytraFlight.gPos.y), FakeElytraFlight.f(FakeElytraFlight.gPos.z), FakeElytraFlight.f(FakeElytraFlight.gVel.x), FakeElytraFlight.f(FakeElytraFlight.gVel.y), FakeElytraFlight.f(FakeElytraFlight.gVel.z), gOnGround});
    }

    private static void beginGhost(LocalPlayer p) {
        LocalPlayerSendStateAccessor s = (LocalPlayerSendStateAccessor)p;
        gPos = p.position();
        gVel = p.getDeltaMovement();
        gOnGround = p.onGround();
        gHorizontalCollision = p.horizontalCollision;
        sentPos = new Vec3(s.r3dmenu$xLast(), s.r3dmenu$yLast(), s.r3dmenu$zLast());
        sentYRot = s.r3dmenu$yRotLast();
        sentXRot = s.r3dmenu$xRotLast();
        sentOnGround = s.r3dmenu$lastOnGround();
        sentHorizontalCollision = s.r3dmenu$lastHorizontalCollision();
        positionReminder = 0;
        ClientPacketListener conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            conn.send((Packet)new ServerboundPlayerInputPacket(Input.EMPTY));
            if (s.r3dmenu$wasSprinting()) {
                conn.send((Packet)new ServerboundPlayerCommandPacket((Entity)p, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                s.r3dmenu$setWasSprinting(false);
            }
        }
    }

    private static void endGhost(LocalPlayer p) {
        LocalPlayerSendStateAccessor s = (LocalPlayerSendStateAccessor)p;
        p.setPos(FakeElytraFlight.gPos.x, FakeElytraFlight.gPos.y, FakeElytraFlight.gPos.z);
        p.setDeltaMovement(gVel);
        p.setOnGround(gOnGround);
        p.resetFallDistance();
        if (sentPos != null) {
            s.r3dmenu$setXLast(FakeElytraFlight.sentPos.x);
            s.r3dmenu$setYLast(FakeElytraFlight.sentPos.y);
            s.r3dmenu$setZLast(FakeElytraFlight.sentPos.z);
        }
        s.r3dmenu$setYRotLast(sentYRot);
        s.r3dmenu$setXRotLast(sentXRot);
        s.r3dmenu$setLastOnGround(sentOnGround);
        s.r3dmenu$setLastHorizontalCollision(sentHorizontalCollision);
        s.r3dmenu$setPositionReminder(positionReminder);
        s.r3dmenu$setLastSentInput(Input.EMPTY);
        gPos = null;
        gVel = Vec3.ZERO;
        sentPos = null;
    }

    private static void ghostTick(LocalPlayer p) {
        double vz;
        Level level = p.level();
        AABB box = new AABB(FakeElytraFlight.gPos.x - 0.3, FakeElytraFlight.gPos.y, FakeElytraFlight.gPos.z - 0.3, FakeElytraFlight.gPos.x + 0.3, FakeElytraFlight.gPos.y + 1.8, FakeElytraFlight.gPos.z + 0.3);
        boolean inWater = level.getFluidState(BlockPos.containing((double)FakeElytraFlight.gPos.x, (double)(FakeElytraFlight.gPos.y + 0.4), (double)FakeElytraFlight.gPos.z)).is(FluidTags.WATER);
        Vec3 moved = Entity.collideBoundingBox((Entity)p, (Vec3)gVel, (AABB)box, (Level)level, List.of());
        boolean hitBelow = FakeElytraFlight.gVel.y < 0.0 && moved.y != FakeElytraFlight.gVel.y;
        gHorizontalCollision = moved.x != FakeElytraFlight.gVel.x || moved.z != FakeElytraFlight.gVel.z;
        gOnGround = hitBelow;
        gPos = gPos.add(moved);
        double vx = moved.x != FakeElytraFlight.gVel.x ? 0.0 : FakeElytraFlight.gVel.x;
        double vy = moved.y != FakeElytraFlight.gVel.y ? 0.0 : FakeElytraFlight.gVel.y;
        double d = vz = moved.z != FakeElytraFlight.gVel.z ? 0.0 : FakeElytraFlight.gVel.z;
        if (inWater) {
            vx *= 0.8;
            vy = vy * 0.8 - 0.005;
            vz *= 0.8;
        } else {
            float blockFriction = gOnGround ? level.getBlockState(BlockPos.containing((double)FakeElytraFlight.gPos.x, (double)(FakeElytraFlight.gPos.y - 0.5000001), (double)FakeElytraFlight.gPos.z)).getBlock().getFriction() : 1.0f;
            double friction = (double)blockFriction * 0.91;
            vx *= friction;
            vy = (vy - 0.08) * 0.98;
            vz *= friction;
        }
        if (vx * vx + vz * vz < 9.0E-6) {
            vx = 0.0;
            vz = 0.0;
        }
        if (Math.abs(vy) < 0.003) {
            vy = 0.0;
        }
        gVel = new Vec3(vx, vy, vz);
    }

    public static void sendGhostPosition(ClientPacketListener connection, LocalPlayer p) {
        boolean rot;
        if (gPos == null) {
            return;
        }
        if (sentPos == null) {
            sentPos = gPos;
        }
        float yRot = p.getYRot();
        float xRot = p.getXRot();
        Vec3 d = gPos.subtract(sentPos);
        boolean move = d.lengthSqr() > Mth.square((double)2.0E-4) || ++positionReminder >= 20;
        boolean bl = rot = yRot != sentYRot || xRot != sentXRot;
        if (move && rot) {
            connection.send((Packet)new ServerboundMovePlayerPacket.PosRot(gPos, yRot, xRot, gOnGround, gHorizontalCollision));
        } else if (move) {
            connection.send((Packet)new ServerboundMovePlayerPacket.Pos(gPos, gOnGround, gHorizontalCollision));
        } else if (rot) {
            connection.send((Packet)new ServerboundMovePlayerPacket.Rot(yRot, xRot, gOnGround, gHorizontalCollision));
        } else if (sentOnGround != gOnGround || sentHorizontalCollision != gHorizontalCollision) {
            connection.send((Packet)new ServerboundMovePlayerPacket.StatusOnly(gOnGround, gHorizontalCollision));
        }
        if (move) {
            sentPos = gPos;
            positionReminder = 0;
        }
        if (rot) {
            sentYRot = yRot;
            sentXRot = xRot;
        }
        sentOnGround = gOnGround;
        sentHorizontalCollision = gHorizontalCollision;
    }

    public static boolean onServerVelocity(ClientboundSetEntityMotionPacket packet, LocalPlayer p) {
        if (!FakeElytraFlight.frozen() || gPos == null || packet.id() != p.getId()) {
            return false;
        }
        gVel = packet.movement();
        return true;
    }

    public static boolean onServerTeleport(ClientboundPlayerPositionPacket packet, LocalPlayer p, ClientPacketListener connection) {
        if (!FakeElytraFlight.frozen() || gPos == null || p.isPassenger()) {
            return false;
        }
        PositionMoveRotation ghostView = new PositionMoveRotation(gPos, gVel, p.getYRot(), p.getXRot());
        PositionMoveRotation abs = PositionMoveRotation.calculateAbsolute((PositionMoveRotation)ghostView, (PositionMoveRotation)packet.change(), (Set)packet.relatives());
        Vec3 to = abs.position();
        double dist = Math.sqrt(to.distanceToSqr(gPos));
        if (dist > 24.0) {
            FakeElytraFlight.forceReset();
            return false;
        }
        gPos = to;
        gVel = abs.deltaMovement();
        gOnGround = false;
        connection.send((Packet)new ServerboundAcceptTeleportationPacket(packet.id()));
        connection.send((Packet)new ServerboundMovePlayerPacket.PosRot(to, p.getYRot(), p.getXRot(), false, false));
        sentPos = to;
        sentYRot = p.getYRot();
        sentXRot = p.getXRot();
        sentOnGround = false;
        sentHorizontalCollision = false;
        positionReminder = 0;
        LOGGER.info("[r3dmenu] server teleport #{} applied to ghost: to {} (moved {} blocks)", new Object[]{++teleportsAbsorbed, to, FakeElytraFlight.f(dist)});
        return true;
    }

    private static void keepCreativeFlightOff(LocalPlayer p) {
        ((PlayerJumpTriggerAccessor)p).r3dmenu$setJumpTriggerTime(0);
        if (p.getAbilities().flying) {
            p.getAbilities().flying = false;
            p.onUpdateAbilities();
        }
    }

    private static String f(double d) {
        return String.format("%.3f", d);
    }
}

