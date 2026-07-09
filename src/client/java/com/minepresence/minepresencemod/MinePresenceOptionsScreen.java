package com.minepresence.minepresencemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MinePresenceOptionsScreen extends Screen {
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 4;

    private final Screen parent;

    public MinePresenceOptionsScreen(Screen parent) {
        super(Component.literal("MinePresence"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(190, (this.width - GAP * 2) / 2 - GAP);
        int firstY = 42;

        Setting[] settings = Setting.values();
        int leftX = this.width / 2 - buttonWidth - GAP;
        int rightX = this.width / 2 + GAP;
        for (int index = 0; index < settings.length; index++) {
            int x = index % 2 == 0 ? leftX : rightX;
            int y = firstY + (index / 2) * 24;
            addSettingsButton(x, y, buttonWidth, settings[index]);
        }

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        MinePresenceMod.saveConfig();
        MinePresenceWindowController.forceApply(Minecraft.getInstance());
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }

    private void addSettingsButton(int x, int y, int width, Setting setting) {
        Button button = Button.builder(setting.message(), pressed -> {
                    setting.apply();
                    MinePresenceMod.saveConfig();
                    MinePresenceWindowController.forceApply(Minecraft.getInstance());
                    pressed.setMessage(setting.message());
                })
                .bounds(x, y, width, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(setting.tooltip()))
                .build();
        this.addRenderableWidget(button);
    }

    private enum Setting {
        ENABLED {
            @Override
            Component message() {
                return toggle("Enabled", MinePresenceMod.CONFIG.enabled);
            }

            @Override
            Component tooltip() {
                return Component.literal("Turns all MinePresence window and stream-share behavior on or off.");
            }

            @Override
            void apply() {
                MinePresenceMod.CONFIG.enabled = !MinePresenceMod.CONFIG.enabled;
            }
        },
        BORDERLESS {
            @Override
            Component message() {
                return toggle("Borderless", MinePresenceMod.CONFIG.borderlessWindow);
            }

            @Override
            Component tooltip() {
                return Component.literal("Uses a decorated=false GLFW window instead of native fullscreen.");
            }

            @Override
            void apply() {
                MinePresenceMod.CONFIG.borderlessWindow = !MinePresenceMod.CONFIG.borderlessWindow;
            }
        },
        DISCORD_GUARD {
            @Override
            Component message() {
                return toggle("Discord Guard", MinePresenceMod.CONFIG.discordGuard);
            }

            @Override
            Component tooltip() {
                return Component.literal("Keeps desktop capture DWM-friendly while Minecraft is focused.");
            }

            @Override
            void apply() {
                MinePresenceMod.CONFIG.discordGuard = !MinePresenceMod.CONFIG.discordGuard;
            }
        },
        SAFE_INSET {
            private final int[] values = new int[]{0, 1, 2, 4, 8};

            @Override
            Component message() {
                return Component.literal("Safe Inset: " + MinePresenceMod.CONFIG.safeInsetPixels + " px");
            }

            @Override
            Component tooltip() {
                return Component.literal("0 is edge-to-edge. 1 px usually avoids fullscreen capture paths.");
            }

            @Override
            void apply() {
                MinePresenceMod.CONFIG.safeInsetPixels = nextValue(values, MinePresenceMod.CONFIG.safeInsetPixels);
            }
        },
        FRAME_PACING {
            @Override
            Component message() {
                return toggle("FPS Limit", MinePresenceMod.CONFIG.framePacing);
            }

            @Override
            Component tooltip() {
                return Component.literal("Optional cap/yield mode. Leave off for unlimited FPS.");
            }

            @Override
            void apply() {
                MinePresenceMod.CONFIG.framePacing = !MinePresenceMod.CONFIG.framePacing;
            }
        },
        TARGET_FPS {
            private final int[] values = new int[]{0, 30, 60, 72, 120, 144, 240};

            @Override
            Component message() {
                int fps = MinePresenceMod.CONFIG.targetFps;
                return Component.literal("FPS Cap: " + (fps == 0 ? "Unlimited" : fps));
            }

            @Override
            Component tooltip() {
                return Component.literal("Render cap used only while FPS Limit is on. Unlimited applies no cap.");
            }

            @Override
            void apply() {
                MinePresenceMod.CONFIG.targetFps = nextValue(values, MinePresenceMod.CONFIG.targetFps);
            }
        },
        AUTO_REAPPLY {
            @Override
            Component message() {
                return toggle("Auto Reapply", MinePresenceMod.CONFIG.autoReapply);
            }

            @Override
            Component tooltip() {
                return Component.literal("Periodically verifies borderless mode and repairs it only if needed.");
            }

            @Override
            void apply() {
                MinePresenceMod.CONFIG.autoReapply = !MinePresenceMod.CONFIG.autoReapply;
            }
        },
        VERIFY_INTERVAL {
            private final int[] values = new int[]{1, 2, 5, 10, 30, 60};

            @Override
            Component message() {
                return Component.literal("Verify Every: " + MinePresenceMod.CONFIG.verifyIntervalSeconds + " s");
            }

            @Override
            Component tooltip() {
                return Component.literal("How often Auto Reapply checks the window. Longer is cheaper.");
            }

            @Override
            void apply() {
                MinePresenceMod.CONFIG.verifyIntervalSeconds =
                        nextValue(values, MinePresenceMod.CONFIG.verifyIntervalSeconds);
            }
        },
        COOPERATE_WITH_WINDOW_MODS {
            @Override
            Component message() {
                return toggle("Coexist Mode", MinePresenceMod.CONFIG.cooperateWithWindowMods);
            }

            @Override
            Component tooltip() {
                return Component.literal("With borderless mods like Cubes Without Borders: On enforces the safe inset on their window; Off makes MinePresence take over the window itself.");
            }

            @Override
            void apply() {
                MinePresenceMod.CONFIG.cooperateWithWindowMods = !MinePresenceMod.CONFIG.cooperateWithWindowMods;
            }
        },
        RESTORE_DECORATIONS {
            @Override
            Component message() {
                return toggle("Restore Borders", MinePresenceMod.CONFIG.restoreDecorationsWhenDisabled);
            }

            @Override
            Component tooltip() {
                return Component.literal("Gives the window its title bar back when MinePresence is turned off.");
            }

            @Override
            void apply() {
                MinePresenceMod.CONFIG.restoreDecorationsWhenDisabled = !MinePresenceMod.CONFIG.restoreDecorationsWhenDisabled;
            }
        },
        DIAGNOSTICS {
            @Override
            Component message() {
                return toggle("Diagnostics", MinePresenceMod.CONFIG.diagnostics);
            }

            @Override
            Component tooltip() {
                return Component.literal("Logs lightweight MinePresence status every 10 seconds.");
            }

            @Override
            void apply() {
                MinePresenceMod.CONFIG.diagnostics = !MinePresenceMod.CONFIG.diagnostics;
            }
        },
        RELEASE_MOUSE {
            @Override
            Component message() {
                return toggle("Release Mouse", MinePresenceMod.CONFIG.releaseMouseOnFocusLoss);
            }

            @Override
            Component tooltip() {
                return Component.literal("Unlocks the cursor immediately when Minecraft loses focus.");
            }

            @Override
            void apply() {
                MinePresenceMod.CONFIG.releaseMouseOnFocusLoss = !MinePresenceMod.CONFIG.releaseMouseOnFocusLoss;
            }
        };

        abstract Component message();

        abstract Component tooltip();

        abstract void apply();

        static Component toggle(String label, boolean enabled) {
            return Component.literal(label + ": " + (enabled ? "On" : "Off"));
        }

        static int nextValue(int[] values, int current) {
            for (int index = 0; index < values.length; index++) {
                if (values[index] == current) {
                    return values[(index + 1) % values.length];
                }
            }
            return values[0];
        }
    }
}
