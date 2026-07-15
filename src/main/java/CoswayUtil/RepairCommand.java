package CoswayUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RepairCommand implements CommandExecutor {
    private boolean isToolOrArmor(Material mat) {
        if (mat == null) return false;

        String name = mat.name();

        // Tools
        if (name.endsWith("_PICKAXE") ||
                name.endsWith("_AXE") ||
                name.endsWith("_SHOVEL") ||
                name.endsWith("_HOE") ||
                name.endsWith("_SWORD") ||
                name.endsWith("_MACE") ||
                name.equals("MACE") ||
                name.equals("FISHING_ROD") ||
                name.equals("SHEARS") ||
                name.equals("BOW") ||
                name.equals("CROSSBOW") ||
                name.equals("TRIDENT") ||
                name.equals("BRUSH") ||
                name.equals("FLINT_AND_STEEL")) {
            return true;
        }

        // Armor
        if (name.endsWith("_HELMET") ||
                name.endsWith("_CHESTPLATE") ||
                name.endsWith("_LEGGINGS") ||
                name.endsWith("_BOOTS") ||
                name.equals("SHIELD") ||
                name.equals("ELYTRA")) {
            return true;
        }

        return false;
    }
    private static final Set<Material> REPAIRABLE_TOOLS = new HashSet<>(Arrays.asList(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
            Material.FISHING_ROD, Material.SHEARS, Material.BOW, Material.CROSSBOW, Material.TRIDENT, Material.MACE
    ));

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be holding a tool to repair it!");
            return true;
        }

        if (!isToolOrArmor(item.getType())) {
            player.sendMessage(ChatColor.RED + "That item is not a valid tool or armor!");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable)) {
            player.sendMessage(ChatColor.RED + "That item cannot be repaired.");
            return true;
        }

        Damageable damageable = (Damageable) meta;
        if (damageable.getDamage() == 0) {
            player.sendMessage(ChatColor.YELLOW + "This tool is already at full durability!");
            return true;
        }

        damageable.setDamage(0);
        item.setItemMeta(meta);
        player.sendMessage(ChatColor.GREEN + "Your tool has been fully repaired!");
        return true;
    }
}
