package CoswayUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RenameCommand implements CommandExecutor {


    private final CoswayUtil plugin;


    public RenameCommand(CoswayUtil plugin) {
        this.plugin = plugin;
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



        if (!player.hasPermission("coswayutil.rename")) {

            player.sendMessage(
                    ChatColor.RED +
                            "You do not have permission."
            );

            return true;
        }



        if (args.length == 0) {

            player.sendMessage(
                    ChatColor.YELLOW +
                            "Usage: /rename <name>"
            );

            return true;
        }



        ItemStack item =
                player.getInventory()
                        .getItemInMainHand();



        if (item.getType().isAir()) {

            player.sendMessage(
                    ChatColor.RED +
                            "You must be holding an item."
            );

            return true;
        }



        ItemMeta meta =
                item.getItemMeta();



        if (meta == null) {
            return true;
        }



        String name =
                String.join(" ", args);



        name =
                ChatColor.translateAlternateColorCodes(
                        '&',
                        name
                );



        meta.setDisplayName(name);


        item.setItemMeta(meta);



        player.sendMessage(
                ChatColor.GREEN +
                        "Item renamed to: " +
                        name
        );


        return true;
    }
}