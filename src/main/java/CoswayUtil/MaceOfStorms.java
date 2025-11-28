package CoswayUtil;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class MaceOfStorms implements Listener {

    private final JavaPlugin plugin;

    public MaceOfStorms(JavaPlugin plugin) {
        this.plugin = plugin;
        // You *can* register here, or do it from CoswayUtil.onEnable
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Factory method to create the actual Mace item.
     */
    public static ItemStack createMaceOfStorms() {
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "Mace of Storms");
            meta.setLore(List.of(
                    ChatColor.GOLD + "A weapon infused with lightning.",
                    ChatColor.GRAY + "Right-click while looking at a mob to smite it."
            ));
            meta.setUnbreakable(true);
            mace.setItemMeta(meta);
        }
        return mace;
    }

    @EventHandler
    public void onMaceUse(PlayerInteractEvent event) {
        // Ignore off-hand to avoid double trigger
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isMaceOfStorms(item)) {
            return;
        }

        // Optional: cancel normal mace usage (so it only does our effect)
        event.setCancelled(true);

        LivingEntity target = getTargetEntity(player, 20);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "You are not looking at a valid target!");
            return;
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);

        // Shoot particles from player to target
        shootParticleBeam(player.getLocation().add(0, 1.5, 0), target.getLocation().add(0, 1, 0));

        // Circle the target and strike with lightning
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 40 || target.isDead()) { // 2 seconds @ 2-tick interval
                    target.getWorld().strikeLightning(target.getLocation());
                    cancel();
                    return;
                }

                spawnCirclingParticles(target.getLocation().add(0, 0.5, 0), ticks);
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private boolean isMaceOfStorms(ItemStack item) {
        if (item == null || item.getType() != Material.MACE) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        String stripped = ChatColor.stripColor(meta.getDisplayName());
        return stripped.equalsIgnoreCase("Mace of Storms");
    }

    private LivingEntity getTargetEntity(Player player, double range) {
        List<Entity> nearbyEntities = player.getNearbyEntities(range, range, range);
        Vector lookDir = player.getLocation().getDirection().normalize();

        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity) || entity == player) continue;

            Vector toEntity = entity.getLocation().toVector()
                    .subtract(player.getEyeLocation().toVector())
                    .normalize();

            // dot product close to 1 means nearly in front
            if (lookDir.dot(toEntity) > 0.98) {
                return (LivingEntity) entity;
            }
        }
        return null;
    }

    private void shootParticleBeam(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);

        for (double i = 0; i < distance; i += 0.3) {
            Location point = from.clone().add(direction.clone().multiply(i));
            from.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, point, 3, 0.05, 0.05, 0.05, 0);
        }
    }

    private void spawnCirclingParticles(Location location, int time) {
        double angle = Math.toRadians(time * 9); // rotation speed
        for (int i = 0; i < 6; i++) {
            double offsetAngle = angle + (i * Math.PI / 3);
            double offsetX = Math.cos(offsetAngle) * 1.5;
            double offsetZ = Math.sin(offsetAngle) * 1.5;

            spawnImpactParticles(location.clone().add(offsetX, 0.1, offsetZ));
        }
    }

    private void spawnImpactParticles(Location location) {
        for (int i = 0; i < 4; i++) {
            location.getWorld().spawnParticle(
                    Particle.CRIT,
                    location,
                    1,
                    0.1, 0.1, 0.1, 0
            );
        }
    }
}
