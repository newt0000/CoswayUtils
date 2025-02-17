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

        if (item.getType() != Material.CARROT_ON_A_STICK || !item.hasItemMeta() ||
                !ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Illumination Wand")) {
            return;
        }

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

        RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(), player.getEyeLocation().getDirection(), 20, FluidCollisionMode.NEVER
        );

        if (result == null || result.getHitBlock() == null) {
            player.sendMessage(ChatColor.RED + "You must aim at a block within range!");
            return;
        }

        Block targetBlock = player.getTargetBlockExact(20, FluidCollisionMode.NEVER); // Get the block the player is looking at (within 20 blocks)

// Ensure a valid block is being targeted
        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must look at a solid block to place light!");
            return;
        }

// Get the correct face where the block should be placed
        BlockFace hitFace = event.getBlockFace();
        if (hitFace == null) {
            player.sendMessage(ChatColor.RED + "Couldn't determine block face!");
            return;
        }

        // Get the exact block where the light should be placed
        Block placeLocation = targetBlock.getRelative(hitFace);
        Location safeSpot = placeLocation.getLocation();

// Ensure we are not replacing a solid block
        if (!placeLocation.getType().isAir() && placeLocation.getType() != Material.WATER) {

            Block checkUp = placeLocation.getRelative(BlockFace.UP);
            Block checkDown = placeLocation.getRelative(BlockFace.DOWN);

            if (!checkUp.getType().isAir() && checkUp.getType() != Material.WATER) {
                if (!checkDown.getType().isAir() && checkDown.getType() != Material.WATER) {
                    player.sendMessage(ChatColor.RED + "You cannot place a light inside a block!");
                    return;
                } else {
                    safeSpot = checkDown.getLocation();
                }
            } else {
                safeSpot = checkUp.getLocation();
            }
        }

        // Place the light block at the correct location
        safeSpot.getBlock().setType(Material.LIGHT);
        Light lightBlock = (Light) safeSpot.getBlock().getBlockData();
        lightBlock.setLevel(15); // Set light level to max
        safeSpot.getBlock().setBlockData(lightBlock);

        // Particle effect at the placed location
        player.getWorld().spawnParticle(Particle.END_ROD, safeSpot.add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, 0);
        player.getWorld().playSound(safeSpot, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.5f);

    }
}
