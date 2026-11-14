/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.monster.skeleton.Skeleton
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package name.modid.client.gui;

import name.modid.client.SkeletonSpawner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

final class MenuDiorama {
    private LivingEntity mascot;
    private ClientLevel builtFor;

    MenuDiorama() {
    }

    boolean render(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int scale, float mouseX, float mouseY, float sway) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return false;
        }
        try {
            if (this.mascot == null || this.builtFor != level) {
                this.mascot = new Skeleton(EntityTypes.SKELETON, (Level)level);
                this.mascot.setNoGravity(true);
                this.builtFor = level;
            }
            float lookX = mouseX + Mth.cos((double)sway) * 55.0f;
            float lookY = mouseY + Mth.sin((double)(sway * 0.7f)) * 18.0f;
            InventoryScreen.extractEntityInInventoryFollowsMouse((GuiGraphicsExtractor)g, (int)x0, (int)y0, (int)x1, (int)y1, (int)scale, (float)0.0625f, (float)lookX, (float)lookY, (LivingEntity)this.mascot);
            return true;
        }
        catch (Throwable t) {
            this.mascot = null;
            this.builtFor = null;
            return false;
        }
    }

    static ItemStack fallbackIcon() {
        return SkeletonSpawner.spawnerItem();
    }
}

