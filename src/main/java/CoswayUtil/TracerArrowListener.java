package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
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

    // When arrow is shot
    @EventHandler
    public void onArrowShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;

        // Require LOYALTY on the bow (your design)
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

                LivingEntity target = getNearestHostile(arrow);
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

    // Only target hostile mobs (Monsters)
    private LivingEntity getNearestHostile(Arrow arrow) {
        List<Entity> entities = arrow.getNearbyEntities(25, 25, 25);
        return entities.stream()
                .filter(e -> e instanceof Monster)                   // hostile only
                .map(e -> (LivingEntity) e)
                .min(Comparator.comparingDouble(
                        e -> e.getLocation().distanceSquared(arrow.getLocation())
                ))
                .orElse(null);
    }

    // When player switches hotbar slot (puts bow away)
    @EventHandler
    public void onHotbarChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        // Item they are switching FROM
        int previousSlot = event.getPreviousSlot();
        ItemStack oldItem = player.getInventory().getItem(previousSlot);

        // Only act if they were holding a loyalty bow
        if (oldItem == null
                || oldItem.getType() != org.bukkit.Material.BOW
                || !oldItem.containsEnchantment(Enchantment.LOYALTY)) {
            return;
        }

        // Kill all arrows in 150 block radius around player
        double radius = 150.0;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Arrow arrow) {
                arrow.remove();
            }
        }
    }
}
