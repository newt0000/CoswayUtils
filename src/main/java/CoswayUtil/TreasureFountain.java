package CoswayUtil;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public class TreasureFountain implements Listener {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public TreasureFountain(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUseTreasureFountain(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Entity target = event.getRightClicked();

        if (!isTreasureFountain(item)) return;

        if (!(target instanceof Monster)) {
            player.sendMessage(ChatColor.RED + "This item only works on monsters!");
            return;
        }

        Location loc = target.getLocation();
        target.remove();
        player.sendMessage(ChatColor.GOLD + "The Treasure Fountain is activated!");

        // consume item
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        spawnTreasureCloud(loc);
    }

    public static ItemStack getTreasureFountainItem() {
        ItemStack treasureFountain = new ItemStack(Material.STICK, 1);
        ItemMeta meta = treasureFountain.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Treasure Fountain");
            meta.setLore(List.of(
                    ChatColor.GRAY + "Use this on a monster to",
                    ChatColor.GRAY + "summon a treasure fountain!"
            ));
            treasureFountain.setItemMeta(meta);
        }
        return treasureFountain;
    }

    private boolean isTreasureFountain(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return ChatColor.stripColor(item.getItemMeta().getDisplayName())
                .equalsIgnoreCase("Treasure Fountain");
    }

    private void spawnTreasureCloud(Location loc) {
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 60) {
                    spawnTreasureItems(loc);
                    cancel();
                    return;
                }
                loc.getWorld().spawnParticle(Particle.CLOUD, loc, 10, 0.5, 0.5, 0.5, 0);
                count++;
            }
        }.runTaskTimer(plugin, 0, 5);
    }

    private void spawnTreasureItems(Location loc) {
        FileConfiguration config = plugin.getConfig();
        List<String> treasureItems = config.getStringList("treasure");
        if (treasureItems.isEmpty()) {
            Bukkit.getLogger().warning("[CoswayUtil] Treasure list is empty in config!");
            return;
        }

        int itemCount = 10 + random.nextInt(11); // 10-20
        for (int i = 0; i < itemCount; i++) {
            String itemName = treasureItems.get(random.nextInt(treasureItems.size()));
            Material material = Material.matchMaterial(itemName);
            if (material == null) continue;

            ItemStack treasure = new ItemStack(material, 1);
            spawnFloatingItem(loc, treasure);
        }
    }

    private void spawnFloatingItem(Location loc, ItemStack item) {
        World world = loc.getWorld();
        if (world == null) return;

        org.bukkit.entity.Item dropped = world.dropItem(loc, item);
        dropped.setVelocity(new Vector(
                (random.nextDouble() - 0.5) * 0.6,
                0.5 + random.nextDouble() * 0.5,
                (random.nextDouble() - 0.5) * 0.6
        ));
        dropped.setPickupDelay(20);
    }
}
