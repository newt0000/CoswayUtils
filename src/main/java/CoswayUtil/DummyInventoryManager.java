package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DummyInventoryManager {

    private final CoswayUtil plugin;

    private File folder;
    private File file;
    private YamlConfiguration config;


    public DummyInventoryManager(CoswayUtil plugin) {

        this.plugin = plugin;

        folder = new File(
                plugin.getDataFolder(),
                "dummys"
        );

        if (!folder.exists()) {
            folder.mkdirs();
        }


        file = new File(
                folder,
                "inventories.yml"
        );


        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        config = YamlConfiguration.loadConfiguration(file);
    }



    public Inventory loadInventory(Mannequin mannequin) {

        Inventory inventory = Bukkit.createInventory(
                new DummyChestHolder(mannequin),
                27,
                "Dummy Inventory"
        );


        String path =
                "dummies."
                        + mannequin.getUniqueId()
                        + ".contents";


        if (config.contains(path)) {

            for (String slot : config.getConfigurationSection(path).getKeys(false)) {

                ItemStack item =
                        config.getItemStack(
                                path + "." + slot
                        );


                if (item != null) {

                    inventory.setItem(
                            Integer.parseInt(slot),
                            item
                    );
                }
            }
        }


        return inventory;
    }



    public void saveInventory(
            Mannequin mannequin,
            Inventory inventory
    ) {


        String path =
                "dummies."
                        + mannequin.getUniqueId()
                        + ".contents";


        config.set(path, null);


        for (int i = 0; i < inventory.getSize(); i++) {

            ItemStack item =
                    inventory.getItem(i);


            if (item != null) {

                config.set(
                        path + "." + i,
                        item
                );
            }
        }


        save();
    }



    private void save() {

        try {

            config.save(file);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}