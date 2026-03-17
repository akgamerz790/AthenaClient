package dev.akgamerz_790.athenaclient.command;

import dev.akgamerz_790.athenaclient.hud.HudConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public class CompassCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("cp")
                    .then(ClientCommandManager.literal("circle")
                        .executes(ctx -> {
                            HudConfig.compassShape = HudConfig.CompassShape.CIRCLE;
                            ctx.getSource().sendFeedback(Text.literal("§aCompass: §fCircle"));
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("c")
                        .executes(ctx -> {
                            HudConfig.compassShape = HudConfig.CompassShape.CIRCLE;
                            ctx.getSource().sendFeedback(Text.literal("§aCompass: §fCircle"));
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("square")
                        .executes(ctx -> {
                            HudConfig.compassShape = HudConfig.CompassShape.SQUARE;
                            ctx.getSource().sendFeedback(Text.literal("§aCompass: §fSquare"));
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("s")
                        .executes(ctx -> {
                            HudConfig.compassShape = HudConfig.CompassShape.SQUARE;
                            ctx.getSource().sendFeedback(Text.literal("§aCompass: §fSquare"));
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("toggle")
                        .executes(ctx -> {
                            HudConfig.showCompassRose = !HudConfig.showCompassRose;
                            ctx.getSource().sendFeedback(Text.literal("§aCompass: §f" + (HudConfig.showCompassRose ? "ON" : "OFF")));
                            return 1;
                        })
                    )
                    // bare /cp toggles on/off
                    .executes(ctx -> {
                        HudConfig.showCompassRose = !HudConfig.showCompassRose;
                        ctx.getSource().sendFeedback(Text.literal("§aCompass: §f" + (HudConfig.showCompassRose ? "ON" : "OFF")));
                        return 1;
                    })
            );
        });
    }
}