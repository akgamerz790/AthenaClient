package dev.akgamerz_790.athenaclient.discord;

import dev.akgamerz_790.athenaclient.AthenaClient;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PresenceUpdater {

    private static ScheduledExecutorService scheduler;

    public static void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AthenaClient-PresenceUpdater");
            t.setDaemon(true);
            return t;
        });
        // Update presence every 5 seconds
        scheduler.scheduleAtFixedRate(PresenceUpdater::update, 5, 5, TimeUnit.SECONDS);
    }

    public static void update() {
        try {
            ServerDetector.Server server = ServerDetector.getCurrentServer();
            String details;
            String state;

            if (server == ServerDetector.Server.SINGLEPLAYER) {
                details = "Playing AthenaClient 1.21.11";
                state = "Singleplayer";
            } else if (server == ServerDetector.Server.HYPIXEL) {
                ScoreboardParser.ParsedState parsed = ScoreboardParser.getLatest();
                details = buildDetails(parsed);
                state = buildState(parsed);
            } else {
                details = "Playing AthenaClient 1.21.11";
                state = ServerDetector.getServerIP();
            }

            DiscordIPC.updatePresence(state, details);
        } catch (Exception e) {
            AthenaClient.LOGGER.warn("[AthenaClient] Presence update error: {}", e.getMessage());
        }
    }

    private static String buildState(ScoreboardParser.ParsedState p) {
        switch (p.gameType) {
            case LOBBY:
                return "Main Lobby" + (p.players.isEmpty() ? "" : " • " + p.players + " players");

            case BEDWARS:
            case SKYWARS:
                switch (p.status) {
                    case "WAITING":
                        return (p.mode.isEmpty() ? "" : p.mode + " • ")
                            + "Waiting " + p.players
                            + (p.map.isEmpty() ? "" : " • " + p.map);
                    case "STARTING":
                        return (p.mode.isEmpty() ? "" : p.mode + " • ")
                            + "Starting in " + p.startingIn
                            + (p.map.isEmpty() ? "" : " • " + p.map);
                    case "INGAME":
                        return (p.mode.isEmpty() ? "" : p.mode + " • ")
                            + "K: " + p.kills + " FK: " + p.finalKills;
                    case "STATS":
                        return p.game + " Lobby";
                    default:
                        return p.game;
                }

            case DISASTER:
                if (!p.currentDisasters.isEmpty()) {
                    String disasters = String.join(", ", p.currentDisasters);
                    return disasters + (p.countdown.isEmpty() ? "" : " • " + p.countdown);
                }
                return p.countdown.isEmpty() ? "Disaster" : p.countdown;

            case MURDER_MYSTERY:
                return "Murder Mystery";

            case BUILD_BATTLE:
                return "Build Battle";

            default:
                return p.game.isEmpty() ? "Hypixel Network" : p.game;
        }
    }

    private static String buildDetails(ScoreboardParser.ParsedState p) {
        if (p.game.isEmpty()) return "Hypixel Network";
        return "Hypixel • " + p.game;
    }

    public static void stop() {
        if (scheduler != null) scheduler.shutdownNow();
    }
}