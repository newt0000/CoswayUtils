package CoswayUtil;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
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

import java.text.NumberFormat;
import java.util.*;

public class BlockShop implements Listener {
    private static JavaPlugin plugin;
    private final Economy economy;
    private static FileConfiguration config;

    private final NamespacedKey shopItemKey = new NamespacedKey("coswayutil", "block_shop_access");

    public BlockShop(JavaPlugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
        config = plugin.getConfig(); // Fix: Initialize config properly
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

    public static void openShop(Player player, int page) {
        reloadShopConfig();
        Inventory shopInventory = Bukkit.createInventory(null, 54, ChatColor.GREEN+"Block Shop");

        // Get the categories from the config
        if (config == null) {
            player.sendMessage(ChatColor.RED + "Shop configuration is missing.");
            return;
        }

        ConfigurationSection categoriesSection = config.getConfigurationSection("shop.categories");
        ConfigurationSection Conf = config.getConfigurationSection("shop");
        if (categoriesSection == null) {
            player.sendMessage(ChatColor.RED + "No categories found in the shop.");
            player.sendMessage(ChatColor.AQUA + "" + Conf);
            return;
        }

        Set<String> categories = categoriesSection.getKeys(false);
        int maxItemsPerPage = config.getInt("pagination.max_items_per_page");
        int startIndex = (page - 1) * maxItemsPerPage;
        int endIndex = startIndex + maxItemsPerPage;

        // Loop through the categories
        for (String categoryName : categories) {
            List<ItemStack> items = loadItemsFromCategory(categoryName);

            // Calculate the correct number of items for pagination
            for (int i = startIndex; i < endIndex && i < items.size(); i++) {
                ItemStack item = items.get(i);

                // Get the item key from config based on index
                String itemKey = new ArrayList<>(config.getConfigurationSection("shop.categories." + categoryName + ".items").getKeys(false)).get(i);

                // Retrieve the quantity from the config
                int quantity = config.getInt("shop.categories." + categoryName + ".items." + itemKey + ".quantity", 64); // Default 64 if missing

                // Set the correct stack size
                item.setAmount(quantity);
                shopInventory.addItem(item);
            }

        }

        // Show inventory to the player
        player.openInventory(shopInventory);
    }

    private static List<ItemStack> loadItemsFromCategory(String category) {
        List<ItemStack> items = new ArrayList<>(); // Correct variable name

        // Load items for the category from the config
        ConfigurationSection itemSection = config.getConfigurationSection("shop.categories." + category + ".items");
        if (itemSection == null) {
            return items;
        }

        for (String itemKey : itemSection.getKeys(false)) {
            String materialName = config.getString("shop.categories." + category + ".items." + itemKey + ".material");
            int price = config.getInt("shop.categories." + category + ".items." + itemKey + ".price");
            String displayName = config.getString("shop.categories." + category + ".items." + itemKey + ".display_name");
            boolean available = config.getBoolean("shop.categories." + category + ".items." + itemKey + ".available");
            if (available) {
                if (materialName != null) {
                    Material material = Material.matchMaterial(materialName.toUpperCase());
                    if (material != null) {
                        ItemStack itemStack = new ItemStack(material);
                        ItemMeta meta = itemStack.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(ChatColor.GREEN + (displayName != null ? displayName : "Unnamed Item"));
                            List<String> lore = new ArrayList<>();
                            // Format price with commas
                            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                            String formattedPrice = formatter.format(price);
                            lore.add(ChatColor.GOLD + "Price: $" + formattedPrice);
                            meta.setLore(lore);
                            itemStack.setItemMeta(meta);
                            items.add(itemStack);
                        }
                    }
                }
                //--non availability logic
            }
        }

        return items;
    }

    public static void reloadShopConfig() {
        plugin.reloadConfig(); // Reloads config from disk
        config = plugin.getConfig(); // Reassign the updated config
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        InventoryView clickedInventory = event.getWhoClicked().getOpenInventory();
        Inventory topInventory = clickedInventory.getTopInventory(); // The shop inventory
        Inventory clickedSlotInventory = event.getClickedInventory(); // Where the click happened

        // Ensure the clicked inventory is the Block Shop (not player's inventory)
        if (clickedInventory.getTitle().equals(ChatColor.GREEN + "Block Shop")) {
            event.setCancelled(true); // Prevent item movement

            // Ignore clicks outside the shop inventory
            if (clickedSlotInventory == null || !clickedSlotInventory.equals(topInventory)) {
                return;
            }

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                double price = getPrice(clickedItem);
                if (!economy.has(player, price)) {
                    player.sendMessage(ChatColor.RED + getConfigLine("poor", "You don't have enough money to buy this."));
                    return;
                }

                // Create a new ItemStack with the default meta
                ItemStack sold = clickedItem.clone();

                // Only reset the meta if the item is NOT a Knowledge Book
                if (clickedItem.getType() != Material.KNOWLEDGE_BOOK) {
                    ItemMeta defaultMeta = sold.getItemMeta();
                    if (defaultMeta != null) {
                        defaultMeta.setDisplayName(null);
                        defaultMeta.setLore(null);
                        sold.setItemMeta(defaultMeta);
                    }
                }

                // Check if player's inventory has space before proceeding
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(sold);
                if (!leftover.isEmpty()) {
                    player.sendMessage(ChatColor.RED + getConfigLine("inventory_full", "Your inventory is full! Purchase failed."));
                    return;
                }

                // Finalize the purchase
                economy.withdrawPlayer(player, price);
                player.sendMessage(ColorKey("&aYou bought &b" + clickedItem.getAmount() + " &7" +
                        String.valueOf(sold.getType()).toLowerCase().replace("_", " ") +
                        "&a for &6$" + price));
            }
        }
    }


    public String ColorKey(String t) {
        char searchChar = '&';  // Character to search for
        char replacementChar = '§';  // Character to replace with
        StringBuilder sb = new StringBuilder(t);
        // Search for the character in the StringBuilder
        int index = sb.indexOf(String.valueOf(searchChar));

        // Replace the character if found
        for (int j = 0; j < sb.length(); j++) {
            if (sb.charAt(j) == searchChar) {
                sb.setCharAt(j, replacementChar);
            }
        }
        return sb.toString();
    }
    public String getConfigLine(String source, String dfault) {
        reloadShopConfig();
        String value = plugin.getConfig().getString(source, dfault);
        if (!config.contains(source)) {
            config.set(source, dfault);
            plugin.saveConfig();
        }
        Bukkit.getLogger().info("Fetching config key: " + source + " | Found: " + value);
        return value;
    }


    // Helper method to retrieve the price of an item (can be customized to use a config file for prices)
    private double getPrice(ItemStack item) {
        for (String category : config.getConfigurationSection("shop.categories").getKeys(false)) {
            ConfigurationSection itemsSection = config.getConfigurationSection("shop.categories." + category + ".items");
            if (itemsSection != null) {
                for (String itemKey : itemsSection.getKeys(false)) {
                    String materialName = config.getString("shop.categories." + category + ".items." + itemKey + ".material");
                    if (materialName != null && item.getType() == Material.matchMaterial(materialName.toUpperCase())) {
                        return config.getDouble("shop.categories." + category + ".items." + itemKey + ".price", 0);
                    }
                }
            }
        }
        return 0; // Default to 0 if no price is found
    }


    // Register the block shop commands and events
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}

