package CoswayUtil;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LaunchStick implements Listener {
    private final CoswayUtil plugin;
    private final Set<Player> noFallPlayers = new HashSet<>();
    private final double LAUNCH_POWER = 4.0; // Adjust this to change launch strength

    public LaunchStick(CoswayUtil plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Event handler for right-clicking with the launch stick
    @EventHandler
    public void onPlayerUseLaunchStick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isLaunchStick(item)) return;

        event.setCancelled(true);

        // Get the direction the player is looking and apply launch
        Vector direction = player.getLocation().getDirection().normalize().multiply(LAUNCH_POWER);
        direction.setY(direction.getY() + 0.5); // Add slight upward boost
        player.setVelocity(direction);

        // Add player to no-fall damage list
        noFallPlayers.add(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_JUMP,10,0);
        player.getWorld().playEffect(player.getLocation(), Effect.TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS,1);
        //player.sendMessage(ChatColor.GREEN + "You launched yourself forward!");
    }

    // Prevent fall damage for launched players
    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (noFallPlayers.contains(player) && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
                noFallPlayers.remove(player); // Remove player once they hit the ground
            }
        }
    }

    // Detect when a player lands on the ground and reset no-fall
    @EventHandler
    public void onPlayerLand(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (noFallPlayers.contains(player)) {
            if (player.isOnGround()) {
                noFallPlayers.remove(player);
            }
        }
    }

    // Check if an item is the launch stick
    private boolean isLaunchStick(ItemStack item) {
        if (item == null || item.getType() != Material.CARROT_ON_A_STICK || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Launch Stick");
    }

    // Method to create the Launch Stick
    public static ItemStack createLaunchStick() {
        ItemStack item = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Launch Stick");
            meta.setLore(Collections.singletonList(ChatColor.GOLD + "Click to launch forward"));
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            item.setItemMeta(meta);
        }
        return item;
    }
}

