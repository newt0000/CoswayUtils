package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TPPCommand implements CommandExecutor, TabCompleter {


    private final CoswayUtil plugin;


    public TPPCommand(CoswayUtil plugin) {
        this.plugin = plugin;
    }



    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {


        if (!sender.hasPermission("coswayutil.tpp")) {

            sender.sendMessage(
                    ChatColor.RED +
                            "You do not have permission."
            );

            return true;
        }



        if (args.length < 2) {

            sender.sendMessage(
                    ChatColor.YELLOW +
                            "Usage: /tpp <player> <to player>"
            );

            return true;
        }



        Player target =
                Bukkit.getPlayerExact(args[0]);


        Player destination =
                Bukkit.getPlayerExact(args[1]);



        if (target == null) {

            sender.sendMessage(
                    ChatColor.RED +
                            "Target player is not online."
            );

            return true;
        }



        if (destination == null) {

            sender.sendMessage(
                    ChatColor.RED +
                            "Destination player is not online."
            );

            return true;
        }




        target.teleport(
                destination.getLocation()
        );



        sender.sendMessage(
                ChatColor.GREEN +
                        "Teleported " +
                        target.getName() +
                        " to " +
                        destination.getName()
        );



        if (!target.equals(sender)) {

            target.sendMessage(
                    ChatColor.GREEN +
                            "You were teleported to " +
                            destination.getName()
            );
        }


        return true;
    }







    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {


        if (args.length == 1 ||
                args.length == 2) {


            List<String> players =
                    new ArrayList<>();


            for (Player player :
                    Bukkit.getOnlinePlayers()) {

                players.add(
                        player.getName()
                );
            }


            return players;
        }


        return null;
    }
}