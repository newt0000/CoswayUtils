package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Comparator;

public class DummyAIManager {

    private final CoswayUtil plugin;
    private final DummyManager dummyManager;


    private final NamespacedKey followKey;
    private final NamespacedKey lookKey;
    private final NamespacedKey followTargetKey;


    public DummyAIManager(
            CoswayUtil plugin,
            DummyManager dummyManager
    ) {

        this.plugin = plugin;
        this.dummyManager = dummyManager;


        followKey =
                new NamespacedKey(
                        plugin,
                        "dummy_follow"
                );

        lookKey =
                new NamespacedKey(
                        plugin,
                        "dummy_look"
                );

        followTargetKey =
                new NamespacedKey(
                        plugin,
                        "dummy_follow_target"
                );


        startTask();
    }



    private void startTask() {


        new BukkitRunnable() {

            @Override
            public void run() {


                for (var world : Bukkit.getWorlds()) {


                    for (Mannequin mannequin :
                            world.getEntitiesByClass(Mannequin.class)) {


                        if (!dummyManager.isDummy(mannequin))
                            continue;



                        handleFollow(mannequin);

                        handleLook(mannequin);

                    }
                }


            }

        }.runTaskTimer(
                plugin,
                1L,
                1L
        );
    }





    private void handleFollow(Mannequin mannequin) {

        Boolean enabled = mannequin.getPersistentDataContainer().get(
                followKey,
                PersistentDataType.BOOLEAN
        );

        if (!Boolean.TRUE.equals(enabled)) {
            return;
        }


        String uuid = mannequin.getPersistentDataContainer().get(
                followTargetKey,
                PersistentDataType.STRING
        );

        if (uuid == null) {
            return;
        }


        Player target;

        try {
            target = Bukkit.getPlayer(java.util.UUID.fromString(uuid));
        } catch (IllegalArgumentException ex) {
            return;
        }


        if (target == null || !target.isOnline()) {
            mannequin.setVelocity(new Vector());
            return;
        }


        if (!target.getWorld().equals(mannequin.getWorld())) {
            mannequin.setVelocity(new Vector());
            return;
        }


        Location dummyLoc = mannequin.getLocation();
        Location targetLoc = target.getLocation();


        double distance = dummyLoc.distance(targetLoc);


        if (distance > 64.0) {
            mannequin.setVelocity(new Vector());
            return;
        }



        Vector direction = targetLoc.toVector()
                .subtract(dummyLoc.toVector());

        direction.setY(0);



        if (direction.lengthSquared() <= 0.0001) {
            return;
        }


        direction.normalize();



        // Rotate only - DO NOT TELEPORT
        mannequin.setRotation(
                dummyLoc.clone()
                        .setDirection(direction)
                        .getYaw(),
                dummyLoc.getPitch()
        );



        // Stop near player but preserve gravity
        if (distance <= 3.0) {

            Vector current = mannequin.getVelocity();

            mannequin.setVelocity(
                    new Vector(
                            0,
                            current.getY(),
                            0
                    )
            );

            return;
        }



        Vector velocity = direction.clone()
                .multiply(0.22);



        // Preserve gravity
        velocity.setY(
                mannequin.getVelocity().getY()
        );



        /*
         * Jump detection
         */

        Location ahead = mannequin.getLocation()
                .clone()
                .add(
                        direction.clone()
                                .multiply(0.5)
                );


        boolean blockAhead =
                ahead.getBlock()
                        .getType()
                        .isSolid();


        boolean spaceAbove =
                !ahead.clone()
                        .add(0,1,0)
                        .getBlock()
                        .getType()
                        .isSolid()
                        &&
                        !ahead.clone()
                                .add(0,2,0)
                                .getBlock()
                                .getType()
                                .isSolid();



        if (blockAhead
                && spaceAbove
                && mannequin.isOnGround()) {

            velocity.setY(0.42);
        }



        mannequin.setVelocity(velocity);
    }
    private void handleLook(Mannequin mannequin) {

        // Follow mode already controls rotation
        Boolean following = mannequin.getPersistentDataContainer().get(
                followKey,
                PersistentDataType.BOOLEAN
        );

        if (Boolean.TRUE.equals(following)) {
            return;
        }

        Boolean enabled = mannequin.getPersistentDataContainer().get(
                lookKey,
                PersistentDataType.BOOLEAN
        );

        if (!Boolean.TRUE.equals(enabled)) {
            return;
        }

        Player closest = mannequin.getLocation()
                .getNearbyPlayers(15)
                .stream()
                .min(Comparator.comparingDouble(
                        p -> p.getLocation().distanceSquared(mannequin.getLocation())
                ))
                .orElse(null);

        if (closest == null) {
            return;
        }

        Location loc = mannequin.getLocation();

        Vector direction = closest.getEyeLocation().toVector()
                .subtract(loc.clone().add(0, 1.6, 0).toVector());

        direction.setY(0);

        if (direction.lengthSquared() < 0.0001) {
            return;
        }

        direction.normalize();

        loc.setDirection(direction);

        mannequin.setRotation(
                loc.getYaw(),
                loc.getPitch()
        );
    }

}