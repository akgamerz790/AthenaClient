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

@Environment(EnvType.CLIENT)
public class HudRenderer {

    private static final int COLOR_WHITE  = 0xFFFFFFFF;
    private static final int COLOR_GREEN  = 0xFF00FF88;
    private static final int COLOR_YELLOW = 0xFFFFAA00;
    private static final int COLOR_CYAN   = 0xFF55FFFF;
    private static final int COLOR_RED    = 0xFFFF5555;
    private static final int COLOR_PURPLE = 0xFFAA55FF;

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
            int col = fps >= 60 ? COLOR_GREEN : fps >= 30 ? COLOR_YELLOW : COLOR_RED;
            draw(ctx, mc, "FPS: " + fps, X, y, col);
            y += LINE_HEIGHT;
        }

        // Coordinates
        if (HudConfig.showCoords) {
            draw(ctx, mc,
                String.format("XYZ: %d / %d / %d", pos.getX(), pos.getY(), pos.getZ()),
                X, y, COLOR_WHITE);
            y += LINE_HEIGHT;
        }

        // Direction
        if (HudConfig.showDirection) {
            draw(ctx, mc,
                String.format("Facing: %s (%.1f)", getFacing(player.getYaw()), normalizeYaw(player.getYaw())),
                X, y, COLOR_YELLOW);
            y += LINE_HEIGHT;
        }

        // Biome
        if (HudConfig.showBiome) {
            final int biomeY = y;
            mc.world.getBiome(pos).getKey().ifPresent(key -> {
                String name = formatBiomeName(key.getValue().getPath());
                draw(ctx, mc, "Biome: " + name, X, biomeY, COLOR_CYAN);
            });
            y += LINE_HEIGHT;
        }

        // Dimension
        if (HudConfig.showDimension) {
            String dim = mc.world.getRegistryKey().getValue().getPath();
            draw(ctx, mc, "Dim: " + dim, X, y, COLOR_PURPLE);
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
            draw(ctx, mc, String.format("Light  Sky: %d  Block: %d", sky, block), X, y, COLOR_WHITE);
            y += LINE_HEIGHT;
        }

        // CORNERED COMPASS PRO
        if (HudConfig.showCompassRose) {
            renderCompassRose(ctx, mc, player);
        }
    }

    private static void draw(DrawContext ctx, MinecraftClient mc, String text, int x, int y, int color) {
        ctx.drawText(mc.textRenderer, text, x, y, color, true);
    }

    private static void renderCompassRose(DrawContext ctx, MinecraftClient mc, PlayerEntity player) {
        float yaw = normalizeYaw(player.getYaw());

        int boxX = 4;
        int boxY = 4 + (LINE_HEIGHT * 8);
        int boxSize = 60;
        int centerX = boxX + boxSize / 2;
        int centerY = boxY + boxSize / 2;
        int radius = 24;

        // Background
        if (HudConfig.compassShape == HudConfig.CompassShape.CIRCLE) {
            // Draw circle background using filled segments
            drawCircleBackground(ctx, centerX, centerY, radius + 6, 0xAA000000);
        } else {
            ctx.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0xAA000000);
        }

        // Direction labels — smooth because yaw is float
        String[] dirs = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        float[] angles = {180f, 225f, 270f, 315f, 0f, 45f, 90f, 135f};

        for (int i = 0; i < dirs.length; i++) {
            float angle = (float) Math.toRadians(angles[i] + yaw);
            float dx = (float) Math.sin(angle) * radius;
            float dy = (float) -Math.cos(angle) * radius;

            // Circle shape — skip if outside radius
            if (HudConfig.compassShape == HudConfig.CompassShape.CIRCLE) {
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist > radius + 2) continue;
            }

            int tx = centerX + (int) dx - mc.textRenderer.getWidth(dirs[i]) / 2;
            int ty = centerY + (int) dy - mc.textRenderer.fontHeight / 2;

            int color;
            if (dirs[i].equals("N"))       color = 0xFFFF5555;
            else if (dirs[i].length() == 1) color = 0xFFFFFFFF;
            else                            color = 0xFFAAAAAA;

            ctx.drawTextWithShadow(mc.textRenderer, dirs[i], tx, ty, color);
        }

        // Center dot
        ctx.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, 0xFFFFFFFF);

        // Current facing label
        String facing = getFacing(player.getYaw());
        int facingX = boxX + (boxSize - mc.textRenderer.getWidth(facing)) / 2;
        ctx.drawTextWithShadow(mc.textRenderer, facing, facingX, boxY + boxSize + 2, 0xFFFFAA00);
    }

    private static void drawCircleBackground(DrawContext ctx, int cx, int cy, int r, int color) {
        for (int y = -r; y <= r; y++) {
            int halfWidth = (int) Math.sqrt(r * r - y * y);
            ctx.fill(cx - halfWidth, cy + y, cx + halfWidth, cy + y + 1, color);
        }
    }

    private static int renderArmor(DrawContext ctx, MinecraftClient mc, PlayerEntity player, int y) {
        boolean hasArmor = false;
        for (int slot = 36; slot <= 39; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty() && stack.isDamageable()) {
                int remaining = stack.getMaxDamage() - stack.getDamage();
                int pct = (int) ((remaining / (float) stack.getMaxDamage()) * 100);
                int col = pct > 50 ? COLOR_GREEN : pct > 20 ? COLOR_YELLOW : COLOR_RED;
                String name = stack.getItem().toString();
                name = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name;
                draw(ctx, mc, name + ": " + pct + "%", X, y, col);
                y += LINE_HEIGHT;
                hasArmor = true;
            }
        }
        if (!hasArmor) {
            draw(ctx, mc, "Armor: None", X, y, COLOR_RED);
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