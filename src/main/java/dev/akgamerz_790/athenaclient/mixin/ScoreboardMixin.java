package dev.akgamerz_790.athenaclient.mixin;

import dev.akgamerz_790.athenaclient.discord.ScoreboardParser;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class ScoreboardMixin {

    @Inject(
        method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
        at = @At("HEAD")
    )
    private void onRenderSidebar(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        if (objective != null) {
            ScoreboardParser.updateFromObjective(objective);
        }
    }
}