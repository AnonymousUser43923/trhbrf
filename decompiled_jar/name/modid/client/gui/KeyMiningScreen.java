/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package name.modid.client.gui;

import java.util.ArrayList;
import java.util.List;
import name.modid.client.KeyMining;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class KeyMiningScreen
extends Screen {
    private final Screen parent;
    private static final int COLS = 2;
    private static final int CELL_W = 178;
    private static final int CELL_H = 22;
    private static final int GRID_TOP = 52;
    private int gridLeft;
    private Button navigateButton;
    private final List<ItemStack> icons = new ArrayList<ItemStack>();

    public KeyMiningScreen(Screen parent) {
        super((Component)Component.literal((String)"ELYTRA LOCATOR"));
        this.parent = parent;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    protected void init() {
        if (this.icons.isEmpty()) {
            for (KeyMining.Target t : KeyMining.TARGETS) {
                this.icons.add(new ItemStack((ItemLike)t.item()));
            }
        }
        this.gridLeft = this.width / 2 - 178;
        this.navigateButton = Button.builder((Component)KeyMiningScreen.navigateLabel(), btn -> {
            KeyMining.navigate();
            btn.setMessage(KeyMiningScreen.navigateLabel());
        }).bounds(this.width / 2 - 100, this.height - 100, 200, 20).build();
        this.navigateButton.active = KeyMining.complete() && !KeyMining.navigating();
        this.addRenderableWidget((GuiEventListener)this.navigateButton);
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)KeyMiningScreen.hideLabel(), btn -> {
            KeyMining.toggleHud();
            btn.setMessage(KeyMiningScreen.hideLabel());
        }).bounds(this.width / 2 - 100, this.height - 76, 200, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)KeyMiningScreen.stopLabel(), btn -> {
            KeyMining.stop();
            btn.setMessage(KeyMiningScreen.stopLabel());
        }).bounds(this.width / 2 - 100, this.height - 52, 200, 20).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Back"), btn -> this.onClose()).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    private static Component navigateLabel() {
        if (KeyMining.navigating()) {
            return Component.literal((String)"Navigating...").withStyle(ChatFormatting.YELLOW);
        }
        return Component.literal((String)"Navigate").withStyle(KeyMining.complete() ? ChatFormatting.GREEN : ChatFormatting.GRAY);
    }

    public void tick() {
        if (this.navigateButton != null) {
            this.navigateButton.active = KeyMining.complete() && !KeyMining.navigating();
            this.navigateButton.setMessage(KeyMiningScreen.navigateLabel());
        }
    }

    private static Component stopLabel() {
        return Component.literal((String)"Stop scan").withStyle(KeyMining.active ? ChatFormatting.RED : ChatFormatting.GRAY);
    }

    private static Component hideLabel() {
        return Component.literal((String)(KeyMining.hudHidden ? "Show overlay" : "Hide overlay")).withStyle(KeyMining.hudHidden ? ChatFormatting.GRAY : ChatFormatting.AQUA);
    }

    private int rows() {
        return (KeyMining.TARGETS.size() + 2 - 1) / 2;
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, -1073741824);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        String title = "ELYTRA LOCATOR";
        graphics.text(this.font, title, this.width / 2 - this.font.width(title) / 2, 16, -10748038);
        String sub = "Click the Elytra to start scanning chunks. This is just a screen gag.";
        graphics.text(this.font, sub, this.width / 2 - this.font.width(sub) / 2, 30, -5592406);
        int hover = this.indexAt(mouseX, mouseY);
        for (int i = 0; i < KeyMining.TARGETS.size(); ++i) {
            boolean mining;
            KeyMining.Target t = KeyMining.TARGETS.get(i);
            int col = i % 2;
            int row = i / 2;
            int x = this.gridLeft + col * 178;
            int y = 52 + row * 22;
            boolean bl = mining = KeyMining.active && KeyMining.target == t;
            if (mining) {
                graphics.fill(x, y, x + 178 - 8, y + 22 - 2, 1616641914);
            } else if (i == hover) {
                graphics.fill(x, y, x + 178 - 8, y + 22 - 2, 0x40FFFFFF);
            }
            graphics.item(this.icons.get(i), x + 2, y + 2);
            graphics.text(this.font, t.name(), x + 24, y + 1, -1);
            graphics.text(this.font, KeyMiningScreen.estimate(t.minutes()), x + 24, y + 11, -8396657);
        }
    }

    private static String estimate(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return "~ " + h + "h" + (String)(m == 0 ? "" : " " + m + "m") + " to scan";
    }

    private int indexAt(double mouseX, double mouseY) {
        if (mouseX < (double)this.gridLeft || mouseX >= (double)(this.gridLeft + 356)) {
            return -1;
        }
        if (mouseY < 52.0 || mouseY >= (double)(52 + this.rows() * 22)) {
            return -1;
        }
        int row = (int)((mouseY - 52.0) / 22.0);
        int col = (int)((mouseX - (double)this.gridLeft) / 178.0);
        int index = row * 2 + col;
        return index >= 0 && index < KeyMining.TARGETS.size() ? index : -1;
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        int index = this.indexAt(click.x(), click.y());
        if (index >= 0) {
            KeyMining.start(KeyMining.TARGETS.get(index));
            this.onClose();
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }
}

