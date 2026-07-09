package com.minepresence.minepresencemod;

import net.minecraft.client.Minecraft;

import java.util.concurrent.locks.LockSupport;

public final class MinePresenceFramePacer {
    private static long nextFrameNanos;
    private static long frameNanos;
    private static int lastTargetFps;

    private MinePresenceFramePacer() {
    }

    public static void afterRender(Minecraft client) {
        MinePresenceConfig config = MinePresenceMod.CONFIG;
        if (client == null || !config.enabled || !config.discordGuard || !config.framePacing || config.targetFps <= 0) {
            if (nextFrameNanos != 0L) {
                reset();
            }
            return;
        }

        int targetFps = config.targetFps;
        long now = System.nanoTime();

        if (targetFps != lastTargetFps) {
            lastTargetFps = targetFps;
            frameNanos = 1_000_000_000L / targetFps;
            nextFrameNanos = now + frameNanos;
            return;
        }

        // Resynchronize after long stalls (world load, screenshots, GC pauses)
        // instead of sprinting to catch up on missed frames.
        if (now > nextFrameNanos + frameNanos * 4L) {
            nextFrameNanos = now + frameNanos;
            return;
        }

        long waitNanos = nextFrameNanos - now;
        if (waitNanos > 2_000_000L) {
            LockSupport.parkNanos(waitNanos - 500_000L);
        } else if (waitNanos > 0L) {
            Thread.yield();
        }

        nextFrameNanos += frameNanos;
    }

    public static void reset() {
        nextFrameNanos = 0L;
        frameNanos = 0L;
        lastTargetFps = 0;
    }
}
