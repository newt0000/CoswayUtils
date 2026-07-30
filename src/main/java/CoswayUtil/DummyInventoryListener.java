package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DummyInventoryListener implements Listener {

    private final DummyManager dummyManager;
    private final DummyInventoryManager inventoryManager;

    public DummyInventoryListener(
            DummyManager dummyManager,
            DummyInventoryManager inventoryManager
    ) {

        this.dummyManager = dummyManager;
        this.inventoryManager = inventoryManager;
    }


    @EventHandler
    public void onDummyClick(PlayerInteractEntityEvent event) {

        if (!(event.getRightClicked() instanceof Mannequin mannequin)) {
            return;
        }


        if (!dummyManager.isDummy(mannequin)) {
            return;
        }


        event.setCancelled(true);


        Inventory inventory =
                inventoryManager.loadInventory(mannequin);


        event.getPlayer().openInventory(inventory);
    }
}