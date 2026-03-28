package dev.akgamerz_790.athenaclient.performance;

import dev.akgamerz_790.athenaclient.AthenaClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import dev.akgamerz_790.athenaclient.AthenaClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class PerformanceTweaks {

    // Tuneable constants
    private static final int MAX_PARTICLES = 2000;
    private static final int UNFOCUSED_FPS_CAP = 0;
    private static final int FOCUSED_FPS_CAP = 0; // 0 = unlimited (uses in-game setting)

    private static boolean initialized = false;

    public static void register() {
        if (initialized) {
            AthenaClient.LOGGER.warn("[AthenaClient] PerformanceTweaks already registered, skipping.");
            return;
        }

        registerUnfocusedFpsThrottle();
        registerTickDiagnostics();

        initialized = true;
        AthenaClient.LOGGER.info("[AthenaClient] PerformanceTweaks loaded. MAX_PARTICLES={}, UNFOCUSED_FPS_CAP={}",
                MAX_PARTICLES, UNFOCUSED_FPS_CAP);
    }

    // --- Throttle FPS when window is not focused ---
    private static void registerUnfocusedFpsThrottle() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.world == null)
                return;

            if (!client.isWindowFocused()) {
                // Cap FPS when tabbed out — saves CPU/GPU significantly
                client.options.getMaxFps().setValue(UNFOCUSED_FPS_CAP);
            } else {
                // Restore to whatever the user had set if we changed it
                // Only restore if it was us who throttled it
                if (client.options.getMaxFps().getValue() == UNFOCUSED_FPS_CAP) {
                    client.options.getMaxFps().setValue(FOCUSED_FPS_CAP == 0 ? 260 : FOCUSED_FPS_CAP);
                }
            }
        });
    }

    // --- Tick diagnostics (logs if ticks are spiking) ---
    private static long lastTickTime = System.currentTimeMillis();
    private static int spikeCount = 0;

    private static void registerTickDiagnostics() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null)
                return;

            long now = System.currentTimeMillis();
            long delta = now - lastTickTime;
            lastTickTime = now;

            // A tick should be ~50ms (20 TPS). Flag if it spikes over 100ms
            if (delta > 100) {
                spikeCount++;
                if (spikeCount % 10 == 1) { // don't spam logs, log every 10th spike
                    AthenaClient.LOGGER.warn("[AthenaClient] Tick spike detected: {}ms (spike #{})", delta, spikeCount);
                }
            }
        });
    }

    // --- Getters for HUD or debug use ---
    public static int getMaxParticles() {
        return MAX_PARTICLES;
    }

    public static int getSpikeCount() {
        return spikeCount;
    }
}