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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.block.data.type.RespawnAnchor;

import java.util.HashMap;
import java.util.Map;



public final class CoswayUtil extends JavaPlugin {

    @Override
    public void onEnable() {
        serverMessage(ColorKey("&aaw sheit here we go again...."));
        Bukkit.getPluginManager().registerEvents(new AnchorShield(), this);
        // Start the detection loop when the plugin is enabled
        new AnchorShield().startDetectionLoop();
        //getCommand("clearanchors").setExecutor(new ClearAnchorsCommand(this, anchorShield));
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPotionEffect(PotionEffectType.RESISTANCE)) {
                        Location center = player.getLocation().add(0, 1, 0); // Center around player's head
                        double radius = 1.5; // Radius of the sphere

                        for (double theta = 0; theta < Math.PI * 2; theta += Math.PI / 8) { // Horizontal rotation
                            for (double phi = 0; phi < Math.PI; phi += Math.PI / 8) { // Vertical rotation
                                double x = radius * Math.sin(phi) * Math.cos(theta);
                                double y = radius * Math.cos(phi);
                                double z = radius * Math.sin(phi) * Math.sin(theta);

                                Location particleLoc = center.clone().add(x, y, z);
                                player.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.AQUA, 0.5F));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0, 2); // Runs every 10 ticks (0.5 seconds)

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
        if (cmd.getName().equalsIgnoreCase("clearanchors") && sender instanceof Player) {
            Player player = (Player) sender;
            //AnchorShield.clearAnchors();
            serverMessage("&6this does not work yet...");
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
        getServer().broadcastMessage(prefix()+ColorKey(msg));
    }
    //------------------------------------------------------------------------
    public class AnchorShield implements Listener {
        private final Map<Location, ArmorStand> activeAnchors = new HashMap<>();
        private final int RING_RADIUS = 25;
        private final int FUEL_DECREASE_TIME = 1 * 60 * 20; // 5 minutes in ticks

        public void clearActiveAnchors() {
            for (ArmorStand marker : activeAnchors.values()) {
                if (marker != null && !marker.isDead()) {
                    marker.remove(); // Remove the ArmorStand from the world
                    serverMessage("removed marker");
                }
            }
            activeAnchors.clear(); // Clear the HashMap
        }


        public void startDetectionLoop() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (World world : Bukkit.getWorlds()) {
                        for (Chunk chunk : world.getLoadedChunks()) {
                            for (BlockState state : chunk.getTileEntities()) {
                                if (state.getBlock().getType() == Material.HEAVY_CORE) {
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
            return loc.getBlock().getType() == Material.HEAVY_CORE &&
                    loc.clone().add(0, -1, 0).getBlock().getType() == Material.LIGHTNING_ROD &&
                    loc.clone().add(0, -2, 0).getBlock().getType() == Material.RESPAWN_ANCHOR; // Assuming Heavy Core
        }

        private void manageAnchor(Location loc) {
            if (!activeAnchors.containsKey(loc)) {
                ArmorStand marker = spawnMarker(loc);
                activeAnchors.put(loc, marker);
                startFuelTimer(loc);
                startParticleEffect(loc);  // Start repeated particle and mob clearing
            }
        }

        private ArmorStand spawnMarker(Location loc) {
            ArmorStand marker = loc.getWorld().spawn(loc.clone().add(0.5, 1, 0.5), ArmorStand.class);
            marker.setInvisible(true);
            marker.setInvulnerable(true);
            marker.setMarker(true);
            serverMessage("Anchor shield created");
            marker.getWorld().playEffect(marker.getLocation().subtract(0,2,0),Effect.TRIAL_SPAWNER_BECOME_OMINOUS,1);
            marker.getWorld().playEffect(marker.getLocation().subtract(0,2,0),Effect.SMASH_ATTACK,1);
            marker.getWorld().playSound(marker.getLocation(),Sound.BLOCK_END_PORTAL_SPAWN,100,0);
            marker.getWorld().playEffect(marker.getLocation().subtract(0,2,0),Effect.ELECTRIC_SPARK,1);
            return marker;
        }

        private void startParticleEffect(Location loc) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!activeAnchors.containsKey(loc)) {
                        cancel();
                        serverMessage("anchors active: " + activeAnchors.toString());
                        serverMessage("active loc: "+loc);
                        serverMessage("cancel runnable initiated 1");
                        return;
                    }
                    createParticleRing(loc);
                    killHostileMobs(loc);
                }
            }.runTaskTimer(CoswayUtil.this, 20, 10); // Run every second
        }

        private void createParticleRing(Location loc) {
            for (int i = 0; i < 360; i += 1) {
                double radians = Math.toRadians(i);
                double x = loc.getX() + RING_RADIUS * Math.cos(radians);
                double z = loc.getZ() + RING_RADIUS * Math.sin(radians);
                Location particleLoc = new Location(loc.getWorld(), x + 0.5, loc.getY() - 1, z + 0.5);
                //loc.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, new Particle.DustOptions(Color.LIME, 1));
                loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 3, 0, 1, 0, 0);

            }
            Location centered = new Location(loc.getWorld(),loc.getX() + 0.5, loc.getY(), loc.getZ() + 0.5);
            loc.getWorld().spawnParticle(Particle.PORTAL, centered,10);
        }

        private void killHostileMobs(Location loc) {
            loc.getWorld().getEntitiesByClass(Monster.class).forEach(mob -> {
                if (mob.getLocation().distance(loc) <= RING_RADIUS) {
                    mob.getWorld().playEffect(mob.getLocation(),Effect.TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS,1);
                    mob.getWorld().playSound(mob.getLocation(),Sound.ENTITY_BREEZE_JUMP,10,0);
                    mob.remove();
                }
            });
        }

        private void startFuelTimer(Location loc) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!activeAnchors.containsKey(loc)) {
                        serverMessage("anchor mapping did not match, removed anchor: "+loc);
                        cancel();
                        serverMessage("cancel runnable initiated 2");
                        return;
                    }

                    Block block = loc.clone().subtract(0,2,0).getBlock();
                    RespawnAnchor anchorData = (RespawnAnchor) block.getBlockData();
                    if (block.getType() != Material.RESPAWN_ANCHOR) {
                        serverMessage(String.valueOf(block.getType()));
                        removeMarker(loc);
                        cancel();
                        serverMessage("cancel runnable initiated 3");
                        return;
                    }

                    int fuelLevel = anchorData.getCharges();

                    if (fuelLevel > 0) {
                        anchorData.setCharges(anchorData.getCharges() - 1);
                        block.setBlockData(anchorData); // Apply the new data
                        loc.getBlock().getWorld().playSound(loc,Sound.BLOCK_BEACON_DEACTIVATE,10,0);
                        loc.getBlock().getWorld().playEffect(loc,Effect.TRIAL_SPAWNER_DETECT_PLAYER,1);
                        serverMessage("depleted anchor charge");
                    } else {
                        removeMarker(loc);
                        serverMessage("removed anchor shield for no fuel");
                        cancel();
                        serverMessage("cancel runnable initiated 4");
                    }
                }
            }.runTaskTimer(CoswayUtil.this, FUEL_DECREASE_TIME, FUEL_DECREASE_TIME);
        }

        private void removeMarker(Location loc) {
            if (activeAnchors.containsKey(loc)) {
                activeAnchors.get(loc).remove();
                activeAnchors.remove(loc);
                serverMessage("force removed shield mapping");
            }
        }

        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            Location loc = event.getBlock().getLocation();

            for (Location anchorLoc : activeAnchors.keySet()) {
                if (isMultiBlock(anchorLoc) && (
                        loc.equals(anchorLoc) ||
                                loc.equals(anchorLoc.clone().add(0, -1, 0)) ||
                                loc.equals(anchorLoc.clone().add(0, -2, 0))
                )) {
                    removeMarker(anchorLoc);
                    return;
                }
            }
        }

        @EventHandler
        public void onBlockPlace(BlockPlaceEvent event) {
            Location loc = event.getBlockPlaced().getLocation();
            if (isMultiBlock(loc)) {
                serverMessage("Multiblock made");
                manageAnchor(loc);  // Start managing the anchor once the multi-block structure is assembled
            }
        }
    }


}

