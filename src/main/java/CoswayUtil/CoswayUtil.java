package CoswayUtil;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.LightningRod;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.block.data.type.RespawnAnchor;
import CoswayUtil.GravityGauntletCommand;
import net.milkbowl.vault.economy.Economy;

import java.util.HashMap;
import java.util.Map;



public final class CoswayUtil extends JavaPlugin implements Listener {
    private Economy economy;
    private FileConfiguration config;
    @Override
    public void onEnable() {
        // Create or load the configuration file
        saveDefaultConfig();  // This creates the config file if it doesn't exist.
        config = getConfig(); // Get the loaded configuration
        serverMessage(ColorKey("&aaw sheit here we go again...."));
        Bukkit.getPluginManager().registerEvents(new AnchorShield(), this);
        // Start the detection loop when the plugin is enabled
        new AnchorShield().startDetectionLoop();
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
        // Register Gravity Gauntlet
        new GravityGauntlet(this);
        // Register TotemShield
        new TotemShield(this);
        // Register the ShadowStep listener
        new MobLevitationWand(this);
        if (!setupEconomy()) {
            getLogger().severe("Vault not found or no economy provider found.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new BlockShop(this, economy).register();
        new BlockShop(this, economy);
        new RapidFireBow(this);
        getServer().getPluginManager().registerEvents(new RapidFireBow(this), this);
        getServer().getPluginManager().registerEvents(new ShadowStep(this), this);
        //register levitation wand
        Bukkit.getPluginManager().registerEvents(new MobLevitationWand(this), this);
        // Register the PhantomDodge listener
        getServer().getPluginManager().registerEvents(new PhantomDodge(this), this);
        // Register the WitherContract listener
        getServer().getPluginManager().registerEvents(new WitherContract(this), this);
        // Register commands
        this.getCommand("gravitygauntlet").setExecutor(new GravityGauntletCommand());
        this.getCommand("getwand").setExecutor(new GiveWandCommand());
        getServer().getPluginManager().registerEvents(new LaunchStick(this), this);
    }


    private boolean setupEconomy() {
        // Setup Vault economy (make sure it's enabled and available)
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
            return economy != null;
        }
        return false;
    }
    @Override
    public void onDisable() {
        serverMessage(ColorKey("&cim dead, im alive but im dead...."));
    }
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String @NotNull [] args) {
        if (cmd.getName().equalsIgnoreCase("scale") && sender instanceof Player player) {
            if (args.length < 1) {
            return false;
            } else {
            setScale(player, Float.parseFloat(args[0]));
        }
        }
        if (cmd.getName().equalsIgnoreCase("launchstick") && sender instanceof Player player) {
            player.getInventory().addItem(LaunchStick.createLaunchStick());
        }
        if (cmd.getName().equalsIgnoreCase("blockshop") && sender instanceof Player player) {
            BlockShop.openShop(player,1);
        }
        if (cmd.getName().equalsIgnoreCase("shopbook") && sender instanceof Player player) {
            player.getInventory().addItem(BlockShop.createShopItem());
        }
        if (cmd.getName().equalsIgnoreCase("throw") && sender instanceof Player player) {
            Player Target = getNearestPlayer(player,10);
            if(Target == null) {
                returnMsg(player,"&cNo players in radius");
            } else {
                throwEntityAway(Target, player.getLocation(), 8);
                returnMsg(Target,"&cYou were thrown by "+player.getName());
                returnMsg(player,"&aYou threw "+Target.getName());
            }
        }
        if (cmd.getName().equalsIgnoreCase("rapidbow") && sender instanceof Player player) {
            player.getInventory().addItem(RapidFireBow.createRapidFireBow());
        }

        return true;
    }

    public String prefix() {
        return ColorKey("[&7Cosway Utility&r] ");
    }
    public void returnMsg(Player p,String msg) {
        p.sendMessage(ColorKey(msg));
    }
    public void setScale(Player player, float value) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                10,
                entity -> !(entity instanceof Player) // Ignore the player
        );

        if (result != null && result.getHitEntity() != null) {
            Entity entity = result.getHitEntity();
            String entityUUID = entity.getUniqueId().toString();

            String command = "attribute " + entityUUID + " minecraft:scale base set " + value;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            player.sendMessage("§aSet scale of " + entity.getName() + " to " + value);
        } else {
            player.sendMessage("§cNot Looking at any entity! setting for nearest player in radius!");
            Entity entity = getNearestPlayer(player,4);
            String entityUUID = entity.getUniqueId().toString();

            String command = "attribute " + entityUUID + " minecraft:scale base set " + value;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            player.sendMessage("§aSet scale of " + entity.getName() + " to " + value);
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
    private void throwEntityAway(Entity entity, Location source, double power) {
        if (entity == null || source == null) return;

        Location entityLoc = entity.getLocation();
        entity.getWorld().playEffect(entity.getLocation(),Effect.TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS,1);
        entity.getWorld().playSound(entity.getLocation(),Sound.ENTITY_BREEZE_JUMP,10,0);

        Vector knockbackDirection = entityLoc.toVector().subtract(source.toVector()).normalize();

        // Apply velocity in the opposite direction
        entity.setVelocity(knockbackDirection.multiply(power).setY(1));
    }
    @EventHandler
    public void ev(PlayerChangedWorldEvent event) {

    }
    private Player getNearestPlayer(Player sender, double radius) {
        Location senderLoc = sender.getLocation();
        Player nearestPlayer = null;
        double closestDistance = radius;

        for (Player player : sender.getWorld().getPlayers()) {
            if (player.equals(sender)) continue; // Skip the sender

            double distance = senderLoc.distance(player.getLocation());
            if (distance <= radius && distance < closestDistance) {
                closestDistance = distance;
                nearestPlayer = player;
            }
        }
        return nearestPlayer; // Returns null if no player is found
    }

    //------------------------------------------------------------------------
    public class AnchorShield implements Listener {
        private final Map<Location, ArmorStand> activeAnchors = new HashMap<>();
        private final int RING_RADIUS = 25;
        boolean debug = false;
        public void debugMessage(String msg) {
            if(debug) {
                Bukkit.broadcastMessage(ColorKey("[&6DEBUG&r] &c" + msg));
            }
        }
        public void clearActiveAnchors() {
            for (ArmorStand marker : activeAnchors.values()) {
                if (marker != null && !marker.isDead()) {
                    marker.remove(); // Remove the ArmorStand from the world
                    debugMessage("removed marker");
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
            debugMessage("Anchor shield created");
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
                        debugMessage("anchors active: " + activeAnchors.toString());
                        debugMessage("active loc: "+loc);
                        debugMessage("cancel runnable initiated 1");
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

        @EventHandler
        public void onAnchorExplosion(BlockExplodeEvent event) {
            Block block = event.getBlock();

            // Check if the explosion is caused by a Respawn Anchor in a non-Nether world
            if (block.getType() == Material.RESPAWN_ANCHOR && block.getWorld().getEnvironment() != World.Environment.NETHER) {
                event.setCancelled(true); // Cancel the explosion
            }
        }

        @EventHandler
        public void onAnchorEntityExplosion(EntityExplodeEvent event) {
            Block block = event.getLocation().getBlock();

            // Check if the explosion is caused by a Respawn Anchor in a non-Nether world
            if (block.getType() == Material.RESPAWN_ANCHOR && block.getWorld().getEnvironment() != World.Environment.NETHER) {
                event.setCancelled(true); // Cancel the explosion
            }
        }
        @EventHandler
        public void onRespawnAnchorInteract(PlayerInteractEvent event) {
            // Check if the player right-clicked a block
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block block = event.getClickedBlock();
                Player player = event.getPlayer();
                ItemStack itemInHand = player.getInventory().getItemInMainHand();

                if (block != null && block.getType() == Material.RESPAWN_ANCHOR) {
                    World world = block.getWorld();

                    // Check if the world is not the Nether
                    if (world.getEnvironment() != World.Environment.NETHER) {
                        // Get the Respawn Anchor's data
                        RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();

                        // Check if the anchor is fully charged (4 charges)
                        if (anchor.getCharges() >= anchor.getMaximumCharges()) {
                            event.setCancelled(true);
                            player.sendMessage("This Respawn Anchor is fully charged and cannot be used here.");
                            return;
                        }

                        // Allow charging if holding Glowstone
                        if (itemInHand.getType() == Material.GLOWSTONE) {
                            // Let the charging happen naturally
                            player.sendMessage("Charging Respawn Anchor...");
                        } else {
                            // Cancel any other interaction (like right-clicking with an empty hand)
                            event.setCancelled(true);
                            player.sendMessage("Respawn Anchors can only be charged with Glowstone outside the Nether.");
                        }
                    }
                }
            }
        }
        @EventHandler
        public void placedevent(BlockPlaceEvent event) {
            Block placed = event.getBlockPlaced();
            Player player = event.getPlayer();

            if (placed.getType() == Material.RESPAWN_ANCHOR) {
                Location above = placed.getLocation().clone().add(0, 1, 0);
                Block blockAbove = placed.getWorld().getBlockAt(above);

                // Check if the player has at least one Lightning Rod
                if (player.getInventory().contains(Material.LIGHTNING_ROD, 1)) {
                    blockAbove.setType(Material.LIGHTNING_ROD);

                    // Ensure the Lightning Rod is placed upright
                    LightningRod rodData = (LightningRod) blockAbove.getBlockData();
                    rodData.setFacing(BlockFace.UP);
                    blockAbove.setBlockData(rodData);

                    // Remove one Lightning Rod from the player's inventory
                    ItemStack lightningRod = new ItemStack(Material.LIGHTNING_ROD, 1);
                    player.getInventory().removeItem(lightningRod);
                } else {
                    player.sendMessage(ChatColor.RED + "You need a Lightning Rod in your inventory to place one!");
                }
            }

        }
        private void killHostileMobs(Location loc) {
            loc.getWorld().getEntitiesByClass(Monster.class).forEach(mob -> {
                if (mob.getLocation().distance(loc) <= RING_RADIUS) {
                    mob.getWorld().playEffect(mob.getLocation(),Effect.TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS,1);
                    mob.getWorld().playSound(mob.getLocation(),Sound.ENTITY_BREEZE_JUMP,10,0);
                    throwEntityAway(mob,loc,6);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (mob.getLocation().distance(loc) <= RING_RADIUS) {
                                mob.remove();
                            }
                        }
                    }.runTaskLater(CoswayUtil.this,30);

                }
            });
        }

        private void startFuelTimer(Location loc) {
            // 5 minutes in ticks
            int FUEL_DECREASE_TIME = 10 * 60 * 20;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!activeAnchors.containsKey(loc)) {
                        debugMessage("anchor mapping did not match, removed anchor: "+loc);
                        cancel();
                        debugMessage("cancel runnable initiated 2");
                        return;
                    }

                    Block block = loc.clone().subtract(0,2,0).getBlock();
                    RespawnAnchor anchorData = (RespawnAnchor) block.getBlockData();
                    if (block.getType() != Material.RESPAWN_ANCHOR) {
                        debugMessage(String.valueOf(block.getType()));
                        removeMarker(loc);
                        cancel();
                        debugMessage("cancel runnable initiated 3");
                        return;
                    }

                    int fuelLevel = anchorData.getCharges();

                    if (fuelLevel > 0) {
                        anchorData.setCharges(anchorData.getCharges() - 1);
                        block.setBlockData(anchorData); // Apply the new data
                        loc.getBlock().getWorld().playSound(loc,Sound.BLOCK_BEACON_DEACTIVATE,10,0);
                        loc.getBlock().getWorld().playEffect(loc,Effect.TRIAL_SPAWNER_DETECT_PLAYER,1);
                        debugMessage("depleted anchor charge");
                    } else {
                        removeMarker(loc);
                        debugMessage("removed anchor shield for no fuel");
                        loc.getBlock().getWorld().playSound(loc,Sound.ITEM_TOTEM_USE,10,0);
                        loc.getBlock().getWorld().playEffect(loc,Effect.ENDER_DRAGON_DEATH,1);
                        cancel();
                        debugMessage("cancel runnable initiated 4");
                    }
                }
            }.runTaskTimer(CoswayUtil.this, FUEL_DECREASE_TIME, FUEL_DECREASE_TIME);
        }

        private void removeMarker(Location loc) {
            Location newloc = loc.clone().add(0,1,0);
            loc.getBlock().getWorld().playSound(loc,Sound.ITEM_TOTEM_USE,10,0);
            loc.getBlock().getWorld().playEffect(loc,Effect.ENDER_DRAGON_DEATH,1);
            if (activeAnchors.containsKey(loc)) {
                activeAnchors.get(loc).remove();
                activeAnchors.remove(loc);
                debugMessage("force removed shield mapping");
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
                debugMessage("Multiblock made");
                manageAnchor(loc);  // Start managing the anchor once the multi-block structure is assembled
            }
        }
    }


}

