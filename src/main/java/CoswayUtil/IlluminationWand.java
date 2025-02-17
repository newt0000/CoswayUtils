package CoswayUtil;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Light;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.UUID;

public class IlluminationWand implements Listener {

    private final CoswayUtil plugin;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 1000; // 1 second in milliseconds

    // Constructor to accept the main plugin instance
    public IlluminationWand(CoswayUtil plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static ItemStack getIlluminationWand() {
        ItemStack wand = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Illumination Wand");
            meta.setLore(java.util.Arrays.asList(ChatColor.GOLD + "Right-click to place Light Blocks!", ChatColor.GRAY + "Reach: 20 blocks"));
            meta.setUnbreakable(true);
            wand.setItemMeta(meta);
        }
        return wand;
    }

    @EventHandler
    public void onPlayerUseWand(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Ensure the player is holding the Illumination Wand
        if (item.getType() != Material.CARROT_ON_A_STICK || !item.hasItemMeta() ||
                !ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Illumination Wand")) {
            return;
        }

        // Cooldown check (except in Creative mode)
        if (player.getGameMode() != GameMode.CREATIVE) {
            if (cooldowns.containsKey(player.getUniqueId())) {
                long lastUse = cooldowns.get(player.getUniqueId());
                if (System.currentTimeMillis() - lastUse < COOLDOWN_TIME) {
                    player.sendMessage(ChatColor.RED + "The wand is on cooldown!");
                    return;
                }
            }
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }

        // Ray trace to detect block the player is looking at
        RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(), player.getEyeLocation().getDirection(), 20, FluidCollisionMode.NEVER
        );

        if (result == null || result.getHitBlock() == null || result.getHitBlockFace() == null) {
            player.sendMessage(ChatColor.RED + "You must aim at a block within range!");
            return;
        }

        Block targetBlock = result.getHitBlock(); // Get the exact hit block
        BlockFace hitFace = result.getHitBlockFace(); // Get the exact hit face

        // Get the correct placement location
        Block placeLocation = targetBlock.getRelative(hitFace);

        // Ensure we are not replacing a solid block
        if (!placeLocation.getType().isAir() && placeLocation.getType() != Material.WATER) {
            player.sendMessage(ChatColor.RED + "You cannot place a light inside a block!");
            return;
        }

        // Place the light block at the correct location
        placeLocation.setType(Material.LIGHT);
        Light lightBlock = (Light) placeLocation.getBlockData();
        lightBlock.setLevel(15); // Set light level to max
        placeLocation.setBlockData(lightBlock);

        // Particle and sound effects
        player.getWorld().spawnParticle(Particle.END_ROD, placeLocation.getLocation().add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, 0);
        player.getWorld().playSound(placeLocation.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.5f);

        player.sendMessage(ChatColor.GREEN + "Light placed!");
    }
}

