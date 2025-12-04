package CoswayUtil;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CorruptionShockwaveUtil {

    private CorruptionShockwaveUtil() {}

    public static void startCorruptionShockwave(JavaPlugin plugin, Location center, boolean canChainFromNonHostiles) {
        World world = center.getWorld();
        if (world == null) return;

        Set<UUID> triggeredNonHostiles = new HashSet<>();

        new BukkitRunnable() {
            double radius = 0.0;
            double lastRadius = 0.0;
            final double maxRadius = 30.0;

            @Override
            public void run() {
                if (radius > maxRadius) {
                    cancel();
                    return;
                }

                double y = center.getY() + 0.1;
                int points = Math.max(32, (int) (radius * 10));

                // Visual ring
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = center.getX() + radius * Math.cos(angle);
                    double z = center.getZ() + radius * Math.sin(angle);

                    world.spawnParticle(
                            Particle.DUST,
                            x, y, z,
                            5,
                            0, 0, 0,
                            0,
                            new Particle.DustOptions(Color.fromRGB(20, 0, 0), 1.6f),
                            true
                    );
                    world.spawnParticle(
                            Particle.DUST,
                            x, y, z,
                            5,
                            0, 0, 0,
                            0,
                            new Particle.DustOptions(Color.fromRGB(20, 0, 10), 1.6f),
                            true
                    );
                    world.spawnParticle(
                            Particle.FLAME,
                            x, y + 0.2, z,
                            0,
                            0, 0, 0,
                            0
                    );
                }

                double inner = Math.max(0.0, lastRadius - 0.8);
                double outer = radius + 0.8;

                double searchRadius = Math.min(maxRadius, outer + 1.0);
                for (Entity entity : world.getNearbyEntities(center, searchRadius, 8, searchRadius)) {
                    if (!(entity instanceof LivingEntity living)) continue;
                    if (living.isDead()) continue;
                    if (living instanceof ArmorStand) continue;

                    double dist = living.getLocation().distance(center);
                    if (dist < inner || dist > outer) continue;

                    boolean isHostile = living instanceof Enemy;

                    // 1) Hostiles: full impact
                    if (isHostile) {
                        Vector dir = living.getLocation().toVector().subtract(center.toVector());
                        dir.setY(0);
                        if (dir.lengthSquared() == 0) continue;

                        dir.normalize();
                        dir.setY(0.5);
                        living.setVelocity(dir.multiply(2.8));

                        living.addPotionEffect(new PotionEffect(
                                PotionEffectType.WITHER,
                                100,
                                255,
                                false,
                                true,
                                true
                        ));
                        living.addPotionEffect(new PotionEffect(
                                PotionEffectType.STRENGTH,
                                100,
                                255,
                                false,
                                true,
                                true
                        ));

                        world.spawnParticle(
                                Particle.LARGE_SMOKE,
                                living.getLocation().add(0, 0.8, 0),
                                12,
                                0.3, 0.6, 0.3,
                                0.02
                        );

                        continue;
                    }

                    // 2) Non-hostiles: only spawn subwaves (in main wave)
                    if (!canChainFromNonHostiles) {
                        continue;
                    }

                    UUID id = living.getUniqueId();
                    if (triggeredNonHostiles.contains(id)) continue;

                    if (living.getLocation().distanceSquared(center) > 150 * 150) continue;

                    triggeredNonHostiles.add(id);

                    Location subCenter = living.getLocation().clone();

                    world.spawnParticle(
                            Particle.LARGE_SMOKE,
                            subCenter.clone().add(0, 0.5, 0),
                            15,
                            0.3, 0.6, 0.3,
                            0.02
                    );
                    world.playSound(
                            subCenter,
                            Sound.ENTITY_PHANTOM_SWOOP,
                            0.7f,
                            0.4f
                    );

                    // Start subwave from non-hostile, no further chaining
                    startCorruptionShockwave(plugin, subCenter, false);
                }

                if (radius == 0.0) {
                    world.playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 0.3f);
                    world.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 0.7f, 0.3f);
                    world.playSound(center, Sound.BLOCK_CONDUIT_DEACTIVATE, 0.7f, 0.3f);
                }

                lastRadius = radius;
                radius += 1.0;
            }
        }.runTaskTimer(plugin, 10L, 3L);
    }
}
