package CoswayUtil;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MageCommand implements CommandExecutor, TabCompleter {


    private final CoswayUtil plugin;


    public MageCommand(CoswayUtil plugin) {
        this.plugin = plugin;
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



        if (!player.hasPermission("coswayutil.mage")) {

            player.sendMessage(
                    ChatColor.RED +
                            "You do not have permission."
            );

            return true;
        }




        if (args.length < 2) {

            player.sendMessage(
                    ChatColor.YELLOW +
                            "Usage: /mage <enchantment> <level>"
            );

            return true;
        }



        ItemStack item =
                player.getInventory()
                        .getItemInMainHand();



        if (item.getType().isAir()) {

            player.sendMessage(
                    ChatColor.RED +
                            "You must be holding an item."
            );

            return true;
        }



        Enchantment enchant =
                getEnchantment(args[0]);



        if (enchant == null) {

            player.sendMessage(
                    ChatColor.RED +
                            "Unknown enchantment: " +
                            args[0]
            );

            return true;
        }




        int level;

        try {

            level =
                    Integer.parseInt(args[1]);

        } catch (NumberFormatException e) {

            player.sendMessage(
                    ChatColor.RED +
                            "Level must be a number."
            );

            return true;
        }



        if (level < 1) {

            player.sendMessage(
                    ChatColor.RED +
                            "Level must be at least 1."
            );

            return true;
        }




        item.addUnsafeEnchantment(
                enchant,
                level
        );



        player.sendMessage(
                ChatColor.GREEN +
                        "Applied " +
                        enchant.getKey().getKey() +
                        " level " +
                        level +
                        " to your item."
        );


        return true;
    }







    private Enchantment getEnchantment(String input) {


        input =
                input.toLowerCase()
                        .replace("minecraft:", "");



        for (Enchantment enchantment :
                Enchantment.values()) {


            if (enchantment.getKey()
                    .getKey()
                    .equalsIgnoreCase(input)) {

                return enchantment;
            }
        }


        return null;
    }








    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {


        if (args.length == 1) {


            List<String> list =
                    new ArrayList<>();


            for (Enchantment enchant :
                    Enchantment.values()) {


                list.add(
                        enchant.getKey()
                                .getKey()
                );
            }


            return list;
        }



        if (args.length == 2) {

            return Arrays.asList(
                    "1",
                    "2",
                    "3",
                    "4",
                    "5",
                    "10",
                    "50",
                    "100",
                    "255"
            );
        }



        return null;
    }
}