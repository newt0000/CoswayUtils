package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class BowTrajectoryVisualizer implements Listener {

    private final JavaPlugin plugin;
    private final Map<Player, BukkitRunnable> activeTrajectories = new HashMap<>();
    private static final double GRAVITY = 0.08;

    public BowTrajectoryVisualizer(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDrawBow(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !item.getType().toString().contains("BOW")) return;

        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            startTrajectory(player);
        }
    }

    @EventHandler
    public void onArrowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            stopTrajectory(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopTrajectory(event.getPlayer());
    }

    private void startTrajectory(Player player) {
        stopTrajectory(player);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !player.isHandRaised()) {
                    stopTrajectory(player);
                    return;
                }

                Location eye = player.getEyeLocation();
                Vector dir = eye.getDirection().normalize().multiply(3.0);
                Location pos = eye.clone();

                double velX = dir.getX();
                double velY = dir.getY();
                double velZ = dir.getZ();

                for (int i = 0; i < 40; i++) {
                    pos.add(velX, velY, velZ);
                    player.getWorld().spawnParticle(Particle.CRIT, pos, 1, 0, 0, 0, 0);

                    velY -= GRAVITY;
                    velX *= 0.99;
                    velY *= 0.99;
                    velZ *= 0.99;
                }
            }
        };

        task.runTaskTimer(plugin, 0L, 2L);
        activeTrajectories.put(player, task);
    }

    private void stopTrajectory(Player player) {
        BukkitRunnable task = activeTrajectories.remove(player);
        if (task != null) {
            task.cancel();
        }
    }
}
