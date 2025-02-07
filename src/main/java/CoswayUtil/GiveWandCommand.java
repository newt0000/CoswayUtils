package CoswayUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveWandCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack wand = MobLevitationWand.createWand(); // Calls the wand creation method
            player.getInventory().addItem(wand);
            player.sendMessage("You have received the Levitation Wand!");
            return true;
        }
        sender.sendMessage("Only players can use this command!");
        return false;
    }
}
