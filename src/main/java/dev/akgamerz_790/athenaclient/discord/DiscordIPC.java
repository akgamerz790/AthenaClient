package dev.akgamerz_790.athenaclient.discord;

import dev.akgamerz_790.athenaclient.AthenaClient;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DiscordIPC {

    private static final long CLIENT_ID = 1483327854115815576L;
    private static final long START_TIME = System.currentTimeMillis() / 1000L;

    private static RandomAccessFile pipe;
    private static boolean connected = false;
    private static int nonce = 1;

    public static void start() {
        Thread thread = new Thread(() -> {
            try {
                connect();
            } catch (Exception e) {
                AthenaClient.LOGGER.warn("[AthenaClient] Discord IPC failed: {}", e.getMessage());
            }
        }, "AthenaClient-DiscordIPC");
        thread.setDaemon(true);
        thread.start();
    }

    private static void connect() throws Exception {
        for (int i = 0; i < 10; i++) {
            try {
                pipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
                break;
            } catch (Exception e) {
                // try next
            }
        }

        if (pipe == null) {
            AthenaClient.LOGGER.warn("[AthenaClient] Discord not found");
            return;
        }

        sendPacket(0, "{\"v\":1,\"client_id\":\"" + CLIENT_ID + "\"}");
        readPacket();

        connected = true;
        AthenaClient.LOGGER.info("[AthenaClient] Discord RPC connected");
        // PresenceUpdater handles updates from here
    }

    public static void updatePresence(String state, String details) {
        if (!connected || pipe == null) return;
        try {
            String json = "{"
            + "\"cmd\":\"SET_ACTIVITY\","
            + "\"args\":{"
            +   "\"pid\":" + ProcessHandle.current().pid() + ","
            +   "\"activity\":{"
            +     "\"state\":\"" + state + "\","
            +     "\"details\":\"" + details + "\","
            +     "\"timestamps\":{\"start\":" + START_TIME + "},"
            +     "\"assets\":{"
            +       "\"large_image\":\"athena\","
            +       "\"large_text\":\"AthenaClient\","
            +       "\"small_image\":\"akgamerz_790\","
            +       "\"small_text\":\"akgamerz_790\""
            +     "},"
            +     "\"buttons\":["
            +       "{\"label\":\"Download AthenaClient\",\"url\":\"https://github.com/akgamerz790/AthenaClient\"},"
            +       "{\"label\":\"My Hypixel Stats\",\"url\":\"https://plancke.io/hypixel/player/stats/akgamerz_790\"}"
            +     "]"
            +   "}"
            + "},"
            + "\"nonce\":\"" + (nonce++) + "\""
            + "}";
            sendPacket(1, json);
            readPacket();
        } catch (Exception e) {
            AthenaClient.LOGGER.warn("[AthenaClient] Discord presence update failed: {}", e.getMessage());
            connected = false;
        }
    }

    private static void sendPacket(int op, String json) throws Exception {
        byte[] jsonBytes = json.getBytes("UTF-8");
        ByteBuffer buf = ByteBuffer.allocate(8 + jsonBytes.length)
            .order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(op);
        buf.putInt(jsonBytes.length);
        buf.put(jsonBytes);
        pipe.write(buf.array());
    }

    private static String readPacket() throws Exception {
        byte[] header = new byte[8];
        pipe.readFully(header);
        ByteBuffer buf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        int op = buf.getInt();
        int length = buf.getInt();
        byte[] data = new byte[length];
        pipe.readFully(data);
        return new String(data, "UTF-8");
    }

    public static void stop() {
        try {
            if (pipe != null) pipe.close();
        } catch (Exception e) {
            // ignore
        }
        connected = false;
    }
}