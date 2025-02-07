package CoswayUtil;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TotemShield implements Listener {
    private final JavaPlugin plugin;
    private final Random random = new Random();
    private static final double SAVE_CHANCE = 0.5; // 50% chance to not consume the Totem

    private final Set<Player> shieldedPlayers = new HashSet<>();
    private static final int SHIELD_DURATION = 100; // 5 seconds (100 ticks)

    public TotemShield(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Check if player is holding a totem
        if (!hasTotem(player)) return;

        if (shieldedPlayers.contains(player)) {
            event.setCancelled(true); // Cancel damage if shield is active
            return;
        }

        activateShield(player);
    }

    private boolean hasTotem(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        return offHand.getType() == Material.TOTEM_OF_UNDYING;
    }

    private void activateShield(Player player) {
        shieldedPlayers.add(player);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 50, 1, 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                shieldedPlayers.remove(player);
                consumeTotem(player);
            }
        }.runTaskLater(plugin, SHIELD_DURATION);
    }

    private void consumeTotem(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (random.nextDouble() < SAVE_CHANCE) {
            player.sendMessage("§6Your Totem activated, but was not consumed!");
        } else {
            if (offHand.getType() == Material.TOTEM_OF_UNDYING) {
                offHand.setAmount(offHand.getAmount() - 1);
                player.sendMessage("§6Your Totem activated, and was consumed!");
            }
        }
    }

    @EventHandler
    public void onTotemUse(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return; // Ensure it's a player using the Totem
        }
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return; // Do nothing for creative/spectator mode
        }

        // Random chance to prevent Totem consumption
        if (random.nextDouble() < SAVE_CHANCE) {
            event.setCancelled(true); // Prevents the Totem from being consumed
            player.setHealth(Math.min(player.getMaxHealth(), 10.0)); // Revives with half HP
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 0.5f);
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 50);
            player.sendMessage("§6Your Totem activated, but was not consumed!");

        } else {
            player.sendMessage("§cYour Totem was consumed as usual.");
        }
    }

}

