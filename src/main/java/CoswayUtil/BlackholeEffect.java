package CoswayUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class BlackholeEffect extends BukkitRunnable {

    private final CoswayUtil plugin;
    private final Map<Entity, Integer> pullTimer = new HashMap<>();

    public BlackholeEffect(CoswayUtil plugin) {
        this.plugin = plugin;
        startTask();
        //Bukkit.broadcastMessage(ChatColor.GREEN+"this is proof the blackhole class is registered properly");
    }

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Monster && "Blackhole".equalsIgnoreCase(ChatColor.stripColor(entity.getCustomName()))) {
                    Location blackholeLocation = entity.getLocation();
                    ((LivingEntity) entity).setAI(false);
                    entity.setInvulnerable(true);
                    entity.setGravity(false);
                    ((Monster) entity).setCollidable(false);

                    entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 4, 0);
                    entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 4, 0);
                    spawnBlackholeParticles(blackholeLocation);

                    List<Entity> nearbyEntities = entity.getNearbyEntities(20, 20, 20);
                    for (Entity nearbyEntity : nearbyEntities) {
                        if (nearbyEntity instanceof Monster && nearbyEntity != entity) {
                            pullEntityTowards(nearbyEntity, blackholeLocation);
                            pullTimer.putIfAbsent(nearbyEntity, 0);
                        }
                    }

                    pullTimer.replaceAll((mob, time) -> time + 1);

                    Iterator<Map.Entry<Entity, Integer>> iterator = pullTimer.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Entity, Integer> entry = iterator.next();
                        Entity mob = entry.getKey();

                        if (entry.getValue() >= 100) {
                            new BukkitRunnable() {
                                int ticks = 0;
                                final int maxTicks = 100;

                                @Override
                                public void run() {
                                    if (ticks >= maxTicks || mob.isDead()) {
                                        mob.getWorld().playEffect(mob.getLocation(), Effect.END_GATEWAY_SPAWN, 1);
                                        mob.remove();
                                        cancel();
                                        return;
                                    }
                                    Location currentLocation = mob.getLocation();
                                    mob.teleport(currentLocation.add(0, 0.1, 0)); // Move entity up
                                    ticks++;
                                }
                            }.runTaskTimer(plugin, 0, 1);
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }


    private void pullEntityTowards(Entity entity, Location targetLocation) {
        Vector direction = targetLocation.toVector().subtract(entity.getLocation().toVector()).normalize();
        entity.setVelocity(direction.multiply(0.3));
    }

    private void spawnBlackholeParticles(Location location) {
        for (int i = 0; i < 20; i++) {
            double angle = Math.toRadians(i * 18);
            double x = Math.cos(angle) * 2;
            double z = Math.sin(angle) * 2;
            location.getWorld().spawnParticle(Particle.PORTAL,location,2);
            location.getWorld().spawnParticle(Particle.PORTAL, location.clone().add(x, 0.5, z), 1, 0, 0, 0, 0);
        }
    }

    public void startTask() {
        this.runTaskTimer(plugin, 0, 3);
    }
}
