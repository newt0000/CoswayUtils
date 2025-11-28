//in MaceOfStorms.java
package CoswayUtil;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import CoswayUtil.CoswayUtil;

import java.util.List;

public class MaceOfStorms implements Listener {

    private final CoswayUtil plugin;

    public MaceOfStorms(CoswayUtil plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMaceUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isMaceOfStorms(item)) {
            return;
        }

        LivingEntity target = getTargetEntity(player, 20);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "You are not looking at a valid target!");
            return;
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);

        // Shoot particles from player to target
        shootParticleBeam(player.getLocation().add(0, 1.5, 0), target.getLocation());

        // Circle the target and strike with lightning
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 40) { // Runs for 2 seconds
                    target.getWorld().strikeLightning(target.getLocation());
                    cancel();
                    return;
                }

                spawnCirclingParticles(target.getLocation(), ticks);
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0, 2);
    }
    private void spawnImpactParticles(Location location) {
        for (int i = 0; i < 20; i++) {
            double angle = Math.toRadians((360.0 / 20) * i);
            double offsetX = Math.cos(angle) * 1.5;
            double offsetZ = Math.sin(angle) * 1.5;

            location.getWorld().spawnParticle(
                    Particle.CRIT,
                    location.clone().add(offsetX, 0.5, offsetZ),
                    1,
                    0, 0, 0, 0
            );
        }
    }

    private boolean isMaceOfStorms(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SHOVEL) { // Mace-like weapon
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Mace of Storms");
    }

    private LivingEntity getTargetEntity(Player player, double range) {
        List<Entity> nearbyEntities = player.getNearbyEntities(range, range, range);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player) {
                Vector direction = player.getLocation().getDirection();
                Vector entityDirection = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                if (direction.dot(entityDirection) > 0.98) { // Check if the entity is in line of sight
                    return (LivingEntity) entity;
                }
            }
        }
        return null;
    }

    private void shootParticleBeam(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);
        for (double i = 0; i < distance; i += 0.5) {
            Location point = from.clone().add(direction.clone().multiply(i));
            from.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, point, 2, 0.1, 0.1, 0.1, 0);
        }
    }

    private void spawnCirclingParticles(Location location, int time) {
        double angle = Math.toRadians(time * 9); // Adjust rotation speed
        for (int i = 0; i < 6; i++) { // 6 particles around the entity
            double offsetX = Math.cos(angle + (i * Math.PI / 3)) * 1.5;
            double offsetZ = Math.sin(angle + (i * Math.PI / 3)) * 1.5;
            spawnImpactParticles( location.clone().add(offsetX, 0.5, offsetZ));
        }
    }
}



//in coswayutil.java

//##as command



