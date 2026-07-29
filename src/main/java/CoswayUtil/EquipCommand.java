package CoswayUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

public class EquipCommand implements CommandExecutor {

    private final DummyManager dummyManager;


    public EquipCommand(DummyManager dummyManager) {
        this.dummyManager = dummyManager;
    }


    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {


        if (!(sender instanceof Player player)) {

            sender.sendMessage(
                    ChatColor.RED +
                            "Only players can use this command."
            );

            return true;
        }


        Entity target = getTarget(player);


        if (!(target instanceof Mannequin mannequin)) {

            player.sendMessage(
                    ChatColor.RED +
                            "You must be looking at a mannequin."
            );

            return true;
        }


        if (!dummyManager.isDummy(mannequin)) {

            player.sendMessage(
                    ChatColor.RED +
                            "That is not a CoswayUtil dummy."
            );

            return true;
        }


        openEquipGUI(player, mannequin);


        return true;
    }



    private void openEquipGUI(
            Player player,
            Mannequin mannequin
    ) {


        DummyInventoryHolder holder =
                new DummyInventoryHolder(
                        mannequin
                );


        Inventory inventory =
                org.bukkit.Bukkit.createInventory(
                        holder,
                        9,
                        ChatColor.DARK_GRAY +
                                "Dummy Equipment"
                );


        holder.setInventory(inventory);



        /*
         * Equipment layout:
         *
         * 0 Helmet
         * 1 Chestplate
         * 2 Leggings
         * 3 Boots
         * 4 Main Hand
         * 5 Off Hand
         *
         * 6-8 Locked
         */


        inventory.setItem(
                0,
                mannequin.getEquipment()
                        .getHelmet()
        );


        inventory.setItem(
                1,
                mannequin.getEquipment()
                        .getChestplate()
        );


        inventory.setItem(
                2,
                mannequin.getEquipment()
                        .getLeggings()
        );


        inventory.setItem(
                3,
                mannequin.getEquipment()
                        .getBoots()
        );


        inventory.setItem(
                4,
                mannequin.getEquipment()
                        .getItemInMainHand()
        );


        inventory.setItem(
                5,
                mannequin.getEquipment()
                        .getItemInOffHand()
        );


        ItemStack filler =
                new ItemStack(
                        Material.BLACK_STAINED_GLASS_PANE
                );


        for (int i = 6; i < 9; i++) {

            inventory.setItem(
                    i,
                    filler
            );
        }


        player.openInventory(inventory);
    }




    private Entity getTarget(Player player) {


        RayTraceResult result =
                player.getWorld()
                        .rayTraceEntities(
                                player.getEyeLocation(),
                                player.getEyeLocation()
                                        .getDirection(),
                                5,
                                entity ->
                                        entity instanceof Mannequin
                        );


        if (result == null)
            return null;


        return result.getHitEntity();
    }
}