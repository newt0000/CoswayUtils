package CoswayUtil;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DummyCommand implements CommandExecutor {

    private final CoswayUtil plugin;
    private final DummyManager dummyManager;
    private final SkinResolver skinResolver;


    public DummyCommand(
            CoswayUtil plugin,
            DummyManager dummyManager,
            SkinResolver skinResolver
    ) {
        this.plugin = plugin;
        this.dummyManager = dummyManager;
        this.skinResolver = skinResolver;
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


        if (args.length != 1) {

            player.sendMessage(
                    ChatColor.RED +
                            "Usage: /dummy <username|uuid>"
            );

            return true;
        }


        String target = args[0];


        player.sendMessage(
                ChatColor.GRAY +
                        "Loading skin..."
        );


        skinResolver.resolve(target, skin -> {


            if (skin == null) {

                player.sendMessage(
                        ChatColor.RED +
                                "Could not find that player skin."
                );

                return;
            }


            plugin.getServer()
                    .getScheduler()
                    .runTask(
                            plugin,
                            () -> {


                                Location location =
                                        getSpawnLocation(player);


                                Mannequin mannequin =
                                        dummyManager.createDummy(
                                                location,
                                                skin,
                                                player.getUniqueId()
                                        );


                                if (mannequin != null) {

                                    player.sendMessage(
                                            ChatColor.GREEN +
                                                    "Dummy created for "
                                                    + target
                                    );
                                }
                            }
                    );
        });


        return true;
    }


    /**
     * Places dummy a few blocks in front of player.
     */
    private Location getSpawnLocation(Player player) {

        Location location =
                player.getLocation().clone().add(0.0,2.0,0.0);


        Vector direction =
                location.getDirection()
                        .normalize();


        location.add(
                direction.multiply(3)
        );


        location.setYaw(
                player.getLocation()
                        .getYaw()
        );


        location.setPitch(0);


        return location;
    }
}