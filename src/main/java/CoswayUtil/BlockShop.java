package CoswayUtil;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BlockShop implements Listener {
    private JavaPlugin plugin;
    private final Economy economy;
    private static FileConfiguration config;

    private final NamespacedKey shopItemKey = new NamespacedKey("coswayutil", "block_shop_access");

    public BlockShop(JavaPlugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    // Method to create the special book (or other item) to open the shop
    public static ItemStack createShopItem() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK); // Or use any other item
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Block Shop Access");
        meta.setLore(Collections.singletonList(ChatColor.GOLD + "Right click to open the shop."));
        meta.getPersistentDataContainer().set(new NamespacedKey("coswayutil", "block_shop_access"), PersistentDataType.STRING, "true");
        item.setItemMeta(meta);
        return item;
    }



    public void ShopGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        config = plugin.getConfig();
    }

    public static void openShop(Player player, int page) {
        Inventory shopInventory = Bukkit.createInventory(null, 54, "Shop");

        // Get the categories from the config
        List<String> categories = config.getStringList("shop.categories");
        int maxItemsPerPage = config.getInt("pagination.max_items_per_page");
        int startIndex = (page - 1) * maxItemsPerPage;
        int endIndex = startIndex + maxItemsPerPage;

        // Loop through the categories
        for (String categoryName : categories) {
            List<ItemStack> items = loadItemsFromCategory(categoryName);

            // Calculate the correct number of items for pagination
            for (int i = startIndex; i < endIndex && i < items.size(); i++) {
                ItemStack item = items.get(i);
                shopInventory.addItem(item);
            }
        }

        // Show inventory to the player
        player.openInventory(shopInventory);
    }

    private static List<ItemStack> loadItemsFromCategory(String category) {
        List<ItemStack> items = new ArrayList<>();

        // Load items for the category from the config
        List<String> itemList = config.getStringList("shop.categories." + category + ".items");

        for (String item : itemList) {
            String materialName = config.getString("shop.categories." + category + ".items." + item + ".material");
            Material material = Material.getMaterial(materialName.toUpperCase());
            int price = config.getInt("shop.categories." + category + ".items." + item + ".price");
            String displayName = config.getString("shop.categories." + category + ".items." + item + ".display_name");

            if (material != null) {
                ItemStack itemStack = new ItemStack(material);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(displayName);
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GOLD + "Price: " + price + " coins");
                    meta.setLore(lore);
                    itemStack.setItemMeta(meta);
                    items.add(itemStack);
                }
            }
        }

        return items;
    }

    // Helper method to add items to the shop
    private void addItemToShop(Inventory shop, Material material, String displayName, double price) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(Collections.singletonList(ChatColor.GOLD + "Price: " + price + " currency"));
        item.setItemMeta(meta);
        shop.addItem(item);
    }

    // Handle item right-click to open the shop
    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(shopItemKey, PersistentDataType.STRING)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                openShop(player,1);
                event.setCancelled(true); // Prevent default interaction
            }
        }
    }

    // Handle purchases from the shop
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        InventoryView clickedInventory = event.getWhoClicked().getOpenInventory();

        if (clickedInventory.getTitle().equals(ChatColor.DARK_GREEN + "Block Shop")) {
            event.setCancelled(true); // Prevent item from being moved

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                double price = getPrice(clickedItem);
                if (economy.has(player, price)) {
                    economy.withdrawPlayer(player, price);
                    player.getInventory().addItem(clickedItem);
                    player.sendMessage(ChatColor.GREEN + "You bought " + clickedItem.getType() + " for " + price + " currency.");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have enough currency to buy this.");
                }
            }
        }
    }

    // Helper method to retrieve the price of an item (can be customized to use a config file for prices)
    private double getPrice(ItemStack item) {
        // For simplicity, we assign prices based on material name, you can use a config file for more flexibility
        switch (item.getType()) {
            case STONE: return 10;
            case DIRT: return 5;
            case OAK_PLANKS: return 15;
            case BRICKS: return 20;
            default: return 0;
        }
    }

    // Register the block shop commands and events
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}

