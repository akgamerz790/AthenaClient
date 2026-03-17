package dev.akgamerz_790.athenaclient.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderWorldStart(CallbackInfo ci) {
        // Hook point for future render-time performance work
        // e.g. frame timing, render pass profiling
    }

    @Inject(method = "renderWorld", at = @At("TAIL"))
    private void onRenderWorldEnd(CallbackInfo ci) {
        // Hook point for post-frame work
    }
}