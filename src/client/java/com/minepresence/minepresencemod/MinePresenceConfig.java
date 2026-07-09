package com.minepresence.minepresencemod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinePresenceConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int CONFIG_VERSION = 5;

    public int version = CONFIG_VERSION;
    public boolean enabled = true;
    public boolean borderlessWindow = true;
    public boolean discordGuard = true;
    public boolean framePacing = false;
    public int targetFps = 0;
    public int safeInsetPixels = 1;
    public boolean autoReapply = true;
    public int verifyIntervalSeconds = 5;
    public boolean cooperateWithWindowMods = true;
    public boolean diagnostics = false;
    public boolean releaseMouseOnFocusLoss = true;
    public boolean restoreDecorationsWhenDisabled = true;

    public static MinePresenceConfig load() {
        Path path = configPath();
        if (!Files.exists(path)) {
            return new MinePresenceConfig();
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            MinePresenceConfig config = GSON.fromJson(reader, MinePresenceConfig.class);
            if (config == null) {
                return new MinePresenceConfig();
            }
            config.sanitize();
            return config;
        } catch (Exception exception) {
            MinePresenceMod.LOGGER.warn("Could not read MinePresence config, using defaults", exception);
            return new MinePresenceConfig();
        }
    }

    public void save() {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception exception) {
            MinePresenceMod.LOGGER.warn("Could not save MinePresence config", exception);
        }
    }

    public void sanitize() {
        if (version < 2) {
            framePacing = false;
            targetFps = 0;
        }

        version = CONFIG_VERSION;
        if (targetFps != 0) {
            targetFps = clamp(targetFps, 30, 360);
        }
        safeInsetPixels = clamp(safeInsetPixels, 0, 8);
        verifyIntervalSeconds = clamp(verifyIntervalSeconds, 1, 60);
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("minepresencemod.json");
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
