package CoswayUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GravityGauntletCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack gauntlet = new ItemStack(Material.NETHERITE_HOE); // You can change this to any item you prefer
            ItemMeta meta = gauntlet.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Gravity Gauntlet");
                meta.setUnbreakable(true);
                gauntlet.setItemMeta(meta);
            }

            player.getInventory().addItem(gauntlet);
            player.sendMessage(ChatColor.GREEN + "You have been given the Gravity Gauntlet!");
        } else {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
        }
        return true;
    }
}