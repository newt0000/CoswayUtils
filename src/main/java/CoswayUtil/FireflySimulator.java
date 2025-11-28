package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Random;

public class FireflySimulator {
    private final CoswayUtil plugin;
    private final Random random = new Random();
    private final int radius = 10; // Radius around the player where fireflies will appear
    private final int fireflyCount = 5; // Number of fireflies per player

    public FireflySimulator(CoswayUtil plugin) {
        this.plugin = plugin;
        startSimulation();
    }

    public void startSimulation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().getTime() == 13000) { // Only at night
                        player.stopAllSounds();
                        player.playSound(player.getLocation().clone().add(0,100,0), Sound.MUSIC_DISC_MALL,10,1);
                        statMsg(player,"&6time for bed");
                    }
                    if (player.getWorld().getTime() == 1137) { // Only at night
                        player.stopAllSounds();
                        player.playSound(player.getLocation().clone().add(0,100,0), Sound.MUSIC_DISC_OTHERSIDE,10,1);
                        statMsg(player,"&eRize and Shine!");
                    }
                }
            }
        }.runTaskTimer(plugin,0L,1);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().getTime() >= 13000) { // Only at night
                        spawnFireflies(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Runs every 10 ticks (0.5 seconds)
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
    public void statMsg(Player player,String msg) {
        player.sendMessage(ColorKey("&7[&dCosway Util&7] "+msg));
    }
    private void spawnFireflies(Player player) {
        Location playerLocation = player.getLocation();
        for (int i = 0; i < fireflyCount; i++) {
            double offsetX = (random.nextDouble() - 0.5) * radius * 2;
            double offsetZ = (random.nextDouble() - 0.5) * radius * 2;
            double offsetY = random.nextDouble() * 3 + 1; // Fireflies hover slightly above the ground

            Location fireflyLocation = playerLocation.clone().add(offsetX, offsetY, offsetZ);
            player.getWorld().spawnParticle(Particle.FIREFLY, fireflyLocation, 0, 0, 0.01, 0, 1); // Small movement effect
        }
    }
}
