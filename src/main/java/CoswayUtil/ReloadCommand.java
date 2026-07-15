package CoswayUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final CoswayUtil plugin;

    public ReloadCommand(CoswayUtil plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {


        if (!sender.hasPermission("coswayutil.reload")) {

            sender.sendMessage(
                    ChatColor.RED +
                            "You do not have permission."
            );

            return true;
        }



        plugin.reloadConfig();



        sender.sendMessage(
                ChatColor.GREEN +
                        "CoswayUtil configuration reloaded."
        );


        return true;
    }
}