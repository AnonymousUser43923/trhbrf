/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvent
 */
package name.modid.client.gui;

import java.math.BigDecimal;
import name.modid.client.FakeScoreboard;
import name.modid.client.ModSounds;
import name.modid.client.Money;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public class FakeChatScreen
extends ChatScreen {
    public FakeChatScreen() {
        super("", false);
    }

    public void handleChatInput(String message, boolean addToRecentChat) {
        Component line;
        String[] parts;
        boolean isFakePay;
        String text = this.normalizeChatMessage(message);
        if (text.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (addToRecentChat) {
            mc.gui.hud.getChat().addRecentChat(text);
        }
        boolean bl = isFakePay = (parts = text.split("\\s+")).length >= 3 && parts[0].equalsIgnoreCase("/pay");
        if (isFakePay) {
            String recipient = parts[1];
            String amount = parts[2];
            line = FakeChatScreen.fakePayLine(recipient, amount);
            mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)ModSounds.DING, (float)1.0f));
            FakeChatScreen.subtractFromFakeBalance(amount);
        } else if (mc.player != null) {
            String name = mc.player.getName().getString();
            line = Component.literal((String)("<" + name + "> " + text));
        } else {
            line = null;
        }
        if (line != null) {
            mc.gui.hud.getChat().addClientSystemMessage(line);
        }
        this.input.setValue("");
    }

    private static Component fakePayLine(String recipient, String amount) {
        return Component.literal((String)("You paid " + recipient + " ")).withStyle(ChatFormatting.WHITE).append((Component)Component.literal((String)"$").withStyle(ChatFormatting.GREEN)).append((Component)Component.literal((String)(" " + amount)).withStyle(ChatFormatting.WHITE));
    }

    private static void subtractFromFakeBalance(String amountPaid) {
        Money.parse(FakeScoreboard.amount).ifPresent(balance -> Money.parse(amountPaid).ifPresent(paid -> {
            BigDecimal left = balance.subtract((BigDecimal)paid);
            FakeScoreboard.amount = Money.format(left);
        }));
    }
}

