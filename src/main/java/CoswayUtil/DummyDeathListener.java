package CoswayUtil;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DummyDeathListener implements Listener {

    private final DummyManager dummyManager;
    private final DummyInventoryManager inventoryManager;


    public DummyDeathListener(
            DummyManager dummyManager,
            DummyInventoryManager inventoryManager
    ) {
        this.dummyManager = dummyManager;
        this.inventoryManager = inventoryManager;
    }


    @EventHandler
    public void onDummyDeath(EntityDeathEvent event) {

        Entity entity = event.getEntity();


        if (!(entity instanceof Mannequin mannequin)) {
            return;
        }


        if (!dummyManager.isDummy(mannequin)) {
            return;
        }


        /*
         * Only handle chest inventories
         */
        Inventory inventory =
                inventoryManager.loadInventory(mannequin);


        if (!(inventory.getHolder() instanceof DummyChestHolder)) {
            return;
        }


        Location location = mannequin.getLocation();


        /*
         * Remove vanilla drops
         */
        event.getDrops().clear();
        event.setDroppedExp(0);


        /*
         * Drop stored dummy chest contents
         */
        for (ItemStack item : inventory.getContents()) {

            if (item == null) {
                continue;
            }

            location.getWorld()
                    .dropItemNaturally(
                            location,
                            item
                    );
        }


        /*
         * Clear saved inventory after dropping
         */
        inventoryManager.clearInventory(mannequin);
    }
}