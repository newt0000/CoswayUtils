package CoswayUtil;

import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DummyManager {

    private final CoswayUtil plugin;

    private final NamespacedKey dummyKey;
    private final NamespacedKey ownerKey;
    private final Set<UUID> dummies = new HashSet<>();

    public DummyManager(CoswayUtil plugin) {
        this.plugin = plugin;

        dummyKey = new NamespacedKey(plugin, "dummy");
        ownerKey = new NamespacedKey(plugin, "dummy_owner");
    }


    /**
     * Creates a mannequin and applies a skin.
     */
    public Mannequin createDummy(Location location, SkinData skinData, UUID owner) {

        Mannequin mannequin = (Mannequin) location.getWorld()
                .spawnEntity(location, EntityType.MANNEQUIN);


        /*
         * Apply skin profile
         *
         * Paper's Mannequin supports player profiles.
         */
        if (skinData != null) {
            mannequin.setProfile(
                    skinData.getProfile()
            );
        }


        // Basic mannequin settings
        mannequin.setAI(true);
        mannequin.setGravity(true);
        mannequin.setInvulnerable(false);
        mannequin.setSilent(false);


        // Mark as our dummy
        mannequin.getPersistentDataContainer()
                .set(
                        dummyKey,
                        PersistentDataType.BYTE,
                        (byte) 1
                );


        if (owner != null) {

            mannequin.getPersistentDataContainer()
                    .set(
                            ownerKey,
                            PersistentDataType.STRING,
                            owner.toString()
                    );
        }


        dummies.add(mannequin.getUniqueId());

        return mannequin;
    }


    /**
     * Checks if an entity is a CoswayUtil dummy.
     */
    public boolean isDummy(Mannequin mannequin) {

        return mannequin.getPersistentDataContainer()
                .has(
                        dummyKey,
                        PersistentDataType.BYTE
                );
    }


    /**
     * Remove tracking.
     */
    public void removeDummy(Mannequin mannequin) {

        dummies.remove(
                mannequin.getUniqueId()
        );

        mannequin.remove();
    }


    public Set<UUID> getDummies() {
        return dummies;
    }
}