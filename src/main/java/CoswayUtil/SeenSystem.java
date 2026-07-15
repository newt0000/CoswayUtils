package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SeenSystem {


    private final CoswayUtil plugin;


    public SeenSystem(CoswayUtil plugin) {
        this.plugin = plugin;

        startTask();
    }



    private void startTask() {

        new BukkitRunnable() {

            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {


                    if (player.hasPermission("coswayutil.seen")) {


                        PotionEffect glow =
                                player.getPotionEffect(
                                        PotionEffectType.GLOWING
                                );


                        // Reapply if missing or close to expiring
                        if (glow == null ||
                                glow.getDuration() < 200) {


                            player.addPotionEffect(
                                    new PotionEffect(
                                            PotionEffectType.GLOWING,
                                            Integer.MAX_VALUE,
                                            0,
                                            false,
                                            false,
                                            false
                                    )
                            );
                        }


                    } else {


                        // Remove effect if permission was removed
                        if (player.hasPotionEffect(
                                PotionEffectType.GLOWING
                        )) {

                            player.removePotionEffect(
                                    PotionEffectType.GLOWING
                            );
                        }
                    }
                }

            }

        }.runTaskTimer(
                plugin,
                0L,
                100L
        ); // Runs every 5 seconds
    }
}