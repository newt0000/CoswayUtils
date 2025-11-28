package CoswayUtil.CoswayUtil;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.text.DecimalFormat;
import java.util.Arrays;

public class CheckCommand implements CommandExecutor {
    private final CoswayUtil plugin;
    private final Economy econ;
    private final NamespacedKey recipientKey;
    private final NamespacedKey amountKey;

    public CheckCommand(CoswayUtil plugin, Economy econ) {
        this.plugin = plugin;
        this.econ = econ;
        this.recipientKey = new NamespacedKey(plugin, "check_recipient");
        this.amountKey = new NamespacedKey(plugin, "check_amount");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("writecheck")) {
            if (args.length != 2) {
                player.sendMessage("Usage: /writecheck <player> <amount>");
                return true;
            }

            Player recipient = Bukkit.getPlayerExact(args[0]);
            if (recipient == null) {
                player.sendMessage("Player not found!");
                return true;
            }

            double amount;
            try {
                amount = Double.parseDouble(args[1]);
                if (amount <= 0) {
                    player.sendMessage("Amount must be positive.");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid amount.");
                return true;
            }

            if (!econ.has(player, amount)) {
                player.sendMessage("Insufficient funds.");
                return true;
            }

            econ.withdrawPlayer(player, amount);
            player.getInventory().addItem(createCheck(recipient.getName(), amount));
            player.sendMessage("Check for " + amount + " written to " + recipient.getName() + ".");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("cashcheck")) {
            ItemStack check = player.getInventory().getItemInMainHand();
            if (!isCheck(check)) {
                player.sendMessage("You must hold a valid check to cash it.");
                return true;
            }

            ItemMeta meta = check.getItemMeta();
            String recipient = meta.getPersistentDataContainer().get(recipientKey, PersistentDataType.STRING);
            Double amount = meta.getPersistentDataContainer().get(amountKey, PersistentDataType.DOUBLE);

            if (recipient == null || amount == null) {
                player.sendMessage("Invalid check.");
                return true;
            }

            if (!player.getName().equalsIgnoreCase(recipient)) {
                player.sendMessage("This check is not made out to you!");
                return true;
            }

            econ.depositPlayer(player, amount);
            player.getInventory().setItemInMainHand(null);
            player.sendMessage("You cashed a check for " + amount + ".");
            return true;
        }

        return false;
    }

    private ItemStack createCheck(String recipient, double amount) {
        ItemStack check = new ItemStack(Material.PAPER);
        ItemMeta meta = check.getItemMeta();
        meta.setDisplayName("Â§6C.S.E.L. Check");


        final DecimalFormat formatter = new DecimalFormat("#,###.##");

        private ItemStack createCheck(String recipient, double amount) {
            ItemStack check = new ItemStack(Material.PAPER);
            ItemMeta meta = check.getItemMeta();
            meta.setDisplayName("Â§6C.S.E.L. Check");

            // Format the amount for better readability
            String formattedAmount = formatter.format(amount);

            meta.setLore(Arrays.asList("Â§7Recipient: Â§b" + recipient, "Â§7Amount: Â§a$" + formattedAmount));

            meta.getPersistentDataContainer().set(recipientKey, PersistentDataType.STRING, recipient);
            meta.getPersistentDataContainer().set(amountKey, PersistentDataType.DOUBLE, amount);

            check.setItemMeta(meta);
            return check;
        }


        meta.getPersistentDataContainer().set(recipientKey, PersistentDataType.STRING, recipient);
        meta.getPersistentDataContainer().set(amountKey, PersistentDataType.DOUBLE, amount);

        check.setItemMeta(meta);
        return check;
    }

    private boolean isCheck(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(recipientKey, PersistentDataType.STRING);
    }
}


