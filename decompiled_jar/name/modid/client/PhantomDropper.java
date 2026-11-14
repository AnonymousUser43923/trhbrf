/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.DispenserBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package name.modid.client;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import name.modid.client.GhostItems;
import name.modid.client.PhantomItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class PhantomDropper {
    public static final int SIZE = 9;
    public static volatile boolean enabled = false;
    public static volatile int riggedSlot = 0;
    private static final Map<Long, NonNullList<ItemStack>> contents = new ConcurrentHashMap<Long, NonNullList<ItemStack>>();

    private PhantomDropper() {
    }

    public static NonNullList<ItemStack> contentsAt(BlockPos pos) {
        return contents.computeIfAbsent(pos.asLong(), key -> NonNullList.withSize((int)9, (Object)ItemStack.EMPTY));
    }

    public static boolean hasContents(BlockPos pos) {
        NonNullList<ItemStack> list = contents.get(pos.asLong());
        if (list == null) {
            return false;
        }
        for (ItemStack stack : list) {
            if (stack.isEmpty()) continue;
            return true;
        }
        return false;
    }

    public static void collectIds(Set<UUID> out) {
        for (NonNullList<ItemStack> list : contents.values()) {
            for (ItemStack stack : list) {
                UUID id = PhantomItems.idOf(stack);
                if (id == null) continue;
                out.add(id);
            }
        }
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!value) {
            contents.clear();
        }
    }

    public static void fire(ClientLevel level, BlockPos pos) {
        NonNullList<ItemStack> list = contents.get(pos.asLong());
        if (list == null) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.is((Object)Blocks.DROPPER)) {
            return;
        }
        int slot = riggedSlot;
        if (slot < 0 || slot >= 9 || ((ItemStack)list.get(slot)).isEmpty()) {
            return;
        }
        ItemStack stack = (ItemStack)list.get(slot);
        Direction facing = (Direction)state.getValue((Property)DispenserBlock.FACING);
        double x = (double)pos.getX() + 0.5 + (double)facing.getStepX() * 0.7;
        double y = (double)pos.getY() + 0.5 + (double)facing.getStepY() * 0.7 - 0.15;
        double z = (double)pos.getZ() + 0.5 + (double)facing.getStepZ() * 0.7;
        double speed = 0.25;
        GhostItems.spawn(level, x, y, z, stack.copyWithCount(1), (double)facing.getStepX() * speed, (double)facing.getStepY() * speed + 0.1, (double)facing.getStepZ() * speed);
        level.playLocalSound(pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
        for (int i = 0; i < 10; ++i) {
            double r = level.getRandom().nextDouble() * 0.2 + 0.01;
            level.addParticle((ParticleOptions)ParticleTypes.SMOKE, x, y, z, (double)facing.getStepX() * r, (double)facing.getStepY() * r, (double)facing.getStepZ() * r);
        }
        UUID id = PhantomItems.idOf(stack);
        stack.shrink(1);
        if (stack.isEmpty() && id != null) {
            PhantomItems.forget(id);
        }
    }

    public static void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            contents.clear();
            return;
        }
        contents.keySet().removeIf(key -> !level.getBlockState(BlockPos.of((long)key)).is((Object)Blocks.DROPPER));
    }
}

