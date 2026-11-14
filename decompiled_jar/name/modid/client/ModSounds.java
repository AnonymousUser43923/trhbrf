/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 */
package name.modid.client;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    public static final Identifier DING_ID = Identifier.fromNamespaceAndPath((String)"r3dmenu", (String)"ding");
    public static final SoundEvent DING = SoundEvent.createVariableRangeEvent((Identifier)DING_ID);
    public static final Identifier INTRO_ID = Identifier.fromNamespaceAndPath((String)"r3dmenu", (String)"intro");
    public static final SoundEvent INTRO = SoundEvent.createVariableRangeEvent((Identifier)INTRO_ID);

    private ModSounds() {
    }

    public static void register() {
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)DING_ID, (Object)DING);
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)INTRO_ID, (Object)INTRO);
    }
}

