package CoswayUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkinResolver {

    private final CoswayUtil plugin;

    private final Map<String, SkinData> cache =
            new ConcurrentHashMap<>();


    public SkinResolver(CoswayUtil plugin) {
        this.plugin = plugin;
    }



    public void resolve(
            String input,
            SkinCallback callback
    ) {


        String key =
                input.toLowerCase();


        if (cache.containsKey(key)) {

            callback.complete(
                    cache.get(key)
            );

            return;
        }



        Bukkit.getScheduler()
                .runTaskAsynchronously(
                        plugin,
                        () -> {

                            try {

                                UUID uuid;
                                String name = input;


                                if (looksLikeUUID(input)) {

                                    uuid =
                                            UUID.fromString(input);

                                } else {

                                    uuid =
                                            fetchUUID(input);

                                    if (uuid == null) {

                                        callback.complete(null);
                                        return;
                                    }
                                }



                                SkinData skin =
                                        fetchSkin(
                                                uuid,
                                                name
                                        );


                                if (skin != null) {

                                    cache.put(
                                            key,
                                            skin
                                    );

                                    cache.put(
                                            uuid.toString(),
                                            skin
                                    );
                                }


                                callback.complete(skin);


                            } catch (Exception e) {

                                e.printStackTrace();
                                callback.complete(null);
                            }

                        }
                );
    }




    private UUID fetchUUID(
            String username
    ) throws Exception {


        URL url =
                new URL(
                        "https://api.mojang.com/users/profiles/minecraft/"
                                + username
                );


        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();


        connection.setRequestMethod(
                "GET"
        );


        if (connection.getResponseCode() != 200) {
            return null;
        }


        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()
                        )
                );


        JsonObject json =
                JsonParser.parseReader(reader)
                        .getAsJsonObject();


        reader.close();


        String id =
                json.get("id")
                        .getAsString();


        return UUID.fromString(
                id.replaceFirst(
                        "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                        "$1-$2-$3-$4-$5"
                )
        );
    }





    private SkinData fetchSkin(
            UUID uuid,
            String name
    ) throws Exception {


        String id =
                uuid.toString()
                        .replace("-", "");



        URL url =
                new URL(
                        "https://sessionserver.mojang.com/session/minecraft/profile/"
                                + id
                                + "?unsigned=false"
                );


        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();


        connection.setRequestMethod(
                "GET"
        );


        if (connection.getResponseCode() != 200) {
            return null;
        }



        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()
                        )
                );


        JsonObject json =
                JsonParser.parseReader(reader)
                        .getAsJsonObject();


        reader.close();



        JsonArray properties =
                json.getAsJsonArray(
                        "properties"
                );



        for (int i = 0; i < properties.size(); i++) {


            JsonObject property =
                    properties.get(i)
                            .getAsJsonObject();


            if (!property.get("name")
                    .getAsString()
                    .equals("textures")) {

                continue;
            }



            return new SkinData(
                    uuid,
                    name,
                    property.get("value")
                            .getAsString(),

                    property.get("signature")
                            .getAsString()
            );
        }


        return null;
    }





    private boolean looksLikeUUID(
            String input
    ) {

        try {

            UUID.fromString(input);
            return true;

        } catch (Exception ignored) {

            return false;
        }
    }





    public interface SkinCallback {

        void complete(
                SkinData skin
        );
    }
}