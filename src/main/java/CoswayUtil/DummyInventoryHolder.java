package CoswayUtil;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.entity.Mannequin;

public class DummyInventoryHolder implements InventoryHolder {

    private final Mannequin mannequin;

    private Inventory inventory;


    public DummyInventoryHolder(Mannequin mannequin) {
        this.mannequin = mannequin;
    }


    public Mannequin getMannequin() {
        return mannequin;
    }


    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }


    @Override
    public Inventory getInventory() {
        return inventory;
    }
}