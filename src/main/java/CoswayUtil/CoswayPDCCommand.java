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
        String command = cmd.getName().toLowerCase();

        switch (command) {

            // -------------------------------------------------------------------
            //  /coswaysetpdc  <@s|player> <key> <value>
            // -------------------------------------------------------------------
            case "coswaysetpdc" -> {

                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players may use this.");
                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /coswaysetpdc <@s|player> <key> <value>");
                    return true;
                }

                // Resolve target
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
                String newValue = args[2];

                String oldValue = PDCUtil.getString(target, key);
                if (oldValue == null) oldValue = "&cNULL";

                PDCUtil.setString(target, key, newValue);

                String msg = "&aSet &e" + key + "&a from &6" + oldValue + "&a to &b" + newValue +
                        " &afor player &e" + target.getName();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

                return true;
            }

            // -------------------------------------------------------------------
            //  /coswaygetpdc  <player> <key>
            // -------------------------------------------------------------------
            case "coswaygetpdc" -> {

                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players may use this.");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /coswaygetpdc <player> <key>");
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }

                String key = args[1];
                String value = PDCUtil.getString(target, key);

                if (value == null) value = ChatColor.RED + "NULL";

                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&aPDC for player &e" + target.getName() +
                                "&a key &e" + key +
                                "&a is set to: &b" + value));

                return true;
            }
        }

        return false; // Unknown command
    }
}
