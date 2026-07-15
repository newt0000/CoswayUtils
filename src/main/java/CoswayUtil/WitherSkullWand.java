package CoswayUtil;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Collections;

public class WitherSkullWand implements Listener {

    private final CoswayUtil plugin;

    private final NamespacedKey wandKey;


    public WitherSkullWand(CoswayUtil plugin) {
        this.plugin = plugin;
        this.wandKey = new NamespacedKey(plugin, "wither_skull_wand");
    }


    /**
     * Creates the Wither Skull Wand item
     */
    public static ItemStack getWitherSkullWand() {

        ItemStack item = new ItemStack(Material.CARROT_ON_A_STICK);

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            meta.setDisplayName(
                    ChatColor.DARK_BLUE + "Wither Skull Launcher"
            );

            meta.setLore(Collections.singletonList(
                    ChatColor.GRAY + "Right click to fire a blue wither skull"
            ));

            item.setItemMeta(meta);
        }


        return item;
    }



    /**
     * Gives the item a persistent identifier
     */
    private ItemStack tagItem(ItemStack item) {

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            meta.getPersistentDataContainer().set(
                    wandKey,
                    PersistentDataType.BYTE,
                    (byte) 1
            );

            item.setItemMeta(meta);
        }

        return item;
    }



    /**
     * Gets a properly tagged wand
     * Use this for BlockShop / knowledge book rewards
     */
    public ItemStack createWand() {

        return tagItem(getWitherSkullWand());
    }



    private boolean isWand(ItemStack item) {

        if (item == null || item.getType() != Material.CARROT_ON_A_STICK) {
            return false;
        }


        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }


        return meta.getPersistentDataContainer()
                .has(wandKey, PersistentDataType.BYTE);
    }




    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }


        Player player = event.getPlayer();


        if (!isWand(player.getInventory().getItemInMainHand())) {
            return;
        }


        if (!(event.getAction().isRightClick())) {
            return;
        }


        event.setCancelled(true);


        shootSkull(player);


        // carrot on stick durability behavior
        player.swingMainHand();
    }




    private void shootSkull(Player player) {


        Location eye = player.getEyeLocation();


        WitherSkull skull =
                (WitherSkull) player.getWorld()
                        .spawnEntity(
                                eye.add(
                                        eye.getDirection()
                                                .multiply(1.2)
                                ),
                                EntityType.WITHER_SKULL
                        );


        Vector direction =
                player.getEyeLocation()
                        .getDirection()
                        .normalize();


        skull.setVelocity(
                direction.multiply(6.5)
        );


        // Make it a blue charged style skull
        skull.setCharged(true);
        skull.setYield(
                (float) plugin.getConfig()
                        .getDouble("wither-skull-launcher.explosion-power", 2.5)
        );



        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_WITHER_SHOOT,
                1.0f,
                1.5f
        );


        player.getWorld().spawnParticle(
                Particle.SOUL_FIRE_FLAME,
                player.getEyeLocation()
                        .add(direction),
                10,
                0.1,
                0.1,
                0.1,
                0.05
        );
    }
}