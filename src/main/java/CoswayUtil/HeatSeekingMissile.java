package CoswayUtil;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;
import java.util.Collections;


public class HeatSeekingMissile implements Listener {


    private final CoswayUtil plugin;

    private final NamespacedKey missileKey;



    public HeatSeekingMissile(CoswayUtil plugin) {

        this.plugin = plugin;

        this.missileKey =
                new NamespacedKey(
                        plugin,
                        "heat_seeking_missile"
                );
    }




    public ItemStack getMissileLauncher() {


        ItemStack item =
                new ItemStack(
                        Material.CARROT_ON_A_STICK
                );


        ItemMeta meta =
                item.getItemMeta();


        if(meta != null) {


            meta.setDisplayName(
                    ChatColor.RED +
                            "Heat Seeking Missile Launcher"
            );


            meta.setLore(
                    Collections.singletonList(
                            ChatColor.GRAY +
                                    "Launches a guided missile"
                    )
            );


            meta.getPersistentDataContainer()
                    .set(
                            missileKey,
                            PersistentDataType.BYTE,
                            (byte)1
                    );


            item.setItemMeta(meta);
        }


        return item;
    }






    private boolean isLauncher(ItemStack item) {


        if(item == null ||
                item.getType() != Material.CARROT_ON_A_STICK)
            return false;



        ItemMeta meta =
                item.getItemMeta();


        if(meta == null)
            return false;



        return meta.getPersistentDataContainer()
                .has(
                        missileKey,
                        PersistentDataType.BYTE
                );
    }







    @EventHandler
    public void onUse(PlayerInteractEvent event) {


        if(event.getHand() != EquipmentSlot.HAND)
            return;


        if(!event.getAction().isRightClick())
            return;



        Player player =
                event.getPlayer();



        if(!isLauncher(
                player.getInventory()
                        .getItemInMainHand()
        ))
            return;



        event.setCancelled(true);


        launch(player);
    }







    private void launch(Player player) {


        Location start =
                player.getEyeLocation()
                        .add(
                                player.getLocation()
                                        .getDirection()
                                        .multiply(1.2)
                        );



        Arrow missile =
                player.getWorld()
                        .spawn(
                                start,
                                Arrow.class,
                                arrow -> {

                                    arrow.setShooter(player);

                                    arrow.setGravity(false);

                                    arrow.setSilent(true);

                                    arrow.setInvulnerable(true);

                                    arrow.setPickupStatus(
                                            AbstractArrow.PickupStatus.DISALLOWED
                                    );
                                }
                        );



        missile.setGravity(false);

        missile.setSilent(true);

        missile.setInvulnerable(true);


        missile.setPickupStatus(
                AbstractArrow.PickupStatus.DISALLOWED
        );



        missile.setVelocity(
                player.getEyeLocation()
                        .getDirection()
                        .normalize()
                        .multiply(
                                getSpeed()
                        )
        );



        trackMissile(
                missile,
                player
        );



        player.getWorld()
                .playSound(
                        player.getLocation(),
                        Sound.ENTITY_FIREWORK_ROCKET_LAUNCH,
                        1,
                        0.8f
                );
    }









    private void trackMissile(
            Arrow missile,
            Player shooter
    ) {


        new BukkitRunnable() {


            LivingEntity target = null;

            int age = 0;
            int stuckTimer = 0;
            int waterTimer = 0;

            boolean waterEscape = false;

            int escapeTicks = 0;
            int retargetTimer = 0;

            Location lastLocation =
                    missile.getLocation();





            @Override
            public void run() {


                if(missile.isDead()) {

                    cancel();
                    return;
                }


                age++;


                if(age > getMaxLife()) {

                    missile.remove();

                    cancel();

                    return;
                }



                Location location =
                        missile.getLocation();



                location.getWorld()
                        .spawnParticle(
                                Particle.FLAME,
                                location,
                                4,
                                0.05,
                                0.05,
                                0.05,
                                0.01
                        );



                /*
                 * Detect if missile is stuck
                 */
                /*
                 * Water recovery system
                 */

                if(location.getBlock().isLiquid()) {

                    waterTimer++;

                }
                else {

                    waterTimer = 0;

                    if(waterEscape) {

                        waterEscape = false;

                        escapeTicks = 0;
                    }
                }





                if(waterTimer >= getWaterRecoveryDelay()
                        || waterEscape) {



                    waterEscape = true;

                    escapeTicks++;



                    Vector direction =
                            missile.getVelocity();



                    if(direction.lengthSquared() < 0.2) {

                        direction =
                                shooter.getLocation()
                                        .getDirection();
                    }



                    /*
                     * Force missile upward and forward
                     */

                    Vector escape =
                            direction.normalize()
                                    .multiply(
                                            getWaterRecoveryBoost()
                                    );



                    escape.setY(2.5);



                    missile.setVelocity(
                            escape
                    );



                    /*
                     * Move projectile out of water
                     * instead of repeatedly fighting fluid
                     */

                    missile.teleport(
                            location.clone()
                                    .add(
                                            0,
                                            0.7,
                                            0
                                    )
                    );



                    target = null;


                    retargetTimer =
                            getRetargetDelay();





                    location.getWorld()
                            .spawnParticle(
                                    Particle.BUBBLE_COLUMN_UP,
                                    location,
                                    15,
                                    .3,
                                    .3,
                                    .3,
                                    .1
                            );





                    /*
                     * Once above water resume hunting
                     */

                    if(
                            !location.getBlock()
                                    .isLiquid()
                                    &&
                                    escapeTicks > 5
                    ) {

                        waterEscape = false;

                        waterTimer = 0;
                    }
                }
                if(location.distanceSquared(
                        lastLocation
                ) < 0.01) {

                    stuckTimer++;

                } else {

                    stuckTimer = 0;
                }


                lastLocation =
                        location.clone();




                /*
                 * Force recovery if stuck
                 */

                if(stuckTimer >=
                        getStuckDelay()) {


                    Vector velocity =
                            missile.getVelocity();


                    if(velocity.lengthSquared() < 0.1) {

                        velocity =
                                shooter.getLocation()
                                        .getDirection();

                    }


                    missile.setVelocity(
                            velocity.normalize()
                                    .multiply(
                                            getSpeed()
                                                    +
                                                    getRecoveryBoost()
                                    )
                    );


                    target = null;


                    stuckTimer = 0;
                }





                /*
                 * Periodic retargeting
                 */

                retargetTimer++;


                if(target == null ||
                        target.isDead() ||
                        !target.isValid() ||
                        retargetTimer >= getRetargetDelay()) {


                    target =
                            findTarget(
                                    missile,
                                    shooter
                            );


                    retargetTimer = 0;
                }






                /*
                 * Tracking logic
                 */

                if(target != null) {


                    Vector desired =
                            target.getLocation()
                                    .add(
                                            0,
                                            1,
                                            0
                                    )
                                    .toVector()
                                    .subtract(
                                            location.toVector()
                                    )
                                    .normalize();



                    Vector current =
                            missile.getVelocity()
                                    .normalize();




                    Vector steering =
                            current.multiply(
                                            1 - getSteering()
                                    )
                                    .add(
                                            desired.multiply(
                                                    getSteering()
                                            )
                                    )
                                    .normalize();




                    missile.setVelocity(
                            steering.multiply(
                                    getSpeed()
                            )
                    );




                    if(location.distanceSquared(
                            target.getLocation()
                    ) <= 9) {


                        explode(location,shooter);


                        missile.remove();


                        cancel();

                        return;
                    }

                }

                else {


                    /*
                     * No target:
                     * keep hunting forward
                     */

                    Vector velocity =
                            missile.getVelocity();



                    if(velocity.lengthSquared() < 0.2) {


                        velocity =
                                shooter.getLocation()
                                        .getDirection();

                    }



                    missile.setVelocity(
                            velocity.normalize()
                                    .multiply(
                                            getSpeed()
                                    )
                    );
                }
            }


        }.runTaskTimer(
                plugin,
                1,
                1
        );
    }







    @EventHandler
    public void onMissileHit(EntityDamageByEntityEvent event) {


        if(!(event.getDamager() instanceof Arrow arrow))
            return;



        if(!(arrow.getShooter() instanceof Player player))
            return;



        event.setDamage(event.getDamage());


    }

    private LivingEntity findTarget(
            Arrow missile,
            Player shooter
    ) {



        FileConfiguration config =
                plugin.getConfig();



        if(!config.getBoolean(
                "heat-seeking-missile.targets.enabled",
                true
        )) {

            return null;
        }



        int range =
                config.getInt(
                        "heat-seeking-missile.tracking.range",
                        30
                );



        List<String> allowed =
                config.getStringList(
                        "heat-seeking-missile.targets.entities"
                );





        return missile.getNearbyEntities(
                        range,
                        range,
                        range
                )
                .stream()


                .filter(
                        e -> e instanceof LivingEntity
                )


                .filter(
                        e -> e != shooter
                )


                .map(
                        e -> (LivingEntity)e
                )


                .filter(
                        e -> allowed.contains(
                                e.getType().name()
                        )
                )


                .filter(
                        e -> isInFront(
                                missile,
                                e
                        )
                )


                .filter(
                        e -> hasLineOfSight(
                                missile.getLocation(),
                                e.getLocation()
                        )
                )


                .min(
                        Comparator.comparingDouble(
                                e ->
                                        e.getLocation()
                                                .distanceSquared(
                                                        missile.getLocation()
                                                )
                        )
                )


                .orElse(null);
    }









    private boolean isInFront(
            Arrow missile,
            Entity entity
    ) {


        Vector direction =
                missile.getVelocity()
                        .normalize();



        Vector toEntity =
                entity.getLocation()
                        .toVector()
                        .subtract(
                                missile.getLocation()
                                        .toVector()
                        )
                        .normalize();



        return direction.dot(toEntity) > 0.2;
    }









    private boolean hasLineOfSight(
            Location from,
            Location to
    ) {


        return from.getWorld()
                .rayTraceBlocks(
                        from,
                        to.toVector()
                                .subtract(
                                        from.toVector()
                                )
                                .normalize(),
                        from.distance(to)
                ) == null;
    }









    private void explode(Location location, Player shooter) {

        FileConfiguration config =
                plugin.getConfig();


        boolean terrain =
                config.getBoolean(
                        "heat-seeking-missile.explosion.fiery-explosion.enabled",
                        true
                );


        float power =
                (float) config.getDouble(
                        "heat-seeking-missile.explosion.power",
                        8
                );



        /*
         * Damage entities as player-caused damage
         */
        double damage =
                config.getDouble(
                        "heat-seeking-missile.explosion.entity-damage",
                        20
                );



        for(Entity entity : location.getWorld().getNearbyEntities(
                location,
                4,
                4,
                4
        )) {


            if(!(entity instanceof LivingEntity living))
                continue;


            if(entity == shooter)
                continue;



            living.damage(
                    damage,
                    shooter
            );
        }




        /*
         * Terrain explosion
         */

        if(terrain) {


            location.getWorld()
                    .createExplosion(
                            location,
                            power,
                            false,
                            true
                    );

        }




        /*
         * Rainbow firework visual
         */

        if(config.getBoolean(
                "heat-seeking-missile.explosion.rainbow-firework.enabled",
                true
        )) {


            Firework firework =
                    location.getWorld()
                            .spawn(
                                    location,
                                    Firework.class
                            );


            FireworkMeta meta =
                    firework.getFireworkMeta();



            meta.addEffect(
                    FireworkEffect.builder()
                            .with(
                                    FireworkEffect.Type.BALL_LARGE
                            )
                            .withColor(
                                    Color.RED,
                                    Color.ORANGE,
                                    Color.YELLOW,
                                    Color.GREEN,
                                    Color.BLUE,
                                    Color.PURPLE
                            )
                            .trail(true)
                            .flicker(true)
                            .build()
            );


            firework.setFireworkMeta(meta);



            new BukkitRunnable(){

                @Override
                public void run(){

                    if(!firework.isDead())
                        firework.detonate();

                }

            }.runTaskLater(
                    plugin,
                    1
            );
        }



        location.getWorld()
                .playSound(
                        location,
                        Sound.ENTITY_GENERIC_EXPLODE,
                        2,
                        .5f
                );
    }






    private double getSpeed() {

        return plugin.getConfig()
                .getDouble(
                        "heat-seeking-missile.tracking.speed",
                        1.7
                );
    }



    private double getSteering() {

        return plugin.getConfig()
                .getDouble(
                        "heat-seeking-missile.tracking.steering-strength",
                        0.45
                );
    }

    private int getRetargetDelay() {

        return plugin.getConfig()
                .getInt(
                        "heat-seeking-missile.tracking.retarget-delay",
                        20
                );
    }



    private int getStuckDelay() {

        return plugin.getConfig()
                .getInt(
                        "heat-seeking-missile.tracking.stuck-check-delay",
                        10
                );
    }


    private int getWaterRecoveryDelay() {

        return plugin.getConfig()
                .getInt(
                        "heat-seeking-missile.tracking.water-recovery-delay",
                        20
                );
    }



    private double getWaterRecoveryBoost() {

        return plugin.getConfig()
                .getDouble(
                        "heat-seeking-missile.tracking.water-recovery-boost",
                        3.0
                );
    }
    private double getRecoveryBoost() {

        return plugin.getConfig()
                .getDouble(
                        "heat-seeking-missile.tracking.recovery-boost",
                        1.2
                );
    }

    private int getMaxLife() {

        return plugin.getConfig()
                .getInt(
                        "heat-seeking-missile.tracking.max-life-ticks",
                        600
                );
    }

}