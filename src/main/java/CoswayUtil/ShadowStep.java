package CoswayUtil;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ShadowStep implements Listener {

    private final CoswayUtil plugin;

    public ShadowStep(CoswayUtil plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerUseEnderPearl(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        // Check if player is sneaking and right-clicking with Ender Pearl
        if (player.isSneaking() && event.getAction().toString().contains("RIGHT_CLICK") &&
                player.getInventory().getItemInMainHand().getType() == Material.ENDER_PEARL) {

            // Cancel the event to prevent the pearl from being thrown
            event.setCancelled(true);

            // Find the nearest enemy (Player or Hostile Mob)
            Entity nearestEnemy = findNearestEnemy(player);
            if (nearestEnemy != null) {
                teleportBehindEnemy(player, nearestEnemy);
                applyBlindnessEffect(nearestEnemy);
            }
        }
    }

    private Entity findNearestEnemy(Player player) {
        Entity nearestEnemy = null;
        double closestDistance = 10.0;
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity != player && isHostile(entity)) {
                double distance = player.getLocation().distance(entity.getLocation());
                if (distance <= closestDistance) {
                    closestDistance = distance;
                    nearestEnemy = entity;
                }
            }
        }
        return nearestEnemy;
    }

    // Check if the entity is a hostile mob or player
    private boolean isHostile(Entity entity) {
        if (entity instanceof Player) {
            return true; // Players are considered hostile for this mechanic
        }
        if (entity instanceof Monster) {
            return true; // Check if the entity is a hostile mob (Monster subclass)
        }
        return false;
    }

    private void teleportBehindEnemy(Player player, Entity enemy) {
        if (enemy == null) return;
        Location enemyLocation = enemy.getLocation();
        Vector direction = enemyLocation.getDirection().normalize();
        Location teleportLocation = enemyLocation.subtract(direction.multiply(2)); // Teleport 2 blocks behind
        player.teleport(teleportLocation);
    }

    private void applyBlindnessEffect(Entity enemy) {
        if (enemy instanceof Player) {
            Player targetPlayer = (Player) enemy;
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1)); // 5 seconds of blindness
        } else if (enemy instanceof LivingEntity) {
            // If it's a mob, apply blindness
            LivingEntity mob = (LivingEntity) enemy;
            mob.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1)); // 5 seconds of blindness
        }
    }
}