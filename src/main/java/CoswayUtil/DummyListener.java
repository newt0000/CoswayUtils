package CoswayUtil;

import org.bukkit.Material;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class DummyListener implements Listener {


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getInventory().getHolder()
                instanceof DummyInventoryHolder holder)) {
            return;
        }


        /*
         * Block the filler slots.
         */
        if (event.getRawSlot() >= 6 &&
                event.getRawSlot() <= 8) {

            event.setCancelled(true);
            return;
        }


        /*
         * Prevent shift-clicking items into the GUI.
         * This avoids bypassing the locked slots.
         */
        if (event.isShiftClick()) {

            event.setCancelled(true);
        }
    }



    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (!(event.getInventory().getHolder()
                instanceof DummyInventoryHolder holder)) {
            return;
        }


        Mannequin mannequin =
                holder.getMannequin();


        if (mannequin == null ||
                mannequin.isDead()) {
            return;
        }


        ItemStack helmet =
                event.getInventory()
                        .getItem(0);

        ItemStack chest =
                event.getInventory()
                        .getItem(1);

        ItemStack legs =
                event.getInventory()
                        .getItem(2);

        ItemStack boots =
                event.getInventory()
                        .getItem(3);

        ItemStack main =
                event.getInventory()
                        .getItem(4);

        ItemStack off =
                event.getInventory()
                        .getItem(5);



        mannequin.getEquipment()
                .setHelmet(helmet);


        mannequin.getEquipment()
                .setChestplate(chest);


        mannequin.getEquipment()
                .setLeggings(legs);


        mannequin.getEquipment()
                .setBoots(boots);


        mannequin.getEquipment()
                .setItemInMainHand(main);


        mannequin.getEquipment()
                .setItemInOffHand(off);
    }
}