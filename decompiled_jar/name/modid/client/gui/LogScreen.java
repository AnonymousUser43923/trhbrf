/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.PlayerSkin
 *  net.minecraft.world.item.ItemStack
 */
package name.modid.client.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import name.modid.client.MacroProfile;
import name.modid.client.PayoutLog;
import name.modid.client.gui.SpinningSkinWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;

public class LogScreen
extends Screen {
    private final Screen parent;
    private static final int MARGIN = 24;
    private static final int ROW_H = 30;
    private static final int LIST_TOP = 54;
    private static final int CELL = 18;
    private int scroll = 0;
    private int expanded = -1;
    private int confirmDelete = -1;
    private PayoutLog.Entry capturing = null;
    private int listLeft;
    private int listWidth;
    private int listBottom;

    public LogScreen(Screen parent) {
        super((Component)Component.literal((String)"LapisMenu Log"));
        this.parent = parent;
    }

    public boolean isPauseScreen() {
        return false;
    }

    protected void init() {
        PlayerInfo info;
        this.listLeft = 24;
        this.listWidth = this.width - 48;
        this.listBottom = this.height - 34;
        this.addRenderableWidget((GuiEventListener)Button.builder((Component)Component.literal((String)"Back"), b -> this.onClose()).bounds(this.width / 2 - 50, this.height - 26, 100, 20).build());
        List<PayoutLog.Entry> entries = PayoutLog.entries();
        if (!entries.isEmpty() && Minecraft.getInstance().getConnection() != null && (info = Minecraft.getInstance().getConnection().getPlayerInfoIgnoreCase(entries.get((int)0).payer)) != null && info.getSkin() != null) {
            PlayerSkin skin = info.getSkin();
            SpinningSkinWidget w = new SpinningSkinWidget(22, 34, this.minecraft.getEntityModels(), () -> skin);
            w.setPosition(this.width - 24 - 26, 14);
            this.addRenderableWidget((GuiEventListener)w);
        }
    }

    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    private int accent() {
        return MacroProfile.isOrange() ? -24004 : -45747;
    }

    private List<Row> layout() {
        ArrayList<Row> rows = new ArrayList<Row>();
        List<PayoutLog.Entry> entries = PayoutLog.entries();
        int y = 54;
        int perRow = Math.max(1, (this.listWidth - 16) / 18);
        for (int i = 0; i < entries.size(); ++i) {
            int h = 30;
            int echestRows = 0;
            if (i == this.expanded) {
                PayoutLog.Entry e = entries.get(i);
                h += 16;
                if (e.echest == null) {
                    h += 24;
                } else {
                    echestRows = (int)Math.ceil((double)e.echest.size() / (double)perRow);
                    h += 6 + echestRows * 18 + 6;
                }
            }
            rows.add(new Row(i, y, h, echestRows));
            y += h + 4;
        }
        return rows;
    }

    private int contentHeight(List<Row> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        Row last = rows.get(rows.size() - 1);
        return last.y() + last.height() - 54;
    }

    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, -267776226, -134019570);
        List<PayoutLog.Entry> entries = PayoutLog.entries();
        String title = "LAPISMENU LOG";
        g.text(this.font, title, this.width / 2 - this.font.width(title) / 2, 18, this.accent(), true);
        String sub = entries.size() + " payment(s) logged";
        g.text(this.font, sub, this.width / 2 - this.font.width(sub) / 2, 32, -6643020, true);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        if (entries.isEmpty()) {
            String none = "No payments of 100M+ logged yet.";
            g.text(this.font, none, this.width / 2 - this.font.width(none) / 2, 74, -8946034, true);
        } else {
            List<Row> rows = this.layout();
            this.clampScroll(rows);
            g.enableScissor(this.listLeft, 54, this.listLeft + this.listWidth, this.listBottom);
            for (Row row : rows) {
                int ry = row.y() - this.scroll;
                if (ry > this.listBottom || ry + row.height() < 54) continue;
                this.drawRow(g, entries.get(row.index()), row, ry, mouseX, mouseY);
            }
            g.disableScissor();
        }
        if (this.capturing != null) {
            this.drawCapturePrompt(g, mouseX, mouseY);
        }
    }

    private void drawRow(GuiGraphicsExtractor g, PayoutLog.Entry e, Row row, int ry, int mouseX, int mouseY) {
        boolean open = row.index() == this.expanded;
        g.fill(this.listLeft, ry, this.listLeft + this.listWidth, ry + row.height(), 1074927136);
        g.outline(this.listLeft, ry, this.listWidth, row.height(), 0x1EFFFFFF);
        g.text(this.font, e.payer, this.listLeft + 8, ry + 6, -787457, true);
        String amt = "$" + PayoutLog.shortAmount(e.amount);
        g.text(this.font, amt, this.listLeft + 8, ry + 18, this.accent(), true);
        String when = LogScreen.relTime(e.time);
        g.text(this.font, when, this.listLeft + this.listWidth - 70 - this.font.width(when), ry + 12, -8946034, true);
        int expX = this.listLeft + this.listWidth - 58;
        g.fill(expX, ry + 7, expX + 22, ry + 20, 0x33FFFFFF);
        g.text(this.font, open ? "^" : "v", expX + 9, ry + 9, -2236963, true);
        if (this.confirmDelete == row.index()) {
            int cX = this.listLeft + this.listWidth - 32;
            g.fill(cX, ry + 7, cX + 24, ry + 20, 0x66FF3333);
            g.text(this.font, "OK?", cX + 4, ry + 9, -1, true);
        } else {
            int dX = this.listLeft + this.listWidth - 32;
            g.fill(dX, ry + 7, dX + 24, ry + 20, 0x33FF5555);
            g.text(this.font, "Del", dX + 5, ry + 9, -25958, true);
        }
        if (!open) {
            return;
        }
        int innerY = ry + 30;
        g.text(this.font, "Received " + LogScreen.fullTime(e.time), this.listLeft + 8, innerY, -6643020, true);
        innerY += 16;
        if (e.echest == null) {
            int bx = this.listLeft + 8;
            g.fill(bx, innerY, bx + 110, innerY + 18, 0x55000000);
            g.outline(bx, innerY, 110, 18, this.accent());
            g.text(this.font, "Log Echest?", bx + 8, innerY + 5, this.accent(), true);
            return;
        }
        int perRow = Math.max(1, (this.listWidth - 16) / 18);
        int gx = this.listLeft + 8;
        int gy = innerY + 6;
        for (int i = 0; i < e.echest.size(); ++i) {
            int col = i % perRow;
            int rrow = i / perRow;
            int ix = gx + col * 18;
            int iy = gy + rrow * 18;
            ItemStack stack = PayoutLog.toStack(e.echest.get(i));
            g.item(stack, ix, iy);
            g.itemDecorations(this.font, stack, ix, iy);
            if (mouseX < ix || mouseX >= ix + 16 || mouseY < iy || mouseY >= iy + 16) continue;
            List<PayoutLog.Item> contents = e.echest.get((int)i).contents;
            if (contents != null && !contents.isEmpty()) {
                ArrayList<Object> lines = new ArrayList<Object>();
                lines.add(stack.getHoverName());
                for (PayoutLog.Item c : contents) {
                    ItemStack cs = PayoutLog.toStack(c);
                    lines.add(Component.literal((String)(" " + c.count + "x ")).append(cs.getHoverName()));
                }
                g.setTooltipForNextFrame(this.font, lines, Optional.empty(), mouseX, mouseY);
                continue;
            }
            g.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
        }
    }

    private void drawCapturePrompt(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.fill(0, 0, this.width, this.height, -1073741824);
        int cx = this.width / 2;
        int cy = this.height / 2;
        String l1 = "Put all echest items in your inventory";
        g.text(this.font, l1, cx - this.font.width(l1) / 2, cy - 24, -787457, true);
        int dw = 70;
        int doneX = cx - dw - 6;
        int backX = cx + 6;
        g.fill(doneX, cy, doneX + dw, cy + 20, 0x5500AA00);
        g.outline(doneX, cy, dw, 20, -11141291);
        g.text(this.font, "Done", doneX + dw / 2 - this.font.width("Done") / 2, cy + 6, -1048593, true);
        g.fill(backX, cy, backX + dw, cy + 20, 0x55AA0000);
        g.outline(backX, cy, dw, 20, -43691);
        g.text(this.font, "Back", backX + dw / 2 - this.font.width("Back") / 2, cy + 6, -4113, true);
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean dbl) {
        double mx = click.x();
        double my = click.y();
        if (this.capturing != null) {
            int cx = this.width / 2;
            int cy = this.height / 2;
            int dw = 70;
            int doneX = cx - dw - 6;
            int backX = cx + 6;
            if (LogScreen.inside(mx, my, doneX, cy, dw, 20)) {
                PayoutLog.logEchest(this.capturing);
                this.capturing = null;
                return true;
            }
            if (LogScreen.inside(mx, my, backX, cy, dw, 20)) {
                this.capturing = null;
                return true;
            }
            return true;
        }
        List<PayoutLog.Entry> entries = PayoutLog.entries();
        List<Row> rows = this.layout();
        for (Row row : rows) {
            int by;
            int bx;
            int ry = row.y() - this.scroll;
            if (ry + row.height() < 54 || ry > this.listBottom) continue;
            PayoutLog.Entry e = entries.get(row.index());
            int expX = this.listLeft + this.listWidth - 58;
            if (LogScreen.inside(mx, my, expX, ry + 7, 22, 13)) {
                this.expanded = this.expanded == row.index() ? -1 : row.index();
                this.confirmDelete = -1;
                return true;
            }
            int dX = this.listLeft + this.listWidth - 32;
            if (LogScreen.inside(mx, my, dX, ry + 7, 24, 13)) {
                if (this.confirmDelete == row.index()) {
                    PayoutLog.delete(e);
                    this.confirmDelete = -1;
                    this.expanded = -1;
                } else {
                    this.confirmDelete = row.index();
                }
                return true;
            }
            if (row.index() != this.expanded || e.echest != null || !LogScreen.inside(mx, my, bx = this.listLeft + 8, by = ry + 30 + 16, 110, 18)) continue;
            this.capturing = e;
            return true;
        }
        return super.mouseClicked(click, dbl);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double sx, double sy) {
        this.scroll -= (int)(sy * 18.0);
        this.clampScroll(this.layout());
        return true;
    }

    private void clampScroll(List<Row> rows) {
        int max = Math.max(0, this.contentHeight(rows) - (this.listBottom - 54));
        if (this.scroll < 0) {
            this.scroll = 0;
        }
        if (this.scroll > max) {
            this.scroll = max;
        }
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
    }

    private static String relTime(long then) {
        long s = (System.currentTimeMillis() - then) / 1000L;
        if (s < 60L) {
            return s + "s ago";
        }
        if (s < 3600L) {
            return s / 60L + "m ago";
        }
        if (s < 86400L) {
            return s / 3600L + "h ago";
        }
        return s / 86400L + "d ago";
    }

    private static String fullTime(long then) {
        return new SimpleDateFormat("MMM d, HH:mm").format(new Date(then));
    }

    private record Row(int index, int y, int height, int echestRows) {
    }
}

