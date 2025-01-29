package CoswayUtil;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class CoswayUtil extends JavaPlugin {

    @Override
    public void onEnable() {
        serverMessage(ColorKey("&aaw sheit here we go again...."));
        Bukkit.getPluginManager().registerEvents(new AnchorShield(), this);

    }

    @Override
    public void onDisable() {
        serverMessage(ColorKey("&cim dead, im alive but im dead...."));
    }
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("scale") && sender instanceof Player) {
        Player player = (Player) sender;
        if (args.length < 1) {
            return false;
            } else {
            setScale(player, Float.parseFloat(args[0]));
        }
        }
        return true;
    }

    public String prefix() {
        return ColorKey("[&7Cosway Utility&r] ");
    }
    public void setScale(Player player, float value) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                10,
                entity -> entity instanceof Player == false // Ignore the player
        );

        if (result != null && result.getHitEntity() != null) {
            Entity entity = result.getHitEntity();
            String entityUUID = entity.getUniqueId().toString();

            String command = "attribute " + entityUUID + " minecraft:scale base set " + value;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            player.sendMessage("§aSet scale of " + entity.getName() + " to " + value);
        } else {
            player.sendMessage("§cNot Looking at any entity!");
        }
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
    public void serverMessage(String msg) {
        getServer().broadcastMessage(prefix()+msg);
    }
    //------------------------------------------------------------------------
    public class AnchorShield implements Listener {
        private final Map<Location, ArmorStand> activeAnchors = new HashMap<>();
        private final int RING_RADIUS = 15;
        private final int FUEL_DECREASE_TIME = 5 * 60 * 20; // 5 minutes in ticks

        public void startDetectionLoop() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (World world : Bukkit.getWorlds()) {
                        for (Chunk chunk : world.getLoadedChunks()) {
                            for (BlockState state : chunk.getTileEntities()) {
                                if (state.getBlock().getType() == Material.RESPAWN_ANCHOR) {
                                    Location anchorLoc = state.getLocation();
                                    if (isMultiBlock(anchorLoc)) {
                                        manageAnchor(anchorLoc);
                                    }
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(CoswayUtil.this, 0, 100); // Run every 5 seconds
        }

        private boolean isMultiBlock(Location loc) {
            return loc.getBlock().getType() == Material.RESPAWN_ANCHOR &&
                    loc.clone().add(0, 1, 0).getBlock().getType() == Material.LIGHTNING_ROD &&
                    loc.clone().add(0, 2, 0).getBlock().getType() == Material.HEAVY_CORE; // Assuming Heavy Core
        }

        private void manageAnchor(Location loc) {
            if (!activeAnchors.containsKey(loc)) {
                ArmorStand marker = spawnMarker(loc);
                activeAnchors.put(loc, marker);
                startFuelTimer(loc);
            }

            int fuelLevel = loc.getBlock().getBlockData().getAsString().contains("charges=") ?
                    Integer.parseInt(loc.getBlock().getBlockData().getAsString().split("charges=")[1].substring(0, 1)) : 0;

            if (fuelLevel > 0) {
                createParticleRing(loc);
                killHostileMobs(loc);
            }
        }

        private ArmorStand spawnMarker(Location loc) {
            ArmorStand marker = loc.getWorld().spawn(loc.add(0.5, 1, 0.5), ArmorStand.class);
            marker.setInvisible(true);
            marker.setInvulnerable(true);
            marker.setMarker(true);
            return marker;
        }

        private void createParticleRing(Location loc) {
            for (int i = 0; i < 360; i += 10) {
                double radians = Math.toRadians(i);
                double x = loc.getX() + RING_RADIUS * Math.cos(radians);
                double z = loc.getZ() + RING_RADIUS * Math.sin(radians);
                Location particleLoc = new Location(loc.getWorld(), x, loc.getY() + 1, z);
                loc.getWorld().spawnParticle(Particle.CRIT, particleLoc, 1, new Particle.DustOptions(Color.RED, 1));
            }
        }

        private void killHostileMobs(Location loc) {
            loc.getWorld().getEntitiesByClass(Monster.class).forEach(mob -> {
                if (mob.getLocation().distance(loc) <= RING_RADIUS) {
                    mob.remove();
                }
            });
        }

        private void startFuelTimer(Location loc) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!activeAnchors.containsKey(loc)) {
                        cancel();
                        return;
                    }

                    Block block = loc.getBlock();
                    if (block.getType() != Material.RESPAWN_ANCHOR) {
                        removeMarker(loc);
                        cancel();
                        return;
                    }

                    int fuelLevel = block.getBlockData().getAsString().contains("charges=") ?
                            Integer.parseInt(block.getBlockData().getAsString().split("charges=")[1].substring(0, 1)) : 0;

                    if (fuelLevel > 0) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "data merge block " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " {Charges:" + (fuelLevel - 1) + "}");
                    } else {
                        removeMarker(loc);
                        cancel();
                    }
                }
            }.runTaskTimer(CoswayUtil.this, FUEL_DECREASE_TIME, FUEL_DECREASE_TIME);
        }

        private void removeMarker(Location loc) {
            if (activeAnchors.containsKey(loc)) {
                activeAnchors.get(loc).remove();
                activeAnchors.remove(loc);
            }
        }

        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            Location loc = event.getBlock().getLocation();
            if (activeAnchors.containsKey(loc) || isMultiBlock(loc)) {
                removeMarker(loc);
            }
        }
    }
}

