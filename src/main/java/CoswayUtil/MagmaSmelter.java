package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MagmaSmelter implements Listener, CommandExecutor {
    private final JavaPlugin plugin;
    private final Map<String, Smelter> smelters = new HashMap<>();

    //private static final int SMELT_DELAY = 5;
    //private static final int OUTPUT_INTERVAL = 1;
    private int smeltDelay;
    private int outputInterval;
    private int particleInterval;
    private final Map<Material, ItemStack> customRecipes = new HashMap<>();
    private final List<Location> activeMagmaFurnaces = new ArrayList<>();

    public MagmaSmelter(JavaPlugin plugin) {
        this.plugin = plugin;
        this.smeltDelay = Math.max(1, plugin.getConfig().getInt(
                "magma-smelter.smelt-delay-ticks",
                5
        ));

        this.outputInterval = Math.max(1, plugin.getConfig().getInt(
                "magma-smelter.output-interval-ticks",
                1
        ));
        loadCustomRecipes();
        if (plugin.getCommand("placemagmafurnace") != null) {
            plugin.getCommand("placemagmafurnace").setExecutor(this);
        }

        loadMagmaFurnaces();

        startTask();
    }

    /*
     * ============================================================
     * STRUCTURE DETECTION
     * ============================================================
     */
    public void loadMagmaFurnaces() {
        activeMagmaFurnaces.clear();
        Map<String, Smelter> oldSmelters = new HashMap<>(smelters);
        smelters.clear();

        List<String> furnaces = plugin.getConfig().getStringList("magma-furnaces");

        for (String data : furnaces) {
            String[] split = data.split(",");

            if (split.length != 4) {
                continue;
            }

            World world = Bukkit.getWorld(split[0]);

            if (world == null) {
                continue;
            }

            Location center = new Location(
                    world,
                    Integer.parseInt(split[1]),
                    Integer.parseInt(split[2]),
                    Integer.parseInt(split[3])
            );

            Structure structure = checkCandidateCenter(center.getBlock());

            if (structure == null) {
                plugin.getLogger().warning(
                        "Saved Magma Furnace at " + data + " is no longer valid."
                );
                continue;
            }

            Smelter smelter = oldSmelters.get(
                    locationKey(structure.center)
            );

            if (smelter == null) {
                smelter = new Smelter(
                        structure.center,
                        structure.furnace,
                        structure.outputFace
                );
            }

            smelters.put(
                    locationKey(structure.center),
                    smelter
            );

            activeMagmaFurnaces.add(center);
        }

        plugin.getLogger().info(
                "Loaded " + smelters.size() + " Magma Furnaces."
        );
    }
    private void removeMagmaFurnace(Location loc) {

        List<String> furnaces = plugin.getConfig().getStringList("magma-furnaces");

        String data = loc.getWorld().getName() + "," +
                loc.getBlockX() + "," +
                loc.getBlockY() + "," +
                loc.getBlockZ();

        furnaces.remove(data);

        plugin.getConfig().set("magma-furnaces", furnaces);
        plugin.saveConfig();

        activeMagmaFurnaces.removeIf(existing ->
                existing.getBlockX() == loc.getBlockX()
                        && existing.getBlockY() == loc.getBlockY()
                        && existing.getBlockZ() == loc.getBlockZ()
                        && existing.getWorld().equals(loc.getWorld())
        );
    }
    public void saveMagmaFurnace(Location loc) {
        List<String> furnaces = plugin.getConfig().getStringList("magma-furnaces");

        String data = loc.getWorld().getName() + "," +
                loc.getBlockX() + "," +
                loc.getBlockY() + "," +
                loc.getBlockZ();

        if (!furnaces.contains(data)) {
            furnaces.add(data);
            plugin.getConfig().set("magma-furnaces", furnaces);
            plugin.saveConfig();
        }

        if (!activeMagmaFurnaces.contains(loc)) {
            activeMagmaFurnaces.add(loc);
        }
    }
    private void loadCustomRecipes() {
        customRecipes.clear();

        if (!plugin.getConfig().isConfigurationSection("magma-smelter.recipes")) {
            return;
        }

        for (String key : plugin.getConfig().getConfigurationSection("magma-smelter.recipes").getKeys(false)) {

            String base = "magma-smelter.recipes." + key;

            String inputName = plugin.getConfig().getString(base + ".input");
            String outputName = plugin.getConfig().getString(base + ".output");

            if (inputName == null || outputName == null) {
                plugin.getLogger().warning("MagmaSmelter recipe '" + key + "' is missing an input or output.");
                continue;
            }

            Material input;
            Material output;

            try {
                input = Material.valueOf(inputName.toUpperCase());
                output = Material.valueOf(outputName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid material in MagmaSmelter recipe '" + key + "'");
                continue;
            }

            customRecipes.put(input, new ItemStack(output));
        }

        plugin.getLogger().info("Loaded " + customRecipes.size() + " custom MagmaSmelter recipes.");
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();

        Bukkit.getScheduler().runTask(plugin, () -> {
            Structure structure = findStructureNear(placed);

            if (structure == null) return;

            String key = locationKey(structure.center);

            if (smelters.containsKey(key)) return;

            Smelter smelter = new Smelter(
                    structure.center,
                    structure.furnace,
                    structure.outputFace
            );

            smelters.put(key, smelter);
            saveMagmaFurnace(structure.center);
            playBuildEffect(smelter);
        });
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();

        Smelter smelter = findSmelterContaining(broken.getLocation());

        /*
         * If the machine was not currently registered, check whether
         * the block being broken belongs to a valid structure anyway.
         * This makes detection reliable even after plugin reloads.
         */
        if (smelter == null) {
            Structure structure = findStructureNear(broken);

            if (structure != null && structure.contains(broken.getLocation())) {
                smelter = new Smelter(
                        structure.center,
                        structure.furnace,
                        structure.outputFace
                );
            }
        }

        if (smelter == null) return;

        String key = locationKey(smelter.center);

        smelters.remove(key);
        removeMagmaFurnace(smelter.center);

        /*
         * Return anything currently being processed or waiting
         * for output.
         */
        smelter.disassemble();

        /*
         * Let Minecraft naturally drop the block the player broke.
         */
        playBreakEffect(smelter);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    private void handleExplosion(List<Block> blocks) {
        if (blocks.isEmpty()) return;

        List<Smelter> affected = new ArrayList<>();

        for (Block block : blocks) {
            Smelter smelter = findSmelterContaining(block.getLocation());

            if (smelter != null && !affected.contains(smelter)) {
                affected.add(smelter);
            }
        }

        for (Smelter smelter : affected) {
            smelters.remove(locationKey(smelter.center));
            removeMagmaFurnace(smelter.center);
            smelter.disassemble();
            playBreakEffect(smelter);
        }
    }

    private Structure findStructureNear(Block changedBlock) {
        /*
         * A changed block can be anywhere in or immediately adjacent
         * to the 3x3x3 machine. Search a generous radius so detection
         * does not depend on which block was placed last.
         */
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    Block possibleCenter = changedBlock.getRelative(x, y, z);

                    Structure structure = checkCandidateCenter(possibleCenter);

                    if (structure == null) continue;

                    if (structure.contains(changedBlock.getLocation())) {
                        return structure;
                    }
                }
            }
        }

        return null;
    }

    /*
     * Checks whether the supplied block is the CENTER of a valid
     * 3x3x3 machine.
     *
     * The center is the middle block of the 3x3x3 volume.
     */
    private Structure checkCandidateCenter(Block center) {
        if (center.getWorld() == null) return null;

        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        int minX = cx - 1;
        int maxX = cx + 1;
        int minY = cy - 1;
        int maxY = cy + 1;
        int minZ = cz - 1;
        int maxZ = cz + 1;

        Block furnace = null;
        int furnaceCount = 0;

        /*
         * Every block in the 3x3x3 must be either:
         *
         *   Smooth Stone
         *   exactly one Furnace
         *
         * Anything else invalidates the structure.
         */
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = center.getWorld().getBlockAt(x, y, z);

                    if (block.getType() == Material.FURNACE) {
                        furnaceCount++;
                        furnace = block;
                    } else if (block.getType() != Material.SMOOTH_STONE) {
                        return null;
                    }
                }
            }
        }

        if (furnaceCount != 1 || furnace == null) {
            return null;
        }

        /*
         * The furnace MUST be:
         *
         *   middle X
         *   middle Y
         *   outer Z
         *
         * OR
         *
         *   middle Y
         *   middle Z
         *   outer X
         *
         * This prevents a furnace in a corner or on the bottom/top.
         */
        BlockFace outputFace = null;

        if (furnace.getX() == cx &&
                furnace.getY() == cy &&
                furnace.getZ() == minZ) {
            outputFace = BlockFace.NORTH;
        } else if (furnace.getX() == cx &&
                furnace.getY() == cy &&
                furnace.getZ() == maxZ) {
            outputFace = BlockFace.SOUTH;
        } else if (furnace.getX() == maxX &&
                furnace.getY() == cy &&
                furnace.getZ() == cz) {
            outputFace = BlockFace.EAST;
        } else if (furnace.getX() == minX &&
                furnace.getY() == cy &&
                furnace.getZ() == cz) {
            outputFace = BlockFace.WEST;
        }

        if (outputFace == null) {
            return null;
        }

        /*
         * The furnace must face OUTWARD from the machine.
         *
         * Example:
         *
         *     S S S
         *     S F S  -> F must face South
         *     S S S
         */
        if (!(furnace.getBlockData() instanceof Directional directional)) {
            return null;
        }

        if (directional.getFacing() != outputFace) {
            return null;
        }

        return new Structure(
                center.getLocation(),
                furnace.getLocation(),
                outputFace
        );
    }

    /*
     * ============================================================
     * MACHINE TASK
     * ============================================================
     */

    private void startTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<String, Smelter>> iterator =
                    smelters.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, Smelter> entry = iterator.next();
                Smelter smelter = entry.getValue();

                /*
                 * Revalidate the complete physical structure every tick.
                 *
                 * This catches changes made by commands, pistons,
                 * plugins, world edits, etc.
                 */
                if (!smelter.isStructureValid()) {
                    smelter.disassemble();
                    iterator.remove();
                    playBreakEffect(smelter);
                    continue;
                }

                smelter.tick();
            }
        }, 1L, 1L);
    }

    /*
     * ============================================================
     * COMMAND
     * ============================================================
     */

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!command.getName().equalsIgnoreCase("placemagmafurnace")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("coswayutil.placemagmafurnace")) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        BlockFace direction = getCardinalDirection(player);

        /*
         * Put the center of the machine two blocks in front of
         * the player's block position.
         */
        Location center = player.getLocation().getBlock().getLocation()
                .add(direction.getModX() * 2, 0, direction.getModZ() * 2);

        /*
         * The furnace is placed on the side facing the player.
         *
         * Example:
         *
         *       PLAYER
         *          ↓
         *          F
         *       S S S
         *       S S S
         *       S S S
         *
         * So output comes toward the player.
         */
        BlockFace furnaceFace = direction.getOppositeFace();

        if (!canPlaceStructure(center)) {
            player.sendMessage("§cThere is not enough space to place the Magma Furnace there.");
            return true;
        }

        placeStructure(center, furnaceFace);

        Structure structure = checkCandidateCenter(center.getBlock());

        if (structure != null) {
            String key = locationKey(structure.center);

            if (!smelters.containsKey(key)) {
                Smelter smelter = new Smelter(
                        structure.center,
                        structure.furnace,
                        structure.outputFace
                );

                smelters.put(key, smelter);
                saveMagmaFurnace(structure.center);
                playBuildEffect(smelter);
            }
        }

        player.sendMessage("§6Magma Furnace §ahas been assembled.");
        return true;
    }

    private BlockFace getCardinalDirection(Player player) {
        float yaw = player.getLocation().getYaw();

        yaw %= 360;

        if (yaw < 0) {
            yaw += 360;
        }

        if (yaw >= 315 || yaw < 45) {
            return BlockFace.SOUTH;
        }

        if (yaw < 135) {
            return BlockFace.WEST;
        }

        if (yaw < 225) {
            return BlockFace.NORTH;
        }

        return BlockFace.EAST;
    }

    private boolean canPlaceStructure(Location center) {
        World world = center.getWorld();

        if (world == null) return false;

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        /*
         * The command places the entire 3x3x3 into empty space.
         */
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = world.getBlockAt(
                            cx + x,
                            cy + y,
                            cz + z
                    );

                    if (!block.isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void placeStructure(Location center, BlockFace furnaceFace) {
        World world = center.getWorld();

        if (world == null) return;

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    world.getBlockAt(
                            cx + x,
                            cy + y,
                            cz + z
                    ).setType(Material.SMOOTH_STONE, false);
                }
            }
        }

        Block furnace = world.getBlockAt(
                cx + furnaceFace.getModX(),
                cy,
                cz + furnaceFace.getModZ()
        );

        furnace.setType(Material.FURNACE, false);

        if (furnace.getBlockData() instanceof Directional directional) {
            directional.setFacing(furnaceFace);
            furnace.setBlockData(directional, false);
        }
    }

    /*
     * ============================================================
     * EFFECTS
     * ============================================================
     */

    private void playBuildEffect(Smelter smelter) {
        World world = smelter.center.getWorld();

        if (world == null) return;

        Location location = smelter.getTopCenter();

        world.spawnParticle(
                Particle.FLAME,
                location,
                40,
                0.25,
                0.3,
                0.25,
                0.05
        );

        world.spawnParticle(
                Particle.CAMPFIRE_COSY_SMOKE,
                location,
                25,
                0.25,
                0.4,
                0.25,
                0.04
        );

        world.playSound(
                location,
                Sound.BLOCK_BEACON_ACTIVATE,
                1.0f,
                1.25f
        );
    }

    private void playBreakEffect(Smelter smelter) {
        World world = smelter.center.getWorld();

        if (world == null) return;

        Location location = smelter.center.clone().add(0.5, 0.5, 0.5);

        world.spawnParticle(
                Particle.SMOKE,
                location,
                25,
                0.5,
                0.5,
                0.5,
                0.04
        );

        world.playSound(
                location,
                Sound.BLOCK_BEACON_DEACTIVATE,
                1.0f,
                0.8f
        );
    }

    /*
     * ============================================================
     * RECIPE HANDLING
     * ============================================================
     */
    public void reload() {

        smeltDelay = Math.max(
                1,
                plugin.getConfig().getInt(
                        "magma-smelter.smelt-delay-ticks",
                        5
                )
        );

        outputInterval = Math.max(
                1,
                plugin.getConfig().getInt(
                        "magma-smelter.output-interval-ticks",
                        1
                )
        );

        particleInterval = Math.max(
                1,
                plugin.getConfig().getInt(
                        "magma-smelter.particle-interval-ticks",
                        1
                )
        );

        loadCustomRecipes();
        loadMagmaFurnaces();
    }
    private ItemStack getSmeltResult(ItemStack input) {

        if (input == null || input.getType().isAir()) {
            return null;
        }

        /*
         * -------------------------
         * Custom recipes first
         * -------------------------
         */

        ItemStack custom = customRecipes.get(input.getType());

        if (custom != null) {
            return custom.clone();
        }

        /*
         * -------------------------
         * Vanilla furnace recipes
         * -------------------------
         */

        Iterator<Recipe> recipes = Bukkit.recipeIterator();

        while (recipes.hasNext()) {

            Recipe recipe = recipes.next();

            if (!(recipe instanceof FurnaceRecipe furnaceRecipe)) {
                continue;
            }

            RecipeChoice choice = furnaceRecipe.getInputChoice();

            if (choice == null) {
                continue;
            }

            if (choice.test(input)) {
                return furnaceRecipe.getResult().clone();
            }
        }

        return null;
    }

    /*
     * ============================================================
     * MACHINE
     * ============================================================
     */

    private class Smelter {
        private final Location center;
        private final Location furnace;
        private final BlockFace outputFace;

        private final Queue<SmeltJob> jobs = new ArrayDeque<>();
        private final Queue<ItemStack> outputQueue = new ArrayDeque<>();

        private long tick;
        private long nextOutputTick;

        private Smelter(
                Location center,
                Location furnace,
                BlockFace outputFace
        ) {
            this.center = center.clone();
            this.furnace = furnace.clone();
            this.outputFace = outputFace;
        }

        private void tick() {
            tick++;

            collectItems();
            finishSmelting();
            outputItems();
            spawnParticles();
        }

        private void collectItems() {
            World world = center.getWorld();

            if (world == null) return;

            Location inputLocation = getInputLocation();

            for (Entity entity : world.getNearbyEntities(
                    inputLocation,
                    0.6,
                    0.7,
                    0.6
            )) {
                if (!(entity instanceof Item item)) {
                    continue;
                }

                if (!item.isValid() || item.isDead()) {
                    continue;
                }

                ItemStack input = item.getItemStack();

                if (input == null || input.getType().isAir()) {
                    continue;
                }

                ItemStack result = getSmeltResult(input);

                if (result == null) {
                    continue;
                }

                /*
                 * Store the entire original stack.
                 */
                jobs.add(new SmeltJob(
                        input.clone(),
                        result.clone(),
                        tick + smeltDelay
                ));

                /*
                 * The world item disappears immediately.
                 */
                item.remove();
            }
        }

        private void finishSmelting() {
            Iterator<SmeltJob> iterator = jobs.iterator();

            while (iterator.hasNext()) {
                SmeltJob job = iterator.next();

                if (tick < job.finishTick) {
                    continue;
                }

                int inputAmount = job.input.getAmount();
                int resultAmountPerInput = job.resultPerItem.getAmount();

                long totalOutput =
                        (long) inputAmount * resultAmountPerInput;

                while (totalOutput > 0) {
                    int amount = (int) Math.min(
                            totalOutput,
                            job.resultPerItem.getMaxStackSize()
                    );

                    ItemStack output = job.resultPerItem.clone();
                    output.setAmount(amount);

                    outputQueue.add(output);

                    totalOutput -= amount;
                }

                iterator.remove();
            }
        }

        private void outputItems() {
            if (outputQueue.isEmpty()) {
                return;
            }

            if (tick < nextOutputTick) {
                return;
            }

            ItemStack stack = outputQueue.peek();

            if (stack == null ||
                    stack.getType().isAir() ||
                    stack.getAmount() <= 0) {
                outputQueue.poll();
                return;
            }

            ItemStack output = stack.clone();
            output.setAmount(1);

            stack.setAmount(stack.getAmount() - 1);

            if (stack.getAmount() <= 0) {
                outputQueue.poll();
            }

            spawnOutput(output);

            nextOutputTick = tick + outputInterval;
        }

        private void spawnOutput(ItemStack output) {
            World world = furnace.getWorld();

            if (world == null) return;

            Vector direction =
                    outputFace.getDirection().normalize();

            Location spawn = furnace.clone()
                    .add(0.5, 0.5, 0.5)
                    .add(direction.clone().multiply(0.75));

            Item item = world.dropItem(spawn, output);

            item.setVelocity(
                    direction.clone()
                            .multiply(0.30)
                            .setY(0.12)
            );

            item.setPickupDelay(0);

            world.playSound(
                    spawn,
                    Sound.BLOCK_FIRE_EXTINGUISH,
                    0.7f,
                    1.5f
            );
        }

        private void spawnParticles() {
            World world = center.getWorld();

            if (world == null) return;

            Location top = getTopCenter();

            world.spawnParticle(
                    Particle.FLAME,
                    top,
                    2,
                    0.12,
                    0.08,
                    0.12,
                    0.015
            );

            for (int i = 0; i < 3; i++) {
                world.spawnParticle(
                        Particle.CAMPFIRE_COSY_SMOKE,
                        top.clone().add(
                                (Math.random() - 0.5) * 0.2,
                                i * 0.15,
                                (Math.random() - 0.5) * 0.2
                        ),
                        3,
                        0.12,
                        0.05,
                        0.12,
                        0.03
                );
            }
        }

        private Location getInputLocation() {
            /*
             * Center of the top face of the 3x3x3 structure.
             */
            return center.clone().add(0.5, 1.55, 0.5);
        }

        private Location getTopCenter() {
            /*
             * Particles appear above the center of the top layer.
             */
            return center.clone().add(0.5, 1.65, 0.5);
        }

        private boolean contains(Location location) {
            if (location == null ||
                    location.getWorld() == null ||
                    center.getWorld() == null) {
                return false;
            }

            if (!location.getWorld().getUID().equals(
                    center.getWorld().getUID()
            )) {
                return false;
            }

            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();

            int cx = center.getBlockX();
            int cy = center.getBlockY();
            int cz = center.getBlockZ();

            return x >= cx - 1 &&
                    x <= cx + 1 &&
                    y >= cy - 1 &&
                    y <= cy + 1 &&
                    z >= cz - 1 &&
                    z <= cz + 1;
        }

        private boolean isStructureValid() {
            Structure structure =
                    checkCandidateCenter(center.getBlock());

            if (structure == null) {
                return false;
            }

            return structure.center.getBlockX() == center.getBlockX() &&
                    structure.center.getBlockY() == center.getBlockY() &&
                    structure.center.getBlockZ() == center.getBlockZ() &&
                    structure.furnace.getBlockX() == furnace.getBlockX() &&
                    structure.furnace.getBlockY() == furnace.getBlockY() &&
                    structure.furnace.getBlockZ() == furnace.getBlockZ() &&
                    structure.outputFace == outputFace;
        }

        private void disassemble() {
            World world = center.getWorld();

            if (world == null) {
                jobs.clear();
                outputQueue.clear();
                return;
            }

            Location dropLocation =
                    center.clone().add(0.5, 1.0, 0.5);

            /*
             * Anything that has not finished gets its ORIGINAL INPUT
             * back.
             */
            for (SmeltJob job : jobs) {
                if (job.input != null &&
                        job.input.getAmount() > 0) {
                    world.dropItemNaturally(
                            dropLocation,
                            job.input.clone()
                    );
                }
            }

            /*
             * Anything already finished gets returned as the finished
             * product.
             */
            for (ItemStack output : outputQueue) {
                if (output != null &&
                        output.getAmount() > 0) {
                    world.dropItemNaturally(
                            dropLocation,
                            output.clone()
                    );
                }
            }

            jobs.clear();
            outputQueue.clear();
        }
    }

    /*
     * ============================================================
     * DATA CLASSES
     * ============================================================
     */

    private static class SmeltJob {
        private final ItemStack input;
        private final ItemStack resultPerItem;
        private final long finishTick;

        private SmeltJob(
                ItemStack input,
                ItemStack resultPerItem,
                long finishTick
        ) {
            this.input = input;
            this.resultPerItem = resultPerItem;
            this.finishTick = finishTick;
        }
    }

    private static class Structure {
        private final Location center;
        private final Location furnace;
        private final BlockFace outputFace;

        private Structure(
                Location center,
                Location furnace,
                BlockFace outputFace
        ) {
            this.center = center.clone();
            this.furnace = furnace.clone();
            this.outputFace = outputFace;
        }

        private boolean contains(Location location) {
            if (location == null ||
                    location.getWorld() == null ||
                    center.getWorld() == null) {
                return false;
            }

            if (!location.getWorld().getUID().equals(
                    center.getWorld().getUID()
            )) {
                return false;
            }

            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();

            int cx = center.getBlockX();
            int cy = center.getBlockY();
            int cz = center.getBlockZ();

            return x >= cx - 1 &&
                    x <= cx + 1 &&
                    y >= cy - 1 &&
                    y <= cy + 1 &&
                    z >= cz - 1 &&
                    z <= cz + 1;
        }
    }

    private String locationKey(Location location) {
        return location.getWorld().getUID() + ":" +
                location.getBlockX() + ":" +
                location.getBlockY() + ":" +
                location.getBlockZ();
    }

    private Smelter findSmelterContaining(Location location) {
        for (Smelter smelter : smelters.values()) {
            if (smelter.contains(location)) {
                return smelter;
            }
        }

        return null;
    }
}