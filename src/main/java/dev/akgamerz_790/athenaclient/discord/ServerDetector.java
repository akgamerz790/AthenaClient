package dev.akgamerz_790.athenaclient.discord;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

public class ServerDetector {

    public enum Server { HYPIXEL, OTHER, SINGLEPLAYER }

    public static Server getCurrentServer() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getCurrentServerEntry() == null) return Server.SINGLEPLAYER;
        String ip = mc.getCurrentServerEntry().address.toLowerCase();
        if (ip.contains("hypixel.net")) return Server.HYPIXEL;
        return Server.OTHER;
    }

    public static String getServerIP() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ServerInfo info = mc.getCurrentServerEntry();
        if (info == null) return "Singleplayer";
        return info.address;
    }
}