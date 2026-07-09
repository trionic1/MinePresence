package com.minepresence.minepresencemod.mixin;

import com.minepresence.minepresencemod.MinePresenceOptionsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OptionsScreen.class, remap = false)
public abstract class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("TAIL"), remap = false)
    private void minepresence$addOptionsButton(CallbackInfo ci) {
        int buttonWidth = Math.min(132, Math.max(96, this.width / 4));
        int x = this.width - buttonWidth - 6;
        int y = 6;

        this.addRenderableWidget(Button.builder(Component.literal("MinePresence"), button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreenAndShow(new MinePresenceOptionsScreen(this));
                    }
                })
                .bounds(x, y, buttonWidth, 20)
                .tooltip(Tooltip.create(Component.literal("Borderless and Discord stream settings.")))
                .build());
    }
}
