package com.minepresence.minepresencemod;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinePresenceMod implements ClientModInitializer {
    public static final String MOD_ID = "minepresencemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MinePresenceConfig CONFIG = MinePresenceConfig.load();

    // Fabric Loader instantiates entrypoints reflectively and requires a public
    // no-arg constructor; a private one crashes the game during startup.
    public MinePresenceMod() {
    }

    @Override
    public void onInitializeClient() {
        CONFIG.sanitize();
        CONFIG.save();
        MinePresenceWindowController.detectWindowMods();
        MinePresenceWindowController.onConfigChanged();
        LOGGER.info("MinePresence initialized");
    }

    public static void saveConfig() {
        CONFIG.sanitize();
        CONFIG.save();
        MinePresenceWindowController.onConfigChanged();
    }
}
