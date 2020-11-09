package me.earth.phobos.util;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.util.UUIDTypeAdapter;
import me.earth.phobos.features.command.Command;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

import static net.minecraft.advancements.AdvancementManager.GSON;

public class PlayerUtil implements Util {

    public static Timer timer = new Timer();
    private static JsonParser PARSER = new JsonParser();

    public static String getNameFromUUID(UUID uuid) {
        try {
            lookUpName process = new lookUpName(uuid);
            Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getNameFromUUID(String uuid) {
        try {
            lookUpName process = new lookUpName(uuid);
            Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getName();
        } catch (Exception e) {
            return null;
        }
    }

    public static UUID getUUIDFromName(String name) {
        try {
            lookUpUUID process = new lookUpUUID(name);
            Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getUUID();
        } catch (Exception e) {
            return null;
        }
    }

    public static class lookUpUUID implements Runnable {

        private volatile UUID uuid;
        private final String name;

        public lookUpUUID(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            NetworkPlayerInfo profile;
            try {
                ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<NetworkPlayerInfo>(Objects.requireNonNull(mc.getConnection()).getPlayerInfoMap());
                profile = infoMap.stream().filter(networkPlayerInfo -> networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                assert profile != null;
                uuid = profile.getGameProfile().getId();
            } catch (Exception e) {
                profile = null;
            }

            if (profile == null) {
                Command.sendMessage("Player isn't online. Looking up UUID..");
                String s = requestIDs("[\"" + name + "\"]");
                if (s == null || s.isEmpty()) {
                    Command.sendMessage("Couldn't find player ID. Are you connected to the internet? (0)");
                } else {
                    JsonElement element = new JsonParser().parse(s);
                    if (element.getAsJsonArray().size() == 0) {
                        Command.sendMessage("Couldn't find player ID. (1)");
                    } else {
                        try {
                            String id = element.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
                            uuid = UUIDTypeAdapter.fromString(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Command.sendMessage("Couldn't find player ID. (2)");
                        }
                    }
                }
            }
        }

        public UUID getUUID() {
            return this.uuid;
        }

        public String getName() {
            return this.name;
        }
    }

    public static class lookUpName implements Runnable {

        private volatile String name;
        private final String uuid;
        private final UUID uuidID;

        public lookUpName(String input) {
            this.uuid = input;
            this.uuidID = UUID.fromString(input);
        }

        public lookUpName(UUID input) {
            this.uuidID = input;
            this.uuid = input.toString();
        }

        @Override
        public void run() {
            name = lookUpName();
        }

        public String lookUpName() {
            EntityPlayer player = null;
            if (mc.world != null) {
                player = mc.world.getPlayerEntityByUUID(uuidID);
            }

            if (player == null) {
                String url = "https://api.mojang.com/user/profiles/" + uuid.replace("-", "") + "/names";
                try {
                    @SuppressWarnings("deprecation")
                    String nameJson = IOUtils.toString(new URL(url));
                    JSONArray nameValue = (JSONArray) JSONValue.parseWithException(nameJson);
                    String playerSlot = nameValue.get(nameValue.size() - 1).toString();
                    JSONObject nameObject = (JSONObject) JSONValue.parseWithException(playerSlot);
                    return nameObject.get("name").toString();
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
                return null;
            }
            return player.getName();
        }

        public String getName() {
            return this.name;
        }
    }

    public static String requestIDs(String data) {
        try {
            String query = "https://api.mojang.com/profiles/minecraft";
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.close();
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String res = convertStreamToString(in);
            in.close();
            conn.disconnect();
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    public static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "/";
    }

    public static List<String> getHistoryOfNames(UUID id) {
        try {
            JsonArray array = getResources(new URL("https://api.mojang.com/user/profiles/" + getIdNoHyphens(id) + "/names"), "GET").getAsJsonArray();
            List<String> temp = Lists.newArrayList();
            for (JsonElement e : array) {
                JsonObject node = e.getAsJsonObject();
                String name = node.get("name").getAsString();
                long changedAt = node.has("changedToAt") ? node.get("changedToAt").getAsLong() : 0L;
                temp.add(name + TextUtil.DARK_GRAY + new Date(changedAt).toString());
            }
            Collections.sort(temp);
            return temp;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String getIdNoHyphens(UUID uuid) {
        return uuid.toString().replaceAll("-", "");
    }

    private static JsonElement getResources(URL url, String request) throws Exception {
        return getResources(url, request, (JsonElement)null);
    }

    private static JsonElement getResources(URL url, String request, JsonElement element) throws Exception {
        HttpsURLConnection connection = null;

        try {
            connection = (HttpsURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(request);
            connection.setRequestProperty("Content-Type", "application/json");
            if (element != null) {
                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                output.writeBytes(GSON.toJson(element));
                output.close();
            }

            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder builder = new StringBuilder();

            while(scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
                builder.append('\n');
            }

            scanner.close();
            String json = builder.toString();
            JsonElement data = PARSER.parse(json);
            return data;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
