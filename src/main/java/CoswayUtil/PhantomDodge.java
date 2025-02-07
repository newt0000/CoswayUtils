package CoswayUtil;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.event.EventPriority;

public class PhantomDodge implements Listener {

    private final CoswayUtil plugin;

    public PhantomDodge(CoswayUtil plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        // Check if the entity being attacked is a player
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Check if player is sprinting and jumping
            if (player.isSprinting() && player.isFlying() && isValidPlayerMove(player)) {
                // Momentarily turn invisible
                makeInvisible(player);

                // Spawn the phantom version of the player
                spawnPhantom(player);

                // Prevent damage for a short period while the phantom is active
                event.setCancelled(true);
            }
        }
    }

    // Check if player is jumping, indicating Phantom Dodge
    private boolean isValidPlayerMove(Player player) {
        return player.getVelocity().getY() > 0.2; // Check if the player is jumping (based on Y velocity)
    }

    // Make the player invisible for 1 second
    private void makeInvisible(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 1)); // 1 second of invisibility
    }

    // Spawn the phantom version of the player
    private void spawnPhantom(Player player) {
        Location playerLocation = player.getLocation();
        Entity phantom = player.getWorld().spawnEntity(playerLocation, EntityType.PLAYER);

        // Set phantom's name and appearance to confuse enemies (could customize more here)
        phantom.setCustomName(player.getName() + " Phantom");
        phantom.setCustomNameVisible(true);
        phantom.setInvisible(true); // Make phantom invisible

        // Phantom will despawn after 1 second
        new BukkitRunnable() {
            @Override
            public void run() {
                phantom.remove(); // Remove the phantom after 1 second
            }
        }.runTaskLater(plugin, 20); // Delay of 1 second (20 ticks)
    }
}
