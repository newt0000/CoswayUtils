package CoswayUtil;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WitherContract implements Listener {

    private final CoswayUtil plugin;

    public WitherContract(CoswayUtil plugin) {
        this.plugin = plugin;
    }

    // Event handler for Wither Skull placed on Soul Sand
    @EventHandler
    public void onWitherSkullPlace(BlockPlaceEvent event) {
        // Check if the block placed is Wither Skull on Soul Sand
        Block block = event.getBlockPlaced();
        Block blockBellow = block.getLocation().clone().subtract(0,1,0).getBlock();
        if (blockBellow.getType() == Material.SOUL_SAND && event.getItemInHand().getType() == Material.WITHER_SKELETON_SKULL) {
            Player player = event.getPlayer();
            World world = block.getWorld();
            plugin.serverMessage("&6Wither Contract has been Activated!");

            // Find a random hostile mob within 30 blocks
            LivingEntity target = findRandomHostileEntity(block.getLocation(), 30);
            if (target != null) {
                // Apply Wither Curse to the found mob
                applyWitherCurse(target);
                player.sendMessage("Wither Contract activated on " + target.getName());
            } else {
                player.sendMessage("No nearby hostile mobs found to mark.");
            }
        }
    }

    // Find a random hostile entity within the specified radius of the location
    private LivingEntity findRandomHostileEntity(Location location, double radius) {
        List<LivingEntity> hostileEntities = new ArrayList<>();
        for (Entity entity : location.getWorld().getEntities()) {
            if (entity instanceof LivingEntity && entity instanceof Monster) {
                LivingEntity livingEntity = (LivingEntity) entity;
                if (livingEntity.getLocation().distance(location) <= radius) {
                    hostileEntities.add(livingEntity);
                }
            }
        }
        if (hostileEntities.isEmpty()) {
            return null;
        }
        // Return a random entity from the list
        Random rand = new Random();
        return hostileEntities.get(rand.nextInt(hostileEntities.size()));
    }

    // Apply Wither Curse: Damage over time and handle spread when the mob dies
    private void applyWitherCurse(LivingEntity target) {
        // Damage over time effect (every second)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isDead()) {
                    target.getWorld().playEffect(target.getLocation().clone().subtract(0.5,0,0.5),Effect.END_GATEWAY_SPAWN,1);
                    cancel();  // Stop task if the mob is dead
                } else {
                    target.getWorld().playEffect(target.getLocation().clone().subtract(0,0,0),Effect.TRIAL_SPAWNER_BECOME_OMINOUS,1);
                    target.getWorld().spawnParticle(Particle.DUST, target.getLocation().clone().subtract(0,-2,0), 1, new Particle.DustOptions(Color.RED, 2));
                    target.damage(1);  // Deal 1 damage per tick (20 ticks = 1 second)
                }
            }
        }.runTaskTimer(plugin, 0, 10);  // Run every 10 ticks (0.5 second)

        // Listen for the mob's death and apply curse to nearby mobs
        target.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onEntityDeath(EntityDeathEvent deathEvent) {
                if (deathEvent.getEntity() == target) {
                    // When the marked mob dies, spread the curse
                    LivingEntity nextTarget = findRandomHostileEntity(target.getLocation(), 30);
                    if (nextTarget != null) {
                        applyWitherCurse(nextTarget);
                    }
                }
            }
        }, plugin);
    }
}