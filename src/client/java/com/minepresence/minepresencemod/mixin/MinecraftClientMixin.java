package com.minepresence.minepresencemod.mixin;

import com.minepresence.minepresencemod.MinePresenceFramePacer;
import com.minepresence.minepresencemod.MinePresenceWindowController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftClientMixin {
    @Inject(method = "tick()V", at = @At("TAIL"), remap = false)
    private void minepresence$tick(CallbackInfo ci) {
        MinePresenceWindowController.tick((Minecraft) (Object) this);
    }

    @Inject(method = "renderFrame(Z)V", at = @At("RETURN"), require = 0, remap = false)
    private void minepresence$afterRender(boolean tick, CallbackInfo ci) {
        MinePresenceFramePacer.afterRender((Minecraft) (Object) this);
    }
}
