/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.PlayerSkinWidget
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.world.entity.player.PlayerSkin
 */
package name.modid.client.gui;

import java.util.function.Supplier;
import name.modid.client.mixin.PlayerSkinWidgetAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.entity.player.PlayerSkin;

public class SpinningSkinWidget
extends PlayerSkinWidget {
    private static final float SECONDS_PER_TURN = 9.0f;

    public SpinningSkinWidget(int width, int height, EntityModelSet models, Supplier<PlayerSkin> skin) {
        super(width, height, models, skin);
    }

    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        float millisPerTurn = 9000.0f;
        float degrees = (float)(System.currentTimeMillis() % (long)millisPerTurn) / millisPerTurn * 360.0f;
        ((PlayerSkinWidgetAccessor)((Object)this)).r3dmenu$setRotationY(degrees);
        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
    }
}

