/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.entity.CommandBlockEntity
 */
package name.modid.client.gui;

import name.modid.client.PlankCommandBlock;
import name.modid.client.mixin.CommandBlockEditScreenAccessor;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class FakeCommandBlockScreen
extends CommandBlockEditScreen {
    private final BlockPos pos;

    public FakeCommandBlockScreen(CommandBlockEntity fake) {
        super(fake);
        this.pos = fake.getBlockPos();
    }

    protected void populateAndSendPacket() {
        CommandBlockEditScreenAccessor choices = (CommandBlockEditScreenAccessor)((Object)this);
        PlankCommandBlock.configure(this.pos, this.commandEdit.getValue(), choices.r3dmenu$mode(), choices.r3dmenu$conditional(), choices.r3dmenu$autoexec(), this.getCommandBlock().isTrackOutput());
    }
}

