package CoswayUtil;

import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class RapidFireBow implements Listener {
    private final NamespacedKey bowKey = new NamespacedKey("coswayutil", "rapid_fire_bow");
    private final HashMap<UUID, BukkitRunnable> shootingTasks = new HashMap<>();
    private final CoswayUtil plugin;

    public RapidFireBow(CoswayUtil plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the player is holding the custom bow
        if (!isRapidFireBow(item)) return;

        // If the player is already shooting, do nothing
        if (shootingTasks.containsKey(player.getUniqueId())) return;

        // Start firing arrows every tick
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isHoldingBow(player)) {
                    stopShooting(player);
                    return;
                }

                // Fire arrow
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setVelocity(player.getLocation().getDirection().multiply(3)); // High speed
                arrow.setCritical(true);
                arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED); // Prevent pickup
                arrow.setShooter(player);

                // Play sound and particles
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.5f);
                player.getWorld().spawnParticle(Particle.CRIT, arrow.getLocation(), 5, 0.2, 0.2, 0.2, 0.1);
            }
        };
        task.runTaskTimer(plugin, 0L, 1L); // Fire every tick
        shootingTasks.put(player.getUniqueId(), task);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopShooting(event.getPlayer());
    }

    private boolean isHoldingBow(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return isRapidFireBow(item);
    }

    private boolean isRapidFireBow(ItemStack item) {
        if (item == null || item.getType() != Material.BOW || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(bowKey, PersistentDataType.STRING);
    }

    private void stopShooting(Player player) {
        UUID playerId = player.getUniqueId();
        if (shootingTasks.containsKey(playerId)) {
            shootingTasks.get(playerId).cancel();
            shootingTasks.remove(playerId);
        }
    }

    public static ItemStack createRapidFireBow() {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Rapid-Fire Bow");
        meta.setLore(Collections.singletonList(ChatColor.RED + "Hold Right-Click to unleash rapid arrows!"));
        meta.getPersistentDataContainer().set(new NamespacedKey("coswayutil", "rapid_fire_bow"), PersistentDataType.STRING, "true");
        bow.setItemMeta(meta);
        return bow;
    }
}
