package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;

public class TracerArrowListener implements Listener {

    private final JavaPlugin plugin;

    public TracerArrowListener(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onArrowShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;

        if (!player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.LOYALTY)) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (arrow.isDead() || arrow.isInBlock()) {
                    cancel();
                    return;
                }

                LivingEntity target = getNearestEntity(arrow);
                if (target == null) return;

                Vector newVel = target.getLocation().toVector()
                        .subtract(arrow.getLocation().toVector())
                        .normalize()
                        .multiply(3.0);

                arrow.setVelocity(newVel);
                arrow.getWorld().spawnParticle(Particle.CRIT, arrow.getLocation(), 3, 0.1, 0.1, 0.1, 0);
                arrow.getWorld().playSound(arrow.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.4f, 1.5f);
            }
        }.runTaskTimer(plugin, 5L, 2L);
    }

    private LivingEntity getNearestEntity(Arrow arrow) {
        List<Entity> entities = arrow.getNearbyEntities(25, 25, 25);
        return entities.stream()
                .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
                .map(e -> (LivingEntity) e)
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(arrow.getLocation())))
                .orElse(null);
    }
}
