package CoswayUtil;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.entity.Mannequin;

public class DummyInventoryCloseListener implements Listener {

    private final DummyInventoryManager inventoryManager;


    public DummyInventoryCloseListener(
            DummyInventoryManager inventoryManager
    ) {

        this.inventoryManager = inventoryManager;
    }


    @EventHandler
    public void onClose(InventoryCloseEvent event) {


        if (!(event.getInventory().getHolder()
                instanceof DummyChestHolder holder)) {
            return;
        }

        Mannequin mannequin = holder.getMannequin();


        inventoryManager.saveInventory(
                mannequin,
                event.getInventory()
        );
    }
}