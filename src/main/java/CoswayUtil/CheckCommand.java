package CoswayUtil;

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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("writecheck")) {
            return handleWriteCheck(player, args);
        }

        if (cmd.getName().equalsIgnoreCase("cashcheck")) {
            return handleCashCheck(player);
        }

        return false;
    }

    private boolean handleWriteCheck(Player player, String[] args) {
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
        } catch (NumberFormatException ex) {
            player.sendMessage("Invalid amount.");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage("Amount must be positive.");
            return true;
        }

        if (!econ.has(player, amount)) {
            player.sendMessage("Insufficient funds to write that check.");
            return true;
        }

        econ.withdrawPlayer(player, amount);
        ItemStack check = createCheck(recipient.getName(), amount);
        player.getInventory().addItem(check);
        player.sendMessage("You wrote a check for $" + formatAmount(amount) + " to " + recipient.getName() + ".");
        return true;
    }

    private boolean handleCashCheck(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isCheck(item)) {
            player.sendMessage("You must be holding a valid C.S.E.L. Check to cash it.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage("Invalid check.");
            return true;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String recipientName = pdc.get(recipientKey, PersistentDataType.STRING);
        Double amount = pdc.get(amountKey, PersistentDataType.DOUBLE);

        if (recipientName == null || amount == null) {
            player.sendMessage("This check appears to be corrupted.");
            return true;
        }

        if (!player.getName().equalsIgnoreCase(recipientName)) {
            player.sendMessage("This check is not made out to you!");
            return true;
        }

        econ.depositPlayer(player, amount);
        player.getInventory().setItemInMainHand(null);
        player.sendMessage("You cashed a check for $" + formatAmount(amount) + ".");
        return true;
    }

    private ItemStack createCheck(String recipient, double amount) {
        ItemStack check = new ItemStack(Material.PAPER);
        ItemMeta meta = check.getItemMeta();
        if (meta == null) {
            return check;
        }

        meta.setDisplayName("§6C.S.E.L. Check");

        String formattedAmount = formatAmount(amount);
        meta.setLore(Arrays.asList(
                "§7Recipient: §b" + recipient,
                "§7Amount: §a$" + formattedAmount
        ));

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(recipientKey, PersistentDataType.STRING, recipient);
        pdc.set(amountKey, PersistentDataType.DOUBLE, amount);

        check.setItemMeta(meta);
        return check;
    }

    private boolean isCheck(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(recipientKey, PersistentDataType.STRING)
                && pdc.has(amountKey, PersistentDataType.DOUBLE);
    }

    private String formatAmount(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###.##");
        return formatter.format(amount);
    }
}
