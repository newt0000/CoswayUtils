package CoswayUtil;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class PlayerFireworkBurst implements Listener {
    private final JavaPlugin plugin;
    private final Random random = new Random();

    public PlayerFireworkBurst(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        spawnBurst(event.getPlayer().getLocation());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        spawnBurst(event.getPlayer().getLocation());
    }

    private void spawnBurst(Location location) {
        Location center = location.clone().add(0, 1, 0);

        // Bright white flash in the center
        center.getWorld().spawnParticle(
                Particle.FLASH,
                center,
                1,
                0, 0, 0,
                0,
                Color.WHITE
        );

        // Rainbow spherical burst
        Color[] colors = {
                Color.RED,
                Color.ORANGE,
                Color.YELLOW,
                Color.LIME,
                Color.AQUA,
                Color.BLUE,
                Color.PURPLE,
                Color.FUCHSIA
        };

        for (int i = 0; i < 90; i++) {
            double theta = random.nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * random.nextDouble() - 1);
            double radius = 0.15 + random.nextDouble() * 1.35;

            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.cos(phi);
            double z = radius * Math.sin(phi) * Math.sin(theta);

            Color color = colors[i % colors.length];

            Particle.DustOptions dust = new Particle.DustOptions(color, 1.5f);

            center.getWorld().spawnParticle(
                    Particle.DUST,
                    center.clone().add(x, y, z),
                    1,
                    0, 0, 0,
                    0,
                    dust
            );
        }

        // Small white sparkle particles around the explosion
        center.getWorld().spawnParticle(
                Particle.FIREWORK,
                center,
                25,
                0.6, 0.6, 0.6,
                0.08
        );
    }
}