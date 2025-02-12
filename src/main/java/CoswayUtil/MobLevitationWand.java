package CoswayUtil;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class MobLevitationWand implements Listener {
    private final NamespacedKey wandKey = new NamespacedKey("coswayutil", "levitation_wand");
    private final HashMap<UUID, Entity> levitatedMobs = new HashMap<>();
    private final CoswayUtil plugin;

    public MobLevitationWand(CoswayUtil plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUseWand(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the player is holding the custom wand
        if (!isLevitationWand(item)) return;

        event.setCancelled(true); // Prevent default interaction

        UUID playerId = player.getUniqueId();

        // If already holding a mob, release it
        if (levitatedMobs.containsKey(playerId)) {
            releaseMob(player);
            return;
        }

        // Try to pick up a nearby mob
        Entity target = getNearestEntity(player);
        if (target != null) {
            levitatedMobs.put(playerId, target);
            target.setGravity(false);
            target.setInvulnerable(true);
            startLevitating(player, target);
        } else {
            player.sendMessage(ChatColor.RED + "No entity found to pick up!");
        }
    }


    private boolean isLevitationWand(ItemStack item) {
        if (item == null || item.getType() != Material.CARROT_ON_A_STICK || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(wandKey, PersistentDataType.STRING);
    }

    private Entity getNearestEntity(Player player) {
        return player.getNearbyEntities(5, 5, 5).stream()
                .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player))
                .findFirst()
                .orElse(null);
    }

    private void startLevitating(Player player, Entity entity) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!levitatedMobs.containsKey(player.getUniqueId()) || !player.isOnline()) {
                    releaseMob(player);
                    cancel();
                    return;
                }

                Vector direction = player.getLocation().getDirection().normalize().multiply(8);
                Location newLocation = player.getLocation().add(direction);
                World world = player.getWorld();

                // Ensure the mob is not placed inside a solid block
                if (newLocation.getBlock().getType().isSolid()) {
                    Location safeLocation = findSafeLocation(newLocation);
                    if (safeLocation != null) {
                        newLocation = safeLocation;
                    } else {
                        player.sendMessage(ChatColor.RED + "No safe location found for teleporting!");
                        return;
                    }
                }

                entity.teleport(newLocation);

                // Spawn particle chain
                for (double i = 0; i <= 1; i += 0.1) {
                    double x = player.getLocation().getX() + (newLocation.getX() - player.getLocation().getX()) * i;
                    double y = player.getLocation().getY() + (newLocation.getY() - player.getLocation().getY()) * i;
                    double z = player.getLocation().getZ() + (newLocation.getZ() - player.getLocation().getZ()) * i;
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, new Location(player.getWorld(), x, y, z), 2, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    /**
     * Finds the nearest safe location for teleportation by searching upwards but not going to max height.
     */
    private Location findSafeLocation(Location loc) {
        World world = loc.getWorld();
        int maxHeight = world.getMaxHeight();

        for (int y = loc.getBlockY(); y < maxHeight; y++) {
            Location checkLoc = new Location(world, loc.getX(), y, loc.getZ());
            if (!checkLoc.getBlock().getType().isSolid() && !checkLoc.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                return checkLoc; // Found a safe spot
            }
        }

        return null; // No valid space found
    }



    private void releaseMob(Player player) {
        Entity entity = levitatedMobs.remove(player.getUniqueId());
        if (entity != null) {
            entity.setGravity(true);
            entity.setInvulnerable(false);
        }
    }

    public static ItemStack createWand() {
        ItemStack wand = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Levitation Wand");
        meta.setLore(Collections.singletonList(ChatColor.GOLD + "Right Click near a mob to pick it up"));
        meta.getPersistentDataContainer().set(new NamespacedKey("coswayutil", "levitation_wand"), PersistentDataType.STRING, "true");
        wand.setItemMeta(meta);
        return wand;
    }
}
