/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Input
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package name.modid.client.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LocalPlayer.class})
public interface LocalPlayerSendStateAccessor {
    @Accessor(value="xLast")
    public double r3dmenu$xLast();

    @Accessor(value="yLast")
    public double r3dmenu$yLast();

    @Accessor(value="zLast")
    public double r3dmenu$zLast();

    @Accessor(value="yRotLast")
    public float r3dmenu$yRotLast();

    @Accessor(value="xRotLast")
    public float r3dmenu$xRotLast();

    @Accessor(value="lastOnGround")
    public boolean r3dmenu$lastOnGround();

    @Accessor(value="lastHorizontalCollision")
    public boolean r3dmenu$lastHorizontalCollision();

    @Accessor(value="wasSprinting")
    public boolean r3dmenu$wasSprinting();

    @Accessor(value="xLast")
    public void r3dmenu$setXLast(double var1);

    @Accessor(value="yLast")
    public void r3dmenu$setYLast(double var1);

    @Accessor(value="zLast")
    public void r3dmenu$setZLast(double var1);

    @Accessor(value="yRotLast")
    public void r3dmenu$setYRotLast(float var1);

    @Accessor(value="xRotLast")
    public void r3dmenu$setXRotLast(float var1);

    @Accessor(value="lastOnGround")
    public void r3dmenu$setLastOnGround(boolean var1);

    @Accessor(value="lastHorizontalCollision")
    public void r3dmenu$setLastHorizontalCollision(boolean var1);

    @Accessor(value="positionReminder")
    public void r3dmenu$setPositionReminder(int var1);

    @Accessor(value="wasSprinting")
    public void r3dmenu$setWasSprinting(boolean var1);

    @Accessor(value="lastSentInput")
    public void r3dmenu$setLastSentInput(Input var1);
}

