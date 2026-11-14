/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ModInitializer
 *  net.minecraft.resources.Identifier
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package name.modid;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class R3dMenu
implements ModInitializer {
    public static final String MOD_ID = "r3dmenu";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"r3dmenu");

    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath((String)MOD_ID, (String)path);
    }

    public void onMenuTick() {
    }
}

