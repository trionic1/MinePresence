package com.minepresence.minepresencemod;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public final class MinePresenceWindowController {
    private static final long DIAGNOSTIC_INTERVAL_NANOS = 10_000_000_000L;
    // Mods that turn Minecraft's fullscreen into a borderless window themselves
    // (e.g. Cubes Without Borders). In cooperative mode MinePresence lets them
    // own the borderless conversion and only enforces the capture-safe inset on
    // top, instead of fighting them for control of the window.
    private static final String[] WINDOW_MOD_IDS = {
            "cwb",
            "cubes-without-borders",
            "borderless-mining",
            "borderlessmining",
            "fullscreenwindowed"
    };

    private static long lastHandle;
    private static int lastX;
    private static int lastY;
    private static int lastWidth;
    private static int lastHeight;
    private static int ticksUntilVerify;
    private static int applyCount;
    private static long lastDiagnosticNanos;
    private static String lastApplyReason = "never";
    private static boolean applied;
    private static boolean wasFocused = true;
    private static boolean wasFullscreen;
    private static String detectedWindowMod;
    private static boolean loggedCoexist;

    private MinePresenceWindowController() {
    }

    static void detectWindowMods() {
        for (String modId : WINDOW_MOD_IDS) {
            var container = FabricLoader.getInstance().getModContainer(modId);
            if (container.isPresent()) {
                detectedWindowMod = container.get().getMetadata().getName();
                MinePresenceMod.LOGGER.info(
                        "Detected borderless mod '{}'; MinePresence will cooperate with it and enforce the safe inset on top.",
                        detectedWindowMod);
                return;
            }
        }
    }

    public static void onConfigChanged() {
        lastHandle = 0L;
        ticksUntilVerify = 0;
        applied = false;
        wasFullscreen = false;
        loggedCoexist = false;
        MinePresenceFramePacer.reset();
    }

    public static void tick(Minecraft client) {
        if (client == null) {
            return;
        }

        Window window = client.getWindow();
        if (window == null) {
            return;
        }

        MinePresenceConfig config = MinePresenceMod.CONFIG;
        boolean focusRegained = updateFocus(client, config);

        if (!config.enabled || !config.borderlessWindow) {
            restoreIfNeeded(client, config);
            return;
        }

        if (config.cooperateWithWindowMods && detectedWindowMod != null) {
            cooperativeTick(window, config, focusRegained);
        } else {
            authoritativeTick(client, window, config);
        }

        logDiagnostics(config);
    }

    public static void forceApply(Minecraft client) {
        onConfigChanged();
        if (client != null) {
            tick(client);
        }
    }

    // MinePresence owns the window outright: convert any fullscreen to borderless
    // windowed and enforce the inset. Used when no other borderless mod is present.
    private static void authoritativeTick(Minecraft client, Window window, MinePresenceConfig config) {
        boolean fullscreen = window.isFullscreen();

        if (!applied || window.handle() != lastHandle) {
            applyBorderless(client, config, "initial apply");
            resetVerifyTimer(config);
        } else if (fullscreen) {
            applyBorderless(client, config, "fullscreen override");
            resetVerifyTimer(config);
        } else if (wasFullscreen) {
            applyBorderless(client, config, "fullscreen exit");
            resetVerifyTimer(config);
        } else if (config.autoReapply && --ticksUntilVerify <= 0) {
            if (needsReapply(window)) {
                applyBorderless(client, config, "verification");
            }
            resetVerifyTimer(config);
        }

        wasFullscreen = window.isFullscreen();
    }

    // Another mod (e.g. Cubes Without Borders) owns the borderless conversion.
    // We do not touch Minecraft's fullscreen flag; we only nudge the window in by
    // the safe inset so Windows keeps compositing it (and Discord capture keeps
    // working). This is the single lever that differs between "capture works" and
    // "capture freezes" once the window is already borderless.
    private static void cooperativeTick(Window window, MinePresenceConfig config, boolean focusRegained) {
        long handle = window.handle();
        if (handle == 0L || GLFW.glfwWindowShouldClose(handle)) {
            return;
        }

        // The other mod hasn't converted to borderless yet (still real fullscreen,
        // or a genuine exclusive-fullscreen window). Wait for it rather than fight.
        if (GLFW.glfwGetWindowMonitor(handle) != 0L) {
            applied = false;
            return;
        }

        int inset = config.discordGuard ? config.safeInsetPixels : 0;
        if (inset <= 0) {
            // No inset requested: nothing to enforce, let the other mod's window stand.
            applied = false;
            return;
        }

        boolean verifyDue = config.autoReapply && --ticksUntilVerify <= 0;
        if (!applied || focusRegained || verifyDue) {
            if (!applied || needsInset(handle, inset)) {
                applyInset(handle, inset);
            }
            resetVerifyTimer(config);
        }
    }

    private static void resetVerifyTimer(MinePresenceConfig config) {
        ticksUntilVerify = config.verifyIntervalSeconds * 20;
    }

    private static boolean updateFocus(Minecraft client, MinePresenceConfig config) {
        boolean focused = client.isWindowActive();
        boolean lostFocus = wasFocused && !focused;
        boolean regainedFocus = !wasFocused && focused;
        wasFocused = focused;

        if (lostFocus && config.enabled && config.releaseMouseOnFocusLoss) {
            try {
                client.mouseHandler.releaseMouse();
            } catch (Exception exception) {
                MinePresenceMod.LOGGER.debug("Could not unlock cursor after focus loss", exception);
            }
        }

        return regainedFocus;
    }

    // Cheap check against the last applied geometry: three GLFW queries, no
    // monitor enumeration. A full monitor lookup only happens when this
    // reports a mismatch and applyBorderless runs.
    private static boolean needsReapply(Window window) {
        long handle = window.handle();
        if (handle == 0L) {
            return false;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer windowX = stack.mallocInt(1);
            IntBuffer windowY = stack.mallocInt(1);
            IntBuffer windowWidth = stack.mallocInt(1);
            IntBuffer windowHeight = stack.mallocInt(1);
            GLFW.glfwGetWindowPos(handle, windowX, windowY);
            GLFW.glfwGetWindowSize(handle, windowWidth, windowHeight);

            boolean decorated = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_DECORATED) == GLFW.GLFW_TRUE;
            return decorated
                    || windowX.get(0) != lastX
                    || windowY.get(0) != lastY
                    || windowWidth.get(0) != lastWidth
                    || windowHeight.get(0) != lastHeight;
        } catch (Exception exception) {
            MinePresenceMod.LOGGER.debug("Could not verify window state", exception);
            return false;
        }
    }

    // In cooperative mode the target depends on the monitor the other mod placed
    // the window on, so this recomputes bounds. Returns true when the window has
    // drifted back toward edge-to-edge (which is what re-triggers capture freezes).
    private static boolean needsInset(long handle, int inset) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer windowX = stack.mallocInt(1);
            IntBuffer windowY = stack.mallocInt(1);
            IntBuffer windowWidth = stack.mallocInt(1);
            IntBuffer windowHeight = stack.mallocInt(1);
            GLFW.glfwGetWindowPos(handle, windowX, windowY);
            GLFW.glfwGetWindowSize(handle, windowWidth, windowHeight);

            MonitorBounds monitor = findBestMonitor(handle);
            int targetX = monitor.x + inset;
            int targetY = monitor.y + inset;
            int targetWidth = Math.max(1, monitor.width - inset * 2);
            int targetHeight = Math.max(1, monitor.height - inset * 2);

            return windowX.get(0) != targetX
                    || windowY.get(0) != targetY
                    || windowWidth.get(0) != targetWidth
                    || windowHeight.get(0) != targetHeight;
        } catch (Exception exception) {
            MinePresenceMod.LOGGER.debug("Could not verify cooperative window state", exception);
            return false;
        }
    }

    private static void applyInset(long handle, int inset) {
        try {
            MonitorBounds monitor = findBestMonitor(handle);
            int targetX = monitor.x + inset;
            int targetY = monitor.y + inset;
            int targetWidth = Math.max(1, monitor.width - inset * 2);
            int targetHeight = Math.max(1, monitor.height - inset * 2);

            GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowMonitor(handle, 0L, targetX, targetY, targetWidth, targetHeight, GLFW.GLFW_DONT_CARE);

            lastHandle = handle;
            lastX = targetX;
            lastY = targetY;
            lastWidth = targetWidth;
            lastHeight = targetHeight;
            applied = true;
            applyCount++;
            lastApplyReason = "cooperative inset";

            if (!loggedCoexist) {
                MinePresenceMod.LOGGER.info(
                        "Cooperating with '{}': enforcing {}px safe inset on its borderless window.",
                        detectedWindowMod, inset);
                loggedCoexist = true;
            }
        } catch (Exception exception) {
            MinePresenceMod.LOGGER.warn("Could not apply cooperative inset", exception);
        }
    }

    private static void applyBorderless(Minecraft client, MinePresenceConfig config, String reason) {
        Window window = client.getWindow();
        long handle = window.handle();
        if (handle == 0L || GLFW.glfwWindowShouldClose(handle)) {
            return;
        }

        try {
            if (window.isFullscreen()) {
                window.toggleFullScreen();
            }

            MonitorBounds monitor = findBestMonitor(handle);
            int inset = config.discordGuard ? config.safeInsetPixels : 0;
            int targetX = monitor.x + inset;
            int targetY = monitor.y + inset;
            int targetWidth = Math.max(1, monitor.width - inset * 2);
            int targetHeight = Math.max(1, monitor.height - inset * 2);

            GLFW.glfwRestoreWindow(handle);
            GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowMonitor(handle, 0L, targetX, targetY, targetWidth, targetHeight, GLFW.GLFW_DONT_CARE);

            lastHandle = handle;
            lastX = targetX;
            lastY = targetY;
            lastWidth = targetWidth;
            lastHeight = targetHeight;
            applied = true;
            applyCount++;
            lastApplyReason = reason;
        } catch (Exception exception) {
            MinePresenceMod.LOGGER.warn("Could not apply borderless window mode", exception);
        }
    }

    private static void restoreIfNeeded(Minecraft client, MinePresenceConfig config) {
        if (!applied) {
            return;
        }

        if (!config.restoreDecorationsWhenDisabled) {
            applied = false;
            return;
        }

        Window window = client.getWindow();
        long handle = window.handle();
        if (handle == 0L) {
            return;
        }

        try {
            GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        } catch (Exception exception) {
            MinePresenceMod.LOGGER.debug("Could not restore window decorations", exception);
        } finally {
            applied = false;
            lastHandle = 0L;
        }
    }

    private static void logDiagnostics(MinePresenceConfig config) {
        if (!config.diagnostics) {
            return;
        }

        long now = System.nanoTime();
        if (now - lastDiagnosticNanos < DIAGNOSTIC_INTERVAL_NANOS) {
            return;
        }

        lastDiagnosticNanos = now;
        boolean cooperating = config.cooperateWithWindowMods && detectedWindowMod != null;
        MinePresenceMod.LOGGER.info(
                "MinePresence diagnostics: mode={}, applied={}, lastReason={}, target={}x{}+{}+{}, fpsLimit={}, discordGuard={}, inset={}px, windowMod={}",
                cooperating ? "cooperative" : "authoritative",
                applyCount,
                lastApplyReason,
                lastWidth,
                lastHeight,
                lastX,
                lastY,
                config.framePacing && config.targetFps != 0 ? config.targetFps : "unlimited",
                config.discordGuard,
                config.discordGuard ? config.safeInsetPixels : 0,
                detectedWindowMod == null ? "none" : detectedWindowMod
        );
    }

    private static MonitorBounds findBestMonitor(long handle) {
        long fullscreenMonitor = GLFW.glfwGetWindowMonitor(handle);
        if (fullscreenMonitor != 0L) {
            return getMonitorBounds(fullscreenMonitor);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer windowX = stack.mallocInt(1);
            IntBuffer windowY = stack.mallocInt(1);
            IntBuffer windowWidth = stack.mallocInt(1);
            IntBuffer windowHeight = stack.mallocInt(1);
            GLFW.glfwGetWindowPos(handle, windowX, windowY);
            GLFW.glfwGetWindowSize(handle, windowWidth, windowHeight);

            PointerBuffer monitors = GLFW.glfwGetMonitors();
            if (monitors == null || monitors.limit() == 0) {
                return getMonitorBounds(GLFW.glfwGetPrimaryMonitor());
            }

            long bestMonitor = GLFW.glfwGetPrimaryMonitor();
            int bestArea = -1;
            for (int index = 0; index < monitors.limit(); index++) {
                long monitor = monitors.get(index);
                MonitorBounds bounds = getMonitorBounds(monitor);
                int area = overlapArea(
                        windowX.get(0),
                        windowY.get(0),
                        windowWidth.get(0),
                        windowHeight.get(0),
                        bounds
                );

                if (area > bestArea) {
                    bestArea = area;
                    bestMonitor = monitor;
                }
            }

            return getMonitorBounds(bestMonitor);
        }
    }

    private static MonitorBounds getMonitorBounds(long monitor) {
        if (monitor == 0L) {
            monitor = GLFW.glfwGetPrimaryMonitor();
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer monitorX = stack.mallocInt(1);
            IntBuffer monitorY = stack.mallocInt(1);
            GLFW.glfwGetMonitorPos(monitor, monitorX, monitorY);

            GLFWVidMode mode = GLFW.glfwGetVideoMode(monitor);
            if (mode == null) {
                return new MonitorBounds(0, 0, 854, 480);
            }

            return new MonitorBounds(monitorX.get(0), monitorY.get(0), mode.width(), mode.height());
        }
    }

    private static int overlapArea(int x, int y, int width, int height, MonitorBounds monitor) {
        int left = Math.max(x, monitor.x);
        int top = Math.max(y, monitor.y);
        int right = Math.min(x + width, monitor.x + monitor.width);
        int bottom = Math.min(y + height, monitor.y + monitor.height);
        return Math.max(0, right - left) * Math.max(0, bottom - top);
    }

    private record MonitorBounds(int x, int y, int width, int height) {
    }
}
