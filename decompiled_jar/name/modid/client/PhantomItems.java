/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomData
 */
package name.modid.client;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import name.modid.client.PhantomDropper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class PhantomItems {
    private static final String TAG_KEY = "r3dmenu_phantom";
    private static final Map<UUID, Entry> tracked = new LinkedHashMap<UUID, Entry>();

    private PhantomItems() {
    }

    public static void track(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        UUID id = PhantomItems.idOf(stack);
        if (id == null) {
            id = UUID.randomUUID();
            String value = id.toString();
            CustomData.update((DataComponentType)DataComponents.CUSTOM_DATA, (ItemStack)stack, tag -> tag.putString(TAG_KEY, value));
        }
        tracked.computeIfAbsent(id, key -> new Entry(stack.copy()));
    }

    public static boolean isPhantom(ItemStack stack) {
        return PhantomItems.idOf(stack) != null;
    }

    public static UUID idOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        String value = data.copyTag().getStringOr(TAG_KEY, "");
        if (value.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void forget(ItemStack stack) {
        UUID id = PhantomItems.idOf(stack);
        if (id != null) {
            tracked.remove(id);
        }
    }

    public static void forget(UUID id) {
        tracked.remove(id);
    }

    public static void untag(ItemStack stack) {
        if (!stack.isEmpty() && stack.has(DataComponents.CUSTOM_DATA)) {
            CustomData.update((DataComponentType)DataComponents.CUSTOM_DATA, (ItemStack)stack, tag -> tag.remove(TAG_KEY));
        }
    }

    public static void clear() {
        tracked.clear();
    }

    public static void reapply(Inventory inventory) {
        if (tracked.isEmpty()) {
            return;
        }
        HashSet<UUID> present = new HashSet<UUID>();
        for (int slot = 0; slot < inventory.getContainerSize(); ++slot) {
            UUID id = PhantomItems.idOf(inventory.getItem(slot));
            if (id == null) continue;
            present.add(id);
            Entry entry = tracked.get(id);
            if (entry == null) continue;
            entry.lastSlot = slot;
        }
        UUID carried = PhantomItems.idOf(inventory.player.containerMenu.getCarried());
        if (carried != null) {
            present.add(carried);
        }
        PhantomDropper.collectIds(present);
        for (Map.Entry<UUID, Entry> tracking : tracked.entrySet()) {
            if (present.contains(tracking.getKey())) continue;
            Entry entry = tracking.getValue();
            int slot = entry.lastSlot;
            if (slot < 0 || slot >= inventory.getContainerSize() || !inventory.getItem(slot).isEmpty()) {
                slot = inventory.getFreeSlot();
            }
            if (slot < 0) break;
            inventory.setItem(slot, entry.template.copy());
        }
    }

    private static final class Entry {
        final ItemStack template;
        int lastSlot = -1;

        Entry(ItemStack template) {
            this.template = template;
        }
    }
}

