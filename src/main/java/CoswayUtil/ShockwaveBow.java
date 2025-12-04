package CoswayUtil;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShockwaveBow implements Listener, CommandExecutor {

    private final JavaPlugin plugin;
    private final NamespacedKey arrowKey;
    private final NamespacedKey bowKey;

    // Track charging particle tasks per player
    private final Map<UUID, Integer> chargeTasks = new HashMap<>();

    public ShockwaveBow(JavaPlugin plugin) {
        this.plugin = plugin;
        this.arrowKey = new NamespacedKey(plugin, "shockwave_arrow");
        this.bowKey = new NamespacedKey(plugin, "shockwave_bow");
    }

    // --------------------------------------------------
    // Command: /shockwavebow
    // --------------------------------------------------
    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!cmd.getName().equalsIgnoreCase("shockwavebow")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        ItemStack bow = createShockwaveBow();
        player.getInventory().addItem(bow);
        player.sendMessage(ChatColor.DARK_PURPLE + "You feel unstable power surge into your hands...");
        return true;
    }

    // --------------------------------------------------
    // Create the special bow
    // --------------------------------------------------
    public ItemStack createShockwaveBow() {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "Shockwave Bow");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Draw back to channel unstable energy.",
                    ChatColor.GRAY + "Release to unleash a corruption shockwave."
            ));
            meta.addEnchant(Enchantment.POWER, 5, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

            // Mark it in PDC
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(bowKey, PersistentDataType.BYTE, (byte) 1);

            bow.setItemMeta(meta);
        }
        return bow;
    }

    private boolean isShockwaveBow(ItemStack stack) {
        if (stack == null || stack.getType() != Material.BOW || !stack.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(bowKey, PersistentDataType.BYTE)) {
            return true;
        }
        // Fallback on name, just in case
        return meta.hasDisplayName()
                && ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Shockwave Bow");
    }

    // --------------------------------------------------
    // Charging effect while drawing the bow (right-click)
    // --------------------------------------------------
    @EventHandler
    public void onBowUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack inHand = player.getInventory().getItemInMainHand();

        if (!isShockwaveBow(inHand)) {
            return;
        }

        // Start charging on right-click if not already
        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                startChargingEffect(player);
                break;
            default:
                break;
        }
    }

    private void startChargingEffect(Player player) {
        UUID id = player.getUniqueId();
        if (chargeTasks.containsKey(id)) {
            return; // already charging
        }

        int taskId = new BukkitRunnable() {
            double t = 0;

            @Override
            public void run() {
                // Stop if player no longer has the bow in hand or is offline
                if (!player.isOnline()
                        || !isShockwaveBow(player.getInventory().getItemInMainHand())) {
                    cancel();
                    chargeTasks.remove(id);
                    return;
                }

                Location loc = player.getLocation().add(0, 1.2, 0);
                World world = loc.getWorld();
                if (world == null) {
                    cancel();
                    chargeTasks.remove(id);
                    return;
                }

                // Sucking energy spiral into the player
                double radius = 1.2 - (t * 0.03);
                if (radius < 0.3) radius = 0.3;

                int points = 16;
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points + t * 0.25;
                    double x = loc.getX() + radius * Math.cos(angle);
                    double z = loc.getZ() + radius * Math.sin(angle);
                    double y = loc.getY() + 0.2 * Math.sin(t * 0.3 + i);

                    world.spawnParticle(
                            Particle.DUST,
                            x, y, z,
                            2,
                            0, 0, 0,
                            0,
                            new Particle.DustOptions(Color.fromRGB(90, 0, 140), 0.7f),
                            true
                    );
                    world.spawnParticle(
                            Particle.DUST,
                            x, y, z,
                            1,
                            0, 0, 0,
                            0,
                            new Particle.DustOptions(Color.fromRGB(20, 0, 40), 0.5f),
                            true
                    );
                }

                if (t % 10 == 0) {
                    world.playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.4f, 0.4f + (float) (t * 0.01));
                }

                t += 1;
            }
        }.runTaskTimer(plugin, 0L, 2L).getTaskId();

        chargeTasks.put(id, taskId);
    }

    private void stopChargingEffect(Player player) {
        UUID id = player.getUniqueId();
        Integer taskId = chargeTasks.remove(id);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    // --------------------------------------------------
    // On shoot: release burst, mark arrow, start trail
    // --------------------------------------------------
    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack bow = event.getBow();
        if (!isShockwaveBow(bow)) return;

        // Stop charging loop
        stopChargingEffect(player);

        Projectile proj = (Projectile) event.getProjectile();
        if (!(proj instanceof Arrow arrow)) return;

        // Mark arrow in PDC
        PersistentDataContainer pdc = arrow.getPersistentDataContainer();
        pdc.set(arrowKey, PersistentDataType.BYTE, (byte) 1);

        // Release burst at player
        playReleaseBurst(player);

        // Start arrow trail
        startArrowTrail(arrow);
    }

    private void playReleaseBurst(Player player) {
        Location loc = player.getLocation().add(0, 1.0, 0);
        World world = loc.getWorld();
        if (world == null) return;

        // Spherical burst
        for (double r = 0.3; r <= 2.0; r += 0.3) {
            int points = 24;
            for (int i = 0; i < points; i++) {
                double theta = 2 * Math.PI * (i / (double) points);
                double phi = Math.acos(2 * (i / (double) points) - 1); // rough spread

                double x = r * Math.sin(phi) * Math.cos(theta);
                double y = r * Math.cos(phi);
                double z = r * Math.sin(phi) * Math.sin(theta);

                world.spawnParticle(
                        Particle.DUST,
                        loc.clone().add(x, y, z),
                        4,
                        0, 0, 0,
                        0,
                        new Particle.DustOptions(Color.fromRGB(140, 0, 200), 0.8f),
                        true
                );
            }
        }

        world.playSound(loc, Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.4f);
        world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 0.3f);
        world.playSound(loc, Sound.BLOCK_CONDUIT_ACTIVATE, 0.8f, 0.5f);
    }

    private void startArrowTrail(Arrow arrow) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arrow.isDead() || arrow.isInBlock() || arrow.getWorld() == null) {
                    cancel();
                    return;
                }

                Location loc = arrow.getLocation();
                World world = loc.getWorld();

                world.spawnParticle(
                        Particle.DUST,
                        loc,
                        3,
                        0.15, 0.15, 0.15,
                        0,
                        new Particle.DustOptions(Color.fromRGB(120, 0, 200), 0.5f),
                        true
                );
                world.spawnParticle(
                        Particle.PORTAL,
                        loc,
                        4,
                        0.1, 0.1, 0.1,
                        0
                );
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // --------------------------------------------------
    // On arrow land: crying obsidian blast
    // --------------------------------------------------
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();
        if (!(proj instanceof Arrow arrow)) return;

        PersistentDataContainer pdc = arrow.getPersistentDataContainer();
        if (!pdc.has(arrowKey, PersistentDataType.BYTE)) {
            return; // Not our special arrow
        }

        Location hitLoc = arrow.getLocation();
        World world = hitLoc.getWorld();
        if (world == null) return;

        // Make sure it's slightly above ground
        hitLoc = hitLoc.clone().add(0, 0.2, 0);

        // Crying obsidian falling blocks blast
        blastCryingObsidian(hitLoc);

        // Creepy powerful sound to everyone nearby (200 block radius)
        world.playSound(hitLoc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 2.0f, 0.3f);
        world.playSound(hitLoc, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.5f, 0.5f);
        world.playSound(hitLoc, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0f, 0.4f);

        // Extra particles
        world.spawnParticle(Particle.SONIC_BOOM, hitLoc, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.LARGE_SMOKE, hitLoc, 40, 1.2, 0.6, 1.2, 0.02);

        arrow.remove();
    }

    private void blastCryingObsidian(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        int count = 18;
        CorruptionShockwaveUtil.startCorruptionShockwave(plugin, center, true);
        for (int i = 0; i < count; i++) {
            Vector dir = randomDirection().multiply(0.7 + (Math.random() * 0.7));
            Location spawnLoc = center.clone().add(0, 0.3, 0);

            FallingBlock fb = world.spawnFallingBlock(
                    spawnLoc,
                    Material.CRYING_OBSIDIAN.createBlockData()
            );
            fb.setDropItem(false);
            fb.setHurtEntities(false);
            fb.setVelocity(dir);
        }
    }

    private Vector randomDirection() {
        double theta = Math.random() * 2 * Math.PI;
        double y = Math.random() * 0.6 + 0.3; // upward bias
        double x = Math.cos(theta);
        double z = Math.sin(theta);
        Vector v = new Vector(x, y, z);
        return v.normalize();
    }
}
