/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractButton
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.InputWithModifiers
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.ClientAsset$ResourceTexture
 *  net.minecraft.core.ClientAsset$Texture
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.PlayerModelType
 *  net.minecraft.world.entity.player.PlayerSkin
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package name.modid.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import name.modid.client.AutoPearl;
import name.modid.client.DevMode;
import name.modid.client.ElytraSkin;
import name.modid.client.FakeChat;
import name.modid.client.FakeScoreboard;
import name.modid.client.GambleGui;
import name.modid.client.KeyMining;
import name.modid.client.MacroProfile;
import name.modid.client.ModSettings;
import name.modid.client.PayoutLog;
import name.modid.client.PhantomDropper;
import name.modid.client.gui.AutoPearlScreen;
import name.modid.client.gui.CreativeMenuScreen;
import name.modid.client.gui.ElytraSkinScreen;
import name.modid.client.gui.FakeScoreboardScreen;
import name.modid.client.gui.GambleGuiScreen;
import name.modid.client.gui.KeyMiningScreen;
import name.modid.client.gui.LogScreen;
import name.modid.client.gui.MenuDiorama;
import name.modid.client.gui.SpinningSkinWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ModMenuScreen
extends Screen {
    private static final int SCRIM = -1308622848;
    private static final int PANEL_TOP = -234222572;
    private static final int PANEL_BOT = -184220149;
    private static final int TXT_HI = -787457;
    private static final int TXT = -5590332;
    private static final int TXT_DIM = -9011570;
    private static final int ROW_BG = 1074927136;
    private static final int ROW_OUTLINE = 0x1EFFFFFF;
    private static final int TAB_HOVER = 0x18FFFFFF;
    private static final int NEUTRAL_LAPIS = -12672769;
    private int accentPrimary;
    private int accentSecondary;
    private int rowHotTop;
    private int rowHotBot;
    private int tabSelected;
    private int gridTint;
    private static final int PAD = 10;
    private static final int HEADER_H = 22;
    private static final int SIDEBAR_W = 62;
    private static final int GAP = 8;
    private static final int CONTENT_W = 236;
    private static final int TAB_H = 27;
    private static final int TAB_GAP = 5;
    private static final int ROW_H = 34;
    private static final int ROW_GAP = 6;
    private static final int MAX_ROWS = 3;
    private static final int FOOTER_GAP = 8;
    private static final int FOOTER_H = 18;
    private static final int FOOTER_BTN_W = 72;
    private static final int FOOTER_BTN_GAP = 10;
    private static final int TAB_COUNT = 5;
    private static final int SIDEBAR_H = 155;
    private static final int CONTENT_H = 114;
    private static final int BODY_H = Math.max(155, 114);
    private static final int PANEL_W = 326;
    private static final int PANEL_H = 32 + BODY_H + 8 + 18 + 10;
    private int panelX;
    private int panelY;
    private int contentLeft;
    private int contentTop;
    private int selected = 0;
    private long flashUntil = 0L;
    private String flashMessage = "";
    private long openedAt = System.currentTimeMillis();
    private final MenuDiorama diorama = new MenuDiorama();
    private long tabSwitchedAt = 0L;
    private static final int TRAIL_LEN = 10;
    private final int[] trailX = new int[10];
    private final int[] trailY = new int[10];
    private boolean trailSeeded = false;
    private static final int PARTICLE_COUNT = 54;
    private static final int GRID_SPACING = 24;
    private static final int STAGE_W = 96;
    private CategoryDef[] categories = new CategoryDef[0];
    private final List<AbstractWidget> contentWidgets = new ArrayList<AbstractWidget>();
    private final List<InfoLine> infoLines = new ArrayList<InfoLine>();

    private static Supplier<PlayerSkin> authorSkin() {
        ClientAsset.ResourceTexture tex = new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath((String)"r3dmenu", (String)MacroProfile.skinPath()));
        PlayerSkin skin = new PlayerSkin((ClientAsset.Texture)tex, (ClientAsset.Texture)tex, (ClientAsset.Texture)tex, PlayerModelType.WIDE, false);
        return () -> skin;
    }

    private void setupTheme() {
        if (MacroProfile.isOrange()) {
            this.accentPrimary = -24004;
            this.accentSecondary = -4034530;
            this.rowHotTop = -524128226;
            this.rowHotBot = -528333294;
        } else {
            this.accentPrimary = -45747;
            this.accentSecondary = -4185037;
            this.rowHotTop = -524278733;
            this.rowHotBot = -528475096;
        }
        this.tabSelected = ModMenuScreen.alpha(this.accentPrimary, 58);
        this.gridTint = ModMenuScreen.alpha(this.accentPrimary, 12);
    }

    private static String title() {
        return MacroProfile.brandTitle() + " MOD MENU";
    }

    public ModMenuScreen() {
        super((Component)Component.literal((String)(MacroProfile.brandName() + " Menu")));
        this.setupTheme();
    }

    public boolean isPauseScreen() {
        return false;
    }

    private CategoryDef[] buildCategories() {
        return new CategoryDef[]{new CategoryDef("DISGUISES", new ItemStack((ItemLike)Items.ELYTRA), List.of(ModMenuScreen.navStatus("Fake Scoreboard", "Show a fake DonutSMP-style sidebar.", () -> FakeScoreboard.enabled, () -> new FakeScoreboardScreen(this)), ModMenuScreen.navStatus("GambleGUI Overlay", "Show a fake gambling advert overlay.", () -> GambleGui.enabled, () -> new GambleGuiScreen(this)))), new CategoryDef("BLOCKS", new ItemStack((ItemLike)Items.DROPPER), List.of(ModMenuScreen.nav("Ghost Blocks", "Browse every item and drop a fake stack.", () -> new CreativeMenuScreen(this)), ModMenuScreen.toggle("Phantom Dropper", "Disguise droppers with a fake block.", () -> PhantomDropper.enabled, () -> PhantomDropper.setEnabled(!PhantomDropper.enabled)))), new CategoryDef("TOOLS", new ItemStack((ItemLike)Items.COMPASS), List.of(ModMenuScreen.navStatus("Elytra Locator", "Fake chunk-scanner for elytras.", () -> KeyMining.active, () -> new KeyMiningScreen(this)), ModMenuScreen.toggle("Fake Chat", "Enable the fake chat screen (R).", () -> FakeChat.enabled, () -> {
            FakeChat.enabled = !FakeChat.enabled;
        }))), new CategoryDef("AUTO", new ItemStack((ItemLike)Items.ENDER_PEARL), List.of(ModMenuScreen.action("/tpauto", "Toggle auto-accepting teleport requests.", () -> this.runCommand("tpauto", "Sent /tpauto")), ModMenuScreen.navStatus("Auto Pearl", "Pull your stasis pearl from a second account.", () -> AutoPearl.enabled, () -> new AutoPearlScreen(this)))), new CategoryDef("SYSTEM", new ItemStack((ItemLike)Items.COMPARATOR), this.systemRows())};
    }

    private List<RowSpec> systemRows() {
        RowSpec second = DevMode.enabled() && PayoutLog.active() ? ModMenuScreen.nav("LOG", "Payout + ender-chest log (dev).", () -> new LogScreen(this)) : ModMenuScreen.info("Credits", "made by " + MacroProfile.brandName());
        return List.of(ModMenuScreen.info(MacroProfile.brandName(), "The mod menu you're looking at right now."), second, ModMenuScreen.navStatus("Chestplate -> Elytra", "Disguise a copper chestplate as an elytra.", () -> ElytraSkin.enabled, () -> new ElytraSkinScreen(this)));
    }

    private static RowSpec toggle(String title, String desc, BooleanSupplier state, Runnable flip) {
        return new RowSpec(Kind.TOGGLE, title, desc, state, flip, null);
    }

    private static RowSpec navStatus(String title, String desc, BooleanSupplier state, Supplier<Screen> open) {
        return new RowSpec(Kind.NAV_STATUS, title, desc, state, null, open);
    }

    private static RowSpec nav(String title, String desc, Supplier<Screen> open) {
        return new RowSpec(Kind.NAV, title, desc, null, null, open);
    }

    private static RowSpec info(String title, String desc) {
        return new RowSpec(Kind.INFO, title, desc, null, null, null);
    }

    private static RowSpec action(String title, String desc, Runnable run) {
        return new RowSpec(Kind.ACTION, title, desc, null, run, null);
    }

    private void runCommand(String command, String note) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            this.flash("Not connected to a server");
            return;
        }
        connection.sendCommand(command);
        this.flash(note);
    }

    private void flash(String message) {
        this.flashMessage = message;
        this.flashUntil = System.currentTimeMillis() + 1200L;
    }

    protected void init() {
        if (this.categories.length == 0) {
            this.categories = this.buildCategories();
            this.openedAt = System.currentTimeMillis();
            this.playUi((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 0.7f, 0.5f);
            this.playUi(SoundEvents.BEACON_ACTIVATE, 1.7f, 0.18f);
        }
        this.panelX = this.width / 2 - 163;
        this.panelY = Math.max(8, this.height / 2 - PANEL_H / 2);
        int sidebarX = this.panelX + 10;
        int sidebarY = this.panelY + 10 + 22;
        for (int i = 0; i < this.categories.length; ++i) {
            SidebarButton b = new SidebarButton(this, i, this.categories[i]);
            b.setPosition(sidebarX, sidebarY + i * 32);
            this.addRenderableWidget((GuiEventListener)b);
        }
        this.contentLeft = sidebarX + 62 + 8;
        this.contentTop = sidebarY;
        int footerY = this.panelY + 10 + 22 + BODY_H + 8;
        int footerTotalW = 154;
        int footerX = this.panelX + (326 - footerTotalW) / 2;
        MenuActionButton save = new MenuActionButton("SAVE", 72, this.accentPrimary, () -> {
            ModSettings.save();
            this.flash("Settings saved");
        });
        save.setPosition(footerX, footerY);
        this.addRenderableWidget((GuiEventListener)save);
        MenuActionButton back = new MenuActionButton("BACK", 72, this.accentSecondary, () -> ((ModMenuScreen)this).onClose());
        back.setPosition(footerX + 72 + 10, footerY);
        this.addRenderableWidget((GuiEventListener)back);
        this.contentWidgets.clear();
        this.selectCategory(Math.min(this.selected, this.categories.length - 1));
    }

    private void selectCategory(int idx) {
        if (idx != this.selected) {
            this.tabSwitchedAt = System.currentTimeMillis();
            this.playUi((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 1.3f, 0.4f);
        }
        this.selected = idx;
        for (AbstractWidget abstractWidget : this.contentWidgets) {
            this.removeWidget((GuiEventListener)abstractWidget);
        }
        this.contentWidgets.clear();
        this.infoLines.clear();
        int y = this.contentTop;
        for (RowSpec spec : this.categories[idx].rows()) {
            if (spec.kind() == Kind.INFO) {
                this.infoLines.add(new InfoLine(spec.title(), spec.desc(), this.contentLeft, y));
            } else {
                RowButton btn = new RowButton(this, spec);
                btn.setPosition(this.contentLeft, y);
                this.addRenderableWidget((GuiEventListener)btn);
                this.contentWidgets.add((AbstractWidget)btn);
            }
            y += 40;
        }
        if (idx == this.categories.length - 1) {
            SpinningSkinWidget spinningSkinWidget = new SpinningSkinWidget(18, 26, this.minecraft.getEntityModels(), ModMenuScreen.authorSkin());
            spinningSkinWidget.setPosition(this.contentLeft + 236 - 28, this.contentTop + 4);
            this.addRenderableWidget((GuiEventListener)spinningSkinWidget);
            this.contentWidgets.add((AbstractWidget)spinningSkinWidget);
        }
    }

    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        long now = System.currentTimeMillis();
        float t = (float)(now - this.openedAt) / 1000.0f;
        float intro = Mth.clamp((float)(t / 0.45f), (float)0.0f, (float)1.0f);
        float cx = (float)this.width / 2.0f;
        float cy = (float)this.height / 2.0f;
        float pdx = ((float)mouseX - cx) / Math.max(1.0f, (float)this.width);
        float pdy = ((float)mouseY - cy) / Math.max(1.0f, (float)this.height);
        this.drawBackdrop(g, t, pdx, pdy);
        int liveAccent = ModMenuScreen.lerpColor(-12672769, this.accentPrimary, intro);
        int px = this.panelX;
        int py = this.panelY;
        int right = px + 326;
        int bottom = py + PANEL_H;
        float shimmer = 0.5f + 0.5f * Mth.sin((double)((float)now / 900.0f));
        this.drawStage(g, mouseX, mouseY, t, shimmer);
        g.fill(px + 3, py + 4, right + 3, bottom + 4, 0x50000000);
        g.fillGradient(px, py, right, bottom, -234222572, -184220149);
        g.fillGradient(px, py, right, py + 40, 0x14FFFFFF, 0xFFFFFF);
        this.drawSheen(g, px, py, right, bottom, t, pdx);
        int haloA = (int)(55.0f + 40.0f * shimmer + 120.0f * (1.0f - intro));
        g.outline(px - 1, py - 1, 328, PANEL_H + 2, ModMenuScreen.alpha(liveAccent, haloA));
        g.outline(px, py, 326, PANEL_H, liveAccent);
        this.drawCornerBrackets(g, px, py, right, bottom, ModMenuScreen.alpha(liveAccent, (int)(200.0f + 55.0f * shimmer)));
        Font f = this.font;
        String title = ModMenuScreen.title();
        int titleW = f.width(title);
        int titleCx = px + 163;
        int titleY = py + 10 - 1;
        g.text(f, title, titleCx - titleW / 2 + 1, titleY + 1, -1073741824);
        g.text(f, title, titleCx - titleW / 2, titleY, -787457);
        ModMenuScreen.drawSparkle(g, titleCx - titleW / 2 - 9, titleY + 4, ModMenuScreen.alpha(liveAccent, (int)(180.0f + 60.0f * shimmer)));
        ModMenuScreen.drawSparkle(g, titleCx + titleW / 2 + 9, titleY + 4, ModMenuScreen.alpha(liveAccent, (int)(180.0f + 60.0f * shimmer)));
        int hdY = titleY + 11;
        g.fillGradient(px + 10, hdY, right - 10, hdY + 1, liveAccent, this.accentSecondary);
        this.drawWirePulse(g, t, now);
        for (InfoLine line : this.infoLines) {
            g.fill(line.x(), line.y(), line.x() + 236, line.y() + 34, 1074927136);
            g.outline(line.x(), line.y(), 236, 34, 0x1EFFFFFF);
            g.text(f, line.title(), line.x() + 8, line.y() + 6, -787457);
            g.text(f, ModMenuScreen.fit(f, line.desc(), 220), line.x() + 8, line.y() + 18, -9011570);
        }
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        this.drawCursorTrail(g, mouseX, mouseY);
        if (now < this.flashUntil) {
            g.centeredText(f, this.flashMessage, px + 163, py + 10 + 22 + BODY_H + 1, this.accentPrimary);
        }
    }

    private void drawBackdrop(GuiGraphicsExtractor g, float t, float pdx, float pdy) {
        g.fillGradient(0, 0, this.width, this.height, -469302511, -234815480);
        int gridColor = this.gridTint;
        int ox = Math.floorMod((int)(t * 9.0f + pdx * 46.0f), 24);
        int oy = Math.floorMod((int)(t * 6.0f + pdy * 46.0f), 24);
        for (int x = ox; x < this.width; x += 24) {
            g.fill(x, 0, x + 1, this.height, gridColor);
        }
        for (int y = oy; y < this.height; y += 24) {
            g.fill(0, y, this.width, y + 1, gridColor);
        }
        Identifier lapis = MacroProfile.lapisTexture();
        for (int i = 0; i < 54; ++i) {
            int depth = i % 3;
            float speed = 7 + i % 5 * 3 + depth * 5;
            float span = this.height + 30;
            float baseX = i * 73 % Math.max(1, this.width);
            float y = (float)(this.height + 15) - t * speed % span;
            float x = baseX + Mth.sin((double)(t * 0.4f + (float)i)) * 7.0f + pdx * (float)(12 + depth * 34);
            int size = 5 + depth * 3;
            g.blit(lapis, (int)x, (int)y, (int)x + size, (int)y + size, 0.0f, 1.0f, 0.0f, 1.0f);
        }
        int vig = -2013265920;
        int band = Math.max(24, this.width / 10);
        g.fillGradient(0, 0, band, this.height, vig, 0);
        g.fillGradient(this.width - band, 0, this.width, this.height, 0, vig);
        g.fillGradient(0, 0, this.width, band, vig, 0);
        g.fillGradient(0, this.height - band, this.width, this.height, 0, vig);
    }

    private void drawSheen(GuiGraphicsExtractor g, int px, int py, int right, int bottom, float t, float pdx) {
        g.enableScissor(px, py, right, bottom);
        float travel = right - px + 80;
        int sx = px - 40 + (int)((t * 55.0f + pdx * 60.0f) % travel);
        for (int k = -12; k <= 12; ++k) {
            int a = (int)(26.0f * (1.0f - (float)Math.abs(k) / 12.0f));
            if (a <= 0) continue;
            int x = sx + k + (bottom - py) / 6;
            g.fill(x - (bottom - py) / 6, py, x, bottom, ModMenuScreen.alpha(-1, a));
        }
        g.disableScissor();
    }

    private void drawCornerBrackets(GuiGraphicsExtractor g, int px, int py, int right, int bottom, int color) {
        int n = 7;
        g.fill(px - 1, py - 1, px + n, py, color);
        g.fill(px - 1, py - 1, px, py + n, color);
        g.fill(right - n, py - 1, right + 1, py, color);
        g.fill(right, py - 1, right + 1, py + n, color);
        g.fill(px - 1, bottom, px + n, bottom + 1, color);
        g.fill(px - 1, bottom - n, px, bottom + 1, color);
        g.fill(right - n, bottom, right + 1, bottom + 1, color);
        g.fill(right, bottom - n, right + 1, bottom + 1, color);
    }

    private void drawWirePulse(GuiGraphicsExtractor g, float t, long now) {
        int x1 = this.contentLeft;
        int x0 = this.panelX + 10 + 62;
        if (x1 <= x0) {
            return;
        }
        int y = this.contentTop + this.selected * 32 + 13;
        y = Math.min(y, this.contentTop + BODY_H - 2);
        g.fill(x0, y, x1, y + 1, ModMenuScreen.alpha(this.accentPrimary, 40));
        boolean justSwitched = now - this.tabSwitchedAt < 350L;
        int dots = justSwitched ? 3 : 2;
        for (int d = 0; d < dots; ++d) {
            float phase = (t * 1.6f + (float)d / (float)dots) % 1.0f;
            int dx = x0 + (int)(phase * (float)(x1 - x0));
            int a = (int)(255.0f * (1.0f - phase) * (justSwitched ? 1.0f : 0.7f));
            g.fill(dx - 1, y - 1, dx + 2, y + 2, ModMenuScreen.alpha(this.accentPrimary, a));
            g.fill(dx, y, dx + 1, y + 1, ModMenuScreen.alpha(-1, a));
        }
    }

    private void drawStage(GuiGraphicsExtractor g, int mouseX, int mouseY, float t, float shimmer) {
        int sx = this.panelX + 326 + 6;
        if (sx + 96 > this.width - 4) {
            sx = this.panelX - 6 - 96;
        }
        if (sx < 4) {
            return;
        }
        int sTop = this.panelY;
        int sBot = this.panelY + PANEL_H;
        g.fill(sx + 3, sTop + 4, sx + 96 + 3, sBot + 4, 0x50000000);
        g.fillGradient(sx, sTop, sx + 96, sBot, -234222572, -184220149);
        g.outline(sx - 1, sTop - 1, 98, PANEL_H + 2, ModMenuScreen.alpha(this.accentSecondary, (int)(60.0f + 40.0f * shimmer)));
        g.outline(sx, sTop, 96, PANEL_H, this.accentSecondary);
        Font f = this.font;
        String head = "LIVE";
        g.centeredText(f, head, sx + 48, sTop + 10 - 1, ModMenuScreen.alpha(this.accentPrimary, 255));
        g.fillGradient(sx + 10, sTop + 10 + 10, sx + 96 - 10, sTop + 10 + 11, this.accentSecondary, this.accentPrimary);
        int gcx = sx + 48;
        int gcy = sTop + PANEL_H / 2 + 8;
        for (int r = 34; r > 0; r -= 6) {
            int a = (int)(28.0f * (1.0f - (float)r / 40.0f) * (0.7f + 0.3f * shimmer));
            g.fill(gcx - r, gcy - r, gcx + r, gcy + r, ModMenuScreen.alpha(this.accentSecondary, Math.max(0, a)));
        }
        int innerX0 = sx + 6;
        int innerY0 = sTop + 22 + 4;
        int innerX1 = sx + 96 - 6;
        int innerY1 = sBot - 22;
        int scale = Math.max(22, (innerY1 - innerY0) / 3);
        boolean drew = this.diorama.render(g, innerX0, innerY0, innerX1, innerY1, scale, mouseX, mouseY, t * 1.1f);
        if (!drew) {
            g.item(MenuDiorama.fallbackIcon(), gcx - 8, gcy - 8);
        }
        if (this.categories.length > 0) {
            String name = this.categories[Math.min(this.selected, this.categories.length - 1)].label();
            g.centeredText(f, name, sx + 48, sBot - 10 - 6, -787457);
        }
    }

    private void playUi(SoundEvent sound, float pitch, float volume) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() != null) {
            mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)sound, (float)pitch, (float)volume));
        }
    }

    private void drawCursorTrail(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int i;
        if (!this.trailSeeded) {
            for (i = 0; i < 10; ++i) {
                this.trailX[i] = mouseX;
                this.trailY[i] = mouseY;
            }
            this.trailSeeded = true;
        }
        for (i = 9; i > 0; --i) {
            this.trailX[i] = this.trailX[i - 1];
            this.trailY[i] = this.trailY[i - 1];
        }
        this.trailX[0] = mouseX;
        this.trailY[0] = mouseY;
        for (i = 9; i >= 1; --i) {
            float f = 1.0f - (float)i / 10.0f;
            int size = Math.max(1, (int)(1.0f + f * 3.0f));
            int a = (int)(f * 150.0f);
            int x = this.trailX[i];
            int y = this.trailY[i];
            g.fill(x - size, y - size, x + size, y + size, ModMenuScreen.alpha(this.accentPrimary, a));
        }
    }

    private static void drawSparkle(GuiGraphicsExtractor g, int cx, int cy, int color) {
        g.fill(cx, cy - 3, cx + 1, cy + 4, color);
        g.fill(cx - 3, cy, cx + 4, cy + 1, color);
    }

    private static int alpha(int argb, int a) {
        return Mth.clamp((int)a, (int)0, (int)255) << 24 | argb & 0xFFFFFF;
    }

    private static int lerpColor(int a, int b, float t) {
        t = Mth.clamp((float)t, (float)0.0f, (float)1.0f);
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        int r = (int)((float)ar + (float)(br - ar) * t);
        int gg = (int)((float)ag + (float)(bg - ag) * t);
        int bl = (int)((float)ab + (float)(bb - ab) * t);
        return 0xFF000000 | r << 16 | gg << 8 | bl;
    }

    private static String fit(Font f, String s, int width) {
        if (f.width(s) <= width) {
            return s;
        }
        int ellipsisW = f.width("...");
        return f.plainSubstrByWidth(s, Math.max(0, width - ellipsisW)) + "...";
    }

    private static void drawChip(GuiGraphicsExtractor g, Font f, int rightX, int centerY, int w, String label, int color) {
        int h = 13;
        int x = rightX - w;
        int y = centerY - h / 2;
        g.fill(x, y, x + w, y + h, ModMenuScreen.alpha(color, 35));
        g.outline(x, y, w, h, ModMenuScreen.alpha(color, 210));
        g.centeredText(f, label, x + w / 2, y + 2, color);
    }

    private void drawToggle(GuiGraphicsExtractor g, int rightX, int centerY, float on) {
        int w = 28;
        int h = 12;
        int x = rightX - w;
        int y = centerY - h / 2;
        boolean onish = on > 0.5f;
        g.fill(x, y, x + w, y + h, ModMenuScreen.lerpColor(-14013387, -15176093, on));
        g.outline(x, y, w, h, onish ? this.accentPrimary : 0x40FFFFFF);
        int knob = 8;
        int kxOff = x + 2;
        int kxOn = x + w - 2 - knob;
        int kx = (int)((float)kxOff + (float)(kxOn - kxOff) * on);
        g.fill(kx, y + 2, kx + knob, y + h - 2, onish ? this.accentPrimary : -5327938);
    }

    private void drawNavStatus(GuiGraphicsExtractor g, Font f, int rightX, int centerY, boolean on) {
        int stateW = 30;
        int openW = 34;
        int gap = 4;
        int h = 13;
        int stateX = rightX - stateW;
        int y = centerY - h / 2;
        ModMenuScreen.drawChip(g, f, stateX - gap, centerY, openW, "OPEN", this.accentPrimary);
        int stateColor = on ? this.accentPrimary : -9011570;
        g.fill(stateX, y, stateX + stateW, y + h, on ? ModMenuScreen.alpha(this.accentPrimary, 60) : 0x2AFFFFFF);
        g.outline(stateX, y, stateW, h, on ? this.accentPrimary : 0x40FFFFFF);
        g.centeredText(f, on ? "ON" : "OFF", stateX + stateW / 2, y + 2, stateColor);
    }

    private record CategoryDef(String label, ItemStack icon, List<RowSpec> rows) {
    }

    private record RowSpec(Kind kind, String title, String desc, BooleanSupplier state, Runnable toggle, Supplier<Screen> open) {
        int controlWidth() {
            return switch (this.kind.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> 28;
                case 1 -> 68;
                case 2 -> 40;
                case 3 -> 34;
                case 4 -> 0;
            };
        }
    }

    private static enum Kind {
        TOGGLE,
        NAV_STATUS,
        NAV,
        ACTION,
        INFO;

    }

    private final class SidebarButton
    extends AbstractButton {
        private final int index;
        private final CategoryDef cat;
        private boolean wasHot;
        final /* synthetic */ ModMenuScreen this$0;

        SidebarButton(ModMenuScreen modMenuScreen, int index, CategoryDef cat) {
            ModMenuScreen modMenuScreen2 = modMenuScreen;
            Objects.requireNonNull(modMenuScreen2);
            this.this$0 = modMenuScreen2;
            super(0, 0, 62, 27, (Component)Component.literal((String)cat.label()));
            this.wasHot = false;
            this.index = index;
            this.cat = cat;
        }

        public void onPress(InputWithModifiers input) {
            this.this$0.selectCategory(this.index);
        }

        protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
            int x = this.getX();
            int y = this.getY();
            int w = this.getWidth();
            int h = this.getHeight();
            boolean sel = this.index == this.this$0.selected;
            boolean hot = this.isHoveredOrFocused();
            if (hot && !this.wasHot) {
                this.this$0.playUi((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 1.6f, 0.2f);
            }
            this.wasHot = hot;
            if (sel) {
                g.fill(x, y, x + w, y + h, this.this$0.tabSelected);
                g.outline(x, y, w, h, this.this$0.accentPrimary);
            } else if (hot) {
                g.fill(x, y, x + w, y + h, 0x18FFFFFF);
            }
            int iconSize = 16;
            g.item(this.cat.icon(), x + (w - iconSize) / 2, y + 3);
            Font f = Minecraft.getInstance().font;
            g.centeredText(f, this.cat.label(), x + w / 2, y + 3 + iconSize + 2, sel ? this.this$0.accentPrimary : (hot ? -787457 : -9011570));
        }

        protected void updateWidgetNarration(NarrationElementOutput out) {
            this.defaultButtonNarrationText(out);
        }
    }

    private static final class MenuActionButton
    extends AbstractButton {
        private final int color;
        private final Runnable action;

        MenuActionButton(String label, int w, int color, Runnable action) {
            super(0, 0, w, 18, (Component)Component.literal((String)label));
            this.color = color;
            this.action = action;
        }

        public void onPress(InputWithModifiers input) {
            this.action.run();
        }

        protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
            int x = this.getX();
            int y = this.getY();
            int w = this.getWidth();
            int h = this.getHeight();
            boolean hot = this.isHoveredOrFocused();
            Font f = Minecraft.getInstance().font;
            g.fill(x, y, x + w, y + h, hot ? ModMenuScreen.alpha(this.color, 80) : ModMenuScreen.alpha(this.color, 28));
            g.outline(x, y, w, h, hot ? this.color : ModMenuScreen.alpha(this.color, 170));
            g.centeredText(f, this.getMessage().getString(), x + w / 2, y + (h - 8) / 2, hot ? -787457 : this.color);
        }

        protected void updateWidgetNarration(NarrationElementOutput out) {
            this.defaultButtonNarrationText(out);
        }
    }

    private record InfoLine(String title, String desc, int x, int y) {
    }

    private final class RowButton
    extends AbstractButton {
        private final RowSpec spec;
        private float toggleAnim;
        private long lastAnimMs;
        private boolean wasHot;
        final /* synthetic */ ModMenuScreen this$0;

        RowButton(ModMenuScreen modMenuScreen, RowSpec spec) {
            ModMenuScreen modMenuScreen2 = modMenuScreen;
            Objects.requireNonNull(modMenuScreen2);
            this.this$0 = modMenuScreen2;
            super(0, 0, 236, 34, (Component)Component.literal((String)spec.title()));
            this.toggleAnim = -1.0f;
            this.lastAnimMs = System.currentTimeMillis();
            this.wasHot = false;
            this.spec = spec;
        }

        public void onPress(InputWithModifiers input) {
            switch (this.spec.kind().ordinal()) {
                case 0: 
                case 3: {
                    this.spec.toggle().run();
                    break;
                }
                case 1: 
                case 2: {
                    Minecraft.getInstance().gui.setScreen(this.spec.open().get());
                    break;
                }
            }
        }

        protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
            int x = this.getX();
            int y = this.getY();
            int w = this.getWidth();
            int h = this.getHeight();
            int r = x + w;
            int b = y + h;
            boolean hot = this.isHoveredOrFocused();
            if (hot && !this.wasHot) {
                this.this$0.playUi((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 1.8f, 0.2f);
            }
            this.wasHot = hot;
            Font f = Minecraft.getInstance().font;
            if (hot) {
                g.fillGradient(x, y, r, b, this.this$0.rowHotTop, this.this$0.rowHotBot);
                g.outline(x, y, w, h, -1996488705);
            } else {
                g.fill(x, y, r, b, 1074927136);
                g.outline(x, y, w, h, 0x1EFFFFFF);
            }
            int controlW = this.spec.controlWidth();
            int textW = w - 16 - controlW - 6;
            int tx = x + 8;
            g.text(f, this.spec.title(), tx, y + 6, -787457);
            g.text(f, ModMenuScreen.fit(f, this.spec.desc(), Math.max(10, textW)), tx, y + 18, hot ? -1186054 : -9011570);
            int cx = r - 8;
            int cy = y + h / 2;
            switch (this.spec.kind().ordinal()) {
                case 0: {
                    float target;
                    float f2 = target = this.spec.state().getAsBoolean() ? 1.0f : 0.0f;
                    if (this.toggleAnim < 0.0f) {
                        this.toggleAnim = target;
                    }
                    long nowMs = System.currentTimeMillis();
                    float dt = Math.min(0.1f, (float)(nowMs - this.lastAnimMs) / 1000.0f);
                    this.lastAnimMs = nowMs;
                    this.toggleAnim += (target - this.toggleAnim) * Math.min(1.0f, dt * 12.0f);
                    this.this$0.drawToggle(g, cx, cy, this.toggleAnim);
                    break;
                }
                case 1: {
                    this.this$0.drawNavStatus(g, f, cx, cy, this.spec.state().getAsBoolean());
                    break;
                }
                case 2: {
                    ModMenuScreen.drawChip(g, f, cx, cy, controlW, "OPEN", this.this$0.accentPrimary);
                    break;
                }
                case 3: {
                    ModMenuScreen.drawChip(g, f, cx, cy, controlW, "RUN", this.this$0.accentSecondary);
                    break;
                }
            }
        }

        protected void updateWidgetNarration(NarrationElementOutput out) {
            this.defaultButtonNarrationText(out);
        }
    }
}

