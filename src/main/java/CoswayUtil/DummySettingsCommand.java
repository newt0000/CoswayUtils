package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DummySettingsCommand implements CommandExecutor, TabCompleter {

    private final DummyManager dummyManager;
    private final CoswayUtil plugin;


    private final List<String> settings = Arrays.asList(
            "gravity",
            "invulnerable",
            "customname",
            "customnamevisible",
            "collision",
            "silent",
            "ai",
            "glowing",
            "persistent",
            "invisible",
            "follow",
            "look"
    );


    public DummySettingsCommand(
            CoswayUtil plugin,
            DummyManager dummyManager
    ) {
        this.plugin = plugin;
        this.dummyManager = dummyManager;
    }



    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {


        if (!(sender instanceof Player player)) {

            sender.sendMessage(
                    ChatColor.RED +
                            "Only players can use this command."
            );

            return true;
        }


        Mannequin dummy = getTargetDummy(player);


        if (dummy == null) {

            player.sendMessage(
                    ChatColor.RED +
                            "You must be looking at a CoswayUtil dummy."
            );

            return true;
        }


        if (args.length < 2) {

            player.sendMessage(
                    ChatColor.YELLOW +
                            "/dummysettings <setting> <value>"
            );

            player.sendMessage(
                    ChatColor.GRAY +
                            "Options: " +
                            String.join(", ", settings)
            );

            return true;
        }


        String setting = args[0].toLowerCase();
        String value = args[1];


        switch (setting) {


            case "gravity" -> {

                Boolean bool = parseBoolean(value);

                if (bool == null)
                    return invalidBoolean(player);

                dummy.setGravity(bool);
            }


            case "invulnerable" -> {

                Boolean bool = parseBoolean(value);

                if (bool == null)
                    return invalidBoolean(player);

                dummy.setInvulnerable(bool);
            }


            case "customname" -> {

                dummy.customName(
                        net.kyori.adventure.text.Component.text(value)
                );
            }


            case "customnamevisible" -> {

                Boolean bool = parseBoolean(value);

                if (bool == null)
                    return invalidBoolean(player);

                dummy.setCustomNameVisible(bool);
            }


            case "collision" -> {

                Boolean bool = parseBoolean(value);

                if (bool == null)
                    return invalidBoolean(player);

                dummy.setCollidable(bool);
            }


            case "silent" -> {

                Boolean bool = parseBoolean(value);

                if (bool == null)
                    return invalidBoolean(player);

                dummy.setSilent(bool);
            }


            case "ai" -> {

                Boolean bool = parseBoolean(value);

                if (bool == null)
                    return invalidBoolean(player);

                dummy.setAI(bool);
            }


            case "glowing" -> {

                Boolean bool = parseBoolean(value);

                if (bool == null)
                    return invalidBoolean(player);

                dummy.setGlowing(bool);
            }


            case "persistent" -> {

                Boolean bool = parseBoolean(value);

                if (bool == null)
                    return invalidBoolean(player);

                dummy.setPersistent(bool);
            }


            case "invisible" -> {

                Boolean bool = parseBoolean(value);

                if (bool == null)
                    return invalidBoolean(player);

                dummy.setInvisible(bool);
            }
            case "follow" -> {

                Boolean bool = parseBoolean(value);

                if (bool == null)
                    return invalidBoolean(player);


                dummy.getPersistentDataContainer()
                        .set(
                                new NamespacedKey(
                                        plugin,
                                        "dummy_follow"
                                ),
                                PersistentDataType.BOOLEAN,
                                bool
                        );


                if (bool) {

                    if (args.length < 3) {

                        player.sendMessage(
                                ChatColor.RED +
                                        "Usage: /dummysettings follow true <player>"
                        );

                        return true;
                    }


                    Player target =
                            Bukkit.getPlayer(args[2]);


                    if (target == null) {

                        player.sendMessage(
                                ChatColor.RED +
                                        "Player not found."
                        );

                        return true;
                    }


                    dummy.getPersistentDataContainer()
                            .set(
                                    new NamespacedKey(
                                            plugin,
                                            "dummy_follow_target"
                                    ),
                                    PersistentDataType.STRING,
                                    target.getUniqueId().toString()
                            );
                }

            }
            case "look" -> {

                Boolean bool = parseBoolean(value);

                if (bool == null)
                    return invalidBoolean(player);


                dummy.getPersistentDataContainer()
                        .set(
                                new NamespacedKey(
                                        plugin,
                                        "dummy_look"
                                ),
                                PersistentDataType.BOOLEAN,
                                bool
                        );

            }

            default -> {

                player.sendMessage(
                        ChatColor.RED +
                                "Unknown setting."
                );

                return true;
            }
        }


        player.sendMessage(
                ChatColor.GREEN +
                        "Updated dummy setting: "
                        + setting
        );


        return true;
    }




    private Mannequin getTargetDummy(Player player) {


        RayTraceResult result =
                player.getWorld()
                        .rayTraceEntities(
                                player.getEyeLocation(),
                                player.getEyeLocation()
                                        .getDirection(),
                                6,
                                entity ->
                                        entity instanceof Mannequin
                        );


        if (result == null)
            return null;


        Entity entity =
                result.getHitEntity();


        if (!(entity instanceof Mannequin mannequin))
            return null;


        if (!dummyManager.isDummy(mannequin))
            return null;


        return mannequin;
    }



    private Boolean parseBoolean(String input) {

        if (input.equalsIgnoreCase("true"))
            return true;

        if (input.equalsIgnoreCase("false"))
            return false;

        return null;
    }



    private boolean invalidBoolean(Player player) {

        player.sendMessage(
                ChatColor.RED +
                        "Value must be true or false."
        );

        return true;
    }





    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {


        if (args.length == 1) {

            List<String> result =
                    new ArrayList<>();

            for (String option : settings) {

                if (option.startsWith(
                        args[0].toLowerCase()
                )) {
                    result.add(option);
                }
            }

            return result;
        }



        if (args.length == 2) {

            if (!args[0].equalsIgnoreCase("customname")) {

                return Arrays.asList(
                        "true",
                        "false"
                );
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("follow")
                && args[1].equalsIgnoreCase("true")) {

            List<String> players = new ArrayList<>();

            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                    players.add(online.getName());
                }
            }

            return players;
        }


        return List.of();
    }
}