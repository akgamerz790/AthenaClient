package dev.akgamerz_790.athenaclient;

import dev.akgamerz_790.athenaclient.hud.HudRenderer;
import dev.akgamerz_790.athenaclient.performance.PerformanceTweaks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.network.ClientPlayerEntity;

@Environment(EnvType.CLIENT)
public class AthenaClient implements ClientModInitializer {

    public static final String MOD_ID = "athenaclient";
    public static final String MOD_NAME = "AthenaClient";
    public static final String MOD_VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static AthenaClient INSTANCE;

    public static AthenaClient getInstance() {
        return INSTANCE;
    }

    public static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        LOGGER.info("[{}] Initializing v{} on Minecraft 1.21.11", MOD_NAME, MOD_VERSION);

        try {
            HudRenderer.register();
            LOGGER.info("[{}] HUD registered successfully", MOD_NAME);
        } catch (Exception e) {
            LOGGER.error("[{}] Failed to register HUD: {}", MOD_NAME, e.getMessage());
        }

        try {
            PerformanceTweaks.register();
            LOGGER.info("[{}] Performance tweaks registered successfully", MOD_NAME);
        } catch (Exception e) {
            LOGGER.error("[{}] Failed to register performance tweaks: {}", MOD_NAME, e.getMessage());
        }

        LOGGER.info("[{}] Done.", MOD_NAME);
    }
}