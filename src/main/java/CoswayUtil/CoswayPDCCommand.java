package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CoswayPDCCommand implements CommandExecutor {

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command cmd,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!cmd.getName().equalsIgnoreCase("coswaysetpdc")) {
            return false; // Not our command
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /coswaysetpdc <@s|player> <key> <value>");
            return true;
        }

        // Target resolution
        Player target;
        if (args[0].equalsIgnoreCase("@s")) {
            target = player;
        } else {
            target = Bukkit.getPlayerExact(args[0]);
        }

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Invalid target player!");
            return true;
        }

        String key = args[1];
        String newValue = args[2]; // single-arg value like your original

        // Get old value
        String oldValue = PDCUtil.getString(target, key);
        if (oldValue == null) {
            oldValue = "&cNULL";
        }

        // Set new value
        PDCUtil.setString(target, key, newValue);

        String msg = "&aSet " + key + " from " + oldValue + " to " + newValue + " for " + target.getName();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

        return true;
    }
}
