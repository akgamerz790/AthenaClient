package dev.akgamerz_790.athenaclient.discord;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ScoreboardParser {

    private static final Pattern COLOR_CODE = Pattern.compile("(?:Â)?\u00A7.");
    private static final Pattern STRIKETHROUGH_CODE = Pattern.compile("(?:Â)?\u00A7m");
    private static final Pattern DATE_LINE = Pattern.compile("^\\d{2}/\\d{2}/\\d{2}.*$");

    private static volatile ParsedState latestState = new ParsedState();

    public enum GameType {
        LOBBY, BEDWARS, DISASTER, SKYWARS, MURDER_MYSTERY, BUILD_BATTLE, OTHER
    }

    public static class ParsedState {
        // Common
        public GameType gameType = GameType.OTHER;
        public String game = "";
        public String status = "";

        // Bedwars
        public String mode = "";
        public String map = "";
        public String players = "";
        public String startingIn = "";
        public int kills = 0;
        public int finalKills = 0;

        // Disaster
        public List<String> currentDisasters = new ArrayList<>();
        public List<String> previousDisasters = new ArrayList<>();
        public String countdown = "";
    }

    public static void updateFromObjective(ScoreboardObjective objective) {
        try {
            List<Line> lines = collectLines(objective);
            latestState = parseLines(lines, objective);
        } catch (Exception ignored) {}
    }

    public static ParsedState getLatest() {
        return latestState;
    }

    // --- Line collection ---

    private static List<Line> collectLines(ScoreboardObjective objective) {
        Scoreboard scoreboard = objective.getScoreboard();
        List<ScoreboardEntry> entries = new ArrayList<>(scoreboard.getScoreboardEntries(objective));
        entries.sort(Comparator.comparingInt(ScoreboardEntry::value).reversed()
            .thenComparing(ScoreboardEntry::owner));

        List<Line> lines = new ArrayList<>();
        lines.add(new Line(objective.getDisplayName().getString(),
            clean(objective.getDisplayName().getString())));

        for (ScoreboardEntry entry : entries) {
            String raw = buildLine(scoreboard, entry);
            String cleaned = clean(raw);
            if (!cleaned.isBlank()) lines.add(new Line(raw, cleaned));
        }
        return lines;
    }

    private static String buildLine(Scoreboard scoreboard, ScoreboardEntry entry) {
        String display = entry.display() != null ? entry.display().getString() : "";
        if (!display.isBlank()) return display;
        Team team = scoreboard.getScoreHolderTeam(entry.owner());
        return Team.decorateName(team, Text.literal(entry.owner())).getString();
    }

    private static String clean(String raw) {
        return COLOR_CODE.matcher(raw).replaceAll("")
            .replace('\u00A0', ' ').trim().replaceAll("\\s+", " ");
    }

    // --- Game detection + parsing ---

    private static ParsedState parseLines(List<Line> lines, ScoreboardObjective objective) {
        ParsedState state = new ParsedState();
        if (lines.isEmpty()) return state;

        String title = lines.get(0).clean.toUpperCase(Locale.ROOT);

        // Detect game type from title
        if (title.contains("BED WARS") || title.contains("BEDWARS")) {
            state.gameType = GameType.BEDWARS;
            state.game = "Bed Wars";
            parseBedwars(lines, state);
        } else if (title.contains("DISASTER")) {
            state.gameType = GameType.DISASTER;
            state.game = "Disaster";
            parseDisaster(lines, state);
        } else if (title.contains("HYPIXEL")) {
            state.gameType = GameType.LOBBY;
            state.game = "Hypixel";
            state.status = "LOBBY";
            parseLobby(lines, state);
        } else if (title.contains("SKYWARS")) {
            state.gameType = GameType.SKYWARS;
            state.game = "Sky Wars";
            parseBedwars(lines, state); // same structure
        } else if (title.contains("MURDER MYSTERY")) {
            state.gameType = GameType.MURDER_MYSTERY;
            state.game = "Murder Mystery";
        } else if (title.contains("BUILD BATTLE")) {
            state.gameType = GameType.BUILD_BATTLE;
            state.game = "Build Battle";
        } else {
            state.gameType = GameType.OTHER;
            state.game = lines.get(0).clean;
        }

        return state;
    }

    // --- Bedwars parser ---
    private static void parseBedwars(List<Line> lines, ParsedState state) {
        for (Line line : lines) {
            String lower = line.clean.toLowerCase(Locale.ROOT);
            if (lower.startsWith("lobby:")) {
                state.status = "LOBBY";
            } else if (lower.startsWith("map:")) {
                state.map = line.clean.substring(4).trim();
            } else if (lower.startsWith("mode:")) {
                state.mode = line.clean.substring(5).trim();
            } else if (lower.startsWith("players:")) {
                state.players = line.clean.substring(8).trim();
                if (state.status.isEmpty()) state.status = "WAITING";
            } else if (lower.startsWith("waiting")) {
                if (state.status.isEmpty()) state.status = "WAITING";
            } else if (lower.startsWith("starting in")) {
                state.status = "STARTING";
                state.startingIn = line.clean.replaceAll("(?i)starting in\\s*", "").trim();
            } else if (lower.startsWith("kills:")) {
                state.status = "INGAME";
                try { state.kills = Integer.parseInt(line.clean.replaceAll("[^0-9]", "")); }
                catch (Exception ignored) {}
            } else if (lower.startsWith("final kills:")) {
                try { state.finalKills = Integer.parseInt(line.clean.replaceAll("[^0-9]", "")); }
                catch (Exception ignored) {}
            } else if (lower.startsWith("level:") || lower.startsWith("progress:") || lower.startsWith("tokens:")) {
                if (state.status.isEmpty()) state.status = "STATS";
            }
        }
    }

    // --- Disaster parser (from your working project) ---
    private static void parseDisaster(List<Line> lines, ParsedState state) {
        // Extract countdown
        for (Line line : lines) {
            String lower = line.clean.toLowerCase(Locale.ROOT);
            if (lower.startsWith("time left:") || lower.startsWith("next disaster:") || lower.startsWith("game starts in:")) {
                state.countdown = line.clean;
                state.status = lower.startsWith("game starts in:") ? "STARTING" : "INGAME";
                break;
            }
        }

        // Extract disasters
        int headerIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).clean.toLowerCase(Locale.ROOT).startsWith("disasters:")) {
                headerIndex = i;
                break;
            }
        }

        if (headerIndex >= 0) {
            for (int i = headerIndex + 1; i < lines.size(); i++) {
                Line line = lines.get(i);
                String lower = line.clean.toLowerCase(Locale.ROOT);
                if (lower.contains("www.hypixel.net")
                    || lower.startsWith("players alive:")
                    || lower.startsWith("time left:")
                    || lower.startsWith("next disaster:")
                    || DATE_LINE.matcher(line.clean).matches()
                    || line.clean.endsWith(":")) break;
                if (line.clean.length() < 2) continue;

                if (STRIKETHROUGH_CODE.matcher(line.raw).find()) {
                    state.previousDisasters.add(line.clean);
                } else {
                    state.currentDisasters.add(line.clean);
                }
            }
        }
    }

    // --- Lobby parser ---
    private static void parseLobby(List<Line> lines, ParsedState state) {
        for (Line line : lines) {
            String lower = line.clean.toLowerCase(Locale.ROOT);
            if (lower.startsWith("players:")) {
                state.players = line.clean.substring(8).trim();
            }
        }
    }

    // --- Line record ---
    private static class Line {
        final String raw;
        final String clean;
        Line(String raw, String clean) {
            this.raw = raw;
            this.clean = clean;
        }
    }
}