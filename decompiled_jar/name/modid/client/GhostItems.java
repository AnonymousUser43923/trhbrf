/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package name.modid.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import name.modid.client.PhantomItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class GhostItems {
    private static final int PICKUP_DELAY = 10;
    private static final int LIFETIME = 1200;
    private static final List<Ghost> ghosts = new ArrayList<Ghost>();
    private static int nextId = -900000;

    private GhostItems() {
    }

    public static void spawn(ClientLevel level, double x, double y, double z, ItemStack stack, double dx, double dy, double dz) {
        if (stack.isEmpty()) {
            return;
        }
        ItemEntity item = new ItemEntity((Level)level, x, y, z, stack.copy(), dx, dy, dz);
        item.setId(nextId--);
        level.addEntity((Entity)item);
        ghosts.add(new Ghost(item.getId()));
    }

    public static void clear() {
        ghosts.clear();
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            ghosts.clear();
            return;
        }
        Iterator<Ghost> it = ghosts.iterator();
        while (it.hasNext()) {
            Ghost ghost = it.next();
            ++ghost.age;
            Entity entity = level.getEntity(ghost.entityId);
            if (entity instanceof ItemEntity) {
                ItemEntity dropped = (ItemEntity)entity;
                if (ghost.age > 10 && mc.player.distanceToSqr(entity) < 3.0625) {
                    ItemStack stack = dropped.getItem();
                    mc.player.getInventory().add(stack);
                    if (PhantomItems.isPhantom(stack)) {
                        PhantomItems.track(stack);
                    }
                    float pitch = ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f;
                    level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, pitch, false);
                    level.removeEntity(ghost.entityId, Entity.RemovalReason.DISCARDED);
                    it.remove();
                    continue;
                }
            }
            if (entity != null && --ghost.ticksLeft > 0) continue;
            level.removeEntity(ghost.entityId, Entity.RemovalReason.DISCARDED);
            it.remove();
        }
    }

    private static final class Ghost {
        final int entityId;
        int age = 0;
        int ticksLeft = 1200;

        Ghost(int entityId) {
            this.entityId = entityId;
        }
    }
}

