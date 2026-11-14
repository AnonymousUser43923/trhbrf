/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package name.modid.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import name.modid.client.DropperSkin;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class DropperSkinScreen
extends Screen {
    private final Screen parent;
    private final List<ItemStack> allItems = new ArrayList<ItemStack>();
    private final List<ItemStack> shown = new ArrayList<ItemStack>();
    private static final int CELL = 20;
    private static final int ICON = 16;
    private static final int MARGIN = 20;
    private EditBox searchBox;
    private boolean editingFiller = false;
    private int columns = 1;
    private int gridTop;
    private int gridBottom;
    private int scrollRow = 0;

    public DropperSkinScreen(Screen parent) {
        super((Component)Component.literal((String)"50/50 Gamble Rig"));
        this.parent = parent;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    protected void init() {
        DropperSkin.ensureInit();
        if (this.allItems.isEmpty()) {
            for (Item item : BuiltInRegistries.ITEM) {
                ItemStack stack = new ItemStack((ItemLike)item);
                if (stack.isEmpty()) continue;
                this.allItems.add(stack);
            }
            this.shown.addAll(this.allItems);
        }
        this.gridTop = 68;
        this.gridBottom = this.height - 54;
        this.columns = Math.max(1, (this.width - 40) / 20);
        this.clampScroll();
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)DropperSkinScreen.toggleLabel(), btn -> {
            DropperSkin.enabled = !DropperSkin.enabled;
            btn.setMessage(DropperSkinScreen.toggleLabel());
        }).bounds(20, 20, 150, 18).build());
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)this.editingLabel(), btn -> {
            this.editingFiller = !this.editingFiller;
            btn.setMessage(this.editingLabel());
        }).bounds(20, 42, 150, 18).build());
        String previousText = this.searchBox == null ? "" : this.searchBox.getValue();
        this.searchBox = new EditBox(this.font, this.width / 2, 21, this.width / 2 - 20 - 4, 18, (Component)Component.literal((String)"Search"));
        this.searchBox.setHint((Component)Component.literal((String)"Search items..."));
        this.searchBox.setValue(previousText);
        this.searchBox.setResponder(this::applyFilter);
        this.addRenderableWidget((GuiEventListener)this.searchBox);
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Back"), btn -> this.onClose()).bounds(this.width / 2 - 100, this.height - 26, 200, 20).build());
    }

    private void applyFilter(String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        this.shown.clear();
        if (q.isEmpty()) {
            this.shown.addAll(this.allItems);
        } else {
            for (ItemStack stack : this.allItems) {
                String id = BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).getPath();
                String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                if (!id.contains(q) && !name.contains(q)) continue;
                this.shown.add(stack);
            }
        }
        this.scrollRow = 0;
    }

    private int visibleRows() {
        return Math.max(1, (this.gridBottom - this.gridTop) / 20);
    }

    private int totalRows() {
        return (this.shown.size() + this.columns - 1) / this.columns;
    }

    private void clampScroll() {
        int maxScroll = Math.max(0, this.totalRows() - this.visibleRows());
        if (this.scrollRow < 0) {
            this.scrollRow = 0;
        }
        if (this.scrollRow > maxScroll) {
            this.scrollRow = maxScroll;
        }
    }

    private ItemStack editTarget() {
        return this.editingFiller ? DropperSkin.fillerItem : DropperSkin.skinItem;
    }

    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(this.font, "50/50 Gamble Rig", 20, 8, -1);
        int fillerColor = this.editingFiller ? -171 : -5592406;
        int skinColor = this.editingFiller ? -5592406 : -171;
        graphics.text(this.font, "Filler:", 20, this.gridBottom + 6, fillerColor);
        graphics.item(DropperSkin.fillerItem, 64, this.gridBottom + 2);
        graphics.text(this.font, DropperSkin.fillerItem.getHoverName().getString(), 86, this.gridBottom + 6, fillerColor);
        graphics.text(this.font, "Disguise:", 20, this.gridBottom + 22, skinColor);
        graphics.item(DropperSkin.skinItem, 64, this.gridBottom + 18);
        graphics.text(this.font, DropperSkin.skinItem.getHoverName().getString(), 86, this.gridBottom + 22, skinColor);
        graphics.enableScissor(20, this.gridTop, 20 + this.columns * 20, this.gridBottom);
        int firstIndex = this.scrollRow * this.columns;
        int lastIndex = Math.min(this.shown.size(), firstIndex + (this.visibleRows() + 1) * this.columns);
        for (int i = firstIndex; i < lastIndex; ++i) {
            int col = i % this.columns;
            int rowOnScreen = i / this.columns - this.scrollRow;
            int x = 20 + col * 20;
            int y = this.gridTop + rowOnScreen * 20;
            if (ItemStack.isSameItem((ItemStack)this.shown.get(i), (ItemStack)this.editTarget())) {
                graphics.fill(x, y, x + 20, y + 20, -2141847723);
            } else if (this.indexAt(mouseX, mouseY) == i) {
                graphics.fill(x, y, x + 20, y + 20, 0x40FFFFFF);
            }
            int inset = 2;
            graphics.item(this.shown.get(i), x + inset, y + inset);
        }
        graphics.disableScissor();
        int hoverIndex = this.indexAt(mouseX, mouseY);
        if (hoverIndex >= 0) {
            graphics.setTooltipForNextFrame(this.font, this.shown.get(hoverIndex), mouseX, mouseY);
        }
    }

    private int indexAt(double mouseX, double mouseY) {
        if (mouseX < 20.0 || mouseX >= (double)(20 + this.columns * 20)) {
            return -1;
        }
        if (mouseY < (double)this.gridTop || mouseY >= (double)this.gridBottom) {
            return -1;
        }
        int rowOnScreen = (int)((mouseY - (double)this.gridTop) / 20.0);
        int col = (int)((mouseX - 20.0) / 20.0);
        int index = (rowOnScreen + this.scrollRow) * this.columns + col;
        if (index < 0 || index >= this.shown.size()) {
            return -1;
        }
        return index;
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        int index = this.indexAt(click.x(), click.y());
        if (index >= 0) {
            ItemStack picked = new ItemStack((ItemLike)this.shown.get(index).getItem());
            if (this.editingFiller) {
                DropperSkin.fillerItem = picked;
            } else {
                DropperSkin.skinItem = picked;
            }
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0.0) {
            --this.scrollRow;
        } else if (scrollY < 0.0) {
            ++this.scrollRow;
        }
        this.clampScroll();
        return true;
    }

    private static Component toggleLabel() {
        return Component.literal((String)"Rig").withStyle(DropperSkin.enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private Component editingLabel() {
        return Component.literal((String)("Editing: " + (this.editingFiller ? "Filler" : "Disguise")));
    }
}

