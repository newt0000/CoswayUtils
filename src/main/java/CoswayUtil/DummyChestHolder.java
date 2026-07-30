package CoswayUtil;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.entity.Mannequin;

public class DummyChestHolder implements InventoryHolder {

    private final Mannequin mannequin;


    public DummyChestHolder(Mannequin mannequin) {
        this.mannequin = mannequin;
    }


    public Mannequin getMannequin() {
        return mannequin;
    }


    @Override
    public Inventory getInventory() {
        return null;
    }
}