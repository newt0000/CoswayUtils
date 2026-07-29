package CoswayUtil;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.LightningRod;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



public final class CoswayUtil extends JavaPlugin implements Listener {
    private Economy economy;
    private FileConfiguration config;
    private WitherSkullWand skullWand;
    private MagmaSmelter magmaSmelter;
    public MagmaSmelter getMagmaSmelter() {
        return magmaSmelter;
    }
    public Economy getEconomy() {
        return economy;
    }
    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault dependency not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        config = getConfig();

        PDCUtil.init(this);
        magmaSmelter = new MagmaSmelter(this);
        magmaSmelter.loadMagmaFurnaces();
        getServer().getPluginManager().registerEvents(magmaSmelter, this);

        DummyManager dummyManager =
                new DummyManager(this);

        SkinResolver skinResolver =
                new SkinResolver(this);

        getCommand("dummy")
                .setExecutor(
                        new DummyCommand(
                                this,
                                dummyManager,
                                skinResolver
                        )
                );
        getCommand("equip")
                .setExecutor(
                        new EquipCommand(dummyManager)
                );
        getServer()
                .getPluginManager()
                .registerEvents(
                        new DummyListener(),
                        this
                );
        DummySettingsCommand dummySettings =
                new DummySettingsCommand(
                        this,
                        dummyManager
                );


        getCommand("dummysettings")
                .setExecutor(dummySettings);

        getCommand("dummysettings")
                .setTabCompleter(dummySettings);
        getServer()
                .getPluginManager()
                .registerEvents(
                        new DummyDamageListener(dummyManager),
                        this
                );
        DummyAIManager dummyAIManager =
                new DummyAIManager(
                        this,
                        dummyManager
                );
        registration("Dummy System");

        getServer().getPluginManager().registerEvents(new PlayerFireworkBurst(this), this);
        registration("Join/Leave particles");

        skullWand = new WitherSkullWand(this);
        getServer().getPluginManager().registerEvents(skullWand,this);
        registration("Wither Skull Launcher");

        InnocentMobJail jail = new InnocentMobJail(this,economy);
        getServer().getPluginManager().registerEvents(jail,this);
        getCommand("jail").setExecutor(jail);
        getCommand("unjail").setExecutor(jail);
        registration("Innocent Mob Jail");
        getServer().getPluginManager().registerEvents(new ChatSoundListener(this), this);
        registration("Chat Sound");
        getCommand("coswaysetpdc").setExecutor(new CoswayPDCCommand());
        getCommand("writecheck").setExecutor(new CheckCommand(this,economy));
        getCommand("cashcheck").setExecutor(new CheckCommand(this,economy));

        ReloadCommand reloadCommand = new ReloadCommand(this);
        getCommand("coswayreload").setExecutor(reloadCommand);

        MageCommand mageCommand = new MageCommand(this);
        getCommand("mage").setExecutor(mageCommand);
        getCommand("mage").setTabCompleter(mageCommand);

        RenameCommand rename = new RenameCommand(this);
        getCommand("rename").setExecutor(rename);

        TPPCommand tpp = new TPPCommand(this);
        getCommand("tpp").setExecutor(tpp);
        getCommand("tpp").setTabCompleter(tpp);

        getCommand("unbreakable").setExecutor(new UnbreakableCommand(this));
        getCommand("repair").setExecutor(new RepairCommand());

        HeatSeekingMissile missile = new HeatSeekingMissile(this);
        getServer().getPluginManager().registerEvents(missile,this);


        TreasureFountain treasure = new TreasureFountain(this);
        getServer().getPluginManager().registerEvents(treasure,this);

        getServer().getPluginManager().registerEvents(new BowTrajectoryVisualizer(this),this);
        getServer().getPluginManager().registerEvents(new TracerArrowListener(this),this);

        ShockwaveBow shockwaveBow = new ShockwaveBow(this);
        getServer().getPluginManager().registerEvents(shockwaveBow,this);
        getCommand("shockwavebow").setExecutor(shockwaveBow);

        new MaceOfStorms(this);

        AnchorShield shield = new AnchorShield();
        getServer().getPluginManager().registerEvents(shield,this);
        shield.startDetectionLoop();

        new BukkitRunnable(){
            @Override
            public void run(){
                for(Player player:Bukkit.getOnlinePlayers()){
                    if(player.hasPotionEffect(PotionEffectType.RESISTANCE)){
                        Location center=player.getLocation().add(0,1,0);

                        for(double theta=0;theta<Math.PI*2;theta+=Math.PI/8){
                            for(double phi=0;phi<Math.PI;phi+=Math.PI/8){

                                double x=1.5*Math.sin(phi)*Math.cos(theta);
                                double y=1.5*Math.cos(phi);
                                double z=1.5*Math.sin(phi)*Math.sin(theta);

                                player.getWorld().spawnParticle(
                                        Particle.DUST,
                                        center.clone().add(x,y,z),
                                        1,
                                        0,0,0,
                                        0,
                                        new Particle.DustOptions(Color.AQUA,0.5F)
                                );
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this,0,2);

        new GravityGauntlet(this);
        //new TotemShield(this);

        MobLevitationWand levitation = new MobLevitationWand(this);
        getServer().getPluginManager().registerEvents(levitation,this);

        EconomyRewards rewards = new EconomyRewards(this);
        getServer().getPluginManager().registerEvents(rewards,this);

        new BlockShop(this,economy).register();

        RapidFireBow rapid = new RapidFireBow(this);
        getServer().getPluginManager().registerEvents(rapid,this);

        getServer().getPluginManager().registerEvents(new ShadowStep(this),this);
        getServer().getPluginManager().registerEvents(new PhantomDodge(this),this);
        getServer().getPluginManager().registerEvents(new WitherContract(this),this);

        getCommand("gravitygauntlet").setExecutor(new GravityGauntletCommand());
        getCommand("getwand").setExecutor(new GiveWandCommand());

        LaunchStick launchStick = new LaunchStick(this);
        getServer().getPluginManager().registerEvents(launchStick,this);

        new FireflySimulator(this);

        BlockShop blockShop = new BlockShop(this,economy);
        blockShop.register();

        new IlluminationWand(this);
        new BlackholeEffect(this);

        new BukkitRunnable(){
            @Override
            public void run(){

                for(Player player:Bukkit.getOnlinePlayers()){

                    if(removeCustomKnowledgeBook(player,ChatColor.GREEN+"Gravity Gauntlet")){
                        ItemStack item=new ItemStack(Material.NETHERITE_HOE);
                        ItemMeta meta=item.getItemMeta();

                        if(meta!=null){
                            meta.setDisplayName(ChatColor.LIGHT_PURPLE+"Gravity Gauntlet");
                            meta.setLore(Collections.singletonList(
                                    ChatColor.GOLD+"Right Click to pull, Shift+Right Click to throw"
                            ));
                            meta.setUnbreakable(true);
                            item.setItemMeta(meta);
                        }

                        player.getInventory().addItem(item);
                    }

                    if(removeCustomKnowledgeBook(player,ChatColor.GREEN+"Wither Skull Cannon")){
                        player.getInventory().addItem(skullWand.createWand());
                    }

                    if(removeCustomKnowledgeBook(player,ChatColor.GREEN+"Seeking Missile Launcher")){
                        player.getInventory().addItem(missile.getMissileLauncher());
                    }

                    if(removeCustomKnowledgeBook(player,ChatColor.GREEN+"Launch Stick")){
                        player.getInventory().addItem(LaunchStick.createLaunchStick());
                    }

                    if(removeCustomKnowledgeBook(player,ChatColor.GREEN+"Levitation Wand")){
                        player.getInventory().addItem(MobLevitationWand.createWand());
                    }

                    if(removeCustomKnowledgeBook(player,ChatColor.GREEN+"Rapid Fire Bow")){
                        player.getInventory().addItem(RapidFireBow.createRapidFireBow());
                    }

                    if(removeCustomKnowledgeBook(player,ChatColor.GREEN+"Illumination Wand")){
                        player.getInventory().addItem(IlluminationWand.getIlluminationWand());
                    }
                }
            }
        }.runTaskTimer(this,0,2);

        registration("CoswayUtil loaded");
        serverMessage("&aCoswayUtil enabled.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }


    public void registration(String msg) {
        Bukkit.broadcastMessage(ColorKey("&7[&eRegistrations&7] &aUtility Registered: &6"+msg));
    }

    @Override
    public void onDisable() {
        serverMessage(ColorKey("&cim dead, im alive but im dead...."));
    }
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String @NotNull [] args) {
        if(cmd.getName().equalsIgnoreCase("givemace") && sender instanceof Player player) {
            ItemStack mace = new ItemStack(Material.NETHERITE_SHOVEL);
            ItemMeta meta = mace.getItemMeta();
            meta.setDisplayName(ChatColor.DARK_PURPLE + "Mace of Storms");
            mace.setItemMeta(meta);
            player.getInventory().addItem(mace);
        }
        if (cmd.getName().equalsIgnoreCase("givetreasurefountain") && sender instanceof Player player) {
            player.getInventory().addItem(TreasureFountain.getTreasureFountainItem());
        }
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
        if (cmd.getName().equalsIgnoreCase("Illumination Wand") && sender instanceof Player player) {
            player.getInventory().addItem(IlluminationWand.getIlluminationWand());
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
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getPlayer();

        Bukkit.getLogger().info("Death event fired");

        Bukkit.getScheduler().runTaskLater(this, () -> {
            Location loc = p.getLocation();

            p.sendMessage(ColorKey(
                    "&c&lYou Died\n" +
                            "&eWorld: &9" + loc.getWorld().getName() +
                            "\n&eX: &9" + loc.getBlockX() +
                            " &eY: &9" + loc.getBlockY() +
                            " &eZ: &9" + loc.getBlockZ()
            ));
        }, 1L);
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
    public boolean removeCustomKnowledgeBook(Player player, String customName) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.KNOWLEDGE_BOOK) {
                ItemMeta meta = item.getItemMeta();
                //player.sendMessage("[DEBUG] you have a knowledge book with name: \n"+ meta.getDisplayName() + "\n"+customName+"\n do they match?");
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(customName)) {
                    player.getInventory().remove(item); // Remove the book
                    return true; // Found and removed
                }
            }
        }
        return false; // No matching book found
    }

    public static void giveAnchorKit(Player player) {
        // Create a chest item
        ItemStack chestItem = new ItemStack(Material.CHEST);
        BlockStateMeta meta = (BlockStateMeta) chestItem.getItemMeta();

        if (meta != null) {
            // Get the block state as a chest
            Chest chestState = (Chest) meta.getBlockState();
            meta.setDisplayName(ChatColor.GOLD+"Anchor Shield Kit");
            // Get the inventory of the chest
            Inventory chestInventory = chestState.getInventory();

            // Add items to the chest
            chestInventory.setItem(0, new ItemStack(Material.RESPAWN_ANCHOR, 1)); // 1 Respawn Anchor in slot 0
            chestInventory.setItem(1, new ItemStack(Material.LIGHTNING_ROD, 1)); // 1 Lightning Rod in slot 1
            chestInventory.setItem(2, new ItemStack(Material.HEAVY_CORE, 1)); // 1 Heavy Core in slot 2
            chestInventory.setItem(3, new ItemStack(Material.GLOWSTONE,4)); // 4 Glowstone in slot 3
            // Update the block state in the meta
            chestState.update();
            meta.setBlockState(chestState);
            chestItem.setItemMeta(meta);
        }

        // Give the player the preloaded chest
        player.getInventory().addItem(chestItem);
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
                    loc.clone().add(0, -2, 0).getBlock().getType() == Material.RESPAWN_ANCHOR;
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
            serverMessage("&6A new anchor shield has been activated at &eX:"+loc.getX()+" Y:"+loc.getY()+" Z:"+loc.getZ());
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
                        loc.getBlock().getWorld().spawnParticle(Particle.PORTAL,loc,100);
                        serverMessage("&cAn Anchor Shield at &eX:"+loc.getX()+" Y:"+loc.getY()+" Z:"+loc.getZ()+" &chas run out of fuel and deactivated!");
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
                    serverMessage("&cAn anchor shield has been broken at &eX:"+anchorLoc.getX()+" Y:"+anchorLoc.getY()+" Z:"+anchorLoc.getZ());
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



