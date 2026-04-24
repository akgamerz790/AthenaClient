package dev.akgamerz_790.athenaclient.hud;

import dev.akgamerz_790.athenaclient.AthenaClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

// AthenaClient Project Imports
// import dev.akgamerz_790.config.ColorProfiles;

@Environment(EnvType.CLIENT)
public class HudRenderer {

    public ColorProfiles CP;

    private static final int X           = 4;
    private static final int LINE_HEIGHT = 11;

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient mc = AthenaClient.mc();
            if (mc.player == null || mc.world == null) return;
            if (mc.options.hudHidden) return;
            if (mc.currentScreen != null) return;
            render(drawContext, mc);
        });
    }

    private static void render(DrawContext ctx, MinecraftClient mc) {
        PlayerEntity player = mc.player;
        BlockPos pos = player.getBlockPos();
        int y = 4;

        // FPS
        if (HudConfig.showFps) {
            int fps = mc.getCurrentFps();
            int col = fps >= 60 ? CP.CP.COLOR_GREEN : fps >= 30 ? CP.COLOR_YELLOW : CP.COLOR_RED;
            draw(ctx, mc, "FPS: " + fps, X, y, col);
            y += LINE_HEIGHT;
        }

        // Coordinates
        if (HudConfig.showCoords) {
            draw(ctx, mc,
                String.format("XYZ: %d / %d / %d", pos.getX(), pos.getY(), pos.getZ()),
                X, y, CP.COLOR_WHITE);
            y += LINE_HEIGHT;
        }

        // Direction
        if (HudConfig.showDirection) {
            draw(ctx, mc,
                String.format("Facing: %s (%.1f)", getFacing(player.getYaw()), normalizeYaw(player.getYaw())),
                X, y, CP.COLOR_YELLOW);
            y += LINE_HEIGHT;
        }

        // Biome
        if (HudConfig.showBiome) {
            final int biomeY = y;
            mc.world.getBiome(pos).getKey().ifPresent(key -> {
                String name = formatBiomeName(key.getValue().getPath());
                draw(ctx, mc, "Biome: " + name, X, biomeY, CP.COLOR_CYAN);
            });
            y += LINE_HEIGHT;
        }

        // Dimension
        if (HudConfig.showDimension) {
            String dim = mc.world.getRegistryKey().getValue().getPath();
            draw(ctx, mc, "Dim: " + dim, X, y, CP.COLOR_PURPLE);
            y += LINE_HEIGHT;
        }

        // Armor
        if (HudConfig.showArmor) {
            y = renderArmor(ctx, mc, player, y);
        }

        // Light
        if (HudConfig.showLight) {
            int sky   = mc.world.getLightLevel(LightType.SKY, pos);
            int block = mc.world.getLightLevel(LightType.BLOCK, pos);
            draw(ctx, mc, String.format("Light  Sky: %d  Block: %d", sky, block), X, y, CP.COLOR_WHITE);
            y += LINE_HEIGHT;
        }
    }

    private static void draw(DrawContext ctx, MinecraftClient mc, String text, int x, int y, int color) {
        ctx.drawText(mc.textRenderer, text, x, y, color, true);
    }

    private static int renderArmor(DrawContext ctx, MinecraftClient mc, PlayerEntity player, int y) {
        boolean hasArmor = false;
        for (int slot = 36; slot <= 39; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty() && stack.isDamageable()) {
                int remaining = stack.getMaxDamage() - stack.getDamage();
                int pct = (int) ((remaining / (float) stack.getMaxDamage()) * 100);
                int col = pct > 50 ? CP.COLOR_GREEN : pct > 20 ? CP.COLOR_YELLOW : CP.COLOR_RED;
                String name = stack.getItem().toString();
                name = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name;
                draw(ctx, mc, name + ": " + pct + "%", X, y, col);
                y += LINE_HEIGHT;
                hasArmor = true;
            }
        }
        if (!hasArmor) {
            draw(ctx, mc, "Armor: None", X, y, CP.COLOR_RED);
            y += LINE_HEIGHT;
        }
        return y;
    }

    private static String getFacing(float yaw) {
        yaw = normalizeYaw(yaw);
        if (yaw < 45 || yaw >= 315) return "South";
        if (yaw < 135)              return "West";
        if (yaw < 225)              return "North";
        return "East";
    }

    private static float normalizeYaw(float yaw) {
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;
        return yaw;
    }

    private static String formatBiomeName(String raw) {
        StringBuilder sb = new StringBuilder();
        for (String word : raw.split("_")) {
            if (!word.isEmpty())
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}