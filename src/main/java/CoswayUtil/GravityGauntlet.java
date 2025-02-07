package CoswayUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GravityGauntlet implements Listener {
    private final CoswayUtil plugin; // Reference to the main plugin
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_TIME = 15 * 1000; // 5 seconds in milliseconds

    public GravityGauntlet(CoswayUtil plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the player is holding the Gravity Gauntlet
        if (item.getType() == Material.NETHERITE_HOE && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Gravity Gauntlet")) {
                event.setCancelled(true);

                // Cooldown check
                if (cooldowns.containsKey(player.getUniqueId())) {
                    long timeLeft = (cooldowns.get(player.getUniqueId()) + COOLDOWN_TIME) - System.currentTimeMillis();
                    if (timeLeft > 0) {
                        player.sendMessage(ColorKey("&cThe Gravity Gauntlet is on cooldown for " + (timeLeft / 1000) + "s!"));
                        return;
                    }
                }
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

                // Shift-click = Throw away, Normal click = Pull in
                if (player.isSneaking()) {
                    throwEntitiesAway(player);
                } else {
                    pullEntitiesTowards(player);
                }
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
    private void pullEntitiesTowards(Player player) {
        Location playerLoc = player.getLocation();
        double radius = 10.0;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                Vector direction = playerLoc.toVector().subtract(entity.getLocation().toVector()).normalize().multiply(1.5);
                entity.setVelocity(direction);
                player.getWorld().playSound(playerLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.getWorld().spawnParticle(Particle.PORTAL, entity.getLocation(), 30);
            }
        }
    }

    private void throwEntitiesAway(Player player) {
        Location playerLoc = player.getLocation();
        double radius = 10.0;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                Vector direction = entity.getLocation().toVector().subtract(playerLoc.toVector()).normalize().multiply(2.5);
                direction.setY(direction.getY() + 1.0); // Give it an upward boost
                entity.setVelocity(direction);
                player.getWorld().playSound(playerLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);
                player.getWorld().spawnParticle(Particle.EXPLOSION, entity.getLocation(), 20);
            }
        }
    }
}